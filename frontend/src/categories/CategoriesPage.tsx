import { type FormEvent, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { categoriesApi, type CategoryResponse } from '../api/categories';
import { useToast } from '../components/Toast';

interface FormState {
  name: string;
  description: string;
}

const emptyForm: FormState = { name: '', description: '' };

export default function CategoriesPage() {
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const { t, i18n } = useTranslation();
  const [editing, setEditing] = useState<CategoryResponse | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState('');

  const { data: categories = [], isLoading } = useQuery({
    queryKey: ['categories-public'],
    queryFn: categoriesApi.listPublic,
  });

  const createMutation = useMutation({
    mutationFn: categoriesApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories-public'] });
      showToast(t('categories.created'));
      resetForm();
    },
    onError: () => {
      showToast(t('categories.nameTaken'), 'error');
      setError(t('categories.nameTaken'));
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: FormState }) =>
      categoriesApi.upsertTranslation(id, i18n.resolvedLanguage ?? 'de', data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories-public'] });
      showToast(t('categories.saved'));
      resetForm();
    },
    onError: () => {
      showToast(t('categories.saveError'), 'error');
      setError(t('categories.saveError'));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: categoriesApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories-public'] });
      showToast(t('categories.deleted'));
    },
    onError: () => showToast(t('categories.deleteError'), 'error'),
  });

  const resetForm = () => {
    setForm(emptyForm);
    setEditing(null);
    setShowForm(false);
    setError('');
  };

  const handleEdit = (cat: CategoryResponse) => {
    setEditing(cat);
    setForm({ name: cat.name, description: cat.description ?? '' });
    setShowForm(true);
    setError('');
  };

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    setError('');
    if (editing) {
      updateMutation.mutate({ id: editing.id, data: form });
    } else {
      createMutation.mutate(form);
    }
  };

  const isSaving = createMutation.isPending || updateMutation.isPending;

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-xl font-semibold text-gray-900">{t('categories.title')}</h1>
        {!showForm && (
          <button
            onClick={() => setShowForm(true)}
            className="bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
          >
            {t('categories.new')}
          </button>
        )}
      </div>

      {showForm && (
        <form
          onSubmit={handleSubmit}
          className="bg-white rounded-xl border border-gray-200 p-5 mb-6 shadow-sm"
        >
          <h2 className="text-sm font-medium text-gray-700 mb-4">
            {editing ? t('categories.editTitle') : t('categories.new')}
          </h2>
          {error && <p className="text-red-500 text-sm mb-3">{error}</p>}
          <div className="space-y-3">
            <input
              type="text"
              placeholder={t('categories.name')}
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
              autoFocus
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <input
              type="text"
              placeholder={t('categories.description')}
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div className="flex gap-2 mt-4">
            <button
              type="submit"
              disabled={isSaving}
              className="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-sm font-medium px-4 py-2 rounded-lg"
            >
              {isSaving ? t('common.saving') : t('common.save')}
            </button>
            <button
              type="button"
              onClick={resetForm}
              className="text-sm text-gray-500 hover:text-gray-700 px-3 py-2"
            >
              {t('common.cancel')}
            </button>
          </div>
        </form>
      )}

      <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
        {isLoading ? (
          <p className="p-4 text-sm text-gray-400">{t('common.loading')}</p>
        ) : categories.length === 0 ? (
          <p className="p-8 text-center text-sm text-gray-400">{t('categories.empty')}</p>
        ) : (
          <ul className="divide-y divide-gray-100">
            {categories.map((cat) => (
              <li key={cat.id} className="flex items-center justify-between px-4 py-3">
                <div>
                  <p className="text-sm font-medium text-gray-900">{cat.name}</p>
                  {cat.description && (
                    <p className="text-xs text-gray-500 mt-0.5">{cat.description}</p>
                  )}
                  <p className="text-xs text-gray-400 font-mono mt-0.5">{cat.slug}</p>
                </div>
                <div className="flex gap-3">
                  <button
                    onClick={() => handleEdit(cat)}
                    className="text-xs text-blue-600 hover:text-blue-800"
                  >
                    {t('common.edit')}
                  </button>
                  <button
                    onClick={() => {
                      if (confirm(t('categories.confirmDelete', { name: cat.name }))) {
                        deleteMutation.mutate(cat.id);
                      }
                    }}
                    className="text-xs text-red-500 hover:text-red-700"
                  >
                    {t('common.delete')}
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
