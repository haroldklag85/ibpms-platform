import { ref, onUnmounted } from 'vue';

export interface UseSoftUndoOptions {
  timeoutMs?: number;
  beaconUrl?: string;
  beaconData?: any;
}

export function useSoftUndo(actionFn: () => void | Promise<void>, options: UseSoftUndoOptions = {}) {
  const timeoutMs = options.timeoutMs || 5000;
  const isUndoing = ref(false);
  const countdown = ref(0);
  let intervalId: ReturnType<typeof setInterval> | null = null;
  let timeoutId: ReturnType<typeof setTimeout> | null = null;

  const handleBeforeUnload = () => {
    if (isUndoing.value && options.beaconUrl) {
      const data = options.beaconData || {};
      const blob = new Blob([JSON.stringify(data)], { type: 'application/json' });
      navigator.sendBeacon(options.beaconUrl, blob);
    } else if (isUndoing.value) {
      actionFn();
    }
  };

  const execute = () => {
    isUndoing.value = true;
    countdown.value = timeoutMs / 1000;

    window.addEventListener('beforeunload', handleBeforeUnload);

    intervalId = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) {
        clearInterval(intervalId!);
      }
    }, 1000);

    timeoutId = setTimeout(() => {
      cleanup();
      actionFn();
    }, timeoutMs);
  };

  const cancel = () => {
    if (isUndoing.value) {
      cleanup();
    }
  };

  const cleanup = () => {
    isUndoing.value = false;
    countdown.value = 0;
    if (intervalId) clearInterval(intervalId);
    if (timeoutId) clearTimeout(timeoutId);
    window.removeEventListener('beforeunload', handleBeforeUnload);
  };

  onUnmounted(() => {
    cleanup();
  });

  return {
    execute,
    cancel,
    isUndoing,
    countdown
  };
}
