# 餐厅预订综合业务管理系统 - 技术架构文档

## 1. 技术栈选择
- **前端框架**：Vue 3 (Composition API) + TypeScript
- **构建工具**：Vite
- **UI 组件库**：Element Plus (配置为中文环境)
- **样式工具**：Tailwind CSS (辅助布局)
- **路由管理**：Vue Router 4
- **状态管理**：Pinia
- **HTTP 请求**：Axios
- **数据模拟**：Mock.js 或 Vite Mock 插件

## 2. 项目目录结构 (Proposed)
```
src/
├── api/             # API 接口定义
├── assets/          # 静态资源
├── components/      # 公共组件
├── layout/          # 布局组件 (LayoutCustomer, LayoutAdmin)
├── mock/            # Mock 数据定义
├── router/          # 路由配置
├── store/           # Pinia 状态管理
├── styles/          # 全局样式
├── types/           # TypeScript 类型定义
├── views/           # 页面视图
│   ├── auth/        # 登录页
│   ├── customer/    # 顾客端页面 (Home, Menu, Reservation)
│   └── admin/       # 管理员端页面 (Dashboard, Recommendations)
└── App.vue
└── main.ts
```

## 3. 数据模型 (TypeScript Interfaces)

### 3.1 用户 (User)
```typescript
interface User {
  id: string;
  role: 'admin' | 'customer';
  name: string;
  token?: string;
}
```

### 3.2 商品 (Product)
```typescript
interface Product {
  id: string;
  name: string;
  price: number;
  description: string;
  imageUrl: string;
  category: string;
  isRecommended: boolean; // 是否推荐
}
```

### 3.3 订单 (Order)
```typescript
interface Order {
  id: string;
  customerId: string;
  items: Array<{ productId: string; quantity: number }>;
  totalAmount: number;
  status: 'pending' | 'completed' | 'cancelled';
  createTime: string;
}
```

### 3.4 预订 (Reservation)
```typescript
interface Reservation {
  id: string;
  customerId: string;
  date: string;
  time: string;
  peopleCount: number;
  status: 'confirmed' | 'pending';
}
```

## 4. 路由规划
- `/login`: 登录页 (入口)
- `/customer`: 顾客端布局
  - `/customer/home`: 推荐菜品/首页
  - `/customer/menu`: 菜单/下单
  - `/customer/reservation`: 预订
- `/admin`: 管理员端布局 (需路由守卫)
  - `/admin/orders`: 订单列表
  - `/admin/recommendations`: 推荐管理

## 5. Mock 策略
- 拦截 Axios 请求，返回模拟 JSON 数据。
- 模拟延迟 (delay) 以测试 Loading 状态。
