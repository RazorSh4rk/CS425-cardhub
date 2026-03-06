import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api';

export default function StudentsPage() {
  const [students, setStudents] = useState([]);
  const [search, setSearch] = useState('');

  const load = (q = '') => {
    api.get('/api/students', { params: q ? { search: q } : {} })
      .then(res => setStudents(res.data));
  };

  useEffect(() => load(), []);

  const handleDelete = (id) => {
    if (window.confirm('Delete this student?')) {
      api.delete(`/api/students/${id}`).then(() => load(search));
    }
  };

  return (
    <div className="page">
      <h1>Students</h1>
      <div className="toolbar">
        <input
          placeholder="Search by name..."
          value={search}
          onChange={e => setSearch(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && load(search)}
        />
        <button onClick={() => load(search)}>Search</button>
        <Link to="/students/new"><button>Add Student</button></Link>
      </div>
      <table>
        <thead>
          <tr>
            <th>Student #</th>
            <th>Name</th>
            <th>CGPA</th>
            <th>Enrollment Date</th>
            <th>International</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {students.map(s => (
            <tr key={s.studentId}>
              <td>{s.studentNumber}</td>
              <td>{[s.firstName, s.middleName, s.lastName].filter(Boolean).join(' ')}</td>
              <td>{s.cgpa ?? '—'}</td>
              <td>{s.enrollmentDate}</td>
              <td>{s.international ? 'Yes' : 'No'}</td>
              <td>
                <div className="actions">
                  <Link to={`/students/${s.studentId}/edit`}><button>Edit</button></Link>
                  <button className="danger" onClick={() => handleDelete(s.studentId)}>Delete</button>
                </div>
              </td>
            </tr>
          ))}
          {students.length === 0 && (
            <tr><td colSpan="6" style={{textAlign:'center', color:'#999'}}>No students found.</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
