<template>
  <div class="app-container">
    <el-card shadow="never" class="filter-card">
      <div class="filter-wrapper">
        <el-select v-model="queryParams.status" placeholder="预约状态" clearable @change="fetchData" style="width: 140px;">
          <el-option label="预约中" :value="0" />
          <el-option label="已完成" :value="1" />
          <el-option label="已取消" :value="2" />
        </el-select>
        <el-input v-model="queryParams.studentNo" placeholder="学号" clearable style="width: 160px; margin-left: 10px;" @keyup.enter="fetchData" />
        <el-button type="primary" icon="Search" @click="fetchData" style="margin-left: 10px;">搜索</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="deviceName" label="设备名称" min-width="160" />
        <el-table-column prop="deviceBuildingNo" label="所在楼栋" width="100" />
        <el-table-column prop="studentName" label="预约学生" width="120" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="reservationDate" label="预约日期" width="120" />
        <el-table-column label="时段" width="140">
          <template #default="scope">{{ scope.row.startTime }} - {{ scope.row.endTime }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="['', 'success', 'info'][scope.row.status] || 'info'">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAdminReservationPage } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, status: null, studentNo: null })

const fetchData = async () => {
  loading.value = true
  try {
    const p = {}; Object.keys(queryParams).forEach(k => { if (queryParams[k] !== null && queryParams[k] !== '') p[k] = queryParams[k] })
    const res = await getAdminReservationPage(p)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const statusLabel = (s) => ({ 0: '预约中', 1: '已完成', 2: '已取消' }[s] || '未知')

onMounted(() => fetchData())
</script>

<style scoped>
.filter-card { margin-bottom: 20px; }
.filter-wrapper { display: flex; align-items: center; }
.table-card { }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
