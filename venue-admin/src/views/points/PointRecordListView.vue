<template>
  <div class="point-record-container">
    <div class="header">
      <h2>积分记录管理</h2>
      <div class="header-actions">
        <el-button v-if="userId" type="primary" @click="backToUserPoints">返回用户积分</el-button>
      </div>
    </div>

    <!-- 搜索条件 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="积分类型">
          <el-select v-model="searchForm.pointType" placeholder="全部类型" clearable>
            <el-option :value="1" label="获取" />
            <el-option :value="2" label="使用" />
            <el-option :value="3" label="过期" />
            <el-option :value="4" label="冻结" />
            <el-option :value="5" label="解冻" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源类型">
          <el-select v-model="searchForm.sourceType" placeholder="全部来源" clearable>
            <el-option :value="1" label="预约" />
            <el-option :value="2" label="评价" />
            <el-option :value="3" label="签到" />
            <el-option :value="4" label="邀请" />
            <el-option :value="5" label="活动" />
            <el-option :value="6" label="积分扣减" />
            <el-option :value="7" label="手动调整" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
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

    <!-- 积分记录列表 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <div class="header-title">积分记录列表</div>
          <div class="header-info">
            <el-tag 
              v-if="totalIncome > 0" 
              type="success"
            >
              总获取: +{{ totalIncome }}
            </el-tag>
            <el-tag 
              v-if="totalExpense > 0" 
              type="danger"
            >
              总支出: -{{ totalExpense }}
            </el-tag>
          </div>
        </div>
      </template>

      <el-table
        :data="recordsList"
        style="width: 100%"
        v-loading="loading"
        border
      >
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column label="积分类型" width="100">
          <template #default="scope">
            <el-tag :type="getPointTypeTag(scope.row.pointType)">
              {{ scope.row.pointTypeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="积分变动" width="100">
          <template #default="scope">
            <span 
              :class="{
                'points-increase': scope.row.points > 0, 
                'points-decrease': scope.row.points < 0
              }"
            >
              {{ scope.row.points > 0 ? '+' : '' }}{{ scope.row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="积分余额" width="100" />
        <el-table-column label="来源类型" width="100">
          <template #default="scope">
            <el-tag :type="getSourceTypeTag(scope.row.sourceType)" size="small">
              {{ scope.row.sourceTypeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sourceId" label="来源ID" width="100" />
        <el-table-column prop="description" label="描述" min-width="180" />
        <el-table-column label="过期时间" width="180">
          <template #default="scope">
            <span v-if="scope.row.expireTime">{{ scope.row.expireTime }}</span>
            <span v-else class="no-expire">永久有效</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" sortable />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserPointRecordsByPage } from '@/api/points'
import { PointRecord } from '@/types/points'

const route = useRoute()
const router = useRouter()

// 列表数据相关
const recordsList = ref<PointRecord[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dateRange = ref(null)
const userId = ref<string | null>(null)

// 搜索条件
const searchForm = reactive({
  userId: '',
  pointType: '',
  sourceType: '',
  startDate: '',
  endDate: ''
})

// 计算总获取和总支出
const totalIncome = computed(() => {
  return recordsList.value
    .filter(record => record.points > 0)
    .reduce((sum, record) => sum + record.points, 0)
})

const totalExpense = computed(() => {
  return Math.abs(recordsList.value
    .filter(record => record.points < 0)
    .reduce((sum, record) => sum + record.points, 0))
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

// 获取积分记录列表
const fetchRecordsList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    
    const { data } = await getUserPointRecordsByPage(params)
    recordsList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取积分记录列表失败', error)
    ElMessage.error('获取积分记录列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchRecordsList()
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.userId = userId.value || ''
  searchForm.pointType = ''
  searchForm.sourceType = ''
  searchForm.startDate = ''
  searchForm.endDate = ''
  dateRange.value = null
  currentPage.value = 1
  fetchRecordsList()
}

// 返回用户积分页面
const backToUserPoints = () => {
  router.push({ name: 'userPoints' })
}

// 获取积分类型标签类型
const getPointTypeTag = (type: number) => {
  const map: Record<number, string> = {
    1: 'success',
    2: 'info',
    3: 'danger',
    4: 'warning',
    5: 'success'
  }
  return map[type] || ''
}

// 获取来源类型标签类型
const getSourceTypeTag = (type: number) => {
  const map: Record<number, string> = {
    1: 'success',
    2: 'info',
    3: 'warning',
    4: 'danger',
    5: 'success',
    6: 'info',
    7: ''
  }
  return map[type] || ''
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

// 页面初始化
onMounted(() => {
  // 检查是否有用户ID参数
  if (route.query.userId) {
    userId.value = route.query.userId as string
    searchForm.userId = userId.value
  }
  
  fetchRecordsList()
})
</script>

<style scoped>
.point-record-container {
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

.points-increase {
  font-weight: bold;
  color: #67C23A;
}

.points-decrease {
  font-weight: bold;
  color: #F56C6C;
}

.no-expire {
  color: #909399;
  font-style: italic;
}
</style> 