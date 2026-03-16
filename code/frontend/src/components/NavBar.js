import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';

export default function NavBar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <Link className="brand" to="/">CardHub</Link>
      <Link to="/">Cards</Link>
      <Link to="/trades">Trades</Link>
      <Link to="/tournaments">Tournaments</Link>
      {user && <Link to="/cart">Cart</Link>}
      {user && <Link to="/orders">My Orders</Link>}
      {user && <Link to="/sell">Sell</Link>}
      {user && (user.role === 'STAFF' || user.role === 'ADMIN') && (
        <Link to="/staff">Staff</Link>
      )}
      {user && user.role === 'ADMIN' && <Link to="/admin">Admin</Link>}
      <div className="spacer" />
      {user ? (
        <>
          <span className="user-info">{user.name} ({user.role})</span>
          <button className="logout-btn" onClick={handleLogout}>Logout</button>
        </>
      ) : (
        <Link to="/login">Login</Link>
      )}
    </nav>
  );
}
