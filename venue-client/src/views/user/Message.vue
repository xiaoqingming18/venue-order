<template>
  <div class="message-container">
    <van-nav-bar
      title="消息中心"
      left-arrow
      @click-left="goBack"
      right-text="全部已读"
      :right-text="hasUnread ? '全部已读' : ''"
      @click-right="handleMarkAllAsRead"
      fixed
    />
    
    <div class="message-list">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <template v-if="!loading">
            <van-empty v-if="messageList.length === 0" description="暂无消息" />
            
            <div v-else>
              <van-swipe-cell
                v-for="item in messageList"
                :key="item.id"
                :name="item.id"
                :before-close="beforeClose"
              >
                <van-cell
                  :class="{ unread: item.isRead === 0 }"
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
                    @click="showDeleteDialog(item.id)"
                  />
                </template>
              </van-swipe-cell>
            </div>
          </template>
        </van-list>
      </van-pull-refresh>
    </div>
    
    <van-dialog
      v-model:show="showDialog"
      title=""
      :show-confirm-button="false"
      class="message-dialog"
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

    <!-- 新消息提示 -->
    <van-popup
      v-model:show="showNewMessage"
      position="top"
      :style="{ height: 'auto' }"
      duration="0.3"
      round
      overlay-class="feedback-message-overlay"
    >
      <div class="new-feedback-message" @click="handleNewMessageClick">
        <div class="message-icon">
          <van-icon name="chat" size="24" color="#1989fa" />
        </div>
        <div class="message-text">
          <div class="message-title">{{ newMessage.title }}</div>
          <div class="message-brief">{{ newMessage.content }}</div>
        </div>
        <div class="message-action">
          <van-icon name="arrow" color="#969799" />
        </div>
      </div>
    </van-popup>
    
    <!-- iOS底部标签栏 -->
    <div class="ios-tab-bar">
      <a class="tab-item" @click="router.push('/')">
        <i class="fas fa-home"></i>
        <span>首页</span>
      </a>
      <a class="tab-item" @click="router.push('/orders')">
        <i class="fas fa-calendar-alt"></i>
        <span>订单</span>
      </a>
      <a class="tab-item active" @click="router.push('/message')">
        <i class="fas fa-bell"></i>
        <span>消息</span>
      </a>
      <a class="tab-item" @click="router.push('/profile')">
        <i class="fas fa-user"></i>
        <span>我的</span>
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
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
const showNewMessage = ref(false)
const newMessage = ref<UserMessage | null>(null)

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
      
      // 检查是否有未读的反馈回复消息，如果有则显示通知
      const unreadFeedbackMessages = data.records.filter(msg => 
        msg.type === 3 && msg.isRead === 0
      )
      
      if (unreadFeedbackMessages.length > 0 && !showDialog.value) {
        newMessage.value = unreadFeedbackMessages[0]
        setTimeout(() => {
          showNewMessage.value = true
          
          // 5秒后自动关闭
          setTimeout(() => {
            showNewMessage.value = false
          }, 5000)
        }, 500)
      }
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
  refreshing.value = true
  finished.value = false
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

// 处理新消息点击
const handleNewMessageClick = () => {
  if (newMessage.value) {
    showNewMessage.value = false
    goToFeedbackDetail(newMessage.value.relatedId)
    
    // 标记为已读
    markAsRead(newMessage.value.id).then(() => {
      // 更新消息列表中的已读状态
      const index = messageList.value.findIndex(item => item.id === newMessage.value?.id)
      if (index !== -1) {
        messageList.value[index].isRead = 1
      }
    })
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
  padding-bottom: 70px;
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
  line-height: 1.5;
  color: #323233;
  margin-bottom: 16px;
  white-space: pre-wrap;
  word-break: break-all;
}

.message-action {
  padding-top: 8px;
}

/* 新消息通知样式 */
.feedback-message-overlay {
  background-color: transparent;
}

.new-feedback-message {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin: 10px;
  cursor: pointer;
}

.message-icon {
  margin-right: 12px;
  flex-shrink: 0;
}

.message-text {
  flex: 1;
  overflow: hidden;
}

.message-text .message-title {
  font-size: 14px;
  font-weight: bold;
  margin: 0 0 4px 0;
  text-align: left;
}

.message-text .message-brief {
  font-size: 12px;
  color: #969799;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.message-action {
  flex-shrink: 0;
  margin-left: 8px;
}

.ios-tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 10px 0;
  border-top: 1px solid #e5e5e5;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: #666;
}

.tab-item i {
  font-size: 18px;
  margin-bottom: 4px;
}

.tab-item span {
  font-size: 12px;
}

.tab-item.active {
  color: #1989fa;
}
</style> 