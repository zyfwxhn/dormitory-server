<template>
  <div class="app-container">
    <!-- 顶部操作区 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-wrapper">
        <el-select v-model="queryParams.status" placeholder="报修状态" clearable @change="fetchData" style="width: 200px;">
          <el-option label="待处理" :value="0" />
          <el-option label="已接单" :value="1" />
          <el-option label="维修中" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
        <el-button type="primary" icon="Plus" @click="openDrawer">发起报修</el-button>
      </div>
    </el-card>

    <!-- 数据表格区 -->
    <el-card shadow="never" class="table-card">
      <el-skeleton v-if="loading && tableData.length === 0" animated :rows="8" :throttle="500" />
      <el-table v-else :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="repairType" label="故障类型" width="150" />
        <el-table-column prop="description" label="详细描述" show-overflow-tooltip />
        <el-table-column prop="addressSnapshot" label="报修地址" width="150" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 0"
              type="danger"
              link
              @click="handleCancel(scope.row.id)"
            >取消</el-button>
            <el-button
              v-if="scope.row.status === 3 && !scope.row.evaluationScore"
              type="success"
              link
              @click="openEvaluate(scope.row)"
            >评价</el-button>
            <el-button type="primary" link @click="openDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
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

    <!-- 发起报修抽屉 -->
    <el-drawer v-model="drawerVisible" title="发起宿舍报修" size="400px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" label-position="top">
        <el-form-item label="故障类型" prop="repairType">
          <el-select v-model="form.repairType" placeholder="请选择故障类型" style="width: 100%">
            <el-option label="水管漏水" value="水管漏水" />
            <el-option label="水管堵塞" value="水管堵塞" />
            <el-option label="灯泡损坏" value="灯泡损坏" />
            <el-option label="电路故障" value="电路故障" />
            <el-option label="门窗损坏" value="门窗损坏" />
            <el-option label="门锁故障" value="门锁故障" />
            <el-option label="墙面脱落" value="墙面脱落" />
            <el-option label="空调故障" value="空调故障" />
            <el-option label="热水器故障" value="热水器故障" />
            <el-option label="网络故障" value="网络故障" />
            <el-option label="下水道堵塞" value="下水道堵塞" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="故障描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述故障情况，以便维修师傅准确定位问题..."
          />
        </el-form-item>

        <el-form-item label="现场照片 (最多3张)">
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
        <div style="flex: auto">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">提交报修</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 评价弹窗 -->
    <el-dialog v-model="evaluateDialogVisible" title="评价报修服务" width="420px">
      <el-form ref="evaluateFormRef" :model="evaluateForm" :rules="evaluateRules" label-width="80px">
        <el-form-item label="评分" prop="evaluationScore">
          <el-rate v-model="evaluateForm.evaluationScore" :max="5" show-score />
        </el-form-item>
        <el-form-item label="评价内容" prop="evaluationContent">
          <el-input
            v-model="evaluateForm.evaluationContent"
            type="textarea"
            :rows="3"
            placeholder="请输入您的评价..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evaluateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="evaluateLoading" @click="submitEvaluate">提交评价</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="报修详情" width="520px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="故障类型">{{ detailData.repairType }}</el-descriptions-item>
        <el-descriptions-item label="故障描述">{{ detailData.description }}</el-descriptions-item>
        <el-descriptions-item label="报修地址">{{ detailData.addressSnapshot }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusType(detailData.status)">{{ getStatusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.workerId" label="维修员ID">#{{ detailData.workerId }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.images" label="现场照片" :span="2">
          <div class="detail-images">
            <el-image v-for="(url, idx) in detailData.images.split(',')" :key="idx" :src="url" style="width:100px;height:100px;margin-right:8px;" :preview-src-list="detailData.images.split(',')" fit="cover" />
          </div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.finishImages" label="完工照片" :span="2">
          <div class="detail-images">
            <el-image v-for="(url, idx) in detailData.finishImages.split(',')" :key="idx" :src="url" style="width:100px;height:100px;margin-right:8px;" :preview-src-list="detailData.finishImages.split(',')" fit="cover" />
          </div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.evaluationScore" label="评分">{{ detailData.evaluationScore }} 分</el-descriptions-item>
        <el-descriptions-item v-if="detailData.evaluationContent" label="评价内容">{{ detailData.evaluationContent }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getMyRepairPage, submitRepair, cancelRepair, evaluateRepair, getRepairDetail } from '@/api/repair'

// === 核心数据与状态 ===
const loading = ref(false)
const submitLoading = ref(false)
const drawerVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  status: null
})

