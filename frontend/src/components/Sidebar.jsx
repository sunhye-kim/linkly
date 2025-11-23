import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function Sidebar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <h2>Linkly</h2>
        {user && <p className="user-info">{user.name}님</p>}
      </div>
      <nav className="sidebar-nav">
        <NavLink
          to="/bookmarks"
          className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}
        >
          북마크 관리
        </NavLink>
        <NavLink
          to="/categories"
          className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}
        >
          카테고리 관리
        </NavLink>
        <NavLink
          to="/users"
          className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}
        >
          유저 관리
        </NavLink>
      </nav>
      <div className="sidebar-footer">
        <button onClick={handleLogout} className="logout-button">
          로그아웃
        </button>
      </div>
    </aside>
  );
}

export default Sidebar;