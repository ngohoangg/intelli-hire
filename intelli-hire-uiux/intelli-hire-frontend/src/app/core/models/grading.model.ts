export interface GradingResultResponse {
  question_id: string;
  question_text: string;
  answer_text: string;
  skill_name: string;
  score: number;
  max_score: number;
  feedback: string;
}

export interface GradingResponse {
  id: string;
  session_id: string;
  user_id: string;
  topic: string;
  status: 'PROCESSING' | 'COMPLETED' | 'FAILED';
  total_questions: number;
  total_score: number;
  max_score: number;
  results: GradingResultResponse[];
  created_at: string;
}
