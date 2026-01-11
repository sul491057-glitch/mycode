import request from '@/utils/request'

// 1. 获取菜单列表 (支持传参，如分页或查询)
export function getProductList(params?: any) {
  return request({
    url: '/products',
    method: 'get',
    params
  })
}

// 2. 切换推荐状态
export function toggleRecommend(data: any) {
  return request({
    url: '/products/recommend',
    method: 'post',
    data
  })
}

// 3. 新增菜品
export function addProduct(data: any) {
  return request({
    url: '/products',
    method: 'post',
    data
  })
}

// 4. 修改菜品
export function updateProduct(data: any) {
  return request({
    url: '/products',
    method: 'put',
    data
  })
}

// 5. 删除菜品
export function deleteProduct(id: number | string) {
  return request({
    url: `/products/${id}`,
    method: 'delete'
  })
}

// 6. 图片上传接口
export function uploadImage(formData: FormData) {
  return request({
    url: '/common/upload',
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' },
    data: formData
  })
}