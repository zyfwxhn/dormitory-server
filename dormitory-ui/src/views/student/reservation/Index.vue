<template>
  <div class="app-container">
    <!-- 所在楼栋设备概览 -->
    <el-card shadow="never" class="device-card">
      <template #header>
        <div class="card-header">
          <span><el-icon><Monitor /></el-icon> {{ buildingName }} 可用设备</span>
          <el-tag type="success" effect="dark" round>共 {{ devices.length }} 台</el-tag>
        </div>
      </template>

      <div v-if="devices.length === 0" class="empty-hint">
        <el-empty description="你所在的楼栋暂无可用设备" :image-size="80" />
      </div>

      <div v-else v-for="floor in floors" :key="floor" class="floor-section">
        <div class="floor-title">{{ floor }}楼</div>
        <el-row :gutter="16">
          <el-col :span="8" v-for="d in getDevicesByFloor(floor)" :key="d.id">
            <div class="device-item" @click="openBooking(d)">
              <div class="device-icon">
                <el-icon :size="36" :color="d.deviceName.includes('洗衣机') ? '#409eff' : d.deviceName.includes('吹风机') ? '#e6a23c' : '#67c23a'">
                  <component :is="deviceIcon(d.deviceName)" />
                </el-icon>
              </div>
              <div class="device-name">{{ d.deviceName }}</div>
              <el-tag :type="d.status === 1 ? 'success' : 'danger'" size="small" effect="dark">
                {{ d.status === 1 ? '可用' : '故障' }}
              </el-tag>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 我的预约记录（折叠） -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span><el-icon><Calendar /></el-icon> 我的预约记录</span>
        </div>
      </template>
      <el-table :data="tableData" v-loading="tableLoading" border stripe empty-text="暂无预约记录">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="设备" min-width="160">
          <template #default="scope">{{ scope.row.deviceName || '未知设备' }}</template>
        </el-table-column>
        <el-table-column prop="reservationDate" label="预约日期" width="120" />
        <el-table-column label="时段" width="180">
          <template #default="scope">{{ scope.row.startTime }} - {{ scope.row.endTime }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="resStatusType(scope.row.status)">{{ resStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="scope">
            <el-button v-if="scope.row.status === 0" type="danger" link @click="handleCancel(scope.row.id)">取消</el-button>
            <span v-else class="no-action">--</span>
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
          @size-change="fetchMyReservations"
          @current-change="fetchMyReservations"
        />
      </div>
    </el-card>

    <!-- 预约弹窗 -->
    <el-dialog v-model="bookingVisible" :title="'预约 - ' + selectedDevice?.deviceName" width="480px">
      <div class="booking-content" v-if="selectedDevice">
        <div class="device-info-bar">
          <span>{{ selectedDevice.buildingNo }}号楼 · {{ selectedDevice.deviceName }}</span>
        </div>
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择预约日期"
          :disabled-date="disabledDate"
          style="width: 100%; margin-top: 16px;"
        />
        <el-button type="primary" :loading="slotsLoading" @click="querySlots" style="width: 100%; margin-top: 12px;">
          查询可用时段
        </el-button>

        <div v-if="availableSlots.length > 0" class="slots-section">
          <div class="slots-label">可选时段（点击选择）：</div>
          <div class="slots-grid">
            <el-tag
              v-for="(slot, idx) in availableSlots"
              :key="idx"
              :type="slot === selectedSlot ? '' : 'info'"
              :effect="slot === selectedSlot ? 'dark' : 'plain'"
              class="slot-tag"
              @click="selectSlot(slot)"
            >
              {{ slot.startTime }} - {{ slot.endTime }}
            </el-tag>
          </div>
        </div>
        <el-empty v-else-if="slotsQueried && !slotsLoading" description="该日期暂无可用时段" :image-size="60" />
      </div>
      <template #footer>
        <el-button @click="bookingVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" :disabled="!selectedSlot" @click="submitBooking">确认预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Monitor, Calendar, Finished, WindPower, Coffee } from '@element-plus/icons-vue'
import { getDeviceList, getAvailableSlots, submitReservation, getMyReservations, cancelReservation } from '@/api/reservation'
import request from '@/utils/request'

// === 获取学生所在楼栋 & 该楼设备 ===
const buildingNo = ref('')
const buildingName = computed(() => buildingNo.value ? `${buildingNo.value}号楼` : '所在楼栋')
const devices = ref([])
const floors = computed(() => {
  const set = new Set()
  devices.value.forEach(d => {
    const m = d.deviceName.match(/(\d+)楼/)
    if (m) set.add(m[1])
  })
  return Array.from(set).sort()
})

const fetchDevices = async () => {
  try {
    const info = await request({ url: '/student/info', method: 'get' }) || {}
    buildingNo.value = info.buildingNo || ''
    const list = await getDeviceList(buildingNo.value) || []
    devices.value = list
  } catch (e) { console.error(e) }
}

const getDevicesByFloor = (floor) => {
  return devices.value.filter(d => d.deviceName.includes(floor + '楼'))
}

