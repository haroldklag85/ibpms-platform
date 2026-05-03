import { ref, onMounted, onUnmounted } from 'vue'

export function useSessionLock(taskId: string) {
    const isLocked = ref(false)
    let channel: BroadcastChannel | null = null

    onMounted(() => {
        if (!taskId) return
        channel = new BroadcastChannel(`ibpms-form-${taskId}`)
        
        // Listen for claims
        channel.onmessage = (event) => {
            if (event.data === 'CLAIM') {
                isLocked.value = true
                // We could also tell them we are here, but for now just lock ours
            } else if (event.data === 'RELEASE') {
                isLocked.value = false
            }
        }
        
        // Claim the form
        channel.postMessage('CLAIM')
    })

    onUnmounted(() => {
        if (channel) {
            channel.postMessage('RELEASE')
            channel.close()
        }
    })

    return {
        isLocked
    }
}
