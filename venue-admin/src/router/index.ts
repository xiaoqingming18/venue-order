import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/home',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
    },
    {
      path: '/home',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
      redirect: '/home/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('../views/DashboardView.vue'),
        },
        {
          path: 'venues',
          name: 'venues',
          component: () => import('../views/venue/VenueListView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'venues/create',
          name: 'venueCreate',
          component: () => import('../views/venue/VenueEditView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'venues/edit/:id',
          name: 'venueEdit',
          component: () => import('../views/venue/VenueEditView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'venues/:id/detail',
          name: 'venueDetail',
          component: () => import('../views/venue/VenueDetailView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'venue-types',
          name: 'venueTypes',
          component: () => import('../views/venue/VenueTypeListView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'facilities',
          name: 'facilities',
          component: () => import('../views/venue/FacilityListView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'locations',
          name: 'locations',
          component: () => import('../views/venue/LocationListView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'bookings',
          name: 'bookings',
          component: () => import('../views/booking/BookingListView.vue'),
        },
        {
          path: 'bookings/:id',
          name: 'bookingDetail',
          component: () => import('../views/booking/BookingDetailView.vue'),
        },
        {
          path: 'booking-rules',
          name: 'bookingRules',
          component: () => import('../views/booking/BookingRuleView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'users',
          name: 'users',
          component: () => import('../views/user/UserListView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'users/detail/:id',
          name: 'userDetail',
          component: () => import('../views/user/UserDetailView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'reviews',
          name: 'reviews',
          component: () => import('../views/review/ReviewListView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'reviews/stats',
          name: 'reviewStats',
          component: () => import('../views/review/ReviewStatsView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'reviews/reports',
          name: 'reviewReports',
          component: () => import('../views/review/ReviewReportView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'reviews/:id',
          name: 'reviewDetail',
          component: () => import('../views/review/ReviewDetailView.vue'),
          meta: { requiresAdmin: true },
        },
      ],
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../views/AdminView.vue'),
      meta: { requiresAdmin: true },
    },
    // 兼容旧路由
    {
      path: '/dashboard',
      redirect: '/home/dashboard',
    },
  ],
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  // 如果访问的是登录或注册页面，直接放行
  if (to.path === '/login' || to.path === '/register') {
    next()
    return
  }

  // 如果未登录，重定向到登录页
  if (!token) {
    next('/login')
    return
  }

  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin) {
    const userStore = useUserStore()

    // 初始化用户信息
    if (!userStore.userInfo) {
      userStore.fetchUserInfo().then(() => {
        if (!userStore.isAdmin) {
          ElMessage.error('权限不足，需要管理员权限')
          next('/home/dashboard')
        } else {
          next()
        }
      })
    } else if (!userStore.isAdmin) {
      ElMessage.error('权限不足，需要管理员权限')
      next('/home/dashboard')
    } else {
      next()
    }
    return
  }

  // 已登录状态下，放行
  next()
})

export default router
