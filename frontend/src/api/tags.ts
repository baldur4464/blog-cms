import { apiClient } from './client';

export interface TagResponse {
  id: string;
  name: string;
  slug: string;
}

export interface TagRequest {
  name: string;
}

export const tagsApi = {
  listPublic: () =>
    apiClient.get<TagResponse[]>('/public/v1/tags').then((r) => r.data),

  create: (data: TagRequest) =>
    apiClient.post<TagResponse>('/admin/v1/tags', data).then((r) => r.data),

  update: (id: string, data: TagRequest) =>
    apiClient.put<TagResponse>(`/admin/v1/tags/${id}`, data).then((r) => r.data),

  delete: (id: string) =>
    apiClient.delete(`/admin/v1/tags/${id}`).then(() => undefined),
};
