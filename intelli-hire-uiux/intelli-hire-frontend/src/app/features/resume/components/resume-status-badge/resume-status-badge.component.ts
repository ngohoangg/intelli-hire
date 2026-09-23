import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { ResumeStatus } from '../../../../core/models/resume.model';

@Component({
  selector: 'app-resume-status-badge',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  template: `
    <span class="badge" [ngClass]="cssClass">
      <span class="dot"></span>{{ label | translate }}
    </span>
  `,
  styles: [`
    .badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 4px 12px;
      border-radius: var(--radius-xs);
      font-size: 11px;
      font-weight: 600;
      letter-spacing: 0.05em;
      border: 1px solid transparent;
      font-family: var(--font-mono);
      text-transform: uppercase;
    }
    .dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: currentColor;
    }
    .uploaded  { background: var(--cyan-glow);           color: #00F0FF; border-color: var(--border-accent); }
    .parsing   { background: rgba(255,170,0,0.1);         color: #FFAA00; border-color: rgba(255,170,0,0.2); }
    .parsing .dot { animation: hud-ping 1.5s infinite; }
    .completed { background: rgba(0,230,138,0.1);         color: #00E68A; border-color: rgba(0,230,138,0.2); }
    .failed    { background: rgba(255,23,68,0.1);         color: #FF3D71; border-color: rgba(255,23,68,0.2); }
    .deleted   { background: rgba(90,94,114,0.1);         color: #5A5E72; border-color: rgba(90,94,114,0.2); }

    @keyframes hud-ping {
      0%, 100% { opacity: 1; transform: scale(1); }
      50% { opacity: 0.4; transform: scale(1.4); }
    }
  `]
})
export class ResumeStatusBadgeComponent {
  @Input({ required: true }) status!: ResumeStatus;

  get cssClass(): string {
    return this.status.toLowerCase();
  }

  get label(): string {
    const map: Record<ResumeStatus, string> = {
      [ResumeStatus.UPLOADED]: 'resume.resumeStatusBadge.uploaded',
      [ResumeStatus.PARSING]: 'resume.resumeStatusBadge.parsing',
      [ResumeStatus.COMPLETED]: 'resume.resumeStatusBadge.completed',
      [ResumeStatus.FAILED]: 'resume.resumeStatusBadge.failed',
      [ResumeStatus.DELETED]: 'resume.resumeStatusBadge.deleted'
    };
    return map[this.status] ?? this.status;
  }
}
