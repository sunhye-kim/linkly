import { useState, useEffect } from 'react';
import { categoryApi } from '../api/categoryApi';

function CategoryForm({ category, onSuccess, onCancel }) {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const isEditMode = !!category;

  useEffect(() => {
    if (category) {
      setFormData({
        name: category.name || '',
        description: category.description || '',
      });
    }
  }, [category]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const requestData = {
        name: formData.name,
        description: formData.description || null,
      };

      if (isEditMode) {
        await categoryApi.updateCategory(category.id, requestData);
      } else {
        await categoryApi.createCategory(requestData);
      }

      setFormData({ name: '', description: '' });
      onSuccess();
    } catch (err) {
      setError(err.response?.data?.message || '요청에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="category-form">
      <h2>{isEditMode ? '카테고리 수정' : '새 카테고리 생성'}</h2>
      {error && <div className="error">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="name">카테고리 이름 *</label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
            maxLength={50}
            placeholder="개발, 디자인, 마케팅 등"
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
            maxLength={255}
            placeholder="카테고리에 대한 설명을 입력하세요"
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
    </div>
  );
}

export default CategoryForm;