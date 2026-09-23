export interface ResumeEducation {
  id: string;
  resume_id: string;
  school: string;
  degree?: string;
  field?: string;
  start_year?: number;
  end_year?: number;
  description?: string;
  created_at: string;
  updated_at: string;
}

export interface ResumeEducationRequest {
  school: string;
  degree?: string;
  field?: string;
  start_year?: number | null;
  end_year?: number | null;
  description?: string;
}
