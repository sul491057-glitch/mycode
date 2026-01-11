<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 1. 引入 API
import { getOrders, updateOrderStatus } from "@/api/order";
import { getReservations, updateReservationStatus } from "@/api/reservation";
import { getProductList as getProducts, toggleRecommend, addProduct, updateProduct, deleteProduct, uploadImage } from "@/api/product";
import { Plus, Delete, Download, ZoomIn, Search } from '@element-plus/icons-vue' // 引入图标

// 2. 状态定义
const activeTab = ref('orders')
const loading = ref(false)

// 搜索和分页状态
const orderSearch = ref('')
const orderPage = ref(1)
const orderPageSize = ref(10)

const reservationSearch = ref('')
const reservationPage = ref(1)
const reservationPageSize = ref(10)

const productSearch = ref('')
const productPage = ref(1)
const productPageSize = ref(10)

// 图片预览相关
const dialogImageUrl = ref('')
const previewVisible = ref(false)
const uploadDisabled = ref(false)

// 商品编辑/新增相关
const productDialogVisible = ref(false)
const isEditMode = ref(false)
const productForm = ref({
  id: '',
  name: '',
  description: '',
  category: '',
  price: 0,
  imageUrl: '',
  isRecommended: false
})

// 数据列表
const orders = ref<any[]>([])
const products = ref<any[]>([])
const reservations = ref<any[]>([])

// 订单详情弹窗相关
const detailsVisible = ref(false)
const currentOrder = ref<any>({})

// 3. 数据获取方法
// 计算属性：过滤和分页
const filteredOrders = computed(() => {
  if (!orderSearch.value) return orders.value
  const query = orderSearch.value.toLowerCase()
  return orders.value.filter(item => 
    String(item.id).toLowerCase().includes(query) || 
    String(item.totalAmount).includes(query)
  )
})

const paginatedOrders = computed(() => {
  const start = (orderPage.value - 1) * orderPageSize.value
  const end = start + orderPageSize.value
  return filteredOrders.value.slice(start, end)
})

const filteredReservations = computed(() => {
  if (!reservationSearch.value) return reservations.value
  const query = reservationSearch.value.toLowerCase()
  return reservations.value.filter(item => 
    String(item.name).toLowerCase().includes(query) || 
    String(item.phone).includes(query)
  )
})

const paginatedReservations = computed(() => {
  const start = (reservationPage.value - 1) * reservationPageSize.value
  const end = start + reservationPageSize.value
  return filteredReservations.value.slice(start, end)
})

const filteredProducts = computed(() => {
  if (!productSearch.value) return products.value
  const query = productSearch.value.toLowerCase()
  return products.value.filter(item => 
    String(item.name).toLowerCase().includes(query) || 
    String(item.category).toLowerCase().includes(query)
  )
})

const paginatedProducts = computed(() => {
  const start = (productPage.value - 1) * productPageSize.value
  const end = start + productPageSize.value
  return filteredProducts.value.slice(start, end)
})

const fetchOrders = async () => {
  loading.value = true
  try {
    const res: any = await getOrders()
    orders.value = res
  } catch (error) {
    ElMessage.error('获取订单失败')
  } finally {
    loading.value = false
  }
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const res: any = await getProducts()
    
    // 🛡️ 数据标准化：兼容后端字段名和类型
    products.value = res.map((p: any) => {
      const rawStatus = p.isRecommended ?? p.is_recommended ?? p.isRecommend
      // 转为布尔值供 Switch 使用
      const isRecommended = rawStatus === 1 || rawStatus === true || rawStatus === '1'
      
      return {
        ...p,
        isRecommended
      }
    })
  } catch (error) {
    ElMessage.error('获取商品失败')
  } finally {
    loading.value = false
  }
}

const fetchReservations = async () => {
  loading.value = true
  try {
    const res: any = await getReservations()
    reservations.value = res
  } catch (error) {
    ElMessage.error('获取预定信息失败')
  } finally {
    loading.value = false
  }
}

// 4. 业务操作方法

