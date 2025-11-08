import { useState } from 'react';
import CategoryList from '../components/CategoryList';
import CategoryForm from '../components/CategoryForm';

function CategoriesPage() {
  const [showForm, setShowForm] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [refreshKey, setRefreshKey] = useState(0);

  const handleEdit = (category) => {
    setSelectedCategory(category);
    setShowForm(true);
  };

  const handleSuccess = () => {
    setShowForm(false);
    setSelectedCategory(null);
    setRefreshKey(prev => prev + 1);
  };

  const handleCancel = () => {
    setShowForm(false);
    setSelectedCategory(null);
  };

  const handleNewCategory = () => {
    setSelectedCategory(null);
    setShowForm(true);
  };

  return (
    <div className="page-content">
      <div className="page-header">
        <h1>카테고리 관리</h1>
        {!showForm && (
          <button onClick={handleNewCategory} className="btn-primary">
            새 카테고리 추가
          </button>
        )}
      </div>

      {showForm ? (
        <CategoryForm
          category={selectedCategory}
          onSuccess={handleSuccess}
          onCancel={handleCancel}
        />
      ) : (
        <CategoryList
          onEdit={handleEdit}
          onRefresh={refreshKey}
        />
      )}
    </div>
  );
}

export default CategoriesPage;