<template>
  <div class="point-rule-container">
    <div class="header">
      <h2>积分规则管理</h2>
      <el-button type="primary" @click="handleAddRule">新增规则</el-button>
    </div>

    <!-- 规则列表 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <div class="header-title">规则列表</div>
          <div class="header-actions">
            <el-button type="success" size="small" @click="handleBatchEnable" :disabled="selectedRules.length === 0">
              批量启用
            </el-button>
            <el-button type="danger" size="small" @click="handleBatchDisable" :disabled="selectedRules.length === 0">
              批量禁用
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="rulesList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="ruleName" label="规则名称" min-width="120" />
        <el-table-column label="规则类型" width="120">
          <template #default="scope">
            <el-tag :type="getRuleTypeTag(scope.row.ruleType)">
              {{ getRuleTypeName(scope.row.ruleType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pointValue" label="积分值" width="100" />
        <el-table-column prop="ruleDescription" label="规则描述" min-width="220" />
        <el-table-column prop="validityDays" label="有效期(天)" width="120" />
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
            <el-button type="primary" size="small" @click="handleEditRule(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteRule(scope.row.id)">删除</el-button>
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

    <!-- 新增/编辑规则对话框 -->
    <el-dialog
      :title="isEdit ? '编辑积分规则' : '新增积分规则'"
      v-model="dialogVisible"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="ruleFormRef"
        :model="ruleForm"
        :rules="ruleRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="ruleForm.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="ruleForm.ruleType" placeholder="请选择规则类型">
            <el-option :value="1" label="预约完成" />
            <el-option :value="2" label="评价" />
            <el-option :value="3" label="签到" />
            <el-option :value="4" label="邀请" />
            <el-option :value="5" label="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分值" prop="pointValue">
          <el-input-number v-model="ruleForm.pointValue" :min="1" :max="10000" placeholder="请输入积分值" />
        </el-form-item>
        <el-form-item label="规则描述" prop="ruleDescription">
          <el-input
            v-model="ruleForm.ruleDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入规则描述"
          />
        </el-form-item>
        <el-form-item label="有效期(天)" prop="validityDays">
          <el-input-number v-model="ruleForm.validityDays" :min="1" :max="3650" placeholder="请输入有效期" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="ruleForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRuleForm" :loading="submitLoading">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { 
  getPointRulesByPage, 
  saveOrUpdatePointRule, 
  updatePointRuleStatus, 
  deletePointRule 
} from '@/api/points'
import { PointRule, PointRuleDTO } from '@/types/points'

// 列表数据相关
const rulesList = ref<PointRule[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedRules = ref<PointRule[]>([])

// 对话框相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const ruleFormRef = ref<FormInstance>()
const submitLoading = ref(false)

// 表单数据
const defaultRuleForm: PointRuleDTO = {
  ruleType: 1,
  ruleName: '',
  pointValue: 10,
  ruleDescription: '',
  validityDays: 365,
  status: 1
}

const ruleForm = reactive<PointRuleDTO>({ ...defaultRuleForm })

// 表单验证规则
const ruleRules = reactive<FormRules>({
  ruleName: [
    { required: true, message: '请输入规则名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  ruleType: [
    { required: true, message: '请选择规则类型', trigger: 'change' }
  ],
  pointValue: [
    { required: true, message: '请输入积分值', trigger: 'blur' }
  ],
  validityDays: [
    { required: true, message: '请输入有效期', trigger: 'blur' }
  ]
})

// 获取积分规则列表
const fetchRulesList = async () => {
  loading.value = true
  try {
    const { data } = await getPointRulesByPage({
      page: currentPage.value,
      size: pageSize.value
    })
    rulesList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取积分规则列表失败', error)
    ElMessage.error('获取积分规则列表失败')
  } finally {
    loading.value = false
  }
}

// 处理表格选择变化
const handleSelectionChange = (selection: PointRule[]) => {
  selectedRules.value = selection
}

// 批量启用规则
const handleBatchEnable = async () => {
  if (selectedRules.value.length === 0) return
  
  try {
    const promises = selectedRules.value.map(rule => 
      updatePointRuleStatus(rule.id, 1)
    )
    await Promise.all(promises)
    ElMessage.success('批量启用成功')
    fetchRulesList()
  } catch (error) {
    console.error('批量启用失败', error)
    ElMessage.error('批量启用失败')
  }
}

// 批量禁用规则
const handleBatchDisable = async () => {
  if (selectedRules.value.length === 0) return
  
  try {
    const promises = selectedRules.value.map(rule => 
      updatePointRuleStatus(rule.id, 0)
    )
    await Promise.all(promises)
    ElMessage.success('批量禁用成功')
    fetchRulesList()
  } catch (error) {
    console.error('批量禁用失败', error)
    ElMessage.error('批量禁用失败')
  }
}

// 处理规则状态变化
const handleStatusChange = async (id: number, status: number) => {
  try {
    await updatePointRuleStatus(id, status)
    ElMessage.success(`${status === 1 ? '启用' : '禁用'}成功`)
  } catch (error) {
    console.error('更新状态失败', error)
    ElMessage.error('更新状态失败')
    // 刷新列表，恢复状态
    fetchRulesList()
  }
}

// 新增规则
const handleAddRule = () => {
  isEdit.value = false
  Object.assign(ruleForm, defaultRuleForm)
  dialogVisible.value = true
}

// 编辑规则
const handleEditRule = (row: PointRule) => {
  isEdit.value = true
  Object.assign(ruleForm, {
    id: row.id,
    ruleName: row.ruleName,
    ruleType: row.ruleType,
    pointValue: row.pointValue,
    ruleDescription: row.ruleDescription,
    validityDays: row.validityDays,
    status: row.status
  })
  dialogVisible.value = true
}

// 删除规则
const handleDeleteRule = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该积分规则吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deletePointRule(id)
    ElMessage.success('删除成功')
    fetchRulesList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const submitRuleForm = async () => {
  if (!ruleFormRef.value) return
  
  await ruleFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await saveOrUpdatePointRule(ruleForm)
        ElMessage.success(`${isEdit.value ? '更新' : '添加'}成功`)
        dialogVisible.value = false
        fetchRulesList()
      } catch (error) {
        console.error(`${isEdit.value ? '更新' : '添加'}失败`, error)
        ElMessage.error(`${isEdit.value ? '更新' : '添加'}失败`)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 获取规则类型标签类型
const getRuleTypeTag = (type: number) => {
  const map: Record<number, string> = {
    1: 'success',
    2: 'info',
    3: 'warning',
    4: 'danger',
    5: ''
  }
  return map[type] || ''
}

// 获取规则类型名称
const getRuleTypeName = (type: number) => {
  const map: Record<number, string> = {
    1: '预约完成',
    2: '评价',
    3: '签到',
    4: '邀请',
    5: '其他'
  }
  return map[type] || '未知'
}

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchRulesList()
}

// 处理当前页变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchRulesList()
}

// 页面加载时获取数据
onMounted(() => {
  fetchRulesList()
})

// 监听对话框关闭，重置表单
watch(dialogVisible, (val) => {
  if (!val && ruleFormRef.value) {
    ruleFormRef.value.resetFields()
  }
})
</script>

<style scoped>
.point-rule-container {
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
</style> 