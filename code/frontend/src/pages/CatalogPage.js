import { useState, useEffect } from 'react';
import { useAuth } from '../AuthContext';
import api from '../api';

const GAMES = ['POKEMON', 'MAGIC_THE_GATHERING', 'YUGIOH', 'OTHER'];
const CONDITIONS = ['MINT', 'NEAR_MINT', 'PLAYED', 'DAMAGED'];

const conditionBadgeClass = c => {
  if (c === 'MINT') return 'badge-green';
  if (c === 'NEAR_MINT') return 'badge-blue';
  if (c === 'PLAYED') return 'badge-yellow';
  return 'badge-red';
};

export default function CatalogPage() {
  const { user } = useAuth();
  const [cards, setCards] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({ search: '', game: '', condition: '', minPrice: '', maxPrice: '' });
  const [cartFeedback, setCartFeedback] = useState({});

  const fetchCards = async () => {
    setLoading(true);
    const params = {};
    if (filters.search) params.search = filters.search;
    if (filters.game) params.game = filters.game;
    if (filters.condition) params.condition = filters.condition;
    if (filters.minPrice) params.minPrice = filters.minPrice;
    if (filters.maxPrice) params.maxPrice = filters.maxPrice;
    try {
      const res = await api.get('/api/cards', { params });
      setCards(res.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchCards(); }, []);

  const handleFilter = e => {
    e.preventDefault();
    fetchCards();
  };

  const addToCart = async card => {
    try {
      await api.post('/api/cart/items', { cardId: card.id, quantity: 1 });
      setCartFeedback(prev => ({ ...prev, [card.id]: 'Added!' }));
      setTimeout(() => setCartFeedback(prev => { const n = { ...prev }; delete n[card.id]; return n; }), 2000);
    } catch (err) {
      const msg = err.response?.data?.error || 'Error';
      setCartFeedback(prev => ({ ...prev, [card.id]: msg }));
      setTimeout(() => setCartFeedback(prev => { const n = { ...prev }; delete n[card.id]; return n; }), 3000);
    }
  };

  return (
    <div>
      <h1>Card Catalog</h1>
      <form className="filters" onSubmit={handleFilter}>
        <input
          placeholder="Search name or set..."
          value={filters.search}
          onChange={e => setFilters({ ...filters, search: e.target.value })}
          style={{ minWidth: 200 }}
        />
        <select value={filters.game} onChange={e => setFilters({ ...filters, game: e.target.value })}>
          <option value="">All Games</option>
          {GAMES.map(g => <option key={g} value={g}>{g.replace(/_/g, ' ')}</option>)}
        </select>
        <select value={filters.condition} onChange={e => setFilters({ ...filters, condition: e.target.value })}>
          <option value="">All Conditions</option>
          {CONDITIONS.map(c => <option key={c} value={c}>{c.replace(/_/g, ' ')}</option>)}
        </select>
        <input
          placeholder="Min $"
          type="number"
          min="0"
          value={filters.minPrice}
          onChange={e => setFilters({ ...filters, minPrice: e.target.value })}
          style={{ width: 90 }}
        />
        <input
          placeholder="Max $"
          type="number"
          min="0"
          value={filters.maxPrice}
          onChange={e => setFilters({ ...filters, maxPrice: e.target.value })}
          style={{ width: 90 }}
        />
        <button type="submit" className="btn btn-primary">Search</button>
        <button type="button" className="btn btn-secondary" onClick={() => { setFilters({ search: '', game: '', condition: '', minPrice: '', maxPrice: '' }); }}>Clear</button>
      </form>

      {loading ? (
        <div className="loading">Loading cards...</div>
      ) : cards.length === 0 ? (
        <p style={{ color: '#6b7280' }}>No cards found.</p>
      ) : (
        <div className="grid">
          {cards.map(card => (
            <div key={card.id} className="card">
              <span className={`badge ${conditionBadgeClass(card.condition)}`}>
                {card.condition.replace(/_/g, ' ')}
              </span>
              {card.foil && <span className="badge badge-blue" style={{ marginLeft: 6 }}>Foil</span>}
              <h3 style={{ marginTop: 10 }}>{card.name}</h3>
              <p className="meta">{card.set}</p>
              <p className="meta">{card.game.replace(/_/g, ' ')}{card.rarity ? ` · ${card.rarity}` : ''}</p>
              <p className="price">${card.sellPrice?.toFixed(2)}</p>
              <p className="meta">In stock: {card.quantity}</p>
              {user && card.quantity > 0 && (
                <button
                  className="btn btn-primary btn-sm"
                  style={{ marginTop: 10 }}
                  onClick={() => addToCart(card)}
                  disabled={cartFeedback[card.id] !== undefined}
                >
                  {cartFeedback[card.id] || 'Add to Cart'}
                </button>
              )}
              {!user && (
                <p className="meta" style={{ marginTop: 10 }}>
                  <a href="/login" style={{ color: '#2563eb' }}>Login</a> to purchase
                </p>
              )}
              {card.quantity === 0 && (
                <p style={{ marginTop: 10, fontSize: 13, color: '#dc2626', fontWeight: 600 }}>Out of stock</p>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
