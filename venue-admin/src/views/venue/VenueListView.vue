<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageVenues, updateVenueStatus, deleteVenue, pageVenueTypes } from '@/api/venue'
import type { Venue, VenueType, PageResult } from '@/types/venue'

const router = useRouter()

// 定义查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  name: '',
  venueTypeId: undefined as number | undefined,
  status: undefined as number | undefined,
})

// 场馆类型列表
const venueTypeOptions = ref<VenueType[]>([])

// 数据列表和加载状态
const venueList = ref<Venue[]>([])
const total = ref(0)
const loading = ref(false)

// 获取场馆类型下拉选项
const getVenueTypeOptions = async () => {
  try {
    const res = await pageVenueTypes({ page: 1, size: 100 }) as { data: PageResult<VenueType> }
    venueTypeOptions.value = res.data.records
  } catch (error) {
    console.error('获取场馆类型列表失败', error)
  }
}

// 获取场馆列表
const getVenueList = async () => {
  loading.value = true
  try {
    const res = await pageVenues(queryParams) as { data: PageResult<Venue> }
    venueList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取场馆列表失败', error)
    ElMessage.error('获取场馆列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.page = 1
  getVenueList()
}

// 重置搜索
const resetQuery = () => {
  queryParams.name = ''
  queryParams.venueTypeId = undefined
  queryParams.status = undefined
  handleQuery()
}

// 处理分页变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  getVenueList()
}

const handleCurrentChange = (val: number) => {
  queryParams.page = val
  getVenueList()
}

// 跳转到添加页面
const handleAdd = () => {
  router.push('/home/venues/create')
}

// 跳转到编辑页面
const handleEdit = (id: number) => {
  router.push(`/home/venues/edit/${id}`)
}

// 跳转到详情页面
const handleDetail = (id: number) => {
  router.push(`/home/venues/${id}/detail`)
}

// 切换场馆状态
const handleStatusChange = async (row: Venue) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '开放' : '关闭'
  
  try {
    await updateVenueStatus(row.id, newStatus)
    ElMessage.success(`已${statusText}场馆`)
    row.status = newStatus // 直接更新本地状态
  } catch (error) {
    console.error('更新场馆状态失败', error)
    ElMessage.error('更新场馆状态失败')
    getVenueList() // 刷新列表
  }
}

// 查看场馆详情
const viewVenue = (id: number) => {
  router.push(`/home/venues/${id}/detail`)
}

// 编辑场馆
const editVenue = (id: number) => {
  router.push(`/home/venues/edit/${id}`)
}

// 管理场馆设施
const manageFacilities = (id: number) => {
  router.push(`/home/facilities?venueId=${id}`)
}

// 删除场馆
const handleDelete = (id: number) => {
  ElMessageBox.confirm('确认删除该场馆吗？此操作将一并删除关联的设施和位置信息。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteVenue(id)
      ElMessage.success('删除成功')
      getVenueList()
    } catch (error) {
      console.error('删除场馆失败', error)
      ElMessage.error('删除场馆失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 获取场馆类型名称
const getVenueTypeName = (typeId: number): string => {
  const type = venueTypeOptions.value.find(item => item.id === typeId)
  return type ? type.name : '未知类型'
}

// 状态标签类型
const getStatusType = (status: number): string => {
  return status === 1 ? 'success' : 'danger'
}

// 状态标签文本
const getStatusText = (status: number): string => {
  return status === 1 ? '开放' : '关闭'
}

// 初始化
onMounted(() => {
  getVenueTypeOptions()
  getVenueList()
})
</script>

<template>
  <div class="venue-container">
    <el-card class="venue-card">
      <template #header>
        <div class="card-header">
          <h2>场馆管理</h2>
          <el-button type="primary" @click="handleAdd">添加场馆</el-button>
        </div>
      </template>

      <el-alert
        type="info"
        title="管理提示"
        description="场馆需要添加设施才能被用户预约。每个场馆至少应该添加一个设施。"
        :closable="true"
        show-icon
        style="margin-bottom: 20px;"
      />

      <el-form :model="queryParams" inline>
        <el-form-item label="场馆名称">
          <el-input v-model="queryParams.name" placeholder="请输入场馆名称" clearable />
        </el-form-item>
        <el-form-item label="场馆类型">
          <el-select v-model="queryParams.venueTypeId" placeholder="请选择场馆类型" clearable>
            <el-option
              v-for="item in venueTypeOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="开放" :value="1" />
            <el-option label="关闭" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="venueList" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="场馆封面" width="120">
          <template #default="{ row }">
            <el-image 
              v-if="row.coverImage" 
              :src="row.coverImage" 
              fit="cover" 
              style="width: 80px; height: 60px; border-radius: 4px;"
              :preview-src-list="[row.coverImage]"
            />
            <div v-else class="no-image">暂无封面</div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="场馆名称" min-width="120" />
        <el-table-column label="场馆类型" width="120">
          <template #default="{ row }">
            <span>{{ getVenueTypeName(row.venueTypeId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="basePrice" label="基准价格" width="100">
          <template #default="{ row }">
            <span>{{ row.basePrice }} 元</span>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="容纳人数" width="100" />
        <el-table-column prop="address" label="场馆地址" min-width="180" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row.id)">查看</el-button>
            <el-button link type="primary" @click="handleEdit(row.id)">编辑</el-button>
            <el-button link type="primary" @click="manageFacilities(row.id)">管理设施</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.venue-container {
  padding: 20px;
}

.venue-card {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.no-image {
  width: 80px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f0f0;
  color: #909399;
  font-size: 12px;
  border-radius: 4px;
}
</style> 