<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View, Edit, Delete } from '@element-plus/icons-vue'
import { getUserList } from '@/api/user'
import type { UserInfo, UserQueryParams, PageResult } from '@/types/user'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'

const router = useRouter()

// 用户列表数据
const userList = ref<UserInfo[]>([])
// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
  pages: 0
})
// 加载状态
const loading = ref(false)

// 查询参数
const queryParams = reactive<UserQueryParams>({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  status: undefined,
  role: undefined,
  current: 1,
  size: 10,
  orderField: 'registerTime',
  orderType: 'desc'
})

// 重置查询
const resetQuery = () => {
  queryParams.username = ''
  queryParams.nickname = ''
  queryParams.phone = ''
  queryParams.email = ''
  queryParams.status = undefined
  queryParams.role = undefined
  queryParams.orderField = 'registerTime'
  queryParams.orderType = 'desc'
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      ...queryParams,
      current: pagination.current,
      size: pagination.size
    })
    
    userList.value = res.data.records
    pagination.total = res.data.total
    pagination.pages = res.data.pages
    pagination.current = res.data.current
    pagination.size = res.data.size
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 处理查询
const handleQuery = () => {
  pagination.current = 1
  fetchUserList()
}

// 处理重置
const handleReset = () => {
  resetQuery()
  handleQuery()
}

// 处理分页变化
const handlePageChange = (val: number) => {
  pagination.current = val
  fetchUserList()
}

// 处理每页条数变化
const handleSizeChange = (val: number) => {
  pagination.size = val
  pagination.current = 1
  fetchUserList()
}

// 处理排序变化
const handleSortChange = ({ prop, order }: { prop: string, order: string }) => {
  if (prop) {
    queryParams.orderField = prop
    queryParams.orderType = order === 'ascending' ? 'asc' : 'desc'
  } else {
    queryParams.orderField = 'registerTime'
    queryParams.orderType = 'desc'
  }
  fetchUserList()
}

// 格式化日期
const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return '无'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 查看用户详情
const handleViewDetail = (id: number) => {
  router.push(`/home/users/detail/${id}`)
}

// 组件挂载时获取用户列表
onMounted(() => {
  fetchUserList()
})

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
</script>

<template>
  <div class="user-list-container">
    <el-card class="search-card">
      <div class="search-header">
        <h3>用户查询</h3>
      </div>
      <el-form :model="queryParams" label-width="80px" inline>
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="queryParams.nickname" placeholder="请输入昵称" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="queryParams.email" placeholder="请输入邮箱" clearable />
        </el-form-item>
        <el-form-item label="用户状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option :value="1" label="正常" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户角色">
          <el-select v-model="queryParams.role" placeholder="请选择" clearable>
            <el-option :value="1" label="普通用户" />
            <el-option :value="2" label="管理员" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :icon="Search">查询</el-button>
          <el-button @click="handleReset" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="list-card">
      <div class="list-header">
        <h3>用户列表</h3>
      </div>

      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%"
        border
        stripe
        @sort-change="handleSortChange"
      >
        <el-table-column type="index" width="50" />
        <el-table-column prop="id" label="用户ID" width="80" sortable="custom" />
        <el-table-column prop="username" label="用户名" sortable="custom" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.role === 2 ? 'warning' : 'info'">
              {{ formatRole(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" sortable="custom">
          <template #default="scope">
            {{ formatDateTime(scope.row.registerTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录时间" sortable="custom">
          <template #default="scope">
            {{ formatDateTime(scope.row.lastLoginTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              :icon="View"
              circle
              @click="handleViewDetail(scope.row.id)"
              title="查看详情"
            />
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.user-list-container {
  padding: 20px;
}

.search-card,
.list-card {
  margin-bottom: 20px;
}

.search-header,
.list-header {
  margin-bottom: 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-header h3,
.list-header h3 {
  font-size: 16px;
  margin: 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 