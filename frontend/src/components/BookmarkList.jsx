import { useState, useEffect, useMemo } from 'react';
import { bookmarkApi } from '../api/bookmarkApi';

function BookmarkList({ onEdit, onRefresh, selectedCategoryId }) {
  const [bookmarks, setBookmarks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchBookmarks = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await bookmarkApi.getBookmarksByUserId();
      setBookmarks(data);
    } catch (err) {
      setError(err.response?.data?.message || '북마크 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookmarks();
  }, [onRefresh]);

  // 카테고리 필터링
  const filteredBookmarks = useMemo(() => {
    if (selectedCategoryId === null) {
      return bookmarks;
    }
    return bookmarks.filter(bookmark => bookmark.categoryId === selectedCategoryId);
  }, [bookmarks, selectedCategoryId]);

  const handleDelete = async (id) => {
    if (!window.confirm('정말 삭제하시겠습니까?')) {
      return;
    }

    try {
      await bookmarkApi.deleteBookmark(id);
      await fetchBookmarks();
    } catch (err) {
      alert(err.response?.data?.message || '삭제에 실패했습니다.');
    }
  };

  if (loading) {
    return <div className="loading">로딩중...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  return (
    <div className="bookmark-list">
      <h2>북마크 목록</h2>
      <div className="bookmark-grid">
        {filteredBookmarks.length === 0 ? (
          <div className="empty-message">
            {bookmarks.length === 0 ? '등록된 북마크가 없습니다.' : '해당 카테고리의 북마크가 없습니다.'}
          </div>
        ) : (
          filteredBookmarks.map((bookmark) => (
            <div key={bookmark.id} className="bookmark-card">
              <div className="bookmark-header">
                <h3>{bookmark.title}</h3>
                {bookmark.categoryName && (
                  <span className="category-badge">{bookmark.categoryName}</span>
                )}
              </div>
              <a
                href={bookmark.url}
                target="_blank"
                rel="noopener noreferrer"
                className="bookmark-url"
              >
                {bookmark.url}
              </a>
              {bookmark.description && (
                <p className="bookmark-description">{bookmark.description}</p>
              )}
              {bookmark.tags && bookmark.tags.length > 0 && (
                <div className="bookmark-tags">
                  {bookmark.tags.map((tag, index) => (
                    <span key={index} className="tag">
                      {tag}
                    </span>
                  ))}
                </div>
              )}
              <div className="bookmark-footer">
                <span className="bookmark-date">
                  {new Date(bookmark.createdAt).toLocaleDateString()}
                </span>
                <div className="bookmark-actions">
                  <button onClick={() => onEdit(bookmark)}>수정</button>
                  <button onClick={() => handleDelete(bookmark.id)}>삭제</button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default BookmarkList;