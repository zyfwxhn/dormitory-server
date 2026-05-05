<template>
  <div class="dashboard-container">
    <!-- 个人信息 -->
    <el-card shadow="hover" class="profile-card">
      <div class="profile-info">
        <el-avatar :size="72" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
        <div class="user-desc">
          <h2 class="greeting">{{ workerInfo.name || '师傅' }}，今天辛苦了 🔧</h2>
          <p class="role-tag">
            <el-tag :type="workerInfo.isAvailable === 1 ? 'success' : 'danger'" effect="dark" round size="small">
              {{ workerInfo.isAvailable === 1 ? '在岗' : '离岗' }}
            </el-tag>
            <span class="detail" v-if="workerInfo.skills">{{ workerInfo.skills }}</span>
          </p>
        </div>
        <div class="profile-actions">
          <el-button type="warning" round @click="$router.push({ path: '/worker/repair', query: { status: 0 } })">
            抢单大厅 <el-badge :value="pendingTotal" :hidden="pendingTotal === 0" :max="99" class="badge-inline" />
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 指标卡 -->
    <el-row :gutter="16" class="metrics-row">
      <el-col :span="6" v-for="m in metrics" :key="m.label">
        <div class="metric-card" :class="m.color" @click="m.link ? goRepair(m.link) : null">
          <div class="metric-icon-box"><el-icon :size="24"><component :is="m.icon" /></el-icon></div>
          <div class="metric-num">{{ m.value }}</div>
          <div class="metric-label">{{ m.label }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="16">
        <!-- 待接单 -->
        <el-card shadow="hover" class="pending-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><List /></el-icon> 待接单工单（{{ pendingTotal }}）</span>
              <el-button type="primary" text @click="goRepair(0)">查看全部</el-button>
            </div>
          </template>
          <el-table :data="pendingOrders" v-loading="pendingLoading" border stripe empty-text="暂无待接单工单，干得漂亮！">
            <el-table-column type="index" label="#" width="50" align="center" />
            <el-table-column prop="repairType" label="故障类型" width="100" />
            <el-table-column prop="addressSnapshot" label="地址" width="120" />
            <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
            <el-table-column label="提交时间" width="170">
              <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="scope">
                <el-button type="primary" link @click="handleAccept(scope.row.id)">接单</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <!-- 最近工单 -->
        <el-card shadow="hover" class="activity-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><Clock /></el-icon> 最近工单</span>
            </div>
          </template>
          <el-timeline v-if="recentOrders.length > 0">
            <el-timeline-item
              v-for="item in recentOrders"
              :key="item.id"
              :type="timelineType(item.status)"
              :timestamp="formatTime(item.createTime)"
            >
              <p class="tl-title">{{ item.repairType }} - {{ item.addressSnapshot }}</p>
              <p class="tl-status">
                <el-tag :type="statusType(item.status)" size="small">{{ statusLabel(item.status) }}</el-tag>
              </p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无工单记录" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { List, Clock, Tickets, Check, Warning, Tools } from '@element-plus/icons-vue'
import { getWorkerInfo, getWorkerRepairPage, updateRepairStatus } from '@/api/worker'

const router = useRouter()

const workerInfo = ref({})
const fetchWorkerInfo = async () => {
  try { workerInfo.value = await getWorkerInfo() } catch (e) { console.error(e) }
}

const stats = reactive({ total: 0, pending: 0, inProgress: 0, completed: 0 })
const pendingTotal = ref(0)

const fetchStats = async () => {
  try {
    const [all, pending, inProg, done] = await Promise.all([
      getWorkerRepairPage({ page: 1, pageSize: 1 }),
      getWorkerRepairPage({ page: 1, pageSize: 1, status: 0 }),
      getWorkerRepairPage({ page: 1, pageSize: 1, status: 2 }),
      getWorkerRepairPage({ page: 1, pageSize: 1, status: 3 })
    ])
    stats.total = all.total || 0
    stats.pending = pending.total || 0
    stats.inProgress = inProg.total || 0
    stats.completed = done.total || 0
    pendingTotal.value = pending.total || 0
  } catch (e) { console.error(e) }
}

const metrics = computed(() => [
  { icon: 'Tickets', label: '待抢工单', value: stats.pending, color: 'metric-red', link: 0 },
  { icon: 'Warning', label: '维修中', value: stats.inProgress, color: 'metric-orange', link: 2 },
  { icon: 'Check', label: '今日完工', value: 0, color: 'metric-green' },
  { icon: 'Tools', label: '累计完成', value: stats.completed, color: 'metric-blue' }
])

const pendingOrders = ref([])
const pendingLoading = ref(false)
const fetchPending = async () => {
  pendingLoading.value = true
  try {
    const res = await getWorkerRepairPage({ page: 1, pageSize: 5, status: 0 })
    pendingOrders.value = res.records || []
  } catch (e) { console.error(e) }
  finally { pendingLoading.value = false }
}

const recentOrders = ref([])
const fetchRecent = async () => {
  try {
    const res = await getWorkerRepairPage({ page: 1, pageSize: 8 })
    recentOrders.value = res.records || []
  } catch (e) { console.error(e) }
}

const handleAccept = async (id) => {
  try {
    await updateRepairStatus({ id, status: 1 })
    ElMessage.success('已接单')
    fetchStats(); fetchPending(); fetchRecent()
  } catch (e) { console.error(e) }
}

const goRepair = (status) => {
  router.push({ path: '/worker/repair', query: status !== null ? { status } : {} })
}

const statusLabel = (s) => ({ 0: '待接单', 1: '已接单', 2: '维修中', 3: '已完成', 4: '已取消' }[s] || '未知')
const statusType = (s) => ({ 0: 'info', 1: 'primary', 2: 'warning', 3: 'success', 4: 'danger' }[s] || 'info')
const timelineType = (s) => ({ 0: 'info', 1: 'primary', 2: 'warning', 3: 'success', 4: 'danger' }[s] || 'info')

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const m = String(timeStr).match(/^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})/)
  if (!m) return String(timeStr)
  return `${m[2]}-${m[3]} ${m[4]}:${m[5]}`
}

