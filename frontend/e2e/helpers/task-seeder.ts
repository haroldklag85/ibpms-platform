import { APIRequestContext } from '@playwright/test';

export async function seedTask(request: APIRequestContext, payloadData: any = {}): Promise<string> {
  const response = await request.post('http://localhost:8080/api/v1/process/generic-approval/start-anonymous', {
    data: {
      payload: 'playwright_seeded_' + Date.now(),
      priority: 'high',
      ...payloadData
    }
  });

  if (!response.ok()) {
    console.warn('Failed to seed task: ' + response.statusText());
  }

  const data = await response.json().catch(() => ({}));
  return data.processInstanceId || `T-${Date.now()}`;
}

export async function seedAgileProject(request: APIRequestContext): Promise<string> {
    const response = await request.post('http://localhost:8080/api/v1/agile/projects', {
        headers: {
             // Requerimos Authorization que será obtenida del storageState
        },
        data: {
            name: 'Project Seeded ' + Date.now(),
            description: 'Zero-Mock Seeding'
        }
    });

    if (!response.ok()) {
        console.warn('Failed to seed agile project: ' + response.statusText());
        return 'UNKNOWN';
    }

    const data = await response.json();
    return data.id; // UUID
}
