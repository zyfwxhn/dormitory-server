<template>
  <div class="app-container">
    <el-card shadow="never" class="filter-card">
      <div class="filter-wrapper">
        <div class="filter-left">
          <el-input v-model="queryParams.deviceName" placeholder="搜索设备名" clearable style="width: 200px;" @keyup.enter="fetchData" />
          <el-select v-model="queryParams.buildingNo" placeholder="楼栋" clearable @change="fetchData" style="width: 140px; margin-left: 10px;">
            <el-option v-for="b in buildings" :key="b" :label="b" :value="b" />
          </el-select>
          <el-select v-model="queryParams.status" placeholder="状态" clearable @change="fetchData" style="width: 120px; margin-left: 10px;">
            <el-option label="正常" :value="1" />
            <el-option label="故障" :value="0" />
          </el-select>
          <el-button type="primary" icon="Search" @click="fetchData" style="margin-left: 10px;">搜索</el-button>
        </div>
        <el-button type="primary" icon="Plus" @click="openDialog()">新增设备</el-button>
        <el-upload
          :action="importUrl"
          :headers="uploadHeaders"
          accept=".xlsx,.xls"
          :show-file-list="false"
          :on-success="handleImportSuccess"
          :on-error="handleImportError"
          style="display: inline-block; margin-left: 10px;"
        >
          <el-button type="success" icon="Upload">批量导入</el-button>
        </el-upload>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="buildingNo" label="楼栋" width="100" />
        <el-table-column prop="deviceName" label="设备名称" min-width="180" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-switch :model-value="scope.row.status === 1" @change="toggleStatus(scope.row)" active-text="正常" inactive-text="故障" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="录入时间" width="180" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="openDialog(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 10px;" v-if="selectedIds.length > 0">
        <el-button type="danger" @click="handleBatchDelete">批量删除（{{ selectedIds.length }}）</el-button>
      </div>

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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑设备' : '新增设备'" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="楼栋号" prop="buildingNo">
          <el-input v-model="form.buildingNo" placeholder="如：8号楼" />
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="form.deviceName" placeholder="如：1楼左侧1号洗衣机" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">故障/停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDevicePage, saveDevice, updateDevice, deleteDevices, toggleDeviceStatus } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const queryParams = reactive({ page: 1, pageSize: 10, buildingNo: null, deviceName: null, status: null })
const buildings = ['1号楼', '2号楼', '3号楼', '4号楼', '5号楼', '6号楼', '7号楼', '8号楼', '9号楼', '10号楼']

// 批量导入
const importUrl = import.meta.env.VITE_APP_BASE_API + '/admin/device/import'
const uploadHeaders = { token: localStorage.getItem('admin_token') || '' }
const handleImportSuccess = (res) => {
  if (res.code === 1) { ElMessage.success(res.msg || '导入成功'); fetchData() }
  else { ElMessage.error(res.msg || '导入失败') }
}
const handleImportError = () => { ElMessage.error('导入失败，请检查文件格式') }

const fetchData = async () => {
  loading.value = true
  try {
    const p = {}; Object.keys(queryParams).forEach(k => { if (queryParams[k] !== null && queryParams[k] !== '') p[k] = queryParams[k] })
    const res = await getDevicePage(p)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleSelectionChange = (rows) => { selectedIds.value = rows.map(r => r.id) }

// === 新增/编辑 ===
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, buildingNo: '', deviceName: '', status: 1 })
const rules = {
  buildingNo: [{ required: true, message: '请输入楼栋号', trigger: 'blur' }],
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }]
}

const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    Object.assign(form, { id: row.id, buildingNo: row.buildingNo, deviceName: row.deviceName, status: row.status })
  } else {
    isEdit.value = false
    Object.assign(form, { id: null, buildingNo: '', deviceName: '', status: 1 })
  }
  dialogVisible.value = true
}

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) { await updateDevice({ ...form }) }
      else { await saveDevice({ ...form }) }
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      fetchData()
    } catch (e) { console.error(e) } finally { submitLoading.value = false }
  })
}

// === 启停 ===
const toggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  toggleDeviceStatus(newStatus, row.id).then(() => {
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已停用')
  }).catch(() => {})
}

// === 删除 ===
const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除该设备？', '提示', { type: 'warning' }).then(async () => {
    await deleteDevices([id])
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) return
  ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个设备？`, '批量删除', { type: 'warning' }).then(async () => {
    await deleteDevices(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
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
</style>
