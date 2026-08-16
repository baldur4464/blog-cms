import { apiClient } from './client';

export interface CategoryResponse {
  id: string;
  name: string;
  slug: string;
  description?: string;
}

export interface CategoryRequest {
  name: string;
  description?: string;
}

export const categoriesApi = {
  listPublic: () =>
    apiClient.get<CategoryResponse[]>('/public/v1/categories').then((r) => r.data),

  create: (data: CategoryRequest) =>
    apiClient.post<CategoryResponse>('/admin/v1/categories', data).then((r) => r.data),

  update: (id: string, data: CategoryRequest) =>
    apiClient.put<CategoryResponse>(`/admin/v1/categories/${id}`, data).then((r) => r.data),

  delete: (id: string) =>
    apiClient.delete(`/admin/v1/categories/${id}`).then(() => undefined),
};
