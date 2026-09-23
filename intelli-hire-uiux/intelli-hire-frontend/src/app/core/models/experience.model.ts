export interface ResumeExperience {
  id: string;
  resume_id: string;
  company: string;
  title: string;
  start_date: string;
  end_date?: string;
  description?: string;
  is_current: boolean;
  created_at: string;
  updated_at: string;
}

export interface ResumeExperienceRequest {
  company: string;
  title: string;
  start_date: string;
  end_date?: string | null;
  description?: string;
  is_current: boolean;
}
