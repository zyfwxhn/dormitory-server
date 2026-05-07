<template>
  <div class="app-container">
    <!-- 筛选区 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-wrapper">
        <div class="filter-left">
          <el-select v-model="queryParams.type" placeholder="信息类型" clearable @change="fetchData" style="width: 140px;">
            <el-option label="寻物启事" :value="0" />
            <el-option label="失物招领" :value="1" />
          </el-select>
          <el-select v-model="queryParams.category" placeholder="物品分类" clearable @change="fetchData" style="width: 140px; margin-left: 10px;">
            <el-option label="校园卡" value="校园卡" />
            <el-option label="数码产品" value="数码产品" />
            <el-option label="书籍资料" value="书籍资料" />
            <el-option label="生活用品" value="生活用品" />
            <el-option label="服饰配饰" value="服饰配饰" />
            <el-option label="其他" value="其他" />
          </el-select>
          <el-input v-model="queryParams.title" placeholder="搜索标题" clearable @keyup.enter="fetchData" style="width: 200px; margin-left: 10px;">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" icon="Search" @click="fetchData" style="margin-left: 10px;">搜索</el-button>
        </div>
        <el-button :type="myMode ? 'warning' : 'default'" @click="toggleMyMode" style="margin-right: 10px;">{{ myMode ? '我的发布' : '全部信息' }}</el-button>
        <el-button type="primary" icon="Plus" @click="openPublish">发布信息</el-button>
      </div>
    </el-card>

    <!-- 卡片网格 -->
    <div v-loading="loading" class="card-grid">
      <el-empty v-if="!loading && tableData.length === 0" description="暂无信息" :image-size="100" />
      <div v-for="item in tableData" :key="item.id" class="lf-card" @click="openDetail(item)">
        <div class="card-img">
          <el-image v-if="item.images" :src="item.images.split(',')[0]" fit="cover" class="card-cover" />
          <div v-else class="card-img-placeholder" :class="'cat-' + (item.category || '其他')">
            <span class="cat-icon">{{ catIcon(item.category) }}</span>
          </div>
          <span class="card-type-tag" :class="item.type === 0 ? 'type-lost' : 'type-found'">
            {{ item.type === 0 ? '寻物' : '招领' }}
          </span>
          <span v-if="item.status !== 0" class="card-status-tag">{{ statusLabel(item.status) }}</span>
        </div>
        <div class="card-body">
          <div class="card-seller">
            <el-avatar :size="20" :src="item.studentAvatar || defaultAvatar" />
            <span>{{ item.studentName || '用户' }}</span>
          </div>
          <div class="card-name">{{ item.title }}</div>
          <div class="card-footer">
            <el-tag size="small" type="info">{{ item.category }}</el-tag>
            <span class="card-location" v-if="item.location">{{ item.location }}</span>
          </div>
          <div class="card-actions" v-if="item.studentId === currentUserId && item.status === 0" @click.stop>
            <el-button type="warning" size="small" @click="openEdit(item)">编辑</el-button>
            <el-button type="success" size="small" @click="handleSolved(item.id)">标记解决</el-button>
            <el-button type="danger" size="small" @click="handleCancel(item.id)">撤销</el-button>
          </div>
          <div class="card-claim" v-if="item.status === 0 && item.studentId !== currentUserId" @click.stop>
            <el-button :type="item.type === 1 ? 'success' : 'warning'" size="small" @click="handleClaim(item)">
              {{ item.type === 1 ? '我想认领' : '我捡到了' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <!-- 发布抽屉 -->
    <el-drawer v-model="drawerVisible" :title="editId ? '编辑信息' : '发布信息'" size="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" label-position="top">
        <el-form-item label="信息类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="0">寻物启事（我丢了东西）</el-radio>
            <el-radio :value="1">失物招领（我捡到东西）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="简要描述，如：丢失蓝色校园卡" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="物品分类" prop="category">
          <el-select v-model="form.category" placeholder="选择分类" style="width: 100%;">
            <el-option label="校园卡" value="校园卡" />
            <el-option label="数码产品" value="数码产品" />
            <el-option label="书籍资料" value="书籍资料" />
            <el-option label="生活用品" value="生活用品" />
            <el-option label="服饰配饰" value="服饰配饰" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="描述物品特征、丢失/捡到的时间地点等信息..." />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location" placeholder="如：教学楼A座3楼" />
        </el-form-item>
        <el-form-item label="联系方式" prop="contactInfo">
          <el-input v-model="form.contactInfo" placeholder="手机号或微信号" />
        </el-form-item>
        <el-form-item label="物品照片（最多3张）">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :limit="3"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">发布</el-button>
      </template>
    </el-drawer>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="详情" width="560px">
      <el-descriptions :column="2" border v-if="detailData.id">
        <el-descriptions-item label="类型" :span="1">
          <el-tag :type="detailData.type === 0 ? 'warning' : 'success'" effect="dark">
            {{ detailData.type === 0 ? '寻物启事' : '失物招领' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="分类">{{ detailData.category }}</el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detailData.description }}</el-descriptions-item>
        <el-descriptions-item label="发布人">{{ detailData.studentName || '--' }} ({{ detailData.studentNo || '--' }})</el-descriptions-item>
        <el-descriptions-item v-if="detailData.studentId === currentUserId" label="联系方式">{{ detailData.contactInfo }}</el-descriptions-item>
        <el-descriptions-item label="地点">{{ detailData.location || '--' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detailData.status)">{{ statusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.images" label="照片" :span="2">
          <div class="detail-images">
            <el-image v-for="(url, idx) in detailData.images.split(',')" :key="idx" :src="url" style="width:100px;height:100px;margin-right:8px;" :preview-src-list="detailData.images.split(',')" fit="cover" />
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getLostFoundPage, publishLostFound, editLostFound, updateLostFoundStatus, claimLostFound } from '@/api/lostfound'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const myMode = ref(false)
const currentUserId = ref(null)
const queryParams = reactive({ page: 1, pageSize: 12, type: null, category: null, title: null })

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const catIcon = (cat) => ({ '校园卡': '💳', '数码产品': '📱', '书籍资料': '📚', '生活用品': '🧹', '服饰配饰': '👕' }[cat] || '📦')

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    Object.keys(params).forEach(k => { if (params[k] === '' || params[k] === null) delete params[k] })
    if (myMode.value && currentUserId.value) {
      params.studentId = currentUserId.value
    }
    const res = await getLostFoundPage(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const toggleMyMode = () => {
  myMode.value = !myMode.value
  queryParams.page = 1
  fetchData()
}

const statusLabel = (s) => ({ 0: '寻找中/待认领', 1: '已解决', 2: '已撤销' }[s] || '未知')
const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'info' }[s] || 'info')

// === 发布 ===
const drawerVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const editId = ref(null)
const form = reactive({ type: 0, title: '', description: '', category: '', location: '', contactInfo: '', images: '' })
const uploadedImages = ref([])
const rules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  contactInfo: [{ required: true, message: '请输入联系方式', trigger: 'blur' }]
}

