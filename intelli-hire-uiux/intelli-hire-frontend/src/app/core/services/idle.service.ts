// src/app/core/services/idle.service.ts
import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';

const IDLE_TIMEOUT_MS = 30 * 60 * 1000;
const LAST_ACTIVITY_KEY = 'lastActivityAt';
const ACTIVITY_EVENTS = ['mousemove', 'keydown', 'click', 'touchstart'] as const;

@Injectable({ providedIn: 'root' })
export class IdleService {
  private timerId: ReturnType<typeof setTimeout> | null = null;
  private boundReset = () => this.resetTimer();
  private isRunning = false;

  constructor(private auth: AuthService) {}

  start(): void {
    if (this.isRunning) return;

    const lastActivity = localStorage.getItem(LAST_ACTIVITY_KEY);
    if (lastActivity) {
      const elapsed = Date.now() - parseInt(lastActivity, 10);
      if (elapsed >= IDLE_TIMEOUT_MS) {
        // Đã quá 30 phút không hoạt động (kể cả khi browser đóng) → logout ngay
        this.auth.logout();
        return;
      }
      // Còn thời gian → schedule phần còn lại
      this.isRunning = true;
      this.scheduleTimer(IDLE_TIMEOUT_MS - elapsed);
    } else {
      this.isRunning = true;
      localStorage.setItem(LAST_ACTIVITY_KEY, String(Date.now()));
      this.scheduleTimer(IDLE_TIMEOUT_MS);
    }

    ACTIVITY_EVENTS.forEach(e => window.addEventListener(e, this.boundReset, { passive: true }));
  }

  stop(): void {
    this.isRunning = false;
    if (this.timerId !== null) {
      clearTimeout(this.timerId);
      this.timerId = null;
    }
    ACTIVITY_EVENTS.forEach(e => window.removeEventListener(e, this.boundReset));
    localStorage.removeItem(LAST_ACTIVITY_KEY);
  }

  resetTimer(): void {
    localStorage.setItem(LAST_ACTIVITY_KEY, String(Date.now()));
    if (this.timerId !== null) clearTimeout(this.timerId);
    this.scheduleTimer(IDLE_TIMEOUT_MS);
  }

  private scheduleTimer(delayMs: number): void {
    this.timerId = setTimeout(() => {
      this.timerId = null;
      this.auth.logout();
    }, delayMs);
  }
}
