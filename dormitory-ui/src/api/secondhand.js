import request from '@/utils/request'

export const getSecondhandPage = (params) => {
  return request({
    url: '/student/secondhand-item/page',
    method: 'get',
    params: params
  })
}

export const publishSecondhand = (data) => {
  return request({
    url: '/student/secondhand-item/publish',
    method: 'post',
    data: data
  })
}

export const updateSecondhandStatus = (data) => {
  return request({
    url: '/student/secondhand-item/status',
    method: 'put',
    data: data
  })
}

export const getSecondhandDetail = (id) => {
  return request({
    url: `/student/secondhand-item/${id}`,
    method: 'get'
  })
}

export const getItemMessages = (itemId) => {
  return request({
    url: `/student/item-message/${itemId}`,
    method: 'get'
  })
}

export const sendItemMessage = (data) => {
  return request({
    url: '/student/item-message',
    method: 'post',
    data: data
  })
}
