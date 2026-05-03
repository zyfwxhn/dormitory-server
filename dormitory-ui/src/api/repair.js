import request from '@/utils/request'

// 分页查询我的报修
export const getMyRepairPage = (params) => {
  return request({
    url: '/student/repair/page',
    method: 'get',
    params: params
  })
}

// 提交报修申请
export const submitRepair = (data) => {
  return request({
    url: '/student/repair/submit',
    method: 'post',
    data: data
  })
}

// 取消报修单
export const cancelRepair = (id) => {
  return request({
    url: `/student/repair/cancel/${id}`,
    method: 'put'
  })
}

// 评价报修单
export const evaluateRepair = (data) => {
  return request({
    url: '/student/repair/evaluate',
    method: 'post',
    data: data
  })
}

// 查询报修单详情
export const getRepairDetail = (id) => {
  return request({
    url: `/student/repair/${id}`,
    method: 'get'
  })
}