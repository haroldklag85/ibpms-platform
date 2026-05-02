import { z } from 'zod';

export enum ItemStatus {
  TO_DO = 'TO_DO',
  IN_PROGRESS = 'IN_PROGRESS',
  IN_REVIEW = 'IN_REVIEW',
  DONE = 'DONE'
}

export enum ItemType {
  EPIC = 'EPIC',
  STORY = 'STORY',
  BUG = 'BUG'
}

export const AgileTagSchema = z.object({
  id: z.string(),
  label: z.string().min(2, "Tag label must be at least 2 characters."),
  color: z.string().optional()
});

export const AgileAssigneeSchema = z.object({
  userId: z.string(),
  name: z.string(),
  email: z.string().email(),
  avatarUrl: z.string().optional()
});

export const BacklogItemSchema = z.object({
  id: z.string(),
  title: z.string().min(5, "Title must be at least 5 characters."),
  description: z.string().optional(),
  type: z.nativeEnum(ItemType),
  status: z.nativeEnum(ItemStatus),
  storyPoints: z.number().nonnegative().optional(),
  sprintId: z.string().nullable().optional(),
  tags: z.array(AgileTagSchema),
  assignees: z.array(AgileAssigneeSchema),
  // CA-13: updatedAt para evaluar tickets rancios (Stale)
  updatedAt: z.string().optional()
});

export const SprintSchema = z.object({
  id: z.string(),
  projectId: z.string(),
  name: z.string().min(3),
  startDate: z.string().optional(),
  endDate: z.string().optional(),
  goal: z.string().optional(),
  status: z.enum(['PLANNED', 'ACTIVE', 'CLOSED'])
});

export const AgileProjectSchema = z.object({
  id: z.string(),
  key: z.string().min(2).max(10),
  name: z.string().min(3),
  description: z.string().optional(),
  status: z.enum(['ACTIVE', 'CLOSED', 'ARCHIVED']).optional()
});

export type AgileTag = z.infer<typeof AgileTagSchema>;
export type AgileAssignee = z.infer<typeof AgileAssigneeSchema>;
export type BacklogItem = z.infer<typeof BacklogItemSchema>;
export type Sprint = z.infer<typeof SprintSchema>;
export type AgileProject = z.infer<typeof AgileProjectSchema>;
