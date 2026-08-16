import { type FormEvent, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { postsApi, type PostStatus, type UpdatePostRequest } from '../api/posts';
import { categoriesApi } from '../api/categories';
import { tagsApi } from '../api/tags';
import { useAuth } from '../auth/AuthContext';
import { useToast } from '../components/Toast';

const STATUS_LABELS: Record<PostStatus, string> = {
  DRAFT: 'Entwurf',
  REVIEW: 'Review',
  PUBLISHED: 'Veröffentlicht',
  ARCHIVED: 'Archiviert',
};

function allowedTransitions(current: PostStatus, role: string): PostStatus[] {
  const isEditor = role === 'ADMIN' || role === 'EDITOR';
  switch (current) {
    case 'DRAFT':
      return isEditor ? ['REVIEW', 'PUBLISHED'] : ['REVIEW'];
    case 'REVIEW':
      return isEditor ? ['PUBLISHED', 'DRAFT'] : [];
    case 'PUBLISHED':
      return isEditor ? ['ARCHIVED', 'DRAFT'] : [];
    case 'ARCHIVED':
      return isEditor ? ['DRAFT'] : [];
    default:
      return [];
  }
}

export default function PostFormPage() {
  const { id } = useParams<{ id: string }>();
  const isEdit = !!id;
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const { showToast } = useToast();

  const [title, setTitle] = useState('');
  const [excerpt, setExcerpt] = useState('');
  const [content, setContent] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [selectedTagIds, setSelectedTagIds] = useState<string[]>([]);
  const [error, setError] = useState('');

  const { data: post } = useQuery({
    queryKey: ['post', id],
    queryFn: () => postsApi.get(id!),
    enabled: isEdit,
  });

  const { data: categories = [] } = useQuery({
    queryKey: ['categories-public'],
    queryFn: categoriesApi.listPublic,
  });

  const { data: tags = [] } = useQuery({
    queryKey: ['tags-public'],
    queryFn: tagsApi.listPublic,
  });

  useEffect(() => {
    if (post) {
      setTitle(post.title);
      setExcerpt(post.excerpt ?? '');
      setContent(post.content);
      setCategoryId(post.categoryId ?? '');
      setSelectedTagIds(post.tagIds ?? []);
    }
  }, [post]);

  const createMutation = useMutation({
    mutationFn: postsApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['posts'] });
      showToast('Beitrag erfolgreich erstellt.');
      navigate('/posts');
    },
    onError: () => {
      showToast('Fehler beim Erstellen des Beitrags.', 'error');
      setError('Fehler beim Speichern.');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ postId, data }: { postId: string; data: UpdatePostRequest }) =>
      postsApi.update(postId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['posts'] });
      queryClient.invalidateQueries({ queryKey: ['post', id] });
      showToast('Beitrag gespeichert.');
    },
    onError: () => {
      showToast('Fehler beim Speichern.', 'error');
      setError('Fehler beim Speichern.');
    },
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: PostStatus }) =>
      postsApi.changeStatus(id, status),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['post', id] });
      queryClient.invalidateQueries({ queryKey: ['posts'] });
      showToast(`Status geändert zu: ${STATUS_LABELS[data.status]}.`);
    },
    onError: () => {
      showToast('Statuswechsel nicht erlaubt.', 'error');
      setError('Statuswechsel nicht erlaubt.');
    },
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    setError('');
    const data = {
      title,
      content,
      excerpt: excerpt || undefined,
      categoryId: categoryId || undefined,
      tagIds: selectedTagIds,
    };
    if (isEdit) {
      updateMutation.mutate({ postId: id!, data });
    } else {
      createMutation.mutate(data);
    }
  };

  const toggleTag = (tagId: string) => {
    setSelectedTagIds((prev) =>
      prev.includes(tagId) ? prev.filter((t) => t !== tagId) : [...prev, tagId],
    );
  };

  const transitions = post ? allowedTransitions(post.status, user?.role ?? 'AUTHOR') : [];
  const isSaving = createMutation.isPending || updateMutation.isPending;

  return (
    <div className="p-8 max-w-4xl">
      <div className="flex items-center gap-4 mb-6">
        <button
          onClick={() => navigate('/posts')}
          className="text-sm text-gray-500 hover:text-gray-700"
        >
          ← Zurück
        </button>
        <h1 className="text-xl font-semibold text-gray-900">
          {isEdit ? 'Beitrag bearbeiten' : 'Neuer Beitrag'}
        </h1>
        {post && (
          <span className="text-xs px-2 py-0.5 bg-gray-100 text-gray-600 rounded-full">
            {STATUS_LABELS[post.status]}
          </span>
        )}
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 text-red-600 text-sm rounded-lg">{error}</div>
      )}

      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Titel *</label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
            maxLength={200}
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Zusammenfassung</label>
          <input
            type="text"
            value={excerpt}
            onChange={(e) => setExcerpt(e.target.value)}
            maxLength={500}
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Kategorie</label>
            <select
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Keine Kategorie</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Tags</label>
            <div className="border border-gray-300 rounded-lg px-3 py-2 flex flex-wrap gap-1.5 min-h-[38px]">
              {tags.map((tag) => (
                <button
                  key={tag.id}
                  type="button"
                  onClick={() => toggleTag(tag.id)}
                  className={`text-xs px-2 py-0.5 rounded-full border transition-colors ${
                    selectedTagIds.includes(tag.id)
                      ? 'bg-blue-600 text-white border-blue-600'
                      : 'bg-white text-gray-600 border-gray-300 hover:border-blue-400'
                  }`}
                >
                  {tag.name}
                </button>
              ))}
            </div>
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Inhalt * <span className="text-gray-400 font-normal">(Markdown)</span>
          </label>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
            rows={20}
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-blue-500 resize-y"
          />
        </div>

        <div className="flex items-center gap-3 pt-2">
          <button
            type="submit"
            disabled={isSaving}
            className="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-sm font-medium px-5 py-2 rounded-lg transition-colors"
          >
            {isSaving ? 'Speichern…' : 'Speichern'}
          </button>

          {isEdit && post && (
            <select
              value=""
              disabled={statusMutation.isPending || transitions.length === 0}
              onChange={(e) => {
                const status = e.target.value as PostStatus;
                if (status) statusMutation.mutate({ id: id!, status });
              }}
              className="text-sm border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <option value="">
                {statusMutation.isPending ? 'Wird geändert…' : `Status: ${STATUS_LABELS[post.status]}`}
              </option>
              {transitions.map((s) => (
                <option key={s} value={s}>
                  → {STATUS_LABELS[s]}
                </option>
              ))}
            </select>
          )}

          <button
            type="button"
            onClick={() => navigate('/posts')}
            className="text-sm text-gray-500 hover:text-gray-700 px-3 py-2"
          >
            Abbrechen
          </button>
        </div>
      </form>
    </div>
  );
}
