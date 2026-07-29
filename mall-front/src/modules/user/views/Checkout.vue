<template>
  <div class="checkout-page" v-loading="loading">
    <!-- 顶部返回 -->
    <button class="back-btn" @click="$router.push('/cart')">
      <el-icon><ArrowLeft /></el-icon>
      <span>返回购物车</span>
    </button>

    <template v-if="items.length">
      <div class="checkout-content">
        <!-- 收货地址 -->
        <section class="checkout-section">
          <h3 class="section-title">
            <el-icon :size="18"><MapLocation /></el-icon>
            <span>收货地址</span>
          </h3>
          <div v-if="addresses.length" class="address-list">
            <div
              v-for="addr in addresses"
              :key="addr.id"
              class="addr-card"
              :class="{ selected: selectedAddressId === addr.id }"
              @click="selectedAddressId = addr.id"
            >
              <div class="addr-radio">
                <span class="radio-dot" v-if="selectedAddressId === addr.id"></span>
              </div>
              <div class="addr-body">
                <div class="addr-line">
                  <strong>{{ addr.receiverName }}</strong>
                  <span class="addr-phone">{{ addr.receiverPhone }}</span>
                  <span v-if="addr.isDefault === 1" class="default-tag">默认</span>
                </div>
                <p class="addr-text">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</p>
              </div>
              <el-icon v-if="selectedAddressId === addr.id" :size="18" color="#FF6B35"><CircleCheck /></el-icon>
            </div>
          </div>
          <div v-else class="empty-block">
            <p>暂无收货地址</p>
            <button class="link-btn" @click="$router.push('/addresses')">去添加地址</button>
          </div>
        </section>

        <!-- 商品明细 -->
        <section class="checkout-section">
          <h3 class="section-title">
            <el-icon :size="18"><Goods /></el-icon>
            <span>商品明细</span>
          </h3>
          <div class="item-list">
            <div v-for="item in items" :key="item.skuId" class="checkout-item">
              <div class="item-img">
                <div class="img-placeholder">
                  <el-icon :size="20"><Goods /></el-icon>
                </div>
              </div>
              <div class="item-info">
                <h4>{{ item.productName }}</h4>
                <span>¥{{ formatPrice(item.price) }} x {{ item.quantity }}</span>
              </div>
              <div class="item-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
            </div>
          </div>
        </section>

        <!-- 备注 -->
        <section class="checkout-section">
          <h3 class="section-title">
            <el-icon :size="18"><Edit /></el-icon>
            <span>订单备注</span>
          </h3>
          <el-input
            v-model="remark"
            type="textarea"
            :rows="2"
            placeholder="选填：如有特殊要求，请在此备注"
            class="remark-input"
          />
        </section>

        <!-- 提交 -->
        <div class="checkout-footer">
          <div class="footer-info">
            <span class="footer-label">应付总额</span>
            <span class="footer-price">¥{{ totalPrice }}</span>
          </div>
          <button
            class="submit-btn"
            :class="{ loading: submitting }"
            :disabled="!selectedAddressId || submitting"
            @click="handleSubmit"
          >
            <span v-if="!submitting">提交订单</span>
            <span v-else class="btn-spinner"></span>
          </button>
        </div>
      </div>
    </template>

    <div v-else class="empty-state">
      <p>没有待结算的商品</p>
      <button class="link-btn" @click="$router.push('/cart')">返回购物车</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, MapLocation, Goods, Edit, CircleCheck } from '@element-plus/icons-vue'
import { useUserStore } from '../../../stores/user'
import { getAddresses } from '../api/address'
import { createOrder } from '../api/order'
import { batchRemoveCart } from '../api/cart'

const router = useRouter()
const store = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const addresses = ref([])
const selectedAddressId = ref(null)
const remark = ref('')
const items = ref([])
const totalPrice = ref('0.00')

const formatPrice = (val) => {
  const n = Number(val)
  return Number.isInteger(n) ? String(n) : n.toFixed(2)
}

