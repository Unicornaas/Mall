<template>
  <section class="seller-dashboard">
    <div class="welcome-card">
      <div>
        <p>欢迎回来，{{ store.user?.nickname || store.user?.username || '商家' }}</p>
        <h2>经营概览</h2>
        <span>实时掌握商品、库存和店铺订单情况。</span>
      </div>
      <div class="welcome-actions">
        <el-button class="refresh-button" :loading="loading" @click="fetchDashboard">
          <el-icon><Refresh /></el-icon>刷新数据
        </el-button>
        <el-icon class="welcome-icon"><Shop /></el-icon>
      </div>
    </div>

    <div v-loading="loading" class="dashboard-body">
      <div class="metric-grid">
        <div v-for="metric in metrics" :key="metric.label" class="metric-card">
          <div class="metric-icon" :class="`tone-${metric.tone}`"><el-icon><component :is="metric.icon" /></el-icon></div>
          <div class="metric-content"><span>{{ metric.label }}</span><strong>{{ metric.value }}</strong><small>{{ metric.note }}</small></div>
        </div>
      </div>

      <div class="content-grid">
        <div class="panel recent-panel">
          <div class="panel-heading"><div><h3>最近订单</h3><p>仅展示当前店铺的最新订单</p></div><el-button link type="primary" @click="router.push('/seller/orders')">查看全部</el-button></div>
          <el-table :data="recentOrders" size="small" stripe>
            <el-table-column prop="orderNo" label="订单号" min-width="175" />
            <el-table-column prop="receiverName" label="收货人" width="100" />
            <el-table-column label="金额" width="105" align="right"><template #default="{ row }">¥{{ formatAmount(row.sellerAmount) }}</template></el-table-column>
            <el-table-column label="状态" width="92"><template #default="{ row }"><el-tag size="small" :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag></template></el-table-column>
            <el-table-column label="时间" min-width="155"><template #default="{ row }">{{ formatTime(row.createTime) }}</template></el-table-column>
            <template #empty><el-empty description="暂无订单" :image-size="60" /></template>
          </el-table>
        </div>

        <div class="panel alert-panel">
          <div class="panel-heading"><div><h3>库存预警</h3><p>可用库存低于安全库存的 SKU</p></div><el-button link type="warning" @click="router.push('/seller/inventory')">管理库存</el-button></div>
          <div v-if="stockAlerts.length" class="alert-list">
            <div v-for="item in stockAlerts" :key="item.skuId" class="alert-item">
              <div class="alert-main"><strong>{{ item.productName || item.skuName || '-' }}</strong><span>{{ item.skuName || item.skuCode || `SKU ${item.skuId}` }}</span></div>
              <div class="alert-stock"><strong>{{ item.availableStock }}</strong><span>/ 安全 {{ item.safetyStock }}</span></div>
            </div>
          </div>
          <el-empty v-else description="库存充足" :image-size="70" />
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Box, CircleCheck, Goods, Refresh, Shop, Tickets, Warning } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../../stores/user'
import { getSellerDashboardOverview, getSellerRecentOrders, getSellerStockAlerts } from '../api/dashboard'

const router = useRouter()
const store = useUserStore()
const loading = ref(false)
const overview = ref({})
const recentOrders = ref([])
const stockAlerts = ref([])

const formatAmount = (value) => Number(value || 0).toFixed(2)
const formatTime = (value) => value ? String(value).replace('T', ' ').slice(0, 19) : '-'
const statusText = (status) => ({ 0: '待支付', 1: '待发货', 2: '已发货', 3: '已完成', 4: '已取消', 5: '部分发货' }[Number(status)] || '未知')
const statusType = (status) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'success', 4: 'info' }[Number(status)] || 'info')

const metrics = computed(() => [
  { label: '商品总数', value: overview.value.productCount || 0, note: `上架 ${overview.value.onSaleProductCount || 0} · 下架 ${overview.value.offSaleProductCount || 0}`, icon: Goods, tone: 'green' },
  { label: 'SKU 总数', value: overview.value.skuCount || 0, note: '当前店铺全部规格', icon: Box, tone: 'blue' },
  { label: '待发货订单', value: overview.value.pendingShipmentOrderCount || 0, note: '已支付待处理', icon: Tickets, tone: 'orange' },
  { label: '库存预警', value: overview.value.warningStockCount || 0, note: '需要及时补货', icon: Warning, tone: 'red' },
  { label: '今日订单', value: overview.value.todayOrderCount || 0, note: '今日创建的店铺订单', icon: CircleCheck, tone: 'purple' },
  { label: '今日销售额', value: `¥${formatAmount(overview.value.todaySalesAmount)}`, note: '今日已支付金额', icon: Tickets, tone: 'teal' },
])

