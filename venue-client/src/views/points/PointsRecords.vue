<template>
  <div class="points-records-container">
    <div class="page-header">
      <h1>积分记录</h1>
      <div class="actions">
        <router-link to="/points">
          <el-button>返回积分概览</el-button>
        </router-link>
      </div>
    </div>

    <el-card class="filter-card">
      <div class="filter-form">
        <el-form :model="queryParams" inline>
          <el-form-item label="类型">
            <el-select v-model="queryParams.type" placeholder="选择类型" @change="handleQuery">
              <el-option label="全部" value="all"></el-option>
              <el-option label="获取" value="income"></el-option>
              <el-option label="支出" value="expense"></el-option>
              <el-option label="过期" value="expire"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              @change="handleDateChange"
            ></el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card class="records-card">
      <div class="records-list">
        <el-table
          :data="records"
          style="width: 100%"
          v-loading="loading"
          empty-text="暂无积分记录"
        >
          <el-table-column prop="createTime" label="时间" width="160">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="pointTypeName" label="类型" width="100">
            <template #default="scope">
              <el-tag :type="getTypeTag(scope.row.pointType)">
                {{ scope.row.pointTypeName }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="points" label="积分变动" width="100">
            <template #default="scope">
              <span :class="scope.row.points > 0 ? 'points-increase' : 'points-decrease'">
                {{ scope.row.points > 0 ? '+' : '' }}{{ scope.row.points }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="balance" label="积分余额" width="100"></el-table-column>
          <el-table-column prop="sourceTypeName" label="来源" width="120"></el-table-column>
          <el-table-column prop="description" label="描述"></el-table-column>
          <el-table-column prop="expireTime" label="过期时间" width="160">
            <template #default="scope">
              {{ scope.row.expireTime ? formatDate(scope.row.expireTime) : '永久有效' }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-container">
        <el-pagination
          v-if="total > 0"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :current-page="queryParams.page"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getUserPointRecords } from '@/api/points'
import { PointRecord, PointRecordQuery } from '@/types/points'
import { ElMessage } from 'element-plus'
import { formatDate as formatDateUtil } from '@/utils/format'

// 查询参数
const queryParams = reactive<PointRecordQuery>({
  type: 'all',
  page: 1,
  size: 10
})

const dateRange = ref<[Date, Date] | null>(null)
const records = ref<PointRecord[]>([])
const total = ref(0)
const loading = ref(false)

// 获取积分记录
const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await getUserPointRecords(queryParams)
    if (res.code === 200) {
      records.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取积分记录失败')
    }
  } catch (error) {
    console.error('获取积分记录出错:', error)
    ElMessage.error('获取积分记录出错')
  } finally {
    loading.value = false
  }
}

// 处理查询
const handleQuery = () => {
  queryParams.page = 1
  fetchRecords()
}

// 重置查询条件
const resetQuery = () => {
  queryParams.type = 'all'
  queryParams.startDate = undefined
  queryParams.endDate = undefined
  queryParams.page = 1
  dateRange.value = null
  fetchRecords()
}

// 处理日期变化
const handleDateChange = (dates: [Date, Date] | null) => {
  if (dates) {
    queryParams.startDate = formatDateUtil(dates[0], 'YYYY-MM-DD')
    queryParams.endDate = formatDateUtil(dates[1], 'YYYY-MM-DD')
  } else {
    queryParams.startDate = undefined
    queryParams.endDate = undefined
  }
}

// 处理每页条数变化
const handleSizeChange = (size: number) => {
  queryParams.size = size
  fetchRecords()
}

// 处理页码变化
const handleCurrentChange = (page: number) => {
  queryParams.page = page
  fetchRecords()
}

// 格式化日期
const formatDate = (dateStr: string) => {
  return formatDateUtil(dateStr, 'YYYY-MM-DD HH:mm')
}

// 获取积分类型对应的标签类型
const getTypeTag = (type: number) => {
  switch (type) {
    case 1: // 获取
      return 'success'
    case 2: // 使用
      return 'info'
    case 3: // 过期
      return 'danger'
    case 4: // 冻结
      return 'warning'
    case 5: // 解冻
      return 'success'
    default:
      return ''
  }
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.points-records-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.filter-form {
  padding: 10px 0;
}

.records-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.records-list {
  margin-bottom: 20px;
}

.pagination-container {
  text-align: right;
  padding: 10px 0;
}

.points-increase {
  color: #67c23a;
  font-weight: bold;
}

.points-decrease {
  color: #f56c6c;
  font-weight: bold;
}
</style> 