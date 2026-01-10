<script setup lang="ts">
import { ref, onMounted } from 'vue'
// 引入 API，注意这里使用了 as 重命名，保持和你原代码习惯一致
import { getProductList as getProducts } from '@/api/product'
import { useRouter } from 'vue-router'

const recommendedProducts = ref<any[]>([])
const loading = ref(false)
const router = useRouter()

const fetchRecommended = async () => {
  loading.value = true
  try {
    const res: any = await getProducts()
    
    // 🔍 调试日志：按 F12 打开控制台，看看打印出来的数组里字段名是 isRecommend 还是 isRecommended
    console.log("首页获取到的所有菜品:", res)

    // 🛡️ 健壮的过滤逻辑：
    // 1. p.isRecommended ?? p.is_recommended ?? p.isRecommend -> 优先取新标准，取不到找旧标准
    // 2. status === true || status === 1 -> 兼容布尔值和数据库的 tinyint(1)
    recommendedProducts.value = res.filter((p: any) => {
      const status = p.isRecommended ?? p.is_recommended ?? p.isRecommend
      // 统一加上 isRecommended 属性，方便模板渲染
      p.isRecommended = (status === true || status === 1 || status === '1')
      return p.isRecommended
    })

    console.log("筛选出的推荐菜品:", recommendedProducts.value)

  } catch (error) {
    console.error("获取推荐菜品失败:", error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRecommended()
})
</script>

<template>
  <div class="home-container p-6 min-h-screen relative overflow-hidden bg-gray-50">
    <!-- 背景光效 -->
    <div class="flowing-light-blob blob-1"></div>
    <div class="flowing-light-blob blob-2"></div>

    <div class="relative z-10 max-w-7xl mx-auto space-y-8">
      <!-- 欢迎 Banner -->
      <div class="bg-gradient-to-r from-orange-500 to-rose-500 text-white p-12 rounded-3xl shadow-xl text-center relative overflow-hidden group">
        <div class="absolute inset-0 bg-white/10 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
        <div class="relative z-10">
          <h1 class="text-5xl font-bold mb-4 font-smiley tracking-wider drop-shadow-md">欢迎来到美味餐厅</h1>
          <p class="text-xl mb-8 opacity-90 font-light tracking-widest">尽享天下美食 · 体验舌尖上的诱惑</p>
          <el-button 
            class="bg-white text-orange-600 font-bold border-none hover:bg-gray-50 hover:scale-105 transition-transform shadow-lg px-8 py-6 text-lg rounded-full" 
            size="large" 
            round
            @click="router.push('/customer/menu')"
          >
            立即点餐
          </el-button>
        </div>
        <!-- 装饰圆圈 -->
        <div class="absolute -top-10 -left-10 w-40 h-40 bg-white/20 rounded-full blur-2xl"></div>
        <div class="absolute -bottom-10 -right-10 w-60 h-60 bg-white/20 rounded-full blur-3xl"></div>
      </div>

      <!-- 推荐标题 -->
      <div class="flex items-center gap-4 px-2">
        <div class="h-8 w-1.5 bg-gradient-to-b from-orange-500 to-rose-500 rounded-full"></div>
        <h2 class="text-3xl font-bold font-smiley text-gray-800">今日推荐</h2>
      </div>
      
      <!-- 无推荐时的占位 -->
      <div v-if="recommendedProducts.length === 0 && !loading" class="text-gray-500 text-center py-16 bg-white/60 backdrop-blur-sm rounded-3xl shadow-sm border border-white/50">
         <div class="text-lg mb-4">暂无特别推荐，请查看完整菜单</div>
         <el-button type="primary" round plain size="large" @click="router.push('/customer/menu')">去菜单看看</el-button>
      </div>

      <div v-loading="loading">
        <!-- 桌面端轮播图 -->
        <el-carousel 
          v-if="recommendedProducts.length > 0"
          :interval="4000" 
          type="card" 
          height="400px" 
          class="hidden md:block custom-carousel"
        >
          <el-carousel-item v-for="item in recommendedProducts" :key="item.id">
            <div class="relative h-full w-full cursor-pointer group rounded-2xl overflow-hidden shadow-lg border border-white/20" @click="router.push('/customer/menu')">
              <img :src="item.imageUrl" class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110" />
              
              <div class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/80 via-black/40 to-transparent p-6 text-white backdrop-blur-[2px] transition-all group-hover:from-black/90">
                <h3 class="text-2xl font-bold font-smiley mb-2">{{ item.name }}</h3>
                <p class="text-sm opacity-90 line-clamp-1 text-gray-200">{{ item.description || '店长推荐美味' }}</p>
              </div>
              <!-- 推荐角标 -->
              <div class="absolute top-4 right-4 bg-rose-600/90 backdrop-blur-md text-white text-sm font-bold px-4 py-1.5 rounded-full shadow-lg z-10 animate-pulse">
                今日推荐
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>

        <!-- 移动端列表 -->
        <div class="md:hidden grid grid-cols-1 gap-6">
          <div 
            v-for="item in recommendedProducts" 
            :key="item.id" 
            class="bg-white/80 backdrop-blur-md rounded-2xl shadow-lg overflow-hidden cursor-pointer active:scale-95 transition-transform relative border border-white/50" 
            @click="router.push('/customer/menu')"
          >
            <div class="absolute top-3 right-3 bg-rose-600/90 backdrop-blur-sm text-white text-xs font-bold px-3 py-1 rounded-full shadow-md z-10">
              今日推荐
            </div>
            <img :src="item.imageUrl" class="w-full h-56 object-cover" />
            <div class="p-5">
              <div class="flex justify-between items-center mb-2">
                <h3 class="font-bold text-xl text-gray-800 font-smiley">{{ item.name }}</h3>
                <div class="text-rose-500 font-bold text-lg font-mono">￥{{ item.price }}</div>
              </div>
              <p class="text-gray-500 text-sm line-clamp-2 leading-relaxed">{{ item.description || '暂无描述' }}</p>
            </div>
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

/* 轮播图圆角修正 */
:deep(.el-carousel__item) {
  border-radius: 16px;
}
</style>