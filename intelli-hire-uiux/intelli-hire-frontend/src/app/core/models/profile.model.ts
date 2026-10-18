// src/app/core/models/profile.model.ts
export interface Profile {
  id: number;
  account_id: number;
  first_name: string | null;
  middle_name: string | null;
  last_name: string | null;
  full_name: string | null;
  avatar_url: string | null;
  phone: string | null;
  address: string | null;
  birth_of_date: string | null;
  gender: string | null;
  github_url: string | null;
  linked_in_url: string | null;
}

export interface UpdateProfileRequest {
  first_name?: string | null;
  middle_name?: string | null;
  last_name?: string | null;
  avatar_url?: string | null;
  phone?: string | null;
  address?: string | null;
  birth_of_date?: string | null;
  gender?: string | null;
  github_url?: string | null;
  linked_in_url?: string | null;
}
