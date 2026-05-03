import request from '@/utils/request'

export const getStudentInfo = () => {
  return request({
    url: '/student/info',
    method: 'get'
  })
}