// === 获取数据 ===
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyRepairPage(queryParams)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取列表失败', error)
  } finally {
    loading.value = false
  }
}

// === 状态字典转换 ===
const getStatusLabel = (status) => {
  const map = { 0: '待处理', 1: '已接单', 2: '维修中', 3: '已完成', 4: '已取消' }
  return map[status] || '未知'
}
const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'primary', 2: 'warning', 3: 'success', 4: 'danger' }
  return map[status] || 'info'
}

// === 表单与上传逻辑 ===
const formRef = ref(null)
const form = reactive({
  repairType: '',
  description: '',
  images: ''
})
const uploadedImages = ref([])

const rules = {
  repairType: [{ required: true, message: '请选择故障类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入故障描述', trigger: 'blur' }]
}

const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/common/upload'
const uploadHeaders = { token: localStorage.getItem('student_token') || '' }

const handleUploadSuccess = (response) => {
  if (response.code === 1) {
    uploadedImages.value.push(response.data)
    form.images = uploadedImages.value.join(',')
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}
const handleRemove = (uploadFile) => {
  const url = uploadFile.url || (uploadFile.response && uploadFile.response.data)
  if (url) {
    uploadedImages.value = uploadedImages.value.filter(u => u !== url)
    form.images = uploadedImages.value.join(',')
  }
}

// === 发起报修 ===
const openDrawer = () => {
  form.repairType = ''
  form.description = ''
  form.images = ''
  uploadedImages.value = []
  drawerVisible.value = true
}

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await submitRepair({
        repairType: form.repairType,
        description: form.description,
        images: form.images
      })
      ElMessage.success('报修提交成功')
      drawerVisible.value = false
      fetchData()
    } catch (error) {
      console.error(error)
    } finally {
      submitLoading.value = false
    }
  })
}

const handleCancel = (id) => {
  ElMessageBox.confirm('确定要取消这条报修申请吗?', '提示', { type: 'warning' }).then(async () => {
    await cancelRepair(id)
    ElMessage.success('取消成功')
    fetchData()
  }).catch(() => {})
}

// === 评价 ===
const evaluateLoading = ref(false)
const evaluateDialogVisible = ref(false)
const evaluateFormRef = ref(null)
const currentEvaluateId = ref(null)
const evaluateForm = reactive({
  evaluationScore: 0,
  evaluationContent: ''
})
const evaluateRules = {
  evaluationScore: [{ required: true, message: '请打分', trigger: 'change' }]
}

const openEvaluate = (row) => {
  currentEvaluateId.value = row.id
  evaluateForm.evaluationScore = 0
  evaluateForm.evaluationContent = ''
  evaluateDialogVisible.value = true
}

const submitEvaluate = () => {
  evaluateFormRef.value.validate(async (valid) => {
    if (!valid) return
    evaluateLoading.value = true
    try {
      await evaluateRepair({
        id: currentEvaluateId.value,
        evaluationScore: evaluateForm.evaluationScore,
        evaluationContent: evaluateForm.evaluationContent
      })
      ElMessage.success('评价成功')
      evaluateDialogVisible.value = false
      fetchData()
    } catch (error) {
      console.error(error)
    } finally {
      evaluateLoading.value = false
    }
  })
}

// === 详情 ===
const detailDialogVisible = ref(false)
const detailData = ref({})

const openDetail = async (row) => {
  try {
    detailData.value = await getRepairDetail(row.id)
  } catch (e) {
    detailData.value = row
  }
  detailDialogVisible.value = true
}

// WebSocket 推送后自动刷新列表
const handleWsMessage = (e) => {
  try {
    const msg = JSON.parse(e.detail)
    if (msg.type === 'repair_status_changed') {
      fetchData()
    }
  } catch {}
}

// 挂载时拉取数据
onMounted(() => {
  fetchData()
  window.addEventListener('ws-message', handleWsMessage)
})
onUnmounted(() => {
  window.removeEventListener('ws-message', handleWsMessage)
})
</script>

<style scoped>
.filter-card {
  margin-bottom: 20px;
}
.filter-wrapper {
  display: flex;
  justify-content: space-between;
}
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.detail-images { display: flex; flex-wrap: wrap; }
</style>
