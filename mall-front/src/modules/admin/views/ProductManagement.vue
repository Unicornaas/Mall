<template>
  <section class="product-page">
    <el-tabs v-model="activeTab" class="product-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="商品管理" name="products">
        <div class="filter-card">
          <el-form :inline="true" :model="query" @submit.prevent="searchProducts">
            <el-form-item label="关键词"><el-input v-model="query.keyword" clearable placeholder="商品名或品牌" @keyup.enter="searchProducts" /></el-form-item>
            <el-form-item label="分类">
              <el-select v-model="query.categoryId" clearable placeholder="全部分类" class="filter-select">
                <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="query.status" clearable placeholder="全部状态" class="filter-select">
                <el-option label="上架" :value="1" /><el-option label="下架" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item><el-button type="primary" :icon="Search" @click="searchProducts">查询</el-button><el-button :icon="Refresh" @click="resetProducts">重置</el-button></el-form-item>
          </el-form>
        </div>
        <div class="table-card">
          <div class="table-heading"><div><h2>商品列表</h2><p>维护商品基础信息、分类归属和上架状态。</p></div><el-button type="primary" :icon="Plus" @click="openProductDialog()">新增商品</el-button></div>
          <el-table v-loading="loading" :data="products" stripe>
            <el-table-column label="商品" min-width="245"><template #default="{ row }"><div class="product-cell"><el-image v-if="row.mainImage" :src="row.mainImage" fit="cover" class="product-image"><template #error><div class="image-fallback"><el-icon><Picture /></el-icon></div></template></el-image><div v-else class="image-fallback"><el-icon><Picture /></el-icon></div><div><strong>{{ row.name }}</strong><span>{{ row.brand || '未设置品牌' }}</span></div></div></template></el-table-column>
            <el-table-column label="分类" min-width="120"><template #default="{ row }">{{ categoryName(row.categoryId) }}</template></el-table-column>
            <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag></template></el-table-column>
            <el-table-column prop="createTime" label="创建时间" min-width="165" />
            <el-table-column label="操作" width="220" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openProductDialog(row)">编辑</el-button><el-button link type="primary" @click="openSkuDrawer(row)">规格</el-button><el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="toggleProductStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button></template></el-table-column>
          </el-table>
          <div class="pagination-wrap"><el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next" @size-change="fetchProducts" @current-change="fetchProducts" /></div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="分类管理" name="categories">
        <div class="table-card">
          <div class="table-heading"><div><h2>商品分类</h2><p>分类新增与编辑使用现有商品服务能力；分类删除暂不开放，避免影响关联商品。</p></div><el-button type="primary" :icon="Plus" @click="openCategoryDialog()">新增分类</el-button></div>
          <el-table :data="categories" stripe>
            <el-table-column prop="name" label="分类名称" min-width="180" />
            <el-table-column label="父分类" min-width="150"><template #default="{ row }">{{ row.parentId === 0 ? '顶级分类' : categoryName(row.parentId) }}</template></el-table-column>
            <el-table-column prop="sortOrder" label="排序" width="100" />
            <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template></el-table-column>
            <el-table-column prop="createTime" label="创建时间" min-width="165" />
            <el-table-column label="操作" width="100" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openCategoryDialog(row)">编辑</el-button></template></el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="productDialog.visible" :title="productDialog.editing ? '编辑商品' : '新增商品'" width="620px" destroy-on-close>
      <el-form ref="productFormRef" :model="productDialog.form" :rules="productRules" label-width="92px">
        <el-form-item label="商品名称" prop="name"><el-input v-model="productDialog.form.name" maxlength="100" show-word-limit /></el-form-item>
        <el-form-item label="所属分类" prop="categoryId"><el-select v-model="productDialog.form.categoryId" placeholder="请选择分类" class="full-width"><el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" /></el-select></el-form-item>
        <el-form-item label="品牌"><el-input v-model="productDialog.form.brand" /></el-form-item>
        <el-form-item label="主图链接"><el-input v-model="productDialog.form.mainImage" placeholder="可填写图片 URL" /></el-form-item>
        <el-form-item label="商品描述"><el-input v-model="productDialog.form.description" type="textarea" :rows="4" maxlength="500" show-word-limit /></el-form-item>
      </el-form>
      <template #footer><el-button @click="productDialog.visible = false">取消</el-button><el-button type="primary" :loading="productDialog.submitting" @click="submitProduct">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="categoryDialog.visible" :title="categoryDialog.editing ? '编辑分类' : '新增分类'" width="520px" destroy-on-close>
      <el-form ref="categoryFormRef" :model="categoryDialog.form" :rules="categoryRules" label-width="82px">
        <el-form-item label="分类名称" prop="name"><el-input v-model="categoryDialog.form.name" /></el-form-item>
        <el-form-item label="父分类" prop="parentId"><el-select v-model="categoryDialog.form.parentId" :disabled="categoryDialog.editing" class="full-width"><el-option label="顶级分类" :value="0" /><el-option v-for="category in categories.filter(item => item.id !== categoryDialog.form.id)" :key="category.id" :label="category.name" :value="category.id" /></el-select></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="categoryDialog.form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="categoryDialog.form.icon" placeholder="可选：图标 URL 或标识" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="categoryDialog.visible = false">取消</el-button><el-button type="primary" :loading="categoryDialog.submitting" @click="submitCategory">保存</el-button></template>
    </el-dialog>

    <el-drawer v-model="skuDrawer.visible" :title="`${skuDrawer.product?.name || ''} · 商品规格`" size="680px" destroy-on-close>
      <div class="drawer-toolbar"><p>规格价格和库存直接复用现有 SKU 管理接口。</p><el-button type="primary" :icon="Plus" @click="openSkuDialog()">新增规格</el-button></div>
      <el-table v-loading="skuDrawer.loading" :data="skuDrawer.records" stripe>
        <el-table-column prop="name" label="规格名称" min-width="135" /><el-table-column prop="skuCode" label="SKU 编码" min-width="125" /><el-table-column prop="price" label="价格" width="100"><template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template></el-table-column><el-table-column prop="stock" label="库存" width="80" /><el-table-column label="操作" width="80"><template #default="{ row }"><el-button link type="primary" @click="openSkuDialog(row)">编辑</el-button></template></el-table-column>
      </el-table>
    </el-drawer>

    <el-dialog v-model="skuDialog.visible" :title="skuDialog.editing ? '编辑规格' : '新增规格'" width="560px" destroy-on-close>
      <el-form ref="skuFormRef" :model="skuDialog.form" :rules="skuRules" label-width="88px"><el-form-item label="规格名称" prop="name"><el-input v-model="skuDialog.form.name" /></el-form-item><el-form-item label="SKU 编码"><el-input v-model="skuDialog.form.skuCode" /></el-form-item><el-form-item label="价格" prop="price"><el-input-number v-model="skuDialog.form.price" :min="0.01" :precision="2" class="full-width" /></el-form-item><el-form-item label="库存" prop="stock"><el-input-number v-model="skuDialog.form.stock" :min="0" class="full-width" /></el-form-item><el-form-item label="规格描述"><el-input v-model="skuDialog.form.specs" placeholder="如：颜色：黑色；容量：256G" /></el-form-item><el-form-item label="图片链接"><el-input v-model="skuDialog.form.images" /></el-form-item></el-form>
      <template #footer><el-button @click="skuDialog.visible = false">取消</el-button><el-button type="primary" :loading="skuDialog.submitting" @click="submitSku">保存</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { createAdminCategory, createAdminProduct, createAdminSku, getAdminCategories, getAdminProductPage, getAdminSkus, updateAdminCategory, updateAdminProduct, updateAdminProductStatus, updateAdminSku } from '../api/adminProduct'

