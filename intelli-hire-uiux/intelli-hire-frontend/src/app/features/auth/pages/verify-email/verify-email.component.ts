import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './verify-email.component.html',
  styleUrl: './verify-email.component.scss'
})
export class VerifyEmailComponent implements OnInit {
  status: 'loading' | 'success' | 'error' = 'loading';
  message = '';

  constructor(private route: ActivatedRoute, private auth: AuthService) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) { this.status = 'error'; this.message = 'Thiếu mã xác minh.'; return; }
    this.auth.verifyEmail(token).subscribe({
      next: () => { this.status = 'success'; this.message = 'Xác minh email thành công! Bạn có thể đăng nhập ngay bây giờ.'; },
      error: err => { this.status = 'error'; this.message = err?.error?.message ?? 'Xác minh thất bại. Mã có thể đã hết hạn.'; }
    });
  }
}
