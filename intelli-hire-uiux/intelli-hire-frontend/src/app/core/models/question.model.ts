export enum QuestionType {
  TECHNICAL = 'TECHNICAL',
  CODING = 'CODING',
  BEHAVIORAL = 'BEHAVIORAL'
}

export type QuestionModerationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'ARCHIVED';

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  total_elements: number;
  total_pages: number;
  is_first: boolean;
  is_last: boolean;
}

export interface TagResponse {
  id: string;
  name: string;
}

export interface TagRequest {
  name: string;
}

export interface TopicResponse {
  id: string;
  name: string;
  description: string | null;
  parent_id: string | null;
  order: number;
  is_active: boolean;
  created_at: string;
  last_updated_at: string;
}

export interface TopicTreeResponse {
  id: string;
  name: string;
  description: string | null;
  order: number;
  children: TopicTreeResponse[];
}

export interface TopicRequest {
  name: string;
  description: string;
  parent_id: string | null;
  order: number;
  is_active: boolean;
}

export interface DifficultyLevelResponse {
  id: string;
  name: string;
  weight: number;
  description: string | null;
}

export interface DifficultyLevelRequest {
  name: string;
  weight: number;
  description: string;
}

export interface QuestionResponse {
  id: string;
  topic_id: string;
  topic_name: string;
  difficulty_id: string;
  difficulty_name: string;
  question_text: string;
  question_type: QuestionType;
  tags: TagResponse[];
  is_active: boolean;
  status: QuestionModerationStatus;
  submitted_by?: string;
  submitted_by_id?: number;
  rejection_reason?: string | null;
  created_at: string;
  last_updated_at: string;
}

export type QuestionDetail = QuestionResponse;

export interface QuestionRequest {
  topic_id: string;
  difficulty_id: string;
  question_text: string;
  question_type: QuestionType;
  tag_ids: string[];
  is_active: boolean;
}

export interface QuestionSearchParams {
  page?: number;
  size?: number;
  sort?: string;
  topicId?: string;
  difficultyId?: string;
  type?: QuestionType | '';
  tagIds?: string[];
  keyword?: string;
  isActive?: boolean | '';
  status?: QuestionModerationStatus | '';
}

export interface RubricResponse {
  id: string;
  question_id: string;
  criteria: string;
  keywords: string[];
  example_answer: string;
  common_mistakes: string[];
  hints: string[];
  max_score: number;
  is_active: boolean;
  created_at: string;
  last_updated_at: string;
}

export interface RubricRequest {
  criteria: string;
  keywords: string[];
  example_answer: string;
  common_mistakes: string[];
  hints: string[];
  max_score: number;
}

export interface StudyArticleResponse {
  id: string;
  topic_id: string;
  topic_name: string;
  difficulty_id: string;
  difficulty_name: string;
  title: string;
  url: string;
  description: string;
  source: string;
  is_active: boolean;
}

export interface StudyArticleRequest {
  topic_id: string;
  difficulty_id: string;
  title: string;
  url: string;
  description: string;
  source: string;
  is_active: boolean;
}

export interface StudyArticleSearchParams {
  topicId?: string;
  difficultyId?: string;
  page?: number;
  size?: number;
}

export interface RandomQuestionRequest {
  topic_id?: string;
  difficulty_id?: string;
  type?: QuestionType | '';
  tag_ids?: string[];
  count: number;
  exclude_ids?: string[];
}

export interface GenerateQuestionsFromResumeRequest {
  resume_id: string;
  count: number;
  question_type?: QuestionType | '';
  difficulty_name?: string;
  language?: string;
  focus_areas?: string[];
}

export interface GeneratedQuestionItemResponse {
  question_text: string;
  question_type: QuestionType | string;
  topic_id?: string | null;
  topic_name?: string | null;
  difficulty_id?: string | null;
  difficulty_name?: string | null;
  rationale?: string | null;
}

export interface GenerateQuestionsFromResumeResponse {
  resume_id: string;
  candidate_summary?: string | null;
  total_questions: number;
  generated_at?: string | null;
  questions: GeneratedQuestionItemResponse[];
}

export interface BulkImportRequest {
  questions: QuestionRequest[];
}

export interface BulkImportError {
  index: number;
  error: string;
}

export interface BulkImportResponse {
  total: number;
  created: number;
  failed: number;
  errors: BulkImportError[];
}
