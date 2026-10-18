import { HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { CreateSessionRequest, InterviewSession, PageResponse } from '../models/interview.model';
import { apiPath, MockResult, notFound, ok, patchData } from './mock-http.utils';
import {
  createMockSession,
  deleteMockSession,
  endMockSession,
  getMockSession,
  isGraded,
  listMockSessions,
  sessionIdFromReportId,
  toGrading,
  toReport,
  toSession,
  toSessionDetail
} from './interview-mock.store';

/**
 * TEMPORARY — fakes the interview flow for UI demos/screenshots: creating a session, the grading
 * poll after it ends, the session detail page and its report. Mock sessions get ids from 900001 up
 * and live in sessionStorage; real sessions still go to the backend. The room's WebSocket is
 * replaced by MockInterviewWebSocketService.
 *
 * Toggle with `environment.mockInterview`; delete the interview mock files once the real flow works.
 */

const CREATE_LATENCY_MS = 1200;
const REGENERATE_LATENCY_MS = 1500;

export const interviewMockInterceptor: HttpInterceptorFn = (req, next) => {
  const path = apiPath(req.url);
  const mocked = path ? route(req, next, path) : null;
  return mocked ?? next(req);
};

function route(req: HttpRequest<unknown>, next: HttpHandlerFn, path: string): MockResult {
  const { method } = req;
  let m: RegExpExecArray | null;

  // Sessions
  if (path === '/interviews/sessions') {
    if (method === 'POST') {
      return ok(toSession(createMockSession(req.body as CreateSessionRequest)), 201, CREATE_LATENCY_MS);
    }
    if (method === 'GET') {
      const firstPage = (req.params.get('page') ?? '0') === '0';
      return patchData(req, next, (page: PageResponse<InterviewSession>) => (firstPage ? withMockSessions(page) : page));
    }
    return null;
  }
  if ((m = /^\/interviews\/sessions\/(\d+)(\/end)?$/.exec(path))) {
    const session = getMockSession(Number(m[1]));
    if (!session) return null;
    const isEnd = !!m[2];
    if (method === 'GET' && !isEnd) return ok(toSessionDetail(session));
    if (method === 'PUT' && isEnd) {
      endMockSession(session);
      return ok(null);
    }
    if (method === 'DELETE' && !isEnd) {
      deleteMockSession(session.id);
      return ok(null);
    }
    return null;
  }

  // Grading poll
  if (method === 'GET' && (m = /^\/gradings\/session\/(\d+)$/.exec(path))) {
    const session = getMockSession(Number(m[1]));
    return session ? ok(toGrading(session, userIdOf(req))) : null;
  }

  // Reports
  if ((m = /^\/reports\/session\/(\d+)$/.exec(path))) {
    const session = getMockSession(Number(m[1]));
    if (!session) return null;
    if (method === 'GET') {
      return isGraded(session) ? ok(toReport(session, userIdOf(req))) : notFound(req, 'Không tìm thấy báo cáo');
    }
    if (method === 'DELETE') return ok(null);
    return null;
  }
  if ((m = /^\/reports\/(mock-report-\d+)(\/regenerate)?$/.exec(path))) {
    const session = getMockSession(sessionIdFromReportId(m[1]));
    if (!session) return notFound(req, 'Không tìm thấy báo cáo');
    const isRegenerate = !!m[2];
    if (method === 'GET' && !isRegenerate) return ok(toReport(session, userIdOf(req)));
    if (method === 'POST' && isRegenerate) return ok(toReport(session, userIdOf(req)), 200, REGENERATE_LATENCY_MS);
    return null;
  }

  return null;
}

function withMockSessions(page: PageResponse<InterviewSession>): PageResponse<InterviewSession> {
  const mockSessions = listMockSessions().map(toSession);
  return {
    ...page,
    content: [...mockSessions, ...(page?.content ?? [])],
    total_elements: (page?.total_elements ?? 0) + mockSessions.length
  };
}

function userIdOf(req: HttpRequest<unknown>): number {
  return Number(req.headers.get('X-User-Id')) || 0;
}
