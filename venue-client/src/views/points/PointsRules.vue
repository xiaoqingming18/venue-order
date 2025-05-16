<template>
  <div class="points-rules-container">
    <div class="page-header">
      <h1>积分规则</h1>
      <div class="actions">
        <router-link to="/points">
          <el-button>返回积分概览</el-button>
        </router-link>
      </div>
    </div>

    <el-card class="rules-card">
      <div class="section-title">积分获取规则</div>
      <div class="rules-list">
        <el-table :data="rules" style="width: 100%" v-loading="loading">
          <el-table-column prop="ruleName" label="规则名称" width="180"></el-table-column>
          <el-table-column prop="pointValue" label="积分值" width="100">
            <template #default="scope">
              <span class="point-value">+{{ scope.row.pointValue }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="ruleDescription" label="规则描述"></el-table-column>
          <el-table-column prop="validityDays" label="有效期" width="120">
            <template #default="scope">
              {{ scope.row.validityDays }}天
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-card class="rules-card">
      <div class="section-title">积分使用规则</div>
      <div class="rules-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="积分价值">100积分 = 1元人民币</el-descriptions-item>
          <el-descriptions-item label="积分使用">预约场馆时可使用积分抵扣部分金额</el-descriptions-item>
          <el-descriptions-item label="抵扣比例">单笔订单最高可使用积分抵扣订单金额的50%</el-descriptions-item>
          <el-descriptions-item label="积分门槛">无使用门槛，1积分起使用</el-descriptions-item>
          <el-descriptions-item label="积分有效期">积分有效期以获取规则为准，过期积分会自动清零</el-descriptions-item>
          <el-descriptions-item label="积分查询">可在 "我的积分" 页面查看积分明细和使用记录</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <el-card class="rules-card">
      <div class="section-title">积分常见问题</div>
      <div class="rules-content">
        <el-collapse accordion>
          <el-collapse-item title="如何获取积分？" name="1">
            <div class="faq-content">
              您可以通过完成预约、评价场馆、每日签到等方式获取积分，具体积分获取规则可参考上方的"积分获取规则"表格。
            </div>
          </el-collapse-item>
          <el-collapse-item title="积分有效期是多久？" name="2">
            <div class="faq-content">
              积分有效期根据获取方式不同而不同，一般为365天。您可以在"我的积分"页面查看即将过期的积分情况。
            </div>
          </el-collapse-item>
          <el-collapse-item title="如何使用积分？" name="3">
            <div class="faq-content">
              在预约场馆付款时，可以选择使用积分抵扣部分金额。100积分可抵扣1元人民币，单笔订单最高可抵扣订单金额的50%。
            </div>
          </el-collapse-item>
          <el-collapse-item title="积分可以转赠吗？" name="4">
            <div class="faq-content">
              目前积分不支持转赠功能，积分仅限本人使用。
            </div>
          </el-collapse-item>
          <el-collapse-item title="积分会过期吗？" name="5">
            <div class="faq-content">
              是的，积分有有效期限制，过期后将自动清零。您可以在"我的积分"页面查看即将过期的积分，并及时使用。
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPointRules } from '@/api/points'
import { PointRule } from '@/types/points'
import { ElMessage } from 'element-plus'

const rules = ref<PointRule[]>([])
const loading = ref(false)

// 获取积分规则
const fetchRules = async () => {
  loading.value = true
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
    loading.value = false
  }
}

onMounted(() => {
  fetchRules()
})
</script>

<style scoped>
.points-rules-container {
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

.rules-card {
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

.rules-list {
  margin-bottom: 20px;
}

.rules-content {
  padding: 10px 0;
}

.point-value {
  color: #67c23a;
  font-weight: bold;
}

.faq-content {
  color: #666;
  line-height: 1.6;
  padding: 10px 0;
}
</style> 