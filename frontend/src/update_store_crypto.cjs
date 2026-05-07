const fs = require('fs');

const storePath = 'C:\\Users\\HaroltAndrésGómezAgu\\ProyectoAntigravity\\ibpms-platform\\frontend\\src\\stores\\genericFormStore.ts';
let storeContent = fs.readFileSync(storePath, 'utf8');

// Add imports
if (!storeContent.includes('useAuthStore')) {
    storeContent = storeContent.replace(
        "import apiClient from '@/services/apiClient'",
        "import apiClient from '@/services/apiClient'\nimport { useAuthStore } from '@/stores/useAuthStore'\nimport { encryptDraft, decryptDraft } from '@/utils/draftCrypto'"
    );
}

// Update autoSaveDraft
const newAutoSaveDraft = `  const autoSaveDraft = debounce(async () => {
    if (!taskId.value) return
    
    syncState.value = "SAVING"
    const payload: GenericFormDraft = {
      observations: observations.value,
      files: [], 
      result: result.value
    }
    
    try {
      // LocalStorage first (Encrypted)
      const authStore = useAuthStore()
      const encryptedData = await encryptDraft(JSON.stringify(payload), authStore.userId || 'default-session-key')
      localStorage.setItem(\`generic_draft_\${taskId.value}\`, encryptedData)
      
      // Remote Save
      await apiClient.put(\`/drafts/\${taskId.value}\`, payload)
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
  }, 1000)`;

const autoSaveRegex = /const autoSaveDraft = debounce\(async \(\) => \{[\s\S]*?syncErrorCount\.value\+\+\n      if \(syncErrorCount\.value >= 3\) \{\n        syncState\.value = "ERROR"\n      \}\n    \}\n  \}, \d+\)/;
storeContent = storeContent.replace(autoSaveRegex, newAutoSaveDraft);

// Update checkForDraft
const newCheckForDraft = `  const checkForDraft = async () => {
    try {
      let remoteDraft: GenericFormDraft | null = null
      try {
        const res = await apiClient.get(\`/drafts/\${taskId.value}\`)
        if (res.data && Object.keys(res.data).length > 0) {
            remoteDraft = res.data
        }
      } catch (e) {
        // Ignored, try local
      }

      if (remoteDraft) {
        return remoteDraft
      }

      const localStr = localStorage.getItem(\`generic_draft_\${taskId.value}\`)
      if (localStr) {
        try {
            const authStore = useAuthStore()
            const decrypted = await decryptDraft(localStr, authStore.userId || 'default-session-key')
            return JSON.parse(decrypted) as GenericFormDraft
        } catch (cryptoErr) {
            console.error("Error decrypting draft, might be corrupted or old version", cryptoErr)
            return null
        }
      }
    } catch (e) {
        console.error("Error checking draft", e)
    }
    return null
  }`;

const checkForDraftRegex = /const checkForDraft = async \(\) => \{[\s\S]*?return null\n  \}/;
storeContent = storeContent.replace(checkForDraftRegex, newCheckForDraft);

fs.writeFileSync(storePath, storeContent);
console.log('Done genericFormStore.ts');
