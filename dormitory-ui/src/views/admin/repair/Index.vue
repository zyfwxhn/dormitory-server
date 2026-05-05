<template>
  <div class="app-container">
    <el-card shadow="never" class="filter-card">
      <div class="filter-wrapper">
        <el-select v-model="queryParams.status" placeholder="报修状态" clearable @change="fetchData" style="width: 140px;">
          <el-option label="待处理" :value="0" />
          <el-option label="已接单" :value="1" />
          <el-option label="维修中" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
        <el-input v-model="queryParams.studentNo" placeholder="学号" clearable style="width: 160px; margin-left: 10px;" @keyup.enter="fetchData" />
        <el-button type="primary" icon="Search" @click="fetchData" style="margin-left: 10px;">搜索</el-button>
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
            <el-tag :type="statusType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workerId" label="维修员" width="100" align="center">
          <template #default="scope">{{ scope.row.workerId ? '#' + scope.row.workerId : '--' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 0"
              type="primary"
              link
              @click="handleDispatch(scope.row.id)"
            >智能派单</el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="工单详情" width="520px">
      <el-descriptions :column="1" border v-if="detailData.id">
        <el-descriptions-item label="故障类型">{{ detailData.repairType }}</el-descriptions-item>
        <el-descriptions-item label="故障描述">{{ detailData.description }}</el-descriptions-item>
        <el-descriptions-item label="报修地址">{{ detailData.addressSnapshot }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.studentName" label="提交人">{{ detailData.studentName }} ({{ detailData.studentNo }})</el-descriptions-item>
        <el-descriptions-item v-if="detailData.studentPhone" label="联系电话">{{ detailData.studentPhone }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detailData.status)">{{ statusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="维修员">#{{ detailData.workerId || '未分配' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detailData.createTime }}</el-descriptions-item>
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
        <el-descriptions-item v-if="detailData.evaluationContent" label="评价">{{ detailData.evaluationContent }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminRepairPage, getAdminRepairDetail, dispatchRepair } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, status: null, studentNo: null })

const fetchData = async () => {
  loading.value = true
  try {
    const p = {}; Object.keys(queryParams).forEach(k => { if (queryParams[k] !== null && queryParams[k] !== '') p[k] = queryParams[k] })
    const res = await getAdminRepairPage(p)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const statusLabel = (s) => ({ 0: '待处理', 1: '已接单', 2: '维修中', 3: '已完成', 4: '已取消' }[s] || '未知')
const statusType = (s) => ({ 0: 'info', 1: 'primary', 2: 'warning', 3: 'success', 4: 'danger' }[s] || 'info')

const handleDispatch = (id) => {
  ElMessageBox.confirm('确认对该工单执行智能派单？', '提示', { type: 'primary' }).then(async () => {
    await dispatchRepair(id)
    ElMessage.success('智能派单成功！')
    fetchData()
  }).catch(() => {})
}

const detailVisible = ref(false)
const detailData = ref({})
const openDetail = async (row) => {
  try {
    detailData.value = await getAdminRepairDetail(row.id)
  } catch (e) {
    detailData.value = row
  }
  detailVisible.value = true
}

// 定时自动刷新（管理员无 WebSocket 推送，轮询代替）
let pollTimer = null
onMounted(() => {
  fetchData()
  pollTimer = setInterval(fetchData, 10000)
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
