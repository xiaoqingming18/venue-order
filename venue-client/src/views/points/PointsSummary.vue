<template>
  <div class="points-summary-container">
    <div class="page-header">
      <h1>我的积分</h1>
      <div class="actions">
        <el-button type="primary" @click="handleSignIn" :disabled="hasSignedIn">
          {{ hasSignedIn ? '今日已签到' : '每日签到' }}
        </el-button>
        <el-button @click="handleExpiredPoints" v-if="summary?.pointsExpiringSoon > 0">
          处理过期积分
        </el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="12" :lg="8" :xl="8">
        <el-card class="points-card">
          <div class="card-header">
            <div class="title">积分概览</div>
          </div>
          <div class="points-data">
            <div class="points-item">
              <div class="label">总积分</div>
              <div class="value">{{ summary?.totalPoints || 0 }}</div>
            </div>
            <div class="points-item">
              <div class="label">可用积分</div>
              <div class="value highlight">{{ summary?.availablePoints || 0 }}</div>
            </div>
            <div class="points-item">
              <div class="label">冻结积分</div>
              <div class="value">{{ summary?.frozenPoints || 0 }}</div>
            </div>
            <div class="points-item">
              <div class="label">已过期积分</div>
              <div class="value">{{ summary?.expiredPoints || 0 }}</div>
            </div>
          </div>
          <div class="view-more">
            <router-link to="/points/records">查看积分明细</router-link>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="12" :lg="8" :xl="8">
        <el-card class="member-card">
          <div class="card-header">
            <div class="title">会员等级</div>
          </div>
          <div class="member-info" v-if="summary?.memberLevel">
            <div class="level-icon">
              <img :src="summary.memberLevel.icon || defaultIcon" alt="Level Icon">
            </div>
            <div class="level-details">
              <div class="level-name">{{ summary.memberLevel.levelName }}</div>
              <div class="level-discount">
                预约享{{ (summary.memberLevel.discount * 10).toFixed(1) }}折优惠
              </div>
              <div class="next-level" v-if="summary.memberLevel.nextLevelPoints">
                距离{{ summary.memberLevel.nextLevelName }}还需{{ summary.memberLevel.nextLevelPoints }}积分
                <el-progress :percentage="calculateProgress()" :format="progressFormat"></el-progress>
              </div>
            </div>
          </div>
          <div class="member-info" v-else>
            <div class="empty-data">暂无会员等级信息</div>
          </div>
          <div class="view-more">
            <router-link to="/member/levels">查看会员体系</router-link>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="24" :lg="8" :xl="8">
        <el-card class="expiry-card">
          <div class="card-header">
            <div class="title">积分到期提醒</div>
          </div>
          <div class="expiry-info">
            <template v-if="summary?.pointsExpiringSoon > 0">
              <div class="expiry-icon">
                <i class="el-icon-warning"></i>
              </div>
              <div class="expiry-details">
                <div class="expiry-text">
                  您有 <span class="expiry-points">{{ summary.pointsExpiringSoon }}</span> 积分即将在
                  <span class="expiry-date">{{ formatDate(summary.expireDate) }}</span> 过期
                </div>
                <div class="expiry-tip">建议尽快使用，过期积分将自动清零</div>
              </div>
            </template>
            <div class="empty-data" v-else>
              <i class="el-icon-check"></i>
              您暂无即将过期的积分
            </div>
          </div>
          <div class="view-more">
            <router-link to="/points/rules">查看积分规则</router-link>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="rules-row">
      <el-col :span="24">
        <el-card class="rules-card">
          <div class="card-header">
            <div class="title">积分获取方式</div>
          </div>
          <div class="rules-list">
            <el-table :data="rules" style="width: 100%" v-loading="rulesLoading">
              <el-table-column prop="ruleName" label="规则名称" width="180"></el-table-column>
              <el-table-column prop="pointValue" label="积分值" width="100"></el-table-column>
              <el-table-column prop="ruleDescription" label="规则描述"></el-table-column>
              <el-table-column prop="validityDays" label="有效期" width="100">
                <template #default="scope">
                  {{ scope.row.validityDays }}天
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getUserPointsSummary, getPointRules, signInForPoints, handleExpiredPoints } from '@/api/points'
import { UserPointsSummary, PointRule } from '@/types/points'
import { ElMessage } from 'element-plus'
import { formatDate as formatDateUtil } from '@/utils/format'

