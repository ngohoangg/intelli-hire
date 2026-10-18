import {
  AnswerDto,
  CreateSessionRequest,
  Difficulty,
  InterviewSession,
  InterviewSessionDetail,
  SessionStatus
} from '../models/interview.model';
import { GradingResponse, GradingResultResponse } from '../models/grading.model';
import { ReportResponse, ReportStatus, SkillScoreResponse } from '../models/report.model';

/**
 * TEMPORARY interview data for the interview mocks (see interview-mock.interceptor.ts).
 * Sessions are kept in sessionStorage so the room/detail pages survive a reload.
 */

export const GRADING_DURATION_MS = 7000;
const MOCK_SESSION_ID_BASE = 900001;
const STORAGE_KEY = 'intellihire.mockInterviews';
const MAX_SCORE = 10;

type Lang = 'en' | 'vi';

interface QuestionTemplate {
  skill: string;
  text: Record<Lang, string>;
}

export interface MockQuestion {
  id: string;
  text: string;
  skill: string;
}

export interface MockSession {
  id: number;
  topic: string;
  difficulty: Difficulty;
  status: SessionStatus;
  lang: Lang;
  questions: MockQuestion[];
  answers: AnswerDto[];
  /** Index of the question currently shown; -1 before the interview starts. */
  currentIndex: number;
  createdAt: string;
  startedAt: string | null;
  endedAt: string | null;
}

// ── Question pools ────────────────────────────────────────────────────────────

/** "AI Generated" questions, based on the mock CV (Java backend developer). */
const RESUME_TECHNICAL: QuestionTemplate[] = [
  {
    skill: 'Apache Kafka',
    text: {
      en: 'Your CV mentions a Kafka-based event pipeline at TechNova Solutions. How did you guarantee message ordering and handle duplicate events?',
      vi: 'CV của bạn có nhắc đến pipeline sự kiện dùng Kafka tại TechNova Solutions. Bạn đã đảm bảo thứ tự message và xử lý sự kiện trùng lặp như thế nào?'
    }
  },
  {
    skill: 'Redis',
    text: {
      en: 'You reduced API p95 latency from 800ms to 180ms with Redis. Walk through your caching strategy and how you handled cache invalidation.',
      vi: 'Bạn đã giảm độ trễ p95 của API từ 800ms xuống 180ms bằng Redis. Hãy trình bày chiến lược cache và cách bạn xử lý việc invalidate cache.'
    }
  },
  {
    skill: 'Spring Boot',
    text: {
      en: 'How do you design transaction boundaries in a Spring Boot service that updates PostgreSQL and publishes an event at the same time?',
      vi: 'Bạn thiết kế ranh giới transaction thế nào trong một service Spring Boot vừa cập nhật PostgreSQL vừa publish sự kiện?'
    }
  },
  {
    skill: 'PostgreSQL',
    text: {
      en: 'How would you diagnose a slow PostgreSQL query in the payment reconciliation APIs you built?',
      vi: 'Bạn sẽ chẩn đoán một câu truy vấn PostgreSQL chậm trong các API đối soát thanh toán mà bạn xây dựng như thế nào?'
    }
  },
  {
    skill: 'Microservices',
    text: {
      en: 'What criteria do you use to split a monolith into microservices, and what problems did you face with inter-service communication?',
      vi: 'Bạn dựa vào tiêu chí nào để tách monolith thành microservices, và đã gặp khó khăn gì trong giao tiếp giữa các service?'
    }
  },
  {
    skill: 'Java',
    text: {
      en: 'How does the JVM garbage collector affect latency in high-traffic services, and which GC settings have you tuned?',
      vi: 'Garbage collector của JVM ảnh hưởng thế nào đến độ trễ của service có lưu lượng cao, và bạn đã tinh chỉnh những thiết lập GC nào?'
    }
  },
  {
    skill: 'Testing',
    text: {
      en: 'How do you test Spring Boot services that depend on PostgreSQL and Kafka? Describe how you use Testcontainers.',
      vi: 'Bạn kiểm thử các service Spring Boot phụ thuộc PostgreSQL và Kafka như thế nào? Hãy mô tả cách bạn dùng Testcontainers.'
    }
  },
  {
    skill: 'API Design',
    text: {
      en: 'How would you make a payment REST API idempotent?',
      vi: 'Bạn sẽ làm cho một REST API thanh toán trở nên idempotent như thế nào?'
    }
  },
  {
    skill: 'Security',
    text: {
      en: 'How would you secure communication between microservices?',
      vi: 'Bạn sẽ bảo mật giao tiếp giữa các microservice như thế nào?'
    }
  },
  {
    skill: 'Observability',
    text: {
      en: 'How do you handle distributed tracing and logging across multiple services?',
      vi: 'Bạn xử lý distributed tracing và logging xuyên suốt nhiều service như thế nào?'
    }
  }
];

