<template>
  <section class="order-page">
    <div class="filter-card">
      <el-form :inline="true" :model="query" @submit.prevent="search">
        <el-form-item label="订单号"><el-input v-model="query.orderNo" clearable placeholder="输入订单号" @keyup.enter="search" /></el-form-item>
        <el-form-item label="用户 ID"><el-input v-model="query.userId" clearable placeholder="输入用户 ID" /></el-form-item>
        <el-form-item label="订单状态"><el-select v-model="query.status" clearable placeholder="全部状态" class="filter-select"><el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" :icon="Search" @click="search">查询</el-button><el-button :icon="Refresh" @click="reset">重置</el-button></el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-heading"><div><h2>订单列表</h2><p>可关闭待支付订单、对已支付订单发货，并确认已发货订单收货。</p></div><el-tag effect="plain">共 {{ total }} 笔订单</el-tag></div>
      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="190" />
        <el-table-column prop="userId" label="用户 ID" width="150" />
        <el-table-column label="商品" min-width="180"><template #default="{ row }"><span class="item-summary">{{ row.items?.[0]?.productName || '-' }}<template v-if="row.items?.length > 1"> 等 {{ row.items.length }} 件</template></span></template></el-table-column>
        <el-table-column label="金额" width="110"><template #default="{ row }">¥{{ Number(row.totalAmount).toFixed(2) }}</template></el-table-column>
        <el-table-column label="状态" width="110"><template #default="{ row }"><el-tag :type="statusType(row.status)" effect="light">{{ statusText(row.status) }}</el-tag></template></el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="235" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openDetail(row.id)">详情</el-button><el-button v-if="row.status === 0" link type="danger" @click="closeOrder(row)">关闭</el-button><el-button v-if="row.status === 1" link type="primary" @click="openShipDialog(row)">发货</el-button><el-button v-if="row.status === 2" link type="success" @click="receiveOrder(row)">确认收货</el-button></template></el-table-column>
      </el-table>
      <div class="pagination-wrap"><el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" :page-sizes="[10,20,50,100]" layout="total, sizes, prev, pager, next" @size-change="fetchOrders" @current-change="fetchOrders" /></div>
    </div>

    <el-drawer v-model="detailDrawer.visible" title="订单详情" size="680px" destroy-on-close>
      <template v-if="detailDrawer.order">
        <div class="detail-top"><div><span>订单号</span><strong>{{ detailDrawer.order.orderNo }}</strong></div><el-tag :type="statusType(detailDrawer.order.status)">{{ statusText(detailDrawer.order.status) }}</el-tag></div>
        <el-descriptions :column="1" border class="detail-block"><el-descriptions-item label="用户 ID">{{ detailDrawer.order.userId }}</el-descriptions-item><el-descriptions-item label="收货人">{{ detailDrawer.order.receiverName }} / {{ detailDrawer.order.receiverPhone }}</el-descriptions-item><el-descriptions-item label="收货地址">{{ detailDrawer.order.receiverAddress }}</el-descriptions-item><el-descriptions-item label="创建时间">{{ detailDrawer.order.createTime }}</el-descriptions-item><el-descriptions-item label="物流信息">{{ detailDrawer.order.shippingCompany ? `${detailDrawer.order.shippingCompany} · ${detailDrawer.order.trackingNo}` : '-' }}</el-descriptions-item><el-descriptions-item label="发货时间">{{ detailDrawer.order.shipTime || '-' }}</el-descriptions-item><el-descriptions-item label="收货时间">{{ detailDrawer.order.receiveTime || '-' }}</el-descriptions-item></el-descriptions>
        <h3>商品明细</h3><el-table :data="detailDrawer.order.items || []" size="small"><el-table-column prop="productName" label="商品" min-width="170" /><el-table-column prop="price" label="单价" width="100"><template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template></el-table-column><el-table-column prop="quantity" label="数量" width="70" /><el-table-column prop="totalPrice" label="小计" width="105"><template #default="{ row }">¥{{ Number(row.totalPrice).toFixed(2) }}</template></el-table-column></el-table>
        <div class="amount-total">订单总额：<strong>¥{{ Number(detailDrawer.order.totalAmount).toFixed(2) }}</strong></div>
      </template>
    </el-drawer>

    <el-dialog v-model="shipDialog.visible" title="订单发货" width="480px" destroy-on-close>
      <el-form ref="shipFormRef" :model="shipDialog.form" :rules="shipRules" label-width="82px"><el-form-item label="物流公司" prop="shippingCompany"><el-input v-model="shipDialog.form.shippingCompany" placeholder="如：顺丰速运" /></el-form-item><el-form-item label="运单号" prop="trackingNo"><el-input v-model="shipDialog.form.trackingNo" /></el-form-item></el-form>
      <template #footer><el-button @click="shipDialog.visible = false">取消</el-button><el-button type="primary" :loading="shipDialog.submitting" @click="submitShip">确认发货</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { closeAdminOrder, getAdminOrderDetail, getAdminOrderPage, receiveAdminOrder, shipAdminOrder } from '../api/adminOrder'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const shipFormRef = ref()
