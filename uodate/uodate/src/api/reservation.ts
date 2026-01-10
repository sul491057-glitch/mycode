import request from '@/utils/request'

// 1. 提交预订 (只保留这一个创建方法)
export function createReservation(data: any) {
  return request({
    url: '/reservations',
    method: 'post',
    data
  })
}

// 2. 获取预定列表 (管理员)
export function getReservations(params?: any) {
  return request({
    url: '/reservations/admin',
    method: 'get',
    params
  })
}

// 3. 更新预订状态
export function updateReservationStatus(id: number | string, status: string) {
  return request({
    url: `/reservations/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 4. 获取已被占用的餐桌
// 注意：如果你还没在后端加 /reservations/occupied 接口，
// 这里会报 404。如果没加，请按之前的建议暂时改成 url: '/reservations/admin'
export function getOccupiedTables() {
  return request({
    url: '/reservations/occupied', 
    method: 'get'
  })
}