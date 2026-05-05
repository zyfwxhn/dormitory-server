import request from '@/utils/request'

export const getWorkerInfo = () => {
  return request({
    url: '/worker/info',
    method: 'get'
  })
}

export const getWorkerRepairPage = (params) => {
  return request({
    url: '/worker/repair/page',
    method: 'get',
    params: params
  })
}

export const getWorkerRepairDetail = (id) => {
  return request({
    url: `/worker/repair/${id}`,
    method: 'get'
  })
}

// params: { id, status, finishImages? }
export const updateRepairStatus = (data) => {
  return request({
    url: '/worker/repair/status',
    method: 'put',
    data: data
  })
}
