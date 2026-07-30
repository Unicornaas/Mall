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
                <el-image v-if="item.productImage" :src="item.productImage" :alt="item.productName" fit="cover" class="item-cover">
                  <template #error><div class="img-placeholder"><el-icon :size="16"><Goods /></el-icon></div></template>
                </el-image>
                <div v-else class="img-placeholder"><el-icon :size="16"><Goods /></el-icon></div>
              </div>
              <span>{{ item.productName }}</span>
            </div>
            <span class="col-price">¥{{ formatPrice(item.price) }}</span>
            <span class="col-qty">x{{ item.quantity }}<small v-if="item.refundedQuantity">已退 {{ item.refundedQuantity }}</small><small v-if="item.shippedQuantity">已发 {{ item.shippedQuantity }}</small><small v-if="item.trackingNo">{{ item.shippingCompany }} · {{ item.trackingNo }}</small></span>
            <span class="col-sub">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>
        </div>
      </section>

      <section v-if="order.sellerOrders?.length" class="info-card">
        <h3 class="card-title">店铺配送</h3>
        <div v-for="shipment in order.sellerOrders" :key="shipment.sellerOrderId" class="shipment-row">
          <div>
            <strong>店铺订单 {{ shipment.sellerOrderId }}</strong>
            <span>{{ sellerShipmentStatus(shipment.status) }}</span>
          </div>
          <div class="shipment-detail">
            <span>{{ shipment.shippingCompany ? `${shipment.shippingCompany} · ${shipment.trackingNo}` : '暂未发货' }}</span>
            <small v-if="shipment.shipTime">发货时间：{{ shipment.shipTime }}</small>
          </div>
        </div>
      </section>

      <!-- 操作 -->
      <div v-if="order.status === 0" class="action-bar">
        <button class="action-btn danger" @click="handleCancel">取消订单</button>
        <button class="action-btn primary" @click="$router.push(`/payment/${order.id}`)">去支付</button>
      </div>
      <div v-else-if="canApplyRefund" class="action-bar">
        <button class="action-btn refund" @click="openRefundDialog">{{ refundInfos.some(item => item.refundStatus === 0) ? '继续申请退款' : '申请退款' }}</button>
      </div>

      <div v-for="refund in refundInfos" :key="refund.id" class="refund-status" :class="`refund-status-${refund.refundStatus}`">
        <strong>{{ refundStatusText(refund.refundStatus) }}</strong>
        <span>{{ refundItemText(refund.items) }}，退款 ¥{{ formatPrice(refund.refundAmount) }}。</span>
        <span v-if="refund.refundStatus === 0">申请已提交，请等待管理员审核。</span>
        <span v-else-if="refund.refundStatus === 2">申请被拒绝{{ refund.processRemark ? `：${refund.processRemark}` : '。' }}</span>
      </div>
    </template>

    <el-dialog v-model="refundDialog.visible" title="申请退款" width="620px" destroy-on-close>
      <p class="refund-hint">请选择需要退款的商品和数量。已发货数量暂不支持直接退款，退款金额由系统按实际成交价计算。</p>
      <el-form ref="refundFormRef" :model="refundDialog.form" :rules="refundRules" label-width="78px">
        <el-form-item label="退款商品">
          <div class="refund-items">
            <div v-for="item in refundDialog.form.items" :key="item.id" class="refund-item-row">
              <el-checkbox v-model="item.selected" :disabled="item.availableRefundQuantity < 1">{{ item.productName }}</el-checkbox>
              <span>可退 {{ item.availableRefundQuantity }}</span>
              <el-input-number v-model="item.quantity" :min="1" :max="item.availableRefundQuantity" :disabled="!item.selected" controls-position="right" />
              <strong>¥{{ formatPrice(item.price * item.quantity) }}</strong>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="退款合计"><span class="refund-amount">¥{{ formatPrice(refundTotal) }}</span></el-form-item>
        <el-form-item label="退款原因" prop="reason"><el-input v-model="refundDialog.form.reason" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="请说明退款原因" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="refundDialog.visible = false">取消</el-button><el-button type="primary" :loading="refundDialog.submitting" @click="submitRefund">提交申请</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Goods } from '@element-plus/icons-vue'
import { useUserStore } from '../../../stores/user'
import { getOrderDetail, cancelOrder } from '../api/order'
import { applyRefund, getRefundList } from '../api/payment'

const route = useRoute()
const store = useUserStore()
const loading = ref(false)
const order = ref(null)
const refundInfos = ref([])
const refundFormRef = ref()
const refundDialog = ref({ visible: false, submitting: false, form: { reason: '', items: [] } })
const refundRules = { reason: [{ required: true, message: '请填写退款原因', trigger: 'blur' }] }

