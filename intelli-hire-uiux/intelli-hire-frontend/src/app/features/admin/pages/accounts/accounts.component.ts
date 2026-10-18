import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService } from '../../../../core/services/admin.service';
import { AdminAccount, AccountStatus } from '../../../../core/models/admin.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-admin-accounts',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingSpinnerComponent],
  templateUrl: './accounts.component.html',
  styleUrl: './accounts.component.scss'
})
export class AdminAccountsComponent implements OnInit {
  readonly Math = Math; // required for Math.ceil in template
  accounts: AdminAccount[] = [];
  loading = false;
  totalElements = 0;
  page = 0;
  size = 20;
  searchQuery = '';
  statusFilter: AccountStatus | '' = '';

  constructor(private adminService: AdminService, public router: Router) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.adminService.listAccounts(this.page, this.size, this.statusFilter || undefined, this.searchQuery || undefined).subscribe({
      next: res => { this.accounts = res.content; this.totalElements = res.totalElements; this.loading = false; },
      error: () => this.loading = false
    });
  }

  onSearch(): void { this.page = 0; this.load(); }

  prevPage(): void { if (this.page > 0) { this.page--; this.load(); } }

  nextPage(): void {
    const maxPage = Math.ceil(this.totalElements / this.size) - 1;
    if (this.page < maxPage) { this.page++; this.load(); }
  }

  updateStatus(account: AdminAccount, status: AccountStatus): void {
    this.adminService.updateAccountStatus(account.id, status).subscribe({
      next: updated => { const i = this.accounts.findIndex(a => a.id === account.id); if (i >= 0) this.accounts[i] = updated; }
    });
  }

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      ACTIVE: 'Hoạt động',
      INACTIVE: 'Không hoạt động',
      LOCKED: 'Đã khóa'
    };
    return map[status] ?? status;
  }

  formatDate(d: string | null): string {
    if (!d) return '—';
    return new Date(d).toLocaleDateString('vi-VN', { year: 'numeric', month: '2-digit', day: '2-digit' });
  }
}
