import { HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { CreateParseJobRequest, JobStatus, JobType, ParseJob } from '../models/parse-job.model';
import { Resume, ResumeStatus } from '../models/resume.model';
import { ResumeContact } from '../models/contact.model';
import { ResumeSkill, ResumeSkillProfile } from '../models/skill.model';
import { buildMockResumeData, buildMockReview, MOCK_SKILL_CATALOG, MockResumeData } from './cv-parse-mock.data';
import { apiPath, MockResult, newId, notFound, ok, patchData } from './mock-http.utils';

/**
 * TEMPORARY — fakes the CV parsing flow for UI demos/screenshots.
 *
 * resume-service's parse worker currently fails (skills.type = 'HARD' is not a SkillType),
 * so this interceptor simulates parse jobs and serves parsed data (contact, experiences,
 * educations, skills, skill profile, AI review, skill catalog) for resumes whose parse was attempted.
 * File upload/download still hits the real backend.
 *
 * Toggle with `environment.mockCvParse`; delete the CV mock files once the backend is fixed.
 */

const QUEUED_MS = 1500;
const PARSE_DURATION_MS = 8000;
const GENERATE_LATENCY_MS = 1500;
const REVIEW_LATENCY_MS = 3500;

interface FakeJob {
  id: string;
  resumeId: string;
  jobType: JobType;
  startedAt: number;
  canceledProgress: number | null;
}

interface ResumeChild {
  id: string;
  resume_id: string;
  created_at: string;
  updated_at: string;
}

const jobs = new Map<string, FakeJob>();
const parsedResumes = new Map<string, MockResumeData>();

export const cvParseMockInterceptor: HttpInterceptorFn = (req, next) => {
  const path = apiPath(req.url);
  const mocked = path ? route(req, next, path) : null;
  return mocked ?? next(req);
};

function route(req: HttpRequest<unknown>, next: HttpHandlerFn, path: string): MockResult {
  const { method } = req;
  let m: RegExpExecArray | null;

  // Skill catalog — the real endpoint returns 500 because of the same enum mismatch
  if (method === 'GET' && path === '/skills') return ok(MOCK_SKILL_CATALOG);
  if (method === 'GET' && path === '/skills/search') {
    const q = (req.params.get('q') ?? '').toLowerCase();
    return ok(MOCK_SKILL_CATALOG.filter(s => s.name.toLowerCase().includes(q)));
  }
  if (method === 'GET' && (m = /^\/skills\/([^/]+)$/.exec(path))) {
    const id = m[1];
    const skill = MOCK_SKILL_CATALOG.find(s => s.id === id);
    return skill ? ok(skill) : null;
  }

  // Parse jobs
  if (method === 'POST' && path === '/parse-jobs') {
    const body = req.body as CreateParseJobRequest;
    return ok(toParseJob(registerJob(newId('mock-job'), body.resume_id, body.job_type)), 201);
  }
  if (method === 'PUT' && (m = /^\/parse-jobs\/([^/]+)\/cancel$/.exec(path))) {
    const job = jobs.get(m[1]);
    if (!job) return null;
    const current = toParseJob(job);
    if (current.status === JobStatus.QUEUED || current.status === JobStatus.RUNNING) {
      job.canceledProgress = current.progress;
    }
    return ok(toParseJob(job));
  }
  if (method === 'GET' && (m = /^\/parse-jobs\/([^/]+)$/.exec(path))) {
    const job = jobs.get(m[1]);
    if (job) return ok(toParseJob(job));
    // Job created by the real backend (e.g. ?jobId= in the URL): keep its ids, fake the progress
    return patchData(req, next, (real: ParseJob) => toParseJob(registerJob(real.id, real.resume_id, real.job_type)));
  }

  // Resumes — real data, faked parse status
  if (method === 'GET' && path === '/resumes') {
    return patchData(req, next, (list: Resume[]) => list.map(withFakeStatus));
  }
  if (method === 'GET' && /^\/resumes\/[^/]+$/.test(path)) {
    return patchData(req, next, withFakeStatus);
  }
  if (method === 'POST' && (m = /^\/resumes\/([^/]+)\/reprocess$/.exec(path))) {
    registerJob(newId('mock-job'), m[1], JobType.REPARSE);
    // Skip the real reprocess (its worker would fail again); return the resume as PARSING
    const getResume = req.clone({ method: 'GET', url: req.url.replace(/\/reprocess$/, ''), body: null });
    return patchData(getResume, next, (r: Resume) => ({ ...r, status: ResumeStatus.PARSING }));
  }

  // Parsed data for resumes
  if ((m = /^\/resumes\/([^/]+)\/(contact|experiences|educations|skills|skill-profile|review)(?:\/([^/]+))?$/.exec(path))) {
    const data = parsedResumes.get(m[1]);
    return data ? resumeChild(req, data, m[2], m[3]) : null;
  }

  return null;
}

function resumeChild(req: HttpRequest<unknown>, data: MockResumeData, kind: string, subId?: string): MockResult {
  const { method } = req;
  const now = new Date().toISOString();

  switch (kind) {
    case 'contact':
      if (subId) return null;
      if (method === 'GET') return ok(data.contact);
      if (method === 'PUT') {
        data.contact = { ...data.contact, ...(req.body as Partial<ResumeContact>), updated_at: now };
        return ok(data.contact);
      }
      return null;
    case 'skill-profile':
      if (method === 'GET' && !subId) return ok(data.profile);
      if (method === 'PUT' && !subId) {
        data.profile = { ...data.profile, ...(req.body as Partial<ResumeSkillProfile>), updated_at: now };
        return ok(data.profile);
      }
      if (method === 'POST' && subId === 'generate') {
        data.profile = { ...data.profile, generated_at: now, updated_at: now };
        return ok(data.profile, 200, GENERATE_LATENCY_MS);
      }
      return null;
    case 'experiences':
      return crud(req, data.experiences, data.resumeId, subId);
    case 'educations':
      return crud(req, data.educations, data.resumeId, subId);
    case 'skills':
      return crud(req, data.skills, data.resumeId, subId, withSkillName);
    case 'review':
      if (method === 'GET' && !subId) {
        return data.review ? ok(data.review) : notFound(req, 'Không tìm thấy đánh giá CV');
      }
      if (method === 'POST' && subId === 'generate') {
        data.review = buildMockReview(data.resumeId);
        return ok(data.review, 200, REVIEW_LATENCY_MS);
      }
      return null;
    default:
      return null;
  }
}

function crud<T extends ResumeChild>(
  req: HttpRequest<unknown>,
  list: T[],
  resumeId: string,
  subId: string | undefined,
  enrich: (item: T) => T = item => item
): MockResult {
  const now = new Date().toISOString();
  const body = req.body as Partial<T>;

  if (!subId) {
    if (req.method === 'GET') return ok(list);
    if (req.method === 'POST') {
      const created = enrich({ ...body, id: newId('mock'), resume_id: resumeId, created_at: now, updated_at: now } as T);
      list.push(created);
      return ok(created, 201);
    }
    return null;
  }

  const index = list.findIndex(item => item.id === subId);
  if (index < 0) return null;
  if (req.method === 'PUT') {
    list[index] = enrich({ ...list[index], ...body, updated_at: now });
    return ok(list[index]);
  }
  if (req.method === 'DELETE') {
    list.splice(index, 1);
    return ok(null);
  }
  return null;
}

function withSkillName(skill: ResumeSkill): ResumeSkill {
  const catalogSkill = MOCK_SKILL_CATALOG.find(s => s.id === skill.skill_id);
  return catalogSkill ? { ...skill, skill_name: catalogSkill.name } : skill;
}

function registerJob(id: string, resumeId: string, jobType: JobType): FakeJob {
  let job = jobs.get(id);
  if (!job) {
    job = { id, resumeId, jobType, startedAt: Date.now(), canceledProgress: null };
    jobs.set(id, job);
  }
  return job;
}

function toParseJob(job: FakeJob): ParseJob {
  const elapsed = Date.now() - job.startedAt;
  let status: JobStatus;
  let progress: number;

  if (job.canceledProgress !== null) {
    status = JobStatus.CANCELED;
    progress = job.canceledProgress;
  } else if (elapsed < QUEUED_MS) {
    status = JobStatus.QUEUED;
    progress = 0;
  } else if (elapsed < PARSE_DURATION_MS) {
    status = JobStatus.RUNNING;
    progress = Math.min(99, Math.max(5, Math.round(((elapsed - QUEUED_MS) / (PARSE_DURATION_MS - QUEUED_MS)) * 100)));
  } else {
    status = JobStatus.SUCCEEDED;
    progress = 100;
    markParsed(job.resumeId);
  }

  return {
    id: job.id,
    resume_id: job.resumeId,
    status,
    job_type: job.jobType,
    progress,
    retry_count: 0,
    started_at: elapsed >= QUEUED_MS ? new Date(job.startedAt + QUEUED_MS).toISOString() : undefined,
    finished_at: status === JobStatus.SUCCEEDED ? new Date(job.startedAt + PARSE_DURATION_MS).toISOString() : undefined,
    created_at: new Date(job.startedAt).toISOString(),
    updated_at: new Date().toISOString()
  };
}

function withFakeStatus(resume: Resume): Resume {
  const resumeJobs = [...jobs.values()].filter(j => j.resumeId === resume.id).map(toParseJob);
  if (resumeJobs.some(j => j.status === JobStatus.QUEUED || j.status === JobStatus.RUNNING)) {
    return { ...resume, status: ResumeStatus.PARSING };
  }
  // A parse was attempted (real worker failed or is stuck) or a fake job finished → show as parsed
  if (parsedResumes.has(resume.id) || resume.status === ResumeStatus.FAILED || resume.status === ResumeStatus.PARSING) {
    markParsed(resume.id);
    return { ...resume, status: ResumeStatus.COMPLETED };
  }
  return resume;
}

function markParsed(resumeId: string): void {
  if (!parsedResumes.has(resumeId)) {
    parsedResumes.set(resumeId, buildMockResumeData(resumeId));
  }
}
