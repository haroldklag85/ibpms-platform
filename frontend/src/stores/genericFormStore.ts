import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import { debounce } from 'lodash-es'
import apiClient from '@/services/apiClient'
import { useAuthStore } from '@/stores/useAuthStore'
import { encryptDraft, decryptDraft } from '@/utils/draftCrypto'

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

  // Draft Recovery Banner (REM-039-C / Patrón CA-85)
  const showDraftBanner = ref(false)
  const pendingDraft = ref<GenericFormDraft | null>(null)

  // Initialize store for a specific task
  const init = async (id: string) => {
    taskId.value = id
    await fetchContext()
    const draft = await checkForDraft()
    if (draft) {
      showDraftBanner.value = true
      pendingDraft.value = draft
    }
  }

  const restoreDraft = () => {
    if (pendingDraft.value) {
      applyDraft(pendingDraft.value)
      showDraftBanner.value = false
      pendingDraft.value = null
    }
  }

  const dismissDraft = () => {
    showDraftBanner.value = false
    pendingDraft.value = null
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
      // LocalStorage first — encrypt PII with AES-GCM (CA-11, GAP-05)
      const authStore = useAuthStore()
      const sessionKey = authStore.userId || 'fallback-key'
      const encrypted = await encryptDraft(JSON.stringify(payload), sessionKey)
      localStorage.setItem(`generic_draft_${taskId.value}`, encrypted)
      
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
        try {
          // Decrypt PII from AES-GCM (CA-11, GAP-05)
          const authStore = useAuthStore()
          const sessionKey = authStore.userId || 'fallback-key'
          const decrypted = await decryptDraft(localStr, sessionKey)
          return JSON.parse(decrypted) as GenericFormDraft
        } catch {
          // Fallback: try parsing as plain JSON (legacy drafts)
          return JSON.parse(localStr) as GenericFormDraft
        }
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

  // --- Submit (Upload-First: ADR-004 / CA-09) ---
  const submitForm = async () => {
    isSubmitting.value = true
    try {
      // Guard: Ensure all files finished uploading before submit
      if (files.value.some(f => f.status === 'UPLOADING')) {
        console.error('Cannot submit: files still uploading')
        return false
      }

      // Panic action validation
      if (panicAction.value) {
        if (!panicJustification.value || panicJustification.value.length < 20) {
          console.error('Panic justification must be >= 20 characters')
          return false
        }
      }

      // Collect temp_ids from successfully uploaded files (Upload-First pattern)
      const attachmentIds = files.value
        .filter(f => f.status === 'SUCCESS' && f.temp_id)
        .map(f => f.temp_id)

      // Send JSON payload — NO FormData, NO multipart/form-data
      await apiClient.post(`/workbox/tasks/${taskId.value}/generic-form-complete`, {
        observations: observations.value,
        result: result.value,
        panicAction: panicAction.value || undefined,
        panicJustification: panicJustification.value || undefined,
        attachments: attachmentIds
      })

      // On success, clear drafts
      await clearDraft()
      return true
    } catch (e) {
      console.error("Submit error", e)
      throw e // Re-throw so GenericFormBody.vue catch can handle 409
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
    showDraftBanner, pendingDraft, restoreDraft, dismissDraft,
    checkForDraft, applyDraft, clearDraft, submitForm
  }
})
