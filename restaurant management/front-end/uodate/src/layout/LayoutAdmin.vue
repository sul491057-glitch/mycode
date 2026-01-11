<template>
  <div class="common-layout h-screen overflow-hidden bg-gray-50 flex">
    <!-- 侧边栏 -->
    <el-aside width="240px" class="bg-white border-r border-gray-100 flex flex-col shadow-lg z-20 relative">
      <div class="h-16 flex items-center justify-center border-b border-gray-50 bg-white/50 backdrop-blur-sm">
        <div class="text-xl font-bold font-smiley bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent tracking-wide">
          餐厅管理系统
        </div>
      </div>
      
      <el-menu
        :default-active="route.path"
        class="flex-1 border-none py-4 px-2 custom-menu"
        router
      >
        <el-menu-item index="/admin/dashboard" class="rounded-xl mb-2 hover:bg-indigo-50 transition-colors">
          <el-icon><Odometer /></el-icon>
          <span class="font-medium">仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders" class="rounded-xl mb-2 hover:bg-indigo-50 transition-colors">
          <el-icon><List /></el-icon>
          <span class="font-medium">订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/reservations" class="rounded-xl mb-2 hover:bg-indigo-50 transition-colors">
          <el-icon><Calendar /></el-icon>
          <span class="font-medium">预定管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/products" class="rounded-xl mb-2 hover:bg-indigo-50 transition-colors">
          <el-icon><KnifeFork /></el-icon>
          <span class="font-medium">菜品及推荐管理</span>
        </el-menu-item>
      </el-menu>

      <div class="p-4 border-t border-gray-50">
        <div class="flex items-center gap-3 px-3 py-2 rounded-xl bg-gray-50 hover:bg-gray-100 transition-colors cursor-pointer">
          <div class="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 font-bold shadow-sm">
            A
          </div>
          <div class="flex-1 min-w-0">
            <div class="text-sm font-bold text-gray-700 truncate">管理员</div>
            <div class="text-xs text-gray-400">在线中</div>
          </div>
          <el-button type="danger" circle size="small" :icon="SwitchButton" plain @click="handleLogout" class="border-none bg-transparent hover:bg-rose-50 text-rose-500 transition-colors" />
        </div>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="flex-1 flex flex-col min-w-0 bg-gray-50 relative">
      <el-main class="p-0 relative overflow-y-auto custom-scrollbar scroll-smooth">
        <div class="min-h-full">
           <router-view></router-view>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { SwitchButton, Menu, Odometer, List, Calendar, KnifeFork } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  router.push('/login')
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

/* 菜单激活样式 */
:deep(.el-menu-item.is-active) {
  background-color: #4f46e5 !important; /* indigo-600 */
  color: white !important;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

:deep(.el-menu-item:hover) {
  background-color: #eef2ff !important; /* indigo-50 */
  color: #4f46e5 !important;
}

/* 滚动条美化 */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(156, 163, 175, 0.3);
  border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(156, 163, 175, 0.5);
}
</style>
