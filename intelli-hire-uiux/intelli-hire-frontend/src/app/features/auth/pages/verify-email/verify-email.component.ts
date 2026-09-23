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
    if (!token) { this.status = 'error'; this.message = 'Missing verification token.'; return; }
    this.auth.verifyEmail(token).subscribe({
      next: () => { this.status = 'success'; this.message = 'Email verified successfully! You can now sign in.'; },
      error: err => { this.status = 'error'; this.message = err?.error?.message ?? 'Verification failed. Token may be expired.'; }
    });
  }
}
