<template>
  <div class="catalog-page">
    <!-- ===== 左侧分类边栏 ===== -->
    <aside class="catalog-sidebar">
      <div class="sidebar-header">
        <h3 class="sidebar-title">全部分类</h3>
        <el-input
          v-model="categorySearch"
          placeholder="搜索分类..."
          :prefix-icon="Search"
          size="small"
          class="sidebar-search"
          clearable
        />
      </div>
      <nav class="category-nav">
        <div
          v-for="cat in filteredCategories"
          :key="cat.id"
          class="category-item"
          :class="{ active: activeCat === String(cat.id) }"
          @click="handleCatSelect(String(cat.id))"
        >
          <el-icon :size="16" class="cat-icon"><component :is="getCatIcon(cat.name)" /></el-icon>
          <span class="cat-name">{{ cat.name }}</span>
          <span class="cat-arrow">›</span>
        </div>
      </nav>
      <el-empty v-if="!filteredCategories.length && !loadingCategories" description="暂无分类" :image-size="48" />
    </aside>

    <!-- ===== 右侧主内容 ===== -->
    <section class="catalog-main">
      <!-- 分类标题 & 排序栏 -->
      <div class="catalog-toolbar">
        <div class="toolbar-left">
          <h2 class="current-cat">{{ currentCatName || '全部商品' }}</h2>
          <span class="result-count" v-if="sortedSpus.length">{{ sortedSpus.length }} 件商品</span>
        </div>
        <div class="toolbar-right">
          <button
            v-for="s in sorts"
            :key="s.key"
            class="sort-btn"
            :class="{ active: currentSort === s.key }"
            @click="currentSort = s.key"
          >
            {{ s.label }}
          </button>
        </div>
      </div>

      <!-- 加载中 -->
      <div v-if="loadingSpus" class="loading-wrap">
        <div class="skeleton-grid">
          <div v-for="i in 6" :key="i" class="skeleton-card">
            <div class="sk-img"></div>
            <div class="sk-line sk-title"></div>
            <div class="sk-line sk-sub"></div>
            <div class="sk-line sk-price"></div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-else-if="!sortedSpus.length" description="该分类暂无商品" :image-size="100" />

      <!-- 商品网格 -->
      <div v-else class="product-grid">
        <article
          v-for="spu in sortedSpus"
          :key="spu.id"
          class="product-card"
          @click="goDetail(spu.id)"
        >
          <!-- 图片区 -->
          <div class="card-image">
            <div class="img-placeholder">
              <el-icon :size="48"><Goods /></el-icon>
            </div>
            <span class="card-category-tag">{{ currentCatName }}</span>
            <span v-if="spu.status === 0" class="card-off-shelf">已下架</span>
          </div>

          <!-- 信息区 -->
          <div class="card-body">
            <h4 class="card-name">{{ spu.name }}</h4>
            <p class="card-brand" v-if="spu.brand">{{ spu.brand }}</p>
            <p class="card-desc" v-if="spu.description">{{ spu.description }}</p>

            <!-- 规格标签 -->
            <div class="card-specs" v-if="skuDataMap[spu.id]?.skuCount">
              <span class="spec-tag">{{ skuDataMap[spu.id].skuCount }} 种规格</span>
              <span v-if="skuDataMap[spu.id]?.minStock <= 10 && skuDataMap[spu.id]?.minStock > 0" class="spec-tag low-stock">库存紧张</span>
            </div>
          </div>

          <!-- 底部：价格 & 按钮 -->
          <div class="card-footer">
            <div class="card-price" v-if="skuDataMap[spu.id]?.minPrice != null">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ formatPrice(skuDataMap[spu.id].minPrice) }}</span>
              <span v-if="skuDataMap[spu.id].minPrice !== skuDataMap[spu.id].maxPrice" class="price-range">
                &nbsp;~ ¥{{ formatPrice(skuDataMap[spu.id].maxPrice) }}
              </span>
            </div>
            <div class="card-price" v-else>
              <span class="price-na">暂无报价</span>
            </div>
            <button
              class="add-cart-btn"
              :disabled="spu.status === 0 || !skuDataMap[spu.id]?.firstSkuId"
              @click.stop="handleAddCart(spu)"
            >
              <el-icon :size="15"><ShoppingCartFull /></el-icon>
              <span>加入购物车</span>
            </button>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  Search, Goods, ShoppingCartFull, Folder,
  Phone, Monitor, Present, Setting,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCategories, getSpusByCategory, getSkusBySpu } from '../api/product'
import { addToCart } from '../api/cart'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

// ====== 分类 ======
const categories = ref([])
const activeCat = ref('')
const currentCatName = ref('')
const categorySearch = ref('')
const loadingCategories = ref(false)

const filteredCategories = computed(() => {
  if (!categorySearch.value) return categories.value
  const q = categorySearch.value.toLowerCase()
  return categories.value.filter(c => c.name.toLowerCase().includes(q))
})

