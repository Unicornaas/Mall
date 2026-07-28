<template>
  <section class="payment-admin-page">
    <el-tabs v-model="activeTab" class="manage-tabs">
      <el-tab-pane label="支付记录" name="payments">
        <div class="filter-card">
          <el-form :inline="true" :model="paymentQuery" @submit.prevent="searchPayments">
            <el-form-item label="订单号"><el-input v-model="paymentQuery.orderNo" clearable placeholder="输入订单号" @keyup.enter="searchPayments" /></el-form-item>
            <el-form-item label="支付状态"><el-select v-model="paymentQuery.payStatus" clearable placeholder="全部状态" class="filter-select"><el-option v-for="item in payStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
            <el-form-item><el-button type="primary" :icon="Search" @click="searchPayments">查询</el-button><el-button :icon="Refresh" @click="resetPayments">重置</el-button></el-form-item>
          </el-form>
        </div>
        <div class="table-card">
          <div class="table-heading"><div><h2>支付记录</h2><p>查看支付单、支付渠道和交易流水；支付状态由订单支付与退款流程自动维护。</p></div><el-tag effect="plain">共 {{ paymentTotal }} 笔</el-tag></div>
          <el-table v-loading="paymentLoading" :data="paymentRecords" stripe>
            <el-table-column prop="orderNo" label="订单号" min-width="190" /><el-table-column prop="userId" label="用户 ID" width="155" />
            <el-table-column label="金额" width="110"><template #default="{ row }">¥{{ amount(row.amount) }}</template></el-table-column>
            <el-table-column label="支付方式" width="105"><template #default="{ row }">{{ row.payType === 2 ? '微信支付' : '支付宝' }}</template></el-table-column>
            <el-table-column label="支付状态" width="115"><template #default="{ row }"><el-tag :type="payStatus(row.payStatus).type" effect="light">{{ payStatus(row.payStatus).label }}</el-tag></template></el-table-column>
            <el-table-column prop="tradeNo" label="交易流水号" min-width="195"><template #default="{ row }">{{ row.tradeNo || '-' }}</template></el-table-column>
            <el-table-column prop="payTime" label="支付时间" min-width="170"><template #default="{ row }">{{ row.payTime || '-' }}</template></el-table-column>
            <el-table-column prop="createTime" label="创建时间" min-width="170" />
          </el-table>
          <div class="pagination-wrap"><el-pagination v-model:current-page="paymentQuery.pageNum" v-model:page-size="paymentQuery.pageSize" :total="paymentTotal" :page-sizes="[10,20,50,100]" layout="total, sizes, prev, pager, next" @size-change="fetchPayments" @current-change="fetchPayments" /></div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="退款审批" name="refunds">
        <div class="rule-tip"><el-icon><InfoFilled /></el-icon><span>当前仅支持已支付、未发货订单的整单退款。审核通过后会取消订单并回补对应库存。</span></div>
        <div class="filter-card">
          <el-form :inline="true" :model="refundQuery" @submit.prevent="searchRefunds">
            <el-form-item label="订单号"><el-input v-model="refundQuery.orderNo" clearable placeholder="输入订单号" @keyup.enter="searchRefunds" /></el-form-item>
            <el-form-item label="退款状态"><el-select v-model="refundQuery.refundStatus" clearable placeholder="全部状态" class="filter-select"><el-option v-for="item in refundStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
            <el-form-item><el-button type="primary" :icon="Search" @click="searchRefunds">查询</el-button><el-button :icon="Refresh" @click="resetRefunds">重置</el-button></el-form-item>
          </el-form>
        </div>
        <div class="table-card">
          <div class="table-heading"><div><h2>退款申请</h2><p>审核结果、处理人和处理备注会保留在退款记录中。</p></div><el-tag type="warning" effect="light">本页待处理 {{ pendingRefundCount }} 笔</el-tag></div>
          <el-table v-loading="refundLoading" :data="refundRecords" stripe>
            <el-table-column prop="orderNo" label="订单号" min-width="190" /><el-table-column prop="userId" label="用户 ID" width="155" />
            <el-table-column label="退款金额" width="115"><template #default="{ row }">¥{{ amount(row.refundAmount) }}</template></el-table-column>
            <el-table-column prop="reason" label="申请原因" min-width="190"><template #default="{ row }">{{ row.reason || '-' }}</template></el-table-column>
            <el-table-column label="状态" width="112"><template #default="{ row }"><el-tag :type="refundStatus(row.refundStatus).type" effect="light">{{ refundStatus(row.refundStatus).label }}</el-tag></template></el-table-column>
            <el-table-column prop="processorId" label="处理人 ID" width="155"><template #default="{ row }">{{ row.processorId || '-' }}</template></el-table-column>
            <el-table-column prop="processRemark" label="处理备注" min-width="180"><template #default="{ row }">{{ row.processRemark || '-' }}</template></el-table-column>
            <el-table-column prop="processTime" label="处理时间" min-width="170"><template #default="{ row }">{{ row.processTime || '-' }}</template></el-table-column>
            <el-table-column label="操作" width="150" fixed="right"><template #default="{ row }"><template v-if="row.refundStatus === 0"><el-button link type="success" @click="openProcess(row, 1)">同意退款</el-button><el-button link type="danger" @click="openProcess(row, 2)">拒绝</el-button></template><span v-else class="muted">已处理</span></template></el-table-column>
          </el-table>
          <div class="pagination-wrap"><el-pagination v-model:current-page="refundQuery.pageNum" v-model:page-size="refundQuery.pageSize" :total="refundTotal" :page-sizes="[10,20,50,100]" layout="total, sizes, prev, pager, next" @size-change="fetchRefunds" @current-change="fetchRefunds" /></div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="processDialog.visible" :title="processDialog.status === 1 ? '同意退款' : '拒绝退款'" width="480px" destroy-on-close>
      <p class="dialog-hint">订单 {{ processDialog.row?.orderNo }}，退款金额 ¥{{ amount(processDialog.row?.refundAmount) }}。{{ processDialog.status === 1 ? '确认后将取消订单并回补库存。' : '请说明拒绝原因。' }}</p>
      <el-form ref="processFormRef" :model="processDialog.form" :rules="processRules" label-width="80px"><el-form-item label="处理备注" prop="processRemark"><el-input v-model="processDialog.form.processRemark" type="textarea" :rows="4" :placeholder="processDialog.status === 1 ? '可选：填写审核说明' : '必填：填写拒绝原因'" maxlength="500" show-word-limit /></el-form-item></el-form>
      <template #footer><el-button @click="processDialog.visible = false">取消</el-button><el-button :type="processDialog.status === 1 ? 'success' : 'danger'" :loading="processDialog.submitting" @click="submitProcess">确认{{ processDialog.status === 1 ? '退款' : '拒绝' }}</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled, Refresh, Search } from '@element-plus/icons-vue'
