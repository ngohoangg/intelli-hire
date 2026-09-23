import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { AdminQuestionService } from '../../core/services/admin-question.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, TranslatePipe],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss'
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  menuOpen = false;
  pendingCount = 0;
  private pollInterval: ReturnType<typeof setInterval> | null = null;
  private countSub: Subscription | null = null;

  constructor(
    public auth: AuthService,
    private adminQuestionService: AdminQuestionService
  ) {}

  ngOnInit(): void {
    if (this.auth.isAdmin) {
      this.adminQuestionService.refreshPendingCount();
      this.countSub = this.adminQuestionService.pendingCount$.subscribe(
        count => { this.pendingCount = count; }
      );
      this.pollInterval = setInterval(() => this.adminQuestionService.refreshPendingCount(), 10_000);
    }
  }

  ngOnDestroy(): void {
    if (this.pollInterval) clearInterval(this.pollInterval);
    this.countSub?.unsubscribe();
  }

  toggleMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.menuOpen = !this.menuOpen;
  }

  closeMenu(): void {
    this.menuOpen = false;
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.menuOpen = false;
  }

  logout(): void {
    this.closeMenu();
    this.auth.logout();
  }
}
