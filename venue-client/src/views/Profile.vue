<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 用户信息
const userInfo = ref(userStore.userInfo)

// 菜单项
const menuItems = [
  {
    id: 'orders',
    title: '我的订单',
    icon: 'calendar-alt',
    color: '#409EFF',
    route: '/orders'
  },
  {
    id: 'reviews',
    title: '我的评价',
    icon: 'comment',
    color: '#F56C6C',
    route: '/my-reviews'
  },
  {
    id: 'reports',
    title: '我的举报',
    icon: 'flag',
    color: '#E6A23C',
    route: '/my-reports'
  },
  {
    id: 'favorites',
    title: '收藏场馆',
    icon: 'heart',
    color: '#67C23A',
    route: '/my-favorites'
  },
  {
    id: 'feedback',
    title: '问题反馈',
    icon: 'comment-dots',
    color: '#E6A23C',
    route: '/feedback'
  },
  {
    id: 'message',
    title: '我的消息',
    icon: 'bell',
    color: '#F56C6C',
    route: '/message'
  },
  {
    id: 'settings',
    title: '账号设置',
    icon: 'cog',
    color: '#909399',
    route: '/profile'
  }
]

// 导航到指定路由
const navigateTo = (route: string) => {
  router.push(route)
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '退出登录',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    userStore.logout()
    router.push('/login')
    ElMessage.success('已成功退出登录')
  } catch (error) {
    // 用户取消操作，不做任何处理
  }
}

onMounted(async () => {
  // 如果未登录，重定向到登录页
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }

  // 确保已加载用户信息
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
      userInfo.value = userStore.userInfo
    } catch (error) {
      console.error('获取用户信息失败', error)
      router.push('/login')
    }
  }
})
</script>

<template>
  <div class="profile-container">
    <!-- 用户信息卡片 -->
    <div class="user-card">
      <div class="user-avatar">
        <img :src="userInfo?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="用户头像">
      </div>
      <div class="user-info">
        <div class="user-name">{{ userInfo?.nickname || userInfo?.username || '用户' }}</div>
        <div class="user-id">ID: {{ userInfo?.id || 'Unknown' }}</div>
      </div>
      <div class="edit-profile" @click="navigateTo('/profile')">
        <i class="fas fa-pen"></i>
      </div>
    </div>
    
    <!-- 菜单列表 -->
    <div class="menu-list">
      <div 
        v-for="item in menuItems" 
        :key="item.id"
        class="menu-item"
        @click="navigateTo(item.route)"
      >
        <div class="menu-icon" :style="{ backgroundColor: `${item.color}15` }">
          <i class="fas" :class="`fa-${item.icon}`" :style="{ color: item.color }"></i>
        </div>
        <div class="menu-title">{{ item.title }}</div>
        <i class="fas fa-chevron-right"></i>
      </div>
    </div>
    
    <!-- 其他菜单项 -->
    <div class="menu-list">
      <div class="menu-item" @click="navigateTo('/profile')">
        <div class="menu-icon" style="background-color: #67C23A15">
          <i class="fas fa-headset" style="color: #67C23A"></i>
        </div>
        <div class="menu-title">联系客服</div>
        <i class="fas fa-chevron-right"></i>
      </div>
      
      <div class="menu-item" @click="navigateTo('/profile')">
        <div class="menu-icon" style="background-color: #90939915">
          <i class="fas fa-info-circle" style="color: #909399"></i>
        </div>
        <div class="menu-title">关于我们</div>
        <i class="fas fa-chevron-right"></i>
      </div>
    </div>
    
    <!-- 退出登录按钮 -->
    <div class="logout-button" @click="handleLogout">
      退出登录
    </div>

    <!-- iOS底部标签栏 -->
    <div class="ios-tab-bar">
      <a class="tab-item" @click="navigateTo('/')">
        <i class="fas fa-home"></i>
        <span>首页</span>
      </a>
      <a class="tab-item" @click="navigateTo('/orders')">
        <i class="fas fa-calendar-alt"></i>
        <span>订单</span>
      </a>
      <a class="tab-item active" @click="navigateTo('/profile')">
        <i class="fas fa-user"></i>
        <span>我的</span>
      </a>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding-bottom: 70px;
}

.user-card {
  background-color: #fff;
  padding: 20px;
  display: flex;
  align-items: center;
  position: relative;
}

.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 15px;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.user-id {
  font-size: 14px;
  color: #909399;
}

.edit-profile {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: #f2f6fc;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  cursor: pointer;
}

.menu-list {
  background-color: #fff;
  margin-top: 15px;
  border-radius: 8px;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f2f6fc;
  cursor: pointer;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.menu-icon i {
  font-size: 18px;
}

.menu-title {
  flex: 1;
  font-size: 16px;
  color: #303133;
}

.menu-item i.fa-chevron-right {
  color: #c0c4cc;
  font-size: 14px;
}

.logout-button {
  background-color: #fff;
  margin: 20px 15px;
  padding: 15px;
  border-radius: 8px;
  text-align: center;
  color: #f56c6c;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
}

/* iOS底部标签栏样式 */
.ios-tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  background-color: white;
  box-shadow: 0 -1px 5px rgba(0, 0, 0, 0.1);
  padding: 10px 0;
  z-index: 100;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: #8e8e93;
  font-size: 10px;
  padding: 5px 0;
  cursor: pointer;
}

.tab-item i {
  font-size: 20px;
  margin-bottom: 3px;
}

.tab-item.active {
  color: #409eff;
}
</style> 