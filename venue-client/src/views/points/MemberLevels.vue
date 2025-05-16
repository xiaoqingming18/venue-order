<template>
  <div class="member-levels-container">
    <div class="page-header">
      <h1>会员等级</h1>
      <div class="actions">
        <router-link to="/points">
          <el-button>返回积分概览</el-button>
        </router-link>
      </div>
    </div>

    <!-- 当前会员等级 -->
    <el-card class="current-level-card" v-loading="userLevelLoading">
      <div class="section-title">我的会员等级</div>
      <div class="current-level-content" v-if="userLevel">
        <div class="level-info">
          <div class="level-icon">
            <img :src="userLevel.icon || defaultIcon" alt="Level Icon">
          </div>
          <div class="level-details">
            <div class="level-name">{{ userLevel.levelName }}</div>
            <div class="level-discount">预约享{{ (userLevel.discount * 10).toFixed(1) }}折优惠</div>
            <div class="level-progress" v-if="userLevel.nextLevelPoints">
              <div class="progress-text">
                距离{{ userLevel.nextLevelName }}还需{{ userLevel.nextLevelPoints }}积分
              </div>
              <el-progress :percentage="calculateProgress()" :format="progressFormat"></el-progress>
            </div>
            <div class="level-max" v-else>
              恭喜您已经达到最高等级！
            </div>
          </div>
        </div>
      </div>
      <div class="empty-data" v-else-if="!userLevelLoading">
        暂无会员等级信息
      </div>
    </el-card>

    <!-- 会员等级体系 -->
    <el-card class="levels-card" v-loading="levelsLoading">
      <div class="section-title">会员等级体系</div>
      <div class="levels-content">
        <div class="level-timeline">
          <div 
            v-for="(level, index) in levels" 
            :key="level.id" 
            class="level-item"
            :class="{ 'active': userLevel && userLevel.levelValue >= level.levelValue }"
          >
            <div class="level-dot"></div>
            <div class="level-card">
              <div class="level-header">
                <img :src="level.iconUrl || defaultIcon" alt="Level Icon" class="level-img">
                <div class="level-title">{{ level.levelName }}</div>
              </div>
              <div class="level-body">
                <div class="level-points">所需积分: {{ level.pointThreshold }}</div>
                <div class="level-discount">折扣率: {{ (level.discountRate * 10).toFixed(1) }}折</div>
                <div class="level-desc">{{ level.description || '暂无描述' }}</div>
              </div>
            </div>
            <div class="level-line" v-if="index < levels.length - 1"></div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 会员特权说明 -->
    <el-card class="privilege-card">
      <div class="section-title">会员特权</div>
      <div class="privilege-content">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="privilege-item">
              <div class="privilege-icon">
                <i class="el-icon-discount"></i>
              </div>
              <div class="privilege-title">预约折扣</div>
              <div class="privilege-desc">会员可享受场馆预约折扣，等级越高折扣越多</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="privilege-item">
              <div class="privilege-icon">
                <i class="el-icon-present"></i>
              </div>
              <div class="privilege-title">生日礼遇</div>
              <div class="privilege-desc">会员生日当月可获得额外积分奖励</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="privilege-item">
              <div class="privilege-icon">
                <i class="el-icon-alarm-clock"></i>
              </div>
              <div class="privilege-title">优先预约</div>
              <div class="privilege-desc">高级会员可享受场馆优先预约权</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="8">
            <div class="privilege-item">
              <div class="privilege-icon">
                <i class="el-icon-chat-dot-round"></i>
              </div>
              <div class="privilege-title">专属客服</div>
              <div class="privilege-desc">高级会员可享受专属客服服务</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="privilege-item">
              <div class="privilege-icon">
                <i class="el-icon-bicycle"></i>
              </div>
              <div class="privilege-title">场地活动</div>
              <div class="privilege-desc">会员可优先参与场馆举办的特色活动</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="privilege-item">
              <div class="privilege-icon">
                <i class="el-icon-medal"></i>
              </div>
              <div class="privilege-title">更多积分</div>
              <div class="privilege-desc">会员预约完成后可获得更多积分奖励</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 升级攻略 -->
    <el-card class="upgrade-card">
      <div class="section-title">升级攻略</div>
      <div class="upgrade-content">
        <el-steps direction="vertical">
          <el-step title="完成场馆预约" description="每完成一次场馆预约可获得相应积分"></el-step>
          <el-step title="评价场馆" description="对预约过的场馆进行评价，获取额外积分"></el-step>
          <el-step title="每日签到" description="每日登录APP签到，持续积累积分"></el-step>
          <el-step title="参与活动" description="积极参与平台举办的各类活动，获取更多积分"></el-step>
          <el-step title="邀请好友" description="邀请好友注册并完成首次预约，双方均可获得积分奖励"></el-step>
        </el-steps>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getUserMemberLevel, getAllMemberLevels } from '@/api/points'
