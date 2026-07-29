import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../modules/user/views/Login.vue'),
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../modules/user/views/Register.vue'),
  },
  {
    path: '/admin',
    component: () => import('../modules/admin/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard',
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../modules/admin/views/AdminDashboard.vue'),
        meta: { title: '运营概览' },
      },
      {
        path: 'users',
        name: 'AdminUserList',
        component: () => import('../modules/admin/views/UserList.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'products',
        name: 'AdminProductManagement',
        component: () => import('../modules/admin/views/ProductManagement.vue'),
        meta: { title: '商品管理' },
      },
      {
        path: 'orders',
        name: 'AdminOrderManagement',
        component: () => import('../modules/admin/views/OrderManagement.vue'),
        meta: { title: '订单管理' },
      },
      {
        path: 'inventory',
        name: 'AdminInventoryManagement',
        component: () => import('../modules/admin/views/InventoryManagement.vue'),
        meta: { title: '库存管理' },
      },
      {
        path: 'payments',
        name: 'AdminPaymentRefundManagement',
        component: () => import('../modules/admin/views/PaymentRefundManagement.vue'),
        meta: { title: '支付与退款' },
      },
    ],
  },
  {
    path: '/seller',
    component: () => import('../modules/seller/layouts/SellerLayout.vue'),
    meta: { requiresAuth: true, requiresSeller: true },
    children: [
      { path: '', redirect: '/seller/dashboard' },
      { path: 'dashboard', name: 'SellerDashboard', component: () => import('../modules/seller/views/SellerDashboard.vue'), meta: { title: '经营概览' } },
      { path: 'products', name: 'SellerProductManagement', component: () => import('../modules/seller/views/SellerProductManagement.vue'), meta: { title: '我的商品' } },
      { path: 'inventory', name: 'SellerInventoryManagement', component: () => import('../modules/seller/views/SellerInventoryManagement.vue'), meta: { title: '库存管理' } },
      { path: 'orders', name: 'SellerOrderManagement', component: () => import('../modules/seller/views/SellerOrderManagement.vue'), meta: { title: '店铺订单' } },
    ],
  },
  {
    path: '/',
    component: () => import('../modules/user/layouts/UserLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/products',
      },
      {
        path: 'products',
        name: 'ProductList',
        component: () => import('../modules/user/views/ProductList.vue'),
      },
      {
        path: 'products/:id',
        name: 'ProductDetail',
        component: () => import('../modules/user/views/ProductDetail.vue'),
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('../modules/user/views/Cart.vue'),
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('../modules/user/views/Checkout.vue'),
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('../modules/user/views/OrderList.vue'),
      },
      {
        path: 'orders/:id',
        name: 'OrderDetail',
        component: () => import('../modules/user/views/OrderDetail.vue'),
      },
      {
        path: 'payment/:id',
        name: 'Payment',
        component: () => import('../modules/user/views/Payment.vue'),
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('../modules/user/views/UserProfile.vue'),
      },
      {
        path: 'addresses',
        name: 'AddressList',
        component: () => import('../modules/user/views/AddressList.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  let user = null
  try {
    user = JSON.parse(localStorage.getItem('user') || 'null')
  } catch {
    localStorage.removeItem('user')
  }

  if (to.matched.some(r => r.meta.requiresAuth) && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.matched.some(r => r.meta.requiresAdmin) && Number(user?.role) !== 2) {
    next('/products')
  } else if (to.matched.some(r => r.meta.requiresSeller) && Number(user?.role) !== 1) {
    next('/products')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    next(Number(user?.role) === 2 ? '/admin/dashboard' : Number(user?.role) === 1 ? '/seller/dashboard' : '/')
  } else {
    next()
  }
})

export default router
