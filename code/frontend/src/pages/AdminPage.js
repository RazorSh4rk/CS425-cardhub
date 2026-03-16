import { useState, useEffect } from 'react';
import api from '../api';

const ROLES = ['CUSTOMER', 'STAFF', 'ADMIN'];
const GAMES = ['POKEMON', 'MAGIC_THE_GATHERING', 'YUGIOH', 'OTHER'];
const CONDITIONS = ['MINT', 'NEAR_MINT', 'PLAYED', 'DAMAGED'];

const emptyCardForm = {
  name: '', set: '', game: 'POKEMON', condition: 'NEAR_MINT',
  quantity: 1, foil: false, imageUrl: '', type: '', rarity: '', color: '',
  marketPrice: '', buyPrice: '', sellPrice: '',
};

export default function AdminPage() {
  const [tab, setTab] = useState('inventory');
  const [cards, setCards] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCardForm, setShowCardForm] = useState(false);
  const [cardForm, setCardForm] = useState(emptyCardForm);
  const [editingCardId, setEditingCardId] = useState(null);
  const [editingPriceId, setEditingPriceId] = useState(null);
  const [priceForm, setPriceForm] = useState({ buyPrice: '', sellPrice: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const fetchCards = () => api.get('/api/cards').then(res => setCards(res.data));
  const fetchUsers = () => api.get('/api/admin/users').then(res => setUsers(res.data));

  useEffect(() => {
    setLoading(true);
    Promise.all([fetchCards(), fetchUsers()])
      .catch(() => setError('Error loading data'))
      .finally(() => setLoading(false));
  }, []);

  const handleCardChange = e => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    setCardForm({ ...cardForm, [e.target.name]: value });
  };

  const openCreate = () => {
    setCardForm(emptyCardForm);
    setEditingCardId(null);
    setError('');
    setSuccess('');
    setShowCardForm(true);
  };

  const openEdit = card => {
    setCardForm({
      name: card.name,
      set: card.set,
      game: card.game,
      condition: card.condition,
      quantity: card.quantity,
      foil: card.foil,
      imageUrl: card.imageUrl || '',
      type: card.type || '',
      rarity: card.rarity || '',
      color: card.color || '',
      marketPrice: card.marketPrice,
      buyPrice: card.buyPrice || '',
      sellPrice: card.sellPrice || '',
    });
    setEditingCardId(card.id);
    setError('');
    setSuccess('');
    setShowCardForm(true);
  };

  const submitCard = async e => {
    e.preventDefault();
    setError('');
    try {
      const payload = {
        ...cardForm,
        quantity: parseInt(cardForm.quantity),
        marketPrice: parseFloat(cardForm.marketPrice),
        buyPrice: cardForm.buyPrice !== '' ? parseFloat(cardForm.buyPrice) : null,
        sellPrice: cardForm.sellPrice !== '' ? parseFloat(cardForm.sellPrice) : null,
        imageUrl: cardForm.imageUrl || null,
        type: cardForm.type || null,
        rarity: cardForm.rarity || null,
        color: cardForm.color || null,
      };
      if (editingCardId) {
        await api.put(`/api/cards/${editingCardId}`, payload);
        setSuccess('Card updated.');
      } else {
        await api.post('/api/cards', payload);
        setSuccess('Card added.');
      }
      setShowCardForm(false);
      setEditingCardId(null);
      fetchCards();
    } catch (err) {
      setError(err.response?.data?.error || 'Error saving card');
    }
  };

  const submitPrice = async cardId => {
    setError('');
    try {
      await api.patch(`/api/cards/${cardId}/price`, {
        buyPrice: parseFloat(priceForm.buyPrice),
        sellPrice: parseFloat(priceForm.sellPrice),
      });
      setEditingPriceId(null);
      setSuccess('Price updated.');
      fetchCards();
    } catch (err) {
      setError(err.response?.data?.error || 'Error updating price');
    }
  };

  const changeRole = async (id, role) => {
    setError('');
    try {
      await api.patch(`/api/admin/users/${id}/role`, { role });
      fetchUsers();
    } catch (err) {
      setError(err.response?.data?.error || 'Error changing role');
    }
  };

  const toggleUser = async id => {
    setError('');
    try {
      await api.patch(`/api/admin/users/${id}/toggle`);
      fetchUsers();
    } catch (err) {
      setError(err.response?.data?.error || 'Error toggling user');
    }
  };

  return (
    <div>
      <h1>Admin Panel</h1>
      {error && <div className="error-msg">{error}</div>}
      {success && <div className="success-msg">{success}</div>}
      <div className="tabs">
        <button className={tab === 'inventory' ? 'active' : ''} onClick={() => setTab('inventory')}>
          Inventory ({cards.length} cards)
        </button>
        <button className={tab === 'users' ? 'active' : ''} onClick={() => setTab('users')}>
          Users ({users.length})
        </button>
      </div>

      {loading ? (
        <div className="loading">Loading...</div>
      ) : tab === 'inventory' ? (
        <>
          {!showCardForm && (
            <button className="btn btn-primary" style={{ marginBottom: 16 }} onClick={openCreate}>
              + Add Card
            </button>
          )}

          {showCardForm && (
            <div className="form-card" style={{ marginBottom: 24, maxWidth: 620 }}>
              <h2>{editingCardId ? 'Edit Card' : 'Add Card'}</h2>
              <form onSubmit={submitCard} style={{ marginTop: 16 }}>
                <div style={{ display: 'flex', gap: 12 }}>
                  <div className="form-group" style={{ flex: 2 }}>
                    <label>Name</label>
                    <input name="name" value={cardForm.name} onChange={handleCardChange} required placeholder="Charizard" />
                  </div>
                  <div className="form-group" style={{ flex: 2 }}>
                    <label>Set</label>
                    <input name="set" value={cardForm.set} onChange={handleCardChange} required placeholder="Base Set" />
                  </div>
                </div>
                <div style={{ display: 'flex', gap: 12 }}>
                  <div className="form-group" style={{ flex: 1 }}>
                    <label>Game</label>
                    <select name="game" value={cardForm.game} onChange={handleCardChange}>
                      {GAMES.map(g => <option key={g} value={g}>{g.replace(/_/g, ' ')}</option>)}
                    </select>
                  </div>
                  <div className="form-group" style={{ flex: 1 }}>
                    <label>Condition</label>
                    <select name="condition" value={cardForm.condition} onChange={handleCardChange}>
                      {CONDITIONS.map(c => <option key={c} value={c}>{c.replace(/_/g, ' ')}</option>)}
                    </select>
                  </div>
                  <div className="form-group" style={{ flex: 1 }}>
                    <label>Quantity</label>
                    <input type="number" name="quantity" value={cardForm.quantity} onChange={handleCardChange} min={0} required />
                  </div>
                </div>
                <div style={{ display: 'flex', gap: 12 }}>
                  <div className="form-group" style={{ flex: 1 }}>
                    <label>Type</label>
                    <input name="type" value={cardForm.type} onChange={handleCardChange} placeholder="Fire" />
                  </div>
                  <div className="form-group" style={{ flex: 1 }}>
                    <label>Rarity</label>
                    <input name="rarity" value={cardForm.rarity} onChange={handleCardChange} placeholder="Rare" />
                  </div>
                  <div className="form-group" style={{ flex: 1 }}>
                    <label>Color</label>
                    <input name="color" value={cardForm.color} onChange={handleCardChange} placeholder="Red" />
                  </div>
                </div>
                <div style={{ display: 'flex', gap: 12 }}>
                  <div className="form-group" style={{ flex: 1 }}>
                    <label>Market Price ($)</label>
                    <input type="number" step="0.01" name="marketPrice" value={cardForm.marketPrice} onChange={handleCardChange} required placeholder="100.00" />
                  </div>
                  <div className="form-group" style={{ flex: 1 }}>
                    <label>Buy Price (optional)</label>
                    <input type="number" step="0.01" name="buyPrice" value={cardForm.buyPrice} onChange={handleCardChange} placeholder="auto" />
                  </div>
                  <div className="form-group" style={{ flex: 1 }}>
                    <label>Sell Price (optional)</label>
                    <input type="number" step="0.01" name="sellPrice" value={cardForm.sellPrice} onChange={handleCardChange} placeholder="auto" />
                  </div>
                </div>
                <div className="form-group">
                  <label>Image URL (optional)</label>
                  <input name="imageUrl" value={cardForm.imageUrl} onChange={handleCardChange} placeholder="https://..." />
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16 }}>
                  <input type="checkbox" id="foil" name="foil" checked={cardForm.foil} onChange={handleCardChange} />
                  <label htmlFor="foil" style={{ marginBottom: 0, fontWeight: 600, fontSize: 13 }}>Foil card</label>
                </div>
                <div style={{ display: 'flex', gap: 10 }}>
                  <button type="submit" className="btn btn-primary">
                    {editingCardId ? 'Update Card' : 'Add Card'}
                  </button>
                  <button type="button" className="btn btn-secondary" onClick={() => { setShowCardForm(false); setEditingCardId(null); }}>
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          )}

          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Set</th>
                  <th>Game</th>
                  <th>Condition</th>
                  <th>Qty</th>
                  <th>Sell Price</th>
                  <th>Buy Price</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {cards.flatMap(card => {
                  const rows = [
                    <tr key={card.id}>
                      <td>
                        {card.name}
                        {card.foil && <span className="badge badge-blue" style={{ marginLeft: 6 }}>Foil</span>}
                      </td>
                      <td>{card.set}</td>
                      <td style={{ fontSize: 12 }}>{card.game.replace(/_/g, ' ')}</td>
                      <td>{card.condition.replace(/_/g, ' ')}</td>
                      <td>{card.quantity}</td>
                      <td>${card.sellPrice?.toFixed(2)}</td>
                      <td>${card.buyPrice?.toFixed(2)}</td>
                      <td>
                        <div style={{ display: 'flex', gap: 6 }}>
                          <button className="btn btn-secondary btn-sm" onClick={() => openEdit(card)}>Edit</button>
                          <button
                            className="btn btn-secondary btn-sm"
                            onClick={() => {
                              setEditingPriceId(card.id);
                              setPriceForm({ buyPrice: card.buyPrice || '', sellPrice: card.sellPrice || '' });
                            }}
                          >
                            Prices
                          </button>
                        </div>
                      </td>
                    </tr>
                  ];
                  if (editingPriceId === card.id) {
                    rows.push(
                      <tr key={`price-${card.id}`}>
                        <td colSpan={8} style={{ background: '#f0f9ff' }}>
                          <div style={{ display: 'flex', gap: 8, alignItems: 'center', padding: '6px 0' }}>
                            <label style={{ fontSize: 13, fontWeight: 600 }}>Buy:</label>
                            <input
                              type="number" step="0.01"
                              value={priceForm.buyPrice}
                              onChange={e => setPriceForm({ ...priceForm, buyPrice: e.target.value })}
                              style={{ width: 110, padding: '5px 8px', border: '1px solid #d1d5db', borderRadius: 6, fontSize: 14 }}
                            />
                            <label style={{ fontSize: 13, fontWeight: 600 }}>Sell:</label>
                            <input
                              type="number" step="0.01"
                              value={priceForm.sellPrice}
                              onChange={e => setPriceForm({ ...priceForm, sellPrice: e.target.value })}
                              style={{ width: 110, padding: '5px 8px', border: '1px solid #d1d5db', borderRadius: 6, fontSize: 14 }}
                            />
                            <button className="btn btn-primary btn-sm" onClick={() => submitPrice(card.id)}>Save</button>
                            <button className="btn btn-secondary btn-sm" onClick={() => setEditingPriceId(null)}>Cancel</button>
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
        </>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map(u => (
                <tr key={u.id}>
                  <td>{u.id}</td>
                  <td>{u.name}</td>
                  <td>{u.email}</td>
                  <td>
                    <select
                      value={u.role}
                      onChange={e => changeRole(u.id, e.target.value)}
                      style={{ padding: '4px 8px', border: '1px solid #d1d5db', borderRadius: 6, fontSize: 13 }}
                    >
                      {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
                    </select>
                  </td>
                  <td>
                    <span className={`badge ${u.accountEnabled ? 'badge-green' : 'badge-red'}`}>
                      {u.accountEnabled ? 'Active' : 'Disabled'}
                    </span>
                  </td>
                  <td>
                    <button
                      className={`btn btn-sm ${u.accountEnabled ? 'btn-danger' : 'btn-success'}`}
                      onClick={() => toggleUser(u.id)}
                    >
                      {u.accountEnabled ? 'Disable' : 'Enable'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
