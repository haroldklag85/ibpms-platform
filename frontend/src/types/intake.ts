export interface PreTriageTask {
  id: string;
  source: string; // e.g. 'EMAIL_WEBHOOK'
  receivedAt: string; // ISO-8601
  sender: string;
  subject: string;
  payloadPreview: string;
  slaDeadline: string; // ISO-8601, computed limit for triage
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
}

export interface ApproveTaskPayload {
  processType: string; // The BPMN process type selected
}

export interface RejectTaskPayload {
  rejectionReason: string; // Mandatory reason
}

export interface PaginatedTriageTasks {
  tasks: PreTriageTask[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
}