const deviceIcon = (name) => {
  if (name.includes('洗衣机')) return 'Finished'
  if (name.includes('吹风机')) return 'WindPower'
  return 'Coffee'
}

// === 预约弹窗 ===
const bookingVisible = ref(false)
const selectedDevice = ref(null)
const selectedDate = ref('')
const availableSlots = ref([])
const selectedSlot = ref(null)
const slotsLoading = ref(false)
const slotsQueried = ref(false)

const disabledDate = (time) => time.getTime() < Date.now() - 8.64e7

// 本地时区格式化日期（避免 toISOString 的 UTC 时差问题）
const fmtDate = (d) => {
  if (!d) return ''
  if (typeof d === 'string') return d
  const dt = new Date(d)
  const y = dt.getFullYear()
  const m = String(dt.getMonth() + 1).padStart(2, '0')
  const day = String(dt.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const openBooking = (device) => {
  selectedDevice.value = device
  selectedDate.value = ''
  availableSlots.value = []
  selectedSlot.value = null
  slotsQueried.value = false
  bookingVisible.value = true
}

const querySlots = async () => {
  if (!selectedDate.value) { ElMessage.warning('请选择日期'); return }
  slotsLoading.value = true
  slotsQueried.value = true
  selectedSlot.value = null
  try {
    const dateStr = fmtDate(selectedDate.value)
    availableSlots.value = await getAvailableSlots(selectedDevice.value.id, dateStr) || []
  } catch (e) { availableSlots.value = [] }
  finally { slotsLoading.value = false }
}

const selectSlot = (slot) => {
  selectedSlot.value = selectedSlot.value === slot ? null : slot
}

const submitLoading = ref(false)
const submitBooking = async () => {
  if (!selectedSlot.value) return
  submitLoading.value = true
  try {
    const dateStr = fmtDate(selectedDate.value)
    await submitReservation({
      deviceId: selectedDevice.value.id,
      reservationDate: dateStr,
      startTime: selectedSlot.value.startTime,
      endTime: selectedSlot.value.endTime
    })
    ElMessage.success('预约成功')
    bookingVisible.value = false
    fetchMyReservations()
  } catch (e) { console.error(e) }
  finally { submitLoading.value = false }
}

// === 我的预约 ===
const tableLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10 })

const fetchMyReservations = async () => {
  tableLoading.value = true
  try {
    const res = await getMyReservations(queryParams)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) { console.error(e) }
  finally { tableLoading.value = false }
}

const resStatusLabel = (s) => ({ 0: '预约中', 1: '已完成', 2: '已取消' }[s] || '未知')
const resStatusType = (s) => ({ 0: 'primary', 1: 'success', 2: 'info' }[s] || 'info')

const handleCancel = (id) => {
  ElMessageBox.confirm('确定要取消该预约吗？', '提示', { type: 'warning' }).then(async () => {
    await cancelReservation(id)
    ElMessage.success('取消成功')
    fetchMyReservations()
  }).catch(() => {})
}

const handleWsMessage = (e) => {
  try {
    const msg = JSON.parse(e.detail)
    if (msg.type === 'device_status_changed') {
      fetchDevices()
    } else if (msg.type === 'reservation_changed') {
      fetchDevices()
      if (bookingVisible.value) {
        availableSlots.value = []
        selectedSlot.value = null
        slotsQueried.value = false
      }
    } else if (msg.type === 'reservation_completed') {
      fetchMyReservations()
    }
  } catch {}
}

onMounted(() => {
  fetchDevices()
  fetchMyReservations()
  window.addEventListener('ws-message', handleWsMessage)
})

onUnmounted(() => {
  window.removeEventListener('ws-message', handleWsMessage)
})
</script>

<style scoped>
.device-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; }
.empty-hint { text-align: center; padding: 20px 0; }
.floor-section { margin-bottom: 20px; }
.floor-title { font-size: 15px; font-weight: 600; color: #303133; margin-bottom: 12px; padding-left: 4px; border-left: 3px solid #409eff; }
.device-item { text-align: center; padding: 20px 8px; border: 1px solid #ebeef5; border-radius: 8px; cursor: pointer; transition: all 0.3s; background: #fff; }
.device-item:hover { border-color: #409eff; box-shadow: 0 2px 12px rgba(64,158,255,0.12); transform: translateY(-2px); }
.device-icon { margin-bottom: 10px; }
.device-name { font-size: 14px; color: #303133; margin-bottom: 8px; line-height: 1.4; }
.table-card { }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
.no-action { color: #c0c4cc; }

.booking-content { }
.device-info-bar { padding: 10px 16px; background: #f0f7ff; border-radius: 6px; color: #409eff; font-weight: 500; }
.slots-section { margin-top: 16px; padding-top: 12px; border-top: 1px dashed #dcdfe6; }
.slots-label { font-size: 14px; color: #606266; margin-bottom: 10px; font-weight: 600; }
.slots-grid { display: flex; flex-wrap: wrap; gap: 10px; }
.slot-tag { cursor: pointer; font-size: 14px; padding: 8px 16px; }
</style>
