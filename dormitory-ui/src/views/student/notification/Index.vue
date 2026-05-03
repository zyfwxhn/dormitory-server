<template>
  <div class="app-container">
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>通知中心</span>
          <el-button type="primary" text :disabled="unreadCount === 0" @click="markAllRead">全部已读</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe empty-text="暂无通知">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="类型" width="110" align="center">
          <template #default="scope">
            <el-tag :type="typeTag(scope.row.type)" size="small">{{ typeLabel(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-badge v-if="scope.row.isRead === 0" is-dot class="unread-dot" />
            <span v-else class="read-label">已读</span>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="180">
          <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.isRead === 0"
              type="primary"
              link
              size="small"
              @click="handleMarkRead(scope.row.id)"
            >标为已读</el-button>
            <span v-else class="no-action">--</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyNotifications, markAsRead } from '@/api/notification'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const fetchData = async () => {
  loading.value = true
  try {
    const list = await getMyNotifications() || []
    total.value = list.length
    const start = (page.value - 1) * pageSize.value
    tableData.value = list.slice(start, start + pageSize.value)
  } catch (e) {
    console.error('获取通知失败', e)
  } finally {
    loading.value = false
  }
}

const unreadCount = computed(() => tableData.value.filter(n => n.isRead === 0).length)

const typeLabel = (type) => ({ 1: '报修通知', 2: '二手留言', 3: '预约提醒' }[type] || '系统通知')
const typeTag = (type) => ({ 1: 'primary', 2: 'success', 3: 'warning' }[type] || 'info')

const handleMarkRead = async (id) => {
  try {
    await markAsRead(id)
    ElMessage.success('已标记为已读')
    fetchData()
  } catch (e) { console.error(e) }
}

const markAllRead = async () => {
  const unread = tableData.value.filter(n => n.isRead === 0)
  if (unread.length === 0) return
  try {
    await Promise.all(unread.map(n => markAsRead(n.id)))
    ElMessage.success('已全部标为已读')
    fetchData()
  } catch (e) { console.error(e) }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const m = String(timeStr).match(/^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})/)
  if (!m) return String(timeStr)
  const d = new Date(+m[1], +m[2] - 1, +m[3], +m[4], +m[5])
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (d.getFullYear() === now.getFullYear()) return `${m[2]}-${m[3]} ${m[4]}:${m[5]}`
  return `${m[1]}-${m[2]}-${m[3]}`
}

onMounted(() => fetchData())
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
.unread-dot { margin-top: 4px; }
.read-label { color: #c0c4cc; font-size: 13px; }
.no-action { color: #c0c4cc; }
</style>
