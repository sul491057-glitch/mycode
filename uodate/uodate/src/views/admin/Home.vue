<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getOrders } from "@/api/order";
import { getReservations } from "@/api/reservation";
import { Wallet, Calendar, TrendCharts } from '@element-plus/icons-vue'

const orders = ref<any[]>([])
const reservations = ref<any[]>([])
const loading = ref(false)

const today = new Date().toISOString().split('T')[0]

// 1. 今日销售额
const todaySales = computed(() => {
  return orders.value
    .filter(o => o.createTime && o.createTime.startsWith(today))
    .reduce((sum, o) => sum + (Number(o.totalAmount) || 0), 0)
    .toFixed(2)
})

// 2. 今日预定数
const todayReservationsCount = computed(() => {
  return reservations.value
    .filter(r => r.reserveTime && r.reserveTime.startsWith(today))
    .length
})

// 3. 待处理订单
const pendingOrdersCount = computed(() => {
  return orders.value.filter(o => o.status === 'pending' || o.status === '待处理').length
})

// 4. 趋势数据 (最近7天)
const trendData = computed(() => {
  const data = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    const dateStr = d.toISOString().split('T')[0]
    
    // 当日销售额
    const dailySales = orders.value
      .filter(o => o.createTime && o.createTime.startsWith(dateStr))
      .reduce((sum, o) => sum + (Number(o.totalAmount) || 0), 0)
      
    data.push({ date: dateStr.slice(5), value: dailySales })
  }
  return data
})

const maxSales = computed(() => {
  return Math.max(...trendData.value.map(d => d.value), 100) // 避免除以0，最小100
})

const fetchData = async () => {
  loading.value = true
  try {
    const [ordersRes, reservationsRes] = await Promise.all([
      getOrders(),
      getReservations()
    ])
    orders.value = ordersRes as any[]
    reservations.value = reservationsRes as any[]
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="p-6 space-y-8">
    <!-- 顶部数据卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div class="bg-gradient-to-br from-indigo-500 to-purple-600 rounded-2xl p-6 text-white shadow-lg shadow-indigo-200 transform hover:scale-105 transition-transform duration-300">
        <div class="flex items-center justify-between mb-4">
          <div class="bg-white/20 p-2 rounded-lg">
            <el-icon class="text-2xl"><Wallet /></el-icon>
          </div>
          <span class="text-xs bg-white/20 px-2 py-1 rounded-full">今日</span>
        </div>
        <div class="text-3xl font-bold font-mono mb-1">￥{{ todaySales }}</div>
        <div class="text-sm opacity-80">今日销售总额</div>
      </div>

      <div class="bg-gradient-to-br from-rose-500 to-orange-500 rounded-2xl p-6 text-white shadow-lg shadow-rose-200 transform hover:scale-105 transition-transform duration-300">
        <div class="flex items-center justify-between mb-4">
          <div class="bg-white/20 p-2 rounded-lg">
            <el-icon class="text-2xl"><Calendar /></el-icon>
          </div>
          <span class="text-xs bg-white/20 px-2 py-1 rounded-full">今日</span>
        </div>
        <div class="text-3xl font-bold font-mono mb-1">{{ todayReservationsCount }} <span class="text-sm font-normal">桌</span></div>
        <div class="text-sm opacity-80">今日预定情况</div>
      </div>

      <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 flex flex-col justify-center">
        <div class="flex items-center gap-4 mb-2">
           <div class="w-12 h-12 rounded-full bg-blue-50 text-blue-500 flex items-center justify-center font-bold text-xl">
             {{ pendingOrdersCount }}
           </div>
           <div>
             <div class="text-gray-500 text-sm">待处理订单</div>
             <div class="font-bold text-gray-800">请及时处理</div>
           </div>
        </div>
        <div class="w-full bg-gray-100 rounded-full h-2 mt-2 overflow-hidden">
           <div class="bg-blue-500 h-full rounded-full animate-pulse" style="width: 60%"></div>
        </div>
      </div>
    </div>

    <!-- 可视化趋势图 (纯 CSS 实现) -->
    <div class="bg-white rounded-3xl p-8 shadow-sm border border-gray-100">
      <div class="flex items-center gap-3 mb-8">
        <div class="p-2 bg-green-50 text-green-600 rounded-lg">
          <el-icon class="text-xl"><TrendCharts /></el-icon>
        </div>
        <h3 class="text-xl font-bold text-gray-800 font-smiley">近7日销售趋势</h3>
      </div>

      <div class="h-64 flex items-end justify-between gap-2 sm:gap-4 px-2">
        <div 
          v-for="(item, index) in trendData" 
          :key="index" 
          class="flex-1 flex flex-col items-center group relative"
        >
          <!-- Tooltip -->
          <div class="absolute -top-12 bg-gray-800 text-white text-xs px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity mb-2 whitespace-nowrap z-10">
            ￥{{ item.value }}
          </div>
          
          <!-- Bar -->
          <div 
            class="w-full max-w-[40px] bg-indigo-100 rounded-t-lg relative overflow-hidden group-hover:bg-indigo-200 transition-all duration-500"
            :style="{ height: `${(item.value / maxSales) * 100}%` }"
          >
             <div class="absolute bottom-0 left-0 right-0 bg-indigo-500 h-0 transition-all duration-1000 group-hover:h-full opacity-80" :style="{ height: '100%' }"></div>
          </div>
          
          <!-- Date -->
          <div class="mt-3 text-xs text-gray-400 font-medium">{{ item.date }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@font-face {
  font-family: 'Smiley Sans';
  src: url('https://npm.elemecdn.com/font-smiley-sans/SmileySans-Oblique.ttf.woff2') format('woff2');
  font-display: swap;
}

.font-smiley {
  font-family: 'Smiley Sans', 'Segoe UI', sans-serif;
}
</style>
