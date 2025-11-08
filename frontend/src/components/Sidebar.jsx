import { NavLink } from 'react-router-dom';

function Sidebar() {
  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <h2>Linkly</h2>
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
    </aside>
  );
}

export default Sidebar;