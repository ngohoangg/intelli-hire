export interface SectionScore {
  score: number;
  feedback: string;
}

export interface ResumeReview {
  id: string;
  resume_id: string;
  overall_score: number;
  sections: {
    contact: SectionScore;
    experience: SectionScore;
    education: SectionScore;
    skills: SectionScore;
    presentation: SectionScore;
  };
  strengths: string[];
  weaknesses: string[];
  priority_actions: string[];
  generated_at: string;
  created_at: string;
  updated_at: string;
}
