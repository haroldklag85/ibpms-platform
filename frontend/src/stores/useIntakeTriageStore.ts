// @Traceability: US-004, CA-6, CA-8
import { defineStore } from 'pinia';
import { ref } from 'vue';
import axios from 'axios';
import type { PreTriageTask, PaginatedTriageTasks } from '../types/intake';

export const useIntakeTriageStore = defineStore('intakeTriage', () => {
  const tasks = ref<PreTriageTask[]>([]);
  const isLoading = ref<boolean>(false);
  const error = ref<string | null>(null);
  
  const currentPage = ref(0);
  const totalPages = ref(0);
  const totalElements = ref(0);

  const fetchTasks = async (page: number = 0, size: number = 10) => {
    isLoading.value = true;
    error.value = null;
    try {
      const response = await axios.get<PaginatedTriageTasks>(`/api/v1/intake/triage/tasks`, {
        params: { page, size }
      });
      tasks.value = response.data.tasks;
      currentPage.value = response.data.currentPage;
      totalPages.value = response.data.totalPages;
      totalElements.value = response.data.totalElements;
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Error fetching triage tasks';
      console.error('Fetch Tasks Error:', e);
    } finally {
      isLoading.value = false;
    }
  };

  const approveTask = async (taskId: string, processType: string) => {
    isLoading.value = true;
    try {
      await axios.post(`/api/v1/intake/triage/tasks/${taskId}/approve`, { processType });
      // Optimistic update
      tasks.value = tasks.value.filter(t => t.id !== taskId);
      totalElements.value = Math.max(0, totalElements.value - 1);
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Error approving task';
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const rejectTask = async (taskId: string, rejectionReason: string) => {
    if (!rejectionReason || rejectionReason.trim().length === 0) {
      error.value = 'Rejection reason is mandatory';
      throw new Error('Rejection reason is mandatory');
    }
    isLoading.value = true;
    try {
      await axios.post(`/api/v1/intake/triage/tasks/${taskId}/reject`, { rejectionReason });
      // Optimistic update
      tasks.value = tasks.value.filter(t => t.id !== taskId);
      totalElements.value = Math.max(0, totalElements.value - 1);
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Error rejecting task';
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  return {
    tasks,
    isLoading,
    error,
    currentPage,
    totalPages,
    totalElements,
    fetchTasks,
    approveTask,
    rejectTask
  };
});
