<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- 左侧：个人信息卡片 -->
      <el-col :span="8">
        <el-card shadow="hover" class="info-card">
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              accept=".jpg,.jpeg,.png,.gif,.webp"
            >
              <el-avatar :size="100" :src="info.avatar || defaultAvatar" class="avatar-img" />
              <div class="avatar-mask">
                <el-icon><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </el-upload>
            <h2>{{ info.name || '同学' }}</h2>
            <p class="student-no">学号：{{ info.studentNo }}</p>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-row"><span class="label">性别</span><span>{{ info.gender === '1' ? '男' : info.gender === '0' ? '女' : '--' }}</span></div>
            <div class="info-row"><span class="label">年级</span><span>{{ info.grade || '--' }}</span></div>
            <div class="info-row"><span class="label">手机号</span><span>{{ info.phone || '--' }}</span></div>
            <div class="info-row"><span class="label">宿舍</span><span>{{ info.buildingNo || '--' }} {{ info.roomNo || '--' }}</span></div>
          </div>
        </el-card>

        <!-- 快捷统计 -->
        <el-card shadow="hover" class="stats-card" style="margin-top: 20px;">
          <template #header><span style="font-weight: bold;">我的数据</span></template>
          <el-row>
            <el-col :span="8"><div class="stat-num">{{ stats.total }}</div><div class="stat-label">报修次数</div></el-col>
            <el-col :span="8"><div class="stat-num" style="color:#E6A23C;">{{ stats.processing }}</div><div class="stat-label">处理中</div></el-col>
            <el-col :span="8"><div class="stat-num" style="color:#67C23A;">{{ stats.done }}</div><div class="stat-label">已完成</div></el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- 右侧：编辑区域 -->
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
                <el-form-item label="性别">
                  <el-select v-model="editForm.gender" style="width: 100%;">
                    <el-option label="男" value="1" />
                    <el-option label="女" value="0" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="年级">
                  <el-select v-model="editForm.grade" style="width: 100%;">
                    <el-option label="大一" value="大一" />
                    <el-option label="大二" value="大二" />
                    <el-option label="大三" value="大三" />
                    <el-option label="大四" value="大四" />
                    <el-option label="研一" value="研一" />
                    <el-option label="研二" value="研二" />
                    <el-option label="研三" value="研三" />
                  </el-select>
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

        <!-- 个人记录（多Tab） -->
        <el-card shadow="hover" class="history-card" style="margin-top: 20px;">
          <template #header><span style="font-weight: bold;">我的记录</span></template>
          <el-tabs v-model="recordTab" @tab-change="onRecordTabChange">
            <el-tab-pane label="报修记录" name="repair">
              <el-table :data="recentRepairs" v-loading="repairsLoading" border stripe size="small">
                <el-table-column prop="repairType" label="故障类型" width="100" />
                <el-table-column prop="description" label="描述" show-overflow-tooltip />
                <el-table-column prop="addressSnapshot" label="地址" width="120" />
                <el-table-column label="状态" width="80" align="center">
                  <template #default="s">
                    <el-tag :type="rStatusType(s.row.status)" size="small">{{ rStatusLabel(s.row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="时间" width="160" />
              </el-table>
              <el-button text type="primary" style="margin-top: 8px;" @click="$router.push('/student/repair')">查看全部报修</el-button>
            </el-tab-pane>

            <el-tab-pane label="预约记录" name="reservation">
              <el-table :data="recentReservations" v-loading="reservationsLoading" border stripe size="small">
                <el-table-column label="设备" min-width="140">
                  <template #default="s">{{ s.row.deviceId ? '设备#' + s.row.deviceId : '--' }}</template>
                </el-table-column>
                <el-table-column prop="reservationDate" label="日期" width="110" />
                <el-table-column label="时段" width="160">
                  <template #default="s">{{ s.row.startTime }} - {{ s.row.endTime }}</template>
                </el-table-column>
                <el-table-column label="状态" width="80" align="center">
                  <template #default="s">
                    <el-tag :type="resStatusType(s.row.status)" size="small">{{ resStatusLabel(s.row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="时间" width="160" />
              </el-table>
              <el-button text type="primary" style="margin-top: 8px;" @click="$router.push('/student/reservation')">查看全部预约</el-button>
            </el-tab-pane>

            <el-tab-pane label="失物招领" name="lostfound">
              <el-table :data="recentLostFound" v-loading="lfLoading" border stripe size="small">
                <el-table-column label="类型" width="80" align="center">
                  <template #default="s">
                    <el-tag :type="s.row.type === 0 ? 'warning' : 'success'" size="small" effect="dark">
                      {{ s.row.type === 0 ? '寻物' : '招领' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="title" label="标题" show-overflow-tooltip />
                <el-table-column prop="category" label="分类" width="90" />
                <el-table-column label="状态" width="80" align="center">
                  <template #default="s">
                    <el-tag :type="lfStatusType(s.row.status)" size="small">{{ lfStatusLabel(s.row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="时间" width="160" />
              </el-table>
              <el-button text type="primary" style="margin-top: 8px;" @click="$router.push('/student/lost-found')">查看全部</el-button>
            </el-tab-pane>

            <el-tab-pane label="二手交易" name="secondhand">
              <el-table :data="recentSecondhand" v-loading="shLoading" border stripe size="small">
                <el-table-column prop="name" label="商品" show-overflow-tooltip />
                <el-table-column label="价格" width="90" align="center">
                  <template #default="s"><span style="color:#f56c6c;">¥{{ s.row.price }}</span></template>
                </el-table-column>
                <el-table-column label="状态" width="80" align="center">
                  <template #default="s">
                    <el-tag :type="shStatusType(s.row.status)" size="small">{{ shStatusLabel(s.row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="时间" width="160" />
              </el-table>
              <el-button text type="primary" style="margin-top: 8px;" @click="$router.push('/student/secondhand')">查看全部</el-button>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStudentInfo, updateStudentProfile, changeStudentPassword, updateAvatar } from '@/api/student'
import { Camera } from '@element-plus/icons-vue'
import { getMyRepairPage } from '@/api/repair'
import { getMyReservations } from '@/api/reservation'
import { getLostFoundPage } from '@/api/lostfound'
import { getSecondhandPage } from '@/api/secondhand'

const info = ref({})
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/common/upload'
const uploadHeaders = { token: localStorage.getItem('student_token') || '' }

const handleAvatarSuccess = async (resp) => {
  // el-upload 不走 axios 拦截器，resp 是完整的 {code, msg, data}
  const url = resp?.data
  if (!url) { ElMessage.error('上传失败'); return }
  await updateAvatar(url)
  info.value.avatar = url
  window.dispatchEvent(new CustomEvent('avatar-updated'))
  ElMessage.success('头像更新成功')
}
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  if (!isLt2M) { ElMessage.error('图片大小不能超过 2MB'); return false }
  return true
}

const loadInfo = async () => {
  try { info.value = await getStudentInfo() || {} }
  catch (e) { console.error(e) }
}

// 统计
const stats = reactive({ total: 0, processing: 0, done: 0 })
const loadStats = async () => {
  try {
    const [all, ing, done] = await Promise.all([
      getMyRepairPage({ page: 1, pageSize: 1 }),
      getMyRepairPage({ page: 1, pageSize: 1, status: 1 }),
      getMyRepairPage({ page: 1, pageSize: 1, status: 3 })
    ])
    stats.total = all.total || 0
    stats.processing = ing.total || 0
    stats.done = done.total || 0
  } catch (e) { console.error(e) }
}

// 编辑个人信息
const profileSaving = ref(false)
const editForm = reactive({ name: '', gender: '', grade: '', phone: '' })
const saveProfile = async () => {
  profileSaving.value = true
  try {
    await updateStudentProfile({ ...editForm })
    ElMessage.success('个人信息已更新')
    // 刷新显示
    info.value = { ...info.value, ...editForm }
  } catch (e) { console.error(e) } finally { profileSaving.value = false }
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
      await changeStudentPassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
      ElMessage.success('密码修改成功，请重新登录')
      localStorage.removeItem('student_token'); localStorage.removeItem('student_userInfo')
      setTimeout(() => window.location.href = '/login', 500)
    } catch (e) { console.error(e) } finally { pwdSaving.value = false }
  })
}

// Tab 切换懒加载
const recordTab = ref('repair')
const loadedTabs = ref({})

// 报修记录
const repairsLoading = ref(false)
const recentRepairs = ref([])
const loadRepairs = async () => {
  repairsLoading.value = true
  try {
    const res = await getMyRepairPage({ page: 1, pageSize: 5 })
    recentRepairs.value = res.records || []
  } catch (e) { console.error(e) } finally { repairsLoading.value = false }
}
const rStatusLabel = (s) => ({ 0: '待处理', 1: '已接单', 2: '维修中', 3: '已完成', 4: '已取消' }[s] || '未知')
const rStatusType = (s) => ({ 0: 'info', 1: 'primary', 2: 'warning', 3: 'success', 4: 'danger' }[s] || 'info')

// 预约记录
const reservationsLoading = ref(false)
const recentReservations = ref([])
const loadReservations = async () => {
  reservationsLoading.value = true
  try {
    const res = await getMyReservations({ page: 1, pageSize: 5 })
    recentReservations.value = res.records || []
  } catch (e) { console.error(e) } finally { reservationsLoading.value = false }
}
const resStatusLabel = (s) => ({ 0: '预约中', 1: '已完成', 2: '已取消' }[s] || '未知')
const resStatusType = (s) => ({ 0: 'primary', 1: 'success', 2: 'info' }[s] || 'info')

// 失物招领记录
const lfLoading = ref(false)
const recentLostFound = ref([])
const loadLostFound = async () => {
  lfLoading.value = true
  try {
    const res = await getLostFoundPage({ page: 1, pageSize: 5, studentId: info.value.id })
    recentLostFound.value = res.records || []
  } catch (e) { console.error(e) } finally { lfLoading.value = false }
}
const lfStatusLabel = (s) => ({ 0: '寻找中', 1: '已解决', 2: '已撤销' }[s] || '未知')
const lfStatusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'info' }[s] || 'info')

// 二手交易记录
const shLoading = ref(false)
const recentSecondhand = ref([])
const loadSecondhand = async () => {
  shLoading.value = true
  try {
    const res = await getSecondhandPage({ page: 1, pageSize: 5, studentId: info.value.id })
    recentSecondhand.value = res.records || []
  } catch (e) { console.error(e) } finally { shLoading.value = false }
}
const shStatusLabel = (s) => ({ 0: '在售', 1: '已售出', 2: '已下架' }[s] || '未知')
const shStatusType = (s) => ({ 0: 'success', 1: 'info', 2: 'danger' }[s] || 'info')

const loaders = { repair: loadRepairs, reservation: loadReservations, lostfound: loadLostFound, secondhand: loadSecondhand }
const onRecordTabChange = (name) => {
  if (!loadedTabs.value[name]) {
    loadedTabs.value[name] = true
    loaders[name]()
  }
}

onMounted(async () => {
  await loadInfo()
  editForm.name = info.value.name || ''
  editForm.gender = info.value.gender || ''
  editForm.grade = info.value.grade || ''
  editForm.phone = info.value.phone || ''
  loadStats()
  loadRepairs()
  loadedTabs.value.repair = true
})
</script>

<style scoped>
.profile-page { }
.info-card { text-align: center; }
.avatar-section { padding: 20px 0; }
.avatar-section h2 { margin: 12px 0 4px; font-size: 22px; }
.avatar-uploader { display: inline-block; position: relative; cursor: pointer; }
.avatar-uploader:hover .avatar-mask { opacity: 1; }
.avatar-mask {
  position: absolute; inset: 0; border-radius: 50%;
  background: rgba(0,0,0,0.4); color: #fff;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  font-size: 13px; gap: 4px; opacity: 0; transition: opacity 0.3s;
}
.student-no { color: #909399; font-size: 14px; }
.info-list { text-align: left; }
.info-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f2f2f2; font-size: 14px; }
.info-row:last-child { border-bottom: none; }
.info-row .label { color: #909399; }
.stats-card { text-align: center; }
.stat-num { font-size: 28px; font-weight: 700; }
.stat-label { font-size: 12px; color: #909399; margin-top: 4px; }
.edit-card, .pwd-card, .history-card { }
</style>
