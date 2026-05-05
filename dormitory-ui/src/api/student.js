import request from '@/utils/request'

export const getStudentInfo = () => {
  return request({
    url: '/student/info',
    method: 'get'
  })
}

export const updateStudentProfile = (data) => {
  return request({
    url: '/student/profile',
    method: 'put',
    data
  })
}

export const changeStudentPassword = (data) => {
  return request({
    url: '/student/password',
    method: 'put',
    data
  })
}

export const updateAvatar = (avatar) => {
  return request({
    url: '/student/avatar',
    method: 'put',
    data: { avatar }
  })
}
