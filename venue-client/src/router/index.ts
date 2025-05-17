import { createRouter, createWebHistory } from 'vue-router'

// 路由守卫：检查用户是否已登录
const checkAuth = (to: any, from: any, next: any) => {
  const token = localStorage.getItem('userToken')
  if (to.meta.requiresAuth && !token) {
    // 如果需要登录但没有token，重定向到登录页并记住原目标路径
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  } else {
    next()
  }
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/Home.vue'),
    },
    {
      path: '/welcome',
      name: 'welcome',
      component: () => import('../views/Welcome.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/Register.vue'),
    },
    {
      path: '/user',
      name: 'userHome',
      component: () => import('../views/Home.vue'),
      meta: {
        requiresAuth: true,
      },
    },
    // 场馆相关路由
    {
      path: '/venues',
      name: 'venues',
      component: () => import('../views/VenueList.vue'),
    },
    {
      path: '/venue/:id',
      name: 'venueDetail',
      component: () => import('../views/VenueDetail.vue'),
      props: true,
    },
    // 预约相关路由
    {
      path: '/booking/venue/:id',
      name: 'venueBooking',
      component: () => import('../views/VenueBooking.vue'),
      meta: {
        requiresAuth: true,
      },
      props: true,
    },
    {
      path: '/booking/success',
      name: 'bookingSuccess',
      component: () => import('../views/BookingSuccess.vue'),
      meta: {
        requiresAuth: true,
      },
    },
    // 支付相关路由
    {
      path: '/payment/success',
      name: 'paymentSuccess',
      component: () => import('../views/PaymentSuccess.vue'),
    },
    {
      path: '/payment/test',
      name: 'paymentTest',
      component: () => import('../views/PayTest.vue'),
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/payment/callback',
      name: 'paymentCallback',
      component: () => import('../views/PaymentCallback.vue'),
      meta: {
        requiresAuth: true,
      },
    },
    // 用户反馈相关路由
    {
      path: '/feedback',
      name: 'feedback',
      component: () => import('../views/user/Feedback.vue'),
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/feedback-detail/:id',
      name: 'feedbackDetail',
      component: () => import('../views/user/FeedbackDetail.vue'),
      meta: {
        requiresAuth: true,
      },
      props: true,
    },
    // 用户消息相关路由
    {
      path: '/message',
      name: 'message',
      component: () => import('../views/user/Message.vue'),
      meta: {
        requiresAuth: true,
      },
    },
    // 订单相关路由
    {
      path: '/orders',
      name: 'orders',
      component: () => import('../views/OrderList.vue'), // 更新为实际的订单列表页面
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/order/:id',
      name: 'orderDetail',
      component: () => import('../views/OrderDetail.vue'), // 订单详情页面
      meta: {
        requiresAuth: true,
      },
      props: true,
    },
    // 评价相关路由
    {
      path: '/my-reviews',
      name: 'myReviews',
      component: () => import('../views/MyReviews.vue'),
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/my-reports',
      name: 'myReports',
      component: () => import('../views/MyReports.vue'),
      meta: {
        requiresAuth: true,
      },
    },
    // 个人中心路由
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/Profile.vue'), // 使用个人中心页面
      meta: {
        requiresAuth: true,
      },
    },
    // 在现有路由中添加我的收藏页面路由
    {
      path: '/my-favorites',
      name: 'MyFavorites',
      component: () => import('@/views/MyFavorites.vue'),
      meta: {
        requiresAuth: true,
        title: '我的收藏'
      }
    },
  ],
})

// 全局前置守卫
router.beforeEach(checkAuth)

export default router
