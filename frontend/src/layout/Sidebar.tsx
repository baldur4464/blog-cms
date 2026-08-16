import { NavLink } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

const navItems = [
  { to: '/posts', label: 'Beiträge' },
  { to: '/categories', label: 'Kategorien' },
  { to: '/tags', label: 'Tags' },
  { to: '/media', label: 'Medien' },
];

export default function Sidebar() {
  const { user, logout } = useAuth();

  return (
    <aside className="w-56 bg-gray-900 text-gray-300 flex flex-col min-h-screen shrink-0">
      <div className="px-4 py-5 border-b border-gray-700">
        <span className="text-white font-semibold text-sm tracking-wide">Blog CMS</span>
      </div>

      <nav className="flex-1 py-4 space-y-1 px-2">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `block px-3 py-2 rounded-lg text-sm transition-colors ${
                isActive
                  ? 'bg-blue-600 text-white'
                  : 'hover:bg-gray-700 hover:text-white'
              }`
            }
          >
            {item.label}
          </NavLink>
        ))}
        {user?.role === 'ADMIN' && (
          <NavLink
            to="/users"
            className={({ isActive }) =>
              `block px-3 py-2 rounded-lg text-sm transition-colors ${
                isActive
                  ? 'bg-blue-600 text-white'
                  : 'hover:bg-gray-700 hover:text-white'
              }`
            }
          >
            Benutzer
          </NavLink>
        )}
      </nav>

      <div className="px-4 py-4 border-t border-gray-700">
        <p className="text-xs text-gray-500 truncate mb-2">{user?.username}</p>
        <button
          onClick={logout}
          className="text-xs text-gray-400 hover:text-white transition-colors"
        >
          Abmelden
        </button>
      </div>
    </aside>
  );
}
