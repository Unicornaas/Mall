<template>
  <div class="detail-page" v-loading="loading">
    <button class="back-btn" @click="$router.push('/orders')">
      <el-icon><ArrowLeft /></el-icon>
      <span>返回订单列表</span>
    </button>

    <template v-if="order">
      <h2 class="page-title">订单详情</h2>

      <!-- 订单信息 -->
      <section class="info-card">
        <h3 class="card-title">订单信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">订单号</span>
            <span class="info-value mono">{{ order.orderNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">订单状态</span>
            <span class="status-tag" :class="'status-' + order.status">{{ statusText(order.status) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">订单金额</span>
            <span class="info-value price">¥{{ order.totalAmount }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">创建时间</span>
            <span class="info-value">{{ order.createTime }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">收货人</span>
            <span class="info-value">{{ order.receiverName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">联系电话</span>
            <span class="info-value">{{ order.receiverPhone }}</span>
          </div>
          <div class="info-item full-width">
            <span class="info-label">收货地址</span>
            <span class="info-value">{{ order.receiverAddress }}</span>
          </div>
          <div v-if="order.remark" class="info-item full-width">
            <span class="info-label">备注</span>
            <span class="info-value">{{ order.remark }}</span>
          </div>
        </div>
      </section>

      <!-- 商品明细 -->
      <section class="info-card">
        <h3 class="card-title">商品明细</h3>
        <div class="item-table">
          <div class="item-header">
            <span class="col-name">商品</span>
            <span class="col-price">单价</span>
            <span class="col-qty">数量</span>
            <span class="col-sub">小计</span>
          </div>
          <div v-for="item in order.items" :key="item.id" class="item-row">
            <div class="col-name">
              <div class="item-img">
                <div class="img-placeholder">
                  <el-icon :size="16"><Goods /></el-icon>
                </div>
              </div>
              <span>{{ item.productName }}</span>
            </div>
            <span class="col-price">¥{{ formatPrice(item.price) }}</span>
            <span class="col-qty">x{{ item.quantity }}</span>
            <span class="col-sub">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>
        </div>
      </section>

      <!-- 操作 -->
      <div v-if="order.status === 0" class="action-bar">
        <button class="action-btn danger" @click="handleCancel">取消订单</button>
        <button class="action-btn primary" @click="$router.push(`/payment/${order.id}`)">去支付</button>
      </div>
      <div v-else-if="order.status === 1" class="action-bar">
        <button v-if="refundInfo?.refundStatus !== 0" class="action-btn refund" @click="openRefundDialog">{{ refundInfo?.refundStatus === 2 ? '重新申请退款' : '申请退款' }}</button>
        <button v-else class="action-btn pending" disabled>退款审核中</button>
      </div>

      <div v-if="refundInfo" class="refund-status" :class="`refund-status-${refundInfo.refundStatus}`">
        <strong>{{ refundStatusText(refundInfo.refundStatus) }}</strong>
        <span v-if="refundInfo.refundStatus === 0">退款申请已提交，请等待管理员审核。</span>
        <span v-else-if="refundInfo.refundStatus === 2">退款申请被拒绝{{ refundInfo.processRemark ? `：${refundInfo.processRemark}` : '。' }}</span>
      </div>
    </template>

    <el-dialog v-model="refundDialog.visible" title="申请退款" width="460px" destroy-on-close>
      <p class="refund-hint">当前仅支持已支付、未发货订单的整单退款。申请提交后请等待管理员审核。</p>
      <el-form ref="refundFormRef" :model="refundDialog.form" :rules="refundRules" label-width="78px"><el-form-item label="退款金额"><span class="refund-amount">¥{{ formatPrice(order?.totalAmount) }}</span></el-form-item><el-form-item label="退款原因" prop="reason"><el-input v-model="refundDialog.form.reason" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="请说明退款原因" /></el-form-item></el-form>
      <template #footer><el-button @click="refundDialog.visible = false">取消</el-button><el-button type="primary" :loading="refundDialog.submitting" @click="submitRefund">提交申请</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Goods } from '@element-plus/icons-vue'
import { useUserStore } from '../../../stores/user'
import { getOrderDetail, cancelOrder } from '../api/order'
import { applyRefund, getRefundStatus } from '../api/payment'

const route = useRoute()
const store = useUserStore()
const loading = ref(false)
const order = ref(null)
const refundInfo = ref(null)
const refundFormRef = ref()
const refundDialog = ref({ visible: false, submitting: false, form: { reason: '' } })
const refundRules = { reason: [{ required: true, message: '请填写退款原因', trigger: 'blur' }] }

const statusMap = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已收货', 4: '已取消', 5: '已关闭' }
const statusText = (s) => statusMap[s] || '未知'
const refundStatusText = (s) => ({ 0: '退款审核中', 1: '退款已完成', 2: '退款申请被拒绝' }[Number(s)] || '退款状态未知')

const formatPrice = (val) => {
  const n = Number(val)
  return Number.isInteger(n) ? String(n) : n.toFixed(2)
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(route.params.id)
    order.value = res.data
    const refundRes = await getRefundStatus(order.value.orderNo)
    refundInfo.value = refundRes.data
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  ElMessageBox.confirm('确定取消该订单？', '提示', { type: 'warning' }).then(async () => {
    try {
      await cancelOrder(order.value.id, store.user.id)
      ElMessage.success('订单已取消')
      await fetchDetail()
    } catch { /* handled */ }
  })
}

const openRefundDialog = () => { refundDialog.value = { visible: true, submitting: false, form: { reason: '' } } }
const submitRefund = async () => {
  const valid = await refundFormRef.value.validate().catch(() => false)
  if (!valid) return
  refundDialog.value.submitting = true
  try {
    const res = await applyRefund({ orderNo: order.value.orderNo, refundAmount: Number(order.value.totalAmount), reason: refundDialog.value.form.reason.trim() })
    refundInfo.value = res.data
    ElMessage.success('退款申请已提交，请等待管理员审核')
    refundDialog.value.visible = false
  } finally {
    refundDialog.value.submitting = false
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.detail-page { max-width: 720px; }

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  background: #f8f7f5;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 16px;
}

.back-btn:hover { background: #efedeb; color: #333; }

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 20px;
  letter-spacing: -0.3px;
}

/* 信息卡片 */
.info-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  margin-bottom: 14px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0efed;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0efed;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  font-size: 12px;
  color: #aaa;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 14px;
  color: #444;
  font-weight: 500;
}

.info-value.mono {
  font-family: 'SF Mono', 'Menlo', monospace;
  font-size: 13px;
}

.info-value.price {
  color: #FF6B35;
  font-weight: 700;
  font-size: 16px;
}

.status-tag {
  display: inline-block;
  align-self: flex-start;
  font-size: 12px;
  font-weight: 600;
  padding: 3px 12px;
  border-radius: 20px;
}

.status-0 { background: rgba(250,173,20,0.1); color: #d48806; }
.status-1 { background: rgba(255,107,53,0.08); color: #FF6B35; }
.status-2 { background: rgba(82,196,26,0.1); color: #52c41a; }
.status-3 { background: rgba(24,144,255,0.08); color: #1677ff; }
.status-4 { background: rgba(0,0,0,0.04); color: #999; }
.status-5 { background: rgba(0,0,0,0.05); color: #bbb; }

/* 商品表格 */
.item-header {
  display: grid;
  grid-template-columns: 1fr 100px 60px 100px;
  gap: 12px;
  padding: 8px 0;
  font-size: 12px;
  color: #aaa;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid #f0efed;
}

.item-row {
  display: grid;
  grid-template-columns: 1fr 100px 60px 100px;
  gap: 12px;
  padding: 12px 0;
  align-items: center;
  font-size: 14px;
}

.item-row + .item-row { border-top: 1px dashed #f0efed; }

.col-name {
  display: flex;
  align-items: center;
  gap: 10px;
  overflow: hidden;
}

.col-name span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-img {
  width: 40px; height: 40px;
  flex-shrink: 0;
}

.img-placeholder {
  width: 100%; height: 100%;
  border-radius: 6px;
  background: linear-gradient(135deg, #eef2f5, #e8ecf1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c5cad2;
}

.col-price { color: #777; }
.col-qty { color: #aaa; text-align: center; }
.col-sub { font-weight: 600; color: #333; text-align: right; }

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}

.action-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn.danger {
  background: rgba(229,90,43,0.08);
  color: #e55a2b;
}

.action-btn.danger:hover {
  background: rgba(229,90,43,0.15);
}

.action-btn.primary {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8F5E 100%);
  color: #fff;
  box-shadow: 0 4px 15px rgba(255,107,53,0.25);
}

.action-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255,107,53,0.35);
}

.action-btn.refund { background: rgba(229,90,43,.08); color: #e55a2b; }
.action-btn.refund:hover { background: rgba(229,90,43,.15); }
.action-btn.pending { background: #f2f4f7; color: #98a2b3; cursor: not-allowed; }
.refund-hint { margin: 0 0 20px; color: #8a6417; font-size: 13px; line-height: 1.7; }.refund-amount { color: #e55a2b; font-weight: 700; font-size: 16px; }
.refund-status { margin-top: 14px; padding: 12px 16px; border-radius: 10px; font-size: 13px; line-height: 1.65; }.refund-status strong { margin-right: 8px; }.refund-status-0 { color: #8a6417; background: #fff8e9; border: 1px solid #f6e2aa; }.refund-status-1 { color: #267a53; background: #edf9f2; border: 1px solid #ccebd9; }.refund-status-2 { color: #b54708; background: #fff4ed; border: 1px solid #f9d6c1; }

@media (max-width: 768px) {
  .info-grid { grid-template-columns: 1fr; }

  .item-header, .item-row {
    grid-template-columns: 1fr 70px 50px 70px;
    gap: 8px;
  }

  .item-header { font-size: 11px; }
}
</style>
