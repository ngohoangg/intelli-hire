import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AdminAccount, AccountRole, AdminRole, AccountStatus, PageResponse } from '../models/admin.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  constructor(private api: ApiService) {}

  listAccounts(page = 0, size = 20, status?: AccountStatus, search?: string): Observable<PageResponse<AdminAccount>> {
    const params: Record<string, string> = { page: String(page), size: String(size) };
    if (status) params['status'] = status;
    if (search) params['search'] = search;
    return this.api.get<PageResponse<AdminAccount>>('/v1/admin/accounts', params);
  }

  getAccount(id: number): Observable<AdminAccount> {
    return this.api.get<AdminAccount>(`/v1/admin/accounts/${id}`);
  }

  updateAccountStatus(id: number, status: AccountStatus, reason?: string): Observable<AdminAccount> {
    return this.api.put<AdminAccount>(`/v1/admin/accounts/${id}/status`, { status, reason });
  }

  getAccountRoles(id: number): Observable<AccountRole[]> {
    return this.api.get<AccountRole[]>(`/v1/admin/accounts/${id}/roles`);
  }

  assignRole(accountId: number, roleId: number): Observable<void> {
    return this.api.post<void>(`/v1/admin/accounts/${accountId}/roles`, { role_id: roleId });
  }

  removeRole(accountId: number, roleId: number): Observable<void> {
    return this.api.delete<void>(`/v1/admin/accounts/${accountId}/roles/${roleId}`);
  }

  listRoles(page = 0, size = 50): Observable<PageResponse<AdminRole>> {
    return this.api.get<PageResponse<AdminRole>>('/v1/admin/roles', { page: String(page), size: String(size) });
  }

  createRole(name: string, description?: string): Observable<AdminRole> {
    return this.api.post<AdminRole>('/v1/admin/roles', { name, description });
  }

  updateRole(id: number, name: string, description: string | null, isActive: boolean): Observable<AdminRole> {
    return this.api.put<AdminRole>(`/v1/admin/roles/${id}`, { name, description, is_active: isActive });
  }

  deleteRole(id: number): Observable<void> {
    return this.api.delete<void>(`/v1/admin/roles/${id}`);
  }
}
