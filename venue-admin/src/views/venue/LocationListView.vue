<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageLocations, deleteLocation, updateLocationStatus, pageVenues, createLocation, updateLocation } from '@/api/venue'
import type { VenueLocation, Venue, PageResult } from '@/types/venue'

// 定义查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  venueId: undefined as number | undefined,
  type: '',
  status: undefined as number | undefined,
})

// 场馆选项列表
const venueOptions = ref<Venue[]>([])

// 数据列表和加载状态
const locationList = ref<VenueLocation[]>([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加场馆位置')

// 表单数据
const form = reactive({
  id: undefined as number | undefined,
  venueId: undefined as number | undefined,
  name: '',
  type: '',
  status: 1,
  price: undefined as number | undefined,
  description: '',
})

// 表单规则
const rules = {
  venueId: [{ required: true, message: '请选择场馆', trigger: 'change' }],
  name: [{ required: true, message: '请输入位置名称', trigger: 'blur' }],
  type: [{ required: true, message: '请输入位置类型', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

// 表单引用
const formRef = ref()

// 获取场馆下拉选项
const getVenueOptions = async () => {
  try {
    const res = await pageVenues({ page: 1, size: 100 }) as { data: PageResult<Venue> }
    venueOptions.value = res.data.records
  } catch (error) {
    console.error('获取场馆列表失败', error)
  }
}

// 获取场馆位置列表
const getLocationList = async () => {
  if (!queryParams.venueId) {
    locationList.value = []
    total.value = 0
    return
  }

  loading.value = true
  try {
    const params = {
      ...queryParams,
      venueId: queryParams.venueId as number // 确保venueId是number类型
    }
    const res = await pageLocations(params) as { data: PageResult<VenueLocation> }
    locationList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取场馆位置列表失败', error)
    ElMessage.error('获取场馆位置列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.page = 1
  getLocationList()
}

// 重置搜索
const resetQuery = () => {
  queryParams.type = ''
  queryParams.status = undefined
  handleQuery()
}

// 打开添加对话框
const handleAdd = () => {
  dialogTitle.value = '添加场馆位置'
  form.id = undefined
  form.venueId = queryParams.venueId
  form.name = ''
  form.type = ''
  form.status = 1
  form.price = undefined
  form.description = ''
  dialogVisible.value = true
}

// 打开编辑对话框
const handleEdit = (row: VenueLocation) => {
  dialogTitle.value = '编辑场馆位置'
  form.id = row.id
  form.venueId = row.venueId
  form.name = row.name
  form.type = row.type
  form.status = row.status
  form.price = row.price
  form.description = row.description
  dialogVisible.value = true
}

// 处理分页变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  getLocationList()
}

const handleCurrentChange = (val: number) => {
  queryParams.page = val
  getLocationList()
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (form.id) {
          // 更新场馆位置
          await updateLocation(form.id, {
            venueId: form.venueId,
            name: form.name,
            type: form.type,
            status: form.status,
            price: form.price,
            description: form.description
          })
        } else {
          // 创建场馆位置
          await createLocation({
            venueId: form.venueId,
            name: form.name,
            type: form.type,
            status: form.status,
            price: form.price,
            description: form.description
          })
        }
        ElMessage.success(form.id ? '更新成功' : '添加成功')
        dialogVisible.value = false
        getLocationList()
      } catch (error) {
        console.error('保存场馆位置失败', error)
        ElMessage.error(form.id ? '更新失败' : '添加失败')
      }
    }
  })
}

// 切换位置状态
const handleStatusChange = async (row: VenueLocation) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '可用' : '不可用'
  
  try {
    await updateLocationStatus(row.id, newStatus)
    ElMessage.success(`已设置为${statusText}`)
    row.status = newStatus // 直接更新本地状态
  } catch (error) {
    console.error('更新位置状态失败', error)
    ElMessage.error('更新位置状态失败')
    getLocationList() // 刷新列表
  }
}

// 删除场馆位置
const handleDelete = (id: number) => {
  ElMessageBox.confirm('确认删除该场馆位置吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteLocation(id)
      ElMessage.success('删除成功')
      getLocationList()
    } catch (error) {
      console.error('删除场馆位置失败', error)
      ElMessage.error('删除场馆位置失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 获取场馆名称
const getVenueName = (venueId: number): string => {
  const venue = venueOptions.value.find(item => item.id === venueId)
  return venue ? venue.name : '未知场馆'
}

// 状态标签类型
const getStatusType = (status: number): string => {
  return status === 1 ? 'success' : 'danger'
}

// 状态标签文本
const getStatusText = (status: number): string => {
  return status === 1 ? '可用' : '不可用'
}

// 初始化
onMounted(() => {
  getVenueOptions()
  
  // 如果URL参数中有venueId，则自动选择该场馆
  const urlParams = new URLSearchParams(window.location.search)
  const venueIdParam = urlParams.get('venueId')
  
  if (venueIdParam) {
    const venueId = parseInt(venueIdParam)
    if (!isNaN(venueId)) {
      queryParams.venueId = venueId
      setTimeout(() => {
        handleQuery()
      }, 500) // 等待场馆选项加载完成
    }
  }
})
</script>

<template>
  <div class="location-container">
    <el-card class="location-card">
      <template #header>
        <div class="card-header">
          <h2>场馆位置管理</h2>
          <div class="header-btns">
            <el-alert
              v-if="!queryParams.venueId"
              type="info"
              title="请先选择场馆，然后添加位置"
              :closable="false"
              show-icon
              style="margin-right: 10px;"
            />
            <el-button type="primary" @click="handleAdd" :disabled="!queryParams.venueId">添加位置</el-button>
          </div>
        </div>
      </template>

      <el-form :model="queryParams" inline>
        <el-form-item label="选择场馆" required>
          <el-select v-model="queryParams.venueId" placeholder="请选择场馆" clearable @change="handleQuery">
            <el-option
              v-for="item in venueOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="位置类型">
          <el-input v-model="queryParams.type" placeholder="请输入位置类型" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="可用" :value="1" />
            <el-option label="不可用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :disabled="!queryParams.venueId">搜索</el-button>
          <el-button @click="resetQuery" :disabled="!queryParams.venueId">重置</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="locationList.length === 0 && queryParams.venueId"
        type="warning"
        title="该场馆暂无位置信息，建议添加位置以便用户了解场馆布局！"
        description="位置信息可以帮助用户更好地了解场馆的布局和环境。"
        :closable="true"
        show-icon
        style="margin-bottom: 20px;"
      />

      <el-table 
        v-else
        v-loading="loading" 
        :data="locationList" 
        style="width: 100%" 
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="场馆名称" width="180">
          <template #default="{ row }">
            <span>{{ getVenueName(row.venueId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="位置名称" min-width="120" />
        <el-table-column prop="type" label="位置类型" width="120" />
        <el-table-column prop="price" label="位置价格" width="120">
          <template #default="{ row }">
            <span v-if="row.price">{{ row.price }} 元</span>
            <el-tag v-else type="info" size="small">使用场馆基准价</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="位置描述" min-width="180" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" v-if="row.status === 0" @click="handleStatusChange(row)">启用</el-button>
            <el-button link type="warning" v-else @click="handleStatusChange(row)">禁用</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" v-if="queryParams.venueId && total > 0">
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

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="场馆" prop="venueId">
          <el-select v-model="form.venueId" placeholder="请选择场馆" style="width: 100%">
            <el-option
              v-for="item in venueOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="位置名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入位置名称，如：A区1号" />
        </el-form-item>
        <el-form-item label="位置类型" prop="type">
          <el-input v-model="form.type" placeholder="请输入位置类型，如：座位、场地等" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">可用</el-radio>
            <el-radio :label="0">不可用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="位置价格">
          <el-input-number 
            v-model="form.price" 
            placeholder="请输入位置价格，留空表示使用场馆基准价格" 
            :min="0.01" 
            :precision="2" 
            :step="10"
            style="width: 100%"
          />
          <div class="form-tip">留空表示使用场馆基准价格</div>
        </el-form-item>
        <el-form-item label="位置描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            placeholder="请输入位置描述" 
            :rows="4"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.location-container {
  padding: 20px;
}

.location-card {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-btns {
  display: flex;
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

.dialog-footer {
  text-align: right;
  margin-top: 20px;
}

.el-alert {
  margin-bottom: 20px;
}

.form-tip {
  margin-top: 10px;
  font-size: 0.8em;
  color: #909399;
}
</style> 