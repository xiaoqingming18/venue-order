<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReviewList, deleteReview, banReview, unbanReview } from '@/api/review'
import type { Review } from '@/types/review'
import { getReviewStatusLabel, getReviewStatusType, reviewStatusOptions } from '@/types/review'

const router = useRouter()

// 列表数据
const tableData = ref<Review[]>([])
const loading = ref(false)
const total = ref(0)

// 查询条件
const queryForm = reactive({
  page: 1,
  size: 10,
  venueId: undefined as number | undefined,
  userId: undefined as number | undefined,
  status: undefined as number | undefined
})

// 获取评价列表
const fetchReviews = async () => {
  loading.value = true
  try {
    const res = await getReviewList(queryForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取评价列表失败', error)
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryForm.page = 1
  fetchReviews()
}

// 重置查询条件
const resetQuery = () => {
  queryForm.venueId = undefined
  queryForm.userId = undefined
  queryForm.status = undefined
  handleQuery()
}

// 分页变化
const handleSizeChange = (size: number) => {
  queryForm.size = size
  fetchReviews()
}

const handleCurrentChange = (page: number) => {
  queryForm.page = page
  fetchReviews()
}

// 查看详情
const handleDetail = (row: Review) => {
  router.push(`/home/reviews/${row.id}`)
}

// 封禁评价
const handleBan = (row: Review) => {
  ElMessageBox.prompt('请输入封禁原因', '封禁评价', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /\S+/,
    inputErrorMessage: '封禁原因不能为空'
  }).then(async ({ value }) => {
    try {
      await banReview(row.id, value)
      ElMessage.success('评价已封禁')
      fetchReviews()
    } catch (error) {
      console.error('封禁评价失败', error)
      ElMessage.error('封禁失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 解除封禁
const handleUnban = (row: Review) => {
  ElMessageBox.confirm('确认解除此评价的封禁状态？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await unbanReview(row.id)
      ElMessage.success('评价已解除封禁')
      fetchReviews()
    } catch (error) {
      console.error('解除封禁失败', error)
      ElMessage.error('解除封禁失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 删除评价
const handleDelete = (row: Review) => {
  ElMessageBox.confirm('确认删除此评价吗？操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteReview(row.id)
      ElMessage.success('删除成功')
      fetchReviews()
    } catch (error) {
      console.error('删除评价失败', error)
    }
  }).catch(() => {
    // 取消删除
  })
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 初始化
onMounted(() => {
  fetchReviews()
})
</script>

<template>
  <div class="review-list-container">
    <div class="page-header">
      <h2>评价管理</h2>
      <div class="header-actions">
        <el-button @click="resetQuery">重置</el-button>
      </div>
    </div>

    <!-- 查询表单 -->
    <div class="filter-container">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="场馆ID">
          <el-input v-model="queryForm.venueId" placeholder="请输入场馆ID" clearable />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="queryForm.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="评价状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option v-for="item in reviewStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 评价列表 -->
    <el-table :data="tableData" style="width: 100%" v-loading="loading" border>
      <el-table-column type="index" width="50" align="center" />
      <el-table-column prop="id" label="评价ID" width="80" align="center" />
      <el-table-column prop="venueId" label="场馆ID" width="80" align="center" />
      <el-table-column prop="venueName" label="场馆名称" min-width="120" />
      <el-table-column prop="userId" label="用户ID" width="80" align="center" />
      <el-table-column prop="nickname" label="用户昵称" min-width="120" />
      <el-table-column label="评价内容" min-width="200">
        <template #default="{ row }">
          <div class="review-content-cell">
            <div class="review-text">{{ row.content }}</div>
            <div class="review-scores">
              <el-tooltip content="综合评分" placement="top">
                <span class="score-item">总体：{{ row.overallScore }}分</span>
              </el-tooltip>
              <el-tooltip content="环境评分" placement="top">
                <span class="score-item">环境：{{ row.environmentScore }}分</span>
              </el-tooltip>
              <el-tooltip content="设施评分" placement="top">
                <span class="score-item">设施：{{ row.facilityScore }}分</span>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getReviewStatusType(row.status)">
            {{ getReviewStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="评价时间" width="160" align="center">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" align="center">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleDetail(row)">详情</el-button>
          <el-button v-if="row.status === 0" type="danger" link size="small" @click="handleBan(row)">封禁</el-button>
          <el-button v-if="row.status === 1" type="success" link size="small" @click="handleUnban(row)">解封</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<style scoped>
.review-list-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
}

.filter-container {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.review-content-cell {
  display: flex;
  flex-direction: column;
}

.review-text {
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.review-scores {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.score-item {
  font-size: 12px;
  color: #606266;
  background-color: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
}
</style> 