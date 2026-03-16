import { useState, useEffect } from 'react';
import { useAuth } from '../AuthContext';
import api from '../api';

export default function TradePage() {
  const { user } = useAuth();
  const [trades, setTrades] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState({ title: '', description: '', contactPreferences: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const fetchTrades = () => {
    api.get('/api/trades')
      .then(res => setTrades(res.data))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchTrades(); }, []);

  const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value });

  const openCreate = () => {
    setForm({ title: '', description: '', contactPreferences: '' });
    setEditingId(null);
    setError('');
    setShowForm(true);
  };

  const openEdit = trade => {
    setForm({ title: trade.title, description: trade.description, contactPreferences: trade.contactPreferences || '' });
    setEditingId(trade.id);
    setError('');
    setShowForm(true);
  };

  const handleSubmit = async e => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      if (editingId) {
        await api.put(`/api/trades/${editingId}`, form);
      } else {
        await api.post('/api/trades', form);
      }
      setShowForm(false);
      setEditingId(null);
      fetchTrades();
    } catch (err) {
      setError(err.response?.data?.error || 'Error saving trade offer');
    } finally {
      setSubmitting(false);
    }
  };

  const deleteTrade = async id => {
    if (!window.confirm('Delete this trade offer?')) return;
    try {
      await api.delete(`/api/trades/${id}`);
      fetchTrades();
    } catch (err) {
      setError(err.response?.data?.error || 'Error deleting trade offer');
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <h1>Trade Board</h1>
        {user && !showForm && (
          <button className="btn btn-primary" onClick={openCreate}>Post Trade Offer</button>
        )}
      </div>

      {error && <div className="error-msg">{error}</div>}

      {showForm && (
        <div className="form-card" style={{ marginBottom: 28 }}>
          <h2>{editingId ? 'Edit Trade Offer' : 'New Trade Offer'}</h2>
          <form onSubmit={handleSubmit} style={{ marginTop: 16 }}>
            <div className="form-group">
              <label>Title</label>
              <input name="title" value={form.title} onChange={handleChange} required placeholder="e.g. Looking for Charizard" />
            </div>
            <div className="form-group">
              <label>Description</label>
              <textarea name="description" value={form.description} onChange={handleChange} required placeholder="What you have, what you want..." />
            </div>
            <div className="form-group">
              <label>Contact Preferences (optional)</label>
              <input name="contactPreferences" value={form.contactPreferences} onChange={handleChange} placeholder="e.g. Meet at store, email preferred" />
            </div>
            <div style={{ display: 'flex', gap: 10 }}>
              <button type="submit" className="btn btn-primary" disabled={submitting}>
                {submitting ? 'Saving...' : editingId ? 'Update Offer' : 'Post Offer'}
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => { setShowForm(false); setEditingId(null); }}>
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {loading ? (
        <div className="loading">Loading trades...</div>
      ) : trades.length === 0 ? (
        <p style={{ color: '#6b7280' }}>No active trade offers.</p>
      ) : (
        <div className="grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))' }}>
          {trades.map(trade => (
            <div key={trade.id} className="card">
              <h3 style={{ marginBottom: 8 }}>{trade.title}</h3>
              <p style={{ fontSize: 14, color: '#374151', lineHeight: 1.5 }}>{trade.description}</p>
              {trade.contactPreferences && (
                <p className="meta" style={{ marginTop: 8 }}>Contact: {trade.contactPreferences}</p>
              )}
              <p className="meta" style={{ marginTop: 6 }}>Posted {new Date(trade.createdAt).toLocaleDateString()}</p>
              <p className="meta">Expires {new Date(trade.expiresAt).toLocaleDateString()}</p>
              {user && user.id === trade.creatorId && (
                <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
                  <button className="btn btn-secondary btn-sm" onClick={() => openEdit(trade)}>Edit</button>
                  <button className="btn btn-danger btn-sm" onClick={() => deleteTrade(trade.id)}>Delete</button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
