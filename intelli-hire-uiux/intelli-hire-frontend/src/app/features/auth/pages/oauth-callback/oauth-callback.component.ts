import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-oauth-callback',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './oauth-callback.component.html',
  styleUrl: './oauth-callback.component.scss'
})
export class OAuthCallbackComponent implements OnInit {
  error = '';
  /** Translation key for the sync pre-request failures, set at construction time
   * before translations may have finished loading — rendered via the
   * `translate` pipe (which re-evaluates on load) instead of `instant()`. */
  errorKey: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private auth: AuthService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    const code = this.route.snapshot.queryParamMap.get('code');
    const state = this.route.snapshot.queryParamMap.get('state');
    const provider = this.route.snapshot.paramMap.get('provider');

    if (!code || !state || !provider) { this.errorKey = 'auth.oauthCallback.missingParams'; return; }

    const storedState = localStorage.getItem('oauth_state');
    localStorage.removeItem('oauth_state');

    if (state !== storedState) { this.errorKey = 'auth.oauthCallback.stateMismatch'; return; }

    this.auth.handleOAuthCallback(provider, code, state).subscribe({
      next: () => this.router.navigate(['/resumes']),
      error: err => { this.error = err?.error?.message ?? this.translate.instant('auth.oauthCallback.authFailed'); }
    });
  }
}
