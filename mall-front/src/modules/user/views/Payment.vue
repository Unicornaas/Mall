<template>
  <div class="payment-page" v-loading="loading">
    <button class="back-btn" @click="$router.back()">
      <el-icon><ArrowLeft /></el-icon>
      <span>返回</span>
    </button>

    <h2 class="page-title">支付订单</h2>

    <template v-if="order">
      <!-- 订单信息卡片 -->
      <section class="info-card">
        <h3 class="card-title">
          <el-icon :size="18"><Document /></el-icon>订单信息
        </h3>
        <div class="info-row">
          <span class="info-label">订单号</span>
          <span class="info-value">{{ order.orderNo }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">订单金额</span>
          <span class="info-value price">¥{{ order.totalAmount }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">订单状态</span>
          <span class="status-tag" :class="'status-' + order.status">{{ statusText(order.status) }}</span>
        </div>
      </section>

      <!-- 支付信息 -->
      <section v-if="payStatus" class="info-card">
        <h3 class="card-title">
          <el-icon :size="18"><Money /></el-icon>支付信息
        </h3>
        <div class="info-row">
          <span class="info-label">交易号</span>
          <span class="info-value">{{ payStatus.tradeNo || '--' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">支付金额</span>
          <span class="info-value price">¥{{ payStatus.amount }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">支付状态</span>
          <span class="pay-status-badge" :class="payStatus.payStatus === 1 ? 'paid' : payStatus.payStatus === 2 ? 'refunded' : 'unpaid'">
            {{ payStatus.payStatus === 1 ? '已支付' : payStatus.payStatus === 2 ? '已退款' : '待支付' }}
          </span>
        </div>
        <div v-if="payStatus.payTime" class="info-row">
          <span class="info-label">支付时间</span>
          <span class="info-value">{{ payStatus.payTime }}</span>
        </div>
      </section>

      <!-- 支付按钮 -->
      <div v-if="order.status === 0 && !paymentDone" class="pay-action">
        <div class="pay-amount">
          <span class="amount-label">需支付</span>
          <span class="amount-value">¥{{ order.totalAmount }}</span>
        </div>
        <div class="pay-buttons">
          <button
            class="pay-btn"
            :class="{ loading: paying }"
            :disabled="paying || cancelling"
            @click="handlePay"
          >
            <span v-if="!paying">确认支付</span>
            <span v-else class="btn-spinner"></span>
          </button>
          <button
            class="cancel-btn"
            :disabled="paying || cancelling"
            @click="handleCancel"
          >
            {{ cancelling ? '取消中...' : '取消订单' }}
          </button>
        </div>
      </div>

      <!-- 支付成功 -->
      <div v-if="paymentDone" class="success-card">
        <div class="success-icon">&#10003;</div>
        <h3>支付成功</h3>
        <p>订单已支付，请等待发货</p>
        <button class="link-btn" @click="$router.push('/orders')">查看订单</button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Document, Money } from '@element-plus/icons-vue'
import { getOrderDetail, cancelOrder } from '../api/order'
import { useUserStore } from '../../../stores/user'
import { payOrder, getPaymentStatus } from '../api/payment'

const route = useRoute()
const store = useUserStore()
const loading = ref(false)
const paying = ref(false)
const cancelling = ref(false)
const paymentDone = ref(false)
const order = ref(null)
const payStatus = ref(null)

const statusMap = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已收货', 4: '已取消', 5: '已关闭' }
const statusText = (s) => statusMap[s] || '未知'

const fetchData = async () => {
  loading.value = true
  try {
    const orderRes = await getOrderDetail(route.params.id)
    order.value = orderRes.data
    try {
      const payRes = await getPaymentStatus(order.value.orderNo)
      payStatus.value = payRes.data
      if (payRes.data?.payStatus === 1) paymentDone.value = true
    } catch { /* payment may not exist yet */ }
  } finally {
    loading.value = false
  }
}

const handlePay = async () => {
  paying.value = true
  try {
    await payOrder(order.value.orderNo)
    ElMessage.success('支付成功')
    paymentDone.value = true
    order.value.status = 1
  } finally {
    paying.value = false
  }
}

const handleCancel = async () => {
  cancelling.value = true
  try {
    await cancelOrder(order.value.id, store.user.id)
    ElMessage.success('订单已取消')
    order.value.status = 4
  } finally {
    cancelling.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.payment-page { max-width: 640px; }

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
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 14px;
}

.card-title .el-icon { color: #FF6B35; }

.info-row {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.info-row + .info-row { border-top: 1px solid #faf8f7; }

.info-label { font-size: 14px; color: #888; min-width: 80px; }
.info-value { font-size: 14px; color: #333; font-weight: 500; }
.info-value.price { color: #FF6B35; font-weight: 700; font-size: 16px; }

.status-tag {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 12px;
  border-radius: 20px;
}

.status-0 { background: rgba(250,173,20,0.1); color: #d48806; }
.status-1 { background: rgba(255,107,53,0.08); color: #FF6B35; }

.pay-status-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 12px;
  border-radius: 20px;
}

.pay-status-badge.unpaid { background: rgba(250,173,20,0.1); color: #d48806; }
.pay-status-badge.paid { background: rgba(82,196,26,0.1); color: #52c41a; }
.pay-status-badge.refunded { background: rgba(0,0,0,0.05); color: #999; }

/* 支付按钮区 */
.pay-action {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0efed;
}

.pay-amount {
  margin-bottom: 18px;
}

.amount-label {
  font-size: 14px;
  color: #888;
  display: block;
  margin-bottom: 4px;
}

.amount-value {
  font-size: 36px;
  font-weight: 800;
  color: #FF6B35;
  letter-spacing: -1px;
}

.pay-buttons {
  display: flex;
  gap: 12px;
}

.pay-btn {
  flex: 1;
  height: 50px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8F5E 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(255,107,53,0.25);
}

.pay-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255,107,53,0.35);
}

.pay-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.pay-btn.loading { pointer-events: none; }

.cancel-btn {
  flex: 1;
  height: 50px;
  border: 2px solid #e0ded9;
  border-radius: 12px;
  background: #fff;
  color: #888;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-btn:hover:not(:disabled) {
  border-color: #ccc;
  color: #666;
}

.cancel-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.btn-spinner {
  display: inline-block;
  width: 20px; height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

/* 成功卡片 */
.success-card {
  background: #fff;
  border-radius: 14px;
  padding: 48px 24px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0efed;
}

.success-icon {
  width: 64px; height: 64px;
  margin: 0 auto 16px;
  background: rgba(82,196,26,0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #52c41a;
}

.success-card h3 {
  font-size: 20px;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.success-card p {
  font-size: 14px;
  color: #999;
  margin: 0 0 20px;
}

.link-btn {
  padding: 10px 28px;
  border: none;
  border-radius: 10px;
  background: #1a1a2e;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.link-btn:hover { background: #333; }
</style>
