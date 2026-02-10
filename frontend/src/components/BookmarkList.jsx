import { useState, useEffect, useMemo } from 'react';
import { bookmarkApi } from '../api/bookmarkApi';
import { linkHealthApi } from '../api/linkHealthApi';

const STATUS_LABEL = {
  HEALTHY: '정상',
  DEAD: '불량',
  TIMEOUT: '타임아웃',
  UNKNOWN: '알수없음',
};

function BookmarkList({ onEdit, onRefresh, selectedCategoryId, keyword }) {
  const [bookmarks, setBookmarks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  // bookmarkId → { status, httpStatus, responseTimeMs, checkedAt }
  const [healthMap, setHealthMap] = useState({});
  // 현재 체크 중인 bookmarkId 집합
  const [checkingIds, setCheckingIds] = useState(new Set());

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

  const fetchSearchResults = async (kw, catId) => {
    setLoading(true);
    setError(null);
    try {
      const data = await bookmarkApi.searchBookmarks(kw, catId);
      setBookmarks(data);
    } catch (err) {
      setError(err.response?.data?.message || '검색에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const fetchHealthResults = async () => {
    try {
      const results = await linkHealthApi.getMyResults();
      const map = {};
      results.forEach((r) => {
        map[r.bookmarkId] = r;
      });
      setHealthMap(map);
    } catch (err) {
      // 헬스체크 결과는 부가 정보이므로 실패해도 화면에 영향 없음
      console.error('헬스체크 결과 조회 실패:', err);
    }
  };

  // 헬스체크 결과는 북마크 추가/수정/삭제 시에만 갱신
  useEffect(() => {
    fetchHealthResults();
  }, [onRefresh]);

  // 북마크 목록: keyword가 있으면 300ms debounce 후 서버 검색, 없으면 즉시 전체 목록 조회
  useEffect(() => {
    if (!keyword || !keyword.trim()) {
      fetchBookmarks();
      return;
    }
    const timer = setTimeout(() => {
      fetchSearchResults(keyword, selectedCategoryId);
    }, 300);
    return () => clearTimeout(timer);
  }, [keyword, selectedCategoryId, onRefresh]);

  // keyword가 있으면 서버에서 이미 필터링 완료, 없으면 client-side 카테고리 필터 적용
  const filteredBookmarks = useMemo(() => {
    if (keyword && keyword.trim()) return bookmarks;
    if (selectedCategoryId === null) return bookmarks;
    return bookmarks.filter((b) => b.categoryId === selectedCategoryId);
  }, [bookmarks, selectedCategoryId, keyword]);

  const handleDelete = async (id) => {
    if (!window.confirm('정말 삭제하시겠습니까?')) return;
    try {
      await bookmarkApi.deleteBookmark(id);
      await fetchBookmarks();
    } catch (err) {
      alert(err.response?.data?.message || '삭제에 실패했습니다.');
    }
  };

  const handleHealthCheck = async (bookmarkId) => {
    setCheckingIds((prev) => new Set(prev).add(bookmarkId));
    try {
      const result = await linkHealthApi.checkNow(bookmarkId);
      setHealthMap((prev) => ({ ...prev, [bookmarkId]: result }));
    } catch (err) {
      alert(err.response?.data?.error?.message || '헬스체크에 실패했습니다.');
    } finally {
      setCheckingIds((prev) => {
        const next = new Set(prev);
        next.delete(bookmarkId);
        return next;
      });
    }
  };

  if (loading) return <div className="loading">로딩중...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="bookmark-list">
      <h2>북마크 목록</h2>
      <div className="bookmark-grid">
        {filteredBookmarks.length === 0 ? (
          <div className="empty-message">
            {keyword && keyword.trim()
              ? '검색 결과가 없습니다.'
              : bookmarks.length === 0
              ? '등록된 북마크가 없습니다.'
              : '해당 카테고리의 북마크가 없습니다.'}
          </div>
        ) : (
          filteredBookmarks.map((bookmark) => {
            const health = healthMap[bookmark.id];
            const isChecking = checkingIds.has(bookmark.id);
            return (
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
                  <div className="bookmark-footer-left">
                    <span className="bookmark-date">
                      {new Date(bookmark.createdAt).toLocaleDateString()}
                    </span>
                    {health ? (
                      <span
                        className={`health-badge health-badge--${health.status.toLowerCase()}`}
                        title={`HTTP ${health.httpStatus ?? '-'} · ${health.responseTimeMs}ms · ${new Date(health.checkedAt).toLocaleString()}`}
                      >
                        {STATUS_LABEL[health.status] ?? health.status}
                      </span>
                    ) : (
                      <span className="health-badge health-badge--unchecked">미체크</span>
                    )}
                  </div>
                  <div className="bookmark-actions">
                    <button className="btn-edit" onClick={() => onEdit(bookmark)}>
                      수정
                    </button>
                    <button className="btn-delete" onClick={() => handleDelete(bookmark.id)}>
                      삭제
                    </button>
                    <button
                      className="btn-check"
                      onClick={() => handleHealthCheck(bookmark.id)}
                      disabled={isChecking}
                    >
                      {isChecking ? '체크중...' : '체크'}
                    </button>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}

export default BookmarkList;
