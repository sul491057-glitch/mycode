<template>
  <div class="common-layout h-screen overflow-hidden bg-gray-50 flex">
    <!-- 侧边栏 -->
    <el-aside 
      :width="isCollapse ? '64px' : '240px'" 
      class="bg-white/80 backdrop-blur-md border-r border-gray-100 flex flex-col shadow-lg z-20 relative transition-all duration-300"
    >
      <div class="h-16 flex items-center justify-between px-4 border-b border-gray-50 bg-transparent overflow-hidden">
        <!-- 标题 (仅展开时显示) -->
        <div 
          v-show="!isCollapse"
          class="text-xl font-bold font-smiley bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent tracking-wide whitespace-nowrap"
        >
          餐厅管理系统
        </div>
        
        <!-- 折叠/展开按钮 -->
        <div 
          class="cursor-pointer text-gray-400 hover:text-indigo-600 transition-colors p-1 rounded-md hover:bg-gray-100/50"
          :class="{ 'mx-auto': isCollapse }"
          @click="toggleCollapse"
        >
          <el-icon :size="20">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </div>
      </div>
      
      <el-menu
        :default-active="route.path"
        class="flex-1 border-none py-4 px-2 custom-menu"
        router
        :collapse="isCollapse"
        :collapse-transition="false"
      >
        <el-menu-item index="/admin/dashboard" class="rounded-xl mb-2 hover:bg-indigo-50 transition-colors">
          <el-icon><Odometer /></el-icon>
          <template #title><span class="font-medium">仪表盘</span></template>
        </el-menu-item>
        <el-menu-item index="/admin/orders" class="rounded-xl mb-2 hover:bg-indigo-50 transition-colors">
          <el-icon><List /></el-icon>
          <template #title><span class="font-medium">订单管理</span></template>
        </el-menu-item>
        <el-menu-item index="/admin/reservations" class="rounded-xl mb-2 hover:bg-indigo-50 transition-colors">
          <el-icon><Calendar /></el-icon>
          <template #title><span class="font-medium">预定管理</span></template>
        </el-menu-item>
        <el-menu-item index="/admin/products" class="rounded-xl mb-2 hover:bg-indigo-50 transition-colors">
          <el-icon><KnifeFork /></el-icon>
          <template #title><span class="font-medium">菜品及推荐管理</span></template>
        </el-menu-item>
      </el-menu>

      <div class="p-4 border-t border-gray-50">
        <div 
          class="flex items-center gap-3 px-3 py-2 rounded-xl bg-gray-50 hover:bg-gray-100 transition-colors cursor-pointer"
          :class="{ 'justify-center px-0': isCollapse }"
        >
          <div class="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 font-bold shadow-sm flex-shrink-0">
            A
          </div>
          <div v-show="!isCollapse" class="flex-1 min-w-0">
            <div class="text-sm font-bold text-gray-700 truncate">管理员</div>
            <div class="text-xs text-gray-400">在线中</div>
          </div>
          <el-button 
            v-show="!isCollapse"
            type="danger" 
            circle 
            size="small" 
            :icon="SwitchButton" 
            plain 
            @click="handleLogout" 
            class="border-none bg-transparent hover:bg-rose-50 text-rose-500 transition-colors" 
          />
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
import { SwitchButton, Menu, Odometer, List, Calendar, KnifeFork, Fold, Expand } from '@element-plus/icons-vue'
import { ref, onMounted, onUnmounted } from 'vue'
import { ElNotification } from 'element-plus'
import { emitRefreshDashboard } from '@/utils/eventBus'

const router = useRouter()
const route = useRoute()

// 侧边栏折叠状态
const isCollapse = ref(false)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// WebSocket 相关逻辑
const ws = ref<WebSocket | null>(null)
// 预加载音频文件 (请确保 public/audio/new_order.mp3 存在)
const audio = new Audio('/audio/new_order.mp3')

const initWebSocket = () => {
  // 建立连接：后端 WebSocket 地址为 /ws/orders
  ws.value = new WebSocket('ws://localhost:8085/ws/orders')

  ws.value.onopen = () => {
    console.log('WebSocket 连接成功')
  }

  ws.value.onmessage = (event) => {
    console.log('收到消息:', event.data)
    
    let messageContent = ''
    let title = '新增订单' // 默认标题
    let targetPath = '/admin/orders' // 默认跳转路径

    // 预处理消息数据，统一转为字符串以便匹配
    let rawData = event.data
    
    // 尝试解析 JSON
    try {
      const data = JSON.parse(event.data)
      // 如果是 JSON，提取可能的字段值
      if (data.content) rawData = data.content
      else if (data.message) rawData = data.message
      else if (data.type) rawData = data.type
    } catch (e) {
      // 忽略解析错误，继续使用原始字符串
    }
    
    // 强制转换为字符串进行匹配
    const contentStr = String(rawData).trim()

    // --- 核心匹配逻辑 (中英文全覆盖) ---
    
    // 1. 判断是否为预定 (RESERVATION)
    if (
      contentStr === 'RESERVATION_UPDATE' || 
      contentStr === 'NEW_RESERVATION' ||
      contentStr.includes('预定') || 
      contentStr.includes('预订')
    ) {
      title = '新增预定'
      targetPath = '/admin/reservations'
      messageContent = '您有一条新的预定信息，请及时处理'
    } 
    // 2. 判断是否为订单 (ORDER)
    else if (
      contentStr === 'NEW_ORDER' || 
      contentStr === 'ORDER_UPDATE' ||
      contentStr.includes('订单')
    ) {
      title = '新增订单'
      targetPath = '/admin/orders'
      messageContent = '您有一条新的订单消息，请及时处理'
    } 
    // 3. 其他情况 (兜底)
    else {
      // 默认为订单提醒，且强制使用中文
      title = '系统提醒'
      messageContent = '收到一条新消息，请查看'
    }

    // 播放提示音
    audio.play().catch(e => console.warn('自动播放音频被阻止:', e))

    // 触发主页数据刷新
    emitRefreshDashboard()

    // 弹窗通知 (毛玻璃效果)
    ElNotification({
      title: title,
      message: messageContent,
      type: 'success',
      duration: 0,
      position: 'bottom-right',
      offset: 50,
      customClass: 'custom-notification-glass',
      onClick: () => {
        router.push(targetPath)
      }
    })
  }

  ws.value.onerror = (error) => {
    console.error('WebSocket 连接发生错误:', error)
  }

  ws.value.onclose = () => {
    console.log('WebSocket 连接已关闭')
  }
}

onMounted(() => {
  initWebSocket()
})

onUnmounted(() => {
  if (ws.value) {
    ws.value.close()
  }
})

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

/* 毛玻璃通知弹窗样式 */
:global(.custom-notification-glass) {
  background-color: rgba(255, 255, 255, 0.7) !important;
  backdrop-filter: blur(12px) !important;
  -webkit-backdrop-filter: blur(12px) !important;
  border: 1px solid rgba(255, 255, 255, 0.6) !important;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.15) !important;
  border-radius: 16px !important;
}
:global(.custom-notification-glass .el-notification__title) {
  color: #111827 !important; /* gray-900 */
  font-weight: 800 !important;
  font-family: 'Smiley Sans', sans-serif !important;
}
:global(.custom-notification-glass .el-notification__content) {
  color: #4b5563 !important; /* gray-600 */
  font-weight: 500 !important;
}
</style>
