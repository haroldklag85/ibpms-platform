// @Traceability: US-029, CA-30, CA-31
import { ref, onMounted, onUnmounted } from 'vue';

export function useTaskSync(taskId: string) {
  const isDuplicateTab = ref(false);
  const syncStatus = ref<'SYNCED' | 'LOCAL_ONLY' | 'SYNCING' | 'OFFLINE'>('SYNCED');
  let channel: BroadcastChannel | null = null;

  onMounted(() => {
    channel = new BroadcastChannel(`task_edit_${taskId}`);
    // Notificar que esta pestaña abrió la tarea
    channel.postMessage({ type: 'OPENED' });

    channel.onmessage = (event) => {
      if (event.data.type === 'OPENED') {
        // Alguien más la abrió después, o ya estaba abierta
        isDuplicateTab.value = true;
        // Responder que estamos activos
        channel?.postMessage({ type: 'ALREADY_ACTIVE' });
      } else if (event.data.type === 'ALREADY_ACTIVE') {
        isDuplicateTab.value = true;
      }
    };
  });

  onUnmounted(() => {
    channel?.close();
  });

  return { isDuplicateTab, syncStatus };
}
