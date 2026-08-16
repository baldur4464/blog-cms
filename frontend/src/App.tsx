import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './auth/AuthContext';
import { ToastProvider } from './components/Toast';
import AppLayout from './layout/AppLayout';
import LoginPage from './auth/LoginPage';
import PostListPage from './posts/PostListPage';
import PostFormPage from './posts/PostFormPage';
import CategoriesPage from './categories/CategoriesPage';
import TagsPage from './tags/TagsPage';
import MediaPage from './media/MediaPage';
import UsersPage from './users/UsersPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, staleTime: 30_000 },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ToastProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route element={<AppLayout />}>
              <Route index element={<Navigate to="/posts" replace />} />
              <Route path="/posts" element={<PostListPage />} />
              <Route path="/posts/new" element={<PostFormPage />} />
              <Route path="/posts/:id/edit" element={<PostFormPage />} />
              <Route path="/categories" element={<CategoriesPage />} />
              <Route path="/tags" element={<TagsPage />} />
              <Route path="/media" element={<MediaPage />} />
              <Route path="/users" element={<UsersPage />} />
            </Route>
          </Routes>
        </BrowserRouter>
        </ToastProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
}
