import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref<any[]>([])

  const totalCount = computed(() => {
    return items.value.reduce((total, item) => total + (Number(item.quantity) || 0), 0)
  })

  const totalAmount = computed(() => {
    return items.value.reduce((total, item) => total + (Number(item.price) || 0) * (Number(item.quantity) || 0), 0)
  })

  const addItem = (product: any) => {
    const existingItem = items.value.find(item => item.id === product.id)
    if (existingItem) {
      existingItem.quantity = (Number(existingItem.quantity) || 0) + 1
    } else {
      items.value.push({ ...product, quantity: 1 })
    }
  }

  const removeItem = (productId: string) => {
    const index = items.value.findIndex(item => item.id === productId)
    if (index > -1) {
      items.value.splice(index, 1)
    }
  }

  const decreaseItem = (productId: string) => {
    const existingItem = items.value.find(item => item.id === productId)
    if (existingItem) {
      if (existingItem.quantity > 1) {
        existingItem.quantity--
      } else {
        removeItem(productId)
      }
    }
  }

  const clearCart = () => {
    items.value = []
  }

  return {
    items,
    totalCount,
    totalAmount,
    addItem,
    removeItem,
    decreaseItem,
    clearCart
  }
})
