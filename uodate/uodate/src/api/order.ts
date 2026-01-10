import request from '@/utils/request'

// 1. 提交订单 (顾客点餐用)
export function submitOrder(data: any) {
  return request({
    url: '/orders',
    method: 'post',
    data
  })
}

// 2. 获取订单列表 (管理员后台用，支持分页/查询)
export function getOrders(params?: any) {
  return request({
    url: '/orders',
    method: 'get',
    params
  })
}

// 3. 更新订单状态 (管理员操作用)
export function updateOrderStatus(id: number | string, status: string) {
  return request({
    url: `/orders/${id}/status`,
    method: 'put',
    params: { status }
  })
}