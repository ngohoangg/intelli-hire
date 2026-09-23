// =============================================
// Report Service — Domain Models
// Backend: intelli-hire-report (port 8085)
// =============================================

// Enums
export enum ReportStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

// Report Summary (used in list/history)
export interface ReportSummary {
  id: string;
  session_id: string;
  user_id: number;
  topic: string;
  total_score: number;
  max_score: number;
  total_questions: number;
  status: ReportStatus;
  created_at: string;
}

// Skill Score
export interface SkillScoreResponse {
  id: string;
  skill_name: string;
  score: number;
  max_score: number;
  question_count: number;
  feedback: string;
}

// Question Score
export interface QuestionScoreResponse {
  id: string;
  question_id: string;
  question_text: string;
  answer_text: string;
  score: number;
  max_score: number;
  feedback: string;
  skill_name: string;
  created_at: string;
}

// Full Report Response (used in detail)
export interface ReportResponse {
  id: string;
  session_id: string;
  user_id: number;
  topic: string;
  total_score: number;
  max_score: number;
  summary: string;
  roadmap_json: any;
  total_questions: number;
  status: ReportStatus;
  skill_scores: SkillScoreResponse[];
  question_scores: QuestionScoreResponse[];
  created_at: string;
  last_updated_at: string;
}

// User Stats
export interface SkillSummary {
  skill_name: string;
  average_score: number;
  total_attempts: number;
}

export interface UserStatsResponse {
  user_id: number;
  total_sessions: number;
  average_score: number;
  highest_score: number;
  lowest_score: number;
  top_skills: SkillSummary[];
  weak_skills: SkillSummary[];
}

// User Progress
export interface UserProgressResponse {
  id: string;
  user_id: number;
  skill_name: string;
  latest_score: number;
  average_score: number;
  total_attempts: number;
  score_trend: number[];
  last_updated_at: string;
}

// Request DTOs
export interface RegenerateRoadmapRequest {
  roadmap_json: any;
}

// Page Response (matches backend PageResponse<T>)
export interface ReportPageResponse<T> {
  content: T[];
  page: number;
  size: number;
  total_elements: number;
  total_pages: number;
  is_first: boolean;
  is_last: boolean;
}