const fetchData = async () => {
  loading.value = true
  try {
    const raw = sessionStorage.getItem('checkout_items')
    items.value = raw ? JSON.parse(raw) : []
    totalPrice.value = sessionStorage.getItem('checkout_total') || '0.00'

    const res = await getAddresses()
    addresses.value = res.data || []
    const def = addresses.value.find(a => a.isDefault === 1)
    selectedAddressId.value = def?.id ?? addresses.value[0]?.id ?? null
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请先添加并选择收货地址')
    return
  }
  submitting.value = true
  try {
    const orderItems = items.value.map(i => ({
      spuId: i.spuId, skuId: i.skuId, quantity: i.quantity,
    }))
    const res = await createOrder({
      userId: store.user.id,
      addressId: selectedAddressId.value,
      remark: remark.value,
      items: orderItems,
    })
    ElMessage.success('下单成功')
    // 从购物车移除已下单的商品
    const cartIds = items.value.map(i => i.cartId).filter(Boolean)
    if (cartIds.length) {
      await batchRemoveCart({ userId: store.user.id, ids: cartIds })
      store.notifyCartChange()
    }
    sessionStorage.removeItem('checkout_items')
    sessionStorage.removeItem('checkout_total')
    router.push(`/orders/${res.data.id}`)
  } finally {
    submitting.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.checkout-page { max-width: 780px; }

/* 返回按钮 */
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
  margin-bottom: 20px;
}

.back-btn:hover {
  background: #efedeb;
  color: #333;
}

/* 分区卡片 */
.checkout-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.checkout-section {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0efed;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 14px;
}

.section-title .el-icon { color: #FF6B35; }

/* 地址列表 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.addr-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 2px solid #f0efed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.addr-card:hover {
  border-color: #e0ded9;
}

.addr-card.selected {
  border-color: #FF6B35;
  background: rgba(255,107,53,0.02);
}

.addr-radio {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid #d0cdc6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s;
}

.addr-card.selected .addr-radio {
  border-color: #FF6B35;
}

.radio-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #FF6B35;
}

.addr-body { flex: 1; min-width: 0; }

.addr-line {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.addr-line strong {
  font-size: 14px;
  color: #333;
}

.addr-phone {
  font-size: 13px;
  color: #888;
}

.default-tag {
  font-size: 11px;
  padding: 1px 8px;
  border-radius: 3px;
  background: rgba(255,107,53,0.08);
  color: #FF6B35;
  font-weight: 500;
}

.addr-text {
  font-size: 13px;
  color: #888;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 商品列表 */
.item-list {
  display: flex;
  flex-direction: column;
}

.checkout-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
}

.checkout-item + .checkout-item {
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

.item-info span {
  font-size: 13px;
  color: #999;
}

.item-total {
  font-size: 15px;
  font-weight: 700;
  color: #333;
  white-space: nowrap;
}

/* 备注 */
.remark-input :deep(.el-textarea__inner) {
  border-radius: 10px;
  background: #fafaf8;
  border-color: #f0efed;
}

.remark-input :deep(.el-textarea__inner:focus) {
  border-color: #FF6B35;
}

/* 提交底部 */
.checkout-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 14px;
  padding: 18px 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0efed;
  position: sticky;
  bottom: 0;
}

.footer-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.footer-label {
  font-size: 14px;
  color: #888;
}

.footer-price {
  font-size: 28px;
  font-weight: 800;
  color: #FF6B35;
  letter-spacing: -0.5px;
}

.submit-btn {
  padding: 13px 40px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8F5E 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(255,107,53,0.25);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255,107,53,0.35);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.submit-btn.loading { pointer-events: none; }

.btn-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

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
  margin: 0 0 16px;
}

.empty-block {
  padding: 16px 0;
  text-align: center;
}

.empty-block p {
  font-size: 14px;
  color: #aaa;
  margin: 0 0 10px;
}

.link-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 8px;
  background: #1a1a2e;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.link-btn:hover { background: #333; }

@media (max-width: 768px) {
  .checkout-section { padding: 14px 16px; }

  .checkout-footer {
    flex-direction: column;
    gap: 14px;
    align-items: stretch;
  }

  .submit-btn { width: 100%; }

  .footer-price { font-size: 24px; }
}
</style>
