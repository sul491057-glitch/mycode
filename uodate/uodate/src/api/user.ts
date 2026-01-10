import request from '@/utils/request'

// 登录接口
export function login(data: any) {
  return request({
    url: '/login', // 对应后端 UserController 的 @PostMapping("/login")
    method: 'post',
    data
  })
}