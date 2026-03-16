import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';

export default function CartPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const fetchCart = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/cart');
      setItems(res.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchCart(); }, []);

  const updateQty = async (item, qty) => {
    if (qty < 1) return;
    setError('');
    try {
      await api.put(`/api/cart/items/${item.id}`, { quantity: qty });
      fetchCart();
    } catch (err) {
      setError(err.response?.data?.error || 'Error updating quantity');
    }
  };

  const removeItem = async id => {
    try {
      await api.delete(`/api/cart/items/${id}`);
      fetchCart();
    } catch (err) {
      setError(err.response?.data?.error || 'Error removing item');
    }
  };

  const clearCart = async () => {
    try {
      await api.delete('/api/cart');
      fetchCart();
    } catch (err) {
      setError('Error clearing cart');
    }
  };

  const subtotal = items.reduce((sum, i) => sum + i.lineTotal, 0);
  const tax = subtotal * 0.08;

  if (loading) return <div className="loading">Loading cart...</div>;

  return (
    <div>
      <h1>My Cart</h1>
      {error && <div className="error-msg">{error}</div>}
      {items.length === 0 ? (
        <div style={{ color: '#6b7280' }}>
          Your cart is empty. <a href="/" style={{ color: '#2563eb' }}>Browse cards</a>
        </div>
      ) : (
        <>
          {items.map(item => (
            <div key={item.id} className="cart-item">
              <div className="item-info">
                <strong style={{ fontSize: 15 }}>{item.cardName}</strong>
                <p style={{ fontSize: 13, color: '#6b7280', marginTop: 2 }}>
                  {item.cardSet} · {item.condition.replace(/_/g, ' ')} · {item.game.replace(/_/g, ' ')}
                  {item.foil ? ' · Foil' : ''}
                </p>
                <p style={{ fontSize: 13, marginTop: 2 }}>${item.sellPrice?.toFixed(2)} each</p>
              </div>
              <div className="qty-controls">
                <button className="btn btn-secondary btn-sm" onClick={() => updateQty(item, item.quantity - 1)}>−</button>
                <input
                  type="number"
                  value={item.quantity}
                  min={1}
                  onChange={e => {
                    const v = parseInt(e.target.value);
                    if (v >= 1) updateQty(item, v);
                  }}
                />
                <button className="btn btn-secondary btn-sm" onClick={() => updateQty(item, item.quantity + 1)}>+</button>
              </div>
              <span className="line-total">${item.lineTotal?.toFixed(2)}</span>
              <button className="btn btn-danger btn-sm" onClick={() => removeItem(item.id)}>Remove</button>
            </div>
          ))}

          <div className="cart-summary">
            <div className="row"><span>Subtotal</span><span>${subtotal.toFixed(2)}</span></div>
            <div className="row"><span>Tax (8%)</span><span>${tax.toFixed(2)}</span></div>
            <div className="row grand"><span>Total</span><span>${(subtotal + tax).toFixed(2)}</span></div>
            <div className="actions">
              <button className="btn btn-primary" onClick={() => navigate('/checkout')}>
                Proceed to Checkout
              </button>
              <button className="btn btn-secondary" onClick={clearCart}>Clear Cart</button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