// 商品推荐切换
const handleRecommendChange = async (row: any) => {
  try {
    // 🔴 关键修正：这里统一使用 isRecommended (带 ed)
    // 后端如果需要 1/0，这里做个转换
    // 假设后端接收 { id, isRecommended: 1 }
    await toggleRecommend({ 
      id: row.id, 
      isRecommended: row.isRecommended ? 1 : 0 
    })
    ElMessage.success(row.isRecommended ? '已设为推荐' : '已取消推荐')
  } catch (error) {
    row.isRecommended = !row.isRecommended // 失败回滚
    ElMessage.error('操作失败')
  }
}

// 删除商品
const handleDeleteProduct = (row: any) => {
  ElMessageBox.confirm('确认删除该菜品吗？此操作不可恢复', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteProduct(row.id)
      ElMessage.success('删除成功')
      fetchProducts()
    } catch (error) {
      console.error(error)
      // ElMessage.error('删除失败') // request.ts 可能已提示
    }
  })
}

// 打开新增窗口
const handleAddProduct = () => {
  isEditMode.value = false
  productForm.value = {
    id: '',
    name: '',
    description: '',
    category: '',
    price: 0,
    imageUrl: '',
    isRecommended: false
  }
  productDialogVisible.value = true
}

// 打开编辑窗口
const handleEditProduct = (row: any) => {
  isEditMode.value = true
  // 复制一份数据，避免直接修改表格
  productForm.value = { ...row }
  productDialogVisible.value = true
}

// 自定义上传逻辑
const handleUpload = async (options: any) => {
  const { file, onSuccess, onError } = options
  const formData = new FormData()
  formData.append('file', file)

  try {
    const res: any = await uploadImage(formData)
    // 兼容多种后端返回格式：
    // 1. 直接返回字符串: "/images/xxx.png"
    // 2. 返回对象: { url: "/images/xxx.png" } 或 { data: "/images/xxx.png" }
    let url = ''
    if (typeof res === 'string') {
      url = res
    } else if (typeof res === 'object') {
      url = res.url || res.data || res.message // 尝试从常见字段获取
    }

    if (!url || !url.startsWith('/')) {
       // 如果获取不到有效路径，打印完整响应以便调试
       console.error('上传响应异常:', res)
       throw new Error('无法解析上传返回的图片路径')
    }

    productForm.value.imageUrl = url
    ElMessage.success('上传成功')
    onSuccess(res)
  } catch (err) {
    console.error('上传失败:', err)
    // ElMessage.error('上传失败') // request.ts 可能已拦截提示
    onError(err)
  }
}

// 预览图片
const handlePictureCardPreview = () => {
  if (!productForm.value.imageUrl) return
  dialogImageUrl.value = productForm.value.imageUrl
  previewVisible.value = true
}

// 删除图片
const handleRemove = () => {
  productForm.value.imageUrl = ''
}

// 下载图片 (简单实现：打开新窗口)
const handleDownload = () => {
  if (!productForm.value.imageUrl) return
  window.open(productForm.value.imageUrl, '_blank')
}

// 提交商品表单
const submitProductForm = async () => {
  try {
    // 简单校验
    if (!productForm.value.name || !productForm.value.price) {
      ElMessage.warning('请填写完整的菜品信息')
      return
    }

    // 处理 isRecommended 转为后端可能需要的格式（如果后端需要数字）
    // 这里假设后端 update/add 接口也能处理 boolean 或自动转换
    const payload = {
      ...productForm.value,
      isRecommended: productForm.value.isRecommended ? 1 : 0
    }

    if (isEditMode.value) {
       await updateProduct(payload)
       ElMessage.success('更新成功')
    } else {
       await addProduct(payload)
       ElMessage.success('添加成功')
    }
    productDialogVisible.value = false
    fetchProducts()
  } catch (error) {
    console.error(error)
    // ElMessage.error('操作失败') // request.ts 可能已有错误提示
  }
}

// 查看订单详情
const handleViewDetails = (row: any) => {
  currentOrder.value = row
  detailsVisible.value = true
}

// 完成订单
const handleCompleteOrder = (row: any) => {
  ElMessageBox.confirm('确认将该订单标记为“已完成”吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateOrderStatus(row.id, '已完成')
      ElMessage.success('订单状态已更新')
      fetchOrders() // 刷新列表
    } catch (error) {
      console.error(error)
    }
  })
}

