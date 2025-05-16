<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getVenueById, createVenue, updateVenue, pageVenueTypes } from '@/api/venue'
import { uploadVenueCover, uploadFile } from '@/api/upload'
import type { VenueType, VenueDTO, PageResult } from '@/types/venue'

const router = useRouter()
const route = useRoute()

// 表单引用
const formRef = ref()

// 场馆ID (编辑模式下不为空)
const venueId = computed(() => {
  return route.params.id ? Number(route.params.id) : undefined
})

// 是否为编辑模式
const isEdit = computed(() => {
  return !!venueId.value
})

// 标题
const title = computed(() => {
  return isEdit.value ? '编辑场馆' : '新增场馆'
})

// 场馆类型选项
const venueTypeOptions = ref<VenueType[]>([])

// 封面图片
const coverImageUrl = ref<string>('')
const uploadLoading = ref<boolean>(false)

// 表单数据
const form = reactive<VenueDTO>({
  name: '',
  venueTypeId: 0,
  basePrice: 0,
  capacity: 0,
  address: '',
  description: '',
  businessHours: '',
  contactPhone: '',
  coverImage: '',
  status: 1,
})

// 加载中状态
const loading = ref(false)

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入场馆名称', trigger: 'blur' }],
  venueTypeId: [{ required: true, message: '请选择场馆类型', trigger: 'change' }],
  basePrice: [{ required: true, message: '请输入基准价格', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容纳人数', trigger: 'blur' }],
  address: [{ required: true, message: '请输入场馆地址', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

// 获取场馆类型下拉选项
const getVenueTypeOptions = async () => {
  try {
    const res = await pageVenueTypes({ page: 1, size: 100 }) as { data: PageResult<VenueType> }
    venueTypeOptions.value = res.data.records
  } catch (error) {
    console.error('获取场馆类型列表失败', error)
    ElMessage.error('获取场馆类型选项失败')
  }
}

// 获取场馆详情
const getVenueDetail = async () => {
  if (!venueId.value) return

  loading.value = true
  try {
    const res = await getVenueById(venueId.value) as { data: any }
    const venueData = res.data
    
    // 将数据填充到表单
    form.name = venueData.name
    form.venueTypeId = venueData.venueTypeId
    form.basePrice = venueData.basePrice
    form.capacity = venueData.capacity
    form.address = venueData.address
    form.description = venueData.description || ''
    form.businessHours = venueData.businessHours || ''
    form.contactPhone = venueData.contactPhone || ''
    form.coverImage = venueData.coverImage || ''
    form.status = venueData.status
    
    // 设置封面图片预览
    if (venueData.coverImage) {
      coverImageUrl.value = venueData.coverImage
    }
  } catch (error) {
    console.error('获取场馆详情失败', error)
    ElMessage.error('获取场馆详情失败')
    router.push('/home/venues')
  } finally {
    loading.value = false
  }
}

// 处理封面上传
const handleCoverUpload = async (file: File) => {
  if (!file) return
  
  // 检查文件类型
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('请上传图片文件')
    return false
  }
  
  // 检查文件大小（限制为2MB）
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  
  uploadLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    // 使用FileReader预览图片
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = (e) => {
      coverImageUrl.value = e.target?.result as string
    }
    
    // 上传到后端获取URL
    const res = await uploadFile(file) as any
    form.coverImage = res.data
    ElMessage.success('封面上传成功')
  } catch (error) {
    console.error('封面上传失败', error)
    ElMessage.error('封面上传失败')
  } finally {
    uploadLoading.value = false
  }
  
  // 阻止默认上传行为
  return false
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    loading.value = true
    try {
      // 确保数据类型正确
      const formData = {
        ...form,
        venueTypeId: Number(form.venueTypeId),
        basePrice: Number(form.basePrice),
        capacity: Number(form.capacity),
        status: Number(form.status)
      }
      
      if (isEdit.value) {
        // 编辑模式
        await updateVenue(venueId.value as number, formData)
        ElMessage.success('更新场馆成功')
      } else {
        // 新增模式
        await createVenue(formData)
        ElMessage.success('创建场馆成功')
      }
      router.push('/home/venues')
    } catch (error: any) {
      console.error('保存场馆失败', error)
      // 尝试提取详细错误信息
      let errorMsg = isEdit.value ? '更新场馆失败' : '创建场馆失败'
      if (error.response && error.response.data) {
        if (error.response.data.message) {
          errorMsg = error.response.data.message
        }
      }
      ElMessage.error(errorMsg)
    } finally {
      loading.value = false
    }
  })
}

// 取消
const cancel = () => {
  router.push('/home/venues')
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  
  coverImageUrl.value = ''
  
  if (isEdit.value) {
    getVenueDetail()
  } else {
    form.status = 1
    form.coverImage = ''
  }
}

// 初始化
onMounted(async () => {
  await getVenueTypeOptions()
  if (isEdit.value) {
    await getVenueDetail()
  }
})
</script>

<template>
  <div class="venue-edit-container">
    <el-card class="venue-edit-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>{{ title }}</h2>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="venue-form"
      >
        <el-form-item label="场馆封面" prop="coverImage">
          <el-upload
            class="cover-uploader"
            action="#"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="(file: any) => handleCoverUpload(file.raw)"
            :before-upload="() => false"
          >
            <el-image v-if="coverImageUrl" :src="coverImageUrl" class="cover-preview" fit="cover" />
            <div v-else class="upload-placeholder">
              <el-icon v-if="!uploadLoading"><Plus /></el-icon>
              <el-icon v-else class="is-loading"><Loading /></el-icon>
              <div class="el-upload__text">
                点击上传封面
                <em>支持jpg、png格式，不超过2MB</em>
              </div>
            </div>
          </el-upload>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="场馆名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入场馆名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="场馆类型" prop="venueTypeId">
              <el-select v-model="form.venueTypeId" placeholder="请选择场馆类型" style="width: 100%">
                <el-option
                  v-for="item in venueTypeOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="基准价格(元)" prop="basePrice">
              <el-input-number v-model="form.basePrice" :min="0" :precision="2" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="容纳人数" prop="capacity">
              <el-input-number v-model="form.capacity" :min="1" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="场馆地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入场馆地址" />
        </el-form-item>

        <el-form-item label="场馆描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入场馆描述" :rows="3" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="营业时间" prop="businessHours">
              <el-input v-model="form.businessHours" placeholder="例如: 09:00-22:00" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">开放</el-radio>
            <el-radio :label="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm">保存</el-button>
          <el-button @click="resetForm">重置</el-button>
          <el-button @click="cancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.venue-edit-container {
  padding: 20px;
}

.venue-edit-card {
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

.venue-form {
  max-width: 900px;
  margin: 0 auto;
}

.cover-uploader {
  width: 100%;
  text-align: center;
}

.cover-preview {
  width: 300px;
  height: 200px;
  border-radius: 8px;
  border: 1px solid #ccc;
  object-fit: cover;
}

.upload-placeholder {
  width: 300px;
  height: 200px;
  margin: 0 auto;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: border-color 0.3s;
  padding: 20px;
}

.upload-placeholder:hover {
  border-color: #409EFF;
}

.upload-placeholder .el-icon {
  font-size: 28px;
  color: #8c939d;
  margin-bottom: 8px;
}

.upload-placeholder .el-upload__text {
  color: #606266;
  font-size: 14px;
  text-align: center;
}

.upload-placeholder .el-upload__text em {
  display: block;
  color: #909399;
  font-style: normal;
  font-size: 12px;
  margin-top: 6px;
}
</style> 