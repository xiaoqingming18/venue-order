<template>
  <div class="points-stats-container">
    <div class="header">
      <h2>积分数据分析</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :shortcuts="shortcuts"
          value-format="YYYY-MM-DD"
          @change="handleDateChange"
        />
        <el-button type="primary" @click="refreshData">刷新数据</el-button>
      </div>
    </div>

    <!-- 积分总体情况 -->
    <el-row :gutter="20" v-loading="overviewLoading">
      <el-col :span="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-title">总用户数</div>
          <div class="overview-value">{{ overview.totalUsers }}</div>
          <div class="overview-icon">
            <i class="el-icon-user"></i>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-title">总积分数</div>
          <div class="overview-value">{{ overview.totalPoints }}</div>
          <div class="overview-icon blue">
            <i class="el-icon-coin"></i>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-title">可用积分</div>
          <div class="overview-value">{{ overview.availablePoints }}</div>
          <div class="overview-icon green">
            <i class="el-icon-shopping-cart-full"></i>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-title">过期积分</div>
          <div class="overview-value">{{ overview.expiredPoints }}</div>
          <div class="overview-icon red">
            <i class="el-icon-time"></i>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 月度和年度积分统计 -->
    <el-row :gutter="20" v-loading="overviewLoading">
      <el-col :span="12">
        <el-card class="stats-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>月度积分统计</span>
            </div>
          </template>
          <div class="card-content">
            <div class="stats-item">
              <div class="stats-label">月度获取积分</div>
              <div class="stats-value increase">+{{ overview.monthlyIncome }}</div>
            </div>
            <div class="stats-item">
              <div class="stats-label">月度使用积分</div>
              <div class="stats-value decrease">-{{ overview.monthlyUsage }}</div>
            </div>
            <div class="stats-item">
              <div class="stats-label">月度积分净增长</div>
              <div class="stats-value" :class="{ 'increase': netMonthlyGrowth >= 0, 'decrease': netMonthlyGrowth < 0 }">
                {{ netMonthlyGrowth >= 0 ? '+' : '' }}{{ netMonthlyGrowth }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="stats-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>年度积分统计</span>
            </div>
          </template>
          <div class="card-content">
            <div class="stats-item">
              <div class="stats-label">年度获取积分</div>
              <div class="stats-value increase">+{{ overview.yearlyIncome }}</div>
            </div>
            <div class="stats-item">
              <div class="stats-label">年度使用积分</div>
              <div class="stats-value decrease">-{{ overview.yearlyUsage }}</div>
            </div>
            <div class="stats-item">
              <div class="stats-label">年度积分净增长</div>
              <div class="stats-value" :class="{ 'increase': netYearlyGrowth >= 0, 'decrease': netYearlyGrowth < 0 }">
                {{ netYearlyGrowth >= 0 ? '+' : '' }}{{ netYearlyGrowth }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 积分来源和使用分析 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover" v-loading="sourceLoading">
          <template #header>
            <div class="card-header">
              <span>积分来源分析</span>
            </div>
          </template>
          <div class="chart-container" ref="sourceChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover" v-loading="usageLoading">
          <template #header>
            <div class="card-header">
              <span>积分使用分析</span>
            </div>
          </template>
          <div class="chart-container" ref="usageChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 积分流通情况 -->
    <el-row>
      <el-col :span="24">
        <el-card class="chart-card" shadow="hover" v-loading="flowLoading">
          <template #header>
            <div class="card-header">
              <span>积分流通趋势</span>
            </div>
          </template>
          <div class="chart-container large" ref="flowChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 会员等级分布 -->
    <el-row>
      <el-col :span="24">
        <el-card class="chart-card" shadow="hover" v-loading="levelLoading">
          <template #header>
            <div class="card-header">
              <span>会员等级分布</span>
            </div>
          </template>
          <div class="chart-container" ref="levelChartRef"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts/core'
import { 
  TitleComponent, 
  TooltipComponent, 
  LegendComponent,
  GridComponent
} from 'echarts/components'
import { 
  BarChart, 
  LineChart, 
  PieChart 
} from 'echarts/charts'
import { UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'
import { 
  getPointsSourceAnalysis, 
  getPointsUsageAnalysis, 
  getPointsFlowStats, 
  getMemberLevelDistribution 
} from '@/api/points'
import { 
  PointsStatistics, 
  PointsSourceAnalysis, 
  PointsUsageAnalysis, 
  PointsFlowStats, 
  MemberLevelDistribution 
} from '@/types/points'

// 注册 ECharts 必须的组件
echarts.use([
  TitleComponent, 
  TooltipComponent, 
  LegendComponent,
  GridComponent,
  BarChart, 
  LineChart, 
  PieChart,
  CanvasRenderer,
  UniversalTransition
])

// 图表实例引用
const sourceChartRef = ref<HTMLElement | null>(null)
const usageChartRef = ref<HTMLElement | null>(null)
const flowChartRef = ref<HTMLElement | null>(null)
const levelChartRef = ref<HTMLElement | null>(null)

// 图表实例
let sourceChart: echarts.ECharts | null = null
let usageChart: echarts.ECharts | null = null
let flowChart: echarts.ECharts | null = null
let levelChart: echarts.ECharts | null = null

// 加载状态
const overviewLoading = ref(false)
const sourceLoading = ref(false)
const usageLoading = ref(false)
const flowLoading = ref(false)
const levelLoading = ref(false)

// 时间范围
const dateRange = ref(null)
const shortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    },
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    },
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    },
  },
]

