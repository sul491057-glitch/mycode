<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
// 🔴 修改 1：引入 API 接口
import { login } from '@/api/user'

const router = useRouter()
const loading = ref(false)

const adminForm = reactive({
  username: '',
  password: ''
})

// 🔴 修改 2：重写管理员登录逻辑
const handleAdminLogin = async () => {
  if (!adminForm.username || !adminForm.password) {
    return ElMessage.warning('请输入账号和密码')
  }

  loading.value = true
  try {
    // 登录前先清除旧 Token，确保请求环境干净
    localStorage.removeItem('token')
    localStorage.removeItem('role')

    // 调用后端接口
    // 注意：src/utils/request.ts 中的拦截器已配置为自动返回 res.data
    // 所以这里的 data 变量就是后端返回的 Token 字符串，而不是整个 JSON 对象
    const data: any = await login(adminForm)
    
    // 如果登录成功（request.ts 已经判断了 code === 1/200）
    ElMessage.success('管理员登录成功')
    
    // 修正任务：确保只把纯字符串的 Token 存进去
    const tokenStr = typeof data === 'string' ? data : data?.data
    
    if (tokenStr && typeof tokenStr === 'string') {
      localStorage.setItem('token', tokenStr)
    } else {
      console.error('Token 格式异常:', data)
    }

    localStorage.setItem('role', 'admin')
    
    router.push('/admin/dashboard')
  } catch (error: any) {
    // 登录失败的提示通常在 request.ts 里已经由 ElMessage 弹出了
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

// 顾客登录逻辑（如果需要对接后端，也应改为 API 调用）
const handleCustomerLogin = () => {
  localStorage.setItem('role', 'customer') // 标记为顾客身份
  ElMessage.success('欢迎光临！')
  router.push('/customer/home')
}
</script>

<template>
  <div class="min-h-screen w-full flex justify-center items-center bg-gray-50 relative overflow-hidden">
    <!-- 全局背景光效 (在最底层) -->
    <div class="flowing-light-blob blob-1"></div>
    <div class="flowing-light-blob blob-2"></div>
    
    <!-- 核心布局容器：左右分栏 -->
    <div class="w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col lg:flex-row items-center justify-between z-10 gap-12 lg:gap-24">
      
      <!-- 左侧：文字展示区 (直接悬浮在背景上，无边框) -->
      <div class="w-full lg:w-1/2 text-center lg:text-left relative">
        <!-- 独立的文字背景光效，增强文字可读性与氛围 -->
        <div class="absolute -top-20 -left-20 w-96 h-96 bg-purple-200 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob"></div>
        <div class="absolute -bottom-20 -right-20 w-96 h-96 bg-blue-200 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob animation-delay-2000"></div>

        <div class="relative">
          <h1 class="font-smiley text-5xl md:text-6xl xl:text-7xl mb-6 text-slate-900 tracking-wider leading-tight typing-container">
            <span class="typing-text-1">餐厅预定管理系统</span>
          </h1>
          <p class="font-smiley text-2xl md:text-3xl xl:text-4xl text-slate-600 tracking-widest typing-container">
            <span class="typing-text-2">欢迎使用</span>
          </p>
        </div>
      </div>

      <!-- 右侧：登录表单卡片 (独立的立体卡片) -->
      <div class="w-full lg:w-[480px] bg-white rounded-3xl shadow-2xl p-8 md:p-12 backdrop-blur-sm bg-opacity-95">
        <div class="space-y-8">
          
          <!-- 管理员登录标题 (恢复默认字体) -->
          <div class="text-left">
            <h2 class="text-3xl font-bold text-gray-800 mb-2">管理员登录</h2>
            <p class="text-gray-400 text-sm">请输入您的管理员账号和密码</p>
          </div>

          <!-- 登录表单 -->
          <el-form :model="adminForm" class="space-y-5" @keyup.enter="handleAdminLogin">
            <el-form-item prop="username">
              <el-input 
                v-model="adminForm.username" 
                placeholder="请输入管理员账号" 
                class="custom-input"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                v-model="adminForm.password" 
                type="password" 
                placeholder="请输入密码" 
                show-password 
                class="custom-input"
              />
            </el-form-item>
            
            <el-button type="primary" class="login-btn" @click="handleAdminLogin" :loading="loading">
              登录
            </el-button>
          </el-form>

          <!-- 顾客通道分割线 -->
          <div class="relative py-2">
            <div class="absolute inset-0 flex items-center">
              <span class="w-full border-t border-gray-100"></span>
            </div>
            <div class="relative flex justify-center text-xs uppercase">
              <span class="px-4 bg-white text-gray-400 tracking-widest">顾客通道</span>
            </div>
          </div>

          <!-- 顾客登录按钮 -->
          <div class="flex justify-center">
            <el-button type="primary" class="login-btn customer-btn" @click="handleCustomerLogin">
              顾客登录
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 引入得意黑字体 */
@font-face {
  font-family: 'Smiley Sans';
  src: url('https://npm.elemecdn.com/font-smiley-sans/SmileySans-Oblique.ttf.woff2') format('woff2');
  font-display: swap;
}

/* 仅用于左侧大标题 */
.font-smiley {
  font-family: 'Smiley Sans', 'Segoe UI', sans-serif;
}

/* 统一按钮样式 */
.login-btn {
  width: 100%;
  height: 3.2rem;
  font-size: 1.1rem;
  font-weight: 500;
  border-radius: 0.75rem; /* rounded-xl */
  border: none;
  background-color: #6366f1; /* indigo-500 */
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 6px -1px rgba(99, 102, 241, 0.2), 0 2px 4px -1px rgba(99, 102, 241, 0.1);
}

.login-btn:hover {
  background-color: #4f46e5; /* indigo-600 */
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(99, 102, 241, 0.3), 0 4px 6px -2px rgba(99, 102, 241, 0.1);
}

.login-btn:active {
  transform: translateY(0);
}

/* 顾客按钮特定样式 */
.customer-btn {
  background-color: #0ea5e9; /* sky-500 */
  box-shadow: 0 4px 6px -1px rgba(14, 165, 233, 0.2), 0 2px 4px -1px rgba(14, 165, 233, 0.1);
}

.customer-btn:hover {
  background-color: #0284c7; /* sky-600 */
  box-shadow: 0 10px 15px -3px rgba(14, 165, 233, 0.3), 0 4px 6px -2px rgba(14, 165, 233, 0.1);
}

/* 全局背景流动光效 */
.flowing-light-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  z-index: 0;
  opacity: 0.5;
}

.blob-1 {
  width: 800px;
  height: 800px;
  background: radial-gradient(circle, rgba(167, 139, 250, 0.4) 0%, rgba(244, 114, 182, 0.2) 60%, transparent 100%);
  top: -20%;
  left: -10%;
  animation: float 15s infinite ease-in-out;
}

.blob-2 {
  width: 700px;
  height: 700px;
  background: radial-gradient(circle, rgba(96, 165, 250, 0.4) 0%, rgba(192, 132, 252, 0.2) 60%, transparent 100%);
  bottom: -20%;
  right: -10%;
  animation: float-reverse 18s infinite ease-in-out;
}

/* 局部光效动画 */
.animate-blob {
  animation: blob 7s infinite;
}
.animation-delay-2000 {
  animation-delay: 2s;
}
@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}

