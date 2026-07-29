<template>
  <div class="order-page" v-loading="loading">
    <h2 class="page-title">我的订单</h2>

    <template v-if="orders.length">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <!-- 订单头部 -->
        <div class="order-header">
          <div class="order-meta">
            <span class="order-no">订单号 <em>{{ order.orderNo }}</em></span>
            <span class="order-time">{{ order.createTime }}</span>
          </div>
          <span class="order-status" :class="'status-' + order.status">
            {{ statusText(order.status) }}
          </span>
        </div>

        <!-- 商品列表 -->
        <div class="order-items">
          <div v-for="item in order.items" :key="item.id" class="order-item">
            <div class="item-img">
              <div class="img-placeholder">
                <el-icon :size="20"><Goods /></el-icon>
              </div>
            </div>
            <div class="item-info">
              <h4>{{ item.productName }}</h4>
              <span class="item-price">¥{{ formatPrice(item.price) }}</span>
              <span class="item-qty">x{{ item.quantity }}</span>
            </div>
            <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
          </div>
        </div>

        <!-- 订单底部 -->
        <div class="order-footer">
          <span class="order-total">共 {{ order.items?.length || 0 }} 件，合计 <em>¥{{ order.totalAmount }}</em></span>
          <div class="order-actions">
            <button class="action-btn secondary" @click="$router.push(`/orders/${order.id}`)">
              查看详情
            </button>
            <button
              v-if="order.status === 0"
              class="action-btn danger"
              @click="handleCancel(order)"
            >
              取消订单
            </button>
            <button
              v-if="order.status === 0"
              class="action-btn primary"
              @click="$router.push(`/payment/${order.id}`)"
            >
              去支付
            </button>
          </div>
        </div>
      </div>
    </template>

    <div v-else class="empty-state">
      <el-icon :size="56" color="#ddd"><Document /></el-icon>
      <p>还没有订单</p>
      <button class="go-shop-btn" @click="$router.push('/products')">去逛逛</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Goods, Document } from '@element-plus/icons-vue'
import { useUserStore } from '../../../stores/user'
import { getOrderList, cancelOrder } from '../api/order'

const store = useUserStore()
const loading = ref(false)
const orders = ref([])

const statusMap = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已收货', 4: '已取消', 5: '部分发货' }

const statusText = (s) => statusMap[s] || '未知'

const formatPrice = (val) => {
  const n = Number(val)
  return Number.isInteger(n) ? String(n) : n.toFixed(2)
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getOrderList()
    orders.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleCancel = (order) => {
  ElMessageBox.confirm('确定取消该订单？', '提示', { type: 'warning' }).then(async () => {
    try {
      await cancelOrder(order.id, store.user.id)
      ElMessage.success('订单已取消')
      await fetchOrders()
    } catch { /* handled */ }
  })
}

onMounted(fetchOrders)
</script>

<style scoped>
.order-page { max-width: 860px; }

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 24px;
  letter-spacing: -0.3px;
}

/* 订单卡片 */
.order-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  margin-bottom: 16px;
  border: 1px solid #f0efed;
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: #fafaf8;
  border-bottom: 1px solid #f0efed;
}

.order-meta {
  display: flex;
  gap: 16px;
  align-items: center;
}

.order-no {
  font-size: 13px;
  color: #999;
}

.order-no em {
  font-style: normal;
  color: #555;
  font-weight: 500;
}

.order-time {
  font-size: 12px;
  color: #bbb;
}

.order-status {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 20px;
  letter-spacing: 0.5px;
}

.status-0 { background: rgba(250,173,20,0.1); color: #d48806; }
.status-1 { background: rgba(255,107,53,0.08); color: #FF6B35; }
.status-2 { background: rgba(82,196,26,0.1); color: #52c41a; }
.status-3 { background: rgba(24,144,255,0.08); color: #1677ff; }
.status-4 { background: rgba(0,0,0,0.04); color: #999; }
.status-5 { background: rgba(0,0,0,0.05); color: #bbb; }

/* 商品列表 */
.order-items {
  padding: 8px 20px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
}

.order-item + .order-item {
  border-top: 1px dashed #f0efed;
}

.item-img {
  width: 56px;
  height: 56px;
  flex-shrink: 0;
}

.img-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  background: linear-gradient(135deg, #eef2f5, #e8ecf1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c5cad2;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-info h4 {
  font-size: 14px;
  color: #333;
  margin: 0 0 4px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-price {
  font-size: 13px;
  font-weight: 600;
  color: #FF6B35;
}

.item-qty {
  font-size: 12px;
  color: #aaa;
  margin-left: 4px;
}

.item-subtotal {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
}

/* 订单底部 */
.order-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-top: 1px solid #f0efed;
  background: #fafaf8;
}

.order-total {
  font-size: 14px;
  color: #777;
}

.order-total em {
  font-style: normal;
  font-weight: 700;
  color: #FF6B35;
  font-size: 16px;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 7px 17px;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn.secondary {
  background: #f0efed;
  color: #666;
}

.action-btn.secondary:hover {
  background: #e4e1dc;
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
  box-shadow: 0 2px 8px rgba(255,107,53,0.2);
}

.action-btn.primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(255,107,53,0.3);
}

/* 空状态 */
.empty-state {
  background: #fff;
  border-radius: 14px;
  padding: 60px 0;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.empty-state p {
  font-size: 15px;
  color: #aaa;
  margin: 14px 0 18px;
}

.go-shop-btn {
  padding: 10px 28px;
  border: none;
  border-radius: 10px;
  background: #1a1a2e;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.go-shop-btn:hover {
  background: #333;
}

@media (max-width: 768px) {
  .order-header, .order-footer {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .order-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