import { getAdminPaymentPage, getAdminRefundPage, processAdminRefund } from '../api/adminPayment'

const activeTab = ref('payments')
const paymentLoading = ref(false), refundLoading = ref(false)
const paymentRecords = ref([]), refundRecords = ref([])
const paymentTotal = ref(0), refundTotal = ref(0)
const processFormRef = ref()
const paymentQuery = reactive({ pageNum: 1, pageSize: 20, orderNo: '', payStatus: undefined })
const refundQuery = reactive({ pageNum: 1, pageSize: 20, orderNo: '', refundStatus: undefined })
const processDialog = reactive({ visible: false, submitting: false, row: null, status: 1, form: { processRemark: '' } })
const payStatusOptions = [{ value: 0, label: '待支付' }, { value: 1, label: '已支付' }, { value: 2, label: '已退款' }, { value: 3, label: '已关闭' }]
const refundStatusOptions = [{ value: 0, label: '待处理' }, { value: 1, label: '已退款' }, { value: 2, label: '已拒绝' }]
const processRules = { processRemark: [{ validator: (_, value, callback) => processDialog.status === 2 && !value?.trim() ? callback(new Error('请填写拒绝原因')) : callback(), trigger: 'blur' }] }
const pendingRefundCount = computed(() => refundRecords.value.filter(item => item.refundStatus === 0).length)
const amount = (value) => Number(value || 0).toFixed(2)
const payStatus = (status) => ({ 0: { label: '待支付', type: 'warning' }, 1: { label: '已支付', type: 'success' }, 2: { label: '已退款', type: 'info' }, 3: { label: '已关闭', type: 'info' } }[Number(status)] || { label: '未知', type: 'info' })
const refundStatus = (status) => ({ 0: { label: '待处理', type: 'warning' }, 1: { label: '已退款', type: 'success' }, 2: { label: '已拒绝', type: 'danger' } }[Number(status)] || { label: '未知', type: 'info' })
const fetchPayments = async () => { paymentLoading.value = true; try { const res = await getAdminPaymentPage(paymentQuery); paymentRecords.value = res.data?.records || []; paymentTotal.value = res.data?.total || 0 } finally { paymentLoading.value = false } }
const fetchRefunds = async () => { refundLoading.value = true; try { const res = await getAdminRefundPage(refundQuery); refundRecords.value = res.data?.records || []; refundTotal.value = res.data?.total || 0 } finally { refundLoading.value = false } }
const searchPayments = () => { paymentQuery.pageNum = 1; fetchPayments() }
const searchRefunds = () => { refundQuery.pageNum = 1; fetchRefunds() }
const resetPayments = () => { Object.assign(paymentQuery, { pageNum: 1, pageSize: 20, orderNo: '', payStatus: undefined }); fetchPayments() }
const resetRefunds = () => { Object.assign(refundQuery, { pageNum: 1, pageSize: 20, orderNo: '', refundStatus: undefined }); fetchRefunds() }
const openProcess = (row, status) => { processDialog.row = row; processDialog.status = status; processDialog.form = { processRemark: '' }; processDialog.visible = true }
const submitProcess = async () => { const valid = await processFormRef.value.validate().catch(() => false); if (!valid) return; processDialog.submitting = true; try { await processAdminRefund(processDialog.row.id, { refundStatus: processDialog.status, processRemark: processDialog.form.processRemark.trim() || null }); ElMessage.success(processDialog.status === 1 ? '退款已处理，订单和库存已同步更新' : '退款申请已拒绝'); processDialog.visible = false; await fetchRefunds(); await fetchPayments() } finally { processDialog.submitting = false } }

