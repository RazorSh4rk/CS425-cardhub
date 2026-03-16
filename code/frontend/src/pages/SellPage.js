import { useState, useEffect } from 'react';
import api from '../api';

const GAMES = ['POKEMON', 'MAGIC_THE_GATHERING', 'YUGIOH', 'OTHER'];
const CONDITIONS = ['MINT', 'NEAR_MINT', 'PLAYED', 'DAMAGED'];

const txStatusBadgeClass = s => {
  if (s === 'PENDING_VERIFICATION') return 'badge-yellow';
  if (s === 'COMPLETED') return 'badge-green';
  if (s === 'REJECTED') return 'badge-red';
  return 'badge-gray';
};

export default function SellPage() {
  const [form, setForm] = useState({ cardName: '', cardSet: '', cardGame: 'POKEMON', condition: 'MINT', quantity: 1 });
  const [quote, setQuote] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [txLoading, setTxLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const fetchTransactions = () => {
    api.get('/api/sell-transactions')
      .then(res => setTransactions(res.data))
      .finally(() => setTxLoading(false));
  };

  useEffect(() => { fetchTransactions(); }, []);

  const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value });

  const getQuote = async e => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setQuote(null);
    setLoading(true);
    try {
      const res = await api.post('/api/sell-transactions/quote', { ...form, quantity: parseInt(form.quantity) });
      setQuote(res.data);
    } catch (err) {
      setError(err.response?.data?.error || 'Error getting quote');
    } finally {
      setLoading(false);
    }
  };

  const acceptQuote = async () => {
    setError('');
    setLoading(true);
    try {
      await api.post('/api/sell-transactions', { ...form, quantity: parseInt(form.quantity) });
      setQuote(null);
      setSuccess('Sell transaction submitted! A staff member will verify your cards.');
      setForm({ cardName: '', cardSet: '', cardGame: 'POKEMON', condition: 'MINT', quantity: 1 });
      setTxLoading(true);
      fetchTransactions();
    } catch (err) {
      setError(err.response?.data?.error || 'Error creating transaction');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>Sell Cards to Store</h1>

      {error && <div className="error-msg">{error}</div>}
      {success && <div className="success-msg">{success}</div>}

      <div className="form-card" style={{ marginBottom: 36, maxWidth: 540 }}>
        <h2>Get a Quote</h2>
        <form onSubmit={getQuote} style={{ marginTop: 16 }}>
          <div className="form-group">
            <label>Card Name</label>
            <input name="cardName" value={form.cardName} onChange={handleChange} required placeholder="e.g. Charizard" />
          </div>
          <div className="form-group">
            <label>Card Set</label>
            <input name="cardSet" value={form.cardSet} onChange={handleChange} required placeholder="e.g. Base Set" />
          </div>
          <div style={{ display: 'flex', gap: 12 }}>
            <div className="form-group" style={{ flex: 1 }}>
              <label>Game</label>
              <select name="cardGame" value={form.cardGame} onChange={handleChange}>
                {GAMES.map(g => <option key={g} value={g}>{g.replace(/_/g, ' ')}</option>)}
              </select>
            </div>
            <div className="form-group" style={{ flex: 1 }}>
              <label>Condition</label>
              <select name="condition" value={form.condition} onChange={handleChange}>
                {CONDITIONS.map(c => <option key={c} value={c}>{c.replace(/_/g, ' ')}</option>)}
              </select>
            </div>
            <div className="form-group" style={{ flex: 1 }}>
              <label>Quantity</label>
              <input type="number" name="quantity" value={form.quantity} onChange={handleChange} min={1} required />
            </div>
          </div>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Getting quote...' : 'Get Quote'}
          </button>
        </form>

        {quote && (
          <div style={{ marginTop: 20, padding: 16, background: '#f0fdf4', borderRadius: 8, border: '1px solid #bbf7d0' }}>
            <h3 style={{ marginBottom: 10 }}>Your Quote</h3>
            <p style={{ fontSize: 14 }}>{quote.quantity}× {quote.cardName} ({quote.condition.replace(/_/g, ' ')})</p>
            <p style={{ fontSize: 14 }}>{quote.cardSet} · {quote.cardGame.replace(/_/g, ' ')}</p>
            <p style={{ fontSize: 14, marginTop: 8 }}>${quote.pricePerCard.toFixed(2)} per card</p>
            <p style={{ fontSize: 20, fontWeight: 700, color: '#059669', marginTop: 4 }}>
              Total: ${quote.totalPrice.toFixed(2)}
            </p>
            <div style={{ display: 'flex', gap: 10, marginTop: 14 }}>
              <button className="btn btn-success" onClick={acceptQuote} disabled={loading}>
                Accept & Submit
              </button>
              <button className="btn btn-secondary" onClick={() => setQuote(null)}>Decline</button>
            </div>
          </div>
        )}
      </div>

      <h2>My Sell Transactions</h2>
      {txLoading ? (
        <div className="loading">Loading...</div>
      ) : transactions.length === 0 ? (
        <p style={{ color: '#6b7280' }}>No transactions yet.</p>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Card</th>
                <th>Condition</th>
                <th>Qty</th>
                <th>Quoted Price</th>
                <th>Status</th>
                <th>Date</th>
                <th>Notes</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map(tx => (
                <tr key={tx.id}>
                  <td>{tx.id}</td>
                  <td>
                    <strong>{tx.cardName}</strong>
                    <br />
                    <span style={{ fontSize: 12, color: '#6b7280' }}>{tx.cardSet} · {tx.cardGame.replace(/_/g, ' ')}</span>
                  </td>
                  <td>{tx.condition.replace(/_/g, ' ')}</td>
                  <td>{tx.quantity}</td>
                  <td>${tx.quotedPrice.toFixed(2)}</td>
                  <td>
                    <span className={`badge ${txStatusBadgeClass(tx.status)}`}>
                      {tx.status.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td>{new Date(tx.createdAt).toLocaleDateString()}</td>
                  <td style={{ fontSize: 12, color: '#991b1b' }}>{tx.rejectionReason || ''}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
