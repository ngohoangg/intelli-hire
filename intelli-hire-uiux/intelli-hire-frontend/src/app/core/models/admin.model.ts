// src/app/core/models/admin.model.ts
export type AccountStatus = 'ACTIVE' | 'INACTIVE' | 'LOCKED';

export interface AdminAccount {
  id: number;
  email: string;
  username: string;
  status: AccountStatus;
  failed_login_attempts: number;
  last_login_at: string | null;
  locked_at: string | null;
  locked_reason: string | null;
  created_at: string;
  updated_at: string;
  is_super_admin: boolean;
}

export interface AccountRole {
  role_id: number;
  role_name: string;
  assigned_at: string;
  expires_at: string | null;
}

export interface AdminRole {
  id: number;
  name: string;
  description: string | null;
  is_system: boolean;
  is_active: boolean;
  created_at: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
