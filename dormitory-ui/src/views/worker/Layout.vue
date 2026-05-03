<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <h2 v-if="!isCollapse">维修员工作台</h2>
        <h2 v-else>维修</h2>
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
        <el-menu-item index="/worker/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/worker/repair">
          <el-icon><Tools /></el-icon>
          <span>工单管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-icon class="fold-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse"/><Expand v-else/>
          </el-icon>
          <span class="greeting">欢迎你，{{ userName }}师傅</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link avatar-wrapper">
              <el-avatar :size="36" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
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
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataLine, Tools, Fold, Expand, CaretBottom } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const isCollapse = ref(false)
const toggleCollapse = () => { isCollapse.value = !isCollapse.value }
const currentPath = computed(() => route.path)
const userInfo = JSON.parse(localStorage.getItem('worker_userInfo') || '{}')
const userName = ref(userInfo.name || '维修员')

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('worker_token')
    localStorage.removeItem('worker_userInfo')
    ElMessage.success('已安全退出')
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/worker/profile')
  }
}
</script>

<style scoped>
.layout-container { height: 100vh; width: 100vw; overflow: hidden; }
.aside { background-color: #304156; transition: width 0.3s; display: flex; flex-direction: column; }
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; overflow: hidden; background-color: #2b3643; }
.logo h2 { margin: 0; font-size: 18px; font-weight: 600; }
.el-menu-vertical { border-right: none; flex: 1; }
.main-container { display: flex; flex-direction: column; }
.header { height: 60px; background-color: #fff; box-shadow: 0 1px 4px rgba(0,21,41,0.08); display: flex; justify-content: space-between; align-items: center; padding: 0 20px; }
.header-left { display: flex; align-items: center; }
.fold-btn { font-size: 20px; cursor: pointer; margin-right: 20px; }
.greeting { font-size: 14px; color: #666; }
.avatar-wrapper { display: flex; align-items: center; cursor: pointer; }
.main-content { background-color: #f0f2f5; padding: 20px; box-sizing: border-box; }
.fade-transform-leave-active, .fade-transform-enter-active { transition: all 0.3s; }
.fade-transform-enter-from { opacity: 0; transform: translateX(-30px); }
.fade-transform-leave-to { opacity: 0; transform: translateX(30px); }
</style>