/** "System Bank" questions; `{topic}` is replaced with the topic the user entered. */
const TOPIC_TECHNICAL: QuestionTemplate[] = [
  {
    skill: '{topic}',
    text: {
      en: 'What are the core concepts of {topic}, and when would you choose it over the alternatives?',
      vi: 'Những khái niệm cốt lõi của {topic} là gì, và khi nào bạn chọn nó thay vì các lựa chọn khác?'
    }
  },
  {
    skill: '{topic}',
    text: {
      en: 'Describe a real project where you used {topic}. What problem did it solve, and what trade-offs did you make?',
      vi: 'Hãy mô tả một dự án thực tế mà bạn đã dùng {topic}. Nó giải quyết vấn đề gì và bạn đã đánh đổi những gì?'
    }
  },
  {
    skill: 'Performance',
    text: {
      en: 'What are common performance pitfalls with {topic}, and how do you avoid them?',
      vi: 'Những lỗi hiệu năng thường gặp khi dùng {topic} là gì, và bạn tránh chúng như thế nào?'
    }
  },
  {
    skill: 'Testing',
    text: {
      en: 'How do you test and debug applications built with {topic}?',
      vi: 'Bạn kiểm thử và debug ứng dụng xây dựng bằng {topic} như thế nào?'
    }
  },
  {
    skill: '{topic}',
    text: {
      en: 'Explain a {topic} best practice that many developers get wrong.',
      vi: 'Hãy giải thích một best practice của {topic} mà nhiều lập trình viên hay làm sai.'
    }
  },
  {
    skill: 'Architecture',
    text: {
      en: 'How would you structure a large {topic} codebase so that it stays maintainable?',
      vi: 'Bạn sẽ tổ chức một codebase {topic} lớn như thế nào để dễ bảo trì?'
    }
  },
  {
    skill: 'Security',
    text: {
      en: 'What security concerns should you keep in mind when working with {topic}?',
      vi: 'Khi làm việc với {topic}, bạn cần lưu ý những vấn đề bảo mật nào?'
    }
  },
  {
    skill: 'Problem Solving',
    text: {
      en: 'Describe the hardest bug you have fixed involving {topic}.',
      vi: 'Hãy mô tả lỗi khó nhất liên quan đến {topic} mà bạn từng sửa.'
    }
  }
];

const BEHAVIORAL: QuestionTemplate[] = [
  {
    skill: 'Teamwork',
    text: {
      en: 'Describe a time you helped a teammate who was struggling. What did you do, and what was the result?',
      vi: 'Hãy kể về một lần bạn giúp đỡ một đồng đội đang gặp khó khăn. Bạn đã làm gì và kết quả ra sao?'
    }
  },
  {
    skill: 'Ownership',
    text: {
      en: 'Tell me about a production incident you owned. What happened, and what did you change afterwards?',
      vi: 'Hãy kể về một sự cố production mà bạn chịu trách nhiệm xử lý. Chuyện gì đã xảy ra và sau đó bạn đã thay đổi điều gì?'
    }
  },
  {
    skill: 'Communication',
    text: {
      en: 'Describe a technical decision you disagreed with. How did you handle it?',
      vi: 'Hãy mô tả một quyết định kỹ thuật mà bạn không đồng ý. Bạn đã xử lý tình huống đó thế nào?'
    }
  },
  {
    skill: 'Prioritization',
    text: {
      en: 'How do you balance paying down technical debt against delivering new features?',
      vi: 'Bạn cân bằng giữa việc xử lý nợ kỹ thuật và phát triển tính năng mới như thế nào?'
    }
  },
  {
    skill: 'Learning',
    text: {
      en: 'Tell me about a time you had to learn a new technology quickly to deliver a project.',
      vi: 'Hãy kể về một lần bạn phải học nhanh một công nghệ mới để kịp hoàn thành dự án.'
    }
  },
  {
    skill: 'Code Review',
    text: {
      en: 'What does a good code review look like to you, both as the author and as the reviewer?',
      vi: 'Theo bạn, một buổi code review tốt trông như thế nào, ở cả vai trò người viết code và người review?'
    }
  }
];

