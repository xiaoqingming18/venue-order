<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
// 使用动态导入方式避免类型错误
import {
  ArrowUp,
  ArrowDown,
  User,
  Calendar,
  Money,
  TrendCharts,
  More,
  Download,
  Refresh,
  Edit,
  Delete,
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

// 各种图表的引用
const visitChartRef = ref<HTMLElement | null>(null)
const bookingChartRef = ref<HTMLElement | null>(null)

// 初始化图表
onMounted(async () => {
  await initVisitChart()
  await initBookingChart()
})

// 初始化访问统计图表
const initVisitChart = async () => {
  if (!visitChartRef.value) return

  try {
    // 动态导入echarts
    const echarts = await import('echarts')
    const chart = echarts.init(visitChartRef.value)

    const option = {
      tooltip: {
        trigger: 'axis',
      },
      legend: {
        data: ['访问量', '注册用户', '预约数'],
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月'],
      },
      yAxis: {
        type: 'value',
      },
      series: [
        {
          name: '访问量',
          type: 'line',
          data: [150, 230, 224, 218, 335, 400, 450],
          smooth: true,
          lineStyle: {
            width: 3,
            color: '#409EFF',
          },
        },
        {
          name: '注册用户',
          type: 'line',
          data: [50, 100, 150, 180, 220, 300, 340],
          smooth: true,
          lineStyle: {
            width: 3,
            color: '#67C23A',
          },
        },
        {
          name: '预约数',
          type: 'line',
          data: [80, 130, 180, 240, 260, 340, 386],
          smooth: true,
          lineStyle: {
            width: 3,
            color: '#E6A23C',
          },
        },
      ],
    }

    chart.setOption(option)

    // 窗口大小变化时重新绘制图表
    window.addEventListener('resize', () => {
      chart.resize()
    })
  } catch (error) {
    console.error('加载访问统计图表失败', error)
  }
}

// 初始化预约分析图表（饼图）
const initBookingChart = async () => {
  if (!bookingChartRef.value) return

  try {
    // 动态导入echarts
    const echarts = await import('echarts')
    const chart = echarts.init(bookingChartRef.value)

    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)',
      },
      legend: {
        orient: 'vertical',
        left: 10,
        data: ['篮球馆', '足球场', '游泳馆', '羽毛球馆', '健身房'],
      },
      series: [
        {
          name: '预约分布',
          type: 'pie',
          radius: ['50%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2,
          },
          label: {
            show: false,
            position: 'center',
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '18',
              fontWeight: 'bold',
            },
          },
          labelLine: {
            show: false,
          },
          data: [
            { value: 335, name: '篮球馆' },
            { value: 210, name: '足球场' },
            { value: 184, name: '游泳馆' },
            { value: 154, name: '羽毛球馆' },
            { value: 130, name: '健身房' },
          ],
        },
      ],
    }

    chart.setOption(option)

    // 窗口大小变化时重新绘制图表
    window.addEventListener('resize', () => {
      chart.resize()
    })
  } catch (error) {
    console.error('加载预约分析图表失败', error)
  }
}

// 预约状态类型
const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    已确认: 'success',
    待付款: 'warning',
    待确认: 'info',
    已取消: 'danger',
  }
  return map[status] || ''
}

// 用户状态类型
const getUserStatusType = (status: string) => {
  const map: Record<string, string> = {
    已激活: 'success',
    待验证: 'warning',
    已禁用: 'danger',
  }
  return map[status] || ''
}

// 导出报表
const exportReport = () => {
  ElMessage.info('报表导出功能即将上线')
}

// 刷新数据
const refreshData = () => {
  ElMessage.success('数据已刷新')
  // 实际项目中，这里应该重新获取数据
}

// 前往管理员面板
const goToAdminPanel = () => {
  router.push('/admin')
}
</script>

