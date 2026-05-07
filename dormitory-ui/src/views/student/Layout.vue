<template>
  <el-container class="layout-container">
    <!-- 左侧侧边栏 -->
    <el-aside width="220px" class="aside">
      <div class="logo">
        <span class="logo-icon">🎓</span>
        <h2 v-if="!isCollapse">学生服务中心</h2>
        <h2 v-else>学生</h2>
      </div>
      <!-- router 属性开启后，点击菜单项会自动根据 index 进行路由跳转 -->
      <el-menu
        :default-active="currentPath"
        router
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        :collapse="isCollapse"
        :collapse-transition="false"
      >
        <el-menu-item index="/student/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>首页看板</span>
        </el-menu-item>
        <el-menu-item index="/student/repair">
          <el-icon><Tools /></el-icon>
          <span>宿舍报修</span>
        </el-menu-item>
        <el-menu-item index="/student/reservation">
          <el-icon><Calendar /></el-icon>
          <span>生活预约</span>
        </el-menu-item>
        <el-menu-item index="/student/lost-found">
          <el-icon><Search /></el-icon>
          <span>失物招领</span>
        </el-menu-item>
        <el-menu-item index="/student/secondhand">
          <el-icon><ShoppingCart /></el-icon>
          <span>二手交易</span>
        </el-menu-item>
        <el-menu-item index="/student/notification">
          <el-icon><Bell /></el-icon>
          <span>通知中心</span>
          <el-badge v-if="unreadCount > 0" :value="unreadCount" class="menu-badge" />
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧主体架构 -->
    <el-container class="main-container">
      <!-- 顶部 Header -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="fold-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse"/>
            <Expand v-else/>
          </el-icon>
          <span class="greeting">欢迎你，{{ userName }}同学</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link avatar-wrapper">
              <el-avatar :size="36" :src="userAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
              <el-icon class="el-icon--right"><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 路由出口 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import {
  DataLine, Tools, Calendar, Search, ShoppingCart, Bell,
  Fold, Expand, CaretBottom
} from '@element-plus/icons-vue'
import { getMyNotifications } from '@/api/notification'
import { getStudentInfo } from '@/api/student'
import { connectWebSocket, disconnectWebSocket } from '@/utils/websocket'

const router = useRouter()
const route = useRoute()

const isCollapse = ref(false)
const toggleCollapse = () => { isCollapse.value = !isCollapse.value }
const currentPath = computed(() => route.path)

const userInfo = JSON.parse(localStorage.getItem('student_userInfo') || '{}')
const userName = ref(userInfo.name || '未知')
const userAvatar = ref('')

const refreshAvatar = async () => {
  try {
    const info = await getStudentInfo()
    if (info?.avatar) userAvatar.value = info.avatar
  } catch (e) { /* ignore */ }
}
const unreadCount = ref(0)

const fetchUnreadCount = async () => {
  try {
    const list = await getMyNotifications() || []
    unreadCount.value = list.filter(n => n.isRead === 0).length
  } catch (e) { /* ignore */ }
}

// WebSocket 实时通知
const onWsMessage = (e) => {
  fetchUnreadCount()
  // 报修状态变更时弹出浏览器通知
  ElNotification.info({ title: '新通知', message: e.detail, duration: 4000 })
  // 触发全局刷新事件，报修/预约等页面自行监听
  window.dispatchEvent(new CustomEvent('repair-status-changed'))
}

onMounted(async () => {
  fetchUnreadCount()
  refreshAvatar()
  const sid = userInfo.id
  if (sid) {
    connectWebSocket(sid)
  }
  window.addEventListener('ws-message', onWsMessage)
  window.addEventListener('notification-read', fetchUnreadCount)
  window.addEventListener('avatar-updated', refreshAvatar)
})

onUnmounted(() => {
  window.removeEventListener('ws-message', onWsMessage)
  window.removeEventListener('notification-read', fetchUnreadCount)
  window.removeEventListener('avatar-updated', refreshAvatar)
})

const handleCommand = (command) => {
  if (command === 'logout') {
    disconnectWebSocket()
    localStorage.removeItem('student_token')
    localStorage.removeItem('student_userInfo')
    ElMessage.success('已安全退出')
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/student/profile')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.aside {
  background-color: #304156;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  display: flex; align-items: center; justify-content: center; gap: 8px;
  overflow: hidden;
  background-color: #2b3643;
}
.logo-icon { font-size: 24px; }
.logo h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.el-menu-vertical {
  border-right: none;
  flex: 1;
}

.main-container {
  display: flex;
  flex-direction: column;
}

.header {
  height: 60px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.fold-btn {
  font-size: 20px;
  cursor: pointer;
  margin-right: 20px;
}

.greeting {
  font-size: 14px;
  color: #666;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  box-sizing: border-box;
}

.menu-badge {
  position: absolute;
  right: 24px;
  top: 50%;
  transform: translateY(-50%);
}

/* 路由切换动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>