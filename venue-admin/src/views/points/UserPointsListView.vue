<template>
  <div class="user-points-container">
    <div class="header">
      <h2>用户积分管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleExpiredPoints">处理过期积分</el-button>
      </div>
    </div>

    <!-- 搜索条件 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="积分范围">
          <el-input-number v-model="searchForm.minPoints" :min="0" placeholder="最小值" class="points-range" />
          <span style="margin: 0 5px;">-</span>
          <el-input-number v-model="searchForm.maxPoints" :min="0" placeholder="最大值" class="points-range" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户积分列表 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <div class="header-title">用户积分列表</div>
          <div class="header-info">
            <el-tag type="info">总用户数：{{ total }}</el-tag>
            <el-tag type="success">积分总量：{{ totalUserPoints }}</el-tag>
          </div>
        </div>
      </template>

      <el-table
        :data="pointsList"
        style="width: 100%"
        v-loading="loading"
        border
      >
        <el-table-column prop="userId" label="用户ID" width="100" sortable />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="totalPoints" label="总积分" width="100" sortable>
          <template #default="scope">
            <span class="highlight">{{ scope.row.totalPoints }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="availablePoints" label="可用积分" width="100" sortable>
          <template #default="scope">
            <span class="success">{{ scope.row.availablePoints }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="frozenPoints" label="冻结积分" width="100" sortable>
          <template #default="scope">
            <span class="warning">{{ scope.row.frozenPoints }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="expiredPoints" label="已过期积分" width="120" sortable>
          <template #default="scope">
            <span class="danger">{{ scope.row.expiredPoints }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastPointsUpdateTime" label="最后更新时间" width="180" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleAdjustPoints(scope.row)">调整积分</el-button>
            <el-button type="info" size="small" @click="viewPointRecords(scope.row.userId)">查看记录</el-button>
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

    <!-- 积分调整对话框 -->
    <el-dialog
      title="调整用户积分"
      v-model="adjustDialogVisible"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="adjustFormRef"
        :model="adjustForm"
        :rules="adjustRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="adjustForm.userId" disabled />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="currentUser.username" disabled />
        </el-form-item>
        <el-form-item label="当前积分">
          <el-input v-model="currentUser.availablePoints" disabled>
            <template #append>可用积分</template>
          </el-input>
        </el-form-item>
        <el-form-item label="调整类型" prop="sourceType">
          <el-select v-model="adjustForm.sourceType" placeholder="请选择调整类型">
            <el-option :value="1" label="预约奖励" />
            <el-option :value="2" label="评价奖励" />
            <el-option :value="3" label="签到奖励" />
            <el-option :value="4" label="邀请奖励" />
            <el-option :value="5" label="活动奖励" />
            <el-option :value="6" label="积分扣减" />
            <el-option :value="7" label="手动调整" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分值" prop="points">
          <el-input-number 
            v-model="adjustForm.points" 
            :min="-10000" 
            :max="10000" 
            placeholder="调整的积分值" 
            :class="{'negative-points': adjustForm.points < 0}"
          />
          <div class="form-help">正数表示增加积分，负数表示扣减积分</div>
        </el-form-item>
        <el-form-item label="有效期(天)" prop="expireDays">
          <el-input-number v-model="adjustForm.expireDays" :min="1" :max="3650" placeholder="有效期" />
          <div class="form-help">积分的有效期，从当前时间开始计算</div>
        </el-form-item>
        <el-form-item label="调整原因" prop="description">
          <el-input
            v-model="adjustForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入调整原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="adjustDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAdjustForm" :loading="submitLoading">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { getUserPointsListByPage, adjustUserPoints, processExpiredPoints } from '@/api/points'
import { UserPoints, PointsAdjustDTO } from '@/types/points'

const router = useRouter()

// 列表数据相关
const pointsList = ref<UserPoints[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索条件
const searchForm = reactive({
  userId: '',
  username: '',
  minPoints: undefined as number | undefined,
  maxPoints: undefined as number | undefined
})

// 积分调整对话框相关
const adjustDialogVisible = ref(false)
const adjustFormRef = ref<FormInstance>()
const submitLoading = ref(false)
const currentUser = reactive<UserPoints>({} as UserPoints)

// 积分调整表单
const adjustForm = reactive<PointsAdjustDTO>({
  userId: 0,
  points: 0,
  description: '',
  sourceType: 7,
  expireDays: 365
})

// 积分调整表单验证规则
const adjustRules = reactive<FormRules>({
  userId: [
    { required: true, message: '用户ID不能为空', trigger: 'blur' }
  ],
  points: [
    { required: true, message: '积分值不能为空', trigger: 'blur' }
  ],
  sourceType: [
    { required: true, message: '请选择调整类型', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入调整原因', trigger: 'blur' },
    { min: 2, max: 200, message: '长度在 2 到 200 个字符', trigger: 'blur' }
  ]
})

// 计算积分总数
const totalUserPoints = computed(() => {
  return pointsList.value.reduce((sum, item) => sum + item.totalPoints, 0)
})

// 获取用户积分列表
const fetchPointsList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    
    const { data } = await getUserPointsListByPage(params)
    pointsList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取用户积分列表失败', error)
    ElMessage.error('获取用户积分列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchPointsList()
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.userId = ''
  searchForm.username = ''
  searchForm.minPoints = undefined
  searchForm.maxPoints = undefined
  currentPage.value = 1
  fetchPointsList()
}

// 调整用户积分
const handleAdjustPoints = (row: UserPoints) => {
  // 设置当前用户信息
  Object.assign(currentUser, row)
  
  // 初始化调整表单
  adjustForm.userId = row.userId
  adjustForm.points = 0
  adjustForm.description = ''
  adjustForm.sourceType = 7
  adjustForm.expireDays = 365
  
  adjustDialogVisible.value = true
}

// 提交积分调整表单
const submitAdjustForm = async () => {
  if (!adjustFormRef.value) return
  
  await adjustFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await adjustUserPoints(adjustForm)
        ElMessage.success('积分调整成功')
        adjustDialogVisible.value = false
        fetchPointsList()
      } catch (error) {
        console.error('积分调整失败', error)
        ElMessage.error('积分调整失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 查看用户积分记录
const viewPointRecords = (userId: number) => {
  router.push({
    name: 'userPointRecords',
    query: { userId }
  })
}

// 处理过期积分
const handleExpiredPoints = async () => {
  try {
    await ElMessageBox.confirm('确定要处理所有用户的过期积分吗？此操作不可恢复', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    const { data } = await processExpiredPoints()
    ElMessage.success(`成功处理 ${data} 个过期积分`)
    fetchPointsList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('处理过期积分失败', error)
      ElMessage.error('处理过期积分失败')
    }
  } finally {
    loading.value = false
  }
}

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchPointsList()
}

// 处理当前页变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchPointsList()
}

// 页面加载时获取数据
onMounted(() => {
  fetchPointsList()
})
</script>

<style scoped>
.user-points-container {
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

.header-info {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.points-range {
  width: 120px;
}

.highlight {
  font-weight: bold;
  color: #409EFF;
}

.success {
  font-weight: bold;
  color: #67C23A;
}

.warning {
  font-weight: bold;
  color: #E6A23C;
}

.danger {
  font-weight: bold;
  color: #F56C6C;
}

.form-help {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.negative-points ::v-deep(.el-input__inner) {
  color: #F56C6C;
}
</style> 