onMounted(async () => { await fetchPayments(); await fetchRefunds() })
</script>

<style scoped>
.payment-admin-page { max-width: 1280px; margin: 0 auto; }.manage-tabs :deep(.el-tabs__header) { margin-bottom: 18px; }.filter-card, .table-card { background: #fff; border: 1px solid #e9edf4; border-radius: 14px; box-shadow: 0 3px 12px rgba(32,50,80,.025); }.filter-card { padding: 20px 22px 4px; }.table-card { margin-top: 18px; overflow: hidden; }.filter-select { width: 140px; }.table-heading { display: flex; align-items: center; justify-content: space-between; gap: 14px; padding: 22px; }.table-heading h2 { margin: 0 0 7px; color: #273246; font-size: 17px; }.table-heading p, .dialog-hint { margin: 0; color: #99a3b5; font-size: 13px; line-height: 1.65; }.pagination-wrap { display: flex; justify-content: flex-end; padding: 18px 22px; border-top: 1px solid #edf0f5; }.rule-tip { display: flex; align-items: flex-start; gap: 8px; margin-bottom: 16px; padding: 12px 16px; color: #8a6417; background: #fff8e9; border: 1px solid #f6e2aa; border-radius: 10px; font-size: 13px; line-height: 1.6; }.rule-tip .el-icon { margin-top: 3px; }.dialog-hint { margin-bottom: 20px; }.muted { color: #a0a8b8; font-size: 13px; }@media (max-width:760px) { .filter-card :deep(.el-form-item) { margin-right: 0; }.table-heading { align-items: flex-start; flex-direction: column; }.pagination-wrap { justify-content: center; overflow-x: auto; } }
</style>