import { MemberLevel } from '@/types/points'
import { ElMessage } from 'element-plus'

const userLevel = ref<MemberLevel | null>(null)
const levels = ref<any[]>([])
const userLevelLoading = ref(false)
const levelsLoading = ref(false)
const defaultIcon = '/src/assets/images/default-level.png'

// 获取用户会员等级
const fetchUserLevel = async () => {
  userLevelLoading.value = true
  try {
    const res = await getUserMemberLevel()
    if (res.code === 200) {
      userLevel.value = res.data
    } else {
      ElMessage.error(res.message || '获取会员等级失败')
    }
  } catch (error) {
    console.error('获取会员等级出错:', error)
    ElMessage.error('获取会员等级出错')
  } finally {
    userLevelLoading.value = false
  }
}

// 获取所有会员等级
const fetchAllLevels = async () => {
  levelsLoading.value = true
  try {
    const res = await getAllMemberLevels()
    if (res.code === 200) {
      levels.value = res.data
    } else {
      ElMessage.error(res.message || '获取会员等级体系失败')
    }
  } catch (error) {
    console.error('获取会员等级体系出错:', error)
    ElMessage.error('获取会员等级体系出错')
  } finally {
    levelsLoading.value = false
  }
}

// 计算会员等级进度
const calculateProgress = () => {
  if (!userLevel.value?.nextLevelPoints) return 100
  
  const nextLevelPoints = userLevel.value.nextLevelPoints
  const currentLevel = levels.value.find(l => l.levelValue === userLevel.value?.levelValue)
  const nextLevel = levels.value.find(l => l.levelValue === userLevel.value?.levelValue + 1)
  
  if (!currentLevel || !nextLevel) return 0
  
  const totalPointsNeeded = nextLevel.pointThreshold - currentLevel.pointThreshold
  const pointsGained = totalPointsNeeded - nextLevelPoints
  
  return Math.min(Math.floor((pointsGained / totalPointsNeeded) * 100), 100)
}

// 格式化进度条文本
const progressFormat = (percentage: number) => {
  return percentage + '%'
}

onMounted(() => {
  fetchUserLevel()
  fetchAllLevels()
})
</script>

<style scoped>
.member-levels-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.current-level-card, .levels-card, .privilege-card, .upgrade-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.current-level-content {
  padding: 10px 0;
}

.level-info {
  display: flex;
  align-items: center;
}

.level-icon {
  margin-right: 20px;
}

.level-icon img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}

.level-details {
  flex: 1;
}

.level-name {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.level-discount {
  font-size: 16px;
  color: #67c23a;
  margin-bottom: 15px;
}

.level-progress, .level-max {
  margin-top: 10px;
}

.progress-text {
  margin-bottom: 8px;
  color: #666;
}

.level-max {
  color: #f56c6c;
  font-weight: bold;
}

.empty-data {
  text-align: center;
  color: #999;
  padding: 20px 0;
}

.levels-content {
  padding: 20px 0;
}

.level-timeline {
  position: relative;
  display: flex;
  flex-direction: column;
}

.level-item {
  position: relative;
  margin-bottom: 30px;
  display: flex;
  align-items: center;
}

.level-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background-color: #dcdfe6;
  z-index: 2;
}

.level-item.active .level-dot {
  background-color: #409EFF;
}

.level-line {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 2px;
  height: calc(100% + 30px);
  background-color: #dcdfe6;
  z-index: 1;
}

.level-item.active .level-line {
  background-color: #409EFF;
}

.level-card {
  margin-left: 20px;
  width: calc(100% - 36px);
  border-radius: 8px;
  border: 1px solid #ebeef5;
  overflow: hidden;
  transition: all 0.3s;
}

.level-item.active .level-card {
  border-color: #409EFF;
  box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.1);
}

.level-header {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.level-item.active .level-header {
  background-color: #ecf5ff;
  border-bottom-color: #d9ecff;
}

.level-img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 10px;
}

.level-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.level-item.active .level-title {
  color: #409EFF;
}

.level-body {
  padding: 15px 20px;
}

.level-points, .level-discount {
  margin-bottom: 8px;
  color: #666;
}

.level-desc {
  color: #999;
  font-size: 14px;
  line-height: 1.5;
}

.privilege-content, .upgrade-content {
  padding: 10px 0;
}

.privilege-item {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  height: 100%;
  transition: all 0.3s;
}

.privilege-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.privilege-icon {
  font-size: 40px;
  color: #409EFF;
  margin-bottom: 15px;
}

.privilege-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.privilege-desc {
  color: #666;
  line-height: 1.5;
}

.upgrade-content {
  padding: 20px;
}
</style> 