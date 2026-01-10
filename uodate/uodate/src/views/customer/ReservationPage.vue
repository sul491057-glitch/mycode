<script setup lang="ts">
import { reactive, ref, onMounted, computed, watch } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { useRouter } from 'vue-router'
// 引用路径指向 api 目录下的 ts 文件
import { createReservation, getReservations } from '@/api/reservation' 

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const reservations = ref<any[]>([])

// 生成餐桌列表 A01 - A16
const allTables = Array.from({ length: 16 }, (_, i) => `A${String(i + 1).padStart(2, '0')}`)

// 辅助函数：将 HH:mm 格式的时间转换为分钟数
const timeToMinutes = (timeStr: string) => {
  if (!timeStr) return 0
  const [h, m] = timeStr.split(':').map(Number)
  return h * 60 + m
}

// 计算已被占用的餐桌
const occupiedTables = computed(() => {
  // 如果未选择日期或时间，暂时不计算冲突（或可视情况提示）
  // 这里逻辑是：只有当用户选定了日期和时间，才能准确判断哪些桌子在那个时间段不可用
  if (!form.date || !form.time) return []

  const DURATION_MINUTES = 4 * 60 // 4小时用餐时间
  const selectedTimeVal = timeToMinutes(form.time)
  const selectedDate = form.date

  return reservations.value
    .filter((r: any) => {
      // 1. 如果状态是已完成或取消，则不占用
      if (r.status === 'completed' || r.status === 'cancelled' || r.status === '已完成') return false
      
      // 2. 解析预定时间 (假设格式为 ISO 字符串或包含 "YYYY-MM-DDTHH:mm")
      if (!r.reserveTime) return false
      
      // 提取日期和时间
      // 兼容后端可能返回的格式，通常是 "2023-01-01T12:00:00"
      const [rDatePart, rTimePart] = r.reserveTime.split('T')
      if (!rDatePart || !rTimePart) return false

      // 3. 检查日期是否相同
      if (rDatePart !== selectedDate) return false

      // 4. 检查时间是否重叠
      // 现有预定的时间段: [rStart, rEnd]
      const rStart = timeToMinutes(rTimePart.substring(0, 5))
      const rEnd = rStart + DURATION_MINUTES

      // 用户想要预定的时间段: [userStart, userEnd]
      const userStart = selectedTimeVal
      const userEnd = userStart + DURATION_MINUTES

      // 判断两个时间段是否有交集
      // 交集条件: (StartA < EndB) && (EndA > StartB)
      return userStart < rEnd && userEnd > rStart
    })
    .map((r: any) => r.tableId)
})

const form = reactive({
  name: '',
  phone: '',
  date: '',
  time: '',
  peopleCount: 2,
  tableId: '', // 新增餐桌选择
  note: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  date: [{ required: true, message: '请选择日期', trigger: 'change' }],
  time: [{ required: true, message: '请选择时间', trigger: 'change' }],
  peopleCount: [{ required: true, message: '请输入人数', trigger: 'change' }],
  tableId: [{ required: true, message: '请选择餐桌', trigger: 'change' }]
}

// 获取当前的预订信息以判断哪些桌子被占用
// 注意：实际生产中应该根据选择的日期和时间动态查询，这里简化为获取所有未完成预订
const fetchReservations = async () => {
  try {
    const res: any = await getReservations()
    reservations.value = res
  } catch (error) {
    console.error("无法获取预订状态", error)
  }
}

const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const reservationDTO = {
          name: form.name,
          phone: form.phone,
          peopleCount: form.peopleCount,
          tableId: form.tableId, // 提交餐桌号
          note: form.note,
          reserveTime: `${form.date}T${form.time}:00` 
        }

        await createReservation(reservationDTO)
        
        ElMessage.success('预订申请提交成功！')
        formEl.resetFields()
        router.push('/customer/home')
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchReservations()
})
</script>

