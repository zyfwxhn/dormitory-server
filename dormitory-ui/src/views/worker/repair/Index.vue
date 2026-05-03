<template>
  <div class="app-container">
    <el-card shadow="never" class="filter-card">
      <div class="filter-wrapper">
        <el-radio-group v-model="viewMode" @change="fetchData" size="default">
          <el-radio-button :value="0">待接单工单</el-radio-button>
          <el-radio-button :value="1">我的工单</el-radio-button>
        </el-radio-group>
        <el-select v-model="queryParams.status" placeholder="筛选状态" clearable @change="fetchData" style="width: 140px; margin-left: 16px;" v-if="viewMode === 1">
          <el-option label="已接单" :value="1" />
          <el-option label="维修中" :value="2" />
          <el-option label="已完成" :value="3" />
        </el-select>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="repairType" label="故障类型" width="120" />
        <el-table-column prop="description" label="故障描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="addressSnapshot" label="报修地址" width="140" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="repairStatusType(scope.row.status)">{{ repairStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="scope">
            <!-- 待接单：接单按钮 -->
            <el-button v-if="scope.row.status === 0" type="primary" link @click="handleAccept(scope.row.id)">接单</el-button>

            <!-- 已接单：开始维修 -->
            <el-button v-if="scope.row.status === 1" type="warning" link @click="handleStartRepair(scope.row.id)">开始维修</el-button>

            <!-- 维修中：完工 -->
            <el-button v-if="scope.row.status === 2" type="success" link @click="openFinish(scope.row.id)">完工</el-button>

            <el-button type="info" link @click="openDetail(scope.row)">详情</el-button>
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

    <!-- 完工弹窗（上传完工照片） -->
    <el-dialog v-model="finishVisible" title="提交完工信息" width="450px">
      <el-form label-width="100px">
        <el-form-item label="完工照片（最多3张）">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-success="handleFinishUploadSuccess"
            :on-remove="handleFinishRemove"
            :limit="3"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finishVisible = false">取消</el-button>
        <el-button type="primary" :loading="finishLoading" @click="submitFinish">提交完工</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="工单详情" width="520px">
      <el-descriptions :column="1" border v-if="detailData.id">
        <el-descriptions-item label="故障类型">{{ detailData.repairType }}</el-descriptions-item>
        <el-descriptions-item label="故障描述">{{ detailData.description }}</el-descriptions-item>
        <el-descriptions-item label="报修地址">{{ detailData.addressSnapshot }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.studentName" label="提交人">{{ detailData.studentName }} ({{ detailData.studentNo }})</el-descriptions-item>
        <el-descriptions-item v-if="detailData.studentPhone" label="联系电话">{{ detailData.studentPhone }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="repairStatusType(detailData.status)">{{ repairStatusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.images" label="现场照片">
          <div class="detail-images">
            <el-image v-for="(url, idx) in detailData.images.split(',')" :key="idx" :src="url" style="width:100px;height:100px;margin-right:8px;" :preview-src-list="detailData.images.split(',')" fit="cover" />
          </div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.finishImages" label="完工照片">
          <div class="detail-images">
            <el-image v-for="(url, idx) in detailData.finishImages.split(',')" :key="idx" :src="url" style="width:100px;height:100px;margin-right:8px;" :preview-src-list="detailData.finishImages.split(',')" fit="cover" />
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getWorkerRepairPage, updateRepairStatus } from '@/api/worker'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const viewMode = ref(0) // 0=待接单, 1=我的工单
const queryParams = reactive({ page: 1, pageSize: 10, status: null })

const fetchData = async () => {
  loading.value = true
  try {
    const params = { page: queryParams.page, pageSize: queryParams.pageSize }
    if (viewMode.value === 0) {
      params.status = 0 // 待接单：只查status=0
    } else {
      if (queryParams.status !== null && queryParams.status !== '') {
        params.status = queryParams.status
      }
      // status=null means all my orders (1,2,3)
    }
    const res = await getWorkerRepairPage(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const repairStatusLabel = (s) => ({ 0: '待处理', 1: '已接单', 2: '维修中', 3: '已完成', 4: '已取消' }[s] || '未知')
const repairStatusType = (s) => ({ 0: 'info', 1: 'primary', 2: 'warning', 3: 'success', 4: 'danger' }[s] || 'info')

// === 操作 ===
const handleAccept = (id) => {
  ElMessageBox.confirm('确认接单？', '提示', { type: 'primary' }).then(async () => {
    await updateRepairStatus({ id, status: 1 })
    ElMessage.success('已接单')
    fetchData()
  }).catch(() => {})
}
const handleStartRepair = (id) => {
  ElMessageBox.confirm('确认开始维修？', '提示', { type: 'warning' }).then(async () => {
    await updateRepairStatus({ id, status: 2 })
    ElMessage.success('已开始维修')
    fetchData()
  }).catch(() => {})
}

// === 完工 ===
const finishVisible = ref(false)
const finishLoading = ref(false)
const currentFinishId = ref(null)
const finishImages = ref([])
const finishImagesStr = ref('')

const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/common/upload'
const uploadHeaders = { token: localStorage.getItem('worker_token') || '' }

const handleFinishUploadSuccess = (resp) => {
  if (resp.code === 1) { finishImages.value.push(resp.data); finishImagesStr.value = finishImages.value.join(',') }
  else { ElMessage.error(resp.msg || '上传失败') }
}
const handleFinishRemove = (f) => {
  const url = f.url || (f.response && f.response.data)
  if (url) { finishImages.value = finishImages.value.filter(u => u !== url); finishImagesStr.value = finishImages.value.join(',') }
}

const openFinish = (id) => {
  currentFinishId.value = id
  finishImages.value = []
  finishImagesStr.value = ''
  finishVisible.value = true
}

const submitFinish = async () => {
  finishLoading.value = true
  try {
    await updateRepairStatus({ id: currentFinishId.value, status: 3, finishImages: finishImagesStr.value || undefined })
    ElMessage.success('已完工')
    finishVisible.value = false
    fetchData()
  } catch (e) { console.error(e) }
  finally { finishLoading.value = false }
}

// === 详情 ===
const detailVisible = ref(false)
const detailData = ref({})
const openDetail = (row) => { detailData.value = row; detailVisible.value = true }

// 定时自动刷新待接单列表
let pollTimer = null
onMounted(() => {
  fetchData()
  pollTimer = setInterval(fetchData, 8000)
})
onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.filter-card { margin-bottom: 20px; }
.filter-wrapper { display: flex; align-items: center; }
.table-card { }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
.detail-images { display: flex; flex-wrap: wrap; }
</style>
