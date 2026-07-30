<template>
  <div class="detail-page" v-loading="loading">
    <!-- 面包屑返回 -->
    <button class="back-btn" @click="$router.back()">
      <el-icon><ArrowLeft /></el-icon>
      <span>返回</span>
    </button>

    <div v-if="spu" class="detail-content">
      <!-- 左侧图片 -->
      <div class="detail-gallery">
        <div class="gallery-main">
          <el-image v-if="currentImage" :src="currentImage" :alt="spu.name" fit="contain" class="product-main-image">
            <template #error>
              <div class="img-placeholder"><el-icon :size="64"><Goods /></el-icon><span v-if="spu.brand" class="img-brand">{{ spu.brand }}</span></div>
            </template>
          </el-image>
          <div v-else class="img-placeholder"><el-icon :size="64"><Goods /></el-icon><span v-if="spu.brand" class="img-brand">{{ spu.brand }}</span></div>
        </div>
      </div>

      <!-- 右侧信息 -->
      <div class="detail-info">
        <div class="info-header">
          <h2 class="product-name">{{ spu.name }}</h2>
          <p class="product-shop">店铺：{{ spu.shopName || '平台自营' }}</p>
          <p class="product-brand" v-if="spu.brand">{{ spu.brand }}</p>
          <p class="product-desc" v-if="spu.description">{{ spu.description }}</p>
          <span class="status-badge" :class="spu.status === 1 ? 'on-sale' : 'off-sale'">
            {{ spu.status === 1 ? '在售' : '已下架' }}
          </span>
        </div>

        <!-- 当前选中 SKU 的价格 -->
        <div class="price-box" v-if="selectedSku">
          <span class="price-currency">¥</span>
          <span class="price-number">{{ formatPrice(selectedSku.price) }}</span>
          <span class="price-sku-name">{{ selectedSku.name || selectedSku.skuCode }}</span>
        </div>

        <!-- 库存提示 -->
        <div class="stock-hint" v-if="selectedSku">
          <span class="stock-dot" :class="stockLevel"></span>
          <span v-if="currentStock > 10">库存充足 ({{ currentStock }} 件)</span>
          <span v-else-if="currentStock > 0">库存紧张 (仅剩 {{ currentStock }} 件)</span>
          <span v-else>暂时缺货</span>
        </div>

        <!-- SKU 规格选择 -->
        <div class="sku-section">
          <h4 class="section-label">选择规格</h4>
          <div v-if="skus.length" class="sku-list">
            <div
              v-for="sku in skus"
              :key="sku.id"
              class="sku-card"
              :class="{ selected: selectedSku?.id === sku.id }"
              @click="selectedSku = sku"
            >
              <span class="sku-name">{{ sku.name || sku.skuCode || 'SKU #' + sku.id }}</span>
              <span class="sku-price">¥{{ formatPrice(sku.price) }}</span>
            </div>
          </div>
          <p v-else class="no-sku">暂无规格可选</p>
        </div>

        <!-- 购买区 -->
        <div v-if="selectedSku && currentStock > 0" class="action-bar">
          <div class="qty-control">
            <button class="qty-btn" :disabled="quantity <= 1" @click="quantity--">−</button>
            <span class="qty-value">{{ quantity }}</span>
            <button class="qty-btn" :disabled="quantity >= 99" @click="quantity++">+</button>
          </div>
          <button
            class="add-cart-btn"
            :class="{ loading: adding }"
            :disabled="adding || currentStock <= 0"
            @click="handleAddCart"
          >
            <el-icon v-if="!adding" :size="18"><ShoppingCart /></el-icon>
            <span v-if="!adding">加入购物车</span>
            <span v-else class="btn-spinner"></span>
          </button>
        </div>

        <div v-else-if="selectedSku && currentStock <= 0" class="action-bar">
          <button class="sold-out-btn" disabled>已售罄</button>
        </div>

        <!-- 元数据 -->
        <div class="detail-meta">
          <span>创建时间：{{ spu.createTime }}</span>
          <span>更新时间：{{ spu.updateTime }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Goods, ShoppingCart } from '@element-plus/icons-vue'
import { getSpuDetail, getSkusBySpu } from '../api/product'
import { addToCart } from '../api/cart'
import { batchGetInventory } from '../api/inventory'
import { useUserStore } from '../../../stores/user'

const route = useRoute()
const store = useUserStore()
const loading = ref(false)
const adding = ref(false)
const spu = ref(null)
const skus = ref([])
const selectedSku = ref(null)
const quantity = ref(1)
const inventoryMap = ref({})

const firstImage = (value) => String(value || '').split(/[\n,]/).map(item => item.trim()).find(Boolean) || ''
// A selected SKU image takes precedence; the product main image is the fallback.
const currentImage = computed(() => firstImage(selectedSku.value?.images) || firstImage(spu.value?.mainImage) || firstImage(spu.value?.images))

const currentStock = computed(() => {
  if (!selectedSku.value) return 0
  const inv = inventoryMap.value[selectedSku.value.id]
  return inv?.availableStock ?? selectedSku.value.stock ?? 0
})

const stockLevel = computed(() => {
  if (currentStock.value > 10) return 'high'
  if (currentStock.value > 0) return 'low'
  return 'none'
})

const formatPrice = (val) => {
  const n = Number(val)
  return Number.isInteger(n) ? String(n) : n.toFixed(2)
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const [spuRes, skuRes] = await Promise.all([
      getSpuDetail(route.params.id),
      getSkusBySpu(route.params.id),
    ])
    spu.value = spuRes.data
    skus.value = skuRes.data || []
    if (skus.value.length) {
      selectedSku.value = skus.value[0]
      const ids = skus.value.map(s => s.id)
      const invRes = await batchGetInventory(ids)
      if (invRes.data) {
        invRes.data.forEach(inv => { inventoryMap.value[inv.skuId] = inv })
      }
    }
  } finally {
    loading.value = false
  }
}

