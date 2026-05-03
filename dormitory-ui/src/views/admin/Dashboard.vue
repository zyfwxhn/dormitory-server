<template>
  <div class="dashboard">
    <!-- 今日指标 -->
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="metric-card metric-blue">
          <div class="metric-icon"><el-icon :size="40"><Plus /></el-icon></div>
          <div class="metric-value">{{ overview.todayMetrics?.newRepairCount || 0 }}</div>
          <div class="metric-label">今日新增报修</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="metric-card metric-green">
          <div class="metric-icon"><el-icon :size="40"><Check /></el-icon></div>
          <div class="metric-value">{{ overview.todayMetrics?.finishedRepairCount || 0 }}</div>
          <div class="metric-label">今日完工</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="metric-card metric-orange">
          <div class="metric-icon"><el-icon :size="40"><Monitor /></el-icon></div>
          <div class="metric-value">{{ overview.todayMetrics?.idleDeviceCount || 0 }}</div>
          <div class="metric-label">空闲设备</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 工单状态分布 -->
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><span class="chart-title">工单状态分布</span></template>
          <div class="status-bars" v-if="overview.statusProportion?.length">
            <div v-for="item in overview.statusProportion" :key="item.name" class="status-row">
              <span class="status-name">{{ item.name }}</span>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: maxCount ? (item.value / maxCount * 100) + '%' : '0%' }"></div>
              </div>
              <span class="status-count">{{ item.value }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>

      <!-- 维修员排行榜 -->
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><span class="chart-title">维修员完工排行 TOP{{ overview.workerRanking?.length || 0 }}</span></template>
          <div class="rank-list" v-if="overview.workerRanking?.length">
            <div v-for="(item, idx) in overview.workerRanking" :key="idx" class="rank-row">
              <span class="rank-num" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</span>
              <span class="rank-name">{{ item.workerName }}</span>
              <div class="bar-track rank-track">
                <div class="bar-fill rank-bar" :style="{ width: rankMax ? (item.completedCount / rankMax * 100) + '%' : '0%' }"></div>
              </div>
              <span class="rank-count">{{ item.completedCount }} 单</span>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, Check, Monitor } from '@element-plus/icons-vue'
import { getStatisticsOverview } from '@/api/admin'

const overview = ref({})

const maxCount = computed(() => {
  const arr = overview.value.statusProportion || []
  return arr.length ? Math.max(...arr.map(i => i.value)) : 1
})
const rankMax = computed(() => {
  const arr = overview.value.workerRanking || []
  return arr.length ? Math.max(...arr.map(i => i.completedCount)) : 1
})

const fetchData = async () => {
  try { overview.value = await getStatisticsOverview() }
  catch (e) { console.error('获取统计数据失败', e) }
}

onMounted(() => fetchData())
</script>

<style scoped>
.dashboard { }
.metric-card { text-align: center; padding: 10px 0; }
.metric-icon { margin-bottom: 10px; }
.metric-blue .metric-icon { color: #409EFF; }
.metric-green .metric-icon { color: #67C23A; }
.metric-orange .metric-icon { color: #E6A23C; }
.metric-value { font-size: 36px; font-weight: 700; color: #303133; }
.metric-label { font-size: 14px; color: #909399; margin-top: 4px; }
.chart-title { font-weight: bold; }
.status-bars, .rank-list { padding: 10px 0; }
.status-row, .rank-row { display: flex; align-items: center; margin-bottom: 14px; }
.status-name, .rank-name { width: 80px; font-size: 13px; color: #606266; flex-shrink: 0; }
.rank-num { width: 24px; height: 24px; line-height: 24px; text-align: center; border-radius: 50%; font-size: 12px; font-weight: 700; color: #fff; margin-right: 8px; flex-shrink: 0; }
.rank-1 { background: #f56c6c; }
.rank-2 { background: #e6a23c; }
.rank-3 { background: #409eff; }
.rank-4, .rank-5 { background: #909399; }
.bar-track { flex: 1; height: 16px; background: #f0f2f5; border-radius: 8px; overflow: hidden; margin: 0 12px; }
.bar-fill { height: 100%; background: linear-gradient(90deg, #409eff, #66b1ff); border-radius: 8px; transition: width 0.6s; }
.rank-bar { background: linear-gradient(90deg, #67c23a, #85ce61); }
.rank-track { height: 14px; }
.status-count, .rank-count { font-size: 13px; color: #303133; font-weight: 600; flex-shrink: 0; min-width: 30px; text-align: right; }
</style>
