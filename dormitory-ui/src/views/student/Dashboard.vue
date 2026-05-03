<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <!-- 左侧区域：个人信息 + 快捷功能 -->
      <el-col :span="16">
        <!-- 个人资料卡片 -->
        <el-card shadow="hover" class="profile-card">
          <div class="profile-info">
            <el-avatar :size="80" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            <div class="user-desc">
              <h2 class="greeting">Hi, {{ studentInfo.name || '同学' }}！今天也是充满活力的一天。</h2>
              <p class="role-tag">
                <el-tag type="success" effect="dark" round>学生端</el-tag>
                <span class="detail">学号：{{ studentInfo.studentNo || '--' }} | {{ studentInfo.grade || '' }} {{ studentInfo.buildingNo || '' }}号楼 {{ studentInfo.roomNo || '' }}室</span>
              </p>
            </div>
          </div>
        </el-card>

        <!-- 快捷功能金刚区 -->
        <el-card shadow="hover" class="quick-nav-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><Compass /></el-icon> 快捷功能</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="6" v-for="(item, index) in quickNavs" :key="index">
              <div class="nav-item" @click="handleNav(item.path)">
                <el-icon :class="item.colorClass" :size="32"><component :is="item.icon" /></el-icon>
                <p>{{ item.title }}</p>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 数据概览 -->
        <el-card shadow="hover" class="data-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><DataLine /></el-icon> 我的报修概览</span>
            </div>
          </template>
          <el-row>
            <el-col :span="8">
              <el-statistic title="累计提交报修" :value="stats.total" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="处理中" :value="stats.processing" value-style="color: #E6A23C;" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="待评价" :value="stats.pendingReview" value-style="color: #67C23A;" />
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- 右侧区域：系统通知动态 -->
      <el-col :span="8">
        <el-card shadow="hover" class="notice-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><Bell /></el-icon> 近期通知动态</span>
              <el-button text type="primary" @click="handleViewAllNotices">查看全部</el-button>
            </div>
          </template>
          <el-timeline v-if="notifications.length > 0">
            <el-timeline-item
              v-for="(item, index) in notifications"
              :key="index"
              :type="noticeTypeMap[item.type] || 'info'"
              :timestamp="formatTime(item.createTime)"
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Compass, DataLine, Bell, Tools, Calendar, Search, ShoppingCart } from '@element-plus/icons-vue'
import { getStudentInfo } from '@/api/student'
import { getMyRepairPage } from '@/api/repair'
import { getMyNotifications } from '@/api/notification'

const router = useRouter()

// 学生信息
const studentInfo = ref({})
const fetchStudentInfo = async () => {
  try {
    studentInfo.value = await getStudentInfo()
  } catch (e) {
    console.error('获取学生信息失败', e)
  }
}

// 报修统计
const stats = reactive({ total: 0, processing: 0, pendingReview: 0 })
const fetchStats = async () => {
  try {
    const [allRes, acceptedRes, inProgressRes, completedRes] = await Promise.all([
      getMyRepairPage({ page: 1, pageSize: 1 }),
      getMyRepairPage({ page: 1, pageSize: 1, status: 1 }),
      getMyRepairPage({ page: 1, pageSize: 1, status: 2 }),
      getMyRepairPage({ page: 1, pageSize: 100, status: 3 })
    ])
    stats.total = allRes.total || 0
    stats.processing = (acceptedRes.total || 0) + (inProgressRes.total || 0)
    stats.pendingReview = (completedRes.records || []).filter(i => !i.evaluationScore).length
  } catch (e) {
    console.error('获取统计数据失败', e)
  }
}

// 通知
const notifications = ref([])
const noticeTypeMap = { 1: 'primary', 2: 'success', 3: 'warning' }
const fetchNotifications = async () => {
  try {
    const list = await getMyNotifications()
    notifications.value = (list || []).slice(0, 6)
  } catch (e) {
    console.error('获取通知失败', e)
  }
}

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

const handleViewAllNotices = () => {
  router.push('/student/notification')
}

// 快捷导航
const quickNavs = ref([
  { title: '发起报修', icon: 'Tools', path: '/student/repair', colorClass: 'color-primary' },
  { title: '生活预约', icon: 'Calendar', path: '/student/reservation', colorClass: 'color-success' },
  { title: '失物招领', icon: 'Search', path: '/student/lost-found', colorClass: 'color-warning' },
  { title: '二手交易', icon: 'ShoppingCart', path: '/student/secondhand', colorClass: 'color-danger' }
])

const handleNav = (path) => {
  router.push(path)
}

onMounted(() => {
  fetchStudentInfo()
  fetchStats()
  fetchNotifications()
})
</script>

<style scoped>
.dashboard-container {
  padding: 10px;
}

.profile-card {
  margin-bottom: 20px;
  background-image: linear-gradient(to right, #ffffff, #f0f7ff);
}

.profile-info {
  display: flex;
  align-items: center;
}

.user-desc {
  margin-left: 25px;
}

.greeting {
  margin: 0 0 10px 0;
  font-size: 22px;
  color: #303133;
}

.role-tag {
  margin: 0;
  display: flex;
  align-items: center;
}

.detail {
  margin-left: 10px;
  font-size: 14px;
  color: #909399;
}

.quick-nav-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s;
}

.nav-item:hover {
  background-color: #f5f7fa;
  transform: translateY(-3px);
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.nav-item p {
  margin-top: 10px;
  font-size: 14px;
  color: #606266;
}

.color-primary { color: #409EFF; }
.color-success { color: #67C23A; }
.color-warning { color: #E6A23C; }
.color-danger { color: #F56C6C; }

.notice-card {
  height: calc(100vh - 120px);
  overflow-y: auto;
}

.notice-title {
  margin: 0 0 4px 0;
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}
.notice-content {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

:deep(.el-statistic__content) {
  font-size: 28px;
  font-weight: bold;
}
</style>