@keyframes float {
  0% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(50px, 50px) rotate(10deg); }
  66% { transform: translate(-30px, 20px) rotate(-5deg); }
  100% { transform: translate(0, 0) rotate(0deg); }
}

@keyframes float-reverse {
  0% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(-50px, -50px) rotate(-10deg); }
  100% { transform: translate(0, 0) rotate(0deg); }
}

/* 打字机效果容器 */
.typing-container {
  display: block;
  width: fit-content;
}

.typing-text-1 {
  display: inline-block;
  overflow: hidden;
  white-space: nowrap;
  border-right: 4px solid #333;
  width: 0;
  animation: typing 1.5s steps(20, end) forwards, blink 0.75s step-end infinite;
}

.typing-text-2 {
  display: inline-block;
  overflow: hidden;
  white-space: nowrap;
  border-right: 4px solid #666;
  width: 0;
  opacity: 0;
  animation: typing 1s steps(10, end) forwards, blink 0.75s step-end infinite;
  animation-delay: 1.6s; /* 等第一行打完 */
  animation-fill-mode: forwards;
}

@keyframes typing {
  from { width: 0; opacity: 1; }
  to { width: 100%; opacity: 1; border-color: transparent; } /* 结束时隐藏光标 */
}

@keyframes blink {
  from, to { border-color: transparent; }
  50% { border-color: currentColor; }
}

/* 自定义输入框样式 */
:deep(.custom-input .el-input__wrapper) {
  background-color: #f9fafb; /* gray-50 */
  box-shadow: none !important;
  border-radius: 0.75rem; /* rounded-xl */
  padding: 12px 16px;
  transition: all 0.3s ease;
}

:deep(.custom-input .el-input__wrapper:hover) {
  background-color: #f3f4f6; /* gray-100 */
}

:deep(.custom-input .el-input__wrapper.is-focus) {
  background-color: #ffffff;
  box-shadow: 0 0 0 2px #6366f1 !important; /* indigo-500 */
  transform: translateY(-1px);
}

:deep(.custom-input .el-input__inner) {
  height: auto;
  font-size: 1rem;
  color: #1f2937;
}
</style>