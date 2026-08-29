import { apiClient } from './client';

export interface CategoryResponse {
  id: string;
  locale: string;
  name: string;
  slug: string;
  description?: string | null;
  translated: boolean;
  availableLocales: string[];
}

export interface CategoryInput {
  name: string;
  description?: string;
}

export const categoriesApi = {
  listPublic: () =>
    apiClient.get<CategoryResponse[]>('/public/v1/categories').then((r) => r.data),

  // Erzeugt die Kategorie samt Default-Locale-Übersetzung (ADR-018 Kapitel 6).
  create: (data: CategoryInput) =>
    apiClient.post<CategoryResponse>('/admin/v1/categories', data).then((r) => r.data),

  // Legt die Übersetzung für eine Locale an oder aktualisiert sie (ADR-018 Kapitel 10).
  upsertTranslation: (id: string, locale: string, data: CategoryInput) =>
    apiClient
      .put(`/admin/v1/categories/${id}/translations/${locale}`, data)
      .then((r) => r.data),

  delete: (id: string) =>
    apiClient.delete(`/admin/v1/categories/${id}`).then(() => undefined),
};
