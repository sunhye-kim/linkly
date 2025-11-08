import { useState, useEffect } from 'react';
import { categoryApi } from '../api/categoryApi';

function CategoryList({ onEdit, onRefresh }) {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchCategories = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await categoryApi.getCategoriesByUserId();
      setCategories(data);
    } catch (err) {
      setError(err.response?.data?.message || '카테고리 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, [onRefresh]);

  const handleDelete = async (id) => {
    if (!window.confirm('정말 삭제하시겠습니까?')) {
      return;
    }

    try {
      await categoryApi.deleteCategory(id);
      await fetchCategories();
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
    <div className="category-list">
      <h2>카테고리 목록</h2>
      <div className="category-grid">
        {categories.length === 0 ? (
          <div className="empty-message">등록된 카테고리가 없습니다.</div>
        ) : (
          categories.map((category) => (
            <div key={category.id} className="category-card">
              <div className="category-header">
                <h3>{category.name}</h3>
              </div>
              {category.description && (
                <p className="category-description">{category.description}</p>
              )}
              <div className="category-footer">
                <span className="category-date">
                  {new Date(category.createdAt).toLocaleDateString()}
                </span>
                <div className="category-actions">
                  <button onClick={() => onEdit(category)}>수정</button>
                  <button onClick={() => handleDelete(category.id)}>삭제</button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default CategoryList;