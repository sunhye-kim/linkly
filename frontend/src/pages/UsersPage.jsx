import { useState } from 'react';
import UserList from '../components/UserList';
import UserForm from '../components/UserForm';

function UsersPage() {
  const [showForm, setShowForm] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [refreshKey, setRefreshKey] = useState(0);

  const handleEdit = (user) => {
    setSelectedUser(user);
    setShowForm(true);
  };

  const handleSuccess = () => {
    setShowForm(false);
    setSelectedUser(null);
    setRefreshKey(prev => prev + 1);
  };

  const handleCancel = () => {
    setShowForm(false);
    setSelectedUser(null);
  };

  const handleNewUser = () => {
    setSelectedUser(null);
    setShowForm(true);
  };

  return (
    <div className="page-content">
      <div className="page-header">
        <h1>유저 관리</h1>
        {!showForm && (
          <button onClick={handleNewUser} className="btn-primary">
            새 유저 생성
          </button>
        )}
      </div>

      {showForm ? (
        <UserForm
          user={selectedUser}
          onSuccess={handleSuccess}
          onCancel={handleCancel}
        />
      ) : (
        <UserList
          onEdit={handleEdit}
          onRefresh={refreshKey}
        />
      )}
    </div>
  );
}

export default UsersPage;
