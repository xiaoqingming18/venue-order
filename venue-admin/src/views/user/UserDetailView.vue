<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, User, Edit } from '@element-plus/icons-vue'
import { getUserDetail } from '@/api/user'
import type { UserInfo } from '@/types/user'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

// 获取路由参数中的用户ID
const userId = ref<number>(parseInt(route.params.id as string))
// 用户详情数据
const userDetail = ref<UserInfo | null>(null)
// 加载状态
const loading = ref(false)

// 获取用户详情
const fetchUserDetail = async () => {
  if (!userId.value) {
    ElMessage.error('用户ID不存在')
    return
  }

  loading.value = true
  try {
    const res = await getUserDetail(userId.value)
    userDetail.value = res.data
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  } finally {
    loading.value = false
  }
}

// 返回上一页
const handleBack = () => {
  router.back()
}

// 编辑用户
const handleEdit = () => {
  router.push(`/home/users/edit/${userId.value}`)
}

// 格式化日期
const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 格式化用户状态
const formatStatus = (status: number) => {
  switch (status) {
    case 0:
      return '禁用'
    case 1:
      return '正常'
    default:
      return '未知'
  }
}

// 格式化用户角色
const formatRole = (role: number) => {
  switch (role) {
    case 1:
      return '普通用户'
    case 2:
      return '管理员'
    default:
      return '未知'
  }
}

// 格式化性别
const formatGender = (gender: string | null) => {
  if (!gender) return '-'
  switch (gender) {
    case 'male':
      return '男'
    case 'female':
      return '女'
    case 'other':
      return '保密'
    case 'M':
      return '男'
    case 'F':
      return '女'
    case 'O':
      return '保密'
    default:
      return gender
  }
}

// 组件挂载时获取用户详情
onMounted(() => {
  fetchUserDetail()
})
</script>

<template>
  <div class="user-detail-container" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button link :icon="ArrowLeft" @click="handleBack">返回</el-button>
            <span class="header-title">用户详情</span>
          </div>
          <div class="header-right">
            <el-button type="primary" :icon="Edit" @click="handleEdit">编辑</el-button>
          </div>
        </div>
      </template>

      <div v-if="userDetail" class="detail-content">
        <!-- 用户基本信息 -->
        <el-descriptions title="基本信息" :column="3" border>
          <el-descriptions-item label="用户ID">{{ userDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ userDetail.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ userDetail.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ userDetail.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ userDetail.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ formatGender(userDetail.gender) }}</el-descriptions-item>
          <el-descriptions-item label="生日">{{ userDetail.birthday || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ userDetail.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="头像">
            <div v-if="userDetail.avatar" class="avatar-container">
              <el-image :src="userDetail.avatar" fit="cover" style="width: 100px; height: 100px;"></el-image>
            </div>
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 用户状态信息 -->
        <el-descriptions class="margin-top" title="状态信息" :column="3" border>
          <el-descriptions-item label="账号状态">
            <el-tag :type="userDetail.status === 1 ? 'success' : 'danger'">
              {{ formatStatus(userDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="账号角色">
            <el-tag :type="userDetail.role === 2 ? 'warning' : 'info'">
              {{ formatRole(userDetail.role) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatDateTime(userDetail.registerTime) }}</el-descriptions-item>
          <el-descriptions-item label="最后登录时间">{{ formatDateTime(userDetail.lastLoginTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <div v-else class="empty-content">
        <el-empty description="用户不存在" />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.user-detail-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-title {
  font-size: 16px;
  font-weight: bold;
  margin-left: 8px;
}

.detail-content {
  padding: 20px 0;
}

.margin-top {
  margin-top: 20px;
}

.avatar-container {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
}

.empty-content {
  display: flex;
  justify-content: center;
  padding: 50px 0;
}
</style> 