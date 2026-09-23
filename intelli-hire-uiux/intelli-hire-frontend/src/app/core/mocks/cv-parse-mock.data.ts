import { ResumeContact } from '../models/contact.model';
import { ResumeEducation } from '../models/education.model';
import { ResumeExperience } from '../models/experience.model';
import { ResumeSkill, ResumeSkillProfile, Skill } from '../models/skill.model';
import { ResumeReview } from '../models/resume-review.model';

/** TEMPORARY sample data for cvParseMockInterceptor — see cv-parse-mock.interceptor.ts */

export interface MockResumeData {
  resumeId: string;
  contact: ResumeContact;
  experiences: ResumeExperience[];
  educations: ResumeEducation[];
  skills: ResumeSkill[];
  profile: ResumeSkillProfile;
  review: ResumeReview | null;
}

export const MOCK_SKILL_CATALOG: Skill[] = [
  { id: 'mock-skill-java', name: 'Java', category: 'Programming Language', type: 'TECHNICAL' },
  { id: 'mock-skill-spring-boot', name: 'Spring Boot', category: 'Framework', type: 'TECHNICAL' },
  { id: 'mock-skill-postgresql', name: 'PostgreSQL', category: 'Database', type: 'TECHNICAL' },
  { id: 'mock-skill-redis', name: 'Redis', category: 'Database', type: 'TECHNICAL' },
  { id: 'mock-skill-kafka', name: 'Apache Kafka', category: 'Messaging', type: 'TECHNICAL' },
  { id: 'mock-skill-microservices', name: 'Microservices', category: 'Architecture', type: 'TECHNICAL' },
  { id: 'mock-skill-angular', name: 'Angular', category: 'Framework', type: 'TECHNICAL' },
  { id: 'mock-skill-docker', name: 'Docker', category: 'DevOps', type: 'TOOL' },
  { id: 'mock-skill-git', name: 'Git', category: 'Version Control', type: 'TOOL' },
  { id: 'mock-skill-teamwork', name: 'Teamwork', category: 'Soft Skill', type: 'SOFT' },
  { id: 'mock-skill-communication', name: 'Communication', category: 'Soft Skill', type: 'SOFT' }
];

export function buildMockResumeData(resumeId: string): MockResumeData {
  const now = new Date().toISOString();
  const base = { resume_id: resumeId, created_at: now, updated_at: now };

  const skill = (
    n: number,
    skillId: string,
    level: string,
    years: number,
    confidence: number,
    isPrimary: boolean,
    evidence: string
  ): ResumeSkill => ({
    ...base,
    id: `mock-rs-${n}`,
    skill_id: skillId,
    skill_name: MOCK_SKILL_CATALOG.find(s => s.id === skillId)?.name,
    proficiency_level: level,
    years_experience: years,
    confidence_score: confidence,
    evidence_text: evidence,
    is_primary: isPrimary
  });

  return {
    resumeId,
    contact: {
      ...base,
      id: 'mock-contact-1',
      full_name: 'Bùi Thị Trang',
      email: 'buithitrang.dev@example.com',
      phone: '+84 912 345 678',
      location: 'Hà Nội, Việt Nam',
      linkedin_url: 'https://www.linkedin.com/in/bui-thi-trang-dev',
      other_info: 'Open to hybrid or remote roles'
    },
    experiences: [
      {
        ...base,
        id: 'mock-exp-1',
        company: 'TechNova Solutions',
        title: 'Senior Java Backend Developer',
        start_date: '2022-03-01',
        is_current: true,
        description: 'Designed and maintained Spring Boot microservices for a recruitment platform serving 200k+ users. '
          + 'Introduced a Kafka-based event pipeline that cut report generation time by 60%. Mentored 4 junior developers.'
      },
      {
        ...base,
        id: 'mock-exp-2',
        company: 'BlueWave Fintech',
        title: 'Java Developer',
        start_date: '2019-07-01',
        end_date: '2022-02-01',
        is_current: false,
        description: 'Built payment reconciliation APIs with Spring Boot and PostgreSQL. '
          + 'Added Redis caching, reducing p95 latency from 800ms to 180ms. Wrote integration tests with Testcontainers.'
      },
      {
        ...base,
        id: 'mock-exp-3',
        company: 'CodeSpark Studio',
        title: 'Software Engineer Intern',
        start_date: '2019-01-01',
        end_date: '2019-06-01',
        is_current: false,
        description: 'Developed an internal admin dashboard with Angular consuming REST APIs.'
      }
    ],
    educations: [
      {
        ...base,
        id: 'mock-edu-1',
        school: 'Hanoi University of Science and Technology',
        degree: 'Bachelor of Engineering',
        field: 'Computer Science',
        start_year: 2015,
        end_year: 2019,
        description: 'GPA 3.4/4.0. Capstone project: real-time interview scheduling system.'
      }
    ],
    skills: [
      skill(1, 'mock-skill-java', 'EXPERT', 5.5, 0.95, true,
        'Senior Java Backend Developer at TechNova Solutions; Java Developer at BlueWave Fintech since 2019.'),
      skill(2, 'mock-skill-spring-boot', 'ADVANCED', 5, 0.92, true,
        'Designed and maintained Spring Boot microservices for a recruitment platform serving 200k+ users.'),
      skill(3, 'mock-skill-postgresql', 'ADVANCED', 4.5, 0.86, true,
        'Built payment reconciliation APIs with Spring Boot and PostgreSQL.'),
      skill(4, 'mock-skill-kafka', 'INTERMEDIATE', 2.5, 0.81, false,
        'Introduced a Kafka-based event pipeline that cut report generation time by 60%.'),
      skill(5, 'mock-skill-redis', 'INTERMEDIATE', 3, 0.78, false,
        'Added Redis caching, reducing p95 latency from 800ms to 180ms.'),
      skill(6, 'mock-skill-microservices', 'ADVANCED', 3, 0.8, false,
        'Designed and maintained Spring Boot microservices.'),
      skill(7, 'mock-skill-docker', 'INTERMEDIATE', 3, 0.72, false,
        'Wrote integration tests with Testcontainers.'),
      skill(8, 'mock-skill-angular', 'BEGINNER', 0.5, 0.55, false,
        'Developed an internal admin dashboard with Angular consuming REST APIs.'),
      skill(9, 'mock-skill-teamwork', 'ADVANCED', 3, 0.7, false,
        'Mentored 4 junior developers.')
    ],
    profile: {
      ...base,
      id: 'mock-profile-1',
      top_skills: {
        'Java': 0.95,
        'Spring Boot': 0.92,
        'PostgreSQL': 0.86,
        'Apache Kafka': 0.81,
        'Microservices': 0.8,
        'Redis': 0.78
      },
      years_estimated: 5.5,
      seniority: 'SENIOR',
      summary: 'Backend engineer with 5+ years of experience building Java/Spring Boot microservices, '
        + 'event-driven systems with Kafka, and high-traffic REST APIs on PostgreSQL and Redis. '
        + 'Has mentored junior developers and owns services end to end.',
      signals: {
        signal_1: 'Mentored a team of 4 junior developers',
        signal_2: 'Reduced API p95 latency by ~75% with Redis caching',
        signal_3: 'Runs a Kafka event pipeline in production',
        signal_4: 'Steady progression from intern to senior engineer'
      },
      generated_at: now
    },
    review: null
  };
}