// ── Feedback copy ─────────────────────────────────────────────────────────────

/** [no answer, weak, partial, solid, strong] */
const ANSWER_FEEDBACK: Record<Lang, string[]> = {
  en: [
    'No answer was submitted for this question.',
    'The answer is too brief to demonstrate understanding. Describe the concept, how you applied it, and the outcome.',
    'The answer points in the right direction but lacks depth. Explain your reasoning step by step and support it with an example.',
    'Solid answer that covers the main points. Add a concrete example from your experience and discuss edge cases.',
    'Clear, well-structured answer with concrete examples. Calling out the trade-offs explicitly would make it excellent.'
  ],
  vi: [
    'Chưa có câu trả lời cho câu hỏi này.',
    'Câu trả lời quá ngắn để thể hiện sự hiểu biết. Hãy mô tả khái niệm, cách bạn áp dụng và kết quả đạt được.',
    'Câu trả lời đúng hướng nhưng còn thiếu chiều sâu. Hãy giải thích lập luận từng bước và minh hoạ bằng ví dụ.',
    'Câu trả lời ổn, bao quát các ý chính. Hãy bổ sung ví dụ cụ thể từ kinh nghiệm của bạn và bàn thêm về các trường hợp biên.',
    'Câu trả lời rõ ràng, có cấu trúc và ví dụ cụ thể. Nếu nêu rõ các đánh đổi (trade-off) thì sẽ rất xuất sắc.'
  ]
};

/** [needs work, partial, good, strong] */
const SKILL_FEEDBACK: Record<Lang, string[]> = {
  en: [
    'Needs significant work — start again from the fundamentals.',
    'Partial understanding — revisit the core concepts and practise explaining them.',
    'Good foundation — deepen it with real-world examples and edge cases.',
    'Strong command of this area — keep it sharp with more advanced scenarios.'
  ],
  vi: [
    'Cần cải thiện nhiều — hãy bắt đầu lại từ kiến thức nền tảng.',
    'Hiểu một phần — hãy ôn lại khái niệm cốt lõi và luyện cách diễn giải.',
    'Nền tảng tốt — hãy đào sâu bằng ví dụ thực tế và các trường hợp biên.',
    'Nắm vững lĩnh vực này — hãy duy trì bằng các tình huống nâng cao hơn.'
  ]
};

// ── Storage ───────────────────────────────────────────────────────────────────

let cache: MockSession[] | null = null;

function load(): MockSession[] {
  if (cache) return cache;
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    cache = raw ? (JSON.parse(raw) as MockSession[]) : [];
  } catch {
    cache = [];
  }
  return cache;
}

function save(sessions: MockSession[]): void {
  cache = sessions;
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(sessions));
  } catch {
    // Storage unavailable — sessions stay in memory for this page load only
  }
}

// ── Sessions ──────────────────────────────────────────────────────────────────

export function createMockSession(request: CreateSessionRequest): MockSession {
  const sessions = load();
  const now = new Date().toISOString();
  const lang: Lang = request.language === 'Vietnamese' ? 'vi' : 'en';
  const topic = request.topic?.trim() || 'Chung';
  const id = sessions.reduce((max, s) => Math.max(max, s.id), MOCK_SESSION_ID_BASE - 1) + 1;
  const templates = pickTemplates(request);
  const count = Math.max(1, Math.min(request.question_count || 5, templates.length));

  const session: MockSession = {
    id,
    topic,
    difficulty: request.difficulty ?? Difficulty.MID,
    status: SessionStatus.CREATED,
    lang,
    questions: templates.slice(0, count).map((t, i) => ({
      id: `mock-q-${id}-${i + 1}`,
      skill: t.skill.replace(/\{topic\}/g, topic),
      text: t.text[lang].replace(/\{topic\}/g, topic)
    })),
    answers: [],
    currentIndex: -1,
    createdAt: now,
    startedAt: null,
    endedAt: null
  };
  save([session, ...sessions]);
  return session;
}

