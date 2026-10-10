import { apiClient } from './client';
import type { PageResponse } from './posts';

export type UserRole = 'ADMIN' | 'EDITOR' | 'AUTHOR';
export type UserStatus = 'ACTIVE' | 'INACTIVE';

export interface UserResponse {
  id: string;
  username: string;
  email: string;
  role: UserRole;
  status: UserStatus;
  preferredLocale?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  role: UserRole;
}

export interface UpdateUserRequest {
  username: string;
  email: string;
  role: UserRole;
}

export interface ResetPasswordRequest {
  newPassword: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export const usersApi = {
  list: (params?: { page?: number; size?: number }) =>
    apiClient
      .get<PageResponse<UserResponse>>('/admin/v1/users', { params })
      .then((r) => r.data),

  get: (id: string) =>
    apiClient.get<UserResponse>(`/admin/v1/users/${id}`).then((r) => r.data),

  create: (data: CreateUserRequest) =>
    apiClient.post<UserResponse>('/admin/v1/users', data).then((r) => r.data),

  update: (id: string, data: UpdateUserRequest) =>
    apiClient.put<UserResponse>(`/admin/v1/users/${id}`, data).then((r) => r.data),

  deactivate: (id: string) =>
    apiClient.delete(`/admin/v1/users/${id}`).then(() => undefined),

  resetPassword: (id: string, data: ResetPasswordRequest) =>
    apiClient.post(`/admin/v1/users/${id}/reset-password`, data).then(() => undefined),

  setPreferredLocale: (locale: string) =>
    apiClient
      .put<UserResponse>('/admin/v1/users/me/preferred-locale', { preferredLocale: locale })
      .then((r) => r.data),

  changePassword: (data: ChangePasswordRequest) =>
    apiClient.put('/admin/v1/users/me/password', data).then(() => undefined),
};
