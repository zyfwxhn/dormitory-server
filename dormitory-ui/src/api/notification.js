import request from '@/utils/request'

export const getMyNotifications = () => {
  return request({
    url: '/student/notification',
    method: 'get'
  })
}

export const markAsRead = (id) => {
  return request({
    url: `/student/notification/${id}/read`,
    method: 'put'
  })
}