<template>
  <div class="dashboard-container">
    <!-- 页面标题 -->
    <div class="page-title">
      <h1>控制台</h1>
      <div class="page-actions">
        <el-button type="info" plain @click="exportReport">
          <el-icon><Download /></el-icon>导出报表
        </el-button>
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>刷新数据
        </el-button>
      </div>
    </div>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :xs="24" :sm="12" :md="6" class="mb-3">
        <div class="stats-card">
          <div class="stats-icon primary">
            <el-icon><User /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value">2,845</div>
            <div class="stats-label">总注册用户</div>
            <div class="stats-change positive">
              <el-icon><ArrowUp /></el-icon>
              <span>较上月增长12.5%</span>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6" class="mb-3">
        <div class="stats-card">
          <div class="stats-icon success">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value">486</div>
            <div class="stats-label">本月预约数</div>
            <div class="stats-change positive">
              <el-icon><ArrowUp /></el-icon>
              <span>较上月增长8.2%</span>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6" class="mb-3">
        <div class="stats-card">
          <div class="stats-icon warning">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value">¥25,678</div>
            <div class="stats-label">本月收入</div>
            <div class="stats-change positive">
              <el-icon><ArrowUp /></el-icon>
              <span>较上月增长5.3%</span>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6" class="mb-3">
        <div class="stats-card">
          <div class="stats-icon danger">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value">78.5%</div>
            <div class="stats-label">场馆利用率</div>
            <div class="stats-change negative">
              <el-icon><ArrowDown /></el-icon>
              <span>较上月下降2.1%</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表与表格 -->
    <el-row :gutter="20">
      <!-- 访问统计图表 -->
      <el-col :lg="16" :md="24" class="mb-4">
        <el-card>
          <template #header>
            <div class="card-header">
              <h2>访问统计</h2>
              <el-dropdown>
                <el-button type="text">
                  <el-icon><More /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>按周查看</el-dropdown-item>
                    <el-dropdown-item>按月查看</el-dropdown-item>
                    <el-dropdown-item>按年查看</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
          <div ref="visitChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>

      <!-- 场馆预约分析 -->
      <el-col :lg="8" :md="24" class="mb-4">
        <el-card>
          <template #header>
            <div class="card-header">
              <h2>预约分析</h2>
              <el-dropdown>
                <el-button type="text">
                  <el-icon><More /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>按场馆类型</el-dropdown-item>
                    <el-dropdown-item>按时间段</el-dropdown-item>
                    <el-dropdown-item>按用户群体</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
          <div ref="bookingChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 最近预约 -->
      <el-col :lg="12" :md="24" class="mb-4">
        <el-card>
          <template #header>
            <div class="card-header">
              <h2>最近预约</h2>
              <el-button type="primary" plain size="small">查看全部</el-button>
            </div>
          </template>
          <el-table
            :data="[
              {
                id: 'BK2305',
                user: '张三',
                venue: '篮球馆A',
                time: '2023-05-15 14:00',
                status: '已确认',
              },
              {
                id: 'BK2304',
                user: '李四',
                venue: '羽毛球馆B',
                time: '2023-05-15 16:30',
                status: '已确认',
              },
              {
                id: 'BK2303',
                user: '王五',
                venue: '足球场',
                time: '2023-05-16 09:00',
                status: '待付款',
              },
              {
                id: 'BK2302',
                user: '赵六',
                venue: '游泳馆',
                time: '2023-05-16 10:30',
                status: '待确认',
              },
              {
                id: 'BK2301',
                user: '孙七',
                venue: '健身房A',
                time: '2023-05-17 08:00',
                status: '已取消',
              },
            ]"
            style="width: 100%"
          >
            <el-table-column prop="id" label="预约ID" width="100" />
            <el-table-column prop="user" label="用户" width="100" />
            <el-table-column prop="venue" label="场馆" />
            <el-table-column prop="time" label="预约时间" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 最新用户 -->
      <el-col :lg="12" :md="24" class="mb-4">
        <el-card>
          <template #header>
            <div class="card-header">
              <h2>最新用户</h2>
              <el-button type="primary" plain size="small">查看全部</el-button>
            </div>
          </template>
          <el-table
            :data="[
              { id: 'U1024', name: '周婷', registerTime: '2023-05-15 10:24', status: '已激活' },
              { id: 'U1023', name: '吴磊', registerTime: '2023-05-15 09:18', status: '已激活' },
              { id: 'U1022', name: '郑宇', registerTime: '2023-05-14 18:45', status: '待验证' },
              { id: 'U1021', name: '陈丽', registerTime: '2023-05-14 15:30', status: '已激活' },
              { id: 'U1020', name: '杨坤', registerTime: '2023-05-14 12:10', status: '已禁用' },
            ]"
            style="width: 100%"
          >
            <el-table-column prop="id" label="用户ID" width="100" />
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="registerTime" label="注册时间" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="getUserStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default>
                <el-button size="small" type="primary" plain
                  ><el-icon><Edit /></el-icon
                ></el-button>
                <el-button size="small" type="danger" plain
                  ><el-icon><Delete /></el-icon
                ></el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 待处理事项 -->
      <el-col :md="12" :sm="24" class="mb-4">
        <el-card>
          <template #header>
            <div class="card-header">
              <h2>待处理事项</h2>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="待审核场馆">
              <el-badge value="3" />
            </el-descriptions-item>
            <el-descriptions-item label="待确认预约">
              <el-badge value="8" />
            </el-descriptions-item>
            <el-descriptions-item label="待处理退款">
              <el-badge value="2" />
            </el-descriptions-item>
            <el-descriptions-item label="用户投诉">
              <el-badge value="5" />
            </el-descriptions-item>
            <el-descriptions-item label="系统通知">
              <el-badge value="12" />
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 系统公告 -->
      <el-col :md="12" :sm="24" class="mb-4">
        <el-card>
          <template #header>
            <div class="card-header">
              <h2>系统公告</h2>
              <el-button type="primary" plain size="small">发布公告</el-button>
            </div>
          </template>
          <div class="announcements">
            <el-timeline>
              <el-timeline-item timestamp="2023-05-10" placement="top" type="primary">
                <div class="announcement-item">
                  <h3>系统维护通知</h3>
                  <p>
                    系统将于2023年5月20日凌晨2:00-4:00进行例行维护，期间系统将暂停服务，给您带来的不便敬请谅解。
                  </p>
                </div>
              </el-timeline-item>

              <el-timeline-item timestamp="2023-05-05" placement="top" type="success">
                <div class="announcement-item">
                  <h3>新功能上线通知</h3>
                  <p>
                    场馆预约系统已上线手机短信通知功能，用户预约成功或取消后将收到手机短信提醒，提高用户体验。
                  </p>
                </div>
              </el-timeline-item>

              <el-timeline-item timestamp="2023-05-01" placement="top" type="warning">
                <div class="announcement-item">
                  <h3>优惠活动公告</h3>
                  <p>
                    五一劳动节期间（5月1日至5月5日），所有场馆预约享8折优惠，欢迎广大用户积极预约参与体育活动。
                  </p>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard-container {
  width: 100%;
  min-height: 100vh;
  padding: 20px;
  background-color: #f3f4f6;
}

.page-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title h1 {
  font-size: 1.75rem;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.page-actions {
  display: flex;
  gap: 10px;
}

.mb-4 {
  margin-bottom: 20px;
}

.mb-3 {
  margin-bottom: 15px;
}

.stats-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  height: 100%;
}

.stats-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 56px;
  height: 56px;
  border-radius: 8px;
  font-size: 24px;
  margin-right: 16px;
}

.stats-icon.primary {
  background-color: rgba(64, 158, 255, 0.1);
  color: #409eff;
}

.stats-icon.success {
  background-color: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.stats-icon.warning {
  background-color: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
}

.stats-icon.danger {
  background-color: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.stats-info {
  flex: 1;
}

.stats-value {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #303133;
}

.stats-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.stats-change {
  display: flex;
  align-items: center;
  font-size: 12px;
}

.stats-change.positive {
  color: #67c23a;
}

.stats-change.negative {
  color: #f56c6c;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0;
  color: #303133;
}

.announcement-item h3 {
  font-size: 1rem;
  margin: 0 0 8px 0;
  color: #303133;
  font-weight: 600;
}

.announcement-item p {
  font-size: 0.9rem;
  color: #606266;
  margin: 0;
  line-height: 1.5;
}
</style>
