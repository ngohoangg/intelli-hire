export interface ResumeContact {
  id: string;
  resume_id: string;
  full_name: string;
  email?: string;
  phone?: string;
  location?: string;
  linkedin_url?: string;
  other_info?: string;
  created_at: string;
  updated_at: string;
}

export interface ResumeContactRequest {
  full_name: string;
  email?: string;
  phone?: string;
  location?: string;
  linkedin_url?: string;
  other_info?: string;
}
