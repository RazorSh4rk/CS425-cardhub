import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import api from '../api';

export default function LoginPage() {
  const [tab, setTab] = useState('login');
  const [form, setForm] = useState({ email: '', password: '', name: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async e => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      delete api.defaults.headers.common['Authorization'];
      const endpoint = tab === 'login' ? '/api/auth/login' : '/api/auth/register';
      const payload = tab === 'login'
        ? { email: form.email, password: form.password }
        : { email: form.email, password: form.password, name: form.name };
      const res = await api.post(endpoint, payload);
      localStorage.setItem('token', res.data.token);
      const meRes = await api.get('/api/users/me');
      login(res.data.token, meRes.data);
      navigate('/');
    } catch (err) {
      const data = err.response?.data;
      if (data?.error) setError(data.error);
      else if (data?.errors) setError(data.errors.map(e => e.message).join(', '));
      else setError('Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', padding: '48px 16px' }}>
      <div className="form-card" style={{ width: '100%' }}>
        <h1 style={{ marginBottom: 20 }}>CardHub</h1>
        <div className="tabs" style={{ marginBottom: 20 }}>
          <button className={tab === 'login' ? 'active' : ''} onClick={() => { setTab('login'); setError(''); }}>
            Login
          </button>
          <button className={tab === 'register' ? 'active' : ''} onClick={() => { setTab('register'); setError(''); }}>
            Register
          </button>
        </div>
        {error && <div className="error-msg">{error}</div>}
        <form onSubmit={handleSubmit}>
          {tab === 'register' && (
            <div className="form-group">
              <label>Full Name</label>
              <input name="name" value={form.name} onChange={handleChange} required placeholder="Levi Szabo" />
            </div>
          )}
          <div className="form-group">
            <label>Email</label>
            <input type="email" name="email" value={form.email} onChange={handleChange} required placeholder="you@example.com" />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" name="password" value={form.password} onChange={handleChange} required minLength={tab === 'register' ? 8 : 1} placeholder={tab === 'register' ? 'Min 8 characters' : ''} />
          </div>
          <button type="submit" className="btn btn-primary" disabled={loading} style={{ width: '100%', justifyContent: 'center', padding: '10px' }}>
            {loading ? 'Please wait...' : tab === 'login' ? 'Login' : 'Create Account'}
          </button>
        </form>
      </div>
    </div>
  );
}
