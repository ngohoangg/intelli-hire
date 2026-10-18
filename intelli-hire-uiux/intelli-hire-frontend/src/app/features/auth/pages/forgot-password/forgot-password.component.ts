import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss'
})
export class ForgotPasswordComponent {
  form: FormGroup;
  loading = false;
  success = '';
  error = '';

  constructor(private fb: FormBuilder, private auth: AuthService) {
    this.form = this.fb.group({ email: ['', [Validators.required, Validators.email]] });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true; this.error = '';
    this.auth.forgotPassword(this.form.value.email).subscribe({
      next: () => { this.success = 'Đã gửi liên kết đặt lại! Vui lòng kiểm tra hộp thư email.'; this.loading = false; },
      error: err => { this.error = err?.error?.message ?? 'Không thể gửi email đặt lại mật khẩu'; this.loading = false; }
    });
  }
}
