<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getVenueReviewStats } from '@/api/review'
import type { ReviewStats } from '@/types/review'

const router = useRouter()

// 数据相关
const tableData = ref<ReviewStats[]>([])
const loading = ref(false)
const total = ref(0)

// 查询条件
const queryForm = reactive({
  page: 1,
  size: 10
})

// 获取评价统计数据
const fetchReviewStats = async () => {
  loading.value = true
  try {
    const res = await getVenueReviewStats(queryForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取评价统计数据失败', error)
  } finally {
    loading.value = false
  }
}

// 分页变化
const handleSizeChange = (size: number) => {
  queryForm.size = size
  fetchReviewStats()
}

const handleCurrentChange = (page: number) => {
  queryForm.page = page
  fetchReviewStats()
}

// 查看场馆详情
const goToVenueDetail = (venueId: number) => {
  router.push(`/home/venues/${venueId}/detail`)
}

// 查看场馆评价列表
const goToVenueReviews = (venueId: number) => {
  router.push('/home/reviews')
  // 在reviews列表页面自动填充场馆ID并触发查询
  // 注意：这里需要通过路由传递参数或全局状态管理实现
}

// 计算评分等级颜色
const getScoreColor = (score: number) => {
  if (score >= 4.5) return '#67c23a' // 优秀
  if (score >= 4.0) return '#409eff' // 良好
  if (score >= 3.5) return '#e6a23c' // 中等
  return '#f56c6c' // 差
}

// 格式化百分比
const formatPercent = (value: number) => {
  return `${(value * 100).toFixed(1)}%`
}

onMounted(() => {
  fetchReviewStats()
})
</script>

<template>
  <div class="review-stats-container">
    <div class="page-header">
      <h2>评价统计</h2>
    </div>

    <!-- 统计列表 -->
    <el-table :data="tableData" style="width: 100%" v-loading="loading" border>
      <el-table-column type="index" width="50" align="center" />
      <el-table-column prop="venueId" label="场馆ID" width="80" align="center" />
      <el-table-column prop="venueName" label="场馆名称" min-width="120">
        <template #default="{ row }">
          <el-button type="primary" link @click="goToVenueDetail(row.venueId)">
            {{ row.venueName }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="评价数量" width="180" align="center">
        <template #default="{ row }">
          <div class="stats-count">
            <div class="count-item">
              <span class="count-label">总计:</span>
              <span class="count-value">{{ row.totalCount }}</span>
            </div>
            <div class="count-item">
              <el-tag type="warning" size="small">待审核: {{ row.pendingCount }}</el-tag>
            </div>
            <div class="count-item">
              <el-tag type="success" size="small">已通过: {{ row.approvedCount }}</el-tag>
            </div>
            <div class="count-item">
              <el-tag type="danger" size="small">已拒绝: {{ row.rejectedCount }}</el-tag>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="平均评分" min-width="200">
        <template #default="{ row }">
          <div class="stats-scores">
            <div class="score-item">
              <span class="score-label">综合评分:</span>
              <span class="score-value" :style="{ color: getScoreColor(row.overallAvgScore) }">
                {{ row.overallAvgScore.toFixed(1) }}
              </span>
              <el-rate v-model="row.overallAvgScore" disabled :colors="['#F56C6C', '#E6A23C', '#67C23A']" />
            </div>
            <div class="score-item">
              <span class="score-label">环境评分:</span>
              <span class="score-value" :style="{ color: getScoreColor(row.environmentAvgScore) }">
                {{ row.environmentAvgScore.toFixed(1) }}
              </span>
            </div>
            <div class="score-item">
              <span class="score-label">设施评分:</span>
              <span class="score-value" :style="{ color: getScoreColor(row.facilityAvgScore) }">
                {{ row.facilityAvgScore.toFixed(1) }}
              </span>
            </div>
            <div class="score-item">
              <span class="score-label">服务评分:</span>
              <span class="score-value" :style="{ color: getScoreColor(row.serviceAvgScore) }">
                {{ row.serviceAvgScore.toFixed(1) }}
              </span>
            </div>
            <div class="score-item">
              <span class="score-label">性价比:</span>
              <span class="score-value" :style="{ color: getScoreColor(row.costPerformanceAvgScore) }">
                {{ row.costPerformanceAvgScore.toFixed(1) }}
              </span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="goodReviewRate" label="好评率" width="100" align="center">
        <template #default="{ row }">
          <div class="good-rate">
            <span :style="{ color: getScoreColor(row.goodReviewRate * 5) }">
              {{ formatPercent(row.goodReviewRate) }}
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="goToVenueReviews(row.venueId)">查看评价</el-button>
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
.review-stats-container {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.stats-count {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.count-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.count-label {
  color: #606266;
  font-size: 13px;
}

.count-value {
  font-weight: bold;
}

.stats-scores {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.score-item {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 5px;
}

.score-label {
  width: 70px;
  color: #606266;
  font-size: 13px;
  text-align: right;
}

.score-value {
  font-weight: bold;
  width: 30px;
}

.good-rate {
  font-weight: bold;
  font-size: 16px;
}
</style> 