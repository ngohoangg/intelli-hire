import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { IdleService } from './idle.service';
import { AuthService } from './auth.service';

const IDLE_MS = 30 * 60 * 1000;

describe('IdleService', () => {
  let service: IdleService;
  let authSpy: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    authSpy = jasmine.createSpyObj('AuthService', ['logout']);
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        IdleService,
        { provide: AuthService, useValue: authSpy }
      ]
    });
    service = TestBed.inject(IdleService);
  });

  afterEach(() => {
    service.stop();
    localStorage.clear();
  });

  // ── timer behavior ──────────────────────────────────────────────────────────

  it('logouts after 30 min of inactivity', fakeAsync(() => {
    service.start();
    tick(IDLE_MS);
    expect(authSpy.logout).toHaveBeenCalledTimes(1);
  }));

  it('does NOT logout when resetTimer is called before 30 min elapses', fakeAsync(() => {
    service.start();
    tick(20 * 60 * 1000);
    service.resetTimer();
    tick(20 * 60 * 1000);
    expect(authSpy.logout).not.toHaveBeenCalled();
  }));

  it('logouts 30 min after the last resetTimer call', fakeAsync(() => {
    service.start();
    tick(20 * 60 * 1000);
    service.resetTimer();
    tick(IDLE_MS);
    expect(authSpy.logout).toHaveBeenCalledTimes(1);
  }));

  it('does NOT logout after stop() is called', fakeAsync(() => {
    service.start();
    tick(10 * 60 * 1000);
    service.stop();
    tick(IDLE_MS);
    expect(authSpy.logout).not.toHaveBeenCalled();
  }));

  it('stop() is safe to call multiple times', () => {
    service.start();
    expect(() => { service.stop(); service.stop(); }).not.toThrow();
  });

  it('start() is safe to call multiple times — only one set of listeners', fakeAsync(() => {
    const addSpy = spyOn(window, 'addEventListener').and.callThrough();
    service.start();
    service.start(); // second call must be a no-op
    expect(addSpy).toHaveBeenCalledTimes(4); // 4 events, not 8
    tick(IDLE_MS);
    expect(authSpy.logout).toHaveBeenCalledTimes(1);
  }));

  // ── localStorage persistence ────────────────────────────────────────────────

  it('resetTimer writes lastActivityAt to localStorage', () => {
    const before = Date.now();
    service.resetTimer();
    const stored = Number(localStorage.getItem('lastActivityAt'));
    expect(stored).toBeGreaterThanOrEqual(before);
    expect(stored).toBeLessThanOrEqual(Date.now());
  });

  it('stop() removes lastActivityAt from localStorage', () => {
    service.start();
    service.stop();
    expect(localStorage.getItem('lastActivityAt')).toBeNull();
  });

  it('start() logouts immediately when lastActivityAt is 31 min ago', fakeAsync(() => {
    localStorage.setItem('lastActivityAt', String(Date.now() - (31 * 60 * 1000)));
    service.start();
    tick(0);
    expect(authSpy.logout).toHaveBeenCalledTimes(1);
  }));

  it('start() sets timer for remaining time when lastActivityAt is 10 min ago', fakeAsync(() => {
    localStorage.setItem('lastActivityAt', String(Date.now() - (10 * 60 * 1000)));
    service.start();
    // 20 min remaining — should not fire at 19 min
    tick(19 * 60 * 1000);
    expect(authSpy.logout).not.toHaveBeenCalled();
    // should fire at the 20 min mark
    tick(1 * 60 * 1000);
    expect(authSpy.logout).toHaveBeenCalledTimes(1);
  }));
});