const handleAddCart = async () => {
  if (!selectedSku.value) {
    ElMessage.warning('请选择规格')
    return
  }
  adding.value = true
  try {
    await addToCart({ userId: store.user.id, skuId: selectedSku.value.id, quantity: quantity.value })
    store.notifyCartChange()
    ElMessage.success(`已添加「${spu.value.name}」到购物车`)
    quantity.value = 1
  } catch { /* handled */ } finally {
    adding.value = false
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.detail-page {
  background: #fff;
  border-radius: 16px;
  padding: 24px 28px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

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

/* 内容布局 */
.detail-content {
  display: flex;
  gap: 48px;
}

/* 左侧图片 */
.detail-gallery {
  width: 440px;
  flex-shrink: 0;
}

.gallery-main {
  width: 100%;
  height: 440px;
  border-radius: 14px;
  overflow: hidden;
}

.product-main-image {
  width: 100%;
  height: 100%;
  display: block;
  background: #fff;
}

.img-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #eef2f5 0%, #e4e8ed 50%, #f0f3f7 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #bec4cd;
  position: relative;
}

.img-brand {
  font-size: 13px;
  color: #999;
  letter-spacing: 1px;
}

/* 右侧信息 */
.detail-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-header {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.product-name {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
  letter-spacing: -0.3px;
  line-height: 1.3;
}

.product-brand {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.product-shop {
  font-size: 14px;
  color: #278d69;
  margin: 0;
  font-weight: 500;
}

.product-desc {
  font-size: 14px;
  color: #777;
  margin: 0;
  line-height: 1.6;
}

.status-badge {
  display: inline-block;
  align-self: flex-start;
  padding: 3px 10px;
  font-size: 12px;
  border-radius: 4px;
  margin-top: 4px;
}

.status-badge.on-sale {
  background: rgba(82,196,26,0.1);
  color: #52c41a;
}

.status-badge.off-sale {
  background: rgba(0,0,0,0.05);
  color: #999;
}

/* 价格区 */
.price-box {
  background: linear-gradient(135deg, #fff9f6 0%, #fff5f0 100%);
  padding: 16px 20px;
  border-radius: 12px;
  border: 1px solid rgba(255,107,53,0.1);
}

.price-currency {
  font-size: 16px;
  font-weight: 600;
  color: #FF6B35;
}

.price-number {
  font-size: 32px;
  font-weight: 800;
  color: #FF6B35;
  letter-spacing: -1px;
  line-height: 1;
}

.price-sku-name {
  font-size: 13px;
  color: #999;
  margin-left: 8px;
}

/* 库存提示 */
.stock-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
}

.stock-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.stock-dot.high { background: #52c41a; box-shadow: 0 0 4px rgba(82,196,26,0.3); }
.stock-dot.low { background: #faad14; box-shadow: 0 0 4px rgba(250,173,20,0.3); }
.stock-dot.none { background: #ccc; }

/* SKU 选择区 */
.sku-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.section-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.sku-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.sku-card {
  padding: 10px 18px;
  border: 2px solid #ebe9e5;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 100px;
  background: #fafaf8;
}

.sku-card:hover {
  border-color: #FF6B35;
  background: #fff;
}

.sku-card.selected {
  border-color: #FF6B35;
  background: rgba(255,107,53,0.05);
  box-shadow: 0 0 0 3px rgba(255,107,53,0.08);
}

.sku-name {
  font-size: 13px;
  font-weight: 500;
  color: #333;
}

.sku-card.selected .sku-name { color: #FF6B35; }

.sku-price {
  font-size: 14px;
  font-weight: 600;
  color: #e55a2b;
}

.no-sku {
  font-size: 13px;
  color: #bbb;
  margin: 0;
}

/* 操作栏 */
.action-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-top: 4px;
}

.qty-control {
  display: flex;
  align-items: center;
  border: 1px solid #e4e1dc;
  border-radius: 10px;
  overflow: hidden;
  height: 46px;
}

.qty-btn {
  width: 38px;
  height: 100%;
  border: none;
  background: #f8f7f5;
  font-size: 18px;
  color: #666;
  cursor: pointer;
  transition: all 0.15s;
}

.qty-btn:hover:not(:disabled) {
  background: #efedeb;
  color: #333;
}

.qty-btn:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.qty-value {
  min-width: 48px;
  text-align: center;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.add-cart-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 46px;
  padding: 0 28px;
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
  flex: 1;
}

.add-cart-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255,107,53,0.35);
}

.add-cart-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.sold-out-btn {
  height: 46px;
  padding: 0 28px;
  border: 2px solid #e4e1dc;
  border-radius: 12px;
  background: #f5f3f0;
  color: #999;
  font-size: 15px;
  font-weight: 500;
  cursor: not-allowed;
  flex: 1;
}

.btn-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 元数据 */
.detail-meta {
  display: flex;
  gap: 24px;
  padding-top: 12px;
  border-top: 1px solid #f0efed;
  font-size: 12px;
  color: #ccc;
}

/* 响应式 */
@media (max-width: 768px) {
  .detail-content {
    flex-direction: column;
    gap: 24px;
  }

  .detail-gallery {
    width: 100%;
  }

  .gallery-main {
    height: 280px;
  }

  .product-name {
    font-size: 20px;
  }

  .price-number {
    font-size: 26px;
  }

  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