const activeTab = ref('products')
const loading = ref(false)
const products = ref([])
const total = ref(0)
const categories = ref([])
const productFormRef = ref()
const categoryFormRef = ref()
const skuFormRef = ref()
const query = reactive({ pageNum: 1, pageSize: 20, keyword: '', categoryId: undefined, status: undefined })
const productDialog = reactive({ visible: false, editing: false, submitting: false, form: {} })
const categoryDialog = reactive({ visible: false, editing: false, submitting: false, form: {} })
const skuDrawer = reactive({ visible: false, loading: false, product: null, records: [] })
const skuDialog = reactive({ visible: false, editing: false, submitting: false, form: {} })

const productRules = { name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }], categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }] }
const categoryRules = { name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }], parentId: [{ required: true, message: '请选择父分类', trigger: 'change' }] }
const skuRules = { name: [{ required: true, message: '请输入规格名称', trigger: 'blur' }], price: [{ required: true, message: '请设置价格', trigger: 'change' }], stock: [{ required: true, message: '请设置库存', trigger: 'change' }] }

const categoryMap = computed(() => new Map(categories.value.map(item => [Number(item.id), item.name])))
const categoryName = (id) => categoryMap.value.get(Number(id)) || '-'

const fetchCategories = async () => { const res = await getAdminCategories(); categories.value = res.data || [] }
const fetchProducts = async () => { loading.value = true; try { const res = await getAdminProductPage(query); products.value = res.data?.records || []; total.value = res.data?.total || 0 } finally { loading.value = false } }
const searchProducts = () => { query.pageNum = 1; fetchProducts() }
const resetProducts = () => { Object.assign(query, { pageNum: 1, pageSize: 20, keyword: '', categoryId: undefined, status: undefined }); fetchProducts() }
const handleTabChange = (tab) => { if (tab === 'categories') fetchCategories() }

