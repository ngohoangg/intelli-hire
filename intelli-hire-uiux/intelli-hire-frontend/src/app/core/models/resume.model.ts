export enum ResumeStatus {
  UPLOADED = 'UPLOADED',
  PARSING = 'PARSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  DELETED = 'DELETED'
}

export interface Resume {
  id: string;
  user_id: string;
  title: string;
  status: ResumeStatus;
  is_active: boolean;
  created_at: string;
  updated_at: string;
  created_by?: string;
  updated_by?: string;
}

export interface CreateResumeRequest {
  title: string;
}

export interface UpdateResumeRequest {
  title: string;
}
