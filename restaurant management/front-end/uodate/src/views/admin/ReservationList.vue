<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReservations, updateReservationStatus } from "@/api/reservation";
import { Search, Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const reservationSearch = ref('')
const reservationPage = ref(1)
const reservationPageSize = ref(10)
const reservations = ref<any[]>([])

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

const handleCompleteReservation = (row: any) => {
  ElMessageBox.confirm(`确认顾客 ${row.name} 的预订已完成（餐桌 ${row.tableId} 将被释放）？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await updateReservationStatus(row.id, '已完成')
      ElMessage.success('预订已完成，餐桌已释放')
      fetchReservations()
    } catch (error) {
      console.error(error)
    }
  })
}

onMounted(() => {
  fetchReservations()
})
</script>

<template>
  <div class="p-6">
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
  </div>
</template>

<style scoped>
/* 搜索框圆角美化 */
:deep(.custom-search .el-input__wrapper) {
  border-radius: 9999px;
}

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
</style>
