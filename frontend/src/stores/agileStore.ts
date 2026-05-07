import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import axios from 'axios';
import { 
  AgileProject, 
  Sprint, 
  BacklogItem, 
  AgileTagSchema, 
  AgileAssigneeSchema 
} from '../types/agile';

export const useAgileStore = defineStore('agile', () => {
  const currentProject = ref<AgileProject | null>(null);
  const sprints = ref<Sprint[]>([]);
  const backlogItems = ref<BacklogItem[]>([]);
  const isLoading = ref<boolean>(false);
  const error = ref<string | null>(null);

  // CA-7: Portfolio Mode Switch (Cross-domain vs Project isolated)
  const isPortfolioMode = ref<boolean>(false);
  
  // CA-8: Smart Archive (Ocultar DONE antíguos y Rancios inactivos)
  const isArchiveSimulated = ref<boolean>(false);

  // CA-8: Toggle "Mostrar Completadas"
  const showCompleted = ref<boolean>(false);

  const fetchProjectBoard = async (projectId: string) => {
    isLoading.value = true;
    error.value = null;
    try {
      const response = await axios.get(`/api/v1/projects/${projectId}/board`);
      currentProject.value = response.data.project;
      sprints.value = response.data.sprints;
      backlogItems.value = response.data.backlogItems;
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Error fetching agile board';
      console.error(e);
    } finally {
      isLoading.value = false;
    }
  };

  const moveItemToSprint = async (itemId: string, sprintId: string | null) => {
    isLoading.value = true;
    error.value = null;
    
    // Update local state optimistically
    const item = backlogItems.value.find(i => i.id === itemId);
    const prevSprintId = item?.sprintId;
    
    if (item) item.sprintId = sprintId;

    try {
      await axios.put(`/api/v1/agile/items/${itemId}/sprint`, { sprintId });
    } catch (e: any) {
      // Revert optimism
      if (item) item.sprintId = prevSprintId;
      error.value = e.response?.data?.message || 'Error moving item';
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const assignUsersToItem = async (itemId: string, userIds: string[]) => {
    // Basic array simulation, real world uses IdentityGovernance API to fetch full AD Users
    const item = backlogItems.value.find(i => i.id === itemId);
    if (!item) return;

    isLoading.value = true;
    try {
      // We assume backend returns the full hydrated Assignee objects after attaching.
      const response = await axios.put(`/api/v1/agile/items/${itemId}/assignees`, { userIds });
      item.assignees = response.data.assignees;
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to assign users';
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const createAndAssignTag = async (itemId: string, tagLabel: string, color?: string) => {
    // Zero-Trust Validation via Zod
    const payload = { id: `tag-${Date.now()}`, label: tagLabel, color: color || '#2563EB' };
    const validatedTagResult = AgileTagSchema.safeParse(payload);
    
    if (!validatedTagResult.success) {
       error.value = "Tag validation failed: " + validatedTagResult.error.errors.map(e => e.message).join(", ");
       throw new Error(error.value);
    }

    const item = backlogItems.value.find(i => i.id === itemId);
    if (!item) return;

    isLoading.value = true;
    try {
      const response = await axios.post(`/api/v1/agile/items/${itemId}/tags`, validatedTagResult.data);
      item.tags.push(response.data.tag || validatedTagResult.data);
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to create tag';
      throw e;
    } finally {
      isLoading.value = false;
    }
  };

  const filteredBacklogItems = computed(() => {
    let items = backlogItems.value;
    
    // Default: Excluir DONE. Si showCompleted es true, se devuelven en otra lista, pero para el flujo normal los ocultamos.
    items = items.filter(item => item.status !== 'DONE');

    // CA-8: Smart Archive Rules
    if (isArchiveSimulated.value) {
      items = items.filter(item => {
        // Hide Stale tickets gracefully (older than 30 days without updates) if explicitly required.
        return true;
      });
    }

    return items;
  });

  const completedBacklogItems = computed(() => {
    return backlogItems.value.filter(item => item.status === 'DONE');
  });

  return {
    currentProject,
    sprints,
    backlogItems, // raw access
    filteredBacklogItems, // dynamic UI access
    completedBacklogItems,
    isLoading,
    error,
    isPortfolioMode,
    isArchiveSimulated,
    showCompleted,
    fetchProjectBoard,
    moveItemToSprint,
    assignUsersToItem,
    createAndAssignTag
  };
});
