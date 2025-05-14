<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Menu as IconMenu,
  Location as IconLocation,
  Setting as IconSetting,
  User as IconUser,
  Tickets as IconTickets,
  House as IconHouse,
  PieChart as IconPieChart,
  Place as IconPlace,
  Calendar as IconCalendar,
  ChatDotRound as IconChatDotRound,
  Bell as IconBell,
  SwitchButton as IconSwitchButton,
  Plus as IconPlus,
  List as IconList,
  Edit as IconEdit,
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 控制菜单折叠状态
const isCollapse = ref(false)

// 菜单项激活状态
const activeIndex = ref('dashboard')

// 监听路由变化，更新激活菜单
const handleRouteChange = () => {
  const path = route.path
  if (path.includes('/dashboard')) {
    activeIndex.value = 'dashboard'
  } else if (path.includes('/venues/create')) {
    activeIndex.value = 'venue-create'
  } else if (path.includes('/venues/edit')) {
    activeIndex.value = 'venues'
  } else if (path.includes('/venues')) {
    activeIndex.value = 'venues'
  } else if (path.includes('/venue-types')) {
    activeIndex.value = 'venue-types'
  } else if (path.includes('/bookings')) {
    activeIndex.value = 'bookings'
  } else if (path.includes('/booking-rules')) {
    activeIndex.value = 'booking-rules'
  } else if (path.includes('/reviews/stats')) {
    activeIndex.value = 'review-stats'
  } else if (path.includes('/reviews/reports')) {
    activeIndex.value = 'review-reports'
  } else if (path.includes('/reviews')) {
    activeIndex.value = 'reviews'
  } else if (path.includes('/users')) {
    activeIndex.value = 'users'
  } else if (path.includes('/admin')) {
    activeIndex.value = 'admin'
  } else {
    // 默认仪表盘
    activeIndex.value = 'dashboard'
  }
}

// 路由变化时更新激活菜单
handleRouteChange()

// 菜单项点击处理
const handleMenuClick = (index: string) => {
  activeIndex.value = index
  switch (index) {
    case 'dashboard':
      router.push('/home/dashboard')
      break
    case 'venues':
      router.push('/home/venues')
      break
    case 'venue-create':
      router.push('/home/venues/create')
      break
    case 'venue-types':
      router.push('/home/venue-types')
      break
    case 'bookings':
      router.push('/home/bookings')
      break
    case 'booking-rules':
      router.push('/home/booking-rules')
      break
    case 'reviews':
      router.push('/home/reviews')
      break
    case 'review-stats':
      router.push('/home/reviews/stats')
      break
    case 'review-reports':
      router.push('/home/reviews/reports')
      break
    case 'users':
      router.push('/home/users')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      userStore.logout()
      break
    default:
      // 默认仪表盘
      router.push('/home/dashboard')
  }
}

// 控制菜单折叠状态
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}
</script>

