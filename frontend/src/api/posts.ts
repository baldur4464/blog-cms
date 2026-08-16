import { apiClient } from './client';

export type PostStatus = 'DRAFT' | 'REVIEW' | 'PUBLISHED' | 'ARCHIVED';

export interface PostSummary {
  id: string;
  title: string;
  slug: string;
  excerpt?: string;
  status: PostStatus;
  authorUsername: string;
  categoryName?: string;
  publishedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PostDetail {
  id: string;
  title: string;
  slug: string;
  excerpt?: string;
  content: string;
  status: PostStatus;
  authorUsername: string;
  categoryId?: string;
  categoryName?: string;
  tagIds: string[];
  tags: string[];
  publishedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CreatePostRequest {
  title: string;
  content: string;
  excerpt?: string;
  categoryId?: string;
  tagIds?: string[];
}

export interface UpdatePostRequest extends CreatePostRequest {}

export const postsApi = {
  list: (params?: { page?: number; size?: number; sort?: string }) =>
    apiClient
      .get<PageResponse<PostSummary>>('/admin/v1/posts', { params })
      .then((r) => r.data),

  get: (id: string) =>
    apiClient.get<PostDetail>(`/admin/v1/posts/${id}`).then((r) => r.data),

  create: (data: CreatePostRequest) =>
    apiClient.post<PostDetail>('/admin/v1/posts', data).then((r) => r.data),

  update: (id: string, data: UpdatePostRequest) =>
    apiClient.put<PostDetail>(`/admin/v1/posts/${id}`, data).then((r) => r.data),

  changeStatus: (id: string, status: PostStatus) =>
    apiClient
      .post<PostDetail>(`/admin/v1/posts/${id}/status`, null, { params: { status } })
      .then((r) => r.data),

  archive: (id: string) =>
    apiClient.delete(`/admin/v1/posts/${id}`).then(() => undefined),
};
