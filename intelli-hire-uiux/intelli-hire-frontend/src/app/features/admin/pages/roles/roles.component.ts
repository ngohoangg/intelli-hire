import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../../core/services/admin.service';
import { AdminRole } from '../../../../core/models/admin.model';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-admin-roles',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingSpinnerComponent],
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

  constructor(private adminService: AdminService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.adminService.listRoles().subscribe({ next: r => { this.roles = r.content; this.loading = false; }, error: () => this.loading = false });
  }

  create(): void {
    if (!this.newRoleName.trim()) return;
    this.adminService.createRole(this.newRoleName, this.newRoleDesc || undefined).subscribe({
      next: r => { this.roles = [r, ...this.roles]; this.newRoleName = ''; this.newRoleDesc = ''; this.creating = false; this.success = 'Đã tạo vai trò.'; },
      error: err => { this.error = err?.error?.message ?? 'Không thể tạo vai trò'; }
    });
  }

  toggleActive(role: AdminRole): void {
    this.adminService.updateRole(role.id, role.name, role.description, !role.is_active).subscribe({
      next: r => { const i = this.roles.findIndex(x => x.id === r.id); if (i >= 0) this.roles[i] = r; },
      error: err => { this.error = err?.error?.message ?? 'Cập nhật thất bại'; }
    });
  }

  delete(role: AdminRole): void {
    if (role.is_system) { this.error = 'Không thể xóa vai trò hệ thống.'; return; }
    this.adminService.deleteRole(role.id).subscribe({
      next: () => { this.roles = this.roles.filter(r => r.id !== role.id); this.success = 'Đã xóa vai trò.'; },
      error: err => { this.error = err?.error?.message ?? 'Xóa thất bại'; }
    });
  }
}
