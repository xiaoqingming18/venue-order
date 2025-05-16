<template>
  <div class="product-edit-container">
    <el-page-header @back="goBack" :title="isEdit ? '编辑积分商品' : '新增积分商品'">
      <template #content>
        <div class="page-title">{{ isEdit ? '编辑积分商品' : '新增积分商品' }}</div>
      </template>
    </el-page-header>

    <el-card class="form-card" shadow="never">
      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productRules"
        label-width="120px"
        label-position="right"
        :disabled="formLoading"
      >
        <el-row :gutter="20">
          <el-col :span="16">
            <!-- 基本信息 -->
            <div class="form-section">
              <h3 class="section-title">基本信息</h3>
              
              <el-form-item label="商品名称" prop="productName">
                <el-input v-model="productForm.productName" placeholder="请输入商品名称" maxlength="50" show-word-limit />
              </el-form-item>
              
              <el-form-item label="商品图片" prop="imageUrl">
                <el-upload
                  class="image-uploader"
                  action="/api/admin/upload/image"
                  :show-file-list="false"
                  :on-success="handleImageSuccess"
                  :before-upload="beforeImageUpload"
                >
                  <img v-if="productForm.imageUrl" :src="productForm.imageUrl" class="upload-image" />
                  <el-icon v-else class="upload-icon"><Plus /></el-icon>
                </el-upload>
                <div class="form-help">建议上传正方形图片，大小不超过2MB</div>
              </el-form-item>
              
              <el-form-item label="商品描述" prop="productDesc">
                <el-input
                  v-model="productForm.productDesc"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入商品描述"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </div>
            
            <!-- 兑换信息 -->
            <div class="form-section">
              <h3 class="section-title">兑换信息</h3>
              
              <el-form-item label="积分价格" prop="pointsPrice">
                <el-input-number 
                  v-model="productForm.pointsPrice" 
                  :min="1" 
                  :max="1000000" 
                  placeholder="兑换所需积分"
                  class="wide-number-input" 
                />
              </el-form-item>
              
              <el-form-item label="原价(元)" prop="originalPrice">
                <el-input-number 
                  v-model="productForm.originalPrice" 
                  :min="0" 
                  :precision="2" 
                  :step="0.01" 
                  placeholder="商品原价" 
                  class="wide-number-input"
                />
                <div class="form-help">若不填写则不显示原价</div>
              </el-form-item>
              
              <el-form-item label="兑换时间" prop="exchangeTime">
                <el-date-picker
                  v-model="exchangeTimeRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%;"
                />
                <div class="form-help">若不填写则不限制兑换时间</div>
              </el-form-item>
            </div>
          </el-col>
          
          <el-col :span="8">
            <!-- 库存设置 -->
            <div class="form-section">
              <h3 class="section-title">库存设置</h3>
              
              <el-form-item label="库存数量" prop="inventory">
                <el-input-number 
                  v-model="productForm.inventory" 
                  :min="0" 
                  :max="999999" 
                  placeholder="库存数量" 
                  class="wide-number-input"
                />
              </el-form-item>
              
              <el-form-item label="每人限兑" prop="limitPerUser">
                <el-input-number 
                  v-model="productForm.limitPerUser" 
                  :min="0" 
                  :max="999" 
                  placeholder="每人限兑数量" 
                  class="wide-number-input"
                />
                <div class="form-help">0表示不限制每人兑换数量</div>
              </el-form-item>
            </div>
            
            <!-- 状态设置 -->
            <div class="form-section">
              <h3 class="section-title">状态设置</h3>
              
              <el-form-item label="上架状态" prop="status">
                <el-switch
                  v-model="productForm.status"
                  :active-value="1"
                  :inactive-value="0"
                  active-text="上架"
                  inactive-text="下架"
                />
              </el-form-item>
              
              <el-form-item label="显示排序" prop="displayOrder">
                <el-input-number 
                  v-model="productForm.displayOrder" 
                  :min="0" 
                  :max="9999" 
                  placeholder="显示排序" 
                  class="wide-number-input"
                />
                <div class="form-help">数值越小越靠前显示</div>
              </el-form-item>
            </div>
            
            <!-- 预览 -->
            <div class="form-section preview-section">
              <h3 class="section-title">商品预览</h3>
              
              <div class="product-preview">
                <div class="preview-card">
                  <div class="preview-image">
                    <img :src="productForm.imageUrl || defaultImage" alt="商品图片" />
                  </div>
                  <div class="preview-content">
                    <div class="preview-name">{{ productForm.productName || '商品名称' }}</div>
                    <div class="preview-price">
                      <span class="points-price">{{ productForm.pointsPrice || 0 }} 积分</span>
                      <span class="original-price" v-if="productForm.originalPrice">¥{{ productForm.originalPrice }}</span>
                    </div>
                    <div class="preview-inventory">库存: {{ productForm.inventory || 0 }}</div>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
        
        <!-- 操作按钮 -->
        <div class="form-actions">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">保存</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getPointsProductById, saveOrUpdatePointsProduct } from '@/api/points'
