import { useState, useEffect } from 'react';
import { userApi } from '../api/userApi';

function UserForm({ user, onSuccess, onCancel }) {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    name: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const isEditMode = !!user;

  useEffect(() => {
    if (user) {
      setFormData({
        email: user.email,
        password: '',
        name: user.name,
      });
    }
  }, [user]);

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
      if (isEditMode) {
        // 수정 모드: password와 name만 전송 (빈 값은 제외)
        const updateData = {};
        if (formData.password) updateData.password = formData.password;
        if (formData.name) updateData.name = formData.name;

        await userApi.updateUser(user.id, updateData);
      } else {
        // 생성 모드: 전체 데이터 전송
        await userApi.createUser(formData);
      }

      setFormData({ email: '', password: '', name: '' });
      onSuccess();
    } catch (err) {
      setError(err.response?.data?.message || '요청에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="user-form">
      <h2>{isEditMode ? '유저 수정' : '새 유저 생성'}</h2>
      {error && <div className="error">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="email">이메일</label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            disabled={isEditMode}
            required={!isEditMode}
            placeholder="user@example.com"
          />
        </div>

        <div className="form-group">
          <label htmlFor="password">
            비밀번호 {isEditMode && '(변경시만 입력)'}
          </label>
          <input
            type="password"
            id="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required={!isEditMode}
            minLength={8}
            placeholder="최소 8자 이상"
          />
        </div>

        <div className="form-group">
          <label htmlFor="name">이름</label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
            maxLength={50}
            placeholder="홍길동"
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

export default UserForm;