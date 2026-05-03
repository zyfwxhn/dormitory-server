import request from '@/utils/request'

// 学生登录
export const studentLogin = (data) => {
  return request({
    url: '/student/login',
    method: 'post',
    data: data // 预期格式: { studentNo: '', password: '' }
  })
}

// 维修员登录
export const workerLogin = (data) => {
  return request({
    url: '/worker/login',
    method: 'post',
    data: data // 预期格式: { username: '', password: '' }
  })
}

// 管理员登录
export const adminLogin = (data) => {
  return request({
    url: '/admin/login',
    method: 'post',
    data: data // 预期格式: { username: '', password: '' }
  })
}