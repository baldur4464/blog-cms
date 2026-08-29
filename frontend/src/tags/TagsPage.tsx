import { type FormEvent, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { tagsApi, type TagResponse } from '../api/tags';
import { useToast } from '../components/Toast';

export default function TagsPage() {
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const { t, i18n } = useTranslation();
  const [editing, setEditing] = useState<TagResponse | null>(null);
  const [name, setName] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState('');

  const { data: tags = [], isLoading } = useQuery({
    queryKey: ['tags-public'],
    queryFn: tagsApi.listPublic,
  });

  const createMutation = useMutation({
    mutationFn: tagsApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tags-public'] });
      showToast(t('tags.created'));
      reset();
    },
    onError: () => {
      showToast(t('tags.nameTaken'), 'error');
      setError(t('tags.nameTaken'));
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, name }: { id: string; name: string }) =>
      tagsApi.upsertTranslation(id, i18n.resolvedLanguage ?? 'de', { name }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tags-public'] });
      showToast(t('tags.saved'));
      reset();
    },
    onError: () => {
      showToast(t('tags.saveError'), 'error');
      setError(t('tags.saveError'));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: tagsApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tags-public'] });
      showToast(t('tags.deleted'));
    },
    onError: () => showToast(t('tags.deleteError'), 'error'),
  });

  const reset = () => {
    setEditing(null);
    setName('');
    setShowForm(false);
    setError('');
  };

  const handleEdit = (tag: TagResponse) => {
    setEditing(tag);
    setName(tag.name);
    setShowForm(true);
    setError('');
  };

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    setError('');
    if (editing) {
      updateMutation.mutate({ id: editing.id, name });
    } else {
      createMutation.mutate({ name });
    }
  };

  const isSaving = createMutation.isPending || updateMutation.isPending;

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-xl font-semibold text-gray-900">{t('tags.title')}</h1>
        {!showForm && (
          <button
            onClick={() => setShowForm(true)}
            className="bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
          >
            {t('tags.new')}
          </button>
        )}
      </div>

      {showForm && (
        <form
          onSubmit={handleSubmit}
          className="bg-white rounded-xl border border-gray-200 p-5 mb-6 shadow-sm"
        >
          <h2 className="text-sm font-medium text-gray-700 mb-3">
            {editing ? t('tags.editTitle') : t('tags.new')}
          </h2>
          {error && <p className="text-red-500 text-sm mb-2">{error}</p>}
          <input
            type="text"
            placeholder={t('tags.name')}
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            autoFocus
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 mb-3"
          />
          <div className="flex gap-2">
            <button
              type="submit"
              disabled={isSaving}
              className="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-sm font-medium px-4 py-2 rounded-lg"
            >
              {isSaving ? t('common.saving') : t('common.save')}
            </button>
            <button
              type="button"
              onClick={reset}
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
        ) : tags.length === 0 ? (
          <p className="p-8 text-center text-sm text-gray-400">{t('tags.empty')}</p>
        ) : (
          <ul className="divide-y divide-gray-100">
            {tags.map((tag) => (
              <li key={tag.id} className="flex items-center justify-between px-4 py-3">
                <div>
                  <p className="text-sm font-medium text-gray-900">{tag.name}</p>
                  <p className="text-xs text-gray-400 font-mono">{tag.slug}</p>
                </div>
                <div className="flex gap-3">
                  <button
                    onClick={() => handleEdit(tag)}
                    className="text-xs text-blue-600 hover:text-blue-800"
                  >
                    {t('common.edit')}
                  </button>
                  <button
                    onClick={() => {
                      if (confirm(t('tags.confirmDelete', { name: tag.name }))) {
                        deleteMutation.mutate(tag.id);
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
