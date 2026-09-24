import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../../../core/services/auth.service';
import { extractApiErrorMessage } from '../../../../core/utils/api-error';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, RouterLink, TranslatePipe],
  templateUrl: './verify-email.component.html',
  styleUrl: './verify-email.component.scss'
})
export class VerifyEmailComponent implements OnInit {
  status: 'loading' | 'success' | 'error' = 'loading';
  message = '';
  /** Translation key for the missing-token case, set at construction time before
   * translations may have finished loading — rendered via the `translate` pipe
   * (which re-evaluates on load) instead of `instant()`. */
  messageKey: string | null = null;

  constructor(private route: ActivatedRoute, private auth: AuthService, private translate: TranslateService) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) { this.status = 'error'; this.messageKey = 'auth.verifyEmail.missingToken'; return; }
    this.auth.verifyEmail(token).subscribe({
      next: () => { this.status = 'success'; this.message = this.translate.instant('auth.verifyEmail.verifySuccess'); },
      error: err => { this.status = 'error'; this.message = extractApiErrorMessage(err) ?? this.translate.instant('auth.verifyEmail.verifyFailed'); }
    });
  }
}
