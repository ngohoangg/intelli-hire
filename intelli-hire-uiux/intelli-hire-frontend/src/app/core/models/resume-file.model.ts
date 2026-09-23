export interface ResumeFile {
  id: string;
  resume_id: string;
  original_name: string;
  file_type: string;
  file_size_bytes: number;
  object_bucket?: string;
  object_key: string;
  created_at: string;
  updated_at: string;
}
