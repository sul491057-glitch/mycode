import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api', 
  timeout: 5000 
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 如果本地有 token，则携带
    const token = localStorage.getItem('token')
    if (token) {
      // 检查 token 是否包含非 ASCII 字符
      // 如果包含非法字符，仅清除本地存储，但不阻断请求
      // 这样如果是登录请求，可以继续进行（不带 token），从而成功登录并覆盖旧 token
      // eslint-disable-next-line no-control-regex
      if (/[^\x00-\x7F]/.test(token)) {
        localStorage.removeItem('token')
        localStorage.removeItem('role')
        console.warn('检测到本地 Token 格式异常，已自动清除，将发起无 Token 请求')
      } else {
        config.headers['token'] = token // 按要求修改 Key 为 'token'
      }
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 注意：这里要对应你后端 Result 类的 code 字段
    // 后端返回 code: 1 也表示成功
    if (res.code === 200 || res.code === 0 || res.code === 1 || res.success === true) {
      return res.data || res
    } else {
      const msg = res.message || res.msg || 'Error'
      ElMessage.error(msg)
      return Promise.reject(new Error(msg))
    }
  },
  (error) => {
    // 监听响应错误
    if (error.response && error.response.status === 401) {
      // 清除 token
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      
      // 提示用户
      ElMessage.error('登录已过期，请重新登录')
      
      // 强制跳转回登录页
      // 使用 window.location.href 确保完全重置状态，或者引入 router 进行 push
      if (window.location.pathname !== '/login') {
         window.location.href = '/login'
      }
      return Promise.reject(error)
    }

    console.error('API Error:', error)
    ElMessage.error(error.message || 'Request failed')
    return Promise.reject(error)
  }
)

// 🔴 关键点：必须加上这一行，否则其他文件 import 会报错
export default service