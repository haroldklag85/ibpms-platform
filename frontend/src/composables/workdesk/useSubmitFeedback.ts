import { ref } from 'vue'

export function useSubmitFeedback() {
    const phase = ref<'idle' | 'validating' | 'saving' | 'success' | 'error'>('idle')
    const errorMessage = ref<string>('')

    const startSubmit = () => {
        phase.value = 'validating'
        errorMessage.value = ''
        // simulates transition to saving
        setTimeout(() => {
            if (phase.value === 'validating') {
                phase.value = 'saving'
            }
        }, 800)
    }

    const setSuccess = () => {
        phase.value = 'success'
    }

    const setError = (msg: string) => {
        phase.value = 'error'
        errorMessage.value = msg
    }
    
    const reset = () => {
        phase.value = 'idle'
        errorMessage.value = ''
    }

    return {
        phase,
        errorMessage,
        startSubmit,
        setSuccess,
        setError,
        reset
    }
}
