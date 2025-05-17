<template>
  <div class="feedback-container">
    <el-card class="feedback-card">
      <template #header>
        <div class="card-header">
          <h3>用户反馈管理</h3>
          <div class="header-actions">
            <el-select v-model="queryParams.status" placeholder="状态筛选" clearable @change="handleFilterChange">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-select v-model="queryParams.type" placeholder="类型筛选" clearable @change="handleFilterChange">
              <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-input
              v-model="queryParams.keyword"
              placeholder="搜索反馈内容"
              style="width: 200px; margin-left: 10px"
              clearable
              @input="handleFilterChange"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="fetchFeedbackList">
              <el-icon><Search /></el-icon>查询
            </el-button>
            <el-button @click="resetFilter">
              <el-icon><Refresh /></el-icon>重置
            </el-button>
          </div>
        </div>
      </template>

      <!-- 数据统计卡片 -->
      <div class="stats-cards">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-value">{{ stats.pending || 0 }}</div>
          <div class="stat-title">待处理</div>
        </el-card>
        <el-card class="stat-card" shadow="hover">
          <div class="stat-value">{{ stats.processing || 0 }}</div>
          <div class="stat-title">处理中</div>
        </el-card>
        <el-card class="stat-card" shadow="hover">
          <div class="stat-value">{{ stats.replied || 0 }}</div>
          <div class="stat-title">已回复</div>
        </el-card>
        <el-card class="stat-card" shadow="hover">
          <div class="stat-value">{{ stats.closed || 0 }}</div>
          <div class="stat-title">已关闭</div>
        </el-card>
        <el-card class="stat-card" shadow="hover">
          <div class="stat-value">{{ stats.total || 0 }}</div>
          <div class="stat-title">总计</div>
        </el-card>
      </div>

      <!-- 反馈列表表格 -->
      <el-table
        v-loading="loading"
        :data="feedbackList"
        border
        style="width: 100%; margin-top: 20px"
        @row-click="handleRowClick"
      >
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="typeName" label="类型" width="100" align="center" />
        <el-table-column prop="nickname" label="用户" width="120" align="center">
          <template #default="{ row }">
            {{ row.nickname || row.username || "未知用户" }}
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180" align="center" />
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="handleView(row.id)">查看</el-button>
            <el-button
              v-if="row.status === 0"
              link
              type="primary"
              @click.stop="handleUpdateStatus(row.id, 1)"
            >
              受理
            </el-button>
            <el-button
              v-if="row.status < 2"
              link
              type="success"
              @click.stop="handleReply(row)"
            >
              回复
            </el-button>
            <el-button
              v-if="row.status < 3"
              link
              type="warning"
              @click.stop="handleUpdateStatus(row.id, 3)"
            >
              关闭
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 回复对话框 -->
    <el-dialog v-model="replyDialogVisible" title="回复反馈" width="500px" append-to-body>
      <div class="feedback-detail" v-if="currentFeedback">
        <h4 class="feedback-title">{{ currentFeedback.title }}</h4>
        <p class="feedback-meta">
          <span>用户：{{ currentFeedback.nickname || currentFeedback.username }}</span>
          <span>类型：{{ currentFeedback.typeName }}</span>
          <span>提交时间：{{ currentFeedback.createdAt }}</span>
        </p>
        <div class="feedback-content">{{ currentFeedback.content }}</div>
      </div>
      <el-form :model="replyForm" ref="replyFormRef" :rules="replyRules" label-width="80px">
        <el-form-item label="回复内容" prop="content">
          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="replyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReply" :loading="submitting">
            提交回复
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { getFeedbackList, updateFeedbackStatus, replyFeedback, getFeedbackStats } from '@/api/feedback'
import type { 
  Feedback, 
  FeedbackQueryParams, 
  FeedbackReplyRequest,
  FeedbackStats
} from '@/types/feedback'
import { FeedbackStatus, FeedbackType } from '@/types/feedback'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const feedbackList = ref<Feedback[]>([])
const total = ref(0)
const replyDialogVisible = ref(false)
const currentFeedback = ref<Feedback | null>(null)
const replyFormRef = ref<FormInstance>()
const stats = ref<FeedbackStats>({
  total: 0,
  pending: 0,
  processing: 0,
  replied: 0,
  closed: 0,
  todayCount: 0,
  weekCount: 0,
  monthCount: 0,
  typeDistribution: []
})

