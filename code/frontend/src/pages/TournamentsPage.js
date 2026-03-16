import { useState, useEffect } from 'react';
import { useAuth } from '../AuthContext';
import api from '../api';

export default function TournamentsPage() {
  const { user } = useAuth();
  const [tournaments, setTournaments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: '', date: '', startTime: '', endTime: '', game: '', format: '', description: '' });
  const [availability, setAvailability] = useState(null);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const fetchTournaments = () => {
    api.get('/api/tournaments')
      .then(res => setTournaments(res.data))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchTournaments(); }, []);

  const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value });

  const toFullTime = t => (t && t.length === 5 ? t + ':00' : t);

  const checkAvailability = async () => {
    if (!form.date || !form.startTime || !form.endTime) return;
    setAvailability(null);
    try {
      const res = await api.get('/api/tournaments/availability', {
        params: {
          date: form.date,
          startTime: toFullTime(form.startTime),
          endTime: toFullTime(form.endTime),
        },
      });
      setAvailability(res.data);
    } catch {
      setAvailability(null);
    }
  };

  const handleSubmit = async e => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await api.post('/api/tournaments', {
        ...form,
        startTime: toFullTime(form.startTime),
        endTime: toFullTime(form.endTime),
        description: form.description || undefined,
      });
      setShowForm(false);
      setAvailability(null);
      setForm({ name: '', date: '', startTime: '', endTime: '', game: '', format: '', description: '' });
      setLoading(true);
      fetchTournaments();
    } catch (err) {
      setError(err.response?.data?.error || 'Error creating tournament');
    } finally {
      setSubmitting(false);
    }
  };

  const cancelTournament = async id => {
    if (!window.confirm('Cancel this tournament?')) return;
    try {
      await api.delete(`/api/tournaments/${id}`);
      fetchTournaments();
    } catch (err) {
      setError(err.response?.data?.error || 'Error cancelling tournament');
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <h1>Tournaments</h1>
        {user && !showForm && (
          <button className="btn btn-primary" onClick={() => { setShowForm(true); setError(''); }}>
            Host Tournament
          </button>
        )}
      </div>

      {error && <div className="error-msg">{error}</div>}

      {showForm && (
        <div className="form-card" style={{ marginBottom: 28, maxWidth: 560 }}>
          <h2>Host a Tournament</h2>
          <form onSubmit={handleSubmit} style={{ marginTop: 16 }}>
            <div className="form-group">
              <label>Tournament Name</label>
              <input name="name" value={form.name} onChange={handleChange} required placeholder="e.g. Friday Night Magic" />
            </div>
            <div style={{ display: 'flex', gap: 12 }}>
              <div className="form-group" style={{ flex: 1 }}>
                <label>Date</label>
                <input type="date" name="date" value={form.date} onChange={handleChange} required onBlur={checkAvailability} />
              </div>
              <div className="form-group" style={{ flex: 1 }}>
                <label>Start Time</label>
                <input type="time" name="startTime" value={form.startTime} onChange={handleChange} required onBlur={checkAvailability} />
              </div>
              <div className="form-group" style={{ flex: 1 }}>
                <label>End Time</label>
                <input type="time" name="endTime" value={form.endTime} onChange={handleChange} required onBlur={checkAvailability} />
              </div>
            </div>
            {availability !== null && (
              <div style={{ marginBottom: 12 }}>
                {availability.available
                  ? <div className="success-msg" style={{ marginBottom: 0 }}>Room is available for this time!</div>
                  : <div className="error-msg" style={{ marginBottom: 0 }}>Room is not available — time slot is taken.</div>
                }
              </div>
            )}
            <div style={{ display: 'flex', gap: 12 }}>
              <div className="form-group" style={{ flex: 1 }}>
                <label>Game</label>
                <input name="game" value={form.game} onChange={handleChange} required placeholder="e.g. Pokemon" />
              </div>
              <div className="form-group" style={{ flex: 1 }}>
                <label>Format</label>
                <input name="format" value={form.format} onChange={handleChange} required placeholder="e.g. Standard" />
              </div>
            </div>
            <div className="form-group">
              <label>Description (optional)</label>
              <textarea name="description" value={form.description} onChange={handleChange} placeholder="Extra details..." />
            </div>
            <div style={{ display: 'flex', gap: 10 }}>
              <button type="submit" className="btn btn-primary" disabled={submitting}>
                {submitting ? 'Creating...' : 'Create Tournament'}
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => { setShowForm(false); setAvailability(null); }}>
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {loading ? (
        <div className="loading">Loading tournaments...</div>
      ) : tournaments.length === 0 ? (
        <p style={{ color: '#6b7280' }}>No upcoming tournaments.</p>
      ) : (
        <div className="grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))' }}>
          {tournaments.map(t => (
            <div key={t.id} className="card">
              <h3 style={{ marginBottom: 8 }}>{t.name}</h3>
              <p className="meta">{t.date} · {t.startTime?.slice(0, 5)} – {t.endTime?.slice(0, 5)}</p>
              <p className="meta">{t.game} · {t.format}</p>
              {t.description && (
                <p style={{ fontSize: 14, color: '#374151', marginTop: 8, lineHeight: 1.5 }}>{t.description}</p>
              )}
              {user && user.id === t.organizerId && (
                <button
                  className="btn btn-danger btn-sm"
                  style={{ marginTop: 12 }}
                  onClick={() => cancelTournament(t.id)}
                >
                  Cancel Tournament
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
