import request from '@/utils/request'

// 获取可用设备列表（可选按楼栋过滤）
export const getDeviceList = (buildingNo) => {
  return request({
    url: '/student/device/list',
    method: 'get',
    params: buildingNo ? { buildingNo } : {}
  })
}

// 查询某设备某天的可用时段
export const getAvailableSlots = (deviceId, reservationDate) => {
  return request({
    url: '/student/reservation/available-slots',
    method: 'get',
    params: { deviceId, reservationDate }
  })
}

// 提交预约
export const submitReservation = (data) => {
  return request({
    url: '/student/reservation/submit',
    method: 'post',
    data: data
  })
}

// 查询我的预约列表
export const getMyReservations = (params) => {
  return request({
    url: '/student/reservation/my',
    method: 'get',
    params: params
  })
}

// 取消预约
export const cancelReservation = (id) => {
  return request({
    url: `/student/reservation/cancel/${id}`,
    method: 'put'
  })
}
