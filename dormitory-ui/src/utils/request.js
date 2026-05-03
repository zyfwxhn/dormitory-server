import axios from 'axios';
import { ElMessage, ElLoading } from 'element-plus';
import router from '@/router';

// 用于存储 loading 实例
let loadingInstance = null;

// 1. 创建 axios 实例
const service = axios.create({
  // baseURL 会自动从 .env 文件中读取 VITE_APP_BASE_API 的值
  baseURL: import.meta.env.VITE_APP_BASE_API,
  // 超时时间：10秒
  timeout: 10000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' }
});

// 2. 请求拦截器 (发出请求前)
service.interceptors.request.use(
  (config) => {
    // 根据请求 URL 自动匹配对应角色的 token，三端互不干扰
    const url = config.url || ''
    let token = null
    if (url.startsWith('/student/')) {
      token = localStorage.getItem('student_token')
    } else if (url.startsWith('/worker/')) {
      token = localStorage.getItem('worker_token')
    } else if (url.startsWith('/admin/')) {
      token = localStorage.getItem('admin_token')
    } else {
      // 通用接口（/common/upload 等）用任一可用 token
      token = localStorage.getItem('student_token')
           || localStorage.getItem('worker_token')
           || localStorage.getItem('admin_token')
    }
    if (token) {
      config.headers['token'] = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 3. 响应拦截器 (接收到后端数据后)
service.interceptors.response.use(
  (response) => {
    // 关掉 loading
    // if (loadingInstance) loadingInstance.close();

    // 拿到后端返回的 Result 对象
    const res = response.data;

    // 后端约定的成功 code 是 1
    if (res.code === 1) {
      // 剥壳：直接返回里面的 data
      return res.data;
    } else {
      // code 为 0 时，说明业务报错，弹出全局提示
      ElMessage.error(res.msg || '系统未知错误');
      return Promise.reject(new Error(res.msg || 'Error'));
    }
  },
  (error) => {
    // if (loadingInstance) loadingInstance.close();
    
    // 处理 HTTP 状态码错误
    let message = '';
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未登录或Token已过期，请重新登录';
          // 根据请求 URL 清除对应角色的失效 token，不影响其他已登录端
          {
            const failUrl = error.config?.url || ''
            if (failUrl.startsWith('/student/')) {
              localStorage.removeItem('student_token')
              localStorage.removeItem('student_userInfo')
            } else if (failUrl.startsWith('/worker/')) {
              localStorage.removeItem('worker_token')
              localStorage.removeItem('worker_userInfo')
            } else if (failUrl.startsWith('/admin/')) {
              localStorage.removeItem('admin_token')
              localStorage.removeItem('admin_userInfo')
            }
          }
          router.push('/login');
          break;
        case 403:
          message = '您没有权限访问该接口';
          break;
        case 404:
          message = '请求的接口不存在';
          break;
        case 500:
          message = '后端服务器异常，请联系管理员';
          break;
        default:
          message = `连接错误 ${error.response.status}`;
      }
    } else {
      message = '网络连接超时或服务器宕机';
    }
    ElMessage.error(message);
    return Promise.reject(error);
  }
);

// 导出封装好的 axios 实例
export default service;