const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/common/upload'
const uploadHeaders = { token: localStorage.getItem('student_token') || '' }

const handleUploadSuccess = (resp) => {
  if (resp.code === 1) { uploadedImages.value.push(resp.data); form.images = uploadedImages.value.join(',') }
  else { ElMessage.error(resp.msg || '上传失败') }
}
const handleRemove = (f) => {
  const url = f.url || (f.response && f.response.data)
  if (url) { uploadedImages.value = uploadedImages.value.filter(u => u !== url); form.images = uploadedImages.value.join(',') }
}

const openPublish = () => {
  editId.value = null
  Object.assign(form, { type: 0, title: '', description: '', category: '', location: '', contactInfo: '', images: '' })
  uploadedImages.value = []
  drawerVisible.value = true
}

const openEdit = (item) => {
  editId.value = item.id
  Object.assign(form, {
    type: item.type, title: item.title, description: item.description,
    category: item.category, location: item.location || '', contactInfo: item.contactInfo || '',
    images: item.images || ''
  })
  uploadedImages.value = item.images ? item.images.split(',').filter(Boolean) : []
  drawerVisible.value = true
}

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (editId.value) {
        await editLostFound(editId.value, { ...form })
        ElMessage.success('修改成功')
      } else {
        await publishLostFound({ ...form })
        ElMessage.success('发布成功')
      }
      drawerVisible.value = false
      fetchData()
    } catch (e) { console.error(e) }
    finally { submitLoading.value = false }
  })
}

