<template>
  <div class="dashboard-container">
    <!-- 个人信息 + 问候 -->
    <el-card shadow="hover" class="profile-card">
      <div class="profile-info">
        <el-avatar :size="72" :src="studentInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
        <div class="user-desc">
          <h2 class="greeting">{{ greeting }}，{{ studentInfo.name || '同学' }}！</h2>
          <p class="today-info">{{ todayStr }}</p>
          <p class="role-tag">
            <el-tag type="success" effect="dark" round size="small">学生端</el-tag>
            <span class="detail">{{ studentInfo.grade || '' }} · {{ studentInfo.buildingNo || '--' }} {{ studentInfo.roomNo || '--' }}</span>
          </p>
        </div>
        <div class="profile-actions">
          <el-button type="primary" round @click="$router.push('/student/repair')">
            <el-icon><Plus /></el-icon> 发起报修
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 待处理汇总条 -->
    <div class="pending-strip" v-if="pendingCount > 0">
      <div class="pending-item pending-repair" @click="$router.push('/student/repair')">
        <span class="pending-num">{{ stats.pendingReview }}</span>
        <span class="pending-label">待评价报修</span>
      </div>
      <div class="pending-item pending-notify" @click="$router.push('/student/notification')">
        <span class="pending-num">{{ unreadNotices }}</span>
        <span class="pending-label">未读通知</span>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <!-- 数据概览指标卡 -->
        <el-row :gutter="16" class="metrics-row">
          <el-col :span="6" v-for="m in metrics" :key="m.label">
            <div class="metric-card" :class="m.color" @click="m.link ? $router.push(m.link) : null">
              <div class="metric-icon-box"><el-icon :size="24"><component :is="m.icon" /></el-icon></div>
              <div class="metric-num">{{ m.value }}</div>
              <div class="metric-label">{{ m.label }}</div>
            </div>
          </el-col>
        </el-row>

        <!-- 快捷功能金刚区 -->
        <el-card shadow="hover" class="quick-nav-card">
          <template #header>
            <div class="card-header"><span>快捷功能</span></div>
          </template>
          <el-row :gutter="16">
            <el-col :span="6" v-for="item in quickNavs" :key="item.title">
              <div class="nav-item" @click="handleNav(item.path)">
                <el-badge :value="item.badge" :hidden="!item.badge" :max="99">
                  <el-icon :class="item.colorClass" :size="30"><component :is="item.icon" /></el-icon>
                </el-badge>
                <p>{{ item.title }}</p>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 最近报修 -->
        <el-card shadow="hover" class="recent-card" v-if="recentRepairs.length > 0">
          <template #header>
            <div class="card-header">
              <span>最近报修</span>
              <el-button text type="primary" @click="$router.push('/student/repair')">全部</el-button>
            </div>
          </template>
          <div class="repair-list">
            <div v-for="r in recentRepairs" :key="r.id" class="repair-item" @click="$router.push('/student/repair')">
              <span class="r-type">{{ r.repairType }}</span>
              <span class="r-desc">{{ r.description }}</span>
              <el-tag :type="rStatusType(r.status)" size="small">{{ rStatusLabel(r.status) }}</el-tag>
              <span class="r-time">{{ r.createTime?.slice(0, 10) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <!-- 通知动态 -->
        <el-card shadow="hover" class="notice-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><Bell /></el-icon> 近期通知</span>
              <el-button text type="primary" @click="handleViewAllNotices">查看全部</el-button>
            </div>
          </template>
          <el-timeline v-if="notifications.length > 0">
            <el-timeline-item
              v-for="(item, index) in notifications"
              :key="index"
              :type="noticeTypeMap[item.type] || 'info'"
              :timestamp="formatRelativeTime(item.createTime)"
            >
              <p class="notice-title">{{ item.title }}</p>
              <p class="notice-content">{{ item.content }}</p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无通知" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Tools, Calendar, Search, ShoppingCart, Bell, Warning, Clock } from '@element-plus/icons-vue'
import { getStudentInfo } from '@/api/student'
import { getMyRepairPage } from '@/api/repair'
import { getMyNotifications } from '@/api/notification'
import { formatRelativeTime } from '@/utils/date'

const router = useRouter()

const studentInfo = ref({})
const greeting = ref('')
const todayStr = ref('')
const getGreeting = () => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了，早点休息 🌙'
  if (h < 9) return '早上好 ☀️'
  if (h < 12) return '上午好 🌤️'
  if (h < 14) return '中午好 🍚'
  if (h < 18) return '下午好 💪'
  return '晚上好 🌆'
}
const weekDays = ['日', '一', '二', '三', '四', '五', '六']

const fetchStudentInfo = async () => {
  try { studentInfo.value = await getStudentInfo() } catch (e) { console.error(e) }
}

// 统计数据
const stats = reactive({ total: 0, processing: 0, pendingReview: 0, resolved: 0 })
const unreadNotices = ref(0)
const recentRepairs = ref([])

const fetchStats = async () => {
  try {
    const [allRes, processingRes, completedRes] = await Promise.all([
      getMyRepairPage({ page: 1, pageSize: 1 }),
      getMyRepairPage({ page: 1, pageSize: 100, status: 1 }),
      getMyRepairPage({ page: 1, pageSize: 100, status: 3 })
    ])
    const inProgress = await getMyRepairPage({ page: 1, pageSize: 1, status: 2 })
    stats.total = allRes.total || 0
    stats.processing = (processingRes.total || 0) + (inProgress.total || 0)
    stats.pendingReview = (completedRes.records || []).filter(i => !i.evaluationScore).length
    stats.resolved = (completedRes.records || []).filter(i => i.evaluationScore).length
    recentRepairs.value = (allRes.records || []).slice(0, 5)
  } catch (e) { console.error(e) }
}

// 指标卡数据
const metrics = computed(() => [
  { icon: 'Warning', label: '处理中', value: stats.processing, color: 'metric-orange', link: '/student/repair' },
  { icon: 'Clock', label: '待评价', value: stats.pendingReview, color: 'metric-blue', link: '/student/repair' },
  { icon: 'Tools', label: '累计报修', value: stats.total, color: 'metric-purple' },
  { icon: 'Bell', label: '未读通知', value: unreadNotices.value, color: 'metric-red', link: '/student/notification' }
])

const pendingCount = computed(() => stats.pendingReview + unreadNotices.value)

// 通知
const notifications = ref([])
const noticeTypeMap = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'primary' }
const fetchNotifications = async () => {
  try {
    const list = await getMyNotifications() || []
    unreadNotices.value = list.filter(n => n.isRead === 0).length
    notifications.value = list.slice(0, 6)
  } catch (e) { console.error(e) }
}

