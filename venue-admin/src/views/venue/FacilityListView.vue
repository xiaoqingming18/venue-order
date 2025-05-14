<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageFacilities, deleteFacility, pageVenues, updateFacility, createFacility } from '@/api/venue'
import type { VenueFacility, Venue, PageResult } from '@/types/venue'

// 定义查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  venueId: undefined as number | undefined,
  facilityType: '',
})

// 场馆选项列表
const venueOptions = ref<Venue[]>([])

// 数据列表和加载状态
const facilityList = ref<VenueFacility[]>([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加场馆设施')

// 表单数据
const form = reactive({
  id: undefined as number | undefined,
  venueId: undefined as number | undefined,
  facilityType: '',
  quantity: 1,
  price: undefined as number | undefined,
  positionDesc: '',
  positionDetails: {
    area: '',
    floor: '',
    section: '',
    details: ''
  }
})

// 表单规则
const rules = {
  venueId: [{ required: true, message: '请选择场馆', trigger: 'change' }],
  facilityType: [{ required: true, message: '请输入设施类型', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入设施数量', trigger: 'blur' }],
  positionDesc: [{ required: true, message: '请输入位置描述', trigger: 'blur' }],
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

// 获取场馆设施列表
const getFacilityList = async () => {
  if (!queryParams.venueId) {
    facilityList.value = []
    total.value = 0
    return
  }
  
  loading.value = true
  try {
    const params = {
      ...queryParams,
      venueId: queryParams.venueId as number // 确保venueId是number类型
    }
    const res = await pageFacilities(params) as { data: PageResult<VenueFacility> }
    facilityList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取场馆设施列表失败', error)
    ElMessage.error('获取场馆设施列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.page = 1
  getFacilityList()
}

// 重置搜索
const resetQuery = () => {
  queryParams.facilityType = ''
  handleQuery()
}

// 打开添加对话框
const handleAdd = () => {
  dialogTitle.value = '添加场馆设施'
  form.id = undefined
  form.venueId = queryParams.venueId
  form.facilityType = ''
  form.quantity = 1
  form.price = undefined
  form.positionDesc = ''
  form.positionDetails = {
    area: '主区域',
    floor: '1',
    section: 'A',
    details: ''
  }
  // 生成初始JSON
  updatePositionDescFromDetails()
  dialogVisible.value = true
}

// 打开编辑对话框
const handleEdit = (row: VenueFacility) => {
  dialogTitle.value = '编辑场馆设施'
  form.id = row.id
  form.venueId = row.venueId
  form.facilityType = row.facilityType
  form.quantity = row.quantity
  form.price = row.price
  form.positionDesc = row.positionDesc
  
  // 尝试解析JSON
  try {
    const positionData = JSON.parse(row.positionDesc)
    form.positionDetails = {
      area: positionData.area || '主区域',
      floor: positionData.floor || '1',
      section: positionData.section || 'A',
      details: positionData.details || ''
    }
  } catch (e) {
    console.error('解析位置描述JSON失败', e)
    form.positionDetails = {
      area: '主区域',
      floor: '1',
      section: 'A',
      details: ''
    }
  }
  
  dialogVisible.value = true
}

// 当位置详情变化时更新位置描述JSON
const updatePositionDescFromDetails = () => {
  const positionDescJson = {
    area: form.positionDetails.area || '主区域',
    floor: form.positionDetails.floor || '1',
    section: form.positionDetails.section || 'A',
    details: form.positionDetails.details || ''
  }
  form.positionDesc = JSON.stringify(positionDescJson, null, 2)
}

// 监听位置详情字段变化
watch(
  () => form.positionDetails,
  () => {
    updatePositionDescFromDetails()
  },
  { deep: true }
)

// 处理分页变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  getFacilityList()
}

const handleCurrentChange = (val: number) => {
  queryParams.page = val
  getFacilityList()
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        // 确保位置描述JSON已更新
        updatePositionDescFromDetails()
        
        const positionDescString = form.positionDesc
        
        if (form.id) {
          // 更新场馆设施
          await updateFacility(form.id, {
            venueId: form.venueId,
            facilityType: form.facilityType,
            quantity: form.quantity,
            price: form.price,
            positionDesc: positionDescString
          })
        } else {
          // 创建场馆设施
          await createFacility({
            venueId: form.venueId,
            facilityType: form.facilityType,
            quantity: form.quantity,
            price: form.price,
            positionDesc: positionDescString
          })
        }
        ElMessage.success(form.id ? '更新成功' : '添加成功')
        dialogVisible.value = false
        getFacilityList()
      } catch (error) {
        console.error('保存场馆设施失败', error)
        ElMessage.error(form.id ? '更新失败' : '添加失败')
      }
    }
  })
}

// 删除场馆设施
const handleDelete = (id: number) => {
  ElMessageBox.confirm('确认删除该场馆设施吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteFacility(id)
      ElMessage.success('删除成功')
      getFacilityList()
    } catch (error) {
      console.error('删除场馆设施失败', error)
      ElMessage.error('删除场馆设施失败')
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
  <div class="facility-container">
    <el-card class="facility-card">
      <template #header>
        <div class="card-header">
          <h2>场馆设施管理</h2>
          <div class="header-btns">
            <el-alert
              v-if="!queryParams.venueId"
              type="info"
              title="请先选择场馆，然后添加设施"
              :closable="false"
              show-icon
              style="margin-right: 10px;"
            />
            <el-button type="primary" @click="handleAdd" :disabled="!queryParams.venueId">添加设施</el-button>
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
        <el-form-item label="设施类型">
          <el-input v-model="queryParams.facilityType" placeholder="请输入设施类型" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :disabled="!queryParams.venueId">搜索</el-button>
          <el-button @click="resetQuery" :disabled="!queryParams.venueId">重置</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="facilityList.length === 0 && queryParams.venueId"
        type="warning"
        title="该场馆暂无设施，请添加设施以便用户能够预约！"
        description="场馆必须有设施才能被用户预约。设施包括场地、器材等可预约的资源。"
        :closable="true"
        show-icon
        style="margin-bottom: 20px;"
      />

      <el-table 
        v-else
        v-loading="loading" 
        :data="facilityList" 
        style="width: 100%" 
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="场馆名称" width="180">
          <template #default="{ row }">
            <span>{{ getVenueName(row.venueId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="facilityType" label="设施类型" min-width="120" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column label="价格" width="150">
          <template #default="{ row }">
            <span v-if="row.price">{{ row.price }} 元/小时</span>
            <el-tag v-else type="info" size="small">使用场馆基准价</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="positionDesc" label="位置描述" min-width="180" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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
        <el-form-item label="设施类型" prop="facilityType">
          <el-input v-model="form.facilityType" placeholder="请输入设施类型，如：灯光、音响等" />
        </el-form-item>
        <el-form-item label="设施数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" :step="1" style="width: 220px" />
        </el-form-item>
        <el-form-item label="设施价格">
          <el-input-number 
            v-model="form.price" 
            placeholder="设置设施价格，留空则使用场馆基准价格" 
            :min="0.01" 
            :precision="2" 
            :step="10"
            style="width: 100%"
          />
          <div class="form-tip">留空表示使用场馆基准价格</div>
        </el-form-item>
        
        <!-- 位置详情表单 -->
        <el-form-item label="位置信息">
          <el-alert
            type="info"
            title="请填写设施位置详情，系统将自动生成JSON格式数据"
            :closable="false"
            show-icon
            style="margin-bottom: 10px;"
          />
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="区域" label-width="60px" style="margin-bottom: 15px;">
                <el-input v-model="form.positionDetails.area" placeholder="主区域/副区域" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="楼层" label-width="60px" style="margin-bottom: 15px;">
                <el-input v-model="form.positionDetails.floor" placeholder="如: 1/2/B1" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="分区" label-width="60px" style="margin-bottom: 15px;">
                <el-input v-model="form.positionDetails.section" placeholder="如: A/B/C" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="详情" label-width="60px">
            <el-input 
              v-model="form.positionDetails.details" 
              type="textarea" 
              placeholder="请输入位置的详细描述" 
              :rows="2"
            />
          </el-form-item>
        </el-form-item>
        
        <!-- 位置描述JSON预览 -->
        <el-form-item label="JSON预览" prop="positionDesc">
          <el-input 
            v-model="form.positionDesc" 
            type="textarea" 
            readonly
            placeholder="系统将根据上方表单自动生成JSON格式数据" 
            :rows="3"
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
.facility-container {
  padding: 20px;
}

.facility-card {
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
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}
</style> 