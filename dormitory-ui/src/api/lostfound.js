import request from '@/utils/request'

export const getLostFoundPage = (params) => {
  return request({
    url: '/student/lost-found/page',
    method: 'get',
    params: params
  })
}

export const publishLostFound = (data) => {
  return request({
    url: '/student/lost-found/publish',
    method: 'post',
    data: data
  })
}

export const updateLostFoundStatus = (data) => {
  return request({
    url: '/student/lost-found/status',
    method: 'put',
    data: data
  })
}

export const getLostFoundDetail = (id) => {
  return request({
    url: `/student/lost-found/${id}`,
    method: 'get'
  })
}

export const claimLostFound = (id) => {
  return request({
    url: `/student/lost-found/claim/${id}`,
    method: 'post'
  })
}

export const editLostFound = (id, data) => {
  return request({
    url: `/student/lost-found/${id}`,
    method: 'put',
    data
  })
}
