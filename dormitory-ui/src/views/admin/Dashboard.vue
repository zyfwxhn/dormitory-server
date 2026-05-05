<template>
  <div class="dashboard">
    <!-- 今日指标 -->
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="metric-card metric-blue">
          <div class="metric-body">
            <div class="metric-icon-box"><el-icon :size="28"><Plus /></el-icon></div>
            <div class="metric-info">
              <div class="metric-value">{{ animatedMetrics.newRepair }}</div>
              <div class="metric-label">今日新增报修</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="metric-card metric-green">
          <div class="metric-body">
            <div class="metric-icon-box"><el-icon :size="28"><Check /></el-icon></div>
            <div class="metric-info">
              <div class="metric-value">{{ animatedMetrics.finished }}</div>
              <div class="metric-label">今日完工</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="metric-card metric-orange">
          <div class="metric-body">
            <div class="metric-icon-box"><el-icon :size="28"><Monitor /></el-icon></div>
            <div class="metric-info">
              <div class="metric-value">{{ animatedMetrics.idle }}</div>
              <div class="metric-label">空闲设备</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 工单状态分布饼图 -->
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><span class="chart-title">工单状态分布</span></template>
          <div ref="pieChartRef" class="chart-box"></div>
        </el-card>
      </el-col>

      <!-- 维修员排行榜柱状图 -->
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><span class="chart-title">维修员完工排行 TOP5</span></template>
          <div ref="barChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Plus, Check, Monitor } from '@element-plus/icons-vue'
import { getStatisticsOverview } from '@/api/admin'

const overview = ref({})
const pieChartRef = ref(null)
const barChartRef = ref(null)
let pieChart = null, barChart = null

// 数字滚动动画
const animatedMetrics = ref({ newRepair: 0, finished: 0, idle: 0 })
const animateNum = (key, end) => {
  const start = animatedMetrics.value[key]
  const diff = end - start
  const steps = 20; let i = 0
  const timer = setInterval(() => {
    i++; animatedMetrics.value[key] = Math.round(start + diff * (i / steps))
    if (i >= steps) { animatedMetrics.value[key] = end; clearInterval(timer) }
  }, 30)
}

const fetchData = async () => {
  try {
    overview.value = await getStatisticsOverview()
    const m = overview.value.todayMetrics || {}
    animateNum('newRepair', m.newRepairCount || 0)
    animateNum('finished', m.finishedRepairCount || 0)
    animateNum('idle', m.idleDeviceCount || 0)
  } catch (e) { console.error('获取统计数据失败', e) }
}

const updateCharts = () => {
  if (!pieChart || !barChart) return
  const data = (overview.value.statusProportion || []).map(i => ({ name: i.name, value: i.value }))
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: ['45%', '70%'], avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%' }, data,
      color: ['#909399', '#409eff', '#e6a23c', '#67c23a', '#f56c6c']
    }]
  })
  const ranking = overview.value.workerRanking || []
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 20, right: 20, bottom: 0, top: 10, containLabel: true },
    xAxis: { type: 'value', show: false, max: Math.max(...ranking.map(i => i.completedCount), 1) * 1.3 },
    yAxis: { type: 'category', data: ranking.map(i => i.workerName).reverse(), axisLine: { show: false }, axisTick: { show: false }, axisLabel: { fontSize: 13 } },
    series: [{
      type: 'bar', data: ranking.map(i => i.completedCount).reverse(), barWidth: 16,
      itemStyle: { borderRadius: [0, 8, 8, 0], color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#67c23a' }, { offset: 1, color: '#85ce61' }]) },
      label: { show: true, position: 'right', formatter: '{c} 单', fontSize: 12, color: '#606266' }
    }]
  })
}

const onResize = () => { pieChart?.resize(); barChart?.resize() }

onMounted(async () => {
  await fetchData()
  await nextTick()
  pieChart = echarts.init(pieChartRef.value)
  barChart = echarts.init(barChartRef.value)
  updateCharts()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  pieChart?.dispose()
  barChart?.dispose()
})

watch(() => overview.value, () => nextTick(updateCharts), { deep: true })
</script>

<style scoped>
.dashboard { }
.metric-card { }
.metric-body { display: flex; align-items: center; padding: 10px 0; }
.metric-icon-box { width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin-right: 16px; }
.metric-blue .metric-icon-box { background: rgba(64,158,255,0.12); color: #409eff; }
.metric-green .metric-icon-box { background: rgba(103,194,58,0.12); color: #67c23a; }
.metric-orange .metric-icon-box { background: rgba(230,162,60,0.12); color: #e6a23c; }
.metric-info { }
.metric-value { font-size: 32px; font-weight: 700; color: #303133; line-height: 1.2; }
.metric-label { font-size: 13px; color: #909399; margin-top: 2px; }
.chart-title { font-weight: bold; }
.chart-box { width: 100%; height: 280px; }
</style>
