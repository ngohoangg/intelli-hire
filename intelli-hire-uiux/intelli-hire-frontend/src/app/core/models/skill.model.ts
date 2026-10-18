export interface Skill {
  id: string;
  name: string;
  category: string;
  type: string;
  description?: string;
}

export interface ResumeSkill {
  id: string;
  resume_id: string;
  skill_id: string;
  skill_name?: string;
  proficiency_level: string;
  years_experience: number;
  confidence_score?: number;
  evidence_text?: string;
  is_primary: boolean;
  created_at: string;
  updated_at: string;
}

export interface ResumeSkillRequest {
  skill_id: string;
  proficiency_level: string;
  years_experience: number;
  confidence_score?: number;
  evidence_text?: string;
  is_primary: boolean;
}

export enum ProficiencyLevel {
  BEGINNER = 'BEGINNER',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED',
  EXPERT = 'EXPERT'
}

export interface ResumeSkillProfile {
  id?: string;
  resume_id: string;
  top_skills: Record<string, any>;
  years_estimated: number;
  seniority: string;
  summary?: string;
  signals: Record<string, any>;
  generated_at?: string;
  created_at?: string;
  updated_at?: string;
}

export interface UpdateSkillProfileRequest {
  summary?: string;
  signals?: Record<string, any>;
}
