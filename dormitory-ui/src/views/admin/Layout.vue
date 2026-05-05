<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <span class="logo-icon">🏠</span>
        <h2 v-if="!isCollapse">宿舍管理系统</h2>
        <h2 v-else>宿舍</h2>
      </div>
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
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/admin/repair">
          <el-icon><Tools /></el-icon>
          <span>工单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/device">
          <el-icon><Monitor /></el-icon>
          <span>设备管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/reservation">
          <el-icon><Calendar /></el-icon>
          <span>预约管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/worker">
          <el-icon><UserFilled /></el-icon>
          <span>维修员管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/student">
          <el-icon><Upload /></el-icon>
          <span>学生导入</span>
        </el-menu-item>
        <el-menu-item index="/admin/review">
          <el-icon><Warning /></el-icon>
          <span>内容审核</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-icon class="fold-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse"/><Expand v-else/>
          </el-icon>
          <span class="greeting">欢迎你，{{ userName }}管理员</span>
        </div>
        <div class="header-right">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notice-badge">
            <el-icon :size="22" style="cursor:pointer;" @click="router.push('/admin/reservation')"><Bell /></el-icon>
          </el-badge>
          <el-dropdown @command="handleCommand" style="margin-left: 20px;">
            <span class="el-dropdown-link avatar-wrapper">
              <el-avatar :size="36" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
              <el-icon class="el-icon--right"><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="changePassword">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>

  <ChangePassword v-model="pwdDialogVisible" role="admin" />
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataLine, Tools, Monitor, UserFilled, Upload, Warning, Calendar, Bell, Fold, Expand, CaretBottom } from '@element-plus/icons-vue'
import ChangePassword from '@/components/ChangePassword.vue'

const router = useRouter()
const route = useRoute()
const isCollapse = ref(false)
const toggleCollapse = () => { isCollapse.value = !isCollapse.value }
const currentPath = computed(() => route.path)
const userInfo = JSON.parse(localStorage.getItem('admin_userInfo') || '{}')
const userName = ref(userInfo.name || '管理员')
const pwdDialogVisible = ref(false)
const unreadCount = ref(0)

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_userInfo')
    ElMessage.success('已安全退出')
    router.push('/login')
  } else if (command === 'changePassword') {
    pwdDialogVisible.value = true
  }
}
</script>

<style scoped>
.layout-container { height: 100vh; width: 100vw; overflow: hidden; }
.aside { background-color: #304156; transition: width 0.3s; display: flex; flex-direction: column; }
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; overflow: hidden; background-color: #2b3643; display: flex; align-items: center; justify-content: center; gap: 8px; }
.logo-icon { font-size: 24px; }
.logo h2 { margin: 0; font-size: 17px; font-weight: 600; }
.el-menu-vertical { border-right: none; flex: 1; }
.main-container { display: flex; flex-direction: column; }
.header { height: 60px; background-color: #fff; box-shadow: 0 1px 4px rgba(0,21,41,0.08); display: flex; justify-content: space-between; align-items: center; padding: 0 20px; }
.header-left { display: flex; align-items: center; }
.fold-btn { font-size: 20px; cursor: pointer; margin-right: 20px; }
.greeting { font-size: 14px; color: #666; }
.notice-badge { margin-right: 4px; }
.avatar-wrapper { display: flex; align-items: center; cursor: pointer; }
.main-content { background-color: #f0f2f5; padding: 20px; box-sizing: border-box; }
.fade-transform-leave-active, .fade-transform-enter-active { transition: all 0.3s; }
.fade-transform-enter-from { opacity: 0; transform: translateX(-30px); }
.fade-transform-leave-to { opacity: 0; transform: translateX(30px); }
</style>