// 完成预订
const handleCompleteReservation = (row: any) => {
  ElMessageBox.confirm(`确认顾客 ${row.name} 的预订已完成（餐桌 ${row.tableId} 将被释放）？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await updateReservationStatus(row.id, '已完成')
      ElMessage.success('预订已完成，餐桌已释放')
      fetchReservations() // 刷新列表
    } catch (error) {
      console.error(error)
    }
  })
}

// Tab 切换刷新逻辑
const refreshData = () => {
  if (activeTab.value === 'orders') {
    fetchOrders()
  } else if (activeTab.value === 'products') {
    fetchProducts()
  } else if (activeTab.value === 'reservations') {
    fetchReservations()
  }
}

// 格式化日期
const formatDate = (row: any, column: any, cellValue: string) => {
  if (!cellValue) return ''
  return new Date(cellValue).toLocaleString()
}

onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="dashboard-container min-h-screen bg-gray-50 relative overflow-hidden">
    <!-- 全局背景光效 -->
    <div class="flowing-light-blob blob-1"></div>
    <div class="flowing-light-blob blob-2"></div>

    <div class="relative z-10 w-full h-full">
      <div class="bg-white/80 backdrop-blur-md shadow-xl overflow-hidden border-l border-white/50 min-h-screen rounded-tl-3xl">
        <div class="p-6 border-b border-gray-100/50 bg-white/50">
          <h2 class="text-2xl font-bold text-gray-800 font-smiley">管理控制台</h2>
        </div>
        
        <div class="p-6">
          <el-tabs v-model="activeTab" @tab-change="refreshData" class="custom-tabs">
            
            <el-tab-pane label="订单管理" name="orders">
              <div class="mb-6 flex justify-between items-center bg-white/60 p-4 rounded-xl shadow-sm">
                <div class="flex items-center gap-4">
                  <h3 class="text-lg font-bold text-gray-700 font-smiley">顾客订单列表</h3>
                  <el-input
                    v-model="orderSearch"
                    placeholder="搜索订单号/金额"
                    :prefix-icon="Search"
                    clearable
                    class="w-64 custom-search"
                  />
                </div>
                <el-button type="primary" plain round :icon="Plus" @click="fetchOrders">刷新列表</el-button>
              </div>
              
              <el-table :data="paginatedOrders" style="width: 100%" v-loading="loading" class="custom-table rounded-xl overflow-hidden shadow-sm">
                <el-table-column prop="id" label="订单号/ID" width="180" show-overflow-tooltip />
                <el-table-column prop="createTime" label="下单时间" width="180" :formatter="formatDate" />
                <el-table-column prop="totalAmount" label="总金额" width="120">
                  <template #default="scope">
                    <span class="text-rose-500 font-bold font-mono">￥{{ scope.row.totalAmount }}</span>
                  </template>
                </el-table-column>
                
                <el-table-column label="详情" align="center" width="120">
                  <template #default="scope">
                     <el-button type="primary" link @click="handleViewDetails(scope.row)">查看详情</el-button>
                  </template>
                </el-table-column>
                
                <el-table-column label="状态/操作" min-width="200">
                  <template #default="scope">
                    <div class="flex items-center gap-3">
                      <el-tag :type="scope.row.status === '已完成' ? 'success' : 'warning'" effect="light" round>
                        {{ scope.row.status === 'pending' ? '待处理' : (scope.row.status || '待处理') }}
                      </el-tag>
                      <el-button 
                        v-if="scope.row.status !== '已完成'"
                        type="success" 
                        size="small" 
                        round
                        plain
                        @click="handleCompleteOrder(scope.row)"
                      >
                        点击完成
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>

              <div class="mt-4 flex justify-end">
                <el-pagination
                  v-model:current-page="orderPage"
                  v-model:page-size="orderPageSize"
                  :total="filteredOrders.length"
                  :page-sizes="[10, 20, 50]"
                  layout="total, sizes, prev, pager, next, jumper"
                  background
                  class="custom-pagination"
                />
              </div>
            </el-tab-pane>

            <el-tab-pane label="预定管理" name="reservations">
              <div class="mb-6 flex justify-between items-center bg-white/60 p-4 rounded-xl shadow-sm">
                <div class="flex items-center gap-4">
                  <h3 class="text-lg font-bold text-gray-700 font-smiley">顾客预定列表</h3>
                  <el-input
                    v-model="reservationSearch"
                    placeholder="搜索姓名/电话"
                    :prefix-icon="Search"
                    clearable
                    class="w-64 custom-search"
                  />
                </div>
                <el-button type="primary" plain round :icon="Plus" @click="fetchReservations">刷新列表</el-button>
              </div>

              <el-table :data="paginatedReservations" style="width: 100%" v-loading="loading" class="custom-table rounded-xl overflow-hidden shadow-sm">
                <el-table-column prop="name" label="顾客姓名" width="120" />
                <el-table-column prop="phone" label="联系电话" width="150" />
                <el-table-column prop="tableId" label="餐桌号" width="100" align="center">
                   <template #default="scope">
                      <el-tag effect="dark" type="info" round>{{ scope.row.tableId || '未选' }}</el-tag>
                   </template>
                </el-table-column>
                <el-table-column prop="reserveTime" label="预定时间" width="180">
                   <template #default="scope">
                      {{ scope.row.reserveTime ? scope.row.reserveTime.replace('T', ' ') : '' }}
                   </template>
                </el-table-column>
                <el-table-column prop="peopleCount" label="人数" width="80" align="center" />
                <el-table-column prop="note" label="备注信息" show-overflow-tooltip />
                <el-table-column label="状态/操作" min-width="180">
                  <template #default="scope">
                    <div class="flex items-center gap-2">
                      <el-tag :type="scope.row.status === '已完成' ? 'success' : 'warning'" effect="light" round>
                        {{ scope.row.status === '已完成' ? '已完成' : '待处理/用餐中' }}
                      </el-tag>
                      <el-button 
                        v-if="scope.row.status !== '已完成'"
                        type="success" 
                        size="small" 
                        round
                        plain
                        @click="handleCompleteReservation(scope.row)"
                      >
                        点击完成
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>

              <div class="mt-4 flex justify-end">
                <el-pagination
                  v-model:current-page="reservationPage"
                  v-model:page-size="reservationPageSize"
                  :total="filteredReservations.length"
                  :page-sizes="[10, 20, 50]"
                  layout="total, sizes, prev, pager, next, jumper"
                  background
                  class="custom-pagination"
                />
              </div>
            </el-tab-pane>

            <el-tab-pane label="菜品及推荐管理" name="products">
              <div class="mb-6 flex justify-between items-center bg-white/60 p-4 rounded-xl shadow-sm">
                <div class="flex items-center gap-4">
                  <h3 class="text-lg font-bold text-gray-700 font-smiley">商品列表与推荐设置</h3>
                  <el-input
                    v-model="productSearch"
                    placeholder="搜索菜名/分类"
                    :prefix-icon="Search"
                    clearable
                    class="w-64 custom-search"
                  />
                </div>
                <div class="flex gap-3">
                  <el-button type="primary" round :icon="Plus" @click="handleAddProduct" class="shadow-md hover:shadow-lg transition-all">新增菜品</el-button>
                  <el-button round plain @click="fetchProducts">刷新</el-button>
                </div>
              </div>

              <el-table :data="paginatedProducts" style="width: 100%" v-loading="loading" class="custom-table rounded-xl overflow-hidden shadow-sm">
                <el-table-column label="图片" width="100">
                  <template #default="scope">
                    <div class="w-16 h-16 rounded-lg overflow-hidden shadow-sm border border-gray-100">
                      <img 
                        v-if="scope.row.imageUrl" 
                        :src="scope.row.imageUrl" 
                        class="w-full h-full object-cover hover:scale-110 transition-transform duration-300" 
                      />
                      <div v-else class="w-full h-full bg-gray-100 flex items-center justify-center text-gray-400 text-xs">无图片</div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="name" label="菜名" width="150" class-name="font-medium" />
                <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
                <el-table-column prop="category" label="分类" width="100">
                   <template #default="scope">
                     <el-tag size="small" effect="plain" round>{{ scope.row.category }}</el-tag>
                   </template>
                </el-table-column>
                <el-table-column prop="price" label="价格" width="100">
                  <template #default="scope"><span class="font-mono text-rose-500 font-bold">￥{{ scope.row.price }}</span></template>
                </el-table-column>
                
                <el-table-column label="操作" width="150" align="center">
                  <template #default="scope">
                    <el-button size="small" type="primary" link @click="handleEditProduct(scope.row)">
                      编辑
                    </el-button>
                    <el-button size="small" type="danger" link @click="handleDeleteProduct(scope.row)">
                      删除
                    </el-button>
                  </template>
                </el-table-column>

                <el-table-column label="推荐状态" width="150">
                  <template #default="scope">
                    <el-switch
                      v-model="scope.row.isRecommended"
                      active-text="推荐"
                      inactive-text="普通"
                      inline-prompt
                      style="--el-switch-on-color: #f43f5e; --el-switch-off-color: #94a3b8"
                      @change="handleRecommendChange(scope.row)"
                    />
                  </template>
                </el-table-column>
              </el-table>

              <div class="mt-4 flex justify-end">
                <el-pagination
                  v-model:current-page="productPage"
                  v-model:page-size="productPageSize"
                  :total="filteredProducts.length"
                  :page-sizes="[10, 20, 50]"
                  layout="total, sizes, prev, pager, next, jumper"
                  background
                  class="custom-pagination"
                />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>

    <!-- 弹窗样式美化 -->
    <el-dialog v-model="detailsVisible" title="订单详情" width="500px" class="custom-dialog" align-center>
      <div v-if="currentOrder.id" class="space-y-4">
        <div class="p-4 bg-indigo-50/50 rounded-xl border border-indigo-100">
           <div class="grid grid-cols-1 gap-2 text-sm">
             <div class="flex justify-between"><span class="text-gray-500">订单号</span> <span class="font-mono font-medium">{{ currentOrder.id }}</span></div>
             <div class="flex justify-between"><span class="text-gray-500">下单时间</span> <span>{{ new Date(currentOrder.createTime).toLocaleString() }}</span></div>
             <div class="flex justify-between items-center pt-2 border-t border-indigo-100 mt-1">
               <span class="text-gray-900 font-bold">总金额</span> 
               <span class="text-rose-500 font-bold text-lg font-mono">￥{{ currentOrder.totalAmount }}</span>
             </div>
           </div>
        </div>
        
        <el-table :data="currentOrder.orderItems || []" border stripe size="small" class="rounded-lg overflow-hidden">
            <el-table-column prop="productName" label="菜品名称" />
            <el-table-column prop="price" label="单价" width="80" />
            <el-table-column prop="quantity" label="数量" width="80" align="center" />
            <el-table-column label="小计" align="right">
               <template #default="scope">
                 <span class="font-mono text-rose-500">￥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}</span>
               </template>
            </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="detailsVisible = false" round>关闭</el-button>
      </template>
    </el-dialog>

    <!-- 商品编辑/新增弹窗 -->
    <el-dialog v-model="productDialogVisible" :title="isEditMode ? '编辑菜品' : '新增菜品'" width="550px" class="custom-dialog" align-center>
      <el-form :model="productForm" label-width="80px" class="px-2">
        <el-form-item label="菜名">
          <el-input v-model="productForm.name" placeholder="请输入菜品名称" class="custom-input" />
        </el-form-item>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="分类">
            <el-input v-model="productForm.category" placeholder="例如：热菜" class="custom-input" />
          </el-form-item>
          <el-form-item label="价格">
            <el-input-number v-model="productForm.price" :min="0" :precision="2" controls-position="right" class="w-full" />
          </el-form-item>
        </div>
        
        <el-form-item label="菜品图片">
          <div class="border-2 border-dashed border-gray-200 rounded-xl p-4 w-full bg-gray-50/50 hover:bg-white transition-colors">
            <el-upload
              action="#" 
              list-type="picture-card" 
              :auto-upload="true"
              :http-request="handleUpload"
              :show-file-list="false"
              class="custom-uploader"
            >
               <div v-if="productForm.imageUrl" class="w-full h-full relative group rounded-lg overflow-hidden">
                  <img class="w-full h-full object-cover" :src="productForm.imageUrl" alt="" />
                  <div class="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all duration-300 gap-4 backdrop-blur-sm">
                    <el-icon class="text-white text-xl cursor-pointer hover:scale-110 transition-transform" @click.stop="handlePictureCardPreview()"><ZoomIn /></el-icon>
                    <el-icon v-if="!uploadDisabled" class="text-white text-xl cursor-pointer hover:scale-110 transition-transform" @click.stop="handleDownload()"><Download /></el-icon>
                    <el-icon v-if="!uploadDisabled" class="text-white text-xl cursor-pointer hover:scale-110 transition-transform hover:text-rose-400" @click.stop="handleRemove()"><Delete /></el-icon>
                  </div>
               </div>
               
               <div v-else class="flex flex-col items-center justify-center h-full text-gray-400">
                 <el-icon class="text-2xl mb-2"><Plus /></el-icon>
                 <span class="text-xs">点击上传图片</span>
               </div>
            </el-upload>
            <div class="text-xs text-gray-400 mt-2 text-center">支持 jpg/png 格式，建议尺寸 1:1</div>
          </div>
        </el-form-item>

        <el-form-item label="描述">
          <el-input v-model="productForm.description" type="textarea" :rows="3" placeholder="请输入菜品描述" class="custom-input" />
        </el-form-item>

        <el-form-item label="推荐状态">
          <el-switch 
            v-model="productForm.isRecommended" 
            active-text="设为推荐" 
            inactive-text="普通菜品" 
            inline-prompt
            style="--el-switch-on-color: #f43f5e; --el-switch-off-color: #94a3b8"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="productDialogVisible = false" round>取消</el-button>
          <el-button type="primary" @click="submitProductForm" round class="bg-indigo-600 border-indigo-600 hover:bg-indigo-700">确定保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" class="bg-transparent shadow-none" align-center width="auto">
      <img :src="dialogImageUrl" alt="Preview" class="max-h-[80vh] rounded-lg shadow-2xl" />
    </el-dialog>
  </div>
</template>

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

/* 自定义表格样式 */
:deep(.custom-table) {
  --el-table-border-color: #f3f4f6;
  --el-table-header-bg-color: #f9fafb;
  --el-table-row-hover-bg-color: #f5f3ff;
}

:deep(.custom-table th.el-table__cell) {
  background-color: #f9fafb;
  font-weight: 600;
  color: #4b5563;
  height: 50px;
}

/* 弹窗圆角美化 */
:deep(.custom-dialog) {
  border-radius: 20px;
  overflow: hidden;
}

:deep(.custom-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 20px 24px;
  border-bottom: 1px solid #f3f4f6;
}

:deep(.custom-dialog .el-dialog__title) {
  font-weight: bold;
  font-family: 'Smiley Sans', sans-serif;
  font-size: 1.25rem;
}

/* 输入框美化 */
:deep(.custom-input .el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e5e7eb inset;
  padding: 8px 12px;
}

:deep(.custom-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #6366f1 inset;
}

/* 搜索框美化 */
:deep(.custom-search .el-input__wrapper) {
  border-radius: 20px;
  background-color: #f3f4f6;
  box-shadow: none;
  border: 1px solid transparent;
  transition: all 0.3s;
}

:deep(.custom-search .el-input__wrapper:hover),
:deep(.custom-search .el-input__wrapper.is-focus) {
  background-color: #fff;
  border-color: #e5e7eb;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

/* 分页美化 */
:deep(.custom-pagination .el-pager li) {
  border-radius: 8px !important;
  background-color: #fff !important;
  border: 1px solid #e5e7eb;
  font-weight: 500;
}

:deep(.custom-pagination .el-pager li:not(.is-disabled).is-active) {
  background-color: #4f46e5 !important;
  color: white !important;
  border-color: #4f46e5 !important;
  box-shadow: 0 2px 6px rgba(79, 70, 229, 0.3);
}

:deep(.custom-pagination .btn-prev),
:deep(.custom-pagination .btn-next) {
  border-radius: 8px !important;
  background-color: #fff !important;
  border: 1px solid #e5e7eb;
}

/* 上传组件样式调整 */
:deep(.custom-uploader .el-upload--picture-card) {
  width: 120px;
  height: 120px;
  border: 1px dashed #d1d5db;
  border-radius: 8px;
  background-color: transparent;
}

:deep(.custom-uploader .el-upload--picture-card:hover) {
  border-color: #6366f1;
  color: #6366f1;
}

/* 隐藏滚动条但保留功能 */
.dashboard-container {
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}
</style>