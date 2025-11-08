import { Routes, Route, Navigate } from 'react-router-dom'
import './App.css'
import Sidebar from './components/Sidebar'
import BookmarksPage from './pages/BookmarksPage'
import CategoriesPage from './pages/CategoriesPage'
import UsersPage from './pages/UsersPage'

function App() {
  return (
    <div className="app">
      <Sidebar />
      <main className="main-content">
        <Routes>
          <Route path="/" element={<Navigate to="/bookmarks" replace />} />
          <Route path="/bookmarks" element={<BookmarksPage />} />
          <Route path="/categories" element={<CategoriesPage />} />
          <Route path="/users" element={<UsersPage />} />
        </Routes>
      </main>
    </div>
  )
}

export default App
