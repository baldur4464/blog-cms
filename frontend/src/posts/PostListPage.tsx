import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { postsApi, type PostStatus } from '../api/posts';
import { useToast } from '../components/Toast';

const STATUS_LABELS: Record<PostStatus, string> = {
  DRAFT: 'Entwurf',
  REVIEW: 'Review',
  PUBLISHED: 'Veröffentlicht',
  ARCHIVED: 'Archiviert',
};

const STATUS_COLORS: Record<PostStatus, string> = {
  DRAFT: 'bg-gray-100 text-gray-700',
  REVIEW: 'bg-yellow-100 text-yellow-800',
  PUBLISHED: 'bg-green-100 text-green-800',
  ARCHIVED: 'bg-red-100 text-red-700',
};

export default function PostListPage() {
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  const { data, isLoading } = useQuery({
    queryKey: ['posts', page],
    queryFn: () => postsApi.list({ page, size: 15, sort: 'createdAt,desc' }),
  });

  const archiveMutation = useMutation({
    mutationFn: postsApi.archive,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['posts'] });
      showToast('Beitrag archiviert.');
    },
    onError: () => showToast('Fehler beim Archivieren.', 'error'),
  });

  if (isLoading) {
    return <div className="p-8 text-gray-500 text-sm">Laden…</div>;
  }

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-xl font-semibold text-gray-900">Beiträge</h1>
        <Link
          to="/posts/new"
          className="bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
        >
          Neuer Beitrag
        </Link>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="text-left px-4 py-3 font-medium text-gray-600">Titel</th>
              <th className="text-left px-4 py-3 font-medium text-gray-600">Status</th>
              <th className="text-left px-4 py-3 font-medium text-gray-600">Autor</th>
              <th className="text-left px-4 py-3 font-medium text-gray-600">Kategorie</th>
              <th className="text-left px-4 py-3 font-medium text-gray-600">Erstellt</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {data?.content.map((post) => (
              <tr key={post.id} className="hover:bg-gray-50">
                <td className="px-4 py-3">
                  <Link
                    to={`/posts/${post.id}/edit`}
                    className="font-medium text-gray-900 hover:text-blue-600"
                  >
                    {post.title}
                  </Link>
                </td>
                <td className="px-4 py-3">
                  <span
                    className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COLORS[post.status]}`}
                  >
                    {STATUS_LABELS[post.status]}
                  </span>
                </td>
                <td className="px-4 py-3 text-gray-600">{post.authorUsername}</td>
                <td className="px-4 py-3 text-gray-600">{post.categoryName ?? '—'}</td>
                <td className="px-4 py-3 text-gray-500">
                  {new Date(post.createdAt).toLocaleDateString('de-DE')}
                </td>
                <td className="px-4 py-3 text-right">
                  <button
                    onClick={() => {
                      if (confirm('Beitrag archivieren?')) {
                        archiveMutation.mutate(post.id);
                      }
                    }}
                    disabled={post.status === 'ARCHIVED'}
                    className="text-xs text-red-500 hover:text-red-700 disabled:opacity-30 disabled:cursor-not-allowed"
                  >
                    Archivieren
                  </button>
                </td>
              </tr>
            ))}
            {data?.content.length === 0 && (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-gray-400">
                  Noch keine Beiträge vorhanden.
                </td>
              </tr>
            )}
          </tbody>
        </table>

        {data && data.totalPages > 1 && (
          <div className="flex items-center justify-between px-4 py-3 border-t border-gray-100">
            <span className="text-xs text-gray-500">
              {data.totalElements} Beiträge insgesamt
            </span>
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
