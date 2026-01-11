<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrders, updateOrderStatus } from "@/api/order";
import { Search, Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const orderSearch = ref('')
const orderPage = ref(1)
const orderPageSize = ref(10)
const orders = ref<any[]>([])
const detailsVisible = ref(false)
const currentOrder = ref<any>({})

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

const handleViewDetails = (row: any) => {
  currentOrder.value = row
  detailsVisible.value = true
}

const handleCompleteOrder = (row: any) => {
  ElMessageBox.confirm('确认将该订单标记为“已完成”吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateOrderStatus(row.id, '已完成')
      ElMessage.success('订单状态已更新')
      fetchOrders()
    } catch (error) {
      console.error(error)
    }
  })
}

const formatDate = (row: any, column: any, cellValue: string) => {
  if (!cellValue) return ''
  return new Date(cellValue).toLocaleString()
}

onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="p-6">
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
}
</style>