const summary = ref<UserPointsSummary | null>(null)
const rules = ref<PointRule[]>([])
const loading = ref(false)
const rulesLoading = ref(false)
const hasSignedIn = ref(false)
const defaultIcon = '/src/assets/images/default-level.png'

// 获取积分概览
const fetchSummary = async () => {
  loading.value = true
  try {
    const res = await getUserPointsSummary()
    if (res.code === 200) {
      summary.value = res.data
    } else {
      ElMessage.error(res.message || '获取积分概览失败')
    }
  } catch (error) {
    console.error('获取积分概览出错:', error)
    ElMessage.error('获取积分概览出错')
  } finally {
    loading.value = false
  }
}

// 获取积分规则
const fetchRules = async () => {
  rulesLoading.value = true
  try {
    const res = await getPointRules()
    if (res.code === 200) {
      rules.value = res.data
    } else {
      ElMessage.error(res.message || '获取积分规则失败')
    }
  } catch (error) {
    console.error('获取积分规则出错:', error)
    ElMessage.error('获取积分规则出错')
  } finally {
    rulesLoading.value = false
  }
}

// 签到获取积分
const handleSignIn = async () => {
  if (hasSignedIn.value) return
  
  try {
    const res = await signInForPoints()
    if (res.code === 200) {
      ElMessage.success(`签到成功，获得${res.data}积分`)
      hasSignedIn.value = true
      // 重新获取积分概览
      fetchSummary()
    } else {
      ElMessage.error(res.message || '签到失败')
    }
  } catch (error) {
    console.error('签到出错:', error)
    ElMessage.error('签到出错')
  }
}

// 处理过期积分
const handleExpiredPoints = async () => {
  try {
    const res = await handleExpiredPoints()
    if (res.code === 200) {
      if (res.data > 0) {
        ElMessage.success(`成功处理${res.data}个过期积分`)
      } else {
        ElMessage.info('暂无过期积分')
      }
      // 重新获取积分概览
      fetchSummary()
    } else {
      ElMessage.error(res.message || '处理过期积分失败')
    }
  } catch (error) {
    console.error('处理过期积分出错:', error)
    ElMessage.error('处理过期积分出错')
  }
}

// 计算会员等级进度
const calculateProgress = () => {
  if (!summary.value?.memberLevel || !summary.value.memberLevel.nextLevelPoints) return 0
  
  const currentAvailablePoints = summary.value.availablePoints
  const currentLevel = summary.value.memberLevel
  const nextLevelPoints = currentLevel.nextLevelPoints
  const totalPointsNeeded = nextLevelPoints + currentAvailablePoints
  
  return Math.min(Math.floor((currentAvailablePoints / totalPointsNeeded) * 100), 100)
}

// 格式化进度条文本
const progressFormat = (percentage: number) => {
  return percentage + '%'
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '--'
  return formatDateUtil(dateStr, 'YYYY-MM-DD')
}

onMounted(() => {
  fetchSummary()
  fetchRules()
})
</script>

<style scoped>
.points-summary-container {
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

.actions {
  display: flex;
  gap: 10px;
}

.points-card, .member-card, .expiry-card, .rules-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.card-header .title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.points-data {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 15px;
}

.points-item {
  width: 50%;
  margin-bottom: 15px;
}

.points-item .label {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.points-item .value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.points-item .value.highlight {
  color: #409EFF;
}

.member-info, .expiry-info {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.level-icon, .expiry-icon {
  margin-right: 15px;
}

.level-icon img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
}

.level-details, .expiry-details {
  flex: 1;
}

.level-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.level-discount {
  font-size: 14px;
  color: #67c23a;
  margin-bottom: 10px;
}

.next-level {
  font-size: 14px;
  color: #666;
}

.expiry-text {
  font-size: 14px;
  color: #333;
  margin-bottom: 5px;
}

.expiry-points {
  font-weight: bold;
  color: #f56c6c;
}

.expiry-date {
  font-weight: bold;
  color: #f56c6c;
}

.expiry-tip {
  font-size: 12px;
  color: #999;
}

.empty-data {
  text-align: center;
  color: #999;
  padding: 20px 0;
  width: 100%;
}

.view-more {
  text-align: right;
  font-size: 14px;
}

.view-more a {
  color: #409EFF;
  text-decoration: none;
}

.rules-row {
  margin-top: 10px;
}

.rules-list {
  margin-top: 15px;
}

@media (max-width: 768px) {
  .points-item {
    width: 100%;
  }
}
</style> 