import { PointsProductDTO } from '@/types/points'

const route = useRoute()
const router = useRouter()
const productId = computed(() => route.params.id as string)
const isEdit = computed(() => !!productId.value)

// 表单相关
const productFormRef = ref<FormInstance>()
const formLoading = ref(false)
const submitLoading = ref(false)
const exchangeTimeRange = ref(null)
const defaultImage = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

// 表单数据
const defaultProductForm: PointsProductDTO = {
  productName: '',
  productDesc: '',
  imageUrl: '',
  pointsPrice: 100,
  originalPrice: undefined,
  inventory: 100,
  limitPerUser: 1,
  startTime: undefined,
  endTime: undefined,
  status: 1,
  displayOrder: 999
}

const productForm = reactive<PointsProductDTO>({ ...defaultProductForm })

// 表单验证规则
const productRules = reactive<FormRules>({
  productName: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  imageUrl: [
    { required: true, message: '请上传商品图片', trigger: 'change' }
  ],
  pointsPrice: [
    { required: true, message: '请输入积分价格', trigger: 'blur' }
  ],
  inventory: [
    { required: true, message: '请输入库存数量', trigger: 'blur' }
  ]
})

// 图片上传相关
const handleImageSuccess = (response: any) => {
  productForm.imageUrl = response.data
}

const beforeImageUpload = (file: File) => {
  const isImage = /^image\/(jpeg|png|gif|jpg)$/.test(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('上传图片只能是 JPG/PNG/GIF 格式!')
  }
  
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
  }
  
  return isImage && isLt2M
}

// 获取商品详情
const fetchProductDetail = async () => {
  if (!isEdit.value) return
  
  formLoading.value = true
  try {
    const { data } = await getPointsProductById(parseInt(productId.value))
    
    // 填充表单数据
    Object.assign(productForm, {
      id: data.id,
      productName: data.productName,
      productDesc: data.productDesc,
      imageUrl: data.imageUrl,
      pointsPrice: data.pointsPrice,
      originalPrice: data.originalPrice,
      inventory: data.inventory,
      limitPerUser: data.limitPerUser,
      status: data.status,
      displayOrder: data.displayOrder || 999
    })
    
    // 设置兑换时间范围
    if (data.startTime && data.endTime) {
      exchangeTimeRange.value = [data.startTime, data.endTime]
    }
  } catch (error) {
    console.error('获取商品详情失败', error)
    ElMessage.error('获取商品详情失败')
    router.push({ name: 'pointsProducts' })
  } finally {
    formLoading.value = false
  }
}

// 提交表单
const submitForm = async () => {
  if (!productFormRef.value) return
  
  await productFormRef.value.validate(async (valid) => {
    if (valid) {
      // 设置兑换时间范围
      if (exchangeTimeRange.value) {
        productForm.startTime = exchangeTimeRange.value[0]
        productForm.endTime = exchangeTimeRange.value[1]
      } else {
        productForm.startTime = undefined
        productForm.endTime = undefined
      }
      
      submitLoading.value = true
      try {
        await saveOrUpdatePointsProduct(productForm)
        ElMessage.success(`${isEdit.value ? '更新' : '添加'}成功`)
        router.push({ name: 'pointsProducts' })
      } catch (error) {
        console.error(`${isEdit.value ? '更新' : '添加'}失败`, error)
        ElMessage.error(`${isEdit.value ? '更新' : '添加'}失败`)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 返回列表页
const goBack = () => {
  router.push({ name: 'pointsProducts' })
}

// 页面初始化
onMounted(() => {
  fetchProductDetail()
})
</script>

<style scoped>
.product-edit-container {
  padding: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
}

.form-card {
  margin-top: 20px;
}

.form-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
  padding-left: 10px;
  border-left: 3px solid #409EFF;
}

.form-help {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.form-actions {
  margin-top: 30px;
  text-align: center;
}

.wide-number-input {
  width: 100%;
}

/* 图片上传样式 */
.image-uploader {
  display: flex;
  justify-content: center;
}

.image-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.image-uploader :deep(.el-upload:hover) {
  border-color: #409EFF;
}

.upload-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.upload-image {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

/* 商品预览样式 */
.preview-section {
  border: none;
}

.product-preview {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.preview-card {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.preview-image {
  height: 180px;
  overflow: hidden;
}

.preview-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-content {
  padding: 12px;
}

.preview-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #303133;
}

.preview-price {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  gap: 8px;
}

.points-price {
  font-size: 18px;
  font-weight: bold;
  color: #E6A23C;
}

.original-price {
  font-size: 14px;
  color: #909399;
  text-decoration: line-through;
}

.preview-inventory {
  font-size: 12px;
  color: #606266;
}
</style> 