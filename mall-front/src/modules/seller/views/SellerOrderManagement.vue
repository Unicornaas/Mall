<template>
  <section class="order-page">
    <div class="filter-panel">
      <el-form :inline="true" :model="query" @submit.prevent="search">
        <el-form-item label="订单号"><el-input v-model="query.orderNo" clearable placeholder="请输入订单号" @keyup.enter="search" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" class="filter-select">
            <el-option v-for="option in statusOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-panel">
      <div class="table-heading">
        <div>
          <h2>店铺订单</h2>
          <p>每条记录只包含本店商品和本店独立物流信息。</p>
        </div>
      </div>

      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column label="订单号" min-width="180"><template #default="{ row }"><strong class="order-no">{{ row.orderNo }}</strong></template></el-table-column>
        <el-table-column label="商品" min-width="190"><template #default="{ row }"><span class="item-summary">{{ itemSummary(row.items) }}</span></template></el-table-column>
        <el-table-column label="收货人" min-width="125"><template #default="{ row }"><div>{{ row.receiverName }}</div><span class="muted-text">{{ row.receiverPhone }}</span></template></el-table-column>
        <el-table-column label="店铺金额" width="115" align="right"><template #default="{ row }"><strong class="amount">¥{{ formatAmount(row.sellerAmount) }}</strong></template></el-table-column>
        <el-table-column label="状态" width="105"><template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag></template></el-table-column>
        <el-table-column prop="paymentTime" label="支付时间" min-width="165"><template #default="{ row }">{{ row.paymentTime || '-' }}</template></el-table-column>
        <el-table-column prop="createTime" label="下单时间" min-width="165" />
        <el-table-column label="操作" width="135" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
            <el-button v-if="row.status === 1" link type="success" @click="openShipDialog(row)">发货</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无店铺订单" /></template>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchOrders"
          @current-change="fetchOrders"
        />
      </div>
    </div>

    <el-drawer v-model="detailDrawer.visible" title="店铺订单详情" size="760px" destroy-on-close>
      <template v-if="detailDrawer.order">
        <div class="detail-top">
          <div><span>订单号</span><strong>{{ detailDrawer.order.orderNo }}</strong></div>
          <el-tag :type="statusType(detailDrawer.order.status)">{{ statusText(detailDrawer.order.status) }}</el-tag>
        </div>
        <el-descriptions :column="1" border class="detail-block">
          <el-descriptions-item label="店铺金额">¥{{ formatAmount(detailDrawer.order.sellerAmount) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ detailDrawer.order.paymentTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detailDrawer.order.receiverName }} / {{ detailDrawer.order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ detailDrawer.order.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item label="买家备注">{{ detailDrawer.order.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="物流信息">{{ shipmentText(detailDrawer.order) }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ detailDrawer.order.shipTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货时间">{{ detailDrawer.order.receiveTime || '-' }}</el-descriptions-item>
        </el-descriptions>
        <h3>本店商品</h3>
        <el-table :data="detailDrawer.order.items || []" size="small">
          <el-table-column prop="productName" label="商品" min-width="185" />
          <el-table-column prop="price" label="单价" width="100"><template #default="{ row }">¥{{ formatAmount(row.price) }}</template></el-table-column>
          <el-table-column prop="quantity" label="数量" width="75" />
          <el-table-column prop="totalPrice" label="小计" width="110"><template #default="{ row }">¥{{ formatAmount(row.totalPrice) }}</template></el-table-column>
        </el-table>
      </template>
    </el-drawer>

    <el-dialog v-model="shipDialog.visible" title="店铺订单发货" width="480px" destroy-on-close>
      <p class="dialog-hint">发货后仅更新本店订单，不会影响同一主订单中的其他店铺。</p>
      <el-form ref="shipFormRef" :model="shipDialog.form" :rules="shipRules" label-width="82px" @submit.prevent="submitShip">
        <el-form-item label="物流公司" prop="shippingCompany"><el-input v-model="shipDialog.form.shippingCompany" placeholder="如：顺丰速运" /></el-form-item>
        <el-form-item label="运单号" prop="trackingNo"><el-input v-model="shipDialog.form.trackingNo" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="shipDialog.submitting" @click="submitShip">确认发货</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { getSellerOrderDetail, getSellerOrderPage, shipSellerOrder } from '../api/order'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const shipFormRef = ref()
const query = reactive({ pageNum: 1, pageSize: 20, orderNo: '', status: undefined })
const detailDrawer = reactive({ visible: false, order: null })
const shipDialog = reactive({ visible: false, submitting: false, order: null, form: { shippingCompany: '', trackingNo: '' } })
const statusOptions = [{ value: 0, label: '待支付' }, { value: 1, label: '待发货' }, { value: 2, label: '已发货' }, { value: 3, label: '已完成' }, { value: 4, label: '已取消' }]
const shipRules = { shippingCompany: [{ required: true, message: '请输入物流公司', trigger: 'blur' }], trackingNo: [{ required: true, message: '请输入运单号', trigger: 'blur' }] }

const statusText = (status) => ({ 0: '待支付', 1: '待发货', 2: '已发货', 3: '已完成', 4: '已取消' }[Number(status)] || '未知')
const statusType = (status) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'success', 4: 'info' }[Number(status)] || 'info')
const formatAmount = (value) => Number(value || 0).toFixed(2)
const itemSummary = (items) => (items || []).map(item => item.productName).join('、') || '-'
const shipmentText = (order) => order.shippingCompany ? `${order.shippingCompany} · ${order.trackingNo}` : '-'

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getSellerOrderPage(query)
    records.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const search = () => { query.pageNum = 1; fetchOrders() }
const reset = () => { Object.assign(query, { pageNum: 1, pageSize: 20, orderNo: '', status: undefined }); fetchOrders() }

const openDetail = async (sellerOrderId) => {
  detailDrawer.visible = true
  detailDrawer.order = null
  const res = await getSellerOrderDetail(sellerOrderId)
  detailDrawer.order = res.data
}

const openShipDialog = (order) => {
  shipDialog.order = order
  shipDialog.form = { shippingCompany: '', trackingNo: '' }
  shipDialog.visible = true
}

const submitShip = async () => {
  const valid = await shipFormRef.value.validate().catch(() => false)
  if (!valid) return
  await ElMessageBox.confirm(`确认发出订单“${shipDialog.order.orderNo}”中的本店商品吗？`, '确认发货', { type: 'warning', confirmButtonText: '确认发货', cancelButtonText: '取消' })
  shipDialog.submitting = true
  try {
    await shipSellerOrder(shipDialog.order.id, shipDialog.form)
    ElMessage.success('本店订单已发货')
    shipDialog.visible = false
    await fetchOrders()
    if (detailDrawer.order?.id === shipDialog.order.id) await openDetail(shipDialog.order.id)
  } finally {
    shipDialog.submitting = false
  }
}

onMounted(fetchOrders)
</script>

<style scoped>
.order-page { max-width: 1280px; margin: 0 auto; }
.filter-panel, .table-panel { background: #fff; border: 1px solid #e9edf4; border-radius: 8px; box-shadow: 0 3px 12px rgba(32, 50, 80, .025); }
.filter-panel { padding: 20px 22px 4px; }.filter-select { width: 140px; }.table-panel { margin-top: 18px; overflow: hidden; }
.table-heading { display: flex; align-items: center; justify-content: space-between; padding: 22px; }.table-heading h2 { margin: 0 0 7px; color: #273246; font-size: 17px; }.table-heading p, .dialog-hint { margin: 0; color: #99a3b5; font-size: 13px; line-height: 1.65; }
.order-no { color: #30394a; font-family: Consolas, monospace; font-size: 13px; }.item-summary { display: block; overflow: hidden; color: #4a5568; text-overflow: ellipsis; white-space: nowrap; }.muted-text { color: #98a2b3; font-size: 12px; }.amount { color: #e56a35; }.pagination-wrap { display: flex; justify-content: flex-end; padding: 18px 22px; border-top: 1px solid #edf0f5; }
.detail-top { display: flex; align-items: flex-start; justify-content: space-between; padding-bottom: 18px; }.detail-top span, .detail-top strong { display: block; }.detail-top span { color: #98a2b3; font-size: 12px; }.detail-top strong { margin-top: 5px; color: #30394a; }.detail-block { margin-bottom: 24px; }.order-page h3 { margin: 20px 0 12px; font-size: 15px; color: #30394a; }.dialog-hint { margin-bottom: 20px; }
@media (max-width: 760px) { .filter-panel :deep(.el-form-item) { margin-right: 0; }.pagination-wrap { justify-content: center; overflow-x: auto; } }
</style>
