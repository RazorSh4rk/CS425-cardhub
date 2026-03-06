import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../api';

const empty = {
  studentNumber: '',
  firstName: '',
  middleName: '',
  lastName: '',
  cgpa: '',
  enrollmentDate: '',
  international: false,
};

export default function StudentFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState(empty);
  const isEdit = !!id;

  useEffect(() => {
    if (isEdit) {
      api.get(`/api/students/${id}`).then(res => setForm({
        ...res.data,
        middleName: res.data.middleName ?? '',
        cgpa: res.data.cgpa != null ? res.data.cgpa : '',
      }));
    }
  }, [id]);

  const handleChange = e => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = e => {
    e.preventDefault();
    const data = {
      ...form,
      cgpa: form.cgpa === '' ? null : parseFloat(form.cgpa),
      international: form.international === 'true' || form.international === true,
    };
    const req = isEdit
      ? api.put(`/api/students/${id}`, data)
      : api.post('/api/students', data);
    req.then(() => navigate('/students'));
  };

  return (
    <div className="page">
      <h1>{isEdit ? 'Edit Student' : 'Add Student'}</h1>
      <form onSubmit={handleSubmit}>
        <label>
          Student Number *
          <input name="studentNumber" value={form.studentNumber} onChange={handleChange} required />
        </label>
        <label>
          First Name *
          <input name="firstName" value={form.firstName} onChange={handleChange} required />
        </label>
        <label>
          Middle Name
          <input name="middleName" value={form.middleName} onChange={handleChange} />
        </label>
        <label>
          Last Name *
          <input name="lastName" value={form.lastName} onChange={handleChange} required />
        </label>
        <label>
          CGPA
          <input name="cgpa" type="number" step="0.01" min="0" max="4" value={form.cgpa} onChange={handleChange} />
        </label>
        <label>
          Enrollment Date *
          <input name="enrollmentDate" type="date" value={form.enrollmentDate} onChange={handleChange} required />
        </label>
        <label>
          International
          <select name="international" value={String(form.international)} onChange={handleChange}>
            <option value="false">No</option>
            <option value="true">Yes</option>
          </select>
        </label>
        <div className="form-actions">
          <button type="submit">{isEdit ? 'Save' : 'Add'}</button>
          <button type="button" onClick={() => navigate('/students')}>Cancel</button>
        </div>
      </form>
    </div>
  );
}