const statusMap = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已收货', 4: '已取消', 5: '部分发货' }
const statusText = (s) => statusMap[s] || '未知'
const sellerShipmentStatus = (s) => ({ 0: '待支付', 1: '待发货', 2: '已发货', 3: '已完成', 4: '已取消', 5: '部分发货' }[Number(s)] || '未知')
const refundStatusText = (s) => ({ 0: '退款审核中', 1: '退款已完成', 2: '退款申请被拒绝' }[Number(s)] || '退款状态未知')
const refundItemText = (items) => (items || []).map(item => `${item.productName} x${item.quantity}`).join('、') || '退款商品'

const formatPrice = (val) => {
  const n = Number(val)
  return Number.isInteger(n) ? String(n) : n.toFixed(2)
}

const refundableItems = computed(() => (order.value?.items || []).filter(item => Number(item.availableRefundQuantity ?? (item.quantity - (item.shippedQuantity || 0) - (item.refundedQuantity || 0))) > 0))
const canApplyRefund = computed(() => [1, 5].includes(Number(order.value?.status)) && refundableItems.value.length > 0)
const refundTotal = computed(() => refundDialog.value.form.items.filter(item => item.selected).reduce((total, item) => total + Number(item.price) * Number(item.quantity), 0))

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(route.params.id)
    order.value = res.data
    const refundRes = await getRefundList(order.value.orderNo)
    refundInfos.value = refundRes.data || []
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

const openRefundDialog = () => {
  refundDialog.value = {
    visible: true,
    submitting: false,
    form: {
      reason: '',
      items: (order.value?.items || []).map(item => {
        const available = Number(item.availableRefundQuantity ?? (item.quantity - (item.shippedQuantity || 0) - (item.refundedQuantity || 0)))
        return { ...item, selected: available > 0, availableRefundQuantity: available, quantity: Math.max(1, available) }
      })
    }
  }
}
const submitRefund = async () => {
  const valid = await refundFormRef.value.validate().catch(() => false)
  if (!valid) return
  refundDialog.value.submitting = true
  try {
    const items = refundDialog.value.form.items.filter(item => item.selected && item.quantity > 0).map(item => ({ orderItemId: item.id, quantity: item.quantity }))
    if (!items.length) {
      ElMessage.warning('请至少选择一个可退款商品')
      return
    }
    await applyRefund({ orderNo: order.value.orderNo, items, reason: refundDialog.value.form.reason.trim() })
    ElMessage.success('退款申请已提交，请等待管理员审核')
    refundDialog.value.visible = false
    await fetchDetail()
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
  overflow: hidden;
  border-radius: 6px;
}

.item-cover { width: 100%; height: 100%; display: block; }

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
.col-qty { color: #aaa; text-align: center; }.col-qty small { display: block; color: #a7adb7; font-size: 11px; }
.col-sub { font-weight: 600; color: #333; text-align: right; }
.shipment-row { display: flex; align-items: center; justify-content: space-between; gap: 20px; padding: 12px 0; }.shipment-row + .shipment-row { border-top: 1px dashed #f0efed; }.shipment-row strong, .shipment-row span, .shipment-detail small { display: block; }.shipment-row strong { color: #444; font-size: 13px; }.shipment-row > div > span { margin-top: 4px; color: #888; font-size: 12px; }.shipment-detail { text-align: right; color: #555; font-size: 13px; }.shipment-detail small { margin-top: 4px; color: #aaa; font-size: 12px; }

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
.refund-hint { margin: 0 0 20px; color: #8a6417; font-size: 13px; line-height: 1.7; }.refund-amount { color: #e55a2b; font-weight: 700; font-size: 16px; }.refund-items { width: 100%; }.refund-item-row { display: grid; grid-template-columns: minmax(180px, 1fr) 65px 110px 75px; gap: 8px; align-items: center; padding: 8px 0; border-bottom: 1px dashed #eee; }.refund-item-row > span { color: #98a2b3; font-size: 12px; }.refund-item-row strong { color: #e55a2b; text-align: right; }
.refund-status { margin-top: 14px; padding: 12px 16px; border-radius: 10px; font-size: 13px; line-height: 1.65; }.refund-status strong { margin-right: 8px; }.refund-status-0 { color: #8a6417; background: #fff8e9; border: 1px solid #f6e2aa; }.refund-status-1 { color: #267a53; background: #edf9f2; border: 1px solid #ccebd9; }.refund-status-2 { color: #b54708; background: #fff4ed; border: 1px solid #f9d6c1; }

@media (max-width: 768px) {
  .info-grid { grid-template-columns: 1fr; }

  .item-header, .item-row {
    grid-template-columns: 1fr 70px 50px 70px;
    gap: 8px;
  }

  .item-header { font-size: 11px; }
  .shipment-row { align-items: flex-start; flex-direction: column; gap: 6px; }.shipment-detail { text-align: left; }
}
</style>
