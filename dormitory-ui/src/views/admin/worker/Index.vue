<template>
  <div class="app-container">
    <el-card shadow="never" class="filter-card">
      <div class="filter-wrapper">
        <div class="filter-left">
          <el-input v-model="queryParams.name" placeholder="搜索姓名" clearable style="width: 180px;" @keyup.enter="fetchData" />
          <el-input v-model="queryParams.phone" placeholder="搜索手机号" clearable style="width: 180px; margin-left: 10px;" @keyup.enter="fetchData" />
          <el-button type="primary" icon="Search" @click="fetchData" style="margin-left: 10px;">搜索</el-button>
        </div>
        <el-button type="primary" icon="Plus" @click="openDialog()">新增维修员</el-button>
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
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="工号/账号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="skills" label="擅长领域" min-width="140" />
        <el-table-column label="在岗状态" width="100" align="center">
          <template #default="scope">
            <el-switch :model-value="scope.row.isAvailable === 1" @change="toggleStatus(scope.row)" active-text="在岗" inactive-text="离岗" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="openDialog(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleToggleStatus(scope.row)">{{ scope.row.isAvailable === 1 ? '停用' : '启用' }}</el-button>
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

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑维修员' : '新增维修员'" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="工号/账号" prop="username">
          <el-input v-model="form.username" placeholder="登录账号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" :prop="isEdit ? null : 'password'">
          <el-input v-model="form.password" placeholder="登录密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="擅长领域">
          <el-select v-model="form.skillsList" multiple filterable allow-create placeholder="选择或输入工种（可多选）" style="width: 100%;">
            <el-option v-for="s in skillOptions" :key="s" :label="s" :value="s" />
          </el-select>
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
import { getWorkerPage, saveWorker, updateWorker, toggleWorkerStatus } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, name: null, phone: null })

// 批量导入
const importUrl = import.meta.env.VITE_APP_BASE_API + '/admin/worker/import'
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
    const res = await getWorkerPage(p)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}

// === 新增/编辑 ===
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const skillOptions = ['电工', '木工', '水暖工', '油漆工', '瓦工', '门窗维修', '管道疏通', '空调维修', '网络布线', '家具维修', '锁具维修', '墙面修补']

const form = reactive({ id: null, username: '', password: '', name: '', phone: '', skillsList: [] })
const rules = {
  username: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    const skillsArr = row.skills ? row.skills.split(',') : []
    Object.assign(form, { id: row.id, username: row.username, password: '', name: row.name, phone: row.phone || '', skillsList: skillsArr })
  } else {
    isEdit.value = false
    Object.assign(form, { id: null, username: '', password: '', name: '', phone: '', skillsList: [] })
  }
  dialogVisible.value = true
}

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = { id: form.id, username: form.username, password: form.password, name: form.name, phone: form.phone, skills: form.skillsList.join(',') || null }
      if (isEdit.value) { await updateWorker(payload) }
      else { await saveWorker(payload) }
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      fetchData()
    } catch (e) { console.error(e) } finally { submitLoading.value = false }
  })
}

const toggleStatus = (row) => {
  const newStatus = row.isAvailable === 1 ? 0 : 1
  toggleWorkerStatus(row.id, newStatus).then(() => {
    row.isAvailable = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已停用')
  }).catch(() => {})
}

const handleToggleStatus = (row) => {
  const newStatus = row.isAvailable === 1 ? 0 : 1
  ElMessageBox.confirm(`确定${newStatus === 0 ? '停用' : '启用'}该维修员？`, '提示', { type: 'warning' }).then(async () => {
    await toggleWorkerStatus(row.id, newStatus)
    row.isAvailable = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已停用')
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
