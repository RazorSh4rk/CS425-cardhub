import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';

export default function CheckoutPage() {
  const [form, setForm] = useState({ contactName: '', contactEmail: '', contactPhone: '' });
  const [order, setOrder] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async e => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await api.post('/api/orders', form);
      setOrder(res.data);
    } catch (err) {
      const data = err.response?.data;
      if (data?.error) setError(data.error);
      else if (data?.errors) setError(data.errors.map(e => e.message).join(', '));
      else setError('Checkout failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (order) {
    return (
      <div>
        <div className="success-msg" style={{ fontSize: 16, marginBottom: 20 }}>
          Order #{order.id} placed successfully!
        </div>
        <div className="card" style={{ maxWidth: 520 }}>
          <h2 style={{ marginBottom: 14 }}>Order Confirmation</h2>
          <p style={{ fontSize: 14 }}><strong>Name:</strong> {order.contactName}</p>
          <p style={{ fontSize: 14 }}><strong>Email:</strong> {order.contactEmail}</p>
          {order.contactPhone && <p style={{ fontSize: 14 }}><strong>Phone:</strong> {order.contactPhone}</p>}
          <p style={{ fontSize: 14, marginTop: 6 }}>
            <strong>Status:</strong>{' '}
            <span className="badge badge-yellow">{order.status.replace(/_/g, ' ')}</span>
          </p>
          <hr style={{ margin: '14px 0', border: 'none', borderTop: '1px solid #e5e7eb' }} />
          {order.items.map(item => (
            <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '4px 0', fontSize: 14 }}>
              <span>{item.cardName} ({item.condition.replace(/_/g, ' ')}) × {item.quantity}</span>
              <span>${item.lineTotal.toFixed(2)}</span>
            </div>
          ))}
          <hr style={{ margin: '12px 0', border: 'none', borderTop: '1px solid #e5e7eb' }} />
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 14 }}>
            <span>Subtotal</span><span>${order.subtotal.toFixed(2)}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 14 }}>
            <span>Tax</span><span>${order.tax.toFixed(2)}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700, fontSize: 16, marginTop: 8 }}>
            <span>Total</span><span>${order.total.toFixed(2)}</span>
          </div>
          <div style={{ display: 'flex', gap: 10, marginTop: 18 }}>
            <button className="btn btn-primary" onClick={() => navigate('/orders')}>View My Orders</button>
            <button className="btn btn-secondary" onClick={() => navigate('/')}>Continue Shopping</button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div>
      <h1>Checkout</h1>
      {error && <div className="error-msg">{error}</div>}
      <div className="form-card">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Full Name</label>
            <input name="contactName" value={form.contactName} onChange={handleChange} required placeholder="Levi Szabo" />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input type="email" name="contactEmail" value={form.contactEmail} onChange={handleChange} required placeholder="you@example.com" />
          </div>
          <div className="form-group">
            <label>Phone (optional)</label>
            <input name="contactPhone" value={form.contactPhone} onChange={handleChange} placeholder="555-1234" />
          </div>
          <button type="submit" className="btn btn-primary" disabled={loading} style={{ width: '100%', justifyContent: 'center', padding: '10px' }}>
            {loading ? 'Placing order...' : 'Place Order'}
          </button>
        </form>
      </div>
    </div>
  );
}
