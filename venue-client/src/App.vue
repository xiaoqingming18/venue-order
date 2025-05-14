<script setup lang="ts">
import { RouterView } from 'vue-router'
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

// 获取用户仓库实例
const userStore = useUserStore()

onMounted(async () => {
  // 初始化用户状态（恢复登录状态并获取用户信息）
  await userStore.initUserState()
  console.log('用户状态初始化完成')

  // 添加全局的未捕获错误处理
  window.addEventListener('unhandledrejection', (event) => {
    console.error('未捕获的Promise错误:', event.reason)
    if (event.reason.response) {
      console.error('错误响应:', {
        status: event.reason.response.status,
        statusText: event.reason.response.statusText,
        data: event.reason.response.data
      })
    }
  })
})
</script>

<template>
  <RouterView />
</template>

<style scoped></style>
