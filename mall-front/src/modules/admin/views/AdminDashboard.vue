<template>
  <section class="dashboard">
    <div class="welcome-card">
      <div>
        <p>欢迎回来，{{ store.user?.nickname || store.user?.username || '管理员' }}</p>
        <h2>平台运营工作台已准备就绪</h2>
        <span>下一阶段将接入订单、用户、商品和库存的实时运营数据。</span>
      </div>
      <el-icon><DataAnalysis /></el-icon>
    </div>

    <div class="metric-grid">
      <article v-for="item in metrics" :key="item.label" class="metric-card">
        <div class="metric-icon" :class="item.tone"><el-icon><component :is="item.icon" /></el-icon></div>
        <div>
          <p>{{ item.label }}</p>
          <strong>{{ item.value }}</strong>
          <span>{{ item.hint }}</span>
        </div>
      </article>
    </div>

    <div class="roadmap-card">
      <div class="card-heading">
        <div>
          <p>开发进度</p>
          <h3>管理员端第一阶段</h3>
        </div>
        <el-tag type="warning" effect="light">进行中</el-tag>
      </div>
      <el-steps :active="1" finish-status="success" simple>
        <el-step title="后台布局" />
        <el-step title="用户管理" />
        <el-step title="商品管理" />
        <el-step title="订单与库存" />
      </el-steps>
    </div>
  </section>
</template>

<script setup>
import { useUserStore } from '../../../stores/user'
import { UserFilled, Goods, Tickets, Box } from '@element-plus/icons-vue'

const store = useUserStore()

const metrics = [
  { label: '用户管理', value: '--', hint: '待接入用户统计', icon: UserFilled, tone: 'blue' },
  { label: '上架商品', value: '--', hint: '待接入商品统计', icon: Goods, tone: 'orange' },
  { label: '今日订单', value: '--', hint: '待接入订单统计', icon: Tickets, tone: 'purple' },
  { label: '库存预警', value: '--', hint: '待接入库存预警', icon: Box, tone: 'green' },
]
</script>

<style scoped>
.dashboard { max-width: 1280px; margin: 0 auto; }

.welcome-card {
  min-height: 180px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  overflow: hidden;
  padding: 32px 42px;
  border-radius: 18px;
  color: #fff;
  background: radial-gradient(circle at 85% 15%, rgba(124, 154, 255, .5), transparent 28%), linear-gradient(120deg, #293b74, #465fd0);
  box-shadow: 0 14px 30px rgba(56, 79, 174, .2);
}

.welcome-card p { margin: 0 0 10px; color: rgba(255,255,255,.72); font-size: 14px; }
.welcome-card h2 { margin: 0 0 10px; font-size: 27px; font-weight: 600; }
.welcome-card span { color: rgba(255,255,255,.6); font-size: 13px; }
.welcome-card > .el-icon { margin-right: 40px; font-size: 90px; color: rgba(255,255,255,.16); }

.metric-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 18px; margin-top: 22px; }

.metric-card, .roadmap-card { background: #fff; border: 1px solid #e9edf4; border-radius: 14px; box-shadow: 0 3px 12px rgba(32, 50, 80, .025); }
.metric-card { display: flex; align-items: center; gap: 14px; padding: 22px; }
.metric-icon { width: 44px; height: 44px; display: grid; place-items: center; border-radius: 12px; font-size: 21px; }
.metric-icon.blue { background: #edf2ff; color: #5278ef; }
.metric-icon.orange { background: #fff3e8; color: #ed8a35; }
.metric-icon.purple { background: #f4efff; color: #8b6ae6; }
.metric-icon.green { background: #eaf8f1; color: #42ae7d; }
.metric-card p, .metric-card span { margin: 0; display: block; color: #9aa4b5; font-size: 12px; }
.metric-card strong { display: block; margin: 3px 0; color: #273246; font-size: 24px; }

.roadmap-card { margin-top: 22px; padding: 24px; }
.card-heading { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
.card-heading p { margin: 0 0 5px; color: #9aa4b5; font-size: 12px; }
.card-heading h3 { margin: 0; color: #273246; font-size: 17px; }

@media (max-width: 1000px) { .metric-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 620px) { .welcome-card { padding: 26px; } .welcome-card h2 { font-size: 22px; } .welcome-card > .el-icon { display: none; } .metric-grid { grid-template-columns: 1fr; } }
</style>
