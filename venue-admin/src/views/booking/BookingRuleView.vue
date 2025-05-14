<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus as IconPlus,
  Edit as IconEdit,
  Delete as IconDelete,
  Refresh as IconRefresh,
  InfoFilled as IconInfoFilled
} from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { 
  getAllBookingRules, 
  createBookingRule, 
  updateBookingRule, 
  deleteBookingRule,
  pageSpecialDateRules,
  createSpecialDateRule,
  updateSpecialDateRule,
  deleteSpecialDateRule
} from '@/api/booking'
import type { BookingRule, SpecialDateRule, BookingRuleRequest, SpecialDateRuleRequest } from '@/types/booking'

// 表格加载状态
const loadingRules = ref(false)
const loadingSpecialDates = ref(false)

// 规则类型选项
const ruleTypeOptions = [
  { value: 'booking_limit', label: '预约限制', description: '限制预约数量、时间等' },
  { value: 'booking_time', label: '预约时间', description: '设置可预约时段、提前预约天数等' },
  { value: 'cancel_rule', label: '取消规则', description: '设置取消预约的条件和限制' },
  { value: 'member_privilege', label: '会员特权', description: '设置会员专属权益' }
]

// 添加预设的规则键选项
const ruleKeyOptions = reactive({
  booking_limit: [
    { value: 'max_daily_bookings', label: '每日最大预约数', description: '用户每天最多可预约的次数', placeholder: '如: 3' },
    { value: 'max_weekly_bookings', label: '每周最大预约数', description: '用户每周最多可预约的次数', placeholder: '如: 10' },
    { value: 'max_duration', label: '最大预约时长(小时)', description: '单次预约的最大时长', placeholder: '如: 2' }
  ],
  booking_time: [
    { value: 'advance_days', label: '提前预约天数', description: '可提前多少天预约', placeholder: '如: 7' },
    { value: 'min_start_time', label: '最早开始时间', description: '一天中最早的预约时间', placeholder: '如: 08:00' },
    { value: 'max_end_time', label: '最晚结束时间', description: '一天中最晚的预约时间', placeholder: '如: 22:00' }
  ],
  cancel_rule: [
    { value: 'cancel_before_hours', label: '提前取消小时数', description: '需提前多少小时取消预约', placeholder: '如: 24' },
    { value: 'refund_percentage', label: '退款比例', description: '取消预约的退款百分比', placeholder: '如: 80（表示退还80%）' },
    { value: 'max_cancellations', label: '最大取消次数', description: '每月允许取消的最大次数', placeholder: '如: 3' }
  ],
  member_privilege: [
    { value: 'discount_rate', label: '折扣率', description: '会员预约折扣', placeholder: '如: 0.9（表示9折）' },
    { value: 'advance_booking_days', label: '提前预约额外天数', description: '会员额外可提前预约的天数', placeholder: '如: 3' },
    { value: 'priority_booking', label: '优先预约', description: '是否允许会员优先预约', placeholder: '如: true/false' }
  ]
})

// 当前选择的规则类型对应的规则键选项
const currentRuleKeyOptions = ref([])

// 监听规则类型变化，更新规则键选项
const handleRuleTypeChange = (type: string) => {
  currentRuleKeyOptions.value = ruleKeyOptions[type] || []
  
  // 如果之前没有选择规则键或者改变了规则类型，则清空规则键和规则值
  if (!ruleForm.ruleKey || !currentRuleKeyOptions.value.some(option => option.value === ruleForm.ruleKey)) {
    ruleForm.ruleKey = ''
    ruleForm.ruleValue = ''
  }
}

// 获取当前选择的规则键的描述和占位符
const getCurrentKeyInfo = () => {
  if (!ruleForm.ruleKey) return { description: '', placeholder: '' }
  
  const keyOption = currentRuleKeyOptions.value.find(option => option.value === ruleForm.ruleKey)
  return keyOption || { description: '', placeholder: '' }
}

