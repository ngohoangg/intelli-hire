// =============================================
// Interview Session — Domain Models
// Backend: intelli-hire-interviews (port 8083)
// =============================================

export enum SessionStatus {
  CREATED = 'CREATED',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  EXPIRED = 'EXPIRED'
}

export enum Difficulty {
  JUNIOR = 'junior',
  MID = 'mid',
  SENIOR = 'senior'
}

// REST Response DTOs — backend dùng global SNAKE_CASE Jackson
export interface InterviewSession {
  id: number;
  topic: string;
  difficulty: Difficulty;
  status: SessionStatus;
  started_at: string | null;
  ended_at: string | null;
  created_at: string;
}

export interface QuestionLogDto {
  question_id: string;
  question_text: string;
  question_order: number;
}

export interface AnswerDto {
  question_id: string;
  answer_text: string;
  submitted_at: string;
}

export interface InterviewSessionDetail extends InterviewSession {
  questions: QuestionLogDto[];
  answers: AnswerDto[];
}

// REST Request DTOs
export interface CreateSessionRequest {
  topic: string;
  difficulty: Difficulty;
  difficulty_id?: string;
  topic_id?: string;
  question_source?: 'bank' | 'resume';
  resume_id?: string;
  question_type?: string;
  difficulty_name?: string;
  language?: string;
  focus_areas?: string[];
  question_count: number;
}

export interface SubmitAnswerRequest {
  questionId: number;
  answerText: string;
}

// WebSocket Message DTOs
// NOTE: backend dùng global SNAKE_CASE Jackson → tất cả field là snake_case
export type WsMessageType = 'QUESTION' | 'SESSION_END' | 'ERROR';

export interface QuestionMessage {
  type: WsMessageType;
  question_id: string | null;
  question_text: string | null;
  question_order: number | null;
  total_questions: number | null;
}

export interface AnswerMessage {
  question_id: string;
  answer_text: string;
}

// Pagination wrapper (matches BaseResponse<Page<T>> từ backend — SNAKE_CASE global Jackson)
export interface PageResponse<T> {
  content: T[];
  total_elements: number;
  total_pages: number;
  number: number;
  size: number;
}