const handleViewAllNotices = () => { router.push('/student/notification') }

const quickNavs = ref([
  { title: '宿舍报修', icon: 'Tools', path: '/student/repair', colorClass: 'color-primary', badge: stats.pendingReview || null },
  { title: '生活预约', icon: 'Calendar', path: '/student/reservation', colorClass: 'color-success', badge: null },
  { title: '失物招领', icon: 'Search', path: '/student/lost-found', colorClass: 'color-warning', badge: null },
  { title: '二手交易', icon: 'ShoppingCart', path: '/student/secondhand', colorClass: 'color-danger', badge: null }
])

const handleNav = (path) => { router.push(path) }

const rStatusLabel = (s) => ({ 0: '待处理', 1: '已接单', 2: '维修中', 3: '已完成', 4: '已取消' }[s] || '未知')
const rStatusType = (s) => ({ 0: 'info', 1: 'primary', 2: 'warning', 3: 'success', 4: 'danger' }[s] || 'info')

onMounted(() => {
  greeting.value = getGreeting()
  const now = new Date()
  todayStr.value = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 星期${weekDays[now.getDay()]}`
  fetchStudentInfo()
  fetchStats()
  fetchNotifications()
})
</script>

<style scoped>
.dashboard-container { padding: 10px; max-width: 1400px; }

/* 个人信息 */
.profile-card {
  margin-bottom: 16px;
  background: linear-gradient(135deg, #f0f7ff 0%, #e8f4e8 50%, #fff8f0 100%);
}
.profile-info { display: flex; align-items: center; }
.user-desc { margin-left: 20px; flex: 1; }
.greeting { margin: 0 0 4px 0; font-size: 20px; color: #303133; }
.today-info { margin: 0 0 6px 0; font-size: 13px; color: #909399; }
.role-tag { display: flex; align-items: center; gap: 8px; }
.detail { font-size: 13px; color: #909399; }
.profile-actions { flex-shrink: 0; }

/* 待处理汇总条 */
.pending-strip { display: flex; gap: 12px; margin-bottom: 16px; }
.pending-item {
  flex: 1; padding: 12px 20px; border-radius: 10px; cursor: pointer;
  display: flex; align-items: center; gap: 10px; transition: transform 0.3s;
}
.pending-item:hover { transform: translateY(-2px); }
.pending-repair { background: linear-gradient(135deg, #fef0f0, #fde2e2); }
.pending-notify { background: linear-gradient(135deg, #ecf5ff, #d9ecff); }
.pending-num { font-size: 28px; font-weight: 700; }
.pending-repair .pending-num { color: #f56c6c; }
.pending-notify .pending-num { color: #409eff; }
.pending-label { font-size: 14px; color: #606266; }

/* 指标卡 */
.metrics-row { margin-bottom: 16px; }
.metric-card {
  background: #fff; border-radius: 12px; padding: 18px 16px;
  text-align: center; cursor: pointer; transition: all 0.3s;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.metric-card:hover { transform: translateY(-3px); box-shadow: 0 6px 20px rgba(0,0,0,0.08); }
.metric-icon-box { margin-bottom: 8px; width: 44px; height: 44px; border-radius: 12px; display: inline-flex; align-items: center; justify-content: center; }
.metric-orange .metric-icon-box { background: rgba(230,162,60,0.12); color: #e6a23c; }
.metric-blue .metric-icon-box { background: rgba(64,158,255,0.12); color: #409eff; }
.metric-purple .metric-icon-box { background: rgba(170,59,255,0.12); color: #aa3bff; }
.metric-red .metric-icon-box { background: rgba(245,108,108,0.12); color: #f56c6c; }
.metric-num { font-size: 28px; font-weight: 700; color: #303133; }
.metric-label { font-size: 13px; color: #909399; margin-top: 4px; }

/* 快捷功能 */
.quick-nav-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; }
.nav-item {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 18px 0; cursor: pointer; border-radius: 10px; transition: all 0.3s;
}
.nav-item:hover { background: #f5f7fa; transform: translateY(-3px); }
.nav-item p { margin-top: 8px; font-size: 13px; color: #606266; }
.color-primary { color: #409EFF; }
.color-success { color: #67C23A; }
.color-warning { color: #E6A23C; }
.color-danger { color: #F56C6C; }

/* 最近报修 */
.recent-card { }
.repair-list { }
.repair-item {
  display: flex; align-items: center; gap: 12px; padding: 10px 0;
  border-bottom: 1px solid #f5f5f5; cursor: pointer; font-size: 13px;
}
.repair-item:last-child { border-bottom: none; }
.repair-item:hover { background: #fafafa; }
.r-type { font-weight: 600; color: #303133; min-width: 80px; }
.r-desc { flex: 1; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.r-time { color: #c0c4cc; min-width: 90px; text-align: right; }

/* 通知 */
.notice-card { height: calc(100vh - 200px); overflow-y: auto; }
.notice-title { margin: 0 0 4px 0; font-weight: 600; font-size: 14px; color: #303133; }
.notice-content { margin: 0; font-size: 13px; color: #909399; }
</style>
