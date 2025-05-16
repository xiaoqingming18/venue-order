<template>
  <div class="exchange-record-container">
    <div class="header">
      <h2>兑换记录管理</h2>
    </div>

    <!-- 搜索条件 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option :value="1" label="待处理" />
            <el-option :value="2" label="已完成" />
            <el-option :value="3" label="已取消" />
          </el-select>
        </el-form-item>
        <el-form-item label="兑换时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 兑换记录列表 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <div class="header-title">兑换记录列表</div>
          <div class="header-actions">
            <el-button-group>
              <el-button type="primary" @click="handleProcessSelected" :disabled="selectedRecords.length === 0">
                批量处理
              </el-button>
              <el-button type="danger" @click="handleCancelSelected" :disabled="selectedRecords.length === 0">
                批量取消
              </el-button>
            </el-button-group>
            <el-button type="success" @click="exportRecords">导出记录</el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="recordsList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column label="商品信息" min-width="180">
          <template #default="scope">
            <div class="product-info">
              <el-image
                style="width: 40px; height: 40px"
                :src="scope.row.productImageUrl || defaultImage"
                fit="cover"
              />
              <div class="product-detail">
                <div class="product-name">{{ scope.row.productName }}</div>
                <div class="points-info">{{ scope.row.pointsPrice }} 积分</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="exchangeQuantity" label="数量" width="80" />
        <el-table-column label="总积分" width="100">
          <template #default="scope">
            <span class="total-points">{{ scope.row.pointsPrice * scope.row.exchangeQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="contactInfo" label="联系方式" min-width="150" />
        <el-table-column prop="remarks" label="备注" min-width="150" />
        <el-table-column label="订单状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="兑换时间" width="180" sortable />
        <el-table-column prop="processTime" label="处理时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleProcess(scope.row)"
              :disabled="scope.row.status !== 1"
            >
              处理
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleCancel(scope.row)"
              :disabled="scope.row.status !== 1"
            >
              取消
            </el-button>
            <el-button 
              type="info" 
              size="small" 
              @click="viewDetail(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-if="total > 0"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 订单处理对话框 -->
    <el-dialog
      title="处理兑换订单"
      v-model="processDialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="processFormRef"
        :model="processForm"
        :rules="processRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="订单号">
          <el-input v-model="currentRecord.orderNo" disabled />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="currentRecord.username" disabled />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="currentRecord.productName" disabled />
        </el-form-item>
        <el-form-item label="数量">
          <el-input v-model="currentRecord.exchangeQuantity" disabled />
        </el-form-item>
        <el-form-item label="总积分">
          <el-input :value="currentRecord.pointsPrice * currentRecord.exchangeQuantity" disabled />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="currentRecord.contactInfo" disabled />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="currentRecord.remarks" disabled />
        </el-form-item>
        <el-form-item label="处理备注" prop="processRemarks">
          <el-input
            v-model="processForm.processRemarks"
            type="textarea"
            :rows="3"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProcessForm" :loading="submitLoading">确认处理</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 订单详情对话框 -->
    <el-dialog
      title="兑换订单详情"
      v-model="detailDialogVisible"
      width="500px"
    >
      <div class="order-detail">
        <div class="detail-item">
          <span class="detail-label">订单号:</span>
          <span class="detail-value">{{ currentRecord.orderNo }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">用户ID:</span>
          <span class="detail-value">{{ currentRecord.userId }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">用户名:</span>
          <span class="detail-value">{{ currentRecord.username }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">商品名称:</span>
          <span class="detail-value">{{ currentRecord.productName }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">积分单价:</span>
          <span class="detail-value">{{ currentRecord.pointsPrice }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">兑换数量:</span>
          <span class="detail-value">{{ currentRecord.exchangeQuantity }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">总积分:</span>
          <span class="detail-value highlight">{{ currentRecord.pointsPrice * currentRecord.exchangeQuantity }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">联系方式:</span>
          <span class="detail-value">{{ currentRecord.contactInfo }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">用户备注:</span>
          <span class="detail-value">{{ currentRecord.remarks || '无' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">订单状态:</span>
          <span class="detail-value">
            <el-tag :type="getStatusTag(currentRecord.status)">
              {{ getStatusName(currentRecord.status) }}
            </el-tag>
          </span>
        </div>
        <div class="detail-item">
          <span class="detail-label">兑换时间:</span>
          <span class="detail-value">{{ currentRecord.createTime }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">处理时间:</span>
          <span class="detail-value">{{ currentRecord.processTime || '未处理' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">处理备注:</span>
          <span class="detail-value">{{ currentRecord.processRemarks || '无' }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { 
  getExchangeRecordsByPage, 
  processExchangeRecord, 
  cancelExchangeRecord, 
  exportExchangeRecords 
} from '@/api/points'
import { ExchangeRecord } from '@/types/points'

// 列表数据相关
const recordsList = ref<ExchangeRecord[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedRecords = ref<ExchangeRecord[]>([])
const dateRange = ref(null)
const defaultImage = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

// 搜索条件
const searchForm = reactive({
  userId: '',
  orderNo: '',
  productName: '',
  status: '',
  startDate: '',
  endDate: ''
})

// 处理对话框相关
const processDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRecord = reactive<ExchangeRecord>({} as ExchangeRecord)
const processFormRef = ref<FormInstance>()
const submitLoading = ref(false)

// 处理表单
const processForm = reactive({
  orderNo: '',
  processRemarks: ''
})

// 表单验证规则
const processRules = reactive<FormRules>({
  processRemarks: [
    { required: true, message: '请输入处理备注', trigger: 'blur' },
    { max: 200, message: '长度不能超过200个字符', trigger: 'blur' }
  ]
})

// 监听日期选择器变化
watch(dateRange, (value) => {
  if (value) {
    searchForm.startDate = value[0]
    searchForm.endDate = value[1]
  } else {
    searchForm.startDate = ''
    searchForm.endDate = ''
  }
})

// 获取兑换记录列表
const fetchRecordsList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    
    const { data } = await getExchangeRecordsByPage(params)
    recordsList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取兑换记录列表失败', error)
    ElMessage.error('获取兑换记录列表失败')
  } finally {
    loading.value = false
  }
}

// 处理表格选择变化
const handleSelectionChange = (selection: ExchangeRecord[]) => {
  selectedRecords.value = selection
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchRecordsList()
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.userId = ''
  searchForm.orderNo = ''
  searchForm.productName = ''
  searchForm.status = ''
  searchForm.startDate = ''
  searchForm.endDate = ''
  dateRange.value = null
  currentPage.value = 1
  fetchRecordsList()
}

// 处理单个订单
const handleProcess = (row: ExchangeRecord) => {
  Object.assign(currentRecord, row)
  processForm.orderNo = row.orderNo
  processForm.processRemarks = ''
  processDialogVisible.value = true
}

// 提交处理表单
const submitProcessForm = async () => {
  if (!processFormRef.value) return
  
  await processFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await processExchangeRecord({
          orderNo: processForm.orderNo,
          processRemarks: processForm.processRemarks
        })
        ElMessage.success('处理成功')
        processDialogVisible.value = false
        fetchRecordsList()
      } catch (error) {
        console.error('处理失败', error)
        ElMessage.error('处理失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 取消订单
const handleCancel = async (row: ExchangeRecord) => {
  try {
    await ElMessageBox.confirm('确定要取消该兑换订单吗？取消后会退回用户积分', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelExchangeRecord({ orderNo: row.orderNo })
    ElMessage.success('取消成功')
    fetchRecordsList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消失败', error)
      ElMessage.error('取消失败')
    }
  }
}

// 批量处理订单
const handleProcessSelected = async () => {
  const pendingRecords = selectedRecords.value.filter(record => record.status === 1)
  
  if (pendingRecords.length === 0) {
    ElMessage.warning('没有可处理的订单，请选择待处理状态的订单')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要批量处理选中的 ${pendingRecords.length} 个订单吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const processRemarks = await ElMessageBox.prompt('请输入处理备注', '批量处理', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValidator: (value) => {
        return value.length > 0 && value.length <= 200
      },
      inputErrorMessage: '处理备注不能为空且不超过200个字符'
    })
    
    loading.value = true
    try {
      const promises = pendingRecords.map(record => 
        processExchangeRecord({
          orderNo: record.orderNo,
          processRemarks: processRemarks.value
        })
      )
      await Promise.all(promises)
      ElMessage.success(`成功处理 ${pendingRecords.length} 个订单`)
      fetchRecordsList()
    } catch (error) {
      console.error('批量处理失败', error)
      ElMessage.error('批量处理失败')
    } finally {
      loading.value = false
    }
  } catch (error) {
    // 用户取消操作
  }
}

// 批量取消订单
const handleCancelSelected = async () => {
  const pendingRecords = selectedRecords.value.filter(record => record.status === 1)
  
  if (pendingRecords.length === 0) {
    ElMessage.warning('没有可取消的订单，请选择待处理状态的订单')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要批量取消选中的 ${pendingRecords.length} 个订单吗？取消后会退回用户积分`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    try {
      const promises = pendingRecords.map(record => 
        cancelExchangeRecord({ orderNo: record.orderNo })
      )
      await Promise.all(promises)
      ElMessage.success(`成功取消 ${pendingRecords.length} 个订单`)
      fetchRecordsList()
    } catch (error) {
      console.error('批量取消失败', error)
      ElMessage.error('批量取消失败')
    } finally {
      loading.value = false
    }
  } catch (error) {
    // 用户取消操作
  }
}

// 查看订单详情
const viewDetail = (row: ExchangeRecord) => {
  Object.assign(currentRecord, row)
  detailDialogVisible.value = true
}

// 导出记录
const exportRecords = async () => {
  try {
    await ElMessageBox.confirm('确定要导出当前筛选条件下的兑换记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    loading.value = true
    try {
      await exportExchangeRecords(searchForm)
      ElMessage.success('导出成功，请在下载列表中查看')
    } catch (error) {
      console.error('导出失败', error)
      ElMessage.error('导出失败')
    } finally {
      loading.value = false
    }
  } catch (error) {
    // 用户取消操作
  }
}

// 获取订单状态标签类型
const getStatusTag = (status: number) => {
  const map: Record<number, string> = {
    1: 'warning',
    2: 'success',
    3: 'info'
  }
  return map[status] || ''
}

// 获取订单状态名称
const getStatusName = (status: number) => {
  const map: Record<number, string> = {
    1: '待处理',
    2: '已完成',
    3: '已取消'
  }
  return map[status] || '未知'
}

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchRecordsList()
}

// 处理当前页变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchRecordsList()
}

// 页面加载时获取数据
onMounted(() => {
  fetchRecordsList()
})
</script>

<style scoped>
.exchange-record-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  font-weight: 600;
  font-size: 22px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-detail {
  display: flex;
  flex-direction: column;
}

.product-name {
  font-weight: 500;
}

.points-info {
  font-size: 12px;
  color: #E6A23C;
}

.total-points {
  font-weight: bold;
  color: #E6A23C;
}

/* 订单详情样式 */
.order-detail {
  padding: 10px;
}

.detail-item {
  display: flex;
  margin-bottom: 15px;
}

.detail-label {
  width: 100px;
  color: #606266;
  text-align: right;
  padding-right: 12px;
}

.detail-value {
  flex: 1;
  color: #303133;
}

.detail-value.highlight {
  font-weight: bold;
  color: #E6A23C;
}
</style> 