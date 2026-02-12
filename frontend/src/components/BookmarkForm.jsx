import { useState, useEffect, useRef } from 'react';
import { bookmarkApi } from '../api/bookmarkApi';
import CategoryModal from './CategoryModal';

function BookmarkForm({ bookmark, onSuccess, onCancel, categories = [], onCategoryCreated }) {
  const [formData, setFormData] = useState({
    title: '',
    url: '',
    description: '',
    categoryId: '',
    tags: '',
  });
  const [loading, setLoading] = useState(false);
  const [metadataLoading, setMetadataLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showCategoryModal, setShowCategoryModal] = useState(false);
  const metadataTimerRef = useRef(null);

  const isEditMode = !!bookmark;

  useEffect(() => {
    if (bookmark) {
      setFormData({
        title: bookmark.title || '',
        url: bookmark.url || '',
        description: bookmark.description || '',
        categoryId: bookmark.categoryId || '',
        tags: bookmark.tags ? bookmark.tags.join(', ') : '',
      });
    }
  }, [bookmark]);

  // URL 변경 시 메타데이터 자동 추출 (편집 모드 제외)
  useEffect(() => {
    if (isEditMode || !formData.url) {
      return;
    }

    // URL 형식 간단 검증
    try {
      new URL(formData.url);
    } catch {
      return;
    }

    if (metadataTimerRef.current) {
      clearTimeout(metadataTimerRef.current);
    }

    metadataTimerRef.current = setTimeout(async () => {
      setMetadataLoading(true);
      try {
        const metadata = await bookmarkApi.fetchUrlMetadata(formData.url);
        setFormData((prev) => ({
          ...prev,
          title: prev.title || metadata.title || '',
          description: prev.description || metadata.description || '',
        }));
      } catch (err) {
        console.warn('메타데이터 추출 실패:', err);
      } finally {
        setMetadataLoading(false);
      }
    }, 800);

    return () => {
      if (metadataTimerRef.current) {
        clearTimeout(metadataTimerRef.current);
      }
    };
  }, [formData.url, isEditMode]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleCategoryCreated = (newCategory) => {
    if (onCategoryCreated) {
      onCategoryCreated(newCategory);
    }
    setFormData((prev) => ({
      ...prev,
      categoryId: newCategory.id,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      // tags를 배열로 변환
      const tagsArray = formData.tags
        ? formData.tags.split(',').map(tag => tag.trim()).filter(tag => tag)
        : [];

      const requestData = {
        title: formData.title,
        url: formData.url,
        description: formData.description || null,
        categoryId: formData.categoryId ? parseInt(formData.categoryId) : null,
        tags: tagsArray,
      };

      if (isEditMode) {
        await bookmarkApi.updateBookmark(bookmark.id, requestData);
      } else {
        await bookmarkApi.createBookmark(requestData);
      }

      setFormData({ title: '', url: '', description: '', categoryId: '', tags: '' });
      onSuccess();
    } catch (err) {
      setError(err.response?.data?.message || '요청에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bookmark-form">
      <h2>{isEditMode ? '북마크 수정' : '새 북마크 생성'}</h2>
      {error && <div className="error">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="url">URL *</label>
          <input
            type="url"
            id="url"
            name="url"
            value={formData.url}
            onChange={handleChange}
            required
            maxLength={500}
            placeholder="https://example.com"
          />
          {metadataLoading && (
            <div className="metadata-loading">메타데이터 가져오는 중...</div>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="title">제목 *</label>
          <input
            type="text"
            id="title"
            name="title"
            value={formData.title}
            onChange={handleChange}
            required
            maxLength={255}
            placeholder="북마크 제목"
          />
        </div>

        <div className="form-group">
          <label htmlFor="description">설명</label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            rows={4}
            placeholder="북마크에 대한 설명을 입력하세요"
          />
        </div>

        <div className="form-group">
          <label htmlFor="categoryId">카테고리 (선택)</label>
          <div className="category-select-wrapper">
            <select
              id="categoryId"
              name="categoryId"
              value={formData.categoryId}
              onChange={handleChange}
            >
              <option value="">카테고리 없음</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
            <button
              type="button"
              className="add-category-btn"
              onClick={() => setShowCategoryModal(true)}
            >
              + 새 카테고리
            </button>
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="tags">태그 (쉼표로 구분)</label>
          <input
            type="text"
            id="tags"
            name="tags"
            value={formData.tags}
            onChange={handleChange}
            placeholder="React, JavaScript, Tutorial"
          />
        </div>

        <div className="form-actions">
          <button type="submit" disabled={loading}>
            {loading ? '처리중...' : isEditMode ? '수정' : '생성'}
          </button>
          <button type="button" onClick={onCancel} disabled={loading}>
            취소
          </button>
        </div>
      </form>

      {showCategoryModal && (
        <CategoryModal
          onClose={() => setShowCategoryModal(false)}
          onCategoryCreated={handleCategoryCreated}
        />
      )}
    </div>
  );
}

export default BookmarkForm;