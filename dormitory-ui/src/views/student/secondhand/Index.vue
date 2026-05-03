<template>
  <div class="app-container">
    <!-- 筛选区 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-wrapper">
        <div class="filter-left">
          <el-select v-model="queryParams.category" placeholder="商品分类" clearable @change="fetchData" style="width: 130px;">
            <el-option label="电子数码" value="电子数码" />
            <el-option label="生活用品" value="生活用品" />
            <el-option label="代步工具" value="代步工具" />
            <el-option label="书籍资料" value="书籍资料" />
            <el-option label="服饰鞋包" value="服饰鞋包" />
            <el-option label="其他" value="其他" />
          </el-select>
          <el-input v-model="queryParams.name" placeholder="搜索商品" clearable @keyup.enter="fetchData" style="width: 180px; margin-left: 10px;">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-input-number v-model="queryParams.minPrice" placeholder="最低价" :min="0" :precision="2" controls-position="right" style="width: 130px; margin-left: 10px;" />
          <span style="margin: 0 8px; color: #999;">-</span>
          <el-input-number v-model="queryParams.maxPrice" placeholder="最高价" :min="0" :precision="2" controls-position="right" style="width: 130px;" />
          <el-select v-model="queryParams.sortMode" placeholder="排序" clearable @change="fetchData" style="width: 120px; margin-left: 10px;">
            <el-option label="最新发布" :value="0" />
            <el-option label="价格从低到高" :value="1" />
            <el-option label="价格从高到低" :value="2" />
          </el-select>
          <el-button type="primary" icon="Search" @click="fetchData" style="margin-left: 10px;">搜索</el-button>
        </div>
        <el-button type="primary" icon="Plus" @click="openPublish">发布商品</el-button>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="商品名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="100" align="center" />
        <el-table-column label="价格" width="110" align="center">
          <template #default="scope">
            <span class="price-tag">¥{{ scope.row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="conditionLevel" label="成色" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="itemStatusType(scope.row.status)">{{ itemStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="openDetail(scope.row)">详情</el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="success"
              link
              @click="handleMarkSold(scope.row.id)"
            >标记售出</el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="danger"
              link
              @click="handleRemoveItem(scope.row.id)"
            >下架</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 发布抽屉 -->
    <el-drawer v-model="drawerVisible" title="发布商品" size="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" label-position="top">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="如：高等数学教材" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="商品分类" prop="category">
          <el-select v-model="form.category" placeholder="选择分类" style="width: 100%;">
            <el-option label="电子数码" value="电子数码" />
            <el-option label="生活用品" value="生活用品" />
            <el-option label="代步工具" value="代步工具" />
            <el-option label="书籍资料" value="书籍资料" />
            <el-option label="服饰鞋包" value="服饰鞋包" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" controls-position="right" style="width: 100%;" placeholder="0.00" />
        </el-form-item>
        <el-form-item label="成色" prop="conditionLevel">
          <el-select v-model="form.conditionLevel" placeholder="选择成色" style="width: 100%;">
            <el-option label="全新" value="全新" />
            <el-option label="9成新" value="9成新" />
            <el-option label="8成新" value="8成新" />
            <el-option label="7成新" value="7成新" />
            <el-option label="有轻微伊拉克战损" value="有轻微伊拉克战损" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="描述商品具体情况、购买时间等信息..." />
        </el-form-item>
        <el-form-item label="商品图片（至少1张，最多3张）">
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

    <!-- 详情弹窗（含留言） -->
    <el-dialog v-model="detailVisible" title="商品详情" width="620px">
      <el-descriptions :column="2" border v-if="detailData.id">
        <el-descriptions-item label="商品名称" :span="2">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ detailData.category }}</el-descriptions-item>
        <el-descriptions-item label="价格"><span class="price-tag">¥{{ detailData.price }}</span></el-descriptions-item>
        <el-descriptions-item label="成色">{{ detailData.conditionLevel }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="itemStatusType(detailData.status)">{{ itemStatusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="卖家">{{ detailData.studentName || '--' }} ({{ detailData.studentNo || '--' }})</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detailData.description }}</el-descriptions-item>
        <el-descriptions-item label="发布时间" :span="2">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.images" label="商品图片" :span="2">
          <div class="detail-images">
            <el-image v-for="(url, idx) in detailData.images.split(',')" :key="idx" :src="url" style="width:120px;height:120px;margin-right:8px;" :preview-src-list="detailData.images.split(',')" fit="cover" />
          </div>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 留言区 -->
      <div class="message-section" v-if="detailData.id">
        <div class="message-header"><span>留言区</span><span class="msg-count">共 {{ messages.length }} 条</span></div>
        <div class="message-list" v-if="messages.length > 0">
          <div v-for="msg in messages" :key="msg.id" class="message-item">
            <div class="msg-meta">
              <span class="msg-from">
                {{ msg.fromStudentName || '用户' }}
                <el-tag v-if="msg.fromSeller" type="warning" size="small" effect="dark" style="margin-left: 4px;">卖家</el-tag>
              </span>
              <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
            </div>
            <div class="msg-body">{{ msg.content }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无留言" :image-size="40" />
        <div class="message-input" v-if="detailData.status === 0">
          <el-input v-model="newMessage" placeholder="给卖家留言..." maxlength="512" show-word-limit @keyup.enter="sendMessage">
            <template #append>
              <el-button :loading="msgSending" @click="sendMessage">发送</el-button>
            </template>
          </el-input>
        </div>
      </div>

      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getSecondhandPage, publishSecondhand, updateSecondhandStatus, getItemMessages, sendItemMessage } from '@/api/secondhand'
import { formatTime } from '@/utils/date'

// === 列表 ===
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({
  page: 1, pageSize: 10, category: null, name: null,
  minPrice: null, maxPrice: null, sortMode: null
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = {}
    Object.keys(queryParams).forEach(k => {
      if (queryParams[k] !== null && queryParams[k] !== '' && queryParams[k] !== undefined) params[k] = queryParams[k]
    })
    const res = await getSecondhandPage(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const itemStatusLabel = (s) => ({ 0: '在售', 1: '已售出', 2: '已下架' }[s] || '未知')
const itemStatusType = (s) => ({ 0: 'success', 1: 'info', 2: 'danger' }[s] || 'info')

// === 发布 ===
const drawerVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const form = reactive({ name: '', description: '', category: '', price: null, conditionLevel: '', images: '' })
const uploadedImages = ref([])
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  conditionLevel: [{ required: true, message: '请选择成色', trigger: 'change' }],
  images: [{ required: true, message: '请至少上传一张图片', trigger: 'change' }]
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
  Object.assign(form, { name: '', description: '', category: '', price: null, conditionLevel: '', images: '' })
  uploadedImages.value = []
  drawerVisible.value = true
}

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    if (uploadedImages.value.length === 0) {
      ElMessage.warning('请至少上传一张商品图片')
      return
    }
    submitLoading.value = true
    try {
      await publishSecondhand({ ...form })
      ElMessage.success('发布成功')
      drawerVisible.value = false
      fetchData()
    } catch (e) { console.error(e) }
    finally { submitLoading.value = false }
  })
}

// === 详情 + 留言 ===
const detailVisible = ref(false)
const detailData = ref({})
const messages = ref([])
const newMessage = ref('')
const msgSending = ref(false)

const openDetail = async (row) => {
  detailData.value = row
  detailVisible.value = true
  newMessage.value = ''
  try { messages.value = await getItemMessages(row.id) || [] }
  catch (e) { messages.value = [] }
}

const sendMessage = async () => {
  if (!newMessage.value.trim()) return
  msgSending.value = true
  try {
    await sendItemMessage({
      itemId: detailData.value.id,
      toStudentId: detailData.value.studentId,
      content: newMessage.value
    })
    ElMessage.success('留言发送成功')
    newMessage.value = ''
    messages.value = await getItemMessages(detailData.value.id) || []
  } catch (e) { console.error(e) }
  finally { msgSending.value = false }
}

// === 操作 ===
const handleMarkSold = (id) => {
  ElMessageBox.confirm('确认标记为已售出？', '提示', { type: 'success' }).then(async () => {
    await updateSecondhandStatus({ id, status: 1 })
    ElMessage.success('已标记为已售出')
    fetchData()
  }).catch(() => {})
}
const handleRemoveItem = (id) => {
  ElMessageBox.confirm('确定要下架该商品吗？', '提示', { type: 'warning' }).then(async () => {
    await updateSecondhandStatus({ id, status: 2 })
    ElMessage.success('已下架')
    fetchData()
  }).catch(() => {})
}

onMounted(() => fetchData())
</script>

<style scoped>
.filter-card { margin-bottom: 20px; }
.filter-wrapper { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
.filter-left { display: flex; align-items: center; flex-wrap: wrap; }
.table-card { }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
.price-tag { color: #f56c6c; font-weight: 600; }
.detail-images { display: flex; flex-wrap: wrap; }
.message-section { margin-top: 20px; border-top: 1px solid #ebeef5; padding-top: 16px; }
.message-header { display: flex; justify-content: space-between; font-weight: bold; margin-bottom: 12px; }
.msg-count { font-weight: normal; color: #909399; font-size: 13px; }
.message-list { max-height: 240px; overflow-y: auto; margin-bottom: 12px; }
.message-item { padding: 10px 0; border-bottom: 1px solid #f2f2f2; }
.message-item:last-child { border-bottom: none; }
.msg-meta { display: flex; justify-content: space-between; font-size: 12px; color: #909399; margin-bottom: 4px; }
.msg-from { font-weight: 600; color: #606266; }
.msg-body { font-size: 14px; color: #303133; line-height: 1.6; }
</style>
