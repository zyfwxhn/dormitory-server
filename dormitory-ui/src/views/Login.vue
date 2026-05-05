<template>
  <div class="login-container">
    <!-- 装饰背景 -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
    </div>

    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">🏠</div>
        <h2 class="title">宿舍报修与生活服务系统</h2>
        <p class="subtitle">Dormitory Repair &amp; Life Service Platform</p>
      </div>

      <!-- 角色切换 -->
      <div class="role-switch">
        <button
          v-for="r in roles" :key="r.key"
          :class="['role-btn', { active: activeRole === r.key }]"
          @click="switchRole(r.key)"
        >{{ r.label }}</button>
      </div>

      <!-- 登录表单 -->
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" size="large">
        <el-form-item prop="account">
          <el-input
            v-model="loginForm.account"
            :placeholder="accountPlaceholder"
            :prefix-icon="User"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            :prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin" round>
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { studentLogin, workerLogin, adminLogin } from '@/api/login'

const router = useRouter()
const loginFormRef = ref(null)
const activeRole = ref('student')
const loading = ref(false)
const loginForm = ref({ account: '', password: '' })

const roles = [
  { key: 'student', label: '🎓 学生端' },
  { key: 'worker', label: '🔧 维修员端' },
  { key: 'admin', label: '⚙️ 管理端' }
]

const accountPlaceholder = computed(() =>
  activeRole.value === 'student' ? '请输入学号' : '请输入账号'
)

const loginRules = {
  account: [{ required: true, message: '账号/学号不能为空', trigger: 'blur' }],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ]
}

const switchRole = (key) => {
  activeRole.value = key
  loginFormRef.value?.resetFields()
}

const handleLogin = () => {
  loginFormRef.value?.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      let res
      if (activeRole.value === 'student') {
        res = await studentLogin({ studentNo: loginForm.value.account, password: loginForm.value.password })
      } else if (activeRole.value === 'worker') {
        res = await workerLogin({ username: loginForm.value.account, password: loginForm.value.password })
      } else {
        res = await adminLogin({ username: loginForm.value.account, password: loginForm.value.password })
      }

      ElMessage.success({ message: '登录成功，欢迎回来！', duration: 1500 })
      const role = activeRole.value
      localStorage.setItem(`${role}_token`, res.token)
      localStorage.setItem(`${role}_userInfo`, JSON.stringify({ id: res.id, name: res.name, role }))

      const routes = { student: '/student', worker: '/worker', admin: '/admin' }
      setTimeout(() => router.push(routes[role]), 200)
    } catch (error) {
      console.error('登录失败:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #0f2027 0%, #203a43 40%, #2c5364 100%);
  overflow: hidden;
  position: relative;
}

/* 装饰形状 */
.bg-shapes { position: absolute; inset: 0; pointer-events: none; }
.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
  background: #fff;
}
.shape-1 { width: 400px; height: 400px; top: -100px; right: -80px; }
.shape-2 { width: 300px; height: 300px; bottom: -60px; left: -60px; }
.shape-3 { width: 200px; height: 200px; top: 50%; left: 10%; transform: translateY(-50%); }
.shape-4 { width: 160px; height: 160px; top: 20%; right: 15%; }

/* 毛玻璃卡片 */
.login-card {
  width: 420px;
  padding: 40px 36px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
}

.login-header { text-align: center; margin-bottom: 28px; }
.logo-icon { font-size: 44px; margin-bottom: 8px; }
.title { margin: 0 0 6px; font-size: 22px; color: #fff; font-weight: 600; letter-spacing: 1px; }
.subtitle { margin: 0; font-size: 12px; color: rgba(255,255,255,0.5); letter-spacing: 2px; text-transform: uppercase; }

/* 角色切换按钮组 */
.role-switch { display: flex; gap: 6px; margin-bottom: 24px; }
.role-btn {
  flex: 1;
  padding: 10px 0;
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 10px;
  background: transparent;
  color: rgba(255,255,255,0.6);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}
.role-btn:hover { border-color: rgba(255,255,255,0.4); color: #fff; }
.role-btn.active {
  background: rgba(255,255,255,0.15);
  border-color: rgba(255,255,255,0.5);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 2px 12px rgba(0,0,0,0.15);
}

.login-btn {
  width: 100%;
  margin-top: 8px;
  font-size: 16px;
  letter-spacing: 4px;
  height: 46px;
}

:deep(.el-input__wrapper) {
  background: rgba(255,255,255,0.1) !important;
  border: 1px solid rgba(255,255,255,0.2) !important;
  box-shadow: none !important;
  border-radius: 10px !important;
}
:deep(.el-input__inner) {
  color: #fff !important;
}
:deep(.el-input__inner::placeholder) {
  color: rgba(255,255,255,0.4) !important;
}
:deep(.el-input__prefix) { color: rgba(255,255,255,0.5) !important; }
:deep(.el-form-item__error) { color: rgba(255,150,150,0.9) !important; }
</style>
