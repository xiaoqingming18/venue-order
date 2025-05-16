<template>
  <div class="member-level-container">
    <div class="header">
      <h2>会员等级管理</h2>
      <el-button type="primary" @click="handleAddLevel">新增等级</el-button>
    </div>

    <!-- 等级列表 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <div class="header-title">等级列表</div>
          <div class="header-actions">
            <el-button type="success" size="small" @click="handleBatchEnable" :disabled="selectedLevels.length === 0">
              批量启用
            </el-button>
            <el-button type="danger" size="small" @click="handleBatchDisable" :disabled="selectedLevels.length === 0">
              批量禁用
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="levelsList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="等级图标" width="100">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.iconUrl || defaultIcon">
              {{ scope.row.levelName?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="levelName" label="等级名称" min-width="120" />
        <el-table-column prop="levelValue" label="等级值" width="80" sortable />
        <el-table-column prop="pointThreshold" label="积分门槛" width="100" sortable />
        <el-table-column label="折扣率" width="100">
          <template #default="scope">
            <span>{{ (scope.row.discountRate * 10).toFixed(1) }}折</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="等级描述" min-width="180" />
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
            <el-button type="primary" size="small" @click="handleEditLevel(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteLevel(scope.row.id)">删除</el-button>
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

    <!-- 新增/编辑会员等级对话框 -->
    <el-dialog
      :title="isEdit ? '编辑会员等级' : '新增会员等级'"
      v-model="dialogVisible"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="levelFormRef"
        :model="levelForm"
        :rules="levelRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="等级名称" prop="levelName">
          <el-input v-model="levelForm.levelName" placeholder="请输入等级名称" />
        </el-form-item>
        <el-form-item label="等级值" prop="levelValue">
          <el-input-number v-model="levelForm.levelValue" :min="1" :max="100" placeholder="请输入等级值" />
          <div class="form-help">等级值越大，等级越高，例如：1表示普通会员，2表示银卡会员</div>
        </el-form-item>
        <el-form-item label="积分门槛" prop="pointThreshold">
          <el-input-number v-model="levelForm.pointThreshold" :min="0" :max="1000000" placeholder="请输入积分门槛" />
          <div class="form-help">用户积分达到或超过该值时，会升级到该等级</div>
        </el-form-item>
        <el-form-item label="折扣率" prop="discountRate">
          <el-slider
            v-model="levelForm.discountRate"
            :min="0.1"
            :max="1"
            :step="0.05"
            :format-tooltip="(val) => (val * 10).toFixed(1) + '折'"
          />
          <div class="discount-value">{{ (levelForm.discountRate * 10).toFixed(1) }}折</div>
        </el-form-item>
        <el-form-item label="等级图标" prop="iconUrl">
          <el-input v-model="levelForm.iconUrl" placeholder="请输入图标URL地址">
            <template #append>
              <el-button>选择图片</el-button>
            </template>
          </el-input>
          <div class="icon-preview">
            <el-avatar :size="50" :src="levelForm.iconUrl || defaultIcon">
              {{ levelForm.levelName?.charAt(0) || '会' }}
            </el-avatar>
          </div>
        </el-form-item>
        <el-form-item label="等级描述" prop="description">
          <el-input
            v-model="levelForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入等级描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="levelForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitLevelForm" :loading="submitLoading">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { 
  getMemberLevelsByPage, 
  saveOrUpdateMemberLevel, 
  updateMemberLevelStatus, 
  deleteMemberLevel 
} from '@/api/points'
import { MemberLevel, MemberLevelDTO } from '@/types/points'

// 列表数据相关
const levelsList = ref<MemberLevel[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedLevels = ref<MemberLevel[]>([])
const defaultIcon = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 对话框相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const levelFormRef = ref<FormInstance>()
const submitLoading = ref(false)

// 表单数据
const defaultLevelForm: MemberLevelDTO = {
  levelName: '',
  levelValue: 1,
  pointThreshold: 0,
  discountRate: 1,
  description: '',
  iconUrl: '',
  status: 1
}

const levelForm = reactive<MemberLevelDTO>({ ...defaultLevelForm })

// 表单验证规则
const levelRules = reactive<FormRules>({
  levelName: [
    { required: true, message: '请输入等级名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  levelValue: [
    { required: true, message: '请输入等级值', trigger: 'blur' }
  ],
  pointThreshold: [
    { required: true, message: '请输入积分门槛', trigger: 'blur' }
  ],
  discountRate: [
    { required: true, message: '请设置折扣率', trigger: 'change' }
  ]
})

// 获取会员等级列表
const fetchLevelsList = async () => {
  loading.value = true
  try {
    const { data } = await getMemberLevelsByPage({
      page: currentPage.value,
      size: pageSize.value
    })
    levelsList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取会员等级列表失败', error)
    ElMessage.error('获取会员等级列表失败')
  } finally {
    loading.value = false
  }
}

// 处理表格选择变化
const handleSelectionChange = (selection: MemberLevel[]) => {
  selectedLevels.value = selection
}

// 批量启用
const handleBatchEnable = async () => {
  if (selectedLevels.value.length === 0) return
  
  try {
    const promises = selectedLevels.value.map(level => 
      updateMemberLevelStatus(level.id, 1)
    )
    await Promise.all(promises)
    ElMessage.success('批量启用成功')
    fetchLevelsList()
  } catch (error) {
    console.error('批量启用失败', error)
    ElMessage.error('批量启用失败')
  }
}

// 批量禁用
const handleBatchDisable = async () => {
  if (selectedLevels.value.length === 0) return
  
  try {
    const promises = selectedLevels.value.map(level => 
      updateMemberLevelStatus(level.id, 0)
    )
    await Promise.all(promises)
    ElMessage.success('批量禁用成功')
    fetchLevelsList()
  } catch (error) {
    console.error('批量禁用失败', error)
    ElMessage.error('批量禁用失败')
  }
}

// 处理等级状态变化
const handleStatusChange = async (id: number, status: number) => {
  try {
    await updateMemberLevelStatus(id, status)
    ElMessage.success(`${status === 1 ? '启用' : '禁用'}成功`)
  } catch (error) {
    console.error('更新状态失败', error)
    ElMessage.error('更新状态失败')
    // 刷新列表，恢复状态
    fetchLevelsList()
  }
}

// 新增等级
const handleAddLevel = () => {
  isEdit.value = false
  Object.assign(levelForm, defaultLevelForm)
  dialogVisible.value = true
}

// 编辑等级
const handleEditLevel = (row: MemberLevel) => {
  isEdit.value = true
  Object.assign(levelForm, {
    id: row.id,
    levelName: row.levelName,
    levelValue: row.levelValue,
    pointThreshold: row.pointThreshold,
    discountRate: row.discountRate,
    description: row.description,
    iconUrl: row.iconUrl,
    status: row.status
  })
  dialogVisible.value = true
}

// 删除等级
const handleDeleteLevel = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该会员等级吗？删除后可能影响用户权益', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteMemberLevel(id)
    ElMessage.success('删除成功')
    fetchLevelsList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const submitLevelForm = async () => {
  if (!levelFormRef.value) return
  
  await levelFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await saveOrUpdateMemberLevel(levelForm)
        ElMessage.success(`${isEdit.value ? '更新' : '添加'}成功`)
        dialogVisible.value = false
        fetchLevelsList()
      } catch (error) {
        console.error(`${isEdit.value ? '更新' : '添加'}失败`, error)
        ElMessage.error(`${isEdit.value ? '更新' : '添加'}失败`)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchLevelsList()
}

// 处理当前页变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchLevelsList()
}

// 页面加载时获取数据
onMounted(() => {
  fetchLevelsList()
})

// 监听对话框关闭，重置表单
watch(dialogVisible, (val) => {
  if (!val && levelFormRef.value) {
    levelFormRef.value.resetFields()
  }
})
</script>

<style scoped>
.member-level-container {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.form-help {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.discount-value {
  margin-top: 5px;
  font-size: 16px;
  color: #409EFF;
  font-weight: 600;
  text-align: center;
}

.icon-preview {
  margin-top: 10px;
  display: flex;
  justify-content: center;
}
</style> 