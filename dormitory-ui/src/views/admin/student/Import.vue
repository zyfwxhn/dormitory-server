<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header><span style="font-weight: bold;">批量导入学生</span></template>

      <el-alert title="导入说明" type="info" :closable="false" style="margin-bottom: 20px;">
        <template #default>
          <p style="margin: 4px 0;">1. 下载 Excel 模板，按格式填写学生信息</p>
          <p style="margin: 4px 0;">2. Excel 表头必须包含：学号、姓名、性别、手机号、楼栋号、宿舍号</p>
          <p style="margin: 4px 0;">3. 导入后默认密码为 123456</p>
          <p style="margin: 4px 0;">4. 学号不能与已有学生重复</p>
        </template>
      </el-alert>

      <el-upload
        ref="uploadRef"
        drag
        :action="uploadUrl"
        :headers="uploadHeaders"
        :on-success="handleSuccess"
        :on-error="handleError"
        :before-upload="beforeUpload"
        :limit="1"
        accept=".xlsx,.xls"
      >
        <el-icon :size="60" class="upload-icon"><UploadFilled /></el-icon>
        <div class="el-upload__text">将 Excel 文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">仅支持 .xlsx / .xls 格式文件</div>
        </template>
      </el-upload>

      <div v-if="importResult" style="margin-top: 20px;">
        <el-alert :title="importResult" type="success" :closable="false" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/admin/student/import'
const uploadHeaders = { token: localStorage.getItem('admin_token') || '' }
const importResult = ref('')

const beforeUpload = (file) => {
  const isExcel = /\.(xlsx|xls)$/i.test(file.name)
  if (!isExcel) {
    ElMessage.error('只能上传 .xlsx 或 .xls 格式的文件！')
    return false
  }
  return true
}

const handleSuccess = (response) => {
  if (response.code === 1) {
    ElMessage.success(response.data || '批量导入成功！')
    importResult.value = response.data || '导入成功'
  } else {
    ElMessage.error(response.msg || '导入失败')
  }
}

const handleError = () => {
  ElMessage.error('上传失败，请重试')
}
</script>

<style scoped>
.app-container { }
.upload-icon { color: #409eff; }
</style>
