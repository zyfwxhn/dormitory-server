import request from '@/utils/request'

// === 数据看板 ===
export const getStatisticsOverview = () => {
  return request({ url: '/admin/statistics/overview', method: 'get' })
}

// === 设备管理 ===
export const getDevicePage = (params) => {
  return request({ url: '/admin/device/page', method: 'get', params })
}
export const saveDevice = (data) => {
  return request({ url: '/admin/device', method: 'post', data })
}
export const updateDevice = (data) => {
  return request({ url: '/admin/device', method: 'put', data })
}
export const deleteDevices = (ids) => {
  return request({ url: '/admin/device', method: 'delete', params: { ids: ids.join(',') } })
}
export const toggleDeviceStatus = (status, id) => {
  return request({ url: `/admin/device/status/${status}`, method: 'post', params: { id } })
}

// === 维修员管理 ===
export const getWorkerPage = (params) => {
  return request({ url: '/admin/worker/page', method: 'get', params })
}
export const saveWorker = (data) => {
  return request({ url: '/admin/worker', method: 'post', data })
}
export const updateWorker = (data) => {
  return request({ url: '/admin/worker', method: 'put', data })
}
export const toggleWorkerStatus = (id, status) => {
  return request({ url: `/admin/worker/${id}/status/${status}`, method: 'put' })
}

// === 报修工单管理 ===
export const getAdminRepairPage = (params) => {
  return request({ url: '/admin/repair/page', method: 'get', params })
}
export const getAdminRepairDetail = (id) => {
  return request({ url: `/admin/repair/${id}`, method: 'get' })
}
export const dispatchRepair = (id) => {
  return request({ url: `/admin/repair/dispatch/${id}`, method: 'post' })
}

// === 学生导入 ===
export const importStudents = (formData) => {
  return request({
    url: '/admin/student/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// === 内容审核 ===
export const getAdminLostFoundPage = (params) => {
  return request({ url: '/admin/lost-found/page', method: 'get', params })
}
export const violateLostFound = (data) => {
  return request({ url: '/admin/lost-found/violate', method: 'put', data })
}
export const getAdminSecondhandPage = (params) => {
  return request({ url: '/admin/secondhand-item/page', method: 'get', params })
}
export const violateSecondhand = (data) => {
  return request({ url: '/admin/secondhand-item/violate', method: 'put', data })
}

// === 批量导入 ===
export const importDevices = (formData) => {
  return request({
    url: '/admin/device/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
export const importWorkers = (formData) => {
  return request({
    url: '/admin/worker/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// === 预约管理 ===
export const getAdminReservationPage = (params) => {
  return request({ url: '/admin/reservation/page', method: 'get', params })
}
