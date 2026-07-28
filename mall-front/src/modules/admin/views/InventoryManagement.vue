<template>
  <section class="inventory-page">
    <div class="filter-card">
      <el-form :inline="true" :model="query" @submit.prevent="search">
        <el-form-item label="商品 / SKU">
          <el-input v-model="query.keyword" clearable placeholder="商品名、SKU 名或编码" @keyup.enter="search" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="query.warningOnly">仅看库存预警</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-heading">
        <div>
          <h2>库存列表</h2>
          <p>库存预警以可用库存小于或等于安全库存为准；锁定、扣减、释放由订单流程自动处理。</p>
        </div>
        <el-tag :type="warningCount ? 'warning' : 'success'" effect="light">预警 {{ warningCount }} 条</el-tag>
      </div>
      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column label="商品 / SKU" min-width="250">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image v-if="row.image" :src="row.image" fit="cover" class="product-image">
                <template #error><div class="image-fallback"><el-icon><Picture /></el-icon></div></template>
              </el-image>
              <div v-else class="image-fallback"><el-icon><Picture /></el-icon></div>
              <div><strong>{{ row.productName }}</strong><span>{{ row.skuName || '-' }} <template v-if="row.skuCode">· {{ row.skuCode }}</template></span></div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="skuId" label="SKU ID" min-width="165" />
        <el-table-column prop="totalStock" label="总库存" width="100" align="center" />
        <el-table-column prop="lockedStock" label="锁定库存" width="100" align="center">
          <template #default="{ row }"><span :class="{ 'locked-stock': row.lockedStock > 0 }">{{ row.lockedStock }}</span></template>
        </el-table-column>
        <el-table-column label="可用库存" width="112" align="center">
          <template #default="{ row }"><el-tag :type="row.warning ? 'danger' : 'success'" effect="light">{{ row.availableStock }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="safetyStock" label="安全库存" width="105" align="center" />
        <el-table-column label="状态" width="105" align="center">
          <template #default="{ row }"><el-tag :type="row.warning ? 'warning' : 'info'" effect="plain">{{ row.warning ? '库存预警' : '正常' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最近更新" min-width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openAddDialog(row)">补货</el-button>
            <el-button link @click="openLogs(row)">变动日志</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next" @size-change="fetchInventory" @current-change="fetchInventory" />
      </div>
    </div>

    <el-dialog v-model="addDialog.visible" title="库存补货" width="460px" destroy-on-close>
      <p class="dialog-hint">{{ addDialog.row?.productName }} / {{ addDialog.row?.skuName }}。本次补货会同步增加总库存和可用库存。</p>
      <el-form ref="addFormRef" :model="addDialog.form" :rules="addRules" label-width="78px" @submit.prevent="submitAdd">
        <el-form-item label="补货数量" prop="quantity"><el-input-number v-model="addDialog.form.quantity" :min="1" :max="999999" class="full-width" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="addDialog.visible = false">取消</el-button><el-button type="primary" :loading="addDialog.submitting" @click="submitAdd">确认补货</el-button></template>
    </el-dialog>

    <el-drawer v-model="logDrawer.visible" title="库存变动日志" size="700px" destroy-on-close>
      <div class="drawer-heading" v-if="logDrawer.row"><strong>{{ logDrawer.row.productName }} / {{ logDrawer.row.skuName }}</strong><span>SKU ID：{{ logDrawer.row.skuId }}</span></div>
      <el-table v-loading="logDrawer.loading" :data="logDrawer.records" size="small">
        <el-table-column prop="changeType" label="类型" width="110"><template #default="{ row }"><el-tag :type="logType(row.changeType).type" effect="light">{{ logType(row.changeType).label }}</el-tag></template></el-table-column>
        <el-table-column prop="changeCount" label="变动数量" width="105" align="center"><template #default="{ row }"><span :class="{ 'add-count': row.changeType === 'ADD' || row.changeType === 'INIT' }">{{ formatChange(row) }}</span></template></el-table-column>
        <el-table-column label="库存变化" min-width="140" align="center"><template #default="{ row }">{{ row.beforeStock }} → {{ row.afterStock }}</template></el-table-column>
        <el-table-column prop="orderNo" label="关联订单" min-width="160"><template #default="{ row }">{{ row.orderNo || '-' }}</template></el-table-column>
        <el-table-column prop="createTime" label="操作时间" min-width="165" />
      </el-table>
    </el-drawer>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, Refresh, Search } from '@element-plus/icons-vue'
