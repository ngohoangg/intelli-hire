import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { TranslatePipe } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { ResumeSkillProfileService } from '../../services/resume-skill-profile.service';
import { ResumeSkillProfile } from '../../../../core/models/skill.model';

@Component({
  selector: 'app-skill-profile-card',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    TranslatePipe
  ],
  templateUrl: './skill-profile-card.component.html',
  styleUrl: './skill-profile-card.component.scss'
})
export class SkillProfileCardComponent implements OnInit, OnDestroy {
  @Input({ required: true }) resumeId!: string;

  profile: ResumeSkillProfile | null = null;
  loading = false;
  generating = false;
  editingSummary = false;
  summaryDraft = '';
  newSignal = '';
  notFound = false;

  private pollSub?: Subscription;

  constructor(private profileService: ResumeSkillProfileService) {}

  ngOnInit(): void {
    this.load();
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  load(): void {
    this.loading = true;
    this.profileService.get(this.resumeId).subscribe({
      next: p => { this.profile = p; this.loading = false; this.notFound = false; },
      error: () => { this.profile = null; this.loading = false; this.notFound = true; }
    });
  }

  generate(): void {
    this.generating = true;
    this.profileService.generate(this.resumeId).subscribe({
      next: p => { this.profile = p; this.generating = false; this.notFound = false; },
      error: () => { this.generating = false; }
    });
  }

  startEditSummary(): void {
    this.summaryDraft = this.profile?.summary ?? '';
    this.editingSummary = true;
  }

  saveSummary(): void {
    if (!this.profile) return;
    this.profileService.update(this.resumeId, {
      summary: this.summaryDraft,
      signals: this.profile.signals
    }).subscribe(p => { this.profile = p; this.editingSummary = false; });
  }

  cancelEditSummary(): void {
    this.editingSummary = false;
  }

  addSignal(): void {
    if (!this.newSignal.trim() || !this.profile) return;
    const key = `signal_${Date.now()}`;
    const signals = { ...(this.profile.signals ?? {}), [key]: this.newSignal.trim() };
    this.profileService.update(this.resumeId, { summary: this.profile.summary, signals })
      .subscribe(p => { this.profile = p; this.newSignal = ''; });
  }

  removeSignal(key: string): void {
    if (!this.profile) return;
    const signals = { ...this.profile.signals };
    delete signals[key];
    this.profileService.update(this.resumeId, { summary: this.profile.summary, signals })
      .subscribe(p => { this.profile = p; });
  }

  topSkillEntries(): [string, any][] {
    if (!this.profile?.top_skills) return [];
    return Object.entries(this.profile.top_skills)
      .sort((a, b) => parseFloat(b[1]) - parseFloat(a[1]));
  }

  signalEntries(): [string, any][] {
    if (!this.profile?.signals) return [];
    return Object.entries(this.profile.signals);
  }

  seniorityColor(seniority: string): string {
    const map: Record<string, string> = {
      JUNIOR: '#64B5F6', MID: '#81C784', SENIOR: '#FFB74D', PRINCIPAL: '#CE93D8'
    };
    return map[seniority] ?? 'var(--accent)';
  }

  toConfidencePct(value: any): number {
    const num = parseFloat(value);
    if (isNaN(num)) return 0;
    // backend stores 0.00–1.00 (precision=3, scale=2), convert to 0–100
    const pct = num <= 1 ? num * 100 : num;
    return Math.round(Math.min(Math.max(pct, 0), 100));
  }

  formatDate(date?: string): string {
    if (!date) return '—';
    return new Date(date).toLocaleDateString('vi-VN', { year: 'numeric', month: 'short', day: 'numeric' });
  }
}