function pickTemplates(request: CreateSessionRequest): QuestionTemplate[] {
  if (request.question_source !== 'resume') return [...TOPIC_TECHNICAL, ...BEHAVIORAL];
  if (request.question_type === 'behavioral') return [...BEHAVIORAL, ...RESUME_TECHNICAL];
  if (request.question_type === 'mixed') return interleave(RESUME_TECHNICAL, BEHAVIORAL);
  return [...RESUME_TECHNICAL, ...BEHAVIORAL];
}

function interleave<T>(a: T[], b: T[]): T[] {
  const result: T[] = [];
  for (let i = 0; i < Math.max(a.length, b.length); i++) {
    if (i < a.length) result.push(a[i]);
    if (i < b.length) result.push(b[i]);
  }
  return result;
}

export function getMockSession(id: number): MockSession | undefined {
  return load().find(s => s.id === id);
}

export function listMockSessions(): MockSession[] {
  return [...load()];
}

export function saveMockSession(session: MockSession): void {
  save(load().map(s => (s.id === session.id ? session : s)));
}

export function deleteMockSession(id: number): void {
  save(load().filter(s => s.id !== id));
}

export function endMockSession(session: MockSession): void {
  if (session.status === SessionStatus.COMPLETED) return;
  session.status = SessionStatus.COMPLETED;
  session.endedAt = new Date().toISOString();
  saveMockSession(session);
}

export function toSession(s: MockSession): InterviewSession {
  return {
    id: s.id,
    topic: s.topic,
    difficulty: s.difficulty,
    status: s.status,
    started_at: s.startedAt,
    ended_at: s.endedAt,
    created_at: s.createdAt
  };
}

export function toSessionDetail(s: MockSession): InterviewSessionDetail {
  return {
    ...toSession(s),
    questions: askedQuestions(s).map((q, i) => ({ question_id: q.id, question_text: q.text, question_order: i + 1 })),
    answers: s.answers
  };
}

function askedQuestions(s: MockSession): MockQuestion[] {
  if (s.status === SessionStatus.CREATED) return [];
  return s.questions.slice(0, Math.min(s.questions.length, s.currentIndex + 1));
}

// ── Grading & report ──────────────────────────────────────────────────────────

export function isGraded(s: MockSession): boolean {
  return s.status === SessionStatus.COMPLETED
    && !!s.endedAt
    && Date.now() - Date.parse(s.endedAt) >= GRADING_DURATION_MS;
}

export function reportIdFor(sessionId: number): string {
  return `mock-report-${sessionId}`;
}

export function sessionIdFromReportId(reportId: string): number {
  return Number(reportId.replace('mock-report-', ''));
}

export function toGrading(s: MockSession, userId: number): GradingResponse {
  const graded = isGraded(s);
  const results = graded ? gradeQuestions(s) : [];
  return {
    id: `mock-grading-${s.id}`,
    session_id: String(s.id),
    user_id: String(userId),
    topic: s.topic,
    status: graded ? 'COMPLETED' : 'PROCESSING',
    total_questions: askedQuestions(s).length,
    total_score: round1(results.reduce((sum, r) => sum + r.score, 0)),
    max_score: askedQuestions(s).length * MAX_SCORE,
    results,
    created_at: s.endedAt ?? s.createdAt
  };
}

export function toReport(s: MockSession, userId: number): ReportResponse {
  const results = gradeQuestions(s);
  const skills = skillScores(s, results);
  const finishedAt = s.endedAt ?? new Date().toISOString();
  return {
    id: reportIdFor(s.id),
    session_id: String(s.id),
    user_id: userId,
    topic: s.topic,
    total_score: round1(results.reduce((sum, r) => sum + r.score, 0)),
    max_score: results.length * MAX_SCORE,
    summary: summaryFor(s, results, skills),
    roadmap_json: roadmapFor(s, skills),
    total_questions: results.length,
    status: ReportStatus.COMPLETED,
    skill_scores: skills,
    question_scores: results.map((r, i) => ({ ...r, id: `mock-qs-${s.id}-${i + 1}`, created_at: finishedAt })),
    created_at: finishedAt,
    last_updated_at: finishedAt
  };
}

