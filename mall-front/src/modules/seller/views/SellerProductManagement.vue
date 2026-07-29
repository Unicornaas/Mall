<template>
  <section class="product-page">
    <div class="filter-panel">
      <el-form :inline="true" :model="query" @submit.prevent="searchProducts">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" clearable placeholder="商品名或品牌" @keyup.enter="searchProducts" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.categoryId" clearable placeholder="全部分类" class="filter-select">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" class="filter-select">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="searchProducts">查询</el-button>
          <el-button :icon="Refresh" @click="resetProducts">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-panel">
      <div class="table-heading">
        <div>
          <h2>我的商品</h2>
          <p>商品仅归当前店铺管理，新建商品默认下架。</p>
        </div>
        <el-button type="primary" :icon="Plus" @click="openProductDialog()">新增商品</el-button>
      </div>

      <el-table v-loading="loading" :data="products" stripe>
        <el-table-column label="商品" min-width="245">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image v-if="row.mainImage" :src="row.mainImage" fit="cover" class="product-image">
                <template #error><div class="image-fallback"><el-icon><Picture /></el-icon></div></template>
              </el-image>
              <div v-else class="image-fallback"><el-icon><Picture /></el-icon></div>
              <div class="product-info">
                <strong>{{ row.name }}</strong>
                <span>{{ row.brand || '未设置品牌' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" min-width="120">
          <template #default="{ row }">{{ categoryName(row.categoryId) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="165" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openProductDialog(row)">编辑</el-button>
            <el-button link type="primary" @click="openSkuDrawer(row)">规格</el-button>
            <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="toggleProductStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无商品" /></template>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchProducts"
          @current-change="fetchProducts"
        />
      </div>
    </div>

    <el-dialog v-model="productDialog.visible" :title="productDialog.editing ? '编辑商品' : '新增商品'" width="620px" destroy-on-close>
      <el-form ref="productFormRef" :model="productDialog.form" :rules="productRules" label-width="92px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productDialog.form.name" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="所属分类" prop="categoryId">
          <el-select v-model="productDialog.form.categoryId" placeholder="请选择分类" class="full-width">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌"><el-input v-model="productDialog.form.brand" /></el-form-item>
        <el-form-item label="主图链接"><el-input v-model="productDialog.form.mainImage" placeholder="请输入图片 URL" /></el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="productDialog.form.description" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="productDialog.submitting" @click="submitProduct">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="skuDrawer.visible" :title="`${skuDrawer.product?.name || ''} · 商品规格`" size="720px" destroy-on-close>
      <div class="drawer-toolbar">
        <p>维护规格价格、初始库存及销售状态。</p>
        <el-button type="primary" :icon="Plus" @click="openSkuDialog()">新增规格</el-button>
      </div>
      <el-table v-loading="skuDrawer.loading" :data="skuDrawer.records" stripe>
        <el-table-column prop="name" label="规格名称" min-width="130" />
        <el-table-column prop="skuCode" label="SKU 编码" min-width="120" />
        <el-table-column label="价格" width="100"><template #default="{ row }">¥{{ formatPrice(row.price) }}</template></el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openSkuDialog(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="toggleSkuStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无商品规格" /></template>
      </el-table>
    </el-drawer>

    <el-dialog v-model="skuDialog.visible" :title="skuDialog.editing ? '编辑规格' : '新增规格'" width="560px" destroy-on-close>
      <el-form ref="skuFormRef" :model="skuDialog.form" :rules="skuRules" label-width="88px">
        <el-form-item label="规格名称" prop="name"><el-input v-model="skuDialog.form.name" /></el-form-item>
        <el-form-item label="SKU 编码"><el-input v-model="skuDialog.form.skuCode" /></el-form-item>
        <el-form-item label="价格" prop="price"><el-input-number v-model="skuDialog.form.price" :min="0.01" :precision="2" class="full-width" /></el-form-item>
        <el-form-item label="库存" prop="stock"><el-input-number v-model="skuDialog.form.stock" :min="0" class="full-width" /></el-form-item>
        <el-form-item label="规格描述"><el-input v-model="skuDialog.form.specs" placeholder="如：颜色：黑色；容量：256G" /></el-form-item>
        <el-form-item label="图片链接"><el-input v-model="skuDialog.form.images" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="skuDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="skuDialog.submitting" @click="submitSku">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  createSellerProduct,
  createSellerSku,
  getProductCategories,
  getSellerProductPage,
  getSellerSkus,
  updateSellerProduct,
  updateSellerProductStatus,
  updateSellerSku,
  updateSellerSkuStatus,
} from '../api/product'

const loading = ref(false)
const products = ref([])
const total = ref(0)
const categories = ref([])
const productFormRef = ref()
const skuFormRef = ref()

const query = reactive({ pageNum: 1, pageSize: 20, keyword: '', categoryId: undefined, status: undefined })
const productDialog = reactive({ visible: false, editing: false, submitting: false, form: {} })
const skuDrawer = reactive({ visible: false, loading: false, product: null, records: [] })
const skuDialog = reactive({ visible: false, editing: false, submitting: false, form: {} })

const productRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
}
const skuRules = {
  name: [{ required: true, message: '请输入规格名称', trigger: 'blur' }],
  price: [{ required: true, message: '请设置价格', trigger: 'change' }],
  stock: [{ required: true, message: '请设置库存', trigger: 'change' }],
}

