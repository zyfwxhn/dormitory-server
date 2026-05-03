<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- 左侧：个人信息卡片 -->
      <el-col :span="8">
        <el-card shadow="hover" class="info-card">
          <div class="avatar-section">
            <el-avatar :size="100" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            <h2>{{ info.name || '维修员' }}</h2>
            <p class="worker-id">工号：{{ info.username }}</p>
            <el-tag :type="info.isAvailable === 1 ? 'success' : 'danger'" style="margin-top: 8px;">
              {{ info.isAvailable === 1 ? '在岗' : '离岗' }}
            </el-tag>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-row"><span class="label">手机号</span><span>{{ info.phone || '--' }}</span></div>
            <div class="info-row"><span class="label">擅长领域</span><span>{{ info.skills || '未填写' }}</span></div>
          </div>
        </el-card>

        <!-- 工作统计 -->
        <el-card shadow="hover" class="stats-card" style="margin-top: 20px;">
          <template #header><span style="font-weight: bold;">工作统计</span></template>
          <el-row>
            <el-col :span="8"><div class="stat-num">{{ stats.done }}</div><div class="stat-label">累计完工</div></el-col>
            <el-col :span="8"><div class="stat-num" style="color:#E6A23C;">{{ stats.inProgress }}</div><div class="stat-label">进行中</div></el-col>
            <el-col :span="8"><div class="stat-num" style="color:#409EFF;">{{ stats.accepted }}</div><div class="stat-label">已接单</div></el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- 右侧：功能区 -->
      <el-col :span="16">
        <!-- 编辑个人信息 -->
        <el-card shadow="hover" class="edit-card">
          <template #header><span style="font-weight: bold;">编辑个人信息</span></template>
          <el-form :model="editForm" label-width="80px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="姓名">
                  <el-input v-model="editForm.name" placeholder="你的姓名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号">
                  <el-input v-model="editForm.phone" placeholder="手机号" maxlength="11" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-button type="primary" :loading="profileSaving" @click="saveProfile">保存个人信息</el-button>
          </el-form>
        </el-card>

        <!-- 擅长领域 -->
        <el-card shadow="hover" class="skills-card" style="margin-top: 20px;">
          <template #header><span style="font-weight: bold;">擅长领域</span></template>
          <el-select v-model="selectedSkills" multiple filterable allow-create placeholder="选择或输入擅长的工种" style="width: 100%;">
            <el-option v-for="s in skillOptions" :key="s" :label="s" :value="s" />
          </el-select>
          <el-button type="primary" style="margin-top: 12px;" :loading="skillsSaving" @click="saveSkills">保存擅长领域</el-button>
        </el-card>

        <!-- 修改密码 -->
        <el-card shadow="hover" class="pwd-card" style="margin-top: 20px;">
          <template #header><span style="font-weight: bold;">修改密码</span></template>
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入" />
            </el-form-item>
            <el-button type="primary" :loading="pwdSaving" @click="changePassword">修改密码</el-button>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { getWorkerRepairPage } from '@/api/worker'

const info = ref({})
const loadInfo = async () => {
  try { info.value = await request({ url: '/worker/info', method: 'get' }) || {} }
  catch (e) { console.error(e) }
}

// 统计
const stats = reactive({ done: 0, inProgress: 0, accepted: 0 })
const loadStats = async () => {
  try {
    const [accepted, inProgress, done] = await Promise.all([
      getWorkerRepairPage({ page: 1, pageSize: 1, status: 1 }),
      getWorkerRepairPage({ page: 1, pageSize: 1, status: 2 }),
      getWorkerRepairPage({ page: 1, pageSize: 1, status: 3 })
    ])
    stats.accepted = accepted.total || 0
    stats.inProgress = inProgress.total || 0
    stats.done = done.total || 0
  } catch (e) { console.error(e) }
}

// 编辑个人信息
const profileSaving = ref(false)
const editForm = reactive({ name: '', phone: '' })
const saveProfile = async () => {
  profileSaving.value = true
  try {
    await request({ url: '/worker/profile', method: 'put', data: { ...editForm } })
    ElMessage.success('个人信息已更新')
    info.value = { ...info.value, ...editForm }
  } catch (e) { console.error(e) } finally { profileSaving.value = false }
}

// 擅长领域
const skillsSaving = ref(false)
const selectedSkills = ref([])
const skillOptions = ['电工', '木工', '水暖工', '油漆工', '瓦工', '门窗维修', '管道疏通', '空调维修', '网络布线', '家具维修', '锁具维修', '墙面修补']

const loadSkills = () => {
  selectedSkills.value = info.value.skills ? info.value.skills.split(',') : []
}
const saveSkills = async () => {
  skillsSaving.value = true
  try {
    await request({ url: '/worker/skills', method: 'put', params: { skills: selectedSkills.value.join(',') } })
    ElMessage.success('擅长领域已更新')
    info.value.skills = selectedSkills.value.join(',')
  } catch (e) { console.error(e) } finally { skillsSaving.value = false }
}

// 修改密码
const pwdSaving = ref(false)
const pwdFormRef = ref(null)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 6, message: '新密码至少6位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: (r, v, cb) => v === pwdForm.newPassword ? cb() : cb(new Error('两次密码不一致')), trigger: 'blur' }
  ]
}
const changePassword = () => {
  pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    pwdSaving.value = true
    try {
      await request({ url: '/worker/password', method: 'put', data: { oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword } })
      ElMessage.success('密码修改成功，请重新登录')
      localStorage.removeItem('worker_token'); localStorage.removeItem('worker_userInfo')
      setTimeout(() => window.location.href = '/login', 500)
    } catch (e) { console.error(e) } finally { pwdSaving.value = false }
  })
}

onMounted(async () => {
  await loadInfo()
  editForm.name = info.value.name || ''
  editForm.phone = info.value.phone || ''
  loadSkills()
  loadStats()
})
</script>

<style scoped>
.profile-page { }
.info-card { text-align: center; }
.avatar-section { padding: 20px 0; }
.avatar-section h2 { margin: 12px 0 4px; font-size: 22px; }
.worker-id { color: #909399; font-size: 14px; }
.info-list { text-align: left; }
.info-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f2f2f2; font-size: 14px; }
.info-row:last-child { border-bottom: none; }
.info-row .label { color: #909399; }
.stats-card { text-align: center; }
.stat-num { font-size: 28px; font-weight: 700; }
.stat-label { font-size: 12px; color: #909399; margin-top: 4px; }
.edit-card, .skills-card, .pwd-card { }
</style>
