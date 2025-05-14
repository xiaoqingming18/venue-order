<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageVenueTypes, createVenueType, updateVenueType, deleteVenueType } from '@/api/venue'
import type { VenueType, PageResult } from '@/types/venue'

// 定义查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  name: '',
})

// 数据列表和加载状态
const venueTypeList = ref<VenueType[]>([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加场馆类型')

// 表单数据
const form = reactive({
  id: undefined as number | undefined,
  name: '',
  description: '',
  basePrice: 0,
})

// 表单规则
const rules = {
  name: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
  basePrice: [{ required: true, message: '请输入基础价格', trigger: 'blur' }],
}

// 表单引用
const formRef = ref()

// 获取场馆类型列表
const getVenueTypeList = async () => {
  loading.value = true
  try {
    const res = await pageVenueTypes(queryParams) as { data: PageResult<VenueType> }
    venueTypeList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取场馆类型列表失败', error)
    ElMessage.error('获取场馆类型列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.page = 1
  getVenueTypeList()
}

// 重置搜索
const resetQuery = () => {
  queryParams.name = ''
  handleQuery()
}

// 打开添加对话框
const handleAdd = () => {
  dialogTitle.value = '添加场馆类型'
  form.id = undefined
  form.name = ''
  form.description = ''
  form.basePrice = 0
  dialogVisible.value = true
}

// 打开编辑对话框
const handleEdit = (row: VenueType) => {
  dialogTitle.value = '编辑场馆类型'
  form.id = row.id
  form.name = row.name
  form.description = row.description
  form.basePrice = row.basePrice
  dialogVisible.value = true
}

// 处理分页变化
const handleSizeChange = (val: number) => {
  queryParams.size = val
  getVenueTypeList()
}

const handleCurrentChange = (val: number) => {
  queryParams.page = val
  getVenueTypeList()
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (form.id) {
          // 更新场馆类型
          await updateVenueType(form.id, {
            name: form.name,
            description: form.description,
            basePrice: form.basePrice
          })
          ElMessage.success('更新成功')
        } else {
          // 创建场馆类型
          await createVenueType({
            name: form.name,
            description: form.description,
            basePrice: form.basePrice
          })
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        getVenueTypeList()
      } catch (error) {
        console.error('保存场馆类型失败', error)
        ElMessage.error(form.id ? '更新失败' : '添加失败')
      }
    }
  })
}

// 删除场馆类型
const handleDelete = (id: number) => {
  ElMessageBox.confirm('确认删除该场馆类型吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteVenueType(id)
      ElMessage.success('删除成功')
      getVenueTypeList()
    } catch (error) {
      console.error('删除场馆类型失败', error)
      ElMessage.error('删除场馆类型失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 初始化
onMounted(() => {
  getVenueTypeList()
})
</script>

<template>
  <div class="venue-type-container">
    <el-card class="venue-type-card">
      <template #header>
        <div class="card-header">
          <h2>场馆类型管理</h2>
          <el-button type="primary" @click="handleAdd">添加场馆类型</el-button>
        </div>
      </template>

      <el-form :model="queryParams" inline>
        <el-form-item label="类型名称">
          <el-input v-model="queryParams.name" placeholder="请输入类型名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="venueTypeList" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="类型名称" min-width="120" />
        <el-table-column prop="description" label="类型描述" min-width="200" />
        <el-table-column prop="basePrice" label="基础价格" width="120">
          <template #default="{ row }">
            <span>{{ row.basePrice }} 元</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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
        label-width="80px"
      >
        <el-form-item label="类型名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入类型名称" />
        </el-form-item>
        <el-form-item label="类型描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入类型描述" />
        </el-form-item>
        <el-form-item label="基础价格" prop="basePrice">
          <el-input-number v-model="form.basePrice" :min="0" :precision="2" :step="10" />
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
.venue-type-container {
  padding: 20px;
}

.venue-type-card {
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

.dialog-footer {
  text-align: right;
  margin-top: 20px;
}
</style> 