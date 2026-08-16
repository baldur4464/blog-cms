import { type FormEvent, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { tagsApi, type TagResponse } from '../api/tags';
import { useToast } from '../components/Toast';

export default function TagsPage() {
  const queryClient = useQueryClient();
  const { showToast } = useToast();
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
      showToast('Tag erstellt.');
      reset();
    },
    onError: () => {
      showToast('Name bereits vergeben.', 'error');
      setError('Name bereits vergeben.');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, name }: { id: string; name: string }) =>
      tagsApi.update(id, { name }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tags-public'] });
      showToast('Tag gespeichert.');
      reset();
    },
    onError: () => {
      showToast('Fehler beim Speichern.', 'error');
      setError('Fehler beim Speichern.');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: tagsApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tags-public'] });
      showToast('Tag gelöscht.');
    },
    onError: () => showToast('Tag konnte nicht gelöscht werden.', 'error'),
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
        <h1 className="text-xl font-semibold text-gray-900">Tags</h1>
        {!showForm && (
          <button
            onClick={() => setShowForm(true)}
            className="bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
          >
            Neuer Tag
          </button>
        )}
      </div>

      {showForm && (
        <form
          onSubmit={handleSubmit}
          className="bg-white rounded-xl border border-gray-200 p-5 mb-6 shadow-sm"
        >
          <h2 className="text-sm font-medium text-gray-700 mb-3">
            {editing ? 'Tag bearbeiten' : 'Neuer Tag'}
          </h2>
          {error && <p className="text-red-500 text-sm mb-2">{error}</p>}
          <input
            type="text"
            placeholder="Name *"
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
              {isSaving ? 'Speichern…' : 'Speichern'}
            </button>
            <button
              type="button"
              onClick={reset}
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
        ) : tags.length === 0 ? (
          <p className="p-8 text-center text-sm text-gray-400">Keine Tags vorhanden.</p>
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
                    Bearbeiten
                  </button>
                  <button
                    onClick={() => {
                      if (confirm(`Tag "${tag.name}" löschen?`)) {
                        deleteMutation.mutate(tag.id);
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
