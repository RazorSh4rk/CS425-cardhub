import { useState, useEffect } from 'react';
import api from '../api';

const statusBadgeClass = s => {
  if (s === 'PENDING_PICKUP') return 'badge-yellow';
  if (s === 'READY') return 'badge-blue';
  if (s === 'PICKED_UP') return 'badge-green';
  if (s === 'CANCELLED') return 'badge-red';
  return 'badge-gray';
};

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expanded, setExpanded] = useState(null);

  useEffect(() => {
    api.get('/api/orders')
      .then(res => setOrders(res.data))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading">Loading orders...</div>;

  return (
    <div>
      <h1>My Orders</h1>
      {orders.length === 0 ? (
        <p style={{ color: '#6b7280' }}>No orders yet. <a href="/" style={{ color: '#2563eb' }}>Browse cards</a></p>
      ) : (
        orders.map(order => (
          <div key={order.id} className="card" style={{ marginBottom: 12 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div>
                <strong style={{ fontSize: 15 }}>Order #{order.id}</strong>
                <span className={`badge ${statusBadgeClass(order.status)}`} style={{ marginLeft: 10 }}>
                  {order.status.replace(/_/g, ' ')}
                </span>
                <p className="meta" style={{ marginTop: 4 }}>
                  {new Date(order.createdAt).toLocaleDateString()} · {order.contactName}
                </p>
              </div>
              <div style={{ textAlign: 'right' }}>
                <strong style={{ fontSize: 16 }}>${order.total.toFixed(2)}</strong>
                <br />
                <button
                  className="btn btn-secondary btn-sm"
                  style={{ marginTop: 6 }}
                  onClick={() => setExpanded(expanded === order.id ? null : order.id)}
                >
                  {expanded === order.id ? 'Hide' : 'Details'}
                </button>
              </div>
            </div>
            {expanded === order.id && (
              <div style={{ marginTop: 12, borderTop: '1px solid #f3f4f6', paddingTop: 12 }}>
                {order.items.map(item => (
                  <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '3px 0', fontSize: 13 }}>
                    <span>{item.cardName} ({item.condition.replace(/_/g, ' ')}) × {item.quantity}</span>
                    <span>${item.lineTotal.toFixed(2)}</span>
                  </div>
                ))}
                <div style={{ marginTop: 8, paddingTop: 8, borderTop: '1px solid #f3f4f6', fontSize: 13, color: '#6b7280' }}>
                  Subtotal ${order.subtotal.toFixed(2)} · Tax ${order.tax.toFixed(2)} · Total ${order.total.toFixed(2)}
                </div>
              </div>
            )}
          </div>
        ))
      )}
    </div>
  );
}
