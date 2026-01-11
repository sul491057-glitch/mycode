<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getOrders } from "@/api/order";
import { getReservations } from "@/api/reservation";
import { getProductList } from "@/api/product";
import { Wallet, Calendar, TrendCharts, PieChart } from '@element-plus/icons-vue'

const router = useRouter()
const orders = ref<any[]>([])
const reservations = ref<any[]>([])
const products = ref<any[]>([])
const loading = ref(false)

const navigateTo = (path: string) => {
  router.push(path)
}

// 获取今日日期字符串 (yyyy-MM-dd)
const getTodayStr = () => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
}
const today = getTodayStr()

// --- ⭐ 核心修复：纯字符串日期比较 (最稳妥，无视时区) ---
// 你的后端返回格式是: "2026-01-09T21:43:27"
// 我们只需要 "T" 之前的部分 "2026-01-09"
const isSameDay = (val: string | null, targetDateStr: string) => {
  if (!val) return false
  // 1. 如果是 "2026-01-09T..." 这种格式，直接切分
  if (typeof val === 'string' && val.includes('T')) {
      return val.split('T')[0] === targetDateStr
  }
  // 2. 兜底：如果是其他格式，尝试转 Date 对象
  const d = new Date(val)
  if (isNaN(d.getTime())) return false
  const dStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  return dStr === targetDateStr
}

// 1. 今日销售额
const todaySales = computed(() => {
  return orders.value
    .filter(o => isSameDay(o.createTime, today))
    .reduce((sum, o) => sum + (Number(o.totalAmount) || 0), 0)
    .toFixed(2)
})

// 2. 今日预定数
const todayReservationsCount = computed(() => {
  return reservations.value
    .filter(r => isSameDay(r.reserveTime, today))
    .length
})

// 3. 待处理订单
const pendingOrdersCount = computed(() => {
  return orders.value.filter(o => o.status === 'pending' || o.status === '待处理').length
})

// 4. 趋势数据 (最近7天)
const trendData = computed(() => {
  const data = []
  // 生成最近7天的日期数组
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    // 构建 yyyy-MM-dd 格式
    const dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    
    // 过滤这一天的订单
    const dailySales = orders.value
      .filter(o => isSameDay(o.createTime, dateStr))
      .reduce((sum, o) => sum + (Number(o.totalAmount) || 0), 0)
      
    // 推入数据：date 只显示 '01-09' 这种短格式
    data.push({ date: dateStr.slice(5), value: dailySales })
  }
  return data
})