// 查询参数
const queryParams = reactive({
  startDate: '',
  endDate: ''
})

// 数据
const overview = reactive<PointsStatistics>({
  totalUsers: 0,
  totalPoints: 0,
  availablePoints: 0,
  frozenPoints: 0,
  expiredPoints: 0,
  monthlyIncome: 0,
  monthlyUsage: 0,
  yearlyIncome: 0,
  yearlyUsage: 0
})

const sourceData = ref<PointsSourceAnalysis[]>([])
const usageData = ref<PointsUsageAnalysis[]>([])
const flowData = ref<PointsFlowStats[]>([])
const levelData = ref<MemberLevelDistribution[]>([])

// 计算属性
const netMonthlyGrowth = computed(() => overview.monthlyIncome - overview.monthlyUsage)
const netYearlyGrowth = computed(() => overview.yearlyIncome - overview.yearlyUsage)

// 处理日期变化
const handleDateChange = (val: any) => {
  if (val) {
    queryParams.startDate = val[0]
    queryParams.endDate = val[1]
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
  
  // 刷新数据
  refreshData()
}

// 刷新所有数据
const refreshData = () => {
  fetchSourceAnalysis()
  fetchUsageAnalysis()
  fetchFlowStats()
  fetchLevelDistribution()
}

// 获取积分来源分析数据
const fetchSourceAnalysis = async () => {
  sourceLoading.value = true
  try {
    const { data } = await getPointsSourceAnalysis(queryParams)
    sourceData.value = data
    renderSourceChart()
  } catch (error) {
    console.error('获取积分来源分析数据失败', error)
    ElMessage.error('获取积分来源分析数据失败')
  } finally {
    sourceLoading.value = false
  }
}

// 获取积分使用分析数据
const fetchUsageAnalysis = async () => {
  usageLoading.value = true
  try {
    const { data } = await getPointsUsageAnalysis(queryParams)
    usageData.value = data
    renderUsageChart()
  } catch (error) {
    console.error('获取积分使用分析数据失败', error)
    ElMessage.error('获取积分使用分析数据失败')
  } finally {
    usageLoading.value = false
  }
}

// 获取积分流通情况数据
const fetchFlowStats = async () => {
  flowLoading.value = true
  try {
    const { data } = await getPointsFlowStats(queryParams)
    flowData.value = data
    renderFlowChart()
  } catch (error) {
    console.error('获取积分流通情况数据失败', error)
    ElMessage.error('获取积分流通情况数据失败')
  } finally {
    flowLoading.value = false
  }
}

// 获取会员等级分布数据
const fetchLevelDistribution = async () => {
  levelLoading.value = true
  try {
    const { data } = await getMemberLevelDistribution()
    levelData.value = data
    renderLevelChart()
  } catch (error) {
    console.error('获取会员等级分布数据失败', error)
    ElMessage.error('获取会员等级分布数据失败')
  } finally {
    levelLoading.value = false
  }
}

// 渲染积分来源分析图表
const renderSourceChart = () => {
  if (!sourceChartRef.value) return
  
  if (!sourceChart) {
    sourceChart = echarts.init(sourceChartRef.value)
  }
  
  // 准备数据
  const sourceNames = sourceData.value.map(item => item.sourceTypeName)
  const sourceValues = sourceData.value.map(item => ({
    value: item.totalPoints,
    name: item.sourceTypeName
  }))
  
  const option = {
    title: {
      text: '积分来源分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: sourceNames
    },
    series: [
      {
        name: '积分来源',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: sourceValues
      }
    ]
  }
  
  sourceChart.setOption(option)
}

// 渲染积分使用分析图表
const renderUsageChart = () => {
  if (!usageChartRef.value) return
  
  if (!usageChart) {
    usageChart = echarts.init(usageChartRef.value)
  }
  
  // 准备数据
  const usageNames = usageData.value.map(item => item.usageTypeName)
  const usageValues = usageData.value.map(item => ({
    value: item.totalPoints,
    name: item.usageTypeName
  }))
  
  const option = {
    title: {
      text: '积分使用分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 'right',
      data: usageNames
    },
    series: [
      {
        name: '积分使用',
        type: 'pie',
        radius: '55%',
        center: ['40%', '50%'],
        data: usageValues,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  usageChart.setOption(option)
}

// 渲染积分流通情况图表
const renderFlowChart = () => {
  if (!flowChartRef.value) return
  
  if (!flowChart) {
    flowChart = echarts.init(flowChartRef.value)
  }
  
  // 准备数据
  const dates = flowData.value.map(item => item.date)
  const incomeData = flowData.value.map(item => item.income)
  const usageData = flowData.value.map(item => item.usage)
  const expireData = flowData.value.map(item => item.expire)
  
  const option = {
    title: {
      text: '积分流通趋势图',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    legend: {
      data: ['积分获取', '积分使用', '积分过期'],
      bottom: '0%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data: dates
      }
    ],
    yAxis: [
      {
        type: 'value'
      }
    ],
    series: [
      {
        name: '积分获取',
        type: 'line',
        stack: 'Total',
        areaStyle: {},
        emphasis: {
          focus: 'series'
        },
        data: incomeData
      },
      {
        name: '积分使用',
        type: 'line',
        stack: 'Total',
        areaStyle: {},
        emphasis: {
          focus: 'series'
        },
        data: usageData
      },
      {
        name: '积分过期',
        type: 'line',
        stack: 'Total',
        areaStyle: {},
        emphasis: {
          focus: 'series'
        },
        data: expireData
      }
    ]
  }
  
  flowChart.setOption(option)
}

// 渲染会员等级分布图表
const renderLevelChart = () => {
  if (!levelChartRef.value) return
  
  if (!levelChart) {
    levelChart = echarts.init(levelChartRef.value)
  }
  
  // 准备数据
  const levelNames = levelData.value.map(item => item.levelName)
  const userCounts = levelData.value.map(item => item.userCount)
  
  const option = {
    title: {
      text: '会员等级分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        data: levelNames,
        axisTick: {
          alignWithLabel: true
        }
      }
    ],
    yAxis: [
      {
        type: 'value'
      }
    ],
    series: [
      {
        name: '用户数',
        type: 'bar',
        barWidth: '60%',
        data: userCounts
      }
    ]
  }
  
  levelChart.setOption(option)
}

// 响应窗口大小变化
const handleResize = () => {
  sourceChart?.resize()
  usageChart?.resize()
  flowChart?.resize()
  levelChart?.resize()
}

// 初始化
onMounted(() => {
  // 获取数据
  refreshData()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

// 清理
onBeforeUnmount(() => {
  // 移除窗口大小变化监听
  window.removeEventListener('resize', handleResize)
  
  // 销毁图表实例
  sourceChart?.dispose()
  usageChart?.dispose()
  flowChart?.dispose()
  levelChart?.dispose()
})
</script>

<style scoped>
.points-stats-container {
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

/* 布局样式 */
.el-row {
  margin-bottom: 20px;
}

/* 统计卡片样式 */
.overview-card {
  height: 120px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.overview-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.overview-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
}

.overview-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.overview-icon {
  position: absolute;
  top: 20px;
  right: 20px;
  font-size: 48px;
  opacity: 0.2;
  color: #409EFF;
}

.overview-icon.blue {
  color: #409EFF;
}

.overview-icon.green {
  color: #67C23A;
}

.overview-icon.red {
  color: #F56C6C;
}

/* 统计卡片样式 */
.stats-card {
  height: 220px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  padding: 20px 0;
}

.stats-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 20px;
}

.stats-label {
  font-size: 14px;
  color: #606266;
}

.stats-value {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.stats-value.increase {
  color: #67C23A;
}

.stats-value.decrease {
  color: #F56C6C;
}

/* 图表卡片样式 */
.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 400px;
  width: 100%;
}

.chart-container.large {
  height: 500px;
}
</style> 