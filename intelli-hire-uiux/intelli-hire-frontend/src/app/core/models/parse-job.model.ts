export enum JobStatus {
  QUEUED = 'QUEUED',
  RUNNING = 'RUNNING',
  SUCCEEDED = 'SUCCEEDED',
  FAILED = 'FAILED',
  CANCELED = 'CANCELED'
}

export enum JobType {
  PARSE = 'PARSE',
  REPARSE = 'REPARSE',
  ANALYZE = 'ANALYZE'
}

export interface ParseJob {
  id: string;
  resume_id: string;
  status: JobStatus;
  job_type: JobType;
  progress: number;
  retry_count: number;
  error_message?: string;
  started_at?: string;
  finished_at?: string;
  created_at?: string;
  updated_at?: string;
}

export interface CreateParseJobRequest {
  resume_id: string;
  job_type: JobType;
}