<template>
  <div class="reservation-container min-h-screen p-6 flex justify-center items-center bg-gray-50 relative overflow-hidden">
    <!-- 背景光效 -->
    <div class="flowing-light-blob blob-1"></div>
    <div class="flowing-light-blob blob-2"></div>

    <div class="relative z-10 w-full max-w-2xl">
      <el-card class="backdrop-blur-md bg-white/80 rounded-3xl shadow-2xl border-white/50 overflow-visible">
        <template #header>
          <div class="text-center py-2">
            <h2 class="text-3xl font-bold font-smiley text-gray-800 tracking-wide">预定餐桌</h2>
            <p class="text-gray-500 text-sm mt-2 font-light">请填写您的预定信息，我们将为您保留最佳位置</p>
          </div>
        </template>

        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="left" class="px-4 py-2">
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <el-form-item label="姓名" prop="name">
              <el-input id="name" v-model="form.name" placeholder="您的称呼" class="custom-input" />
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input id="phone" v-model="form.phone" placeholder="联系电话" class="custom-input" />
            </el-form-item>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <el-form-item label="用餐日期" prop="date">
              <el-date-picker 
                id="date"
                v-model="form.date" 
                type="date" 
                value-format="YYYY-MM-DD" 
                placeholder="选择日期" 
                style="width: 100%"
                class="custom-date-picker"
              />
            </el-form-item>

            <el-form-item label="用餐时间" prop="time">
              <el-time-select
                id="time"
                v-model="form.time"
                start="11:00"
                step="00:30"
                end="23:00"
                placeholder="选择时间"
                style="width: 100%"
                class="custom-time-picker"
              />
            </el-form-item>
          </div>

          <el-form-item label="用餐人数" prop="peopleCount">
            <el-input-number id="peopleCount" v-model="form.peopleCount" :min="1" :max="20" class="w-full" />
          </el-form-item>

          <!-- 自定义布局解决 RadioGroup 的 Label 指向问题 -->
          <div class="flex items-start mb-6">
            <div class="w-[100px] pt-[8px] text-gray-600 font-bold text-sm relative">
               <span class="text-red-500 absolute -left-2 top-2">*</span>
               选择餐桌
            </div>
            <div class="flex-1 min-w-0">
              <el-form-item prop="tableId" label-width="0" class="!mb-0">
                <div class="bg-gray-50/50 rounded-xl p-4 w-full border border-gray-100">
                  <el-radio-group id="tableId" v-model="form.tableId" class="grid grid-cols-4 gap-3 w-full custom-radio-group">
                    <el-radio-button 
                      v-for="table in allTables" 
                      :key="table" 
                      :label="table"
                      :value="table"
                      :disabled="occupiedTables.includes(table)"
                      class="w-full"
                    />
                  </el-radio-group>
                  <div class="flex items-center gap-4 mt-3 text-xs text-gray-400 justify-end px-2">
                    <div class="flex items-center gap-1"><div class="w-3 h-3 bg-white border border-gray-300 rounded-sm"></div> 可选</div>
                    <div class="flex items-center gap-1"><div class="w-3 h-3 bg-indigo-600 rounded-sm"></div> 已选</div>
                    <div class="flex items-center gap-1"><div class="w-3 h-3 bg-gray-100 rounded-sm"></div> 已预订</div>
                  </div>
                </div>
              </el-form-item>
            </div>
          </div>

          <el-form-item label="备注需求" prop="note">
            <el-input 
              id="note"
              v-model="form.note" 
              type="textarea" 
              :rows="3" 
              placeholder="例如：靠窗、宝宝椅等..." 
              class="custom-input"
            />
          </el-form-item>

          <div class="pt-4">
            <el-button 
              type="primary" 
              @click="submitForm(formRef)" 
              :loading="loading" 
              class="w-full h-12 text-lg rounded-xl bg-gradient-to-r from-indigo-500 to-purple-500 border-none shadow-lg shadow-indigo-500/30 hover:shadow-indigo-500/50 hover:scale-[1.02] transition-all duration-300"
            >
              立即预订
            </el-button>
          </div>
        </el-form>
      </el-card>
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

/* 输入框样式美化 */
:deep(.custom-input .el-input__wrapper),
:deep(.custom-input .el-textarea__inner) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px #e5e7eb inset !important;
  background-color: #f9fafb;
  transition: all 0.3s;
}

:deep(.custom-input .el-input__wrapper:hover),
:deep(.custom-input .el-textarea__inner:hover) {
  background-color: #fff;
}

:deep(.custom-input .el-input__wrapper.is-focus),
:deep(.custom-input .el-textarea__inner:focus) {
  background-color: #fff;
  box-shadow: 0 0 0 2px #818cf8 inset !important;
}

/* 隐藏 Radio Button 默认边框，使用自定义样式 */
:deep(.custom-radio-group) {
  display: grid !important;
  grid-template-columns: repeat(4, 1fr) !important;
  gap: 0.75rem !important;
  width: 100%;
}

:deep(.custom-radio-group .el-radio-button) {
  width: 100%;
  margin-right: 0 !important;
}

:deep(.custom-radio-group .el-radio-button__inner) {
  border: 1px solid #e5e7eb;
  border-radius: 8px !important;
  box-shadow: none !important;
  width: 100%;
  background: white;
  color: #4b5563;
  padding: 10px 0; /* 增加点击区域 */
}

:deep(.custom-radio-group .el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #4f46e5;
  border-color: #4f46e5;
  color: white;
  box-shadow: 0 4px 6px rgba(79, 70, 229, 0.3) !important;
}

:deep(.custom-radio-group .el-radio-button__original-radio:disabled + .el-radio-button__inner) {
  background-color: #f3f4f6;
  border-color: #f3f4f6;
  color: #d1d5db;
}
</style>