<template>
  <div class="login-container">
    <el-card class="login-box" shadow="hover">
      <div class="login-header">
        <h2 class="title">宿舍报修与生活服务系统</h2>
        <p class="subtitle">一体化数字校园平台</p>
      </div>

      <!-- 角色切换 Tab -->
      <el-tabs v-model="activeRole" class="role-tabs" stretch @tab-change="handleTabChange">
        <el-tab-pane label="学生端" name="student"></el-tab-pane>
        <el-tab-pane label="维修员端" name="worker"></el-tab-pane>
        <el-tab-pane label="管理端" name="admin"></el-tab-pane>
      </el-tabs>

      <!-- 登录表单 -->
      <el-form 
        ref="loginFormRef" 
        :model="loginForm" 
        :rules="loginRules" 
        size="large"
      >
        <el-form-item prop="account">
          <el-input 
            v-model="loginForm.account" 
            :placeholder="accountPlaceholder" 
            clearable
            @keyup.enter="handleLogin"
          >
            <!-- 引入 Element Plus 的内置图标 -->
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password 
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            class="login-btn" 
            :loading="loading" 
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
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

// 响应式数据
const activeRole = ref('student')
const loading = ref(false)
const loginForm = ref({
  account: '',
  password: ''
})

// 动态计算占位符
const accountPlaceholder = computed(() => {
  return activeRole.value === 'student' ? '请输入学号' : '请输入账号'
})

// 表单校验规则
const loginRules = {
  account: [
    { required: true, message: '账号/学号不能为空', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ]
}

// 切换角色时清空表单和校验记录
const handleTabChange = () => {
  loginFormRef.value?.resetFields()
}

// 登录提交逻辑
const handleLogin = () => {
  loginFormRef.value?.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      let res = null
      
      // 数据适配：根据不同角色构造不同的请求参数体
      if (activeRole.value === 'student') {
        res = await studentLogin({ 
          studentNo: loginForm.value.account, 
          password: loginForm.value.password 
        })
      } else if (activeRole.value === 'worker') {
        res = await workerLogin({ 
          username: loginForm.value.account, 
          password: loginForm.value.password 
        })
      } else if (activeRole.value === 'admin') {
        res = await adminLogin({ 
          username: loginForm.value.account, 
          password: loginForm.value.password 
        })
      }

      // 登录成功处理
      ElMessage.success('登录成功')

      // 三端 token 分 key 存储，互不覆盖，允许同一设备同时登录三端
      const role = activeRole.value
      localStorage.setItem(`${role}_token`, res.token)
      localStorage.setItem(`${role}_userInfo`, JSON.stringify({ id: res.id, name: res.name, role }))

      // 路由跳转分流
      if (activeRole.value === 'student') {
        router.push('/student')
      } else if (activeRole.value === 'worker') {
        router.push('/worker')
      } else if (activeRole.value === 'admin') {
        router.push('/admin')
      }

    } catch (error) {
      console.error('登录失败:', error)
      // 错误提示由我们之前封装的 request.js 拦截器统一处理了，这里只需捕获异常防止崩溃
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
  /* 建议后续在 assets 目录下放一张校园背景图替换此处的渐变色 */
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.login-box {
  width: 420px;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 25px;
}

.title {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.subtitle {
  margin: 10px 0 0;
  font-size: 14px;
  color: #909399;
}

.role-tabs {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
  font-size: 16px;
  letter-spacing: 2px;
}
</style>