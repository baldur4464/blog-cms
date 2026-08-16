import { type FormEvent, useState } from 'react';
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
      showToast('Kategorie erstellt.');
      resetForm();
    },
    onError: () => {
      showToast('Name bereits vergeben.', 'error');
      setError('Name bereits vergeben.');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: FormState }) =>
      categoriesApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories-public'] });
      showToast('Kategorie gespeichert.');
      resetForm();
    },
    onError: () => {
      showToast('Fehler beim Speichern.', 'error');
      setError('Fehler beim Speichern.');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: categoriesApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories-public'] });
      showToast('Kategorie gelöscht.');
    },
    onError: () => showToast('Kategorie konnte nicht gelöscht werden.', 'error'),
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
        <h1 className="text-xl font-semibold text-gray-900">Kategorien</h1>
        {!showForm && (
          <button
            onClick={() => setShowForm(true)}
            className="bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
          >
            Neue Kategorie
          </button>
        )}
      </div>

      {showForm && (
        <form
          onSubmit={handleSubmit}
          className="bg-white rounded-xl border border-gray-200 p-5 mb-6 shadow-sm"
        >
          <h2 className="text-sm font-medium text-gray-700 mb-4">
            {editing ? 'Kategorie bearbeiten' : 'Neue Kategorie'}
          </h2>
          {error && <p className="text-red-500 text-sm mb-3">{error}</p>}
          <div className="space-y-3">
            <input
              type="text"
              placeholder="Name *"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
              autoFocus
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <input
              type="text"
              placeholder="Beschreibung"
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
              {isSaving ? 'Speichern…' : 'Speichern'}
            </button>
            <button
              type="button"
              onClick={resetForm}
              className="text-sm text-gray-500 hover:text-gray-700 px-3 py-2"
            >
              Abbrechen
            </button>
          </div>
        </form>
      )}

      <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
        {isLoading ? (
          <p className="p-4 text-sm text-gray-400">Laden…</p>
        ) : categories.length === 0 ? (
          <p className="p-8 text-center text-sm text-gray-400">Keine Kategorien vorhanden.</p>
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
                    Bearbeiten
                  </button>
                  <button
                    onClick={() => {
                      if (confirm(`Kategorie "${cat.name}" löschen?`)) {
                        deleteMutation.mutate(cat.id);
                      }
                    }}
                    className="text-xs text-red-500 hover:text-red-700"
                  >
                    Löschen
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
