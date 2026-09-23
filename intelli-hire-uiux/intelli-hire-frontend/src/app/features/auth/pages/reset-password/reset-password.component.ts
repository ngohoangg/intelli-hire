import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, TranslatePipe],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.scss'
})
export class ResetPasswordComponent implements OnInit {
  form: FormGroup;
  loading = false;
  success = '';
  error = '';
  /** Translation key for the missing-token case, set at construction time before
   * translations may have finished loading — rendered via the `translate` pipe
   * (which re-evaluates on load) instead of `instant()`. */
  errorKey: string | null = null;
  token = '';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private route: ActivatedRoute,
    private translate: TranslateService
  ) {
    this.form = this.fb.group({ newPassword: ['', [Validators.required, Validators.minLength(8)]] });
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) this.errorKey = 'auth.resetPassword.missingToken';
  }

  submit(): void {
    if (this.form.invalid || !this.token) return;
    this.loading = true; this.error = ''; this.errorKey = null;
    this.auth.resetPassword(this.token, this.form.value.newPassword).subscribe({
      next: () => { this.success = this.translate.instant('auth.resetPassword.resetSuccess'); this.loading = false; },
      error: err => { this.error = err?.error?.message ?? this.translate.instant('auth.resetPassword.resetFailed'); this.loading = false; }
    });
  }
}
