import React, { useState, useEffect } from 'react';
import api from '../api';

const txBadge = s => {
  if (s === 'PENDING_VERIFICATION') return 'badge-yellow';
  if (s === 'COMPLETED') return 'badge-green';
  if (s === 'REJECTED') return 'badge-red';
  return 'badge-gray';
};

const orderBadge = s => {
  if (s === 'PENDING_PICKUP') return 'badge-yellow';
  if (s === 'READY') return 'badge-blue';
  if (s === 'PICKED_UP') return 'badge-green';
  if (s === 'CANCELLED') return 'badge-red';
  return 'badge-gray';
};

const ORDER_STATUSES = ['PENDING_PICKUP', 'READY', 'PICKED_UP', 'CANCELLED'];

export default function StaffPage() {
  const [tab, setTab] = useState('sell');
  const [transactions, setTransactions] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [rejectingId, setRejectingId] = useState(null);
  const [rejectReason, setRejectReason] = useState('');
  const [error, setError] = useState('');

  const fetchAll = () => {
    setLoading(true);
    Promise.all([
      api.get('/api/sell-transactions'),
      api.get('/api/orders'),
    ]).then(([txRes, ordRes]) => {
      setTransactions(txRes.data);
      setOrders(ordRes.data);
    }).catch(() => setError('Error loading data'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchAll(); }, []);

  const completeTx = async id => {
    setError('');
    try {
      await api.patch(`/api/sell-transactions/${id}/complete`);
      fetchAll();
    } catch (err) {
      setError(err.response?.data?.error || 'Error completing transaction');
    }
  };

  const rejectTx = async id => {
    if (!rejectReason.trim()) return;
    setError('');
    try {
      await api.patch(`/api/sell-transactions/${id}/reject`, { reason: rejectReason });
      setRejectingId(null);
      setRejectReason('');
      fetchAll();
    } catch (err) {
      setError(err.response?.data?.error || 'Error rejecting transaction');
    }
  };

  const updateOrderStatus = async (id, status) => {
    setError('');
    try {
      await api.patch(`/api/orders/${id}/status`, { status });
      fetchAll();
    } catch (err) {
      setError(err.response?.data?.error || 'Error updating order status');
    }
  };

  return (
    <div>
      <h1>Staff Panel</h1>
      {error && <div className="error-msg">{error}</div>}
      <div className="tabs">
        <button className={tab === 'sell' ? 'active' : ''} onClick={() => setTab('sell')}>
          Sell Transactions ({transactions.filter(t => t.status === 'PENDING_VERIFICATION').length} pending)
        </button>
        <button className={tab === 'orders' ? 'active' : ''} onClick={() => setTab('orders')}>
          Orders ({orders.length})
        </button>
      </div>

      {loading ? (
        <div className="loading">Loading...</div>
      ) : tab === 'sell' ? (
        transactions.length === 0 ? (
          <p style={{ color: '#6b7280' }}>No sell transactions.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Customer</th>
                  <th>Card</th>
                  <th>Condition</th>
                  <th>Qty</th>
                  <th>Quoted</th>
                  <th>Status</th>
                  <th>Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {transactions.flatMap(tx => {
                  const rows = [
                    <tr key={tx.id}>
                      <td>{tx.id}</td>
                      <td>#{tx.customerId}</td>
                      <td>
                        <strong>{tx.cardName}</strong>
                        <br />
                        <span style={{ fontSize: 12, color: '#6b7280' }}>{tx.cardSet}</span>
                      </td>
                      <td>{tx.condition.replace(/_/g, ' ')}</td>
                      <td>{tx.quantity}</td>
                      <td>${tx.quotedPrice.toFixed(2)}</td>
                      <td>
                        <span className={`badge ${txBadge(tx.status)}`}>
                          {tx.status.replace(/_/g, ' ')}
                        </span>
                        {tx.rejectionReason && (
                          <p style={{ fontSize: 11, color: '#991b1b', marginTop: 4 }}>{tx.rejectionReason}</p>
                        )}
                      </td>
                      <td>{new Date(tx.createdAt).toLocaleDateString()}</td>
                      <td>
                        {tx.status === 'PENDING_VERIFICATION' && (
                          <div style={{ display: 'flex', gap: 6 }}>
                            <button className="btn btn-success btn-sm" onClick={() => completeTx(tx.id)}>Complete</button>
                            <button className="btn btn-danger btn-sm" onClick={() => { setRejectingId(tx.id); setRejectReason(''); }}>Reject</button>
                          </div>
                        )}
                      </td>
                    </tr>
                  ];
                  if (rejectingId === tx.id) {
                    rows.push(
                      <tr key={`reject-${tx.id}`}>
                        <td colSpan={9} style={{ background: '#fff5f5' }}>
                          <div style={{ display: 'flex', gap: 8, padding: '6px 0' }}>
                            <input
                              placeholder="Rejection reason..."
                              value={rejectReason}
                              onChange={e => setRejectReason(e.target.value)}
                              style={{ flex: 1, padding: '6px 10px', border: '1px solid #fca5a5', borderRadius: 6, fontSize: 14 }}
                            />
                            <button className="btn btn-danger btn-sm" onClick={() => rejectTx(tx.id)}>Confirm</button>
                            <button className="btn btn-secondary btn-sm" onClick={() => setRejectingId(null)}>Cancel</button>
                          </div>
                        </td>
                      </tr>
                    );
                  }
                  return rows;
                })}
              </tbody>
            </table>
          </div>
        )
      ) : (
        orders.length === 0 ? (
          <p style={{ color: '#6b7280' }}>No orders.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Customer</th>
                  <th>Contact</th>
                  <th>Total</th>
                  <th>Status</th>
                  <th>Date</th>
                  <th>Update Status</th>
                </tr>
              </thead>
              <tbody>
                {orders.map(order => (
                  <tr key={order.id}>
                    <td>{order.id}</td>
                    <td>#{order.customerId}</td>
                    <td>
                      {order.contactName}
                      <br />
                      <span style={{ fontSize: 12, color: '#6b7280' }}>{order.contactEmail}</span>
                    </td>
                    <td>${order.total.toFixed(2)}</td>
                    <td>
                      <span className={`badge ${orderBadge(order.status)}`}>
                        {order.status.replace(/_/g, ' ')}
                      </span>
                    </td>
                    <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                    <td>
                      <select
                        value={order.status}
                        onChange={e => updateOrderStatus(order.id, e.target.value)}
                        style={{ padding: '5px 8px', border: '1px solid #d1d5db', borderRadius: 6, fontSize: 13 }}
                      >
                        {ORDER_STATUSES.map(s => (
                          <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>
                        ))}
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      )}
    </div>
  );
}
