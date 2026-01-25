import { useState } from 'react';
import { categoryApi } from '../api/categoryApi';

function CategoryModal({ onClose, onCategoryCreated }) {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [isDuplicateError, setIsDuplicateError] = useState(false);

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
    setIsDuplicateError(false);

    try {
      const requestData = {
        name: formData.name,
        description: formData.description || null,
      };

      const newCategory = await categoryApi.createCategory(requestData);
      onCategoryCreated(newCategory);
      onClose();
    } catch (err) {
      const message = err.message;
      const isDuplicate = message.includes('이미 사용 중인') || message.includes('중복');
      setIsDuplicateError(isDuplicate);
      setError(isDuplicate ? `"${formData.name}" 카테고리가 이미 존재합니다. 다른 이름을 입력해주세요.` : message);
    } finally {
      setLoading(false);
    }
  };

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div className="modal-backdrop" onClick={handleBackdropClick}>
      <div className="modal-content">
        <div className="modal-header">
          <h3>새 카테고리 추가</h3>
          <button className="modal-close" onClick={onClose} type="button">
            ×
          </button>
        </div>

        {error && <div className={isDuplicateError ? "warning" : "error"}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="modal-name">카테고리 이름 *</label>
            <input
              type="text"
              id="modal-name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
              maxLength={50}
              placeholder="개발, 디자인, 마케팅 등"
              autoFocus
            />
          </div>

          <div className="form-group">
            <label htmlFor="modal-description">설명</label>
            <textarea
              id="modal-description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows={3}
              maxLength={255}
              placeholder="카테고리에 대한 설명 (선택)"
            />
          </div>

          <div className="form-actions">
            <button type="submit" disabled={loading}>
              {loading ? '생성 중...' : '생성'}
            </button>
            <button type="button" onClick={onClose} disabled={loading}>
              취소
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default CategoryModal;