// 5. 菜品分类占比 (今日)
const categoryStats = computed(() => {
  const stats: Record<string, number> = {}
  
  // 1. 筛选今日订单
  const todayOrders = orders.value.filter(o => isSameDay(o.createTime, today))
  
  // 2. 统计各分类销量
  todayOrders.forEach(order => {
    // 兼容后端常见命名：items (前端习惯) 或 orderItems (Java后端习惯)
    let items = order.items || order.orderItems
    
    // 🔴 修复：如果 items 是字符串（数据库常见情况），尝试解析
    if (typeof items === 'string') {
      try {
        // 先去掉可能存在的转义符
        if (items.startsWith('"') && items.endsWith('"')) {
           items = items.slice(1, -1).replace(/\\"/g, '"');
        }
        items = JSON.parse(items)
      } catch (e) {
        console.error('解析订单 items 失败:', items)
        items = []
      }
    }

    if (items && Array.isArray(items)) {
      items.forEach((item: any) => {
        // 尝试从商品列表中查找分类（如果 item 中没有 category）
        let category = item.category
        if (!category && products.value.length) {
           // 宽松匹配：ID 转字符串对比
           const p = products.value.find(p => 
             String(p.id) === String(item.productId || item.id) || 
             p.name === item.name
           )
           if (p) category = p.category
        }
        
        category = category || '其他'
        
        // 累加数量
        stats[category] = (stats[category] || 0) + (Number(item.quantity) || 1)
      })
    }
  })
  
  const total = Object.values(stats).reduce((a, b) => a + b, 0)
  const colors = ['#6366f1', '#ec4899', '#f59e0b', '#10b981', '#3b82f6', '#8b5cf6', '#64748b']
  
  return Object.entries(stats)
    .map(([name, value], index) => ({
      name,
      value,
      percent: total ? ((value / total) * 100).toFixed(1) : '0.0',
      color: colors[index % colors.length]
    }))
    .sort((a, b) => b.value - a.value)
})

const pieStyle = computed(() => {
  if (!categoryStats.value.length) return { background: '#f3f4f6' }
  
  let segments = []
  let start = 0
  const total = categoryStats.value.reduce((sum, item) => sum + item.value, 0)
  
  categoryStats.value.forEach(item => {
    const p = (item.value / total) * 100
    const end = start + p
    segments.push(`${item.color} ${start}% ${end}%`)
    start = end
  })
  
  return {
    background: `conic-gradient(${segments.join(', ')})`
  }
})

// 计算最大值 (用于图表高度比例)
const maxSales = computed(() => {
  if (trendData.value.length === 0) return 100
  const max = Math.max(...trendData.value.map(d => d.value))
  return max === 0 ? 100 : max 
})

const fetchData = async () => {
  loading.value = true
  try {
    const [ordersRes, reservationsRes, productsRes] = await Promise.all([
      getOrders(),
      getReservations(),
      getProductList()
    ])

    // --- 数据解包 (处理 AxiosResponse) ---
    let realOrders = ordersRes as any
    if (realOrders && realOrders.data) realOrders = realOrders.data
    // 防止双重嵌套
    if (realOrders && realOrders.data && Array.isArray(realOrders.data)) realOrders = realOrders.data

    let realReservations = reservationsRes as any
    if (realReservations && realReservations.data) realReservations = realReservations.data
    
    let realProducts = productsRes as any
    if (realProducts && realProducts.data) realProducts = realProducts.data

    // 赋值
    orders.value = Array.isArray(realOrders) ? realOrders : []
    reservations.value = Array.isArray(realReservations) ? realReservations : []
    products.value = Array.isArray(realProducts) ? realProducts : []

    // 打印调试信息，检查商品数据和订单项格式
    console.log('📦 商品列表:', products.value.slice(0, 3))
    if (orders.value.length > 0) {
      console.log('🧾 首个订单字段:', Object.keys(orders.value[0]))
      const firstItems = orders.value[0].items || orders.value[0].orderItems
      console.log('🧾 首个订单 items 数据:', firstItems)
    }

    // 打印最终用于计算的数据，确保 createTime 存在
    console.log('✅ 最终订单数据:', orders.value.slice(0, 3)) 

  } catch (error) {
    console.error('❌ 数据加载失败:', error)
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
      <div 
        @click="navigateTo('/admin/orders')"
        class="cursor-pointer bg-gradient-to-br from-indigo-500 to-purple-600 rounded-2xl p-6 text-white shadow-lg shadow-indigo-200 transform hover:scale-105 transition-transform duration-300"
      >
        <div class="flex items-center justify-between mb-4">
          <div class="bg-white/20 p-2 rounded-lg">
            <el-icon class="text-2xl"><Wallet /></el-icon>
          </div>
          <span class="text-xs bg-white/20 px-2 py-1 rounded-full">今日</span>
        </div>
        <div class="text-3xl font-bold font-mono mb-1">￥{{ todaySales }}</div>
        <div class="text-sm opacity-80">今日销售总额</div>
      </div>

      <div 
        @click="navigateTo('/admin/reservations')"
        class="cursor-pointer bg-gradient-to-br from-rose-500 to-orange-500 rounded-2xl p-6 text-white shadow-lg shadow-rose-200 transform hover:scale-105 transition-transform duration-300"
      >
        <div class="flex items-center justify-between mb-4">
          <div class="bg-white/20 p-2 rounded-lg">
            <el-icon class="text-2xl"><Calendar /></el-icon>
          </div>
          <span class="text-xs bg-white/20 px-2 py-1 rounded-full">今日</span>
        </div>
        <div class="text-3xl font-bold font-mono mb-1">{{ todayReservationsCount }} <span class="text-sm font-normal">桌</span></div>
        <div class="text-sm opacity-80">今日预定情况</div>
      </div>

      <div 
        @click="navigateTo('/admin/orders')"
        class="cursor-pointer bg-white rounded-2xl p-6 shadow-sm border border-gray-100 flex flex-col justify-center transform hover:scale-105 transition-transform duration-300"
      >
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

    <!-- 可视化趋势图 (调试版) -->
   <div class="bg-white rounded-3xl p-8 shadow-sm border border-gray-100">
      <div class="flex items-center gap-3 mb-8">
        <div class="p-2 bg-green-50 text-green-600 rounded-lg">
          <el-icon class="text-xl"><TrendCharts /></el-icon>
        </div>
        <h3 class="text-xl font-bold text-gray-800 font-smiley">近7日销售趋势</h3>
      </div>

      <!-- 增加 bg-gray-50 方便看容器范围 -->
      <div class="h-64 flex justify-between gap-2 sm:gap-4 px-2 bg-gray-50 rounded-lg pb-2">
        <div 
          v-for="(item, index) in trendData" 
          :key="index" 
          class="flex-1 flex flex-col items-center justify-end h-full group relative"
        >
          <!-- 直接显示数值，确保能看到 -->
          <div class="mb-1 text-xs font-bold text-indigo-600">
             {{ Number(item.value) > 0 ? Math.floor(Number(item.value)) : '' }}
          </div>
          
          <!-- 柱子：强制 min-h-10px，确保不消失 -->
          <div 
            class="w-full max-w-[40px] bg-indigo-500 rounded-t-lg transition-all duration-500 min-h-[10px]"
            :style="{ height: Number(item.value) > 0 ? `${(Number(item.value) / maxSales) * 100}%` : '10px' }"
          >
          </div>
          
          <div class="mt-2 text-xs text-gray-400 font-medium">{{ item.date }}</div>
        </div>
      </div>
    </div>

    <!-- 饼状图区域 -->
    <div class="bg-white rounded-3xl p-8 shadow-sm border border-gray-100">
      <div class="flex items-center gap-3 mb-8">
        <div class="p-2 bg-orange-50 text-orange-600 rounded-lg">
          <el-icon class="text-xl"><PieChart /></el-icon>
        </div>
        <h3 class="text-xl font-bold text-gray-800 font-smiley">今日热销分类占比</h3>
      </div>

      <div class="flex flex-col md:flex-row items-center justify-around gap-8">
        <!-- 饼图 (CSS Conic Gradient) -->
        <div class="relative w-48 h-48 rounded-full shadow-inner flex items-center justify-center" :style="pieStyle">
          <!-- 中间镂空形成甜甜圈效果 -->
          <div class="absolute w-32 h-32 bg-white rounded-full flex flex-col items-center justify-center shadow-sm">
             <span class="text-gray-400 text-xs">今日总销量</span>
             <span class="text-2xl font-bold text-gray-800">
               {{ categoryStats.reduce((sum, item) => sum + item.value, 0) }}
             </span>
          </div>
        </div>

        <!-- 图例 -->
        <div class="flex-1 grid grid-cols-1 sm:grid-cols-2 gap-4 w-full max-w-lg">
          <div 
            v-for="item in categoryStats" 
            :key="item.name"
            class="flex items-center justify-between p-3 rounded-xl bg-gray-50 border border-gray-100"
          >
            <div class="flex items-center gap-3">
              <div class="w-3 h-3 rounded-full shadow-sm" :style="{ backgroundColor: item.color }"></div>
              <span class="font-medium text-gray-700">{{ item.name }}</span>
            </div>
            <div class="flex items-center gap-3">
               <span class="text-sm font-bold text-gray-900">{{ item.value }} 份</span>
               <span class="text-xs text-gray-400 w-10 text-right">{{ item.percent }}%</span>
            </div>
          </div>
          
          <!-- 空状态提示 -->
          <div v-if="categoryStats.length === 0" class="col-span-full text-center text-gray-400 py-4">
            暂无今日销售数据
          </div>
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
