<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductList as getProducts, toggleRecommend, addProduct, updateProduct, deleteProduct, uploadImage } from "@/api/product";
import { Search, Plus, ZoomIn, Download, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const productSearch = ref('')
const productPage = ref(1)
const productPageSize = ref(10)
const products = ref<any[]>([])

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

const fetchProducts = async () => {
  loading.value = true
  try {
    const res: any = await getProducts()
    
    // 🛡️ 数据标准化
    products.value = res.map((p: any) => {
      const rawStatus = p.isRecommended ?? p.is_recommended ?? p.isRecommend
      const isRecommended = rawStatus === 1 || rawStatus === true || rawStatus === '1'
      return { ...p, isRecommended }
    })
  } catch (error) {
    ElMessage.error('获取商品失败')
  } finally {
    loading.value = false
  }
}

const handleRecommendChange = async (row: any) => {
  try {
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
    }
  })
}

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

const handleEditProduct = (row: any) => {
  isEditMode.value = true
  productForm.value = { ...row }
  productDialogVisible.value = true
}

const handleUpload = async (options: any) => {
  const { file, onSuccess, onError } = options
  const formData = new FormData()
  formData.append('file', file)

  try {
    const res: any = await uploadImage(formData)
    let url = ''
    if (typeof res === 'string') {
      url = res
    } else if (typeof res === 'object') {
      url = res.url || res.data || res.message
    }

    if (!url || !url.startsWith('/')) {
       console.error('上传响应异常:', res)
       throw new Error('无法解析上传返回的图片路径')
    }

    productForm.value.imageUrl = url
    ElMessage.success('上传成功')
    onSuccess(res)
  } catch (err) {
    console.error('上传失败:', err)
    onError(err)
  }
}

const handlePictureCardPreview = () => {
  if (!productForm.value.imageUrl) return
  dialogImageUrl.value = productForm.value.imageUrl
  previewVisible.value = true
}

const handleRemove = () => {
  productForm.value.imageUrl = ''
}

const handleDownload = () => {
  if (!productForm.value.imageUrl) return
  window.open(productForm.value.imageUrl, '_blank')
}

const submitProductForm = async () => {
  try {
    if (!productForm.value.name || !productForm.value.price) {
      ElMessage.warning('请填写完整的菜品信息')
      return
    }

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
  }
}

onMounted(() => {
  fetchProducts()
})
</script>

<template>
  <div class="p-6">
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
</style>
