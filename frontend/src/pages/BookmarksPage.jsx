import { useState, useEffect } from 'react';
import BookmarkList from '../components/BookmarkList';
import BookmarkForm from '../components/BookmarkForm';
import { categoryApi } from '../api/categoryApi';

function BookmarksPage() {
  const [showForm, setShowForm] = useState(false);
  const [selectedBookmark, setSelectedBookmark] = useState(null);
  const [refreshKey, setRefreshKey] = useState(0);
  const [categories, setCategories] = useState([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState(null);

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      const data = await categoryApi.getCategoriesByUserId();
      setCategories(data);
    } catch (err) {
      console.error('카테고리 목록을 불러오는데 실패했습니다:', err);
    }
  };

  const handleEdit = (bookmark) => {
    setSelectedBookmark(bookmark);
    setShowForm(true);
  };

  const handleSuccess = () => {
    setShowForm(false);
    setSelectedBookmark(null);
    setRefreshKey(prev => prev + 1);
  };

  const handleCancel = () => {
    setShowForm(false);
    setSelectedBookmark(null);
  };

  const handleNewBookmark = () => {
    setSelectedBookmark(null);
    setShowForm(true);
  };

  const handleCategoryFilter = (categoryId) => {
    setSelectedCategoryId(categoryId);
  };

  return (
    <div className="page-content">
      <div className="page-header">
        <h1>북마크 관리</h1>
        {!showForm && (
          <button onClick={handleNewBookmark} className="btn-primary">
            새 북마크 추가
          </button>
        )}
      </div>

      {!showForm && (
        <div className="category-filter">
          <button
            className={selectedCategoryId === null ? 'filter-btn active' : 'filter-btn'}
            onClick={() => handleCategoryFilter(null)}
          >
            전체
          </button>
          {categories.map((category) => (
            <button
              key={category.id}
              className={selectedCategoryId === category.id ? 'filter-btn active' : 'filter-btn'}
              onClick={() => handleCategoryFilter(category.id)}
            >
              {category.name}
            </button>
          ))}
        </div>
      )}

      {showForm ? (
        <BookmarkForm
          bookmark={selectedBookmark}
          onSuccess={handleSuccess}
          onCancel={handleCancel}
          categories={categories}
        />
      ) : (
        <BookmarkList
          onEdit={handleEdit}
          onRefresh={refreshKey}
          selectedCategoryId={selectedCategoryId}
        />
      )}
    </div>
  );
}

export default BookmarksPage;