// 获取规则值表单提示信息
const getRuleValuePlaceholder = computed(() => {
  const keyInfo = getCurrentKeyInfo()
  return keyInfo.placeholder || '请输入规则值'
})

// 获取规则值表单帮助信息
const getRuleValueDescription = computed(() => {
  const keyInfo = getCurrentKeyInfo()
  return keyInfo.description || ''
})

// 全部规则数据
const rulesData = ref<BookingRule[]>([])

// 特殊日期规则数据和分页
const specialDateRules = ref<SpecialDateRule[]>([])
const specialDatePagination = reactive({
  page: 1,
  size: 10,
  total: 0
})
const specialDateQueryParams = reactive({
  status: undefined as number | undefined,
  startDate: undefined as string | undefined,
  endDate: undefined as string | undefined
})

// 规则表单相关
const ruleFormVisible = ref(false)
const ruleFormLoading = ref(false)
const ruleFormType = ref('add')
const ruleForm = reactive<BookingRuleRequest>({
  ruleType: '',
  ruleKey: '',
  ruleValue: '',
  description: '',
  status: 1
})
const ruleEditId = ref<number>()
const ruleFormRef = ref<FormInstance>()
const ruleFormRules = reactive<FormRules>({
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  ruleKey: [{ required: true, message: '请输入规则键', trigger: 'blur' }],
  ruleValue: [{ required: true, message: '请输入规则值', trigger: 'blur' }]
})

// 特殊日期表单相关
const specialDateFormVisible = ref(false)
const specialDateFormLoading = ref(false)
const specialDateFormType = ref('add')
const specialDateForm = reactive<SpecialDateRuleRequest>({
  specialDate: '',
  description: '',
  priceRate: 1,
  status: 1
})
const specialDateEditId = ref<number>()
const specialDateFormRef = ref<FormInstance>()
const specialDateFormRules = reactive<FormRules>({
  specialDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  priceRate: [
    { required: true, message: '请输入价格系数', trigger: 'blur' },
    { type: 'number', min: 0.1, max: 10, message: '价格系数范围为0.1-10', trigger: 'blur' }
  ]
})

// 加载所有预约规则
const loadBookingRules = async () => {
  loadingRules.value = true
  try {
    const response = await getAllBookingRules()
    rulesData.value = response.data
  } catch (error) {
    console.error('加载预约规则失败', error)
    ElMessage.error('加载预约规则失败')
  } finally {
    loadingRules.value = false
  }
}

// 加载特殊日期规则
const loadSpecialDateRules = async () => {
  loadingSpecialDates.value = true
  try {
    const params = {
      page: specialDatePagination.page,
      size: specialDatePagination.size,
      status: specialDateQueryParams.status,
      startDate: specialDateQueryParams.startDate,
      endDate: specialDateQueryParams.endDate
    }
    
    const response = await pageSpecialDateRules(params)
    specialDateRules.value = response.data.records
    specialDatePagination.total = response.data.total
  } catch (error) {
    console.error('加载特殊日期规则失败', error)
    ElMessage.error('加载特殊日期规则失败')
  } finally {
    loadingSpecialDates.value = false
  }
}

// 打开添加预约规则对话框
const openAddRuleDialog = () => {
  ruleFormType.value = 'add'
  ruleEditId.value = undefined
  ruleForm.ruleType = ''
  ruleForm.ruleKey = ''
  ruleForm.ruleValue = ''
  ruleForm.description = ''
  ruleForm.status = 1
  ruleFormVisible.value = true
  currentRuleKeyOptions.value = [] // 清空规则键选项
}

// 打开编辑预约规则对话框
const openEditRuleDialog = (rule: BookingRule) => {
  ruleFormType.value = 'edit'
  ruleEditId.value = rule.id
  ruleForm.ruleType = rule.ruleType
  ruleForm.ruleKey = rule.ruleKey
  ruleForm.ruleValue = rule.ruleValue
  ruleForm.description = rule.description
  ruleForm.status = rule.status
  ruleFormVisible.value = true
  handleRuleTypeChange(rule.ruleType) // 初始化规则键选项
}

