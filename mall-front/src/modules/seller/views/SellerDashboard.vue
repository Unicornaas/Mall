<template>
  <section class="seller-dashboard">
    <div class="welcome-card"><div><p>欢迎回来，{{ store.user?.nickname || store.user?.username || '商家' }}</p><h2>商家工作台已准备就绪</h2><span>商品、库存和店铺订单能力将在下一阶段接入，仅可管理当前商家所属数据。</span></div><el-icon><Shop /></el-icon></div>
    <div class="notice-card"><el-icon><InfoFilled /></el-icon><div><strong>商家端建设中</strong><p>平台管理员功能与商家自营功能将严格隔离；当前商家账号已经具备独立入口和访问权限保护。</p></div></div>
  </section>
</template>

<script setup>
import { InfoFilled, Shop } from '@element-plus/icons-vue'
import { useUserStore } from '../../../stores/user'

const store = useUserStore()
</script>

<style scoped>
.seller-dashboard { max-width: 1280px; margin: 0 auto; }.welcome-card { min-height: 180px; display: flex; align-items: center; justify-content: space-between; padding: 32px 42px; overflow: hidden; border-radius: 18px; color: #fff; background: radial-gradient(circle at 85% 15%, rgba(119,221,179,.42), transparent 28%),linear-gradient(120deg,#1f5b49,#278d69); box-shadow: 0 14px 30px rgba(30,120,90,.18); }.welcome-card p { margin:0 0 10px; color:rgba(255,255,255,.72); font-size:14px; }.welcome-card h2 { margin:0 0 10px; font-size:27px; font-weight:600; }.welcome-card span { color:rgba(255,255,255,.64); font-size:13px; }.welcome-card>.el-icon { margin-right:40px; font-size:90px; color:rgba(255,255,255,.16); }.notice-card { display:flex; align-items:flex-start; gap:12px; margin-top:22px; padding:20px 22px; color:#536076; background:#fff; border:1px solid #e9edf4; border-radius:14px; }.notice-card>.el-icon { margin-top:2px; color:#278d69; font-size:20px; }.notice-card strong { color:#273246; }.notice-card p { margin:7px 0 0; font-size:13px; line-height:1.7; color:#8791a3; }@media (max-width:620px) { .welcome-card { padding:26px; }.welcome-card h2 { font-size:22px; }.welcome-card>.el-icon { display:none; } }
</style>