export function buildMockReview(resumeId: string): ResumeReview {
  const now = new Date().toISOString();
  return {
    id: 'mock-review-1',
    resume_id: resumeId,
    overall_score: 8.2,
    sections: {
      contact: {
        score: 9.0,
        feedback: 'Complete and professional: name, email, phone, location and LinkedIn are all present and easy to find.'
      },
      experience: {
        score: 8.6,
        feedback: 'Strong, results-oriented bullet points with measurable impact (60% faster reports, p95 latency 800ms → 180ms). '
          + 'Adding team size and the tech stack for each role would make the progression even clearer.'
      },
      education: {
        score: 7.4,
        feedback: 'Relevant Computer Science degree with GPA and capstone project listed. '
          + 'Certifications such as Oracle Certified Java Programmer or AWS Developer would strengthen this section.'
      },
      skills: {
        score: 8.1,
        feedback: 'The core backend stack (Java, Spring Boot, PostgreSQL, Kafka, Redis) is well backed by evidence in the experience section. '
          + 'Cloud platforms and CI/CD tooling are not mentioned.'
      },
      presentation: {
        score: 7.2,
        feedback: 'Clear structure and consistent formatting. The summary could be shorter and tailored to the target role; '
          + 'keep the CV within two pages.'
      }
    },
    strengths: [
      'Quantified achievements that show real business impact',
      'Clear career progression from intern to senior backend developer',
      'Hands-on production experience with event-driven microservices (Kafka)',
      'Mentoring experience that signals readiness for a tech lead track'
    ],
    weaknesses: [
      'No cloud platform (AWS, GCP or Azure) experience listed',
      'CI/CD, monitoring and testing practices are only briefly mentioned',
      'No certifications or open-source contributions',
      'The Angular skill is listed but only backed by internship work'
    ],
    priority_actions: [
      'Add the cloud and CI/CD tools you have used (e.g. AWS, Docker Compose, GitHub Actions, Jenkins)',
      'Add one or two lines on system scale for each role (requests per second, data volume, team size)',
      'Shorten the summary to 2–3 lines aligned with the target Senior Backend role',
      'Either remove Angular or add a recent project that demonstrates it'
    ],
    generated_at: now,
    created_at: now,
    updated_at: now
  };
}