// 提交预约规则表单
const submitRuleForm = async () => {
  if (!ruleFormRef.value) return
  
  await ruleFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    ruleFormLoading.value = true
    try {
      if (ruleFormType.value === 'add') {
        await createBookingRule(ruleForm)
        ElMessage.success('添加规则成功')
      } else if (ruleEditId.value) {
        await updateBookingRule(ruleEditId.value, ruleForm)
        ElMessage.success('更新规则成功')
      }
      
      ruleFormVisible.value = false
      loadBookingRules()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    } finally {
      ruleFormLoading.value = false
    }
  })
}

// 删除预约规则
const handleDeleteRule = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该规则吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteBookingRule(id)
    ElMessage.success('删除成功')
    loadBookingRules()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 打开添加特殊日期规则对话框
const openAddSpecialDateDialog = () => {
  specialDateFormType.value = 'add'
  specialDateEditId.value = undefined
  specialDateForm.specialDate = ''
  specialDateForm.description = ''
  specialDateForm.priceRate = 1
  specialDateForm.status = 1
  specialDateFormVisible.value = true
}

// 打开编辑特殊日期规则对话框
const openEditSpecialDateDialog = (rule: SpecialDateRule) => {
  specialDateFormType.value = 'edit'
  specialDateEditId.value = rule.id
  specialDateForm.specialDate = rule.specialDate
  specialDateForm.description = rule.description
  specialDateForm.priceRate = rule.priceRate
  specialDateForm.status = rule.status
  specialDateFormVisible.value = true
}

// 提交特殊日期规则表单
const submitSpecialDateForm = async () => {
  if (!specialDateFormRef.value) return
  
  await specialDateFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    specialDateFormLoading.value = true
    try {
      if (specialDateFormType.value === 'add') {
        await createSpecialDateRule(specialDateForm)
        ElMessage.success('添加特殊日期规则成功')
      } else if (specialDateEditId.value) {
        await updateSpecialDateRule(specialDateEditId.value, specialDateForm)
        ElMessage.success('更新特殊日期规则成功')
      }
      
      specialDateFormVisible.value = false
      loadSpecialDateRules()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    } finally {
      specialDateFormLoading.value = false
    }
  })
}

// 删除特殊日期规则
const handleDeleteSpecialDate = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该特殊日期规则吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteSpecialDateRule(id)
    ElMessage.success('删除成功')
    loadSpecialDateRules()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 特殊日期分页变化
const handleSpecialDatePageChange = (page: number) => {
  specialDatePagination.page = page
  loadSpecialDateRules()
}

// 特殊日期每页大小变化
const handleSpecialDateSizeChange = (size: number) => {
  specialDatePagination.size = size
  specialDatePagination.page = 1
  loadSpecialDateRules()
}

// 查询特殊日期规则
const handleSearchSpecialDates = () => {
  specialDatePagination.page = 1
  loadSpecialDateRules()
}

// 重置特殊日期查询条件
const resetSpecialDateQuery = () => {
  specialDateQueryParams.status = undefined
  specialDateQueryParams.startDate = undefined
  specialDateQueryParams.endDate = undefined
  specialDatePagination.page = 1
  loadSpecialDateRules()
}

// 获取规则类型的显示文本
const getRuleTypeLabel = (type: string) => {
  const option = ruleTypeOptions.find(opt => opt.value === type)
  return option ? option.label : type
}

// 获取规则键的描述
const getKeyDescription = (type: string, key: string) => {
  const keyOptions = ruleKeyOptions[type]
  if (keyOptions) {
    const keyOption = keyOptions.find(option => option.value === key)
    return keyOption?.description || ''
  }
  return ''
}

// 获取规则键的标签
const getKeyLabel = (type: string, key: string) => {
  const keyOptions = ruleKeyOptions[type]
  if (keyOptions) {
    const keyOption = keyOptions.find(option => option.value === key)
    return keyOption?.label || key
  }
  return key
}

