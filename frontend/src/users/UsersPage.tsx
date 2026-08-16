import { type FormEvent, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { usersApi, type UserResponse, type UserRole } from '../api/users';
import { useAuth } from '../auth/AuthContext';
import { useToast } from '../components/Toast';

interface CreateForm {
  username: string;
  email: string;
  password: string;
  role: UserRole;
}

const ROLES: UserRole[] = ['AUTHOR', 'EDITOR', 'ADMIN'];

export default function UsersPage() {
  const queryClient = useQueryClient();
  const { user: currentUser } = useAuth();
  const { showToast } = useToast();
  const [page, setPage] = useState(0);
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState<CreateForm>({ username: '', email: '', password: '', role: 'AUTHOR' });
  const [resetTarget, setResetTarget] = useState<UserResponse | null>(null);
  const [newPassword, setNewPassword] = useState('');
  const [error, setError] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: ['users', page],
    queryFn: () => usersApi.list({ page, size: 15 }),
  });

  const createMutation = useMutation({
    mutationFn: usersApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      showToast('Benutzer erstellt.');
      setShowCreate(false);
      setForm({ username: '', email: '', password: '', role: 'AUTHOR' });
      setError('');
    },
    onError: () => {
      showToast('Benutzername oder E-Mail bereits vergeben.', 'error');
      setError('Benutzername oder E-Mail bereits vergeben.');
    },
  });

  const deactivateMutation = useMutation({
    mutationFn: usersApi.deactivate,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      showToast('Benutzer deaktiviert.');
    },
    onError: () => showToast('Benutzer konnte nicht deaktiviert werden.', 'error'),
  });

  const resetMutation = useMutation({
    mutationFn: ({ id, newPassword }: { id: string; newPassword: string }) =>
      usersApi.resetPassword(id, { newPassword }),
    onSuccess: () => {
      showToast('Passwort zurückgesetzt.');
      setResetTarget(null);
      setNewPassword('');
    },
    onError: () => showToast('Passwort konnte nicht zurückgesetzt werden.', 'error'),
  });

  const handleCreate = (e: FormEvent) => {
    e.preventDefault();
    setError('');
    createMutation.mutate(form);
  };

  if (isLoading) return <div className="p-8 text-sm text-gray-400">Laden…</div>;

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-xl font-semibold text-gray-900">Benutzer</h1>
        <button
          onClick={() => setShowCreate(true)}
          className="bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
        >
          Neuer Benutzer
        </button>
      </div>

      {showCreate && (
        <form
          onSubmit={handleCreate}
          className="bg-white rounded-xl border border-gray-200 p-5 mb-6 shadow-sm max-w-lg"
        >
          <h2 className="text-sm font-medium text-gray-700 mb-4">Neuer Benutzer</h2>
          {error && <p className="text-red-500 text-sm mb-3">{error}</p>}
          <div className="space-y-3">
            <input
              type="text"
              placeholder="Benutzername *"
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              required
              minLength={3}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <input
              type="email"
              placeholder="E-Mail *"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              required
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <input
              type="password"
              placeholder="Passwort * (min. 8 Zeichen)"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              required
              minLength={8}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <select
              value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value as UserRole })}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {ROLES.map((r) => (
                <option key={r} value={r}>{r}</option>
              ))}
            </select>
          </div>
          <div className="flex gap-2 mt-4">
            <button
              type="submit"
              disabled={createMutation.isPending}
              className="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-sm font-medium px-4 py-2 rounded-lg"
            >
              {createMutation.isPending ? 'Erstellen…' : 'Erstellen'}
            </button>
            <button
              type="button"
              onClick={() => { setShowCreate(false); setError(''); }}
              className="text-sm text-gray-500 hover:text-gray-700 px-3 py-2"
            >
              Abbrechen
            </button>
          </div>
        </form>
      )}

      {resetTarget && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-full max-w-sm shadow-xl">
            <h2 className="text-sm font-medium text-gray-700 mb-4">
              Passwort zurücksetzen für {resetTarget.username}
            </h2>
            <input
              type="password"
              placeholder="Neues Passwort (min. 8 Zeichen)"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              minLength={8}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 mb-4"
            />
            <div className="flex gap-2">
              <button
                onClick={() => resetMutation.mutate({ id: resetTarget.id, newPassword })}
                disabled={newPassword.length < 8 || resetMutation.isPending}
                className="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-sm font-medium px-4 py-2 rounded-lg"
              >
                Zurücksetzen
              </button>
              <button
                onClick={() => { setResetTarget(null); setNewPassword(''); }}
                className="text-sm text-gray-500 hover:text-gray-700 px-3 py-2"
              >
                Abbrechen
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="text-left px-4 py-3 font-medium text-gray-600">Benutzer</th>
              <th className="text-left px-4 py-3 font-medium text-gray-600">Rolle</th>
              <th className="text-left px-4 py-3 font-medium text-gray-600">Status</th>
              <th className="text-left px-4 py-3 font-medium text-gray-600">Erstellt</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {data?.content.map((u) => (
              <tr key={u.id} className="hover:bg-gray-50">
                <td className="px-4 py-3">
                  <p className="font-medium text-gray-900">{u.username}</p>
                  <p className="text-xs text-gray-500">{u.email}</p>
                </td>
                <td className="px-4 py-3">
                  <span className="text-xs px-2 py-0.5 bg-blue-50 text-blue-700 rounded-full">
                    {u.role}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <span
                    className={`text-xs px-2 py-0.5 rounded-full ${
                      u.status === 'ACTIVE'
                        ? 'bg-green-100 text-green-700'
                        : 'bg-gray-100 text-gray-500'
                    }`}
                  >
                    {u.status === 'ACTIVE' ? 'Aktiv' : 'Inaktiv'}
                  </span>
                </td>
                <td className="px-4 py-3 text-gray-500">
                  {new Date(u.createdAt).toLocaleDateString('de-DE')}
                </td>
                <td className="px-4 py-3 text-right">
                  <div className="flex justify-end gap-3">
                    <button
                      onClick={() => setResetTarget(u)}
                      className="text-xs text-blue-600 hover:text-blue-800"
                    >
                      Passwort
                    </button>
                    {u.id !== currentUser?.userId && u.status === 'ACTIVE' && (
                      <button
                        onClick={() => {
                          if (confirm(`Benutzer "${u.username}" deaktivieren?`)) {
                            deactivateMutation.mutate(u.id);
                          }
                        }}
                        className="text-xs text-red-500 hover:text-red-700"
                      >
                        Deaktivieren
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {data && data.totalPages > 1 && (
          <div className="flex items-center justify-between px-4 py-3 border-t border-gray-100">
            <span className="text-xs text-gray-500">{data.totalElements} Benutzer</span>
            <div className="flex gap-2">
              <button
                onClick={() => setPage((p) => p - 1)}
                disabled={page === 0}
                className="text-xs px-3 py-1.5 border border-gray-300 rounded-lg disabled:opacity-40 hover:bg-gray-50"
              >
                Zurück
              </button>
              <span className="text-xs px-3 py-1.5 text-gray-600">
                {page + 1} / {data.totalPages}
              </span>
              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={page >= data.totalPages - 1}
                className="text-xs px-3 py-1.5 border border-gray-300 rounded-lg disabled:opacity-40 hover:bg-gray-50"
              >
                Weiter
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
