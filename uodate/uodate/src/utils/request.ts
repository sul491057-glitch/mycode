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
      config.headers['Authorization'] = token // 或者 'Bearer ' + token，视后端要求而定
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
    // 有些后端成功可能不返回 code，或者 code 为 0，请根据实际情况调整
    if (res.code === 200 || res.code === 0 || res.success === true) {
      return res.data || res
    } else {
      const msg = res.message || res.msg || 'Error'
      ElMessage.error(msg)
      return Promise.reject(new Error(msg))
    }
  },
  (error) => {
    console.error('API Error:', error)
    ElMessage.error(error.message || 'Request failed')
    return Promise.reject(error)
  }
)

// 🔴 关键点：必须加上这一行，否则其他文件 import 会报错
export default service