// === 详情 ===
const detailVisible = ref(false)
const detailData = ref({})
const openDetail = (row) => { detailData.value = row; detailVisible.value = true }

// === 操作 ===
const handleSolved = (id) => {
  ElMessageBox.confirm('确认标记为已解决？', '提示', { type: 'success' }).then(async () => {
    await updateLostFoundStatus({ id, status: 1 })
    ElMessage.success('已标记为已解决')
    fetchData()
  }).catch(() => {})
}
const handleCancel = (id) => {
  ElMessageBox.confirm('确定要撤销这条信息吗？', '提示', { type: 'warning' }).then(async () => {
    await updateLostFoundStatus({ id, status: 2 })
    ElMessage.success('已撤销')
    fetchData()
  }).catch(() => {})
}

const handleClaim = (row) => {
  const isZhaoling = row.type === 1
  const title = isZhaoling ? '认领确认' : '提供线索'
  const msg = isZhaoling
    ? '确认认领该物品？发布者将通过平台通知获知你的信息并主动联系你。'
    : '确认你捡到了该物品？发布者将通过平台通知获知你的信息并主动联系你。'
  ElMessageBox.confirm(msg, title, { type: 'primary' }).then(async () => {
    await claimLostFound(row.id)
    ElMessage.success('已发送，请等待发布者联系你')
  }).catch(() => {})
}

onMounted(async () => {
  try {
    const info = await request({ url: '/student/info', method: 'get' })
    currentUserId.value = info.id || null
  } catch (e) { /* ignore */ }
  fetchData()
})
</script>

<style scoped>
.filter-card { margin-bottom: 20px; }
.filter-wrapper { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
.filter-left { display: flex; align-items: center; flex-wrap: wrap; gap: 0; }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
.detail-images { display: flex; flex-wrap: wrap; }

/* 卡片网格 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
@media (max-width: 1400px) { .card-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 1000px) { .card-grid { grid-template-columns: repeat(2, 1fr); } }

.lf-card {
  background: #fff; border-radius: 12px; overflow: hidden;
  cursor: pointer; transition: all 0.3s;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.lf-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }

.card-img {
  position: relative; width: 100%; height: 160px;
  overflow: hidden; background: #f5f7fa;
}
.card-cover { width: 100%; height: 100%; }
.card-img-placeholder {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
}
.cat-icon { font-size: 48px; }

.card-type-tag {
  position: absolute; top: 8px; left: 8px;
  padding: 2px 10px; border-radius: 10px;
  font-size: 12px; color: #fff; font-weight: 500;
}
.type-lost { background: #e6a23c; }
.type-found { background: #67c23a; }
.card-status-tag {
  position: absolute; top: 8px; right: 8px;
  padding: 2px 10px; border-radius: 10px;
  font-size: 12px; color: #fff; background: #909399;
}

.card-body { padding: 12px; }
.card-seller { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #909399; margin-bottom: 8px; }
.card-name { font-size: 14px; font-weight: 500; color: #303133; margin-bottom: 10px; line-height: 1.4; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.card-location { font-size: 12px; color: #909399; max-width: 120px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-actions, .card-claim { margin-top: 10px; display: flex; gap: 8px; }
</style>
