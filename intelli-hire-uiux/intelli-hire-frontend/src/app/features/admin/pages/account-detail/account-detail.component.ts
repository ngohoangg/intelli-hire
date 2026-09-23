import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { AdminService } from '../../../../core/services/admin.service';
import { AdminAccount, AccountRole, AdminRole } from '../../../../core/models/admin.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-account-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, LoadingSpinnerComponent],
  templateUrl: './account-detail.component.html',
  styleUrl: './account-detail.component.scss'
})
export class AccountDetailComponent implements OnInit {
  account: AdminAccount | null = null;
  roles: AccountRole[] = [];
  allRoles: AdminRole[] = [];
  loading = false;
  error = '';
  success = '';

  constructor(private route: ActivatedRoute, private adminService: AdminService) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loading = true;
    this.adminService.getAccount(id).subscribe({ next: a => { this.account = a; this.loading = false; }, error: () => this.loading = false });
    this.adminService.getAccountRoles(id).subscribe({ next: r => this.roles = r, error: () => {} });
    this.adminService.listRoles().subscribe({ next: r => this.allRoles = r.content, error: () => {} });
  }

  get isSuperAdmin(): boolean {
    return this.roles.some(r =>
      r.role_name === 'ROLE_SUPER_ADMIN' || r.role_name === 'SUPER_ADMIN'
    );
  }

  get unassignedRoles(): AdminRole[] {
    const assignedIds = new Set(this.roles.map(r => r.role_id));
    return this.allRoles.filter(r => !assignedIds.has(r.id));
  }

  assignRole(roleId: number): void {
    if (!this.account) return;
    this.adminService.assignRole(this.account.id, roleId).pipe(
      switchMap(() => this.adminService.getAccountRoles(this.account!.id))
    ).subscribe({
      next: r => { this.roles = r; this.success = 'Role assigned.'; },
      error: err => { this.error = err?.error?.message ?? 'Failed to assign role'; }
    });
  }

  removeRole(roleId: number): void {
    if (!this.account) return;
    this.adminService.removeRole(this.account.id, roleId).subscribe({
      next: () => { this.roles = this.roles.filter(r => r.role_id !== roleId); this.success = 'Role removed.'; },
      error: err => { this.error = err?.error?.message ?? 'Failed to remove role'; }
    });
  }
}
