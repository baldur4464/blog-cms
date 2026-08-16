import { apiClient } from './client';
import type { PageResponse } from './posts';

export interface MediaResponse {
  id: string;
  filename: string;
  originalName: string;
  mimeType: string;
  size: number;
  url: string;
  createdAt: string;
}

export const mediaApi = {
  list: (params?: { page?: number; size?: number }) =>
    apiClient
      .get<PageResponse<MediaResponse>>('/admin/v1/media', { params })
      .then((r) => r.data),

  upload: (file: File) => {
    const form = new FormData();
    form.append('file', file);
    return apiClient
      .post<MediaResponse>('/admin/v1/media', form, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then((r) => r.data);
  },

  delete: (id: string) =>
    apiClient.delete(`/admin/v1/media/${id}`).then(() => undefined),
};