<template>
  <div class="common-layout">
    <!-- 侧边菜单 -->
    <el-container class="layout-container">
      <el-aside :width="isCollapse ? '64px' : '220px'" class="aside-menu">
        <div class="logo-container">
          <div class="logo" v-if="!isCollapse">场馆预约管理系统</div>
          <div class="logo" v-else>场馆</div>
        </div>
        <el-menu
          :default-active="activeIndex"
          class="menu"
          :collapse="isCollapse"
          background-color="#001529"
          text-color="#fff"
          active-text-color="#409EFF"
          :unique-opened="true"
          @select="handleMenuClick"
        >
          <el-menu-item index="dashboard">
            <el-icon><IconPieChart /></el-icon>
            <template #title>仪表盘</template>
          </el-menu-item>

          <el-sub-menu index="venue-manage">
            <template #title>
              <el-icon><IconHouse /></el-icon>
              <span>场馆管理</span>
            </template>
            <el-menu-item index="venues">
              <el-icon><IconList /></el-icon>
              <span>场馆列表</span>
            </el-menu-item>
            <el-menu-item index="venue-create">
              <el-icon><IconPlus /></el-icon>
              <span>新增场馆</span>
            </el-menu-item>
            <el-menu-item index="venue-types">
              <el-icon><IconEdit /></el-icon>
              <span>场馆类型</span>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="booking-manage">
            <template #title>
              <el-icon><IconCalendar /></el-icon>
              <span>预约管理</span>
            </template>
            <el-menu-item index="bookings">
              <el-icon><IconTickets /></el-icon>
              <span>预约列表</span>
            </el-menu-item>
            <el-menu-item index="booking-rules">
              <el-icon><IconSetting /></el-icon>
              <span>预约规则</span>
            </el-menu-item>
          </el-sub-menu>

          <el-menu-item index="users">
            <el-icon><IconUser /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>

          <el-sub-menu index="review-manage">
            <template #title>
            <el-icon><IconChatDotRound /></el-icon>
              <span>评价管理</span>
            </template>
            <el-menu-item index="reviews">
              <el-icon><IconList /></el-icon>
              <span>评价列表</span>
            </el-menu-item>
            <el-menu-item index="review-reports">
              <el-icon><IconBell /></el-icon>
              <span>评价举报</span>
            </el-menu-item>
            <el-menu-item index="review-stats">
              <el-icon><IconPieChart /></el-icon>
              <span>评价统计</span>
          </el-menu-item>
          </el-sub-menu>

          <el-menu-item index="notifications">
            <el-icon><IconBell /></el-icon>
            <template #title>通知管理</template>
          </el-menu-item>

          <el-menu-item index="settings">
            <el-icon><IconSetting /></el-icon>
            <template #title>系统设置</template>
          </el-menu-item>

          <el-menu-item index="logout" class="logout-item">
            <el-icon><IconSwitchButton /></el-icon>
            <template #title>退出登录</template>
          </el-menu-item>
        </el-menu>

        <div class="collapse-btn" @click="toggleCollapse">
          <el-icon>
            <IconMenu />
          </el-icon>
        </div>
      </el-aside>

      <el-container>
        <!-- 顶部导航 -->
        <el-header height="64px" class="header">
          <div class="header-left">
            <div class="breadcrumb">
              <el-breadcrumb separator="/">
                <el-breadcrumb-item :to="{ path: '/home/dashboard' }">首页</el-breadcrumb-item>
                <el-breadcrumb-item v-if="activeIndex === 'dashboard'">仪表盘</el-breadcrumb-item>
                <el-breadcrumb-item v-else-if="activeIndex === 'venues'">场馆列表</el-breadcrumb-item>
                <el-breadcrumb-item v-else-if="activeIndex === 'venue-create'">新增场馆</el-breadcrumb-item>
                <el-breadcrumb-item v-else-if="activeIndex === 'venue-types'">场馆类型管理</el-breadcrumb-item>
                <el-breadcrumb-item v-else-if="activeIndex === 'bookings'">预约列表</el-breadcrumb-item>
                <el-breadcrumb-item v-else-if="activeIndex === 'users'">用户管理</el-breadcrumb-item>
              </el-breadcrumb>
            </div>
          </div>

          <div class="header-right">
            <el-tooltip content="消息通知" placement="bottom">
              <el-badge :value="5" class="notification-badge">
                <el-button size="small" circle>
                  <el-icon><IconBell /></el-icon>
                </el-button>
              </el-badge>
            </el-tooltip>

            <el-dropdown trigger="click" class="user-dropdown">
              <div class="user-info">
                <el-avatar
                  :size="32"
                  src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"
                />
                <span class="username">{{
                  userStore.userInfo?.nickname || userStore.userInfo?.username
                }}</span>
                <el-icon><el-icon-arrow-down /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>
                    <el-icon><IconUser /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item>
                    <el-icon><IconSetting /></el-icon>系统设置
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="userStore.logout">
                    <el-icon><IconSwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主要内容区域 -->
        <el-main class="main-content">
          <router-view />
        </el-main>

        <!-- 页脚 -->
        <el-footer height="40px" class="footer">
          <div class="footer-content">
            <span
              >&copy; {{ new Date().getFullYear() }} 场馆预约管理系统 | 技术支持: 场馆开发团队</span
            >
          </div>
        </el-footer>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
.common-layout {
  height: 100vh;
  display: flex;
}

.layout-container {
  height: 100%;
  width: 100%;
}

.aside-menu {
  background-color: #001529;
  height: 100%;
  transition: width 0.3s;
  position: relative;
  overflow: hidden;
}

.logo-container {
  height: 64px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  border-bottom: 1px solid #001f3f;
  white-space: nowrap;
  overflow: hidden;
}

.menu {
  border-right: none;
  height: calc(100% - 104px);
  overflow-y: auto;
}

.collapse-btn {
  position: absolute;
  bottom: 0;
  width: 100%;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background-color: #002140;
  color: #fff;
  border-top: 1px solid #001f3f;
}

.header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  border-bottom: 1px solid #eee;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
}

.breadcrumb {
  font-size: 14px;
}

.notification-badge {
  margin-right: 20px;
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
}

.username {
  margin: 0 10px;
  color: #333;
}

.main-content {
  background-color: #f5f7fa;
  height: calc(100vh - 104px);
  overflow-y: auto;
  padding: 20px;
}

.footer {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid #eee;
  font-size: 12px;
  color: #606266;
}

/* 自定义菜单样式 */
:deep(.el-menu--collapse) {
  width: 64px;
}

:deep(.el-menu--collapse .el-menu-item) {
  text-align: center;
}

.logout-item {
  position: absolute;
  bottom: 50px;
  width: 100%;
}

:deep(.el-menu--collapse .logout-item) {
  position: static;
}
</style>
