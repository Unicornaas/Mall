<template>
  <section class="dashboard">
    <div class="welcome-card">
      <div>
        <p>欢迎回来，{{ store.user?.nickname || store.user?.username || '管理员' }}</p>
        <h2>平台运营概况</h2>
        <span>实时掌握平台用户、订单、支付和库存情况。</span>
      </div>
      <div class="welcome-actions">
        <el-button class="refresh-button" :loading="loading" @click="fetchDashboard">
          <el-icon><Refresh /></el-icon>刷新数据
        </el-button>
        <el-icon class="welcome-icon"><DataAnalysis /></el-icon>
      </div>
    </div>

    <div v-loading="loading" class="dashboard-body">
      <div class="metric-grid">
        <article v-for="item in metrics" :key="item.label" class="metric-card">
          <div class="metric-icon" :class="item.tone"><el-icon><component :is="item.icon" /></el-icon></div>
          <div><p>{{ item.label }}</p><strong>{{ item.value }}</strong><span>{{ item.hint }}</span></div>
        </article>
      </div>

      <div class="content-grid">
        <div class="panel trend-panel">
          <div class="panel-heading"><div><h3>近 7 天支付金额</h3><p>按支付成功时间统计平台订单金额</p></div><el-tag type="primary" effect="plain">趋势</el-tag></div>
          <div class="trend-chart">
            <div class="axis-labels"><span>¥{{ formatAmount(trendMax) }}</span><span>¥{{ formatAmount(trendMax / 2) }}</span><span>¥0.00</span></div>
            <div class="bars">
              <div v-for="item in trend" :key="item.day" class="bar-item">
                <div class="bar-track"><div class="bar-value" :style="{ height: `${barHeight(item.amount)}%` }" :title="`¥${formatAmount(item.amount)}`"></div></div>
                <span>{{ shortDay(item.day) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="panel alert-panel">
          <div class="panel-heading"><div><h3>库存预警</h3><p>可用库存低于等于安全库存</p></div><el-button link type="warning" @click="router.push('/admin/inventory')">处理预警</el-button></div>
          <div v-if="stockAlerts.length" class="alert-list">
            <div v-for="item in stockAlerts" :key="item.skuId" class="alert-item">
              <div class="alert-main"><strong>{{ item.productName || '-' }}</strong><span>{{ item.skuName || item.skuCode || `SKU ${item.skuId}` }}</span></div>
              <div class="alert-stock"><strong>{{ item.availableStock }}</strong><span>/ 安全 {{ item.safetyStock }}</span></div>
            </div>
          </div>
          <el-empty v-else description="暂无库存预警" :image-size="70" />
        </div>
      </div>

      <div class="panel latest-panel">
        <div class="panel-heading"><div><h3>最新订单</h3><p>最近创建的 10 条平台订单</p></div><el-button link type="primary" @click="router.push('/admin/orders')">查看全部</el-button></div>
        <el-table :data="latestOrders" size="small" stripe>
          <el-table-column prop="orderNo" label="订单号" min-width="185" />
          <el-table-column label="用户" min-width="130"><template #default="{ row }">{{ row.username || `用户 ${row.userId}` }}</template></el-table-column>
          <el-table-column label="金额" width="120" align="right"><template #default="{ row }">¥{{ formatAmount(row.totalAmount) }}</template></el-table-column>
          <el-table-column label="状态" width="105"><template #default="{ row }"><el-tag size="small" :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag></template></el-table-column>
          <el-table-column label="支付时间" min-width="165"><template #default="{ row }">{{ formatTime(row.payTime) }}</template></el-table-column>
          <el-table-column label="创建时间" min-width="165"><template #default="{ row }">{{ formatTime(row.createTime) }}</template></el-table-column>
          <template #empty><el-empty description="暂无订单" :image-size="60" /></template>
        </el-table>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Box, CreditCard, DataAnalysis, Goods, Refresh, Tickets, UserFilled, Warning } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../../stores/user'
import { getAdminDashboardLatestOrders, getAdminDashboardOrderTrend, getAdminDashboardOverview, getAdminDashboardStockAlerts } from '../api/adminDashboard'

const router = useRouter()
const store = useUserStore()
const loading = ref(false)
const overview = ref({})
const trend = ref([])
const latestOrders = ref([])
const stockAlerts = ref([])

const formatAmount = (value) => Number(value || 0).toFixed(2)
const formatTime = (value) => value ? String(value).replace('T', ' ').slice(0, 19) : '-'
const shortDay = (value) => value ? String(value).slice(5) : '-'
const statusText = (status) => ({ 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消' }[Number(status)] || '未知')
const statusType = (status) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'success', 4: 'info' }[Number(status)] || 'info')

const metrics = computed(() => [
  { label: '今日订单', value: overview.value.todayOrderCount || 0, hint: '当天创建，含待支付订单', icon: Tickets, tone: 'purple' },
  { label: '今日支付金额', value: `¥${formatAmount(overview.value.todayPaymentAmount)}`, hint: '支付成功订单金额', icon: CreditCard, tone: 'blue' },
  { label: '待支付订单', value: overview.value.pendingPaymentOrderCount || 0, hint: '当前等待买家支付', icon: Tickets, tone: 'orange' },
  { label: '待处理退款', value: overview.value.pendingRefundCount || 0, hint: '等待平台审核', icon: Warning, tone: 'red' },
  { label: '用户总数', value: overview.value.userCount || 0, hint: `今日新增 ${overview.value.todayNewUserCount || 0}`, icon: UserFilled, tone: 'green' },
  { label: '今日新增用户', value: overview.value.todayNewUserCount || 0, hint: '当天注册用户数', icon: UserFilled, tone: 'teal' },
  { label: '上架商品', value: overview.value.onSaleProductCount || 0, hint: '当前可展示商品', icon: Goods, tone: 'indigo' },
  { label: '库存预警 SKU', value: overview.value.warningStockSkuCount || 0, hint: '需要及时补货', icon: Box, tone: 'yellow' },
])

const trendMax = computed(() => Math.max(...trend.value.map(item => Number(item.amount || 0)), 0))
const barHeight = (amount) => trendMax.value > 0 ? Math.max(Number(amount || 0) / trendMax.value * 100, 3) : 3

const buildTrend = (rows) => {
  const values = new Map((rows || []).map(item => [item.day, item]))
  const result = []
  const now = new Date()
  for (let offset = 6; offset >= 0; offset -= 1) {
    const date = new Date(now)
    date.setHours(0, 0, 0, 0)
    date.setDate(date.getDate() - offset)
    const day = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    result.push(values.get(day) || { day, orderCount: 0, amount: 0 })
  }
  return result
}

const fetchDashboard = async () => {
  loading.value = true
  try {
    const [overviewRes, trendRes, ordersRes, alertsRes] = await Promise.all([
      getAdminDashboardOverview(),
      getAdminDashboardOrderTrend(7),
      getAdminDashboardLatestOrders(10),
      getAdminDashboardStockAlerts(10),
    ])
    overview.value = overviewRes.data || {}
    trend.value = buildTrend(trendRes.data)
    latestOrders.value = ordersRes.data || []
    stockAlerts.value = alertsRes.data || []
  } finally {
    loading.value = false
  }
}

onMounted(fetchDashboard)
</script>

<style scoped>
.dashboard { max-width: 1280px; margin: 0 auto; }.welcome-card { min-height: 150px; display: flex; align-items: center; justify-content: space-between; padding: 28px 36px; overflow: hidden; border-radius: 18px; color: #fff; background: radial-gradient(circle at 85% 15%, rgba(124,154,255,.5), transparent 28%), linear-gradient(120deg, #293b74, #465fd0); box-shadow: 0 14px 30px rgba(56,79,174,.2); }.welcome-card p { margin: 0 0 9px; color: rgba(255,255,255,.72); font-size: 14px; }.welcome-card h2 { margin: 0 0 9px; font-size: 27px; font-weight: 600; }.welcome-card span { color: rgba(255,255,255,.68); font-size: 13px; }.welcome-actions { display: flex; align-items: center; gap: 25px; }.refresh-button { color: #334a9c; border: none; }.welcome-icon { margin-right: 22px; font-size: 82px; color: rgba(255,255,255,.16); }.dashboard-body { min-height: 300px; }.metric-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; margin-top: 22px; }.metric-card, .panel { background: #fff; border: 1px solid #e9edf4; border-radius: 14px; box-shadow: 0 3px 12px rgba(32,50,80,.025); }.metric-card { display: flex; align-items: center; gap: 13px; min-height: 105px; padding: 18px; }.metric-icon { display: grid; place-items: center; width: 45px; height: 45px; flex: 0 0 45px; border-radius: 12px; font-size: 21px; }.metric-icon.blue { color: #5278ef; background: #edf2ff; }.metric-icon.orange { color: #ed8a35; background: #fff3e8; }.metric-icon.purple { color: #8b6ae6; background: #f4efff; }.metric-icon.green { color: #42ae7d; background: #eaf8f1; }.metric-icon.red { color: #d45f62; background: #ffebec; }.metric-icon.teal { color: #1b9c9c; background: #e4f8f7; }.metric-icon.indigo { color: #5668bb; background: #eef0ff; }.metric-icon.yellow { color: #b58b2a; background: #fff7dc; }.metric-card p, .metric-card span { margin: 0; display: block; color: #9aa4b5; font-size: 12px; }.metric-card strong { display: block; margin: 4px 0; color: #273246; font-size: 23px; }.content-grid { display: grid; grid-template-columns: minmax(0, 1.55fr) minmax(300px, 1fr); gap: 18px; margin-top: 18px; }.panel { min-width: 0; padding: 20px 22px; }.panel-heading { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 15px; }.panel-heading h3 { margin: 0 0 6px; color: #30394a; font-size: 16px; }.panel-heading p { margin: 0; color: #a0a8b8; font-size: 12px; }.trend-chart { display: flex; height: 210px; padding-top: 6px; }.axis-labels { display: flex; flex-direction: column; justify-content: space-between; width: 58px; padding: 0 8px 24px 0; color: #a0a8b8; font-size: 10px; text-align: right; }.bars { display: flex; flex: 1; align-items: stretch; justify-content: space-around; gap: 10px; border-bottom: 1px solid #e9edf4; background: repeating-linear-gradient(to bottom, transparent 0, transparent 49px, #f1f3f7 50px); }.bar-item { display: flex; flex: 1; flex-direction: column; align-items: center; justify-content: flex-end; min-width: 25px; }.bar-track { display: flex; align-items: flex-end; width: min(35px, 65%); height: calc(100% - 24px); }.bar-value { width: 100%; min-height: 3px; border-radius: 6px 6px 0 0; background: linear-gradient(180deg, #6f8cff, #465fd0); transition: height .25s ease; }.bar-item > span { height: 24px; padding-top: 7px; color: #8994a8; font-size: 11px; }.alert-list { display: flex; flex-direction: column; gap: 3px; }.alert-item { display: flex; align-items: center; justify-content: space-between; padding: 12px 8px; border-bottom: 1px solid #f0f2f6; }.alert-item:last-child { border-bottom: none; }.alert-main { min-width: 0; }.alert-main strong, .alert-main span { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.alert-main strong { color: #3c4659; font-size: 13px; }.alert-main span { margin-top: 5px; color: #a0a8b8; font-size: 11px; }.alert-stock { margin-left: 12px; text-align: right; white-space: nowrap; }.alert-stock strong { color: #d45f62; font-size: 20px; }.alert-stock span { color: #a0a8b8; font-size: 11px; }.latest-panel { margin-top: 18px; }.latest-panel :deep(.el-table th.el-table__cell) { color: #8b95a7; font-size: 12px; font-weight: 500; }
@media (max-width: 1100px) { .metric-grid { grid-template-columns: repeat(2, 1fr); }.content-grid { grid-template-columns: 1fr; } }
@media (max-width: 620px) { .welcome-card { padding: 25px; }.welcome-card h2 { font-size: 22px; }.welcome-actions { gap: 8px; }.welcome-icon { display: none; }.metric-grid { grid-template-columns: 1fr; }.panel { padding-inline: 14px; } }
</style>
