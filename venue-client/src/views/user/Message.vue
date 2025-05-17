<template>
  <div class="message-container">
    <van-nav-bar 
      title="我的消息" 
      left-arrow
      @click-left="goBack"
      fixed
    >
      <template #right>
        <van-button 
          type="text" 
          size="small" 
          class="read-all-btn" 
          @click="handleMarkAllAsRead"
          v-if="hasUnread"
        >
          全部标为已读
        </van-button>
      </template>
    </van-nav-bar>
    
    <div class="message-list">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <van-empty v-if="messageList.length === 0" description="暂无消息记录" />
          
          <van-swipe-cell
            v-for="item in messageList"
            :key="item.id"
            :before-close="beforeClose"
          >
            <van-cell
              :class="{ 'unread': item.isRead === 0 }"
              :title="item.title"
              :label="item.createdAt"
              @click="handleReadMessage(item)"
            >
              <template #value>
                <span class="message-type">{{ item.typeName }}</span>
              </template>
            </van-cell>
            <template #right>
              <van-button
                square
                text="删除"
                type="danger"
                class="delete-button"
                @click="() => showDeleteDialog(item.id)"
              />
            </template>
          </van-swipe-cell>
        </van-list>
      </van-pull-refresh>
    </div>
    
    <van-dialog
      v-model:show="showDialog"
      title="消息详情"
      class="message-dialog"
      show-cancel-button
      confirmButtonText="关闭"
      :showConfirmButton="false"
    >
      <div class="message-detail" v-if="currentMessage">
        <h3 class="message-title">{{ currentMessage.title }}</h3>
        <div class="message-info">
          <span class="message-type-tag">{{ currentMessage.typeName }}</span>
          <span class="message-time">{{ currentMessage.createdAt }}</span>
        </div>
        <div class="message-content">{{ currentMessage.content }}</div>
        
        <div v-if="currentMessage.type === 3" class="message-action">
          <van-button 
            type="primary" 
            size="small" 
            block 
            @click="goToFeedbackDetail(currentMessage.relatedId)"
          >
            查看反馈详情
          </van-button>
        </div>
      </div>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showFailToast, SwipeCell } from 'vant'
import { getUserMessages, markAsRead, markAllAsRead, deleteMessage } from '@/api/message.js'
import type { UserMessage } from '@/types/feedback'

const router = useRouter()
const messageList = ref<UserMessage[]>([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const showDialog = ref(false)
const currentMessage = ref<UserMessage | null>(null)

// 是否有未读消息
const hasUnread = computed(() => {
  return messageList.value.some(item => item.isRead === 0)
})

// 加载消息列表
const onLoad = async () => {
  try {
    const { data } = await getUserMessages(pageNum.value, pageSize.value)
    
    // 下拉刷新时清空列表
    if (refreshing.value) {
      messageList.value = []
      refreshing.value = false
    }
    
    if (data.records && data.records.length > 0) {
      messageList.value.push(...data.records)
      pageNum.value++
    }
    
    loading.value = false
    if (messageList.value.length >= data.total) {
      finished.value = true
    }
  } catch (error) {
    loading.value = false
    showFailToast('加载失败，请重试')
  }
}

// 下拉刷新
const onRefresh = () => {
  finished.value = false
  loading.value = true
  pageNum.value = 1
  onLoad()
}

// 阅读消息
const handleReadMessage = async (message: UserMessage) => {
  currentMessage.value = message
  showDialog.value = true
  
  // 如果消息未读，标记为已读
  if (message.isRead === 0) {
    try {
      await markAsRead(message.id)
      // 更新本地消息状态
      const index = messageList.value.findIndex(item => item.id === message.id)
      if (index !== -1) {
        messageList.value[index].isRead = 1
      }
    } catch (error) {
      // 忽略错误
    }
  }
}

// 标记全部已读
const handleMarkAllAsRead = async () => {
  try {
    await markAllAsRead()
    showSuccessToast('全部标为已读')
    // 更新本地消息状态
    messageList.value = messageList.value.map(item => ({ ...item, isRead: 1 }))
  } catch (error) {
    showFailToast('操作失败，请重试')
  }
}

// 显示删除确认对话框
const showDeleteDialog = (id: number) => {
  SwipeCell.close('all')
  
  if (window.confirm('确定要删除此消息吗？')) {
    handleDeleteMessage(id)
  }
}

// 删除消息
const handleDeleteMessage = async (id: number) => {
  try {
    await deleteMessage(id)
    showSuccessToast('删除成功')
    // 从本地列表中移除
    messageList.value = messageList.value.filter(item => item.id !== id)
  } catch (error) {
    showFailToast('删除失败，请重试')
  }
}

// 跳转到反馈详情
const goToFeedbackDetail = (feedbackId: number | null) => {
  if (feedbackId) {
    showDialog.value = false
    router.push(`/feedback-detail/${feedbackId}`)
  }
}

// 滑动单元格关闭前的回调
const beforeClose = ({ position, instance }: { position: string, instance: any }) => {
  if (position === 'right') {
    // 点击右侧按钮时，什么都不做，由按钮自己的点击事件处理
    instance.close()
    return
  }
  // 其他情况，直接关闭
  instance.close()
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(() => {
  onLoad()
})
</script>

<style scoped>
.message-container {
  padding-top: 46px;
  min-height: 100vh;
  background-color: #f7f8fa;
}

.message-list {
  min-height: calc(100vh - 46px);
}

.read-all-btn {
  color: #1989fa;
}

.unread {
  position: relative;
  font-weight: 500;
}

.unread::before {
  content: '';
  position: absolute;
  width: 8px;
  height: 8px;
  background-color: #ee0a24;
  border-radius: 50%;
  top: 18px;
  left: 12px;
}

.unread :deep(.van-cell__title) {
  padding-left: 12px;
}

.message-type {
  font-size: 12px;
  color: #1989fa;
  padding: 2px 6px;
  background-color: #e6f7ff;
  border-radius: 10px;
}

.delete-button {
  height: 100%;
}

.message-dialog {
  width: 90%;
  max-width: 360px;
}

.message-detail {
  padding: 16px;
}

.message-title {
  font-size: 18px;
  font-weight: 500;
  margin: 0 0 12px 0;
  text-align: center;
}

.message-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.message-type-tag {
  font-size: 12px;
  color: #1989fa;
  padding: 2px 6px;
  background-color: #e6f7ff;
  border-radius: 10px;
}

.message-time {
  font-size: 12px;
  color: #969799;
}

.message-content {
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  margin-bottom: 16px;
}

.message-action {
  margin-top: 16px;
}
</style> 