const openProductDialog = (product) => { productDialog.editing = !!product; productDialog.form = product ? { ...product } : { name: '', categoryId: undefined, brand: '', mainImage: '', images: '', description: '' }; productDialog.visible = true }
const submitProduct = async () => { const valid = await productFormRef.value.validate().catch(() => false); if (!valid) return; productDialog.submitting = true; try { const data = { ...productDialog.form, images: productDialog.form.images || productDialog.form.mainImage || '' }; if (productDialog.editing) await updateAdminProduct(productDialog.form.id, data); else await createAdminProduct(data); ElMessage.success(productDialog.editing ? '商品已更新' : '商品已创建'); productDialog.visible = false; await fetchProducts() } finally { productDialog.submitting = false } }
const toggleProductStatus = async (product) => { const nextStatus = product.status === 1 ? 0 : 1; const action = nextStatus === 1 ? '上架' : '下架'; await ElMessageBox.confirm(`确认${action}商品“${product.name}”吗？`, '商品状态变更', { type: 'warning' }); await updateAdminProductStatus(product.id, nextStatus); ElMessage.success(`商品已${action}`); await fetchProducts() }

const openCategoryDialog = (category) => { categoryDialog.editing = !!category; categoryDialog.form = category ? { ...category } : { parentId: 0, name: '', sortOrder: 0, icon: '' }; categoryDialog.visible = true }
const submitCategory = async () => { const valid = await categoryFormRef.value.validate().catch(() => false); if (!valid) return; categoryDialog.submitting = true; try { if (categoryDialog.editing) await updateAdminCategory(categoryDialog.form.id, categoryDialog.form); else await createAdminCategory(categoryDialog.form); ElMessage.success(categoryDialog.editing ? '分类已更新' : '分类已创建'); categoryDialog.visible = false; await fetchCategories() } finally { categoryDialog.submitting = false } }

const openSkuDrawer = async (product) => { skuDrawer.product = product; skuDrawer.visible = true; skuDrawer.loading = true; try { const res = await getAdminSkus(product.id); skuDrawer.records = res.data || [] } finally { skuDrawer.loading = false } }
const openSkuDialog = (sku) => { skuDialog.editing = !!sku; skuDialog.form = sku ? { ...sku } : { spuId: skuDrawer.product.id, name: '', skuCode: '', price: 0.01, stock: 0, specs: '', images: '' }; skuDialog.visible = true }
const submitSku = async () => { const valid = await skuFormRef.value.validate().catch(() => false); if (!valid) return; skuDialog.submitting = true; try { if (skuDialog.editing) await updateAdminSku(skuDialog.form.id, skuDialog.form); else await createAdminSku(skuDialog.form); ElMessage.success(skuDialog.editing ? '规格已更新' : '规格已创建'); skuDialog.visible = false; await openSkuDrawer(skuDrawer.product) } finally { skuDialog.submitting = false } }

onMounted(async () => { await fetchCategories(); await fetchProducts() })
</script>

<style scoped>
.product-page { max-width: 1280px; margin: 0 auto; }
.product-tabs :deep(.el-tabs__header) { margin-bottom: 18px; }
.filter-card, .table-card { background: #fff; border: 1px solid #e9edf4; border-radius: 14px; box-shadow: 0 3px 12px rgba(32,50,80,.025); }
.filter-card { padding: 20px 22px 4px; }.filter-select { width: 145px; }.table-card { margin-top: 18px; overflow: hidden; }.table-heading { display: flex; align-items: center; justify-content: space-between; padding: 22px; }.table-heading h2 { margin: 0 0 7px; color: #273246; font-size: 17px; }.table-heading p { margin: 0; color: #99a3b5; font-size: 13px; }.product-cell { display: flex; align-items: center; gap: 11px; }.product-image, .image-fallback { width: 42px; height: 42px; flex: 0 0 42px; border-radius: 8px; overflow: hidden; }.image-fallback { display: grid; place-items: center; background: #f1f4f9; color: #9eabc1; }.product-cell strong, .product-cell span { display: block; }.product-cell strong { color: #30394a; font-size: 13px; }.product-cell span { margin-top: 3px; color: #9ca5b4; font-size: 12px; }.pagination-wrap { display: flex; justify-content: flex-end; padding: 18px 22px; border-top: 1px solid #edf0f5; }.full-width { width: 100%; }.drawer-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 18px; }.drawer-toolbar p { margin: 0; color: #98a2b3; font-size: 13px; }
@media (max-width: 760px) { .filter-card :deep(.el-form-item) { margin-right: 0; }.table-heading { align-items: flex-start; gap: 12px; flex-direction: column; }.pagination-wrap { justify-content: center; overflow-x: auto; }.drawer-toolbar { align-items: flex-start; gap: 12px; flex-direction: column; } }
</style>
