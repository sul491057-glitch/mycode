<template>
  <div class="common-layout h-screen flex flex-col bg-gray-50 font-sans">
    <el-container class="h-full">
      <el-header class="bg-white/80 backdrop-blur-md border-b border-gray-100 flex items-center justify-between px-6 h-16 sticky top-0 z-50 shadow-sm">
        <div class="flex items-center gap-2 cursor-pointer group" @click="router.push('/customer/home')">
          <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-orange-500 to-rose-500 flex items-center justify-center text-white font-bold shadow-md group-hover:scale-110 transition-transform">
            M
          </div>
          <div class="text-xl font-bold font-smiley bg-gradient-to-r from-orange-600 to-rose-600 bg-clip-text text-transparent">
            美味餐厅
          </div>
        </div>
        
        <div class="flex items-center gap-6">
          <div class="hidden md:flex items-center bg-gray-50 rounded-full p-1 border border-gray-100">
            <el-button 
              link 
              class="!text-gray-600 hover:!text-orange-500 !px-4 !font-medium transition-colors"
              :class="{ '!text-orange-600 !font-bold': $route.path === '/customer/home' }"
              @click="router.push('/customer/home')"
            >首页</el-button>
            <el-button 
              link 
              class="!text-gray-600 hover:!text-orange-500 !px-4 !font-medium transition-colors"
              :class="{ '!text-orange-600 !font-bold': $route.path === '/customer/menu' }"
              @click="router.push('/customer/menu')"
            >点餐</el-button>
            <el-button 
              link 
              class="!text-gray-600 hover:!text-orange-500 !px-4 !font-medium transition-colors"
              :class="{ '!text-orange-600 !font-bold': $route.path === '/customer/reservation' }"
              @click="router.push('/customer/reservation')"
            >预订</el-button>
          </div>

          <el-dropdown @command="handleCommand" trigger="click">
            <span class="el-dropdown-link cursor-pointer flex items-center gap-2 hover:bg-gray-50 px-3 py-2 rounded-full transition-colors">
              <el-avatar :size="32" class="bg-indigo-100 text-indigo-600 font-bold">客</el-avatar>
              <span class="text-sm font-medium text-gray-700 hidden sm:block">尊敬的顾客</span>
              <el-icon class="el-icon--right text-gray-400"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu class="custom-dropdown">
                <el-dropdown-item command="logout" class="text-rose-500">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="p-0 relative bg-gray-50">
        <!-- 路由视图容器，确保内容滚动 -->
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()

const handleCommand = (command: string) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    router.push('/login')
  }
}
</script>

<style scoped>
/* 引入字体 */
@font-face {
  font-family: 'Smiley Sans';
  src: url('https://npm.elemecdn.com/font-smiley-sans/SmileySans-Oblique.ttf.woff2') format('woff2');
  font-display: swap;
}

.font-smiley {
  font-family: 'Smiley Sans', 'Segoe UI', sans-serif;
}

.el-main {
  /* 覆盖默认样式，让 router-view 控制滚动 */
  height: calc(100vh - 64px); /* 64px is header height */
  overflow-y: auto;
  scroll-behavior: smooth;
}

/* 滚动条美化 */
.el-main::-webkit-scrollbar {
  width: 6px;
}
.el-main::-webkit-scrollbar-track {
  background: transparent;
}
.el-main::-webkit-scrollbar-thumb {
  background: rgba(156, 163, 175, 0.3);
  border-radius: 3px;
}
.el-main::-webkit-scrollbar-thumb:hover {
  background: rgba(156, 163, 175, 0.5);
}
</style>
