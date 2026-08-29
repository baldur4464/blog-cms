import { useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { mediaApi } from '../api/media';
import { useToast } from '../components/Toast';

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export default function MediaPage() {
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const { t } = useTranslation();
  const fileRef = useRef<HTMLInputElement>(null);
  const [page, setPage] = useState(0);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: ['media', page],
    queryFn: () => mediaApi.list({ page, size: 20 }),
  });

  const deleteMutation = useMutation({
    mutationFn: mediaApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['media'] });
      showToast(t('media.deleted'));
    },
    onError: () => showToast(t('media.deleteError'), 'error'),
  });

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setError('');
    setUploading(true);
    try {
      await mediaApi.upload(file);
      queryClient.invalidateQueries({ queryKey: ['media'] });
      showToast(t('media.uploaded'));
    } catch {
      setError(t('media.uploadHint'));
      showToast(t('media.uploadFailed'), 'error');
    } finally {
      setUploading(false);
      if (fileRef.current) fileRef.current.value = '';
    }
  };

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-xl font-semibold text-gray-900">{t('media.title')}</h1>
        <div>
          <input
            ref={fileRef}
            type="file"
            accept="image/*,application/pdf"
            onChange={handleUpload}
            className="hidden"
          />
          <button
            onClick={() => fileRef.current?.click()}
            disabled={uploading}
            className="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
          >
            {uploading ? t('media.uploading') : t('media.upload')}
          </button>
        </div>
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 text-red-600 text-sm rounded-lg">{error}</div>
      )}

      {isLoading ? (
        <p className="text-sm text-gray-400">{t('common.loading')}</p>
      ) : data?.content.length === 0 ? (
        <div className="text-center py-16 text-gray-400 text-sm">
          {t('media.empty')}
        </div>
      ) : (
        <>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
            {data?.content.map((media) => (
              <div
                key={media.id}
                className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm group"
              >
                {media.mimeType.startsWith('image/') ? (
                  <img
                    src={media.url}
                    alt={media.originalName}
                    className="w-full h-36 object-cover"
                  />
                ) : (
                  <div className="w-full h-36 bg-gray-100 flex items-center justify-center text-gray-400 text-xs">
                    PDF
                  </div>
                )}
                <div className="p-2">
                  <p
                    className="text-xs text-gray-700 truncate font-medium"
                    title={media.originalName}
                  >
                    {media.originalName}
                  </p>
                  <p className="text-xs text-gray-400">{formatBytes(media.size)}</p>
                  <div className="flex items-center justify-between mt-1.5">
                    <a
                      href={media.url}
                      target="_blank"
                      rel="noreferrer"
                      className="text-xs text-blue-600 hover:text-blue-800"
                    >
                      {t('common.open')}
                    </a>
                    <button
                      onClick={() => {
                        if (confirm(t('media.confirmDelete'))) {
                          deleteMutation.mutate(media.id);
                        }
                      }}
                      className="text-xs text-red-500 hover:text-red-700"
                    >
                      {t('common.delete')}
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {data && data.totalPages > 1 && (
            <div className="flex items-center gap-3 mt-6">
              <button
                onClick={() => setPage((p) => p - 1)}
                disabled={page === 0}
                className="text-xs px-3 py-1.5 border border-gray-300 rounded-lg disabled:opacity-40 hover:bg-gray-50"
              >
                {t('common.prev')}
              </button>
              <span className="text-xs text-gray-600">
                {page + 1} / {data.totalPages}
              </span>
              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={page >= data.totalPages - 1}
                className="text-xs px-3 py-1.5 border border-gray-300 rounded-lg disabled:opacity-40 hover:bg-gray-50"
              >
                {t('common.next')}
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
