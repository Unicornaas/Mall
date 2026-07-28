<template>
  <div class="seller-shell">
    <aside class="seller-sidebar">
      <div class="seller-brand" @click="router.push('/seller/dashboard')">
        <span class="brand-mark">◆</span>
        <div><strong>MALL</strong><small>商家工作台</small></div>
      </div>
      <el-menu :default-active="route.path" class="seller-menu" background-color="transparent" text-color="#9ba4b5" active-text-color="#ffffff" router>
        <el-menu-item index="/seller/dashboard"><el-icon><DataAnalysis /></el-icon><span>经营概览</span></el-menu-item>
        <el-menu-item disabled><el-icon><Goods /></el-icon><span>我的商品</span></el-menu-item>
        <el-menu-item disabled><el-icon><Box /></el-icon><span>库存管理</span></el-menu-item>
        <el-menu-item disabled><el-icon><Tickets /></el-icon><span>店铺订单</span></el-menu-item>
      </el-menu>
      <div class="sidebar-note"><span class="status-dot"></span>商家服务运行中</div>
    </aside>
    <section class="seller-main">
      <header class="seller-header"><div><p class="page-kicker">MALL SELLER</p><h1>{{ pageTitle }}</h1></div><el-dropdown trigger="click" @command="handleCommand"><div class="seller-user"><el-avatar :size="36" icon="UserFilled" /><div><strong>{{ store.user?.nickname || store.user?.username || '商家' }}</strong><small>商家账号</small></div><el-icon><ArrowDown /></el-icon></div><template #dropdown><el-dropdown-menu><el-dropdown-item command="mall"><el-icon><Shop /></el-icon>进入商城</el-dropdown-item><el-dropdown-item command="logout" divided><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item></el-dropdown-menu></template></el-dropdown></header>
      <main class="seller-content"><router-view /></main>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../../stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const pageTitle = computed(() => route.meta.title || '商家工作台')
const handleCommand = (command) => command === 'logout' ? store.logout() : router.push('/products')
</script>

<style scoped>
.seller-shell { min-height: 100vh; display: flex; background: #f4f6fb; color: #1d2433; }.seller-sidebar { width: 240px; min-height: 100vh; display: flex; flex-direction: column; padding: 26px 14px 20px; background: linear-gradient(180deg, #17362f, #1f4c42); box-shadow: 8px 0 30px rgba(22,35,62,.08); }.seller-brand { display: flex; align-items: center; gap: 11px; padding: 0 14px 28px; cursor: pointer; color: #fff; }.brand-mark { color: #57d2a0; font-size: 22px; }.seller-brand strong,.seller-brand small,.seller-user strong,.seller-user small { display: block; }.seller-brand strong { letter-spacing: 4px; font-size: 19px; font-weight: 500; }.seller-brand small { margin-top: 3px; color: #9ebbb3; font-size: 11px; letter-spacing: 1px; }.seller-menu { flex: 1; border: none; }.seller-menu :deep(.el-menu-item) { height: 48px; line-height: 48px; margin: 5px 0; border-radius: 10px; font-size: 14px; }.seller-menu :deep(.el-menu-item.is-active) { background: linear-gradient(90deg, rgba(46,167,123,.95), rgba(91,195,151,.82)); box-shadow: 0 6px 15px rgba(28,135,97,.2); }.seller-menu :deep(.el-menu-item.is-disabled) { opacity: .48; }.sidebar-note { display: flex; align-items: center; gap: 8px; padding: 14px; color: #9ebbb3; font-size: 12px; border-top: 1px solid rgba(255,255,255,.08); }.status-dot { width: 7px; height: 7px; border-radius: 50%; background: #57d2a0; box-shadow: 0 0 0 4px rgba(87,210,160,.12); }.seller-main { flex: 1; min-width: 0; }.seller-header { height: 82px; display: flex; align-items: center; justify-content: space-between; padding: 0 34px; background: rgba(255,255,255,.92); border-bottom: 1px solid #e7eaf0; }.page-kicker { margin: 0 0 4px; color: #a2aabb; font-size: 11px; font-weight: 700; letter-spacing: 1.2px; }.seller-header h1 { margin: 0; color: #20293b; font-size: 22px; line-height: 1.2; }.seller-user { display: flex; align-items: center; gap: 9px; padding: 5px 9px 5px 5px; border-radius: 10px; cursor: pointer; }.seller-user:hover { background: #f6f8fc; }.seller-user :deep(.el-avatar) { background: linear-gradient(135deg, #278d69, #5cc39c); }.seller-user strong { color: #30394a; font-size: 13px; }.seller-user small { margin-top: 3px; color: #a0a8b8; font-size: 11px; }.seller-content { padding: 28px 34px 40px; }@media (max-width:860px) { .seller-sidebar { width:72px; padding-inline:8px; }.seller-brand { justify-content:center; padding-inline:0; }.seller-brand>div,.seller-menu :deep(.el-menu-item span),.sidebar-note { display:none; }.seller-menu :deep(.el-menu-item) { justify-content:center; padding:0!important; }.seller-menu :deep(.el-menu-item .el-icon) { margin:0; }.seller-header,.seller-content { padding-inline:20px; } }
</style>
