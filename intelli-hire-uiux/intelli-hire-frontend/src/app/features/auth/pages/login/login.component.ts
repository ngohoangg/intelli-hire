import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../../../core/services/auth.service';
import { OAuthProvider } from '../../../../core/models/auth.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, TranslatePipe],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  form: FormGroup;
  loading = false;
  error = '';
  oauthProviders: OAuthProvider[] = [];

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private translate: TranslateService
  ) {
    this.form = this.fb.group({
      identifier: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    if (this.auth.isLoggedIn) { this.router.navigate([this.homeRoute]); return; }
    this.auth.getOAuthProviders().subscribe({ next: p => this.oauthProviders = p, error: () => {} });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true; this.error = '';
    this.auth.login(this.form.value).subscribe({
      next: () => { this.loading = false; this.router.navigate([this.homeRoute]); },
      error: err => { this.error = err?.error?.message ?? this.translate.instant('auth.login.invalidCredentials'); this.loading = false; }
    });
  }

  private get homeRoute(): string {
    return this.auth.isAdmin ? '/admin/accounts' : '/resumes';
  }

  loginWithOAuth(provider: string): void {
    this.auth.getOAuthAuthorizationUrl(provider).subscribe({
      next: res => { localStorage.setItem('oauth_state', res.state); window.location.href = res.authorization_url; },
      error: () => { this.error = this.translate.instant('auth.login.oauthError'); }
    });
  }
}