onMounted(() => {
  fetchWorkerInfo()
  fetchStats()
  fetchPending()
  fetchRecent()
})
</script>

<style scoped>
.dashboard-container { padding: 10px; max-width: 1400px; }

/* 个人信息 */
.profile-card {
  margin-bottom: 16px;
  background: linear-gradient(135deg, #fff8f0 0%, #fff3e0 50%, #fef0e0 100%);
}
.profile-info { display: flex; align-items: center; }
.user-desc { margin-left: 20px; flex: 1; }
.greeting { margin: 0 0 6px 0; font-size: 20px; color: #303133; }
.role-tag { display: flex; align-items: center; gap: 8px; }
.detail { font-size: 13px; color: #909399; }
.profile-actions { flex-shrink: 0; }
.badge-inline { margin-left: 4px; }

/* 指标卡 */
.metrics-row { margin-bottom: 16px; }
.metric-card {
  background: #fff; border-radius: 12px; padding: 18px 16px;
  text-align: center; cursor: pointer; transition: all 0.3s;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.metric-card:hover { transform: translateY(-3px); box-shadow: 0 6px 20px rgba(0,0,0,0.08); }
.metric-icon-box { margin-bottom: 8px; width: 44px; height: 44px; border-radius: 12px; display: inline-flex; align-items: center; justify-content: center; }
.metric-red .metric-icon-box { background: rgba(245,108,108,0.12); color: #f56c6c; }
.metric-orange .metric-icon-box { background: rgba(230,162,60,0.12); color: #e6a23c; }
.metric-green .metric-icon-box { background: rgba(103,194,58,0.12); color: #67c23a; }
.metric-blue .metric-icon-box { background: rgba(64,158,255,0.12); color: #409eff; }
.metric-num { font-size: 28px; font-weight: 700; color: #303133; }
.metric-label { font-size: 13px; color: #909399; margin-top: 4px; }

/* 待接单 */
.pending-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; }

/* 最近工单 */
.activity-card { height: calc(100vh - 200px); overflow-y: auto; }
.tl-title { margin: 0 0 4px 0; font-size: 14px; color: #303133; font-weight: 500; }
.tl-status { margin: 0; }
</style>
