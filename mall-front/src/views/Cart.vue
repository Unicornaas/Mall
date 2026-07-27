<template>
  <div class="cart-page" v-loading="loading">
    <h2 class="page-title">我的购物车</h2>

    <!-- 空购物车 -->
    <div v-if="!cartItems.length && !loading" class="empty-cart">
      <el-icon :size="64" color="#ddd"><ShoppingCart /></el-icon>
      <p>购物车还是空的</p>
      <button class="go-shop-btn" @click="$router.push('/products')">去逛逛</button>
    </div>

    <!-- 购物车内容 -->
    <template v-else>
      <!-- 工具栏 -->
      <div class="cart-toolbar">
        <label class="select-all" @click="handleSelectAll(!allSelected)">
          <span class="check-box" :class="{ checked: allSelected, indeterminate: isIndeterminate }">
            <el-icon v-if="allSelected" :size="14"><Check /></el-icon>
            <span v-else-if="isIndeterminate" class="ind-line"></span>
          </span>
          <span>全选</span>
        </label>
        <button
          class="batch-del-btn"
          :disabled="!selectedIds.length"
          :class="{ active: selectedIds.length }"
          @click="handleBatchDelete"
        >
          <el-icon :size="14"><Delete /></el-icon>
          <span>删除选中 ({{ selectedIds.length }})</span>
        </button>
      </div>

      <!-- 商品列表 -->
      <div class="cart-list">
        <div v-for="item in cartItems" :key="item.id" class="cart-item">
          <!-- 勾选 -->
          <label class="check-box" :class="{ checked: item._selected }" @click.stop="handleSelectItem(item)">
            <el-icon v-if="item._selected" :size="14"><Check /></el-icon>
          </label>

          <!-- 商品图片 -->
          <div class="item-image">
            <div class="img-placeholder">
              <el-icon :size="28"><Goods /></el-icon>
            </div>
          </div>

          <!-- 商品信息 -->
          <div class="item-info">
            <h4>{{ item.productName || '商品 #' + item.skuId }}</h4>
            <p class="item-price-label">¥{{ formatPrice(item.price) }}</p>
          </div>

          <!-- 数量 -->
          <div class="qty-control">
            <button class="qty-btn" :disabled="item.quantity <= 1" @click="changeQty(item, -1)">−</button>
            <span class="qty-val">{{ item.quantity }}</span>
            <button class="qty-btn" :disabled="item.quantity >= 99" @click="changeQty(item, 1)">+</button>
          </div>

          <!-- 小计 -->
          <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>

          <!-- 删除 -->
          <button class="del-btn" @click="handleDelete(item.id)">
            <el-icon :size="16"><Delete /></el-icon>
          </button>
        </div>
      </div>

      <!-- 底部结算栏 -->
      <div class="cart-footer">
        <div class="footer-left">
          <label class="select-all" @click="handleSelectAll(!allSelected)">
            <span class="check-box" :class="{ checked: allSelected, indeterminate: isIndeterminate }">
              <el-icon v-if="allSelected" :size="14"><Check /></el-icon>
              <span v-else-if="isIndeterminate" class="ind-line"></span>
            </span>
            <span>全选</span>
          </label>
          <span class="selected-count">已选 <em>{{ selectedIds.length }}</em> 件</span>
        </div>
        <div class="footer-right">
          <span class="total-label">合计</span>
          <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
          <button class="checkout-btn" :disabled="!selectedIds.length" @click="handleCheckout">
            去结算
          </button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { Check, Delete, ShoppingCart, Goods } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import {
  getCartList, updateQuantity, updateSelected,
  selectAll, removeCartItem, batchRemoveCart
} from '../api/cart'

const router = useRouter()
const store = useUserStore()
const loading = ref(false)
const cartItems = ref([])

const allSelected = ref(false)

const isIndeterminate = computed(() => {
  const len = selectedIds.value.length
  return len > 0 && len < cartItems.value.length
})

const selectedIds = computed(() =>
  cartItems.value.filter(i => i._selected).map(i => i.id)
)

const totalPrice = computed(() =>
  cartItems.value.filter(i => i._selected).reduce((s, i) => s + i.price * i.quantity, 0)
)

const formatPrice = (val) => {
  const n = Number(val)
  return Number.isInteger(n) ? String(n) : n.toFixed(2)
}

const fetchCart = async () => {
  loading.value = true
  try {
    const res = await getCartList(store.user.id)
    cartItems.value = (res.data || []).map(item => ({
      ...item, _selected: item.selected === 1
    }))
    allSelected.value = cartItems.value.length > 0 && cartItems.value.every(i => i._selected)
  } finally {
    loading.value = false
  }
}

const handleSelectAll = async (val) => {
  await selectAll(store.user.id, val ? 1 : 0)
  cartItems.value.forEach(i => { i._selected = val })
  allSelected.value = val
}

const handleSelectItem = async (item) => {
  item._selected = !item._selected
  await updateSelected(item.id, store.user.id, item._selected ? 1 : 0)
  allSelected.value = cartItems.value.every(i => i._selected)
}