const categoryMap = computed(() => new Map(categories.value.map(item => [Number(item.id), item.name])))
const categoryName = (id) => categoryMap.value.get(Number(id)) || '-'
const formatPrice = (price) => Number(price || 0).toFixed(2)

const fetchCategories = async () => {
  const res = await getProductCategories()
  categories.value = (res.data || []).filter(item => item.status === 1)
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await getSellerProductPage(query)
    products.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const searchProducts = () => {
  query.pageNum = 1
  fetchProducts()
}

const resetProducts = () => {
  Object.assign(query, { pageNum: 1, pageSize: 20, keyword: '', categoryId: undefined, status: undefined })
  fetchProducts()
}

const openProductDialog = (product) => {
  productDialog.editing = Boolean(product)
  productDialog.form = product
    ? { ...product }
    : { name: '', categoryId: undefined, brand: '', mainImage: '', images: '', description: '' }
  productDialog.visible = true
}

const submitProduct = async () => {
  const valid = await productFormRef.value.validate().catch(() => false)
  if (!valid) return
  productDialog.submitting = true
  try {
    const data = {
      ...productDialog.form,
      images: productDialog.form.images || productDialog.form.mainImage || '',
    }
    if (productDialog.editing) {
      await updateSellerProduct(productDialog.form.id, data)
    } else {
      await createSellerProduct(data)
    }
    ElMessage.success(productDialog.editing ? '商品已更新' : '商品已创建，请添加规格后上架')
    productDialog.visible = false
    await fetchProducts()
  } finally {
    productDialog.submitting = false
  }
}

const toggleProductStatus = async (product) => {
  const nextStatus = product.status === 1 ? 0 : 1
  const action = nextStatus === 1 ? '上架' : '下架'
  await ElMessageBox.confirm(`确认${action}商品“${product.name}”吗？`, '商品状态变更', { type: 'warning' })
  await updateSellerProductStatus(product.id, nextStatus)
  ElMessage.success(`商品已${action}`)
  await fetchProducts()
}

const loadSkus = async () => {
  skuDrawer.loading = true
  try {
    const res = await getSellerSkus(skuDrawer.product.id)
    skuDrawer.records = res.data || []
  } finally {
    skuDrawer.loading = false
  }
}

const openSkuDrawer = async (product) => {
  skuDrawer.product = product
  skuDrawer.visible = true
  await loadSkus()
}

const openSkuDialog = (sku) => {
  skuDialog.editing = Boolean(sku)
  skuDialog.form = sku
    ? { ...sku }
    : { spuId: skuDrawer.product.id, name: '', skuCode: '', price: 0.01, stock: 0, specs: '', images: '' }
  skuDialog.visible = true
}

const submitSku = async () => {
  const valid = await skuFormRef.value.validate().catch(() => false)
  if (!valid) return
  skuDialog.submitting = true
  try {
    const data = { ...skuDialog.form, spuId: skuDrawer.product.id }
    if (skuDialog.editing) {
      await updateSellerSku(skuDialog.form.id, data)
    } else {
      await createSellerSku(skuDrawer.product.id, data)
    }
    ElMessage.success(skuDialog.editing ? '规格已更新' : '规格已创建')
    skuDialog.visible = false
    await loadSkus()
  } finally {
    skuDialog.submitting = false
  }
}

const toggleSkuStatus = async (sku) => {
  const nextStatus = sku.status === 1 ? 0 : 1
  const action = nextStatus === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确认${action}规格“${sku.name}”吗？`, '规格状态变更', { type: 'warning' })
  await updateSellerSkuStatus(sku.id, nextStatus)
  ElMessage.success(`规格已${action}`)
  await loadSkus()
}

onMounted(async () => {
  await Promise.all([fetchCategories(), fetchProducts()])
})
</script>

<style scoped>
.product-page { max-width: 1280px; margin: 0 auto; }
.filter-panel, .table-panel { background: #fff; border: 1px solid #e9edf4; border-radius: 8px; box-shadow: 0 3px 12px rgba(32, 50, 80, .025); }
.filter-panel { padding: 20px 22px 4px; }
.filter-select { width: 145px; }
.table-panel { margin-top: 18px; overflow: hidden; }
.table-heading { display: flex; align-items: center; justify-content: space-between; padding: 22px; }
.table-heading h2 { margin: 0 0 7px; color: #273246; font-size: 17px; }
.table-heading p, .drawer-toolbar p { margin: 0; color: #99a3b5; font-size: 13px; }
.product-cell { display: flex; align-items: center; gap: 11px; }
.product-image, .image-fallback { width: 42px; height: 42px; flex: 0 0 42px; border-radius: 6px; overflow: hidden; }
.image-fallback { display: grid; place-items: center; background: #f1f4f9; color: #9eabc1; }
.product-info { min-width: 0; }
.product-info strong, .product-info span { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-info strong { color: #30394a; font-size: 13px; }
.product-info span { margin-top: 3px; color: #9ca5b4; font-size: 12px; }
.pagination-wrap { display: flex; justify-content: flex-end; padding: 18px 22px; border-top: 1px solid #edf0f5; }
.full-width { width: 100%; }
.drawer-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 18px; }
@media (max-width: 760px) {
  .filter-panel :deep(.el-form-item) { margin-right: 0; }
  .table-heading, .drawer-toolbar { align-items: flex-start; gap: 12px; flex-direction: column; }
  .pagination-wrap { justify-content: center; overflow-x: auto; }
}
</style>