import { addAdminInventory, getAdminInventoryLogs, getAdminInventoryPage } from '../api/adminInventory'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const addFormRef = ref()
const query = reactive({ pageNum: 1, pageSize: 20, keyword: '', warningOnly: false })
const addDialog = reactive({ visible: false, submitting: false, row: null, form: { quantity: 1 } })
const logDrawer = reactive({ visible: false, loading: false, row: null, records: [] })
const addRules = { quantity: [{ required: true, type: 'number', min: 1, message: '补货数量必须大于 0', trigger: 'change' }] }

const warningCount = computed(() => records.value.filter(item => item.warning).length)
const logTypes = {
  INIT: { label: '初始化', type: 'info' },
  LOCK: { label: '订单锁定', type: 'warning' },
  DEDUCT: { label: '支付扣减', type: 'danger' },
  RELEASE: { label: '库存释放', type: 'success' },
  ADD: { label: '人工补货', type: 'primary' },
}

const logType = (type) => logTypes[type] || { label: type || '未知', type: 'info' }
const formatChange = (row) => (row.changeType === 'ADD' || row.changeType === 'INIT' ? '+' : '-') + row.changeCount
const fetchInventory = async () => {
  loading.value = true
  try {
    const res = await getAdminInventoryPage(query)
    records.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}
const search = () => { query.pageNum = 1; fetchInventory() }
const reset = () => { Object.assign(query, { pageNum: 1, pageSize: 20, keyword: '', warningOnly: false }); fetchInventory() }
const openAddDialog = (row) => { addDialog.row = row; addDialog.form = { quantity: 1 }; addDialog.visible = true }
const submitAdd = async () => {
  const valid = await addFormRef.value.validate().catch(() => false)
  if (!valid) return
  addDialog.submitting = true
  try {
    await addAdminInventory(addDialog.row.skuId, addDialog.form)
    ElMessage.success('补货成功，库存已同步更新')
    addDialog.visible = false
    await fetchInventory()
  } finally {
    addDialog.submitting = false
  }
}
const openLogs = async (row) => {
  logDrawer.row = row
  logDrawer.records = []
  logDrawer.visible = true
  logDrawer.loading = true
  try {
    const res = await getAdminInventoryLogs(row.skuId)
    logDrawer.records = res.data || []
  } finally {
    logDrawer.loading = false
  }
}

onMounted(fetchInventory)
</script>

<style scoped>
.inventory-page { max-width: 1280px; margin: 0 auto; }
.filter-card, .table-card { background: #fff; border: 1px solid #e9edf4; border-radius: 14px; box-shadow: 0 3px 12px rgba(32, 50, 80, .025); }
.filter-card { padding: 20px 22px 4px; }.table-card { margin-top: 20px; overflow: hidden; }
.table-heading { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 22px; }.table-heading h2 { margin: 0 0 7px; color: #273246; font-size: 17px; }.table-heading p, .dialog-hint { margin: 0; color: #99a3b5; font-size: 13px; line-height: 1.65; }
.product-cell { display: flex; align-items: center; gap: 11px; }.product-image, .image-fallback { width: 42px; height: 42px; flex: 0 0 42px; border-radius: 8px; overflow: hidden; }.image-fallback { display: grid; place-items: center; background: #f1f4f9; color: #9eabc1; }.product-cell strong, .product-cell span { display: block; }.product-cell strong { color: #30394a; font-size: 13px; }.product-cell span { margin-top: 3px; color: #9ca5b4; font-size: 12px; }
.locked-stock { color: #c77a16; font-weight: 600; }.pagination-wrap { display: flex; justify-content: flex-end; padding: 18px 22px; border-top: 1px solid #edf0f5; }.full-width { width: 100%; }.dialog-hint { margin-bottom: 20px; }.drawer-heading { display: flex; justify-content: space-between; gap: 16px; margin-bottom: 18px; color: #4b5565; }.drawer-heading strong { color: #273246; }.drawer-heading span { color: #98a2b3; font-size: 13px; }.add-count { color: #25a26f; font-weight: 600; }
@media (max-width: 760px) { .filter-card :deep(.el-form-item) { margin-right: 0; }.table-heading { align-items: flex-start; flex-direction: column; }.pagination-wrap { justify-content: center; overflow-x: auto; }.drawer-heading { align-items: flex-start; flex-direction: column; gap: 4px; } }
</style>
