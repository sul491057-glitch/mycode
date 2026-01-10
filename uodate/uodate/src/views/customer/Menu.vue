<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
// 👇 修改 1: 引入具体的 API 文件
import { getProductList } from '@/api/product'
import { submitOrder } from '@/api/order'

import { useCartStore } from '@/store/cart'
import { ShoppingCart, Plus, Minus, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const products = ref<any[]>([])
const loading = ref(false)
const activeCategory = ref('全部')
const cartStore = useCartStore()
const cartDrawerVisible = ref(false)
const router = useRouter()

const categories = computed(() => {
  const cats = new Set(products.value.map(p => p.category))
  return ['全部', ...Array.from(cats)]
})

const filteredProducts = computed(() => {
  if (activeCategory.value === '全部') {
    return products.value
  }
  return products.value.filter(p => p.category === activeCategory.value)
})

const fetchProducts = async () => {
  loading.value = true
  try {
    // 👇 修改 2: 调用 getProductList
    const res: any = await getProductList()
    
    // 🔍 调试：打印后端返回的数据结构
    console.log("Menu.vue 获取到的菜品列表:", res)

    // 🛡️ 数据标准化：兼容 is_recommended, isRecommend, 1/0, true/false
    products.value = res.map((p: any) => {
      // 优先取 isRecommended，其次 is_recommended，最后 isRecommend
      const rawStatus = p.isRecommended ?? p.is_recommended ?? p.isRecommend
      // 转换为布尔值：1 -> true, true -> true, 其他 -> false
      const isRecommended = rawStatus === 1 || rawStatus === true || rawStatus === '1'
      
      return {
        ...p,
        isRecommended // 统一为 boolean 类型的 isRecommended
      }
    })
  } catch (error) {
    console.error(error)
    ElMessage.error('获取菜单失败')
  } finally {
    loading.value = false
  }
}

const handleOrder = async () => {
  if (cartStore.items.length === 0) {
    return ElMessage.warning('购物车为空')
  }
  
  try {
    await ElMessageBox.confirm(`确认下单？总金额 ￥${cartStore.totalAmount}`, '提示', {
      confirmButtonText: '确认下单',
      cancelButtonText: '再看看',
      type: 'info'
    })
    
    // 👇 修改 3: 构造符合后端 OrderDTO 的数据结构
    const orderData = {
      tableId: 'A1', // 这里先写死桌号，实际项目中可从 URL 参数或扫码结果获取
      totalAmount: cartStore.totalAmount,
      items: cartStore.items.map((item: any) => ({
        // 确保这些字段名完全对应后端 OrderItemDTO 类
        id: item.id,       // 商品ID
        name: item.name,   // 商品名
        price: item.price, // 单价
        quantity: item.quantity // 数量
      }))
    }

    // 发送请求
    await submitOrder(orderData)
    
    ElMessage.success('下单成功！后厨正在准备中...')
    cartStore.clearCart()
    cartDrawerVisible.value = false
    // router.push('/customer/home') // 下单成功后通常不需要跳转，或者跳转到订单详情
  } catch (e) {
    // Cancelled or Error
    console.error(e)
  }
}

onMounted(() => {
  fetchProducts()
})
</script>

<template>
  <div class="menu-container h-full flex flex-col bg-gray-50 relative overflow-hidden">
    <!-- 背景光效 -->
    <div class="flowing-light-blob blob-1"></div>
    <div class="flowing-light-blob blob-2"></div>

    <!-- 顶部固定分类栏 -->
    <div class="relative z-10 bg-white/70 backdrop-blur-md shadow-sm border-b border-white/50 px-4 py-3 overflow-x-auto whitespace-nowrap custom-scrollbar">
      <el-radio-group v-model="activeCategory" size="large" class="category-tabs">
        <el-radio-button v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
      </el-radio-group>
    </div>

    <!-- 商品列表 -->
    <div class="relative z-10 flex-1 overflow-y-auto px-6 py-6 custom-scrollbar" v-loading="loading">
      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in filteredProducts" :key="item.id" class="mb-6">
          <el-card 
            :body-style="{ padding: '0px' }" 
            class="h-full flex flex-col rounded-2xl border-none shadow-lg hover:shadow-xl transition-all duration-300 hover:-translate-y-1 bg-white/80 backdrop-blur-sm overflow-hidden group"
          >
            <div class="relative h-56 overflow-hidden">
              <img :src="item.imageUrl" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110" />
              <div class="absolute inset-0 bg-black/10 group-hover:bg-black/0 transition-colors"></div>
              
              <div v-if="item.isRecommended" class="absolute top-3 right-3 bg-rose-500/90 backdrop-blur-sm text-white text-xs font-bold px-3 py-1 rounded-full shadow-lg z-10">
                今日推荐
              </div>
            </div>
            
            <div class="p-5 flex-1 flex flex-col justify-between">
              <div>
                <h3 class="text-xl font-bold truncate text-gray-800 font-smiley">{{ item.name }}</h3>
                <p class="text-gray-500 text-sm mt-2 line-clamp-2 leading-relaxed">{{ item.description }}</p>
              </div>
              <div class="mt-5 flex items-center justify-between">
                <span class="text-rose-500 font-bold text-xl font-mono">￥{{ item.price }}</span>
                <el-button 
                  type="primary" 
                  circle 
                  size="large"
                  class="bg-indigo-600 border-none hover:bg-indigo-700 shadow-md hover:shadow-indigo-500/30 transition-all"
                  :icon="Plus" 
                  @click="cartStore.addItem(item)" 
                />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 悬浮购物车按钮 -->
    <div class="fixed bottom-10 right-10 z-50">
      <el-badge :value="cartStore.totalCount" :hidden="cartStore.totalCount <= 0" class="cursor-pointer">
        <div class="bg-gradient-to-r from-orange-500 to-rose-500 text-white p-4 rounded-full shadow-2xl hover:scale-110 transition-transform duration-300 active:scale-95 border-2 border-white" @click="cartDrawerVisible = true">
          <el-icon size="28"><ShoppingCart /></el-icon>
        </div>
      </el-badge>
    </div>

    <!-- 购物车抽屉 -->
    <el-drawer 
      v-model="cartDrawerVisible" 
      title="购物车" 
      direction="rtl" 
      size="420px"
      class="custom-drawer"
    >
      <div class="flex flex-col h-full">
        <div class="flex-1 overflow-y-auto custom-scrollbar px-2">
          <div v-if="cartStore.items.length === 0" class="flex flex-col items-center justify-center h-64 text-gray-400">
            <el-icon size="48" class="mb-2 opacity-50"><ShoppingCart /></el-icon>
            <p>购物车空空如也，快去选购吧~</p>
          </div>
          <div v-else v-for="item in cartStore.items" :key="item.id" class="flex items-center gap-4 mb-4 p-3 bg-gray-50 rounded-xl hover:bg-gray-100 transition-colors">
            <img :src="item.imageUrl" class="w-16 h-16 object-cover rounded-lg shadow-sm" />
            <div class="flex-1 min-w-0">
              <div class="font-bold text-gray-800 truncate">{{ item.name }}</div>
              <div class="text-rose-500 font-mono font-bold mt-1">￥{{ item.price }}</div>
            </div>
            <div class="flex items-center gap-3 bg-white px-2 py-1 rounded-full shadow-sm">
              <el-button circle size="small" :icon="Minus" @click="cartStore.decreaseItem(item.id)" />
              <span class="font-medium w-4 text-center">{{ item.quantity }}</span>
              <el-button circle size="small" :icon="Plus" type="primary" plain @click="cartStore.addItem(item)" />
            </div>
          </div>
        </div>
        
        <div class="border-t border-gray-100 pt-6 mt-4 bg-white z-10">
          <div class="flex justify-between items-center mb-6 px-2">
            <span class="text-gray-500">合计金额</span>
            <span class="text-3xl font-bold text-rose-500 font-mono">￥{{ cartStore.totalAmount }}</span>
          </div>
          <div class="flex gap-4">
            <el-button class="flex-1 h-12 text-lg rounded-xl" :icon="Delete" @click="cartStore.clearCart" plain>清空</el-button>
            <el-button 
              type="primary" 
              class="flex-[2] h-12 text-lg rounded-xl bg-gradient-to-r from-orange-500 to-rose-500 border-none shadow-lg shadow-rose-500/30 hover:shadow-rose-500/50 transition-shadow" 
              @click="handleOrder" 
              :disabled="cartStore.items.length === 0"
            >
              立即下单
            </el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
/* 引入得意黑字体 */
@font-face {
  font-family: 'Smiley Sans';
  src: url('https://npm.elemecdn.com/font-smiley-sans/SmileySans-Oblique.ttf.woff2') format('woff2');
  font-display: swap;
}

.font-smiley {
  font-family: 'Smiley Sans', 'Segoe UI', sans-serif;
}

/* 全局背景流动光效 (复用登录页逻辑) */
.flowing-light-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
  opacity: 0.6;
}

