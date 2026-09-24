import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AdminService } from '../../../../core/services/admin.service';
import { AdminRole } from '../../../../core/models/admin.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { extractApiErrorMessage } from '../../../../core/utils/api-error';

@Component({
  selector: 'app-admin-roles',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingSpinnerComponent, TranslatePipe],
  templateUrl: './roles.component.html',
  styleUrl: './roles.component.scss'
})
export class AdminRolesComponent implements OnInit {
  roles: AdminRole[] = [];
  loading = false;
  error = '';
  success = '';
  creating = false;
  newRoleName = '';
  newRoleDesc = '';

  constructor(private adminService: AdminService, private translate: TranslateService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.adminService.listRoles().subscribe({ next: r => { this.roles = r.content; this.loading = false; }, error: () => this.loading = false });
  }

  create(): void {
    if (!this.newRoleName.trim()) return;
    this.adminService.createRole(this.newRoleName, this.newRoleDesc || undefined).subscribe({
      next: r => { this.roles = [r, ...this.roles]; this.newRoleName = ''; this.newRoleDesc = ''; this.creating = false; this.success = this.translate.instant('admin.roles.roleCreatedSuccess'); },
      error: err => { this.error = extractApiErrorMessage(err) ?? this.translate.instant('admin.roles.createFailed'); }
    });
  }

  toggleActive(role: AdminRole): void {
    this.adminService.updateRole(role.id, role.name, role.description, !role.is_active).subscribe({
      next: r => { const i = this.roles.findIndex(x => x.id === r.id); if (i >= 0) this.roles[i] = r; },
      error: err => { this.error = extractApiErrorMessage(err) ?? this.translate.instant('admin.roles.updateFailed'); }
    });
  }

  delete(role: AdminRole): void {
    if (role.is_system) { this.error = this.translate.instant('admin.roles.cannotDeleteSystem'); return; }
    this.adminService.deleteRole(role.id).subscribe({
      next: () => { this.roles = this.roles.filter(r => r.id !== role.id); this.success = this.translate.instant('admin.roles.roleDeletedSuccess'); },
      error: err => { this.error = extractApiErrorMessage(err) ?? this.translate.instant('admin.roles.deleteFailed'); }
    });
  }
}
