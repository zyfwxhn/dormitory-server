<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="16">
        <!-- 个人信息卡片 -->
        <el-card shadow="hover" class="profile-card">
          <div class="profile-info">
            <el-avatar :size="80" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            <div class="user-desc">
              <h2 class="greeting">Hi, {{ workerInfo.name || '师傅' }}！今天辛苦了。</h2>
              <p class="role-tag">
                <el-tag type="warning" effect="dark" round>维修员端</el-tag>
                <span class="detail" v-if="workerInfo.skills">擅长：{{ workerInfo.skills }}</span>
              </p>
            </div>
          </div>
        </el-card>

        <!-- 工单统计 -->
        <el-card shadow="hover" class="stats-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><DataLine /></el-icon> 我的工单概览</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-item stat-total" @click="goRepair(null)">
                <div class="stat-num">{{ stats.total }}</div>
                <div class="stat-label">全部工单</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item stat-progress" @click="goRepair(2)">
                <div class="stat-num">{{ stats.inProgress }}</div>
                <div class="stat-label">维修中</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item stat-done" @click="goRepair(3)">
                <div class="stat-num">{{ stats.completed }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 待接单工单 -->
        <el-card shadow="hover" class="pending-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><List /></el-icon> 待接单工单</span>
              <el-button type="primary" text @click="goRepair(0)">查看全部</el-button>
            </div>
          </template>
          <el-table :data="pendingOrders" v-loading="pendingLoading" border stripe empty-text="暂无待接单工单">
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
        <!-- 今日动态 -->
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataLine, List, Clock } from '@element-plus/icons-vue'
import { getWorkerInfo, getWorkerRepairPage, updateRepairStatus } from '@/api/worker'

const router = useRouter()

const workerInfo = ref({})
const fetchWorkerInfo = async () => {
  try { workerInfo.value = await getWorkerInfo() } catch (e) { console.error(e) }
}

const stats = reactive({ total: 0, inProgress: 0, completed: 0 })
const fetchStats = async () => {
  try {
    const [all, inProg, done] = await Promise.all([
      getWorkerRepairPage({ page: 1, pageSize: 1 }),
      getWorkerRepairPage({ page: 1, pageSize: 1, status: 2 }),
      getWorkerRepairPage({ page: 1, pageSize: 1, status: 3 })
    ])
    stats.total = all.total || 0
    stats.inProgress = inProg.total || 0
    stats.completed = done.total || 0
  } catch (e) { console.error(e) }
}

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
    fetchPending()
    fetchRecent()
  } catch (e) { console.error(e) }
}

const goRepair = (status) => {
  const query = status !== null ? { status } : {}
  router.push({ path: '/worker/repair', query })
}

const statusLabel = (s) => ({ 0: '待接单', 1: '已接单', 2: '维修中', 3: '已完成', 4: '已取消' }[s] || '未知')
const statusType = (s) => ({ 0: 'info', 1: 'primary', 2: 'warning', 3: 'success', 4: 'danger' }[s] || 'info')
const timelineType = (s) => ({ 0: 'info', 1: 'primary', 2: 'warning', 3: 'success', 4: 'danger' }[s] || 'info')

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const m = String(timeStr).match(/^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})/)
  if (!m) return String(timeStr)
  const d = new Date(+m[1], +m[2] - 1, +m[3], +m[4], +m[5])
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (d.getFullYear() === now.getFullYear()) return `${m[2]}-${m[3]} ${m[4]}:${m[5]}`
  return `${m[1]}-${m[2]}-${m[3]}`
}

onMounted(() => {
  fetchWorkerInfo()
  fetchStats()
  fetchPending()
  fetchRecent()
})
</script>

<style scoped>
.dashboard-container { padding: 10px; }
.profile-card { margin-bottom: 20px; background-image: linear-gradient(to right, #ffffff, #fff8f0); }
.profile-info { display: flex; align-items: center; }
.user-desc { margin-left: 25px; }
.greeting { margin: 0 0 10px 0; font-size: 22px; color: #303133; }
.role-tag { margin: 0; display: flex; align-items: center; }
.detail { margin-left: 10px; font-size: 14px; color: #909399; }

.stats-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; }

.stat-item { text-align: center; padding: 20px 0; border-radius: 8px; cursor: pointer; transition: all 0.3s; }
.stat-item:hover { background-color: #f5f7fa; transform: translateY(-3px); }
.stat-num { font-size: 36px; font-weight: bold; }
.stat-label { font-size: 14px; color: #909399; margin-top: 8px; }
.stat-total .stat-num { color: #409eff; }
.stat-progress .stat-num { color: #e6a23c; }
.stat-done .stat-num { color: #67c23a; }

.pending-card { margin-bottom: 20px; }
.activity-card { height: calc(100vh - 160px); overflow-y: auto; }

.tl-title { margin: 0 0 4px 0; font-size: 14px; color: #303133; font-weight: 500; }
.tl-status { margin: 0; }
</style>
