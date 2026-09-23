import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ProfileService } from '../../../../core/services/profile.service';
import { AuthService } from '../../../../core/services/auth.service';
import { LinkedOAuthProvider } from '../../../../core/models/auth.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-security',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LoadingSpinnerComponent, TranslatePipe],
  templateUrl: './security.component.html',
  styleUrl: './security.component.scss'
})
export class SecurityComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private translate = inject(TranslateService);

  linkedProviders: LinkedOAuthProvider[] = [];
  loading = false;
  error = '';
  success = '';

  pwForm: FormGroup;
  pwSaving = false;
  pwError = '';
  pwSuccess = '';
  showCurrent = false;
  showNew = false;
  showConfirm = false;

  constructor(
    private profileService: ProfileService,
    private auth: AuthService,
    private fb: FormBuilder
  ) {
    this.pwForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loading = true;
    this.profileService.getLinkedProviders()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: p => { this.linkedProviders = p; this.loading = false; },
        error: () => this.loading = false
      });
  }

  unlink(providerCode: string): void {
    this.profileService.unlinkProvider(providerCode).subscribe({
      next: () => { this.linkedProviders = this.linkedProviders.filter(p => p.provider_code !== providerCode); this.success = this.translate.instant('account.security.unlinkSuccess'); },
      error: err => { this.error = err?.error?.message ?? this.translate.instant('account.security.unlinkFailed'); }
    });
  }

  changePassword(): void {
    if (this.pwForm.invalid) return;
    const { currentPassword, newPassword, confirmPassword } = this.pwForm.value;
    if (newPassword !== confirmPassword) {
      this.pwError = this.translate.instant('account.security.passwordMismatch');
      return;
    }
    this.pwSaving = true;
    this.pwError = '';
    this.pwSuccess = '';
    this.auth.changePassword(currentPassword, newPassword, confirmPassword).subscribe({
      next: () => {
        this.pwSuccess = this.translate.instant('account.security.changeSuccess');
        this.pwForm.reset();
        this.pwSaving = false;
      },
      error: err => {
        const body = err?.error;
        if (body?.fieldErrors) {
          this.pwError = Object.values(body.fieldErrors).join(', ');
        } else {
          this.pwError = body?.message ?? this.translate.instant('account.security.changeFailedWithStatus', { status: err.status });
        }
        this.pwSaving = false;
      }
    });
  }
}
