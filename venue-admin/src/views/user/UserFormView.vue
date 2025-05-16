<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElDatePicker } from 'element-plus'
import { ArrowLeft, User } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getUserDetail, addUser, updateUser } from '@/api/user'
import type { UserInfo, UserAddParams, UserEditParams } from '@/types/user'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => route.meta.type === 'edit')
const userId = computed(() => isEdit.value ? parseInt(route.params.id as string) : 0)

// 表单ref
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive<any>({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  nickname: '',
  avatar: '',
  gender: '',
  birthday: null,
  address: '',
  status: 1,
  role: 1,
  newPassword: '',
  confirmNewPassword: ''
})

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)

// 表单验证规则
const formRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度必须在4-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: !isEdit.value, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: !isEdit.value, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== formData.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  confirmNewPassword: [
    {
      validator: (rule, value, callback) => {
        if (formData.newPassword && value !== formData.newPassword) {
          callback(new Error('两次输入的新密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 获取用户详情（编辑模式）
const fetchUserDetail = async () => {
  if (!isEdit.value || !userId.value) return
  
  loading.value = true
  try {
    const res = await getUserDetail(userId.value)
    const user = res.data
    
    // 填充表单数据
    formData.username = user.username
    formData.email = user.email
    formData.phone = user.phone
    formData.nickname = user.nickname
    formData.avatar = user.avatar
    formData.gender = user.gender
    formData.birthday = user.birthday
    formData.address = user.address
    formData.status = user.status
    formData.role = user.role
    
    // 密码相关字段在编辑模式下不填充
    formData.password = ''
    formData.confirmPassword = ''
    formData.newPassword = ''
    formData.confirmNewPassword = ''
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  } finally {
    loading.value = false
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return false
    }
    
    submitLoading.value = true
    try {
      if (isEdit.value) {
        // 编辑用户
        const params: UserEditParams = {
          id: userId.value,
          email: formData.email,
          phone: formData.phone,
          nickname: formData.nickname,
          avatar: formData.avatar,
          gender: formData.gender,
          birthday: formData.birthday,
          address: formData.address,
          status: formData.status,
          role: formData.role
        }
        
        // 如果有新密码，添加到参数中
        if (formData.newPassword) {
          params.newPassword = formData.newPassword
        }
        
        await updateUser(params)
        ElMessage.success('编辑用户成功')
      } else {
        // 添加用户
        const params: UserAddParams = {
          username: formData.username,
          password: formData.password,
          confirmPassword: formData.confirmPassword,
          email: formData.email,
          phone: formData.phone,
          nickname: formData.nickname,
          avatar: formData.avatar,
          gender: formData.gender,
          birthday: formData.birthday,
          address: formData.address,
          status: formData.status,
          role: formData.role
        }
        
        await addUser(params)
        ElMessage.success('添加用户成功')
      }
      
      // 返回用户列表
      router.push('/home/users')
    } catch (error: any) {
      console.error(isEdit.value ? '编辑用户失败:' : '添加用户失败:', error)
      ElMessage.error(error?.response?.data?.message || (isEdit.value ? '编辑用户失败' : '添加用户失败'))
    } finally {
      submitLoading.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
  
  // 如果是编辑模式，重新获取用户信息
  if (isEdit.value) {
    fetchUserDetail()
  } else {
    // 添加模式，设置默认值
    formData.status = 1
    formData.role = 1
  }
}

// 返回用户列表
const handleBack = () => {
  router.push('/home/users')
}

// 组件挂载时获取用户详情（编辑模式）
onMounted(() => {
  fetchUserDetail()
})
</script>

<template>
  <div class="user-form-container" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button link :icon="ArrowLeft" @click="handleBack">返回</el-button>
            <span class="header-title">{{ isEdit ? '编辑用户' : '新增用户' }}</span>
          </div>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        class="user-form"
      >
        <!-- 基本信息 -->
        <div class="form-section">
          <h3 class="section-title">基本信息</h3>
          
          <el-form-item label="用户名" prop="username">
            <el-input v-model="formData.username" placeholder="请输入用户名" :disabled="isEdit" />
          </el-form-item>
          
          <template v-if="!isEdit">
            <el-form-item label="密码" prop="password">
              <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
            
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="formData.confirmPassword" type="password" placeholder="请确认密码" show-password />
            </el-form-item>
          </template>
          
          <template v-else>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="formData.newPassword" type="password" placeholder="请输入新密码（不修改则留空）" show-password />
            </el-form-item>
            
            <el-form-item label="确认新密码" prop="confirmNewPassword">
              <el-input v-model="formData.confirmNewPassword" type="password" placeholder="请确认新密码" show-password />
            </el-form-item>
          </template>
          
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" placeholder="请输入邮箱" />
          </el-form-item>
          
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="formData.phone" placeholder="请输入手机号" />
          </el-form-item>
          
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="formData.nickname" placeholder="请输入昵称" />
          </el-form-item>
        </div>
        
        <!-- 用户资料 -->
        <div class="form-section">
          <h3 class="section-title">用户资料</h3>
          
          <el-form-item label="头像" prop="avatar">
            <el-input v-model="formData.avatar" placeholder="请输入头像URL" />
          </el-form-item>
          
          <el-form-item label="性别" prop="gender">
            <el-select v-model="formData.gender" placeholder="请选择性别" clearable>
              <el-option label="男" value="M" />
              <el-option label="女" value="F" />
              <el-option label="保密" value="O" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="生日" prop="birthday">
            <el-date-picker
              v-model="formData.birthday"
              type="date"
              placeholder="请选择生日"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          
          <el-form-item label="地址" prop="address">
            <el-input v-model="formData.address" placeholder="请输入地址" />
          </el-form-item>
        </div>
        
        <!-- 账户状态 -->
        <div class="form-section">
          <h3 class="section-title">账户状态</h3>
          
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :label="1">正常</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="角色" prop="role">
            <el-radio-group v-model="formData.role">
              <el-radio :label="1">普通用户</el-radio>
              <el-radio :label="2">管理员</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
        
        <!-- 表单操作 -->
        <div class="form-actions">
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">提交</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.user-form-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-title {
  font-size: 16px;
  font-weight: bold;
  margin-left: 8px;
}

.user-form {
  padding: 20px 0;
  max-width: 600px;
  margin: 0 auto;
}

.form-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #ebeef5;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #303133;
}

.form-actions {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style> 