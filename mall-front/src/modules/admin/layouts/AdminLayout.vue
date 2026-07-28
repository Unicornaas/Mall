<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <div class="admin-brand" @click="router.push('/admin/dashboard')">
        <span class="brand-mark">◆</span>
        <div>
          <strong>MALL</strong>
          <small>管理平台</small>
        </div>
      </div>

      <el-menu
        :default-active="route.path"
        class="admin-menu"
        background-color="transparent"
        text-color="#9ba4b5"
        active-text-color="#ffffff"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>运营概览</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><UserFilled /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><Tickets /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/inventory">
          <el-icon><Box /></el-icon>
          <span>库存管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/payments">
          <el-icon><CreditCard /></el-icon>
          <span>支付与退款</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-note">
        <span class="status-dot"></span>
        平台服务运行中
      </div>
    </aside>

    <section class="admin-main">
      <header class="admin-header">
        <div>
          <p class="page-kicker">MALL ADMIN</p>
          <h1>{{ pageTitle }}</h1>
        </div>
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="admin-user">
            <el-avatar :size="36" icon="UserFilled" />
            <div>
              <strong>{{ store.user?.nickname || store.user?.username || '管理员' }}</strong>
              <small>平台管理员</small>
            </div>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="mall">
                <el-icon><Shop /></el-icon>进入商城
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <main class="admin-content">
        <router-view />
      </main>
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

const pageTitle = computed(() => route.meta.title || '管理平台')

const handleCommand = (command) => {
  if (command === 'logout') {
    store.logout()
  } else {
    router.push('/products')
  }
}
</script>

<style scoped>
.admin-shell {
  min-height: 100vh;
  display: flex;
  background: #f4f6fb;
  color: #1d2433;
}

.admin-sidebar {
  width: 240px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #141b2d 0%, #1c2740 100%);
  padding: 26px 14px 20px;
  box-shadow: 8px 0 30px rgba(22, 35, 62, 0.08);
}

.admin-brand {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 0 14px 28px;
  cursor: pointer;
  color: #fff;
}

.brand-mark {
  color: #ff7a45;
  font-size: 25px;
  text-shadow: 0 0 16px rgba(255, 122, 69, 0.5);
}

.admin-brand strong,
.admin-brand small {
  display: block;
}

.admin-brand strong {
  letter-spacing: 4px;
  font-size: 19px;
  font-weight: 500;
}

.admin-brand small {
  margin-top: 3px;
  color: #8e99ad;
  font-size: 11px;
  letter-spacing: 1px;
}

.admin-menu {
  flex: 1;
  border: none;
}

.admin-menu :deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 5px 0;
  border-radius: 10px;
  font-size: 14px;
}

.admin-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(255, 122, 69, 0.95), rgba(255, 143, 94, 0.8));
  box-shadow: 0 6px 15px rgba(255, 104, 60, 0.2);
}

.admin-menu :deep(.el-menu-item.is-disabled) {
  opacity: 0.48;
}

.sidebar-note {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px;
  color: #8290a8;
  font-size: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #37d39b;
  box-shadow: 0 0 0 4px rgba(55, 211, 155, 0.12);
}

.admin-main {
  flex: 1;
  min-width: 0;
}

.admin-header {
  height: 82px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 34px;
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid #e7eaf0;
}

.page-kicker {
  margin: 0 0 4px;
  color: #a2aabb;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1.2px;
}

.admin-header h1 {
  margin: 0;
  color: #20293b;
  font-size: 22px;
  line-height: 1.2;
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 5px 9px 5px 5px;
  border-radius: 10px;
  cursor: pointer;
}

.admin-user:hover {
  background: #f6f8fc;
}

.admin-user :deep(.el-avatar) {
  background: linear-gradient(135deg, #4e77f7, #7a9cff);
}

.admin-user strong,
.admin-user small {
  display: block;
}

.admin-user strong {
  color: #30394a;
  font-size: 13px;
}

.admin-user small {
  margin-top: 3px;
  color: #a0a8b8;
  font-size: 11px;
}

.admin-user > .el-icon {
  color: #a0a8b8;
  font-size: 13px;
}

.admin-content {
  padding: 28px 34px 40px;
}

@media (max-width: 860px) {
  .admin-sidebar { width: 72px; padding-inline: 8px; }
  .admin-brand { justify-content: center; padding-inline: 0; }
  .admin-brand > div, .admin-menu :deep(.el-menu-item span), .sidebar-note { display: none; }
  .admin-menu :deep(.el-menu-item) { justify-content: center; padding: 0 !important; }
  .admin-menu :deep(.el-menu-item .el-icon) { margin: 0; }
  .admin-header, .admin-content { padding-inline: 20px; }
}
</style>