// 初始化
onMounted(() => {
  loadBookingRules()
  loadSpecialDateRules()
})
</script>

<template>
  <div class="booking-rule-container">
    <el-tabs type="border-card">
      <!-- 预约规则标签页 -->
      <el-tab-pane label="预约规则">
        <el-row :gutter="20">
          <el-col :span="18">
            <el-card class="box-card">
              <template #header>
                <div class="card-header">
                  <span>预约规则设置</span>
                  <el-button type="primary" @click="openAddRuleDialog" :icon="IconPlus">添加规则</el-button>
                </div>
              </template>
              
              <el-table
                v-loading="loadingRules"
                :data="rulesData"
                border
                style="width: 100%"
                :stripe="true"
              >
                <el-table-column prop="id" label="ID" width="60" />
                <el-table-column prop="ruleType" label="规则类型" width="120">
                  <template #default="{ row }">
                    {{ getRuleTypeLabel(row.ruleType) }}
                  </template>
                </el-table-column>
                <el-table-column prop="ruleKey" label="规则键" width="150">
                  <template #default="{ row }">
                    <el-tooltip
                      :content="getKeyDescription(row.ruleType, row.ruleKey)"
                      placement="top"
                      :disabled="!getKeyDescription(row.ruleType, row.ruleKey)"
                    >
                      <span>{{ getKeyLabel(row.ruleType, row.ruleKey) || row.ruleKey }}</span>
                    </el-tooltip>
                  </template>
                </el-table-column>
                <el-table-column prop="ruleValue" label="规则值" min-width="120" />
                <el-table-column prop="description" label="描述" min-width="200" />
                <el-table-column prop="status" label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 1 ? 'success' : 'info'">
                      {{ row.status === 1 ? '启用' : '禁用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="创建时间" width="170" />
                <el-table-column fixed="right" label="操作" width="120">
                  <template #default="{ row }">
                    <el-button type="primary" link @click="openEditRuleDialog(row)" :icon="IconEdit">
                      编辑
                    </el-button>
                    <el-button type="danger" link @click="handleDeleteRule(row.id)" :icon="IconDelete">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="guide-card">
              <template #header>
                <div class="card-header">
                  <span>规则设置指南</span>
                </div>
              </template>
              <div class="guide-content">
                <h4>预约规则说明</h4>
                <p>预约规则用于控制用户的预约行为，包括以下几种类型：</p>
                <ul>
                  <li v-for="option in ruleTypeOptions" :key="option.value">
                    <strong>{{ option.label }}：</strong>{{ option.description }}
                  </li>
                </ul>
                
                <h4>如何设置规则</h4>
                <ol>
                  <li>点击"添加规则"按钮</li>
                  <li>选择规则类型</li>
                  <li>选择具体的规则键</li>
                  <li>设置规则值</li>
                  <li>添加规则描述（用户可见）</li>
                  <li>设置规则状态</li>
                </ol>
                
                <h4>常见规则示例</h4>
                <ul>
                  <li>提前预约天数：7（用户可提前7天预约）</li>
                  <li>每日最大预约数：3（用户每天最多预约3次）</li>
                  <li>取消预约限制：24（需提前24小时取消）</li>
                </ul>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
      
      <!-- 特殊日期规则标签页 -->
      <el-tab-pane label="特殊日期规则">
        <el-row :gutter="20">
          <el-col :span="18">
            <el-card class="box-card">
              <template #header>
                <div class="card-header">
                  <span>特殊日期规则设置</span>
                  <el-button type="primary" @click="openAddSpecialDateDialog" :icon="IconPlus">添加特殊日期</el-button>
                </div>
              </template>
              
              <!-- 搜索条件 -->
              <div class="search-bar">
                <el-form :inline="true" :model="specialDateQueryParams">
                  <el-form-item label="状态">
                    <el-select v-model="specialDateQueryParams.status" placeholder="选择状态" clearable>
                      <el-option label="可预约" :value="1" />
                      <el-option label="不可预约" :value="0" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="日期范围">
                    <el-date-picker
                      v-model="specialDateQueryParams.startDate"
                      type="date"
                      placeholder="开始日期"
                      format="YYYY-MM-DD"
                      value-format="YYYY-MM-DD"
                    />
                  </el-form-item>
                  <el-form-item label="至">
                    <el-date-picker
                      v-model="specialDateQueryParams.endDate"
                      type="date"
                      placeholder="结束日期"
                      format="YYYY-MM-DD"
                      value-format="YYYY-MM-DD"
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="handleSearchSpecialDates">查询</el-button>
                    <el-button @click="resetSpecialDateQuery" :icon="IconRefresh">重置</el-button>
                  </el-form-item>
                </el-form>
              </div>
              
              <el-table
                v-loading="loadingSpecialDates"
                :data="specialDateRules"
                border
                style="width: 100%"
                :stripe="true"
              >
                <el-table-column prop="id" label="ID" width="60" />
                <el-table-column prop="specialDate" label="特殊日期" width="120" />
                <el-table-column prop="description" label="描述" min-width="200" />
                <el-table-column prop="priceRate" label="价格系数" width="100">
                  <template #default="{ row }">
                    {{ row.priceRate.toFixed(2) }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                      {{ row.status === 1 ? '可预约' : '不可预约' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="创建时间" width="170" />
                <el-table-column fixed="right" label="操作" width="120">
                  <template #default="{ row }">
                    <el-button type="primary" link @click="openEditSpecialDateDialog(row)" :icon="IconEdit">
                      编辑
                    </el-button>
                    <el-button type="danger" link @click="handleDeleteSpecialDate(row.id)" :icon="IconDelete">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <!-- 分页 -->
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="specialDatePagination.page"
                  v-model:page-size="specialDatePagination.size"
                  :page-sizes="[10, 20, 50, 100]"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="specialDatePagination.total"
                  @size-change="handleSpecialDateSizeChange"
                  @current-change="handleSpecialDatePageChange"
                />
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="guide-card">
              <template #header>
                <div class="card-header">
                  <span>特殊日期规则指南</span>
                </div>
              </template>
              <div class="guide-content">
                <h4>什么是特殊日期规则？</h4>
                <p>特殊日期规则用于设置节假日、活动日等特殊日期的预约规则，可以设置价格系数或禁止预约。</p>
                
                <h4>价格系数说明</h4>
                <ul>
                  <li><strong>大于1</strong>：表示加价，如1.5表示按原价的1.5倍收费</li>
                  <li><strong>等于1</strong>：表示不变，维持原价</li>
                  <li><strong>小于1</strong>：表示折扣，如0.8表示按原价的8折收费</li>
                </ul>
                
                <h4>常见用途</h4>
                <ul>
                  <li>节假日设置价格上浮（如国庆、春节）</li>
                  <li>工作日设置折扣价格</li>
                  <li>场馆维护日设置为不可预约</li>
                  <li>特殊活动日设置为不可预约</li>
                </ul>
                
                <h4>示例</h4>
                <p>设置2023-10-01为国庆节，价格系数为1.5倍，表示国庆节当天预约价格为平时的1.5倍。</p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 预约规则表单对话框 -->
    <el-dialog
      v-model="ruleFormVisible"
      :title="ruleFormType === 'add' ? '添加预约规则' : '编辑预约规则'"
      width="600px"
      @close="ruleFormVisible = false"
    >
      <el-alert
        v-if="ruleFormType === 'add'"
        type="info"
        title="提示：预约规则用于控制用户预约行为，请根据业务需求选择合适的规则"
        description="选择规则类型后，系统将提供相应的规则选项，您只需设置规则值即可"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <el-form
        ref="ruleFormRef"
        :model="ruleForm"
        :rules="ruleFormRules"
        label-width="100px"
        class="rule-form"
      >
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="ruleForm.ruleType" placeholder="请选择规则类型" style="width: 100%" @change="handleRuleTypeChange">
            <el-option 
              v-for="option in ruleTypeOptions" 
              :key="option.value" 
              :label="option.label" 
              :value="option.value" 
            >
              <div>
                <span>{{ option.label }}</span>
                <div class="option-description">{{ option.description }}</div>
              </div>
            </el-option>
          </el-select>
          <div class="form-help-text" v-if="ruleForm.ruleType">
            选择合适的规则类型，系统会显示对应的规则选项
          </div>
        </el-form-item>
        <el-form-item label="规则键" prop="ruleKey">
          <el-select v-model="ruleForm.ruleKey" placeholder="请选择规则键" style="width: 100%">
            <el-option 
              v-for="option in currentRuleKeyOptions" 
              :key="option.value" 
              :label="option.label" 
              :value="option.value" 
            >
              <div>
                <span>{{ option.label }}</span>
                <div class="option-description">{{ option.description }}</div>
              </div>
            </el-option>
          </el-select>
          <div class="form-help-text" v-if="ruleForm.ruleKey && getRuleValueDescription">
            <el-icon><IconInfoFilled /></el-icon>
            <span>{{ getRuleValueDescription }}</span>
          </div>
        </el-form-item>
        <el-form-item label="规则值" prop="ruleValue">
          <el-input v-model="ruleForm.ruleValue" :placeholder="getRuleValuePlaceholder" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="ruleForm.description" type="textarea" :rows="3" placeholder="请输入规则描述（会显示给用户看）" />
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
          <el-button @click="ruleFormVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRuleForm" :loading="ruleFormLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 特殊日期规则表单对话框 -->
    <el-dialog
      v-model="specialDateFormVisible"
      :title="specialDateFormType === 'add' ? '添加特殊日期规则' : '编辑特殊日期规则'"
      width="600px"
      @close="specialDateFormVisible = false"
    >
      <el-alert
        v-if="specialDateFormType === 'add'"
        type="info"
        title="提示：特殊日期规则用于设置节假日、活动日等特殊日期的预约规则"
        description="您可以设置价格系数（大于1表示加价，小于1表示折扣）或禁止预约"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <el-form
        ref="specialDateFormRef"
        :model="specialDateForm"
        :rules="specialDateFormRules"
        label-width="100px"
        class="special-date-form"
      >
        <el-form-item label="特殊日期" prop="specialDate">
          <el-date-picker
            v-model="specialDateForm.specialDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="specialDateForm.description" placeholder="节假日描述，如春节、国庆节等" />
        </el-form-item>
        <el-form-item label="价格系数" prop="priceRate">
          <el-input-number
            v-model="specialDateForm.priceRate"
            :min="0.1"
            :max="10"
            :precision="2"
            :step="0.1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="specialDateForm.status">
            <el-radio :label="1">可预约</el-radio>
            <el-radio :label="0">不可预约</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="specialDateFormVisible = false">取消</el-button>
          <el-button type="primary" @click="submitSpecialDateForm" :loading="specialDateFormLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.booking-rule-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.rule-form,
.special-date-form {
  padding: 10px 20px;
}

.form-help-text {
  margin-top: 5px;
  margin-left: 10px;
  font-size: 0.8em;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 5px;
}

.option-description {
  color: #909399;
  font-size: 0.85em;
  margin-top: 2px;
}

.guide-card {
  height: 100%;
}

.guide-content {
  padding: 10px;
}

.guide-content h4 {
  margin-top: 15px;
  margin-bottom: 8px;
  color: #303133;
}

.guide-content p, .guide-content ul, .guide-content ol {
  margin-bottom: 12px;
  color: #606266;
}

.guide-content ul, .guide-content ol {
  padding-left: 20px;
}

.guide-content li {
  margin-bottom: 5px;
}
</style> 