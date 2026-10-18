import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-oauth-callback',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './oauth-callback.component.html',
  styleUrl: './oauth-callback.component.scss'
})
export class OAuthCallbackComponent implements OnInit {
  error = '';

  constructor(private route: ActivatedRoute, private router: Router, private auth: AuthService) {}

  ngOnInit(): void {
    const code = this.route.snapshot.queryParamMap.get('code');
    const state = this.route.snapshot.queryParamMap.get('state');
    const provider = this.route.snapshot.paramMap.get('provider');

    if (!code || !state || !provider) { this.error = 'Thiếu tham số trong phản hồi OAuth.'; return; }

    const storedState = localStorage.getItem('oauth_state');
    localStorage.removeItem('oauth_state');

    if (state !== storedState) { this.error = 'Trạng thái OAuth không khớp — có thể là tấn công CSRF.'; return; }

    this.auth.handleOAuthCallback(provider, code, state).subscribe({
      next: () => this.router.navigate(['/resumes']),
      error: err => { this.error = err?.error?.message ?? 'Xác thực OAuth thất bại.'; }
    });
  }
}
