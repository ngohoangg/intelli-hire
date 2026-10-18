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
  { id: 'mock-skill-java', name: 'Java', category: 'Ngôn ngữ lập trình', type: 'TECHNICAL' },
  { id: 'mock-skill-spring-boot', name: 'Spring Boot', category: 'Framework', type: 'TECHNICAL' },
  { id: 'mock-skill-postgresql', name: 'PostgreSQL', category: 'Cơ sở dữ liệu', type: 'TECHNICAL' },
  { id: 'mock-skill-redis', name: 'Redis', category: 'Cơ sở dữ liệu', type: 'TECHNICAL' },
  { id: 'mock-skill-kafka', name: 'Apache Kafka', category: 'Hàng đợi thông điệp', type: 'TECHNICAL' },
  { id: 'mock-skill-microservices', name: 'Microservices', category: 'Kiến trúc', type: 'TECHNICAL' },
  { id: 'mock-skill-angular', name: 'Angular', category: 'Framework', type: 'TECHNICAL' },
  { id: 'mock-skill-docker', name: 'Docker', category: 'DevOps', type: 'TOOL' },
  { id: 'mock-skill-git', name: 'Git', category: 'Quản lý phiên bản', type: 'TOOL' },
  { id: 'mock-skill-teamwork', name: 'Làm việc nhóm', category: 'Kỹ năng mềm', type: 'SOFT' },
  { id: 'mock-skill-communication', name: 'Giao tiếp', category: 'Kỹ năng mềm', type: 'SOFT' }
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
      other_info: 'Sẵn sàng làm việc kết hợp hoặc từ xa'
    },
    experiences: [
      {
        ...base,
        id: 'mock-exp-1',
        company: 'TechNova Solutions',
        title: 'Senior Java Backend Developer',
        start_date: '2022-03-01',
        is_current: true,
        description: 'Thiết kế và duy trì các microservice Spring Boot cho nền tảng tuyển dụng phục vụ hơn 200.000 người dùng. '
          + 'Đưa vào pipeline sự kiện dùng Kafka giúp giảm 60% thời gian tạo báo cáo. Kèm cặp 4 lập trình viên junior.'
      },
      {
        ...base,
        id: 'mock-exp-2',
        company: 'BlueWave Fintech',
        title: 'Java Developer',
        start_date: '2019-07-01',
        end_date: '2022-02-01',
        is_current: false,
        description: 'Xây dựng các API đối soát thanh toán với Spring Boot và PostgreSQL. '
          + 'Bổ sung cache Redis, giảm độ trễ p95 từ 800ms xuống 180ms. Viết integration test với Testcontainers.'
      },
      {
        ...base,
        id: 'mock-exp-3',
        company: 'CodeSpark Studio',
        title: 'Software Engineer Intern',
        start_date: '2019-01-01',
        end_date: '2019-06-01',
        is_current: false,
        description: 'Phát triển dashboard quản trị nội bộ bằng Angular, gọi các REST API.'
      }
    ],
    educations: [
      {
        ...base,
        id: 'mock-edu-1',
        school: 'Đại học Bách khoa Hà Nội',
        degree: 'Kỹ sư',
        field: 'Khoa học máy tính',
        start_year: 2015,
        end_year: 2019,
        description: 'GPA 3.4/4.0. Đồ án tốt nghiệp: hệ thống lên lịch phỏng vấn theo thời gian thực.'
      }
    ],
    skills: [
      skill(1, 'mock-skill-java', 'EXPERT', 5.5, 0.95, true,
        'Senior Java Backend Developer tại TechNova Solutions; Java Developer tại BlueWave Fintech từ năm 2019.'),
      skill(2, 'mock-skill-spring-boot', 'ADVANCED', 5, 0.92, true,
        'Thiết kế và duy trì các microservice Spring Boot cho nền tảng tuyển dụng phục vụ hơn 200.000 người dùng.'),
      skill(3, 'mock-skill-postgresql', 'ADVANCED', 4.5, 0.86, true,
        'Xây dựng các API đối soát thanh toán với Spring Boot và PostgreSQL.'),
      skill(4, 'mock-skill-kafka', 'INTERMEDIATE', 2.5, 0.81, false,
        'Đưa vào pipeline sự kiện dùng Kafka giúp giảm 60% thời gian tạo báo cáo.'),
      skill(5, 'mock-skill-redis', 'INTERMEDIATE', 3, 0.78, false,
        'Bổ sung cache Redis, giảm độ trễ p95 từ 800ms xuống 180ms.'),
      skill(6, 'mock-skill-microservices', 'ADVANCED', 3, 0.8, false,
        'Thiết kế và duy trì các microservice Spring Boot.'),
      skill(7, 'mock-skill-docker', 'INTERMEDIATE', 3, 0.72, false,
        'Viết integration test với Testcontainers.'),
      skill(8, 'mock-skill-angular', 'BEGINNER', 0.5, 0.55, false,
        'Phát triển dashboard quản trị nội bộ bằng Angular, gọi các REST API.'),
      skill(9, 'mock-skill-teamwork', 'ADVANCED', 3, 0.7, false,
        'Kèm cặp 4 lập trình viên junior.')
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
      summary: 'Kỹ sư backend với hơn 5 năm kinh nghiệm xây dựng microservice Java/Spring Boot, '
        + 'hệ thống hướng sự kiện với Kafka và các REST API lưu lượng cao trên PostgreSQL và Redis. '
        + 'Từng kèm cặp lập trình viên junior và chịu trách nhiệm dịch vụ từ đầu đến cuối.',
      signals: {
        signal_1: 'Kèm cặp nhóm 4 lập trình viên junior',
        signal_2: 'Giảm khoảng 75% độ trễ p95 của API nhờ cache Redis',
        signal_3: 'Vận hành pipeline sự kiện Kafka trên production',
        signal_4: 'Thăng tiến đều đặn từ thực tập sinh lên kỹ sư cao cấp'
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
        feedback: 'Đầy đủ và chuyên nghiệp: họ tên, email, số điện thoại, địa điểm và LinkedIn đều có và dễ tìm.'
      },
      experience: {
        score: 8.6,
        feedback: 'Các gạch đầu dòng hướng đến kết quả, có tác động đo lường được (báo cáo nhanh hơn 60%, độ trễ p95 800ms → 180ms). '
          + 'Bổ sung quy mô nhóm và công nghệ sử dụng cho từng vị trí sẽ làm lộ trình phát triển rõ ràng hơn.'
      },
      education: {
        score: 7.4,
        feedback: 'Bằng Khoa học máy tính phù hợp, có nêu GPA và đồ án tốt nghiệp. '
          + 'Các chứng chỉ như Oracle Certified Java Programmer hoặc AWS Developer sẽ làm mục này mạnh hơn.'
      },
      skills: {
        score: 8.1,
        feedback: 'Bộ công nghệ backend cốt lõi (Java, Spring Boot, PostgreSQL, Kafka, Redis) được chứng minh rõ trong phần kinh nghiệm. '
          + 'Chưa nhắc đến nền tảng đám mây và công cụ CI/CD.'
      },
      presentation: {
        score: 7.2,
        feedback: 'Bố cục rõ ràng, định dạng nhất quán. Phần tóm tắt nên ngắn hơn và điều chỉnh theo vị trí ứng tuyển; '
          + 'nên giữ CV trong hai trang.'
      }
    },
    strengths: [
      'Thành tích được định lượng, thể hiện tác động kinh doanh thực tế',
      'Lộ trình sự nghiệp rõ ràng từ thực tập sinh đến lập trình viên backend cao cấp',
      'Kinh nghiệm thực tế trên production với microservice hướng sự kiện (Kafka)',
      'Kinh nghiệm kèm cặp cho thấy sẵn sàng theo lộ trình tech lead'
    ],
    weaknesses: [
      'Chưa nêu kinh nghiệm với nền tảng đám mây (AWS, GCP hoặc Azure)',
      'Thực hành CI/CD, giám sát và kiểm thử chỉ được nhắc đến sơ lược',
      'Chưa có chứng chỉ hoặc đóng góp mã nguồn mở',
      'Kỹ năng Angular được liệt kê nhưng chỉ dựa trên công việc thực tập'
    ],
    priority_actions: [
      'Bổ sung các công cụ đám mây và CI/CD bạn đã dùng (ví dụ AWS, Docker Compose, GitHub Actions, Jenkins)',
      'Thêm một hoặc hai dòng về quy mô hệ thống cho mỗi vị trí (số request mỗi giây, khối lượng dữ liệu, quy mô nhóm)',
      'Rút gọn phần tóm tắt còn 2–3 dòng, phù hợp với vị trí Senior Backend mục tiêu',
      'Hoặc bỏ Angular, hoặc thêm một dự án gần đây thể hiện kỹ năng này'
    ],
    generated_at: now,
    created_at: now,
    updated_at: now
  };
}
