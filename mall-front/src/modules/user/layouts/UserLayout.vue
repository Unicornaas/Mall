<template>
  <div class="layout">
    <header class="top-header">
      <div class="header-inner">
        <!-- 品牌标识 -->
        <div class="brand" @click="$router.push('/')">
          <span class="brand-icon">&#9670;</span>
          <span class="brand-text">MALL</span>
        </div>

        <!-- 主导航 -->
        <nav class="main-nav">
          <router-link to="/products" class="nav-link" :class="{ active: activeMenu === '/products' }">
            商品浏览
          </router-link>
          <router-link to="/cart" class="nav-link" :class="{ active: activeMenu === '/cart' }">
            购物车
          </router-link>
          <router-link to="/orders" class="nav-link" :class="{ active: activeMenu === '/orders' }">
            我的订单
          </router-link>
        </nav>

        <!-- 右侧操作区 -->
        <div class="header-actions">
          <!-- 购物车图标 -->
          <div class="cart-icon" @click="$router.push('/cart')">
            <el-icon :size="22"><ShoppingCart /></el-icon>
            <span v-if="cartCount" class="cart-dot">{{ cartCount > 99 ? '99+' : cartCount }}</span>
          </div>

          <!-- 用户下拉 -->
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-trigger">
              <el-avatar :size="34" icon="UserFilled" class="user-avatar" />
              <span class="user-name">{{ store.user?.nickname || store.user?.username }}</span>
              <el-icon :size="14" class="user-chevron"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="Number(store.user?.role) === 1" command="seller">
                  <el-icon><Shop /></el-icon>进入商家工作台
                </el-dropdown-item>
                <el-dropdown-item v-if="Number(store.user?.role) === 2" command="admin">
                  <el-icon><DataAnalysis /></el-icon>返回管理平台
                </el-dropdown-item>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="addresses">
                  <el-icon><MapLocation /></el-icon>收货地址
                </el-dropdown-item>
                <el-dropdown-item command="orders">
                  <el-icon><Document /></el-icon>我的订单
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 主内容 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, MapLocation, Document, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '../../../stores/user'
import { getCartCount } from '../api/cart'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const cartCount = ref(0)

const activeMenu = computed(() => {
  if (route.path.startsWith('/products')) return '/products'
  if (route.path.startsWith('/cart')) return '/cart'
  if (route.path.startsWith('/orders')) return '/orders'
  return ''
})

const fetchCartCount = async () => {
  if (!store.user) return
  try {
    const res = await getCartCount(store.user.id)
    cartCount.value = res.data
  } catch { /* ignore */ }
}

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    store.logout()
  } else if (cmd === 'seller') {
    router.push('/seller/dashboard')
  } else if (cmd === 'admin') {
    router.push('/admin/dashboard')
  } else {
    router.push(`/${cmd}`)
  }
}

onMounted(fetchCartCount)
watch(() => store.cartVersion, fetchCartCount)
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: #f5f5f0;
  display: flex;
  flex-direction: column;
}

/* ===== 顶部导航 ===== */
.top-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0,0,0,0.06);
  box-shadow: 0 1px 6px rgba(0,0,0,0.03);
}

.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 28px;
  gap: 40px;
}

/* 品牌标识 */
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
  flex-shrink: 0;
}

.brand-icon {
  font-size: 24px;
  color: #FF6B35;
  filter: drop-shadow(0 0 6px rgba(255,107,53,0.35));
}

.brand-text {
  font-size: 22px;
  font-weight: 300;
  letter-spacing: 6px;
  color: #1a1a2e;
}

/* 导航链接 */
.main-nav {
  display: flex;
  gap: 4px;
  flex: 1;
}

.nav-link {
  position: relative;
  padding: 8px 18px;
  font-size: 14px;
  color: #666;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.25s ease;
  font-weight: 400;
  letter-spacing: 0.5px;
}

.nav-link:hover {
  color: #1a1a2e;
  background: rgba(0,0,0,0.03);
}

.nav-link.active {
  color: #FF6B35;
  font-weight: 500;
  background: rgba(255,107,53,0.06);
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background: #FF6B35;
  border-radius: 1px;
}

/* 右侧操作 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-shrink: 0;
}

.cart-icon {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  cursor: pointer;
  color: #555;
  transition: all 0.2s ease;
}

.cart-icon:hover {
  background: rgba(0,0,0,0.04);
  color: #FF6B35;
}

.cart-dot {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  font-size: 10px;
  font-weight: 600;
  line-height: 18px;
  text-align: center;
  color: #fff;
  background: #FF6B35;
  border-radius: 9px;
  box-shadow: 0 2px 6px rgba(255,107,53,0.3);
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: rgba(0,0,0,0.02);
}

.user-trigger:hover {
  background: rgba(0,0,0,0.05);
}

.user-avatar {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8F5E 100%);
}

.user-name {
  font-size: 13px;
  color: #444;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-chevron {
  color: #aaa;
  transition: transform 0.2s;
}

.user-trigger:hover .user-chevron {
  color: #666;
}

/* ===== 主内容区 ===== */
.main-content {
  flex: 1;
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px 28px;
  width: 100%;
}

/* 响应式 */
@media (max-width: 768px) {
  .header-inner {
    padding: 0 16px;
    gap: 16px;
  }

  .brand-text {
    font-size: 18px;
    letter-spacing: 4px;
  }

  .main-nav {
    gap: 0;
  }

  .nav-link {
    padding: 8px 10px;
    font-size: 13px;
  }

  .user-name {
    display: none;
  }

  .main-content {
    padding: 16px;
  }
}
</style>