let qtyTimer = null
const changeQty = (item, delta) => {
  item.quantity = Math.max(1, Math.min(99, item.quantity + delta))
  clearTimeout(qtyTimer)
  qtyTimer = setTimeout(() => updateQuantity(item.id, store.user.id, item.quantity), 300)
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该商品？', '提示', { type: 'warning' })
  await removeCartItem(id, store.user.id)
  store.notifyCartChange()
  ElMessage.success('已删除')
  await fetchCart()
}

const handleBatchDelete = async () => {
  if (!selectedIds.value.length) return
  ElMessageBox.confirm(
    `确定删除选中的 ${selectedIds.value.length} 件商品？`, '提示', { type: 'warning' }
  ).then(async () => {
    try {
      await batchRemoveCart({ userId: store.user.id, ids: selectedIds.value })
      store.notifyCartChange()
      ElMessage.success('批量删除成功')
      await fetchCart()
    } catch { /* handled */ }
  })
}

const handleCheckout = () => {
  const items = cartItems.value.filter(i => i._selected).map(i => ({
    cartId: i.id,
    spuId: i.spuId || 0,
    skuId: i.skuId,
    productName: i.productName,
    productImage: i.productImage,
    price: i.price,
    quantity: i.quantity,
  }))
  sessionStorage.setItem('checkout_items', JSON.stringify(items))
  sessionStorage.setItem('checkout_total', totalPrice.value.toFixed(2))
  router.push('/checkout')
}

onMounted(fetchCart)
</script>

<style scoped>
.cart-page {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 24px;
  letter-spacing: -0.3px;
}

/* 空购物车 */
.empty-cart {
  text-align: center;
  padding: 60px 0;
}

.empty-cart p {
  font-size: 15px;
  color: #aaa;
  margin: 16px 0 20px;
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

/* 工具栏 */
.cart-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 14px;
  margin-bottom: 14px;
  border-bottom: 1px solid #f0efed;
}

.select-all {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  user-select: none;
}

/* 自定义复选框 */
.check-box {
  width: 18px;
  height: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #d0cdc6;
  border-radius: 4px;
  transition: all 0.2s ease;
  color: #fff;
  flex-shrink: 0;
}

.check-box.checked {
  background: #FF6B35;
  border-color: #FF6B35;
}

.check-box.indeterminate {
  background: #FF6B35;
  border-color: #FF6B35;
}

.ind-line {
  width: 8px;
  height: 2px;
  background: #fff;
  border-radius: 1px;
}

.batch-del-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #bbb;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.batch-del-btn.active {
  color: #e55a2b;
  cursor: pointer;
}

.batch-del-btn.active:hover {
  background: rgba(229,90,43,0.06);
}

.batch-del-btn:disabled {
  cursor: not-allowed;
}

/* 商品列表 */
.cart-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border: 1px solid #f0efed;
  border-radius: 12px;
  transition: all 0.2s;
}

.cart-item:hover {
  border-color: #e0ded9;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}

.item-image {
  width: 72px;
  height: 72px;
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
  margin: 0 0 6px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-price-label {
  font-size: 14px;
  font-weight: 600;
  color: #FF6B35;
  margin: 0;
}

/* 数量控制 */
.qty-control {
  display: flex;
  align-items: center;
  border: 1px solid #e4e1dc;
  border-radius: 8px;
  overflow: hidden;
}

.qty-btn {
  width: 30px;
  height: 30px;
  border: none;
  background: #f8f7f5;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  transition: all 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qty-btn:hover:not(:disabled) {
  background: #efedeb;
  color: #FF6B35;
}

.qty-btn:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.qty-val {
  min-width: 36px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

/* 小计 */
.item-subtotal {
  font-size: 15px;
  font-weight: 700;
  color: #1a1a2e;
  min-width: 80px;
  text-align: right;
}

/* 删除 */
.del-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #ccc;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.del-btn:hover {
  color: #e55a2b;
  background: rgba(229,90,43,0.06);
}

/* 底部结算 */
.cart-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20px;
  padding: 18px 20px;
  background: #fafaf8;
  border-radius: 14px;
  border: 1px solid #f0efed;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.selected-count {
  font-size: 13px;
  color: #999;
}

.selected-count em {
  font-style: normal;
  color: #FF6B35;
  font-weight: 600;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.total-label {
  font-size: 13px;
  color: #999;
}

.total-price {
  font-size: 26px;
  font-weight: 800;
  color: #FF6B35;
  letter-spacing: -0.5px;
}

.checkout-btn {
  padding: 12px 32px;
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

.checkout-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255,107,53,0.35);
}

.checkout-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* 响应式 */
@media (max-width: 768px) {
  .cart-page { padding: 16px; }

  .cart-item {
    flex-wrap: wrap;
    gap: 10px;
  }

  .item-image { width: 56px; height: 56px; }

  .item-subtotal {
    margin-left: auto;
    min-width: auto;
  }

  .cart-footer {
    flex-direction: column;
    gap: 14px;
    align-items: stretch;
  }

  .footer-right {
    justify-content: space-between;
  }

  .total-price { font-size: 22px; }
}
</style>