// 查询参数
const queryParams = reactive<FeedbackQueryParams>({
  status: undefined,
  type: undefined,
  keyword: '',
  pageNum: 1,
  pageSize: 10
})

// 回复表单
const replyForm = reactive<FeedbackReplyRequest>({
  feedbackId: 0,
  content: ''
})

// 表单校验规则
const replyRules = {
  content: [
    { required: true, message: '请输入回复内容', trigger: 'blur' },
    { min: 1, max: 500, message: '回复内容长度应在1-500个字符之间', trigger: 'blur' }
  ]
}

// 状态选项
const statusOptions = [
  { value: 0, label: '待处理' },
  { value: 1, label: '处理中' },
  { value: 2, label: '已回复' },
  { value: 3, label: '已关闭' }
]

// 类型选项
const typeOptions = [
  { value: 1, label: '问题反馈' },
  { value: 2, label: '功能建议' },
  { value: 3, label: '投诉' },
  { value: 4, label: '其他' }
]

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  switch (status) {
    case FeedbackStatus.PENDING:
      return 'info'
    case FeedbackStatus.PROCESSING:
      return 'warning'
    case FeedbackStatus.REPLIED:
      return 'success'
    case FeedbackStatus.CLOSED:
      return 'danger'
    default:
      return 'info'
  }
}

// 获取反馈列表
const fetchFeedbackList = async () => {
  loading.value = true
  try {
    const res = await getFeedbackList(queryParams)
    feedbackList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取反馈列表失败:', error)
    ElMessage.error('获取反馈列表失败')
  } finally {
    loading.value = false
  }
}

// 获取反馈统计数据
const fetchFeedbackStats = async () => {
  try {
    const res = await getFeedbackStats()
    stats.value = res.data
  } catch (error) {
    console.error('获取反馈统计数据失败:', error)
  }
}

// 查看反馈详情
const handleView = (id: number) => {
  router.push(`/home/feedback/detail/${id}`)
}

// 行点击事件
const handleRowClick = (row: Feedback) => {
  handleView(row.id)
}

// 更新反馈状态
const handleUpdateStatus = async (id: number, status: number) => {
  try {
    await updateFeedbackStatus(id, status)
    ElMessage.success('状态更新成功')
    // 刷新列表
    fetchFeedbackList()
    // 刷新统计数据
    fetchFeedbackStats()
  } catch (error) {
    console.error('更新反馈状态失败:', error)
    ElMessage.error('更新反馈状态失败')
  }
}

// 打开回复对话框
const handleReply = (feedback: Feedback) => {
  currentFeedback.value = feedback
  replyForm.feedbackId = feedback.id
  replyForm.content = ''
  replyDialogVisible.value = true
}

// 提交回复
const submitReply = async () => {
  if (!replyFormRef.value) return
  await replyFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await replyFeedback(replyForm)
        // 更新反馈状态为已回复
        await updateFeedbackStatus(replyForm.feedbackId, FeedbackStatus.REPLIED)
        ElMessage.success('回复提交成功')
        replyDialogVisible.value = false
        // 刷新列表和统计数据
        fetchFeedbackList()
        fetchFeedbackStats()
      } catch (error) {
        console.error('提交回复失败:', error)
        ElMessage.error('提交回复失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 筛选条件变化
const handleFilterChange = () => {
  queryParams.pageNum = 1
  fetchFeedbackList()
}

// 重置筛选条件
const resetFilter = () => {
  queryParams.status = undefined
  queryParams.type = undefined
  queryParams.keyword = ''
  queryParams.pageNum = 1
  fetchFeedbackList()
}

// 分页大小变化
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  fetchFeedbackList()
}

// 页码变化
const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val
  fetchFeedbackList()
}

// 页面加载时获取数据
onMounted(() => {
  fetchFeedbackList()
  fetchFeedbackStats()
})
</script>

<style scoped>
.feedback-container {
  padding: 20px;
}

.feedback-card {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stats-cards {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.stat-title {
  margin-top: 5px;
  color: #606266;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.feedback-detail {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.feedback-title {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
}

.feedback-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
}

.feedback-content {
  white-space: pre-wrap;
  font-size: 14px;
  padding: 10px;
  background-color: #fff;
  border-radius: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 