// 分类 → 图标映射
const iconMap = {
  phone: Phone, mobile: Phone, iphone: Phone, smartphone: Phone,
  computer: Monitor, laptop: Monitor, pc: Monitor, monitor: Monitor,
  gift: Present, present: Present,
  accessory: Setting,
}
const getCatIcon = (name) => {
  const lower = name.toLowerCase()
  for (const [key, icon] of Object.entries(iconMap)) {
    if (lower.includes(key)) return icon
  }
  return Folder
}

// ====== 商品 ======
const spus = ref([])
const loadingSpus = ref(false)
const skuDataMap = ref({})  // spuId → { minPrice, maxPrice, firstSkuId, skuCount, minStock }

const sorts = [
  { key: 'default', label: '综合' },
  { key: 'price_asc', label: '价格↑' },
  { key: 'price_desc', label: '价格↓' },
  { key: 'newest', label: '最新' },
]
const currentSort = ref('default')

const sortedSpus = computed(() => {
  const arr = [...spus.value]
  const map = skuDataMap.value
  switch (currentSort.value) {
    case 'price_asc':
      return arr.sort((a, b) => (map[a.id]?.minPrice ?? Infinity) - (map[b.id]?.minPrice ?? Infinity))
    case 'price_desc':
      return arr.sort((a, b) => (map[b.id]?.minPrice ?? -1) - (map[a.id]?.minPrice ?? -1))
    case 'newest':
      return arr.sort((a, b) => String(b.id).localeCompare(String(a.id)))
    default:
      return arr
  }
})

const formatPrice = (val) => {
  const num = Number(val)
  if (Number.isInteger(num)) return String(num)
  return num.toFixed(2)
}

// ====== 方法 ======
const fetchCategories = async () => {
  loadingCategories.value = true
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } finally {
    loadingCategories.value = false
  }
}

const fetchSpusForCategory = async (catId) => {
  loadingSpus.value = true
  spus.value = []
  skuDataMap.value = {}
  try {
    const res = await getSpusByCategory(catId)
    spus.value = res.data || []
    // 批量获取 SKU 价格
    if (spus.value.length) {
      fetchSkuPrices()
    }
  } finally {
    loadingSpus.value = false
  }
}

const fetchSkuPrices = async () => {
  const results = await Promise.allSettled(
    spus.value.map(s => getSkusBySpu(s.id))
  )
  const map = {}
  results.forEach((r, idx) => {
    const spuId = spus.value[idx].id
    if (r.status === 'fulfilled' && r.value.data?.length) {
      const skus = r.value.data
      const prices = skus.map(s => Number(s.price)).filter(p => !isNaN(p))
      const stocks = skus.map(s => s.stock ?? 0)
      map[spuId] = {
        minPrice: prices.length ? Math.min(...prices) : null,
        maxPrice: prices.length ? Math.max(...prices) : null,
        firstSkuId: skus[0]?.id,
        skuCount: skus.length,
        minStock: stocks.length ? Math.min(...stocks) : 0,
      }
    } else {
      map[spuId] = { minPrice: null, maxPrice: null, firstSkuId: null, skuCount: 0, minStock: 0 }
    }
  })
  skuDataMap.value = map
}

const handleCatSelect = (index) => {
  activeCat.value = index
  const cat = categories.value.find(c => String(c.id) === index)
  currentCatName.value = cat?.name || ''
  currentSort.value = 'default'
  fetchSpusForCategory(index)
}

const goDetail = (id) => {
  router.push(`/products/${id}`)
}

const handleAddCart = async (spu) => {
  const data = skuDataMap.value[spu.id]
  if (!data?.firstSkuId || !userStore.user) return
  try {
    await addToCart({
      userId: userStore.user.id,
      skuId: data.firstSkuId,
      quantity: 1,
    })
    userStore.notifyCartChange()
    ElMessage.success(`已添加「${spu.name}」到购物车`)
  } catch {
    // error handled by interceptor
  }
}

// ====== 生命周期 ======
onMounted(async () => {
  await fetchCategories()
  if (categories.value.length) {
    handleCatSelect(String(categories.value[0].id))
  }
})
</script>

<style scoped>
/* ===== 布局 ===== */
.catalog-page {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* ===== 侧边栏 ===== */
.catalog-sidebar {
  width: 230px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  position: sticky;
  top: 88px;
}

.sidebar-header {
  padding: 18px 16px 12px;
  border-bottom: 1px solid #f0efed;
}

.sidebar-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 12px;
}

.sidebar-search :deep(.el-input__wrapper) {
  background: #f8f7f5;
  border-radius: 8px;
  box-shadow: none;
}

.category-nav {
  padding: 8px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 2px;
}

.category-item:hover {
  background: rgba(255,107,53,0.04);
}

.category-item.active {
  background: rgba(255,107,53,0.08);
  color: #FF6B35;
  font-weight: 500;
}

.category-item.active .cat-arrow {
  opacity: 1;
  color: #FF6B35;
}

.cat-icon {
  color: #999;
  flex-shrink: 0;
}

.category-item.active .cat-icon {
  color: #FF6B35;
}

.cat-name {
  flex: 1;
  font-size: 13px;
  color: #555;
}

.category-item.active .cat-name {
  color: #FF6B35;
}

.cat-arrow {
  font-size: 14px;
  color: #ccc;
  opacity: 0;
  transition: all 0.2s;
}

