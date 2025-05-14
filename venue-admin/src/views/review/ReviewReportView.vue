<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReportList, getReportById, auditReport, getReviewById } from '@/api/review'
import type { ReviewReport, Review } from '@/types/review'
import { getReportStatusLabel, getReportStatusType, reportStatusOptions } from '@/types/review'

const router = useRouter()

// 列表数据
const tableData = ref<ReviewReport[]>([])
const loading = ref(false)
const total = ref(0)

// 查询条件
const queryForm = reactive({
  page: 1,
  size: 10,
  status: undefined as number | undefined
})

// 获取举报列表
const fetchReports = async () => {
  loading.value = true
  try {
    const res = await getReportList(queryForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取举报列表失败', error)
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryForm.page = 1
  fetchReports()
}

// 重置查询条件
const resetQuery = () => {
  queryForm.status = undefined
  handleQuery()
}

// 分页变化
const handleSizeChange = (size: number) => {
  queryForm.size = size
  fetchReports()
}

const handleCurrentChange = (page: number) => {
  queryForm.page = page
  fetchReports()
}

// 查看评价详情
const viewReview = (reviewId: number) => {
  router.push(`/home/reviews/${reviewId}`)
}

// 审核对话框
const auditDialogVisible = ref(false)
const currentReport = ref<ReviewReport | null>(null)
const reviewDetail = ref<Review | null>(null)
const auditLoading = ref(false)
const auditForm = reactive({
  status: 1, // 默认通过
  adminNotes: '',
  banReview: false,
  banReason: ''
})

// 打开审核对话框
const handleAudit = async (row: ReviewReport) => {
  currentReport.value = row
  auditForm.status = 1
  auditForm.adminNotes = ''
  auditForm.banReview = false
  auditForm.banReason = ''
  
  // 获取评价详情
  try {
    const res = await getReviewById(row.reviewId)
    reviewDetail.value = res.data
  } catch (error) {
    console.error('获取评价详情失败', error)
    reviewDetail.value = null
  }
  
  auditDialogVisible.value = true
}

// 提交审核
const submitAudit = async () => {
  if (!currentReport.value) return
  
  // 检查表单
  if (auditForm.status === 1 && auditForm.banReview && !auditForm.banReason.trim()) {
    ElMessage.warning('请输入封禁原因')
    return
  }
  
  auditLoading.value = true
  try {
    await auditReport(currentReport.value.id, auditForm)
    ElMessage.success('审核成功')
    auditDialogVisible.value = false
    fetchReports()
  } catch (error) {
    console.error('审核失败', error)
    ElMessage.error('审核失败')
  } finally {
    auditLoading.value = false
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 初始化
onMounted(() => {
  fetchReports()
})
</script>

<template>
  <div class="report-list-container">
    <div class="page-header">
      <h2>评价举报管理</h2>
      <div class="header-actions">
        <el-button @click="resetQuery">重置</el-button>
      </div>
    </div>

    <!-- 查询表单 -->
    <div class="filter-container">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="举报状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option v-for="item in reportStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 举报列表 -->
    <el-table :data="tableData" style="width: 100%" v-loading="loading" border>
      <el-table-column type="index" width="50" align="center" />
      <el-table-column prop="id" label="举报ID" width="80" align="center" />
      <el-table-column prop="reviewId" label="评价ID" width="80" align="center">
        <template #default="{ row }">
          <el-button type="primary" link @click="viewReview(row.reviewId)">{{ row.reviewId }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="reporterId" label="举报人ID" width="100" align="center" />
      <el-table-column prop="reporterNickname" label="举报人昵称" min-width="120" />
      <el-table-column label="举报原因" min-width="200">
        <template #default="{ row }">
          <div class="reason-cell">
            <div class="reason-type">{{ row.reason }}</div>
            <div v-if="row.reasonDetail" class="reason-detail">{{ row.reasonDetail }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getReportStatusType(row.status)">
            {{ getReportStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="举报时间" width="160" align="center">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="primary" link size="small" @click="handleAudit(row)">审核</el-button>
          <el-button v-else type="info" link size="small" disabled>已处理</el-button>
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
    
    <!-- 审核对话框 -->
    <el-dialog
      title="审核举报"
      v-model="auditDialogVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-loading="auditLoading">
        <div v-if="currentReport && reviewDetail" class="audit-dialog-content">
          <!-- 评价内容 -->
          <div class="review-preview">
            <div class="preview-title">举报的评价内容：</div>
            <div class="preview-content">
              <div class="preview-user">{{ reviewDetail.nickname }}：</div>
              <div class="preview-text">{{ reviewDetail.content }}</div>
            </div>
          </div>
          
          <!-- 举报信息 -->
          <div class="report-info">
            <div class="info-item">
              <span class="info-label">举报原因：</span>
              <span class="info-value">{{ currentReport.reason }}</span>
            </div>
            <div class="info-item" v-if="currentReport.reasonDetail">
              <span class="info-label">详细说明：</span>
              <span class="info-value">{{ currentReport.reasonDetail }}</span>
            </div>
          </div>
          
          <!-- 审核表单 -->
          <el-form :model="auditForm" label-width="100px" class="audit-form">
            <el-form-item label="审核结果">
              <el-radio-group v-model="auditForm.status">
                <el-radio :label="1">通过举报</el-radio>
                <el-radio :label="2">拒绝举报</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="处理说明">
              <el-input
                v-model="auditForm.adminNotes"
                type="textarea"
                rows="2"
                placeholder="请输入处理说明..."
              />
            </el-form-item>
            
            <el-form-item label="封禁评价" v-if="auditForm.status === 1">
              <el-switch v-model="auditForm.banReview" />
            </el-form-item>
            
            <el-form-item label="封禁原因" v-if="auditForm.status === 1 && auditForm.banReview">
              <el-input
                v-model="auditForm.banReason"
                type="textarea"
                rows="2"
                placeholder="请输入封禁原因..."
              />
            </el-form-item>
          </el-form>
        </div>
        <el-empty v-else description="加载评价详情失败"></el-empty>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAudit" :loading="auditLoading">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.report-list-container {
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

.reason-cell {
  display: flex;
  flex-direction: column;
}

.reason-type {
  font-weight: bold;
  margin-bottom: 5px;
}

.reason-detail {
  color: #606266;
  font-size: 13px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.audit-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-preview {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.preview-title {
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
}

.preview-content {
  display: flex;
  flex-direction: column;
}

.preview-user {
  font-weight: bold;
  margin-bottom: 5px;
}

.preview-text {
  line-height: 1.6;
  white-space: pre-wrap;
}

.report-info {
  padding: 15px;
  background-color: #fdf6ec;
  border-radius: 4px;
  border-left: 3px solid #e6a23c;
}

.info-item {
  margin-bottom: 8px;
}

.info-label {
  font-weight: bold;
  color: #303133;
}

.audit-form {
  margin-top: 10px;
}
</style> 