const fetchDashboard = async () => {
  loading.value = true
  try {
    const [overviewRes, ordersRes, alertsRes] = await Promise.all([
      getSellerDashboardOverview(),
      getSellerRecentOrders(10),
      getSellerStockAlerts(10),
    ])
    overview.value = overviewRes.data || {}
    recentOrders.value = ordersRes.data || []
    stockAlerts.value = alertsRes.data || []
  } catch (error) {
    // 公共请求拦截器已负责展示接口错误，这里避免重复弹窗。
    console.error('加载商家经营概况失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchDashboard)
</script>

<style scoped>
.seller-dashboard { max-width: 1280px; margin: 0 auto; }.welcome-card { min-height: 150px; display: flex; align-items: center; justify-content: space-between; padding: 28px 36px; overflow: hidden; border-radius: 18px; color: #fff; background: radial-gradient(circle at 85% 15%, rgba(119,221,179,.42), transparent 28%), linear-gradient(120deg, #1f5b49, #278d69); box-shadow: 0 14px 30px rgba(30,120,90,.18); }.welcome-card p { margin: 0 0 9px; color: rgba(255,255,255,.72); font-size: 14px; }.welcome-card h2 { margin: 0 0 9px; font-size: 27px; font-weight: 600; }.welcome-card span { color: rgba(255,255,255,.68); font-size: 13px; }.welcome-actions { display: flex; align-items: center; gap: 25px; }.refresh-button { color: #245f4b; border: none; }.welcome-icon { margin-right: 22px; font-size: 82px; color: rgba(255,255,255,.16); }.dashboard-body { min-height: 260px; }.metric-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-top: 22px; }.metric-card { display: flex; align-items: center; gap: 14px; min-height: 112px; padding: 18px; background: #fff; border: 1px solid #e9edf4; border-radius: 14px; box-shadow: 0 3px 12px rgba(32,50,80,.025); }.metric-icon { display: flex; align-items: center; justify-content: center; width: 46px; height: 46px; flex: 0 0 46px; border-radius: 13px; font-size: 22px; }.tone-green { color: #278d69; background: #e6f6ef; }.tone-blue { color: #3578c8; background: #eaf2ff; }.tone-orange { color: #d9852d; background: #fff2df; }.tone-red { color: #d45f62; background: #ffebec; }.tone-purple { color: #7e64bd; background: #f0ebff; }.tone-teal { color: #1b9c9c; background: #e4f8f7; }.metric-content { min-width: 0; }.metric-content span, .metric-content strong, .metric-content small { display: block; }.metric-content span { color: #8490a4; font-size: 13px; }.metric-content strong { margin: 5px 0 4px; color: #263247; font-size: 24px; line-height: 1; }.metric-content small { overflow: hidden; color: #a3abba; font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }.content-grid { display: grid; grid-template-columns: minmax(0, 1.55fr) minmax(300px, 1fr); gap: 18px; margin-top: 18px; }.panel { min-width: 0; padding: 20px 22px; background: #fff; border: 1px solid #e9edf4; border-radius: 14px; box-shadow: 0 3px 12px rgba(32,50,80,.025); }.panel-heading { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 14px; }.panel-heading h3 { margin: 0 0 6px; color: #30394a; font-size: 16px; }.panel-heading p { margin: 0; color: #a0a8b8; font-size: 12px; }.recent-panel :deep(.el-table) { color: #4b5568; }.recent-panel :deep(.el-table th.el-table__cell) { color: #8b95a7; font-size: 12px; font-weight: 500; }.alert-list { display: flex; flex-direction: column; gap: 3px; }.alert-item { display: flex; align-items: center; justify-content: space-between; padding: 13px 8px; border-bottom: 1px solid #f0f2f6; }.alert-item:last-child { border-bottom: none; }.alert-main { min-width: 0; }.alert-main strong, .alert-main span { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.alert-main strong { color: #3c4659; font-size: 13px; }.alert-main span { margin-top: 5px; color: #a0a8b8; font-size: 11px; }.alert-stock { margin-left: 12px; text-align: right; white-space: nowrap; }.alert-stock strong { color: #d45f62; font-size: 20px; }.alert-stock span { color: #a0a8b8; font-size: 11px; }
@media (max-width: 1050px) { .metric-grid { grid-template-columns: repeat(2, 1fr); }.content-grid { grid-template-columns: 1fr; } }
@media (max-width: 620px) { .welcome-card { padding: 25px; }.welcome-card h2 { font-size: 22px; }.welcome-actions { gap: 8px; }.welcome-icon { display: none; }.metric-grid { grid-template-columns: 1fr; }.panel { padding-inline: 14px; }.recent-panel :deep(.el-table__body-wrapper) { overflow-x: auto; } }
</style>