.blob-1 {
  width: 800px;
  height: 800px;
  background: radial-gradient(circle, rgba(167, 139, 250, 0.2) 0%, rgba(244, 114, 182, 0.1) 60%, transparent 100%);
  top: -20%;
  left: -10%;
  animation: float 15s infinite ease-in-out;
}

.blob-2 {
  width: 700px;
  height: 700px;
  background: radial-gradient(circle, rgba(96, 165, 250, 0.2) 0%, rgba(192, 132, 252, 0.1) 60%, transparent 100%);
  bottom: -20%;
  right: -10%;
  animation: float-reverse 18s infinite ease-in-out;
}

@keyframes float {
  0% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(30px, 30px) rotate(5deg); }
  100% { transform: translate(0, 0) rotate(0deg); }
}

@keyframes float-reverse {
  0% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(-30px, -30px) rotate(-5deg); }
  100% { transform: translate(0, 0) rotate(0deg); }
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 自定义滚动条 */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
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

/* 抽屉样式覆盖 */
:deep(.custom-drawer) {
  border-radius: 24px 0 0 24px !important;
}
:deep(.custom-drawer .el-drawer__header) {
  margin-bottom: 20px;
  padding: 24px;
  font-family: 'Smiley Sans', sans-serif;
  font-weight: bold;
  font-size: 1.25rem;
}
:deep(.custom-drawer .el-drawer__body) {
  padding: 0 24px 24px 24px;
}

/* 分类 Tab 样式 */
:deep(.category-tabs .el-radio-button__inner) {
  border: none;
  background: transparent;
  border-radius: 20px;
  padding: 8px 20px;
  margin-right: 8px;
  color: #6b7280;
  transition: all 0.3s;
  box-shadow: none !important;
}
:deep(.category-tabs .el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #4f46e5;
  color: white;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3) !important;
}
:deep(.category-tabs .el-radio-button:first-child .el-radio-button__inner) {
  border-left: none;
}
</style>