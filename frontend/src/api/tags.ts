import { apiClient } from './client';

export interface TagResponse {
  id: string;
  locale: string;
  name: string;
  slug: string;
  translated: boolean;
  availableLocales: string[];
}

export interface TagInput {
  name: string;
}

export const tagsApi = {
  listPublic: () =>
    apiClient.get<TagResponse[]>('/public/v1/tags').then((r) => r.data),

  // Erzeugt den Tag samt Default-Locale-Übersetzung (ADR-018 Kapitel 6).
  create: (data: TagInput) =>
    apiClient.post<TagResponse>('/admin/v1/tags', data).then((r) => r.data),

  // Legt die Übersetzung für eine Locale an oder aktualisiert sie (ADR-018 Kapitel 10).
  upsertTranslation: (id: string, locale: string, data: TagInput) =>
    apiClient.put(`/admin/v1/tags/${id}/translations/${locale}`, data).then((r) => r.data),

  delete: (id: string) =>
    apiClient.delete(`/admin/v1/tags/${id}`).then(() => undefined),
};
