import Mock from 'mockjs'

// 模拟延迟
Mock.setup({
  timeout: '200-600'
})

const Random = Mock.Random

// 菜品数据
const products = [
  { id: '1', name: '宫保鸡丁', price: 38, description: '经典川菜，酸甜微辣，鸡肉嫩滑', imageUrl: 'https://images.unsplash.com/photo-1525755662778-989d0524087e?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80', category: '热菜', isRecommended: true },
  { id: '2', name: '麻婆豆腐', price: 22, description: '麻辣鲜香，下饭神器', imageUrl: 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80', category: '热菜', isRecommended: true },
  { id: '3', name: '清蒸鲈鱼', price: 68, description: '鱼肉鲜美，清淡健康', imageUrl: 'https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80', category: '海鲜', isRecommended: false },
  { id: '4', name: '糖醋排骨', price: 45, description: '酸甜可口，老少皆宜', imageUrl: 'https://images.unsplash.com/photo-1544025162-d76694265947?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80', category: '热菜', isRecommended: true },
  { id: '5', name: '西红柿炒鸡蛋', price: 18, description: '国民家常菜', imageUrl: 'https://images.unsplash.com/photo-1598515214211-89d3c73ae83b?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80', category: '素菜', isRecommended: false },
  { id: '6', name: '扬州炒饭', price: 28, description: '粒粒分明，配料丰富', imageUrl: 'https://images.unsplash.com/photo-1603133872878-684f208fb84b?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80', category: '主食', isRecommended: false },
  { id: '7', name: '水果沙拉', price: 25, description: '新鲜时令水果', imageUrl: 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80', category: '凉菜', isRecommended: true },
  { id: '8', name: '可乐鸡翅', price: 35, description: '甜咸适中，鸡肉入味', imageUrl: 'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80', category: '热菜', isRecommended: false },
]

// 订单数据
const generateMockOrders = () => {
  const mockOrders = []
  // 生成过去7天的随机数据，保证图表有数据显示
  for (let i = 0; i < 50; i++) {
    const date = new Date()
    date.setDate(date.getDate() - Math.floor(Math.random() * 8))
    // Use local date string to match client-side filtering
    const dateStr = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    
    mockOrders.push({
      id: Random.id(),
      items: [{ id: '1', name: '宫保鸡丁', price: 38, quantity: 1 }],
      totalAmount: Random.integer(50, 300),
      status: Random.pick(['pending', 'completed', 'cancelled']),
      createTime: `${dateStr} ${Random.time('HH:mm:ss')}`,
      tableId: `A${Random.integer(1, 10)}`
    })
  }
  return mockOrders.sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime())
}

let orders: any[] = generateMockOrders()

// 获取商品列表
Mock.mock(/\/api\/products/, 'get', () => {
  return {
    code: 200,
    data: products,
    message: 'success'
  }
})

// 提交订单
Mock.mock(/\/api\/orders/, 'post', (options: any) => {
  const body = JSON.parse(options.body)
  const newOrder = {
    id: Random.id(),
    items: body.items,
    totalAmount: body.totalAmount,
    status: 'pending',
    createTime: Random.now('yyyy-MM-dd HH:mm:ss'),
    tableId: body.tableId || '无'
  }
  orders.unshift(newOrder)
  return {
    code: 200,
    data: newOrder,
    message: '下单成功'
  }
})

// 获取订单列表 (管理员)
Mock.mock(/\/api\/orders/, 'get', () => {
  return {
    code: 200,
    data: orders,
    message: 'success'
  }
})

// 推荐商品 (管理员)
Mock.mock(/\/api\/products\/recommend/, 'post', (options: any) => {
  const body = JSON.parse(options.body)
  const product = products.find(p => p.id === body.id)
  if (product) {
    product.isRecommended = body.isRecommended
  }
  return {
    code: 200,
    message: '操作成功'
  }
})

// 预订
Mock.mock(/\/api\/reservations/, 'post', () => {
  return {
    code: 200,
    message: '预订申请已提交'
  }
})