function gradeQuestions(s: MockSession): GradingResultResponse[] {
  return askedQuestions(s).map((q, i) => {
    const answer = s.answers.find(a => a.question_id === q.id)?.answer_text ?? '';
    const score = scoreAnswer(answer, i);
    return {
      question_id: q.id,
      question_text: q.text,
      answer_text: answer,
      skill_name: q.skill,
      score,
      max_score: MAX_SCORE,
      feedback: answerFeedback(score, s.lang)
    };
  });
}

/** Longer answers score higher, with a little per-question variation; rounded to 0.5. */
function scoreAnswer(answer: string, index: number): number {
  const words = answer.trim().split(/\s+/).filter(Boolean).length;
  if (words === 0) return 0;
  if (words < 5) return 2.5;
  const raw = 5 + Math.min(words, 60) / 15 + ((index % 3) - 1) * 0.5;
  return Math.round(Math.min(9.5, Math.max(2, raw)) * 2) / 2;
}

function answerFeedback(score: number, lang: Lang): string {
  const bands = ANSWER_FEEDBACK[lang];
  if (score === 0) return bands[0];
  if (score >= 8) return bands[4];
  if (score >= 6) return bands[3];
  if (score >= 4) return bands[2];
  return bands[1];
}

function skillScores(s: MockSession, results: GradingResultResponse[]): SkillScoreResponse[] {
  const groups = new Map<string, GradingResultResponse[]>();
  results.forEach(r => groups.set(r.skill_name, [...(groups.get(r.skill_name) ?? []), r]));

  return [...groups.entries()]
    .map(([skillName, items], i) => {
      const score = round1(items.reduce((sum, r) => sum + r.score, 0));
      const average = score / items.length;
      const bands = SKILL_FEEDBACK[s.lang];
      return {
        id: `mock-skill-score-${s.id}-${i + 1}`,
        skill_name: skillName,
        score,
        max_score: items.length * MAX_SCORE,
        question_count: items.length,
        feedback: average >= 8 ? bands[3] : average >= 6 ? bands[2] : average >= 4 ? bands[1] : bands[0]
      };
    })
    .sort((a, b) => b.score / b.max_score - a.score / a.max_score);
}

function summaryFor(s: MockSession, results: GradingResultResponse[], skills: SkillScoreResponse[]): string {
  const answered = results.filter(r => r.answer_text.trim()).length;
  if (answered === 0) {
    return s.lang === 'vi'
      ? 'Bạn chưa trả lời câu hỏi nào trong phiên phỏng vấn này.'
      : 'No questions were answered in this session.';
  }
  const average = round1(results.reduce((sum, r) => sum + r.score, 0) / results.length);
  const best = skills[0].skill_name;
  const weakest = skills[skills.length - 1].skill_name;
  return s.lang === 'vi'
    ? `Bạn đã trả lời ${answered}/${results.length} câu hỏi về "${s.topic}" với điểm trung bình ${average}/10. `
      + `Thế mạnh nổi bật: ${best}. Cần tập trung cải thiện: ${weakest}.`
    : `You answered ${answered} of ${results.length} questions on "${s.topic}" with an average score of ${average}/10. `
      + `Strongest area: ${best}. Focus next on: ${weakest}.`;
}

function roadmapFor(s: MockSession, skills: SkillScoreResponse[]): Record<string, string> {
  const weakest = [...skills].reverse().map(k => k.skill_name);
  const focus = weakest[0] ?? s.topic;
  const second = weakest[1];

  if (s.lang === 'vi') {
    return {
      'Tuần 1–2': `Ôn lại kiến thức nền tảng về ${focus} và tự viết ghi chú giải thích từng khái niệm bằng lời của bạn.`,
      'Tuần 3–4': `Làm một dự án nhỏ áp dụng ${focus}${second ? ` và ${second}` : ''}, sau đó ghi lại các đánh đổi bạn đã chọn.`,
      'Tuần 5–6': `Luyện trả lời thành tiếng các câu hỏi phỏng vấn về ${s.topic} theo mô hình STAR, mỗi câu 2–3 phút.`
    };
  }
  return {
    'Week 1–2': `Review the fundamentals of ${focus} and write short notes explaining each concept in your own words.`,
    'Week 3–4': `Build a small project that applies ${focus}${second ? ` and ${second}` : ''}, then write up the trade-offs you made.`,
    'Week 5–6': `Practise answering ${s.topic} interview questions out loud using the STAR format, 2–3 minutes per answer.`
  };
}

function round1(value: number): number {
  return Math.round(value * 10) / 10;
}
