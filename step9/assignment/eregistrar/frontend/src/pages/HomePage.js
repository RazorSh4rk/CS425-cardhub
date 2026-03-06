import { Link } from 'react-router-dom';

export default function HomePage() {
  return (
    <div className="page">
      <div className="hero">
        <h1>Welcome to eRegistrar</h1>
        <p>Maharishi University of Management — Student Registration System</p>
        <Link to="/students"><button>View Students</button></Link>
      </div>
    </div>
  );
}
