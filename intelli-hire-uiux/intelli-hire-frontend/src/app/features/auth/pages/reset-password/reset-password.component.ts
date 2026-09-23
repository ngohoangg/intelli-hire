import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.scss'
})
export class ResetPasswordComponent implements OnInit {
  form: FormGroup;
  loading = false;
  success = '';
  error = '';
  token = '';

  constructor(private fb: FormBuilder, private auth: AuthService, private route: ActivatedRoute) {
    this.form = this.fb.group({ newPassword: ['', [Validators.required, Validators.minLength(8)]] });
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) this.error = 'Missing reset token.';
  }

  submit(): void {
    if (this.form.invalid || !this.token) return;
    this.loading = true; this.error = '';
    this.auth.resetPassword(this.token, this.form.value.newPassword).subscribe({
      next: () => { this.success = 'Password reset successfully! You can now sign in.'; this.loading = false; },
      error: err => { this.error = err?.error?.message ?? 'Reset failed. Token may be expired.'; this.loading = false; }
    });
  }
}
