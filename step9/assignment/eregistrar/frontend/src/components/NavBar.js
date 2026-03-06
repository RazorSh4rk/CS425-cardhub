import { Link } from 'react-router-dom';

export default function NavBar() {
  return (
    <nav>
      <span className="brand">eRegistrar</span>
      <Link to="/">Home</Link>
      <Link to="/students">Students</Link>
    </nav>
  );
}