const query = reactive({ pageNum: 1, pageSize: 20, orderNo: '', userId: '', status: undefined })
const detailDrawer = reactive({ visible: false, order: null })
const shipDialog = reactive({ visible: false, submitting: false, orderId: null, form: { shippingCompany: '', trackingNo: '' } })
const statusOptions = [{ value: 0, label: '待支付' }, { value: 1, label: '已支付' }, { value: 2, label: '已发货' }, { value: 3, label: '已完成' }, { value: 4, label: '已取消' }]
const shipRules = { shippingCompany: [{ required: true, message: '请输入物流公司', trigger: 'blur' }], trackingNo: [{ required: true, message: '请输入运单号', trigger: 'blur' }] }
const statusText = (status) => ({ 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消' }[Number(status)] || '未知')
const statusType = (status) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'success', 4: 'info' }[Number(status)] || 'info')

const fetchOrders = async () => { loading.value = true; try { const params = { ...query, userId: query.userId || undefined }; const res = await getAdminOrderPage(params); records.value = res.data?.records || []; total.value = res.data?.total || 0 } finally { loading.value = false } }
const search = () => { query.pageNum = 1; fetchOrders() }
const reset = () => { Object.assign(query, { pageNum: 1, pageSize: 20, orderNo: '', userId: '', status: undefined }); fetchOrders() }
const openDetail = async (id) => { detailDrawer.visible = true; detailDrawer.order = null; const res = await getAdminOrderDetail(id); detailDrawer.order = res.data }
const closeOrder = async (order) => { await ElMessageBox.confirm(`确认关闭待支付订单“${order.orderNo}”吗？该操作会释放已锁定库存。`, '关闭订单', { type: 'warning', confirmButtonText: '确认关闭', cancelButtonText: '取消' }); await closeAdminOrder(order.id); ElMessage.success('订单已关闭，库存已释放'); await fetchOrders() }
const openShipDialog = (order) => { shipDialog.orderId = order.id; shipDialog.form = { shippingCompany: '', trackingNo: '' }; shipDialog.visible = true }
const submitShip = async () => { const valid = await shipFormRef.value.validate().catch(() => false); if (!valid) return; shipDialog.submitting = true; try { await shipAdminOrder(shipDialog.orderId, shipDialog.form); ElMessage.success('订单已发货'); shipDialog.visible = false; await fetchOrders() } finally { shipDialog.submitting = false } }
const receiveOrder = async (order) => { await ElMessageBox.confirm(`确认订单“${order.orderNo}”已收货吗？`, '确认收货', { type: 'warning', confirmButtonText: '确认收货', cancelButtonText: '取消' }); await receiveAdminOrder(order.id); ElMessage.success('订单已完成'); await fetchOrders() }

onMounted(fetchOrders)
</script>

<style scoped>
.order-page { max-width: 1280px; margin: 0 auto; }.filter-card, .table-card { background: #fff; border: 1px solid #e9edf4; border-radius: 14px; box-shadow: 0 3px 12px rgba(32,50,80,.025); }.filter-card { padding: 20px 22px 4px; }.filter-select { width: 140px; }.table-card { margin-top: 20px; overflow: hidden; }.table-heading { display: flex; align-items: center; justify-content: space-between; padding: 22px; }.table-heading h2 { margin: 0 0 7px; color: #273246; font-size: 17px; }.table-heading p { margin: 0; color: #99a3b5; font-size: 13px; }.item-summary { color: #4a5568; }.pagination-wrap { display: flex; justify-content: flex-end; padding: 18px 22px; border-top: 1px solid #edf0f5; }.detail-top { display: flex; align-items: flex-start; justify-content: space-between; padding-bottom: 18px; }.detail-top span, .detail-top strong { display: block; }.detail-top span { color: #98a2b3; font-size: 12px; }.detail-top strong { margin-top: 5px; color: #30394a; }.detail-block { margin-bottom: 24px; }.order-page h3 { margin: 20px 0 12px; font-size: 15px; color: #30394a; }.amount-total { margin-top: 16px; text-align: right; color: #667085; }.amount-total strong { color: #ed6a32; font-size: 19px; }@media (max-width:760px) { .filter-card :deep(.el-form-item) { margin-right: 0; }.table-heading { align-items: flex-start; gap: 12px; flex-direction: column; }.pagination-wrap { justify-content: center; overflow-x: auto; } }
</style>
