<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
const pathName = route.name
</script>

<template>
  <div class="placeholder-container">
    <el-result icon="info" title="页面开发中" sub-title="该功能正在开发中，敬请期待...">
      <template #extra>
        <div class="info-text">
          <p>
            当前页面: <span class="highlight">{{ pathName }}</span>
          </p>
          <p>该页面功能将在系统后续版本中推出</p>
        </div>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.placeholder-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  padding: 20px;
}

.info-text {
  text-align: center;
  color: #606266;
  line-height: 1.8;
}

.highlight {
  color: #409eff;
  font-weight: bold;
}
</style>