.category-item:hover .cat-arrow {
  opacity: 1;
  color: #aaa;
}

/* ===== 主内容区 ===== */
.catalog-main {
  flex: 1;
  min-width: 0;
}

/* 工具栏 */
.catalog-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.toolbar-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.current-cat {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
  letter-spacing: -0.3px;
}

.result-count {
  font-size: 13px;
  color: #aaa;
}

.toolbar-right {
  display: flex;
  gap: 4px;
  background: #fff;
  border-radius: 10px;
  padding: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.sort-btn {
  padding: 7px 16px;
  border: none;
  background: transparent;
  font-size: 13px;
  color: #888;
  border-radius: 7px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 400;
  white-space: nowrap;
}

.sort-btn:hover {
  color: #444;
  background: rgba(0,0,0,0.03);
}

.sort-btn.active {
  background: #1a1a2e;
  color: #fff;
  font-weight: 500;
}

/* ===== 骨架屏 ===== */
.loading-wrap {
  padding: 20px 0;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.skeleton-card {
  background: #fff;
  border-radius: 14px;
  padding: 0 0 16px;
  overflow: hidden;
}

.sk-img {
  height: 200px;
  background: linear-gradient(90deg, #f0efed 25%, #e8e7e5 50%, #f0efed 75%);
  background-size: 200% 100%;
  animation: shimmer 1.6s infinite;
}

.sk-line {
  margin: 12px 16px 0;
  height: 14px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f0efed 25%, #e8e7e5 50%, #f0efed 75%);
  background-size: 200% 100%;
  animation: shimmer 1.6s infinite;
}

.sk-title { width: 70%; }
.sk-sub { width: 45%; height: 10px; }
.sk-price { width: 35%; height: 18px; margin-top: 14px; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ===== 商品网格 ===== */
.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

/* ===== 商品卡片 ===== */
.product-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 40px rgba(0,0,0,0.1);
}

/* 图片区 */
.card-image {
  position: relative;
  height: 210px;
  overflow: hidden;
}

.img-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #eef2f5 0%, #e4e8ed 50%, #f0f3f7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c5cad2;
  transition: transform 0.4s ease;
}

.product-card:hover .img-placeholder {
  transform: scale(1.04);
}

.card-category-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 3px 10px;
  background: rgba(26,26,46,0.7);
  color: #fff;
  font-size: 11px;
  border-radius: 4px;
  letter-spacing: 0.5px;
  backdrop-filter: blur(4px);
}

.card-off-shelf {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 3px 10px;
  background: rgba(200,200,200,0.8);
  color: #fff;
  font-size: 11px;
  border-radius: 4px;
}

/* 信息区 */
.card-body {
  padding: 14px 16px 0;
  flex: 1;
}

.card-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 4px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-brand {
  font-size: 12px;
  color: #999;
  margin: 0 0 4px;
}

.card-desc {
  font-size: 12px;
  color: #bbb;
  margin: 0 0 8px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-specs {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.spec-tag {
  display: inline-block;
  padding: 2px 8px;
  font-size: 11px;
  color: #888;
  background: #f5f3f0;
  border-radius: 4px;
}

.spec-tag.low-stock {
  color: #e55a2b;
  background: rgba(255,107,53,0.08);
}

/* 底部 */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 16px;
  margin-top: auto;
}

.card-price {
  display: flex;
  align-items: baseline;
  min-width: 0;
}

.price-symbol {
  font-size: 13px;
  font-weight: 600;
  color: #FF6B35;
  margin-right: 1px;
}

.price-value {
  font-size: 20px;
  font-weight: 700;
  color: #FF6B35;
  letter-spacing: -0.5px;
  line-height: 1;
}

.price-range {
  font-size: 12px;
  color: #ccc;
  white-space: nowrap;
}

.price-na {
  font-size: 13px;
  color: #ccc;
}

.add-cart-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 8px 14px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8F5E 100%);
  color: #fff;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 3px 10px rgba(255,107,53,0.2);
  white-space: nowrap;
  flex-shrink: 0;
}

.add-cart-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(255,107,53,0.3);
}

.add-cart-btn:active {
  transform: translateY(0);
}

.add-cart-btn:disabled {
  background: #d5d3ce;
  cursor: not-allowed;
  box-shadow: none;
}

.add-cart-btn:disabled:hover {
  transform: none;
}

/* ===== 响应式 ===== */
@media (max-width: 1100px) {
  .product-grid,
  .skeleton-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .catalog-page {
    flex-direction: column;
    gap: 16px;
  }

  .catalog-sidebar {
    width: 100%;
    position: static;
  }

  .category-nav {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }

  .category-item {
    flex: 0 0 auto;
    padding: 6px 12px;
    font-size: 12px;
  }

  .cat-arrow {
    display: none;
  }

  .product-grid,
  .skeleton-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .card-image {
    height: 150px;
  }

  .toolbar-right {
    gap: 0;
  }

  .sort-btn {
    padding: 6px 10px;
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .product-grid,
  .skeleton-grid {
    grid-template-columns: 1fr;
  }

  .catalog-toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
