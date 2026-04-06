import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import { debounce } from 'lodash-es'
import apiClient from '@/services/apiClient'

export interface GenericFormDraft {
  observations: string;
  files: any[];
  result: string;
}

export const useGenericFormStore = defineStore('genericForm', () => {
  const taskId = ref<string>('')
  
  // Context from backend
  const prefillData = ref<Record<string, any>>({})
  const allowedResults = ref<string[]>([])
  const isContextLoading = ref(false)

  // Form State
  const observations = ref('')
  const files = ref<File[]>([])
  const result = ref('')

  // Panic Modal State
  const showPanicModal = ref(false)
  const panicAction = ref<"APPROVED" | "RETURNED" | "CANCELLED" | null>(null)
  const panicJustification = ref('')

  // Sync Flags
  const syncState = ref<"SYNCED" | "SAVING" | "LOCAL_ONLY" | "ERROR">("SYNCED")
  const syncErrorCount = ref(0)
  const isSubmitting = ref(false)

  // Initialize store for a specific task
  const init = async (id: string) => {
    taskId.value = id
    await fetchContext()
    await checkForDraft()
  }

  const fetchContext = async () => {
    isContextLoading.value = true
    try {
      const res = await apiClient.get(`/workbox/tasks/${taskId.value}/generic-form-context`)
      if (res.data) {
        prefillData.value = res.data.prefillData || {}
        allowedResults.value = res.data.allowedResults || []
      }
    } catch (e) {
      console.error("Error fetching form context", e)
    } finally {
      isContextLoading.value = false
    }
  }

  // --- Draft Management ---
  const autoSaveDraft = debounce(async () => {
    if (!taskId.value) return
    
    syncState.value = "SAVING"
    const payload: GenericFormDraft = {
      observations: observations.value,
      files: [], // Files serialization logic ignored for mock/local save simplicity, in real life we might just save file paths or metadata
      result: result.value
    }
    
    try {
      // LocalStorage first
      localStorage.setItem(`generic_draft_${taskId.value}`, JSON.stringify(payload))
      
      // Remote Save
      await apiClient.put(`/drafts/${taskId.value}`, payload)
      syncState.value = "SYNCED"
      syncErrorCount.value = 0
    } catch (e) {
      console.warn("Error remote syncing draft, falling back to local only", e)
      syncState.value = "LOCAL_ONLY"
      syncErrorCount.value++
      if (syncErrorCount.value >= 3) {
        syncState.value = "ERROR"
      }
    }
  }, 10000)

  // Trigger auto-save whenever core form fields change
  watch([observations, result], () => {
    if (syncState.value === "SYNCED") {
      syncState.value = "LOCAL_ONLY" // Visually indicates it needs saving
    }
    autoSaveDraft()
  }, { deep: true })

  const checkForDraft = async () => {
    try {
      let remoteDraft: GenericFormDraft | null = null
      try {
        const res = await apiClient.get(`/drafts/${taskId.value}`)
        if (res.data && Object.keys(res.data).length > 0) {
            remoteDraft = res.data
        }
      } catch (e) {
        // Ignored, try local
      }

      if (remoteDraft) {
        return remoteDraft
      }

      const localStr = localStorage.getItem(`generic_draft_${taskId.value}`)
      if (localStr) {
        return JSON.parse(localStr) as GenericFormDraft
      }
    } catch (e) {
        console.error("Error checking draft", e)
    }
    return null
  }

  const applyDraft = (draft: GenericFormDraft) => {
    observations.value = draft.observations || ''
    result.value = draft.result || ''
  }

  const clearDraft = async () => {
    localStorage.removeItem(`generic_draft_${taskId.value}`)
    try {
      await apiClient.delete(`/drafts/${taskId.value}`)
    } catch (e) {
      // Ignore
    }
    observations.value = ''
    result.value = ''
    syncState.value = "SYNCED"
  }

  // --- Submit ---
  const submitForm = async () => {
    isSubmitting.value = true
    try {
      const formData = new FormData()
      formData.append('observations', observations.value)
      formData.append('result', result.value)
      if (panicAction.value) {
        formData.append('panicAction', panicAction.value)
        formData.append('panicJustification', panicJustification.value)
      }
      
      // Append files
      files.value.forEach((f) => {
        formData.append('evidenceFiles', f)
      })

      await apiClient.post(`/workbox/tasks/${taskId.value}/generic-form-complete`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })

      // On success, clear drafts
      await clearDraft()
      return true
    } catch (e) {
      console.error("Submit error", e)
      return false
    } finally {
      isSubmitting.value = false
    }
  }

  return {
    taskId, init,
    prefillData, allowedResults, isContextLoading,
    observations, files, result,
    showPanicModal, panicAction, panicJustification,
    syncState, isSubmitting,
    checkForDraft, applyDraft, clearDraft, submitForm
  }
})
