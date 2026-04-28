import { defineStore } from 'pinia'
import { ref } from 'vue'
import apiClient from '@/services/apiClient'

export const useUserStore = defineStore('user', () => {
  const users = ref<any[]>([])
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  const fetchUsers = async () => {
    isLoading.value = true
    error.value = null
    try {
      const response = await apiClient.get('/api/v1/users')
      users.value = response.data || []
    } catch (e) {
      console.error('Error fetching users:', e)
      error.value = 'Failed to load users'
      users.value = [] // Fallback estricto Zero-Mock
    } finally {
      isLoading.value = false
    }
  }

  return {
    users,
    isLoading,
    error,
    fetchUsers
  }
})
