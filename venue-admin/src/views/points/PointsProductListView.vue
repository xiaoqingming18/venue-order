<template>
  <div class="points-product-container">
    <div class="header">
      <h2>积分商品管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAddProduct">新增商品</el-button>
      </div>
    </div>

    <!-- 搜索条件 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="积分范围">
          <el-input-number v-model="searchForm.minPoints" :min="0" placeholder="最小值" class="points-range" />
          <span style="margin: 0 5px;">-</span>
          <el-input-number v-model="searchForm.maxPoints" :min="0" placeholder="最大值" class="points-range" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option :value="1" label="上架中" />
            <el-option :value="0" label="已下架" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 积分商品列表 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <div class="header-title">积分商品列表</div>
          <div class="header-actions">
            <el-button 
              type="success" 
              size="small" 
              @click="handleBatchEnable" 
              :disabled="selectedProducts.length === 0"
            >
              批量上架
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleBatchDisable" 
              :disabled="selectedProducts.length === 0"
            >
              批量下架
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="viewExchangeRecords"
            >
              查看兑换记录
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="productsList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="商品图片" width="100">
          <template #default="scope">
            <el-image
              style="width: 50px; height: 50px"
              :src="scope.row.imageUrl || defaultImage"
              fit="cover"
              :preview-src-list="[scope.row.imageUrl || defaultImage]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" min-width="180" />
        <el-table-column label="积分价格" width="100">
          <template #default="scope">
            <span class="points-price">{{ scope.row.pointsPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="原价(元)" width="100">
          <template #default="scope">
            <span v-if="scope.row.originalPrice">¥{{ scope.row.originalPrice }}</span>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="inventory" label="库存" width="80" sortable />
        <el-table-column prop="limitPerUser" label="每人限兑" width="100" />
        <el-table-column prop="exchangeCount" label="已兑换" width="80" sortable />
        <el-table-column label="兑换时间" width="180">
          <template #default="scope">
            <div v-if="scope.row.startTime && scope.row.endTime">
              {{ scope.row.startTime }} 至 {{ scope.row.endTime }}
            </div>
            <span v-else>不限时间</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleStatusChange(scope.row.id, val)"
              active-color="#13ce66"
              inactive-color="#ff4949"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEditProduct(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteProduct(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
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

    <!-- 新增/编辑商品对话框 -->
    <el-dialog
      :title="isEdit ? '编辑积分商品' : '新增积分商品'"
      v-model="dialogVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productRules"
        label-width="120px"
        label-position="right"
      >
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="productForm.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品图片" prop="imageUrl">
          <el-input v-model="productForm.imageUrl" placeholder="请输入图片URL">
            <template #append>
              <el-button>选择图片</el-button>
            </template>
          </el-input>
          <div class="image-preview">
            <el-image
              style="width: 100px; height: 100px"
              :src="productForm.imageUrl || defaultImage"
              fit="cover"
            />
          </div>
        </el-form-item>
        <el-form-item label="积分价格" prop="pointsPrice">
          <el-input-number v-model="productForm.pointsPrice" :min="1" :max="1000000" placeholder="兑换所需积分" />
        </el-form-item>
        <el-form-item label="原价(元)" prop="originalPrice">
          <el-input-number v-model="productForm.originalPrice" :min="0" :precision="2" :step="0.01" placeholder="商品原价" />
          <div class="form-help">若不填写则不显示原价</div>
        </el-form-item>
        <el-form-item label="库存数量" prop="inventory">
          <el-input-number v-model="productForm.inventory" :min="0" :max="999999" placeholder="库存数量" />
        </el-form-item>
        <el-form-item label="每人限兑" prop="limitPerUser">
          <el-input-number v-model="productForm.limitPerUser" :min="0" :max="999" placeholder="每人限兑数量" />
          <div class="form-help">0表示不限制每人兑换数量</div>
        </el-form-item>
        <el-form-item label="兑换时间" prop="exchangeTime">
          <el-date-picker
            v-model="exchangeTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
          <div class="form-help">若不填写则不限制兑换时间</div>
        </el-form-item>
        <el-form-item label="商品描述" prop="productDesc">
          <el-input
            v-model="productForm.productDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="productForm.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProductForm" :loading="submitLoading">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { 
  getPointsProductsByPage, 
  saveOrUpdatePointsProduct, 
  updatePointsProductStatus, 
  deletePointsProduct 
} from '@/api/points'
import { PointsProduct, PointsProductDTO } from '@/types/points'

const router = useRouter()

// 列表数据相关
const productsList = ref<PointsProduct[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedProducts = ref<PointsProduct[]>([])
const defaultImage = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

// 搜索条件
const searchForm = reactive({
  productName: '',
  minPoints: undefined as number | undefined,
  maxPoints: undefined as number | undefined,
  status: ''
})

// 对话框相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const productFormRef = ref<FormInstance>()
const submitLoading = ref(false)
const exchangeTimeRange = ref(null)

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
  status: 1
}

const productForm = reactive<PointsProductDTO>({ ...defaultProductForm })

// 表单验证规则
const productRules = reactive<FormRules>({
  productName: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  pointsPrice: [
    { required: true, message: '请输入积分价格', trigger: 'blur' }
  ],
  inventory: [
    { required: true, message: '请输入库存数量', trigger: 'blur' }
  ]
})

// 获取积分商品列表
const fetchProductsList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    
    const { data } = await getPointsProductsByPage(params)
    productsList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取积分商品列表失败', error)
    ElMessage.error('获取积分商品列表失败')
  } finally {
    loading.value = false
  }
}

// 处理表格选择变化
const handleSelectionChange = (selection: PointsProduct[]) => {
  selectedProducts.value = selection
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchProductsList()
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.productName = ''
  searchForm.minPoints = undefined
  searchForm.maxPoints = undefined
  searchForm.status = ''
  currentPage.value = 1
  fetchProductsList()
}

// 批量上架
const handleBatchEnable = async () => {
  if (selectedProducts.value.length === 0) return
  
  try {
    const promises = selectedProducts.value.map(product => 
      updatePointsProductStatus(product.id, 1)
    )
    await Promise.all(promises)
    ElMessage.success('批量上架成功')
    fetchProductsList()
  } catch (error) {
    console.error('批量上架失败', error)
    ElMessage.error('批量上架失败')
  }
}

// 批量下架
const handleBatchDisable = async () => {
  if (selectedProducts.value.length === 0) return
  
  try {
    const promises = selectedProducts.value.map(product => 
      updatePointsProductStatus(product.id, 0)
    )
    await Promise.all(promises)
    ElMessage.success('批量下架成功')
    fetchProductsList()
  } catch (error) {
    console.error('批量下架失败', error)
    ElMessage.error('批量下架失败')
  }
}

// 处理商品状态变化
const handleStatusChange = async (id: number, status: number) => {
  try {
    await updatePointsProductStatus(id, status)
    ElMessage.success(`${status === 1 ? '上架' : '下架'}成功`)
  } catch (error) {
    console.error('更新状态失败', error)
    ElMessage.error('更新状态失败')
    // 刷新列表，恢复状态
    fetchProductsList()
  }
}

// 新增商品
const handleAddProduct = () => {
  isEdit.value = false
  Object.assign(productForm, defaultProductForm)
  exchangeTimeRange.value = null
  dialogVisible.value = true
}

// 编辑商品
const handleEditProduct = (row: PointsProduct) => {
  isEdit.value = true
  Object.assign(productForm, {
    id: row.id,
    productName: row.productName,
    productDesc: row.productDesc,
    imageUrl: row.imageUrl,
    pointsPrice: row.pointsPrice,
    originalPrice: row.originalPrice,
    inventory: row.inventory,
    limitPerUser: row.limitPerUser,
    status: row.status
  })
  
  // 设置兑换时间范围
  if (row.startTime && row.endTime) {
    exchangeTimeRange.value = [row.startTime, row.endTime]
  } else {
    exchangeTimeRange.value = null
  }
  
  dialogVisible.value = true
}

// 删除商品
const handleDeleteProduct = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该积分商品吗？删除后不可恢复', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deletePointsProduct(id)
    ElMessage.success('删除成功')
    fetchProductsList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const submitProductForm = async () => {
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
        dialogVisible.value = false
        fetchProductsList()
      } catch (error) {
        console.error(`${isEdit.value ? '更新' : '添加'}失败`, error)
        ElMessage.error(`${isEdit.value ? '更新' : '添加'}失败`)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 查看兑换记录
const viewExchangeRecords = () => {
  router.push({ name: 'exchangeRecords' })
}

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchProductsList()
}

// 处理当前页变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchProductsList()
}

// 页面加载时获取数据
onMounted(() => {
  fetchProductsList()
})

// 监听对话框关闭，重置表单
watch(dialogVisible, (val) => {
  if (!val && productFormRef.value) {
    productFormRef.value.resetFields()
  }
})
</script>

<style scoped>
.points-product-container {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.points-range {
  width: 120px;
}

.points-price {
  font-weight: bold;
  color: #E6A23C;
}

.form-help {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.image-preview {
  margin-top: 10px;
  display: flex;
  justify-content: center;
}
</style> 