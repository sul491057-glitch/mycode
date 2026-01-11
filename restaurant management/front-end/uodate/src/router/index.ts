import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
  },
  {
    path: '/customer',
    component: () => import('@/layout/LayoutCustomer.vue'),
    redirect: '/customer/home',
    meta: { requiresAuth: true, role: 'customer' }, // 添加元数据：需要登录，角色为顾客
    children: [
      {
        path: 'home',
        name: 'CustomerHome',
        component: () => import('@/views/customer/Home.vue'),
      },
      {
        path: 'menu',
        name: 'CustomerMenu',
        component: () => import('@/views/customer/Menu.vue'),
      },
      // ... 其他代码不变
      {
        path: 'reservation',
        name: 'CustomerReservation',
        // 🔴 关键修改：指向新的文件名 ReservationPage.vue
        component: () => import('@/views/customer/ReservationPage.vue'),
      },
// ... 其他代码不变
    ],
  },
  {
    path: '/admin',
    component: () => import('@/layout/LayoutAdmin.vue'),
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true, role: 'admin' }, // 添加元数据：需要登录，角色为管理员
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Home.vue'),
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/OrderList.vue'),
      },
      {
        path: 'reservations',
        name: 'AdminReservations',
        component: () => import('@/views/admin/ReservationList.vue'),
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('@/views/admin/ProductList.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  
  // 白名单页面
  const whiteList = ['/login', '/register']

  // 1. 如果去往不需要验证的页面（如登录页），直接放行
  if (whiteList.includes(to.path)) {
    next()
    return
  }
  
  // 2. 检查是否有 token (如果没有 token，强制跳回登录页)
  if (!token) {
    next('/login')
    return
  }

  // 3. 检查是否需要登录 (保留原有逻辑作为双重保障)
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 如果没有角色信息（说明未登录），跳转回登录页
    if (!role) {
      next('/login')
      return
    }

    // 3. 检查权限 (Admin 只能进 Admin，Customer 只能进 Customer)
    // 获取路由要求的角色
    const requiredRole = to.matched.find(record => record.meta.role)?.meta.role

    if (requiredRole) {
      if (requiredRole === 'admin' && role !== 'admin') {
        // 如果需要管理员权限但当前不是管理员 -> 踢回登录页或提示
        next('/login') 
        return
      }
      // 顾客通常权限较低，如果管理员想访问顾客页面，通常是允许的？
      // 这里为了严格区分，假设管理员也需要专门的入口，或者简化为：
      // 顾客不能访问管理员页面，管理员可以访问所有？
      // 这里的逻辑是：如果路由明确要求 'customer'，而当前是 'admin'，是否允许？
      // 简单起见，严格匹配：
      if (requiredRole === 'customer' && role !== 'customer' && role !== 'admin') {
         next('/login')
         return
      }
    }
  }

  next()
})

export default router