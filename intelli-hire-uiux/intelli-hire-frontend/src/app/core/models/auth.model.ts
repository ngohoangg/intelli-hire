// src/app/core/models/auth.model.ts
export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
  confirmPassword: string;
  firstName?: string;
  lastName?: string;
}

export interface LoginResponse {
  access_token: string;
  refresh_token: string;
  token_type: string;
  expires_in: number;
  refresh_expires_in: number;
  account_id: number;
  email: string;
  username: string;
  roles: string[];
}

export interface RegisterResponse {
  account_id: number;
  email: string;
  username: string;
  message: string;
}

export interface OAuthProvider {
  code: string;
  name: string;
  icon_url: string;
}

export interface LinkedOAuthProvider {
  provider_code: string;
  provider_name: string;
  provider_email: string;
  linked_at: string;
  last_used_at: string | null;
}

export interface AuthUser {
  accountId: number;
  email: string;
  username: string;
  roles: string[];
}
