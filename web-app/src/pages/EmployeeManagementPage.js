import React, { useCallback, useState } from 'react';
import { getEmployees, addEmployee, updateEmployee } from '../services/employeeService';
import { exportToCSV } from '../utils/exportUtils';
import SessionManager from '../utils/SessionManager';
import Modal from '../components/Modal';
import { currency, Empty, Feedback, Loading, Stats, useManagementAction, useManagementData } from '../components/ManagementUI';

const EMPTY = { id: 0, employeeId: '', firstName: '', lastName: '', email: '', phone: '', designation: '', joinDate: '', salary: '', status: 'ACTIVE' };
const STATUSES = ['ACTIVE', 'ON_LEAVE', 'RESIGNED', 'TERMINATED'];
const STATUS_LABELS = { ACTIVE: 'Active', ON_LEAVE: 'On leave', RESIGNED: 'Resigned', TERMINATED: 'Terminated' };
export default function EmployeeManagementPage() {
  const can = code => SessionManager.hasPermission(code);
  const canView = can('VIEW_EMPLOYEE');
  const loader = useCallback(async () => (await getEmployees()).data || [], []);
  const list = useManagementData(loader, canView);
  const action = useManagementAction();
  const [query, setQuery] = useState(''), [status, setStatus] = useState('ALL');
  const [form, setForm] = useState(null), [formError, setFormError] = useState('');
  const [initial, setInitial] = useState('');
  const records = list.data.filter(employee => (status === 'ALL' || employee.status === status) && [employee.firstName, employee.lastName, employee.employeeId, employee.designation, employee.email].some(value => String(value || '').toLowerCase().includes(query.trim().toLowerCase())));
  const open = employee => {
    const value = { ...EMPTY, ...employee };
    Object.keys(EMPTY).forEach(key => { if (value[key] == null) value[key] = EMPTY[key]; });
    setForm(value); setInitial(JSON.stringify(value)); setFormError(''); action.clear();
  };
  const save = async () => {
    const salary = String(form.salary).trim();
    if (!form.employeeId.trim() || !form.firstName.trim() || !form.email.trim() || !form.designation.trim()) { setFormError('Employee ID, first name, email and designation are required.'); return; }
    if (salary && (!/^\d+(\.\d{1,2})?$/.test(salary) || Number(salary) > 99999999.99)) { setFormError('Enter a non-negative monthly salary with at most two decimal places.'); return; }
    if (form.phone && (!/^\+?[0-9 -]+$/.test(form.phone) || form.phone.replace(/\D/g, '').length < 7 || form.phone.replace(/\D/g, '').length > 15)) { setFormError('Enter a phone number containing 7 to 15 digits.'); return; }
await action.run(async () => {
      const payload = { ...form, employeeId: form.employeeId.trim(), firstName: form.firstName.trim(), lastName: form.lastName.trim(), email: form.email.trim(), salary: salary || (form.id > 0 ? (JSON.parse(initial).salary ?? 0) : '0') };
      await (form.id > 0 ? updateEmployee(payload) : addEmployee(payload));
      setForm(null); await list.reload();
    }, 'Employee profile saved.');
  };
  if (!canView) return <div className="management-page"><Empty title="Access restricted">You need permission to view employees.</Empty></div>;
  return <div className="management-page">
    <div className="page-header"><div><h1 className="page-title">Employees</h1><p className="page-subtitle">Staff profiles, employment status and monthly salary details.</p></div><div className="management-actions"><button className="btn btn-secondary" disabled={!list.loaded || !records.length} onClick={() => exportToCSV(['Employee ID', 'Name', 'Designation', 'Joining date', 'Monthly salary (INR)', 'Status', 'Profile'], records.map(e => [e.employeeId, [e.firstName, e.lastName].filter(Boolean).join(' '), e.designation, e.joinDate, e.salary, e.status, e.id > 0 ? 'Saved' : 'Needs setup']), 'employees')}>Export CSV</button>{can('CREATE_EMPLOYEE') && <button className="btn btn-primary" disabled={action.busy} onClick={() => open(EMPTY)}>Add employee</button>}</div></div>
    <Feedback error={action.error} notice={action.notice} />
    <Stats items={[[ 'Staff', list.loaded ? list.data.length : '-'], ['Active', list.loaded ? list.data.filter(e => e.status === 'ACTIVE').length : '-'], ['Profiles needing setup', list.loaded ? list.data.filter(e => !(e.id > 0)).length : '-']]} />
    <div className="management-toolbar"><label htmlFor="employee-search">Find an employee</label><input id="employee-search" type="search" className="form-control" placeholder="Name, ID, designation or email" value={query} onChange={e => setQuery(e.target.value)} /><select aria-label="Employment status" className="form-control" value={status} onChange={e => setStatus(e.target.value)}><option value="ALL">All statuses</option>{STATUSES.map(value => <option key={value} value={value}>{STATUS_LABELS[value]}</option>)}</select></div>
    {list.loading ? <Loading /> : list.error ? <Feedback error={list.error} onRetry={list.reload} /> : !records.length ? <Empty title="No employees found">{query || status !== 'ALL' ? 'Try a different search or status filter.' : 'Add an employee to get started.'}</Empty> : <div className="management-grid">{records.map(employee => <article key={employee.id > 0 ? 'employee-' + employee.id : 'account-' + employee.employeeId} className="management-card"><h3>{[employee.firstName, employee.lastName].filter(Boolean).join(' ') || employee.employeeId || 'Staff member'}</h3><p>{employee.designation || 'Designation not set'} &middot; {employee.employeeId || 'ID not set'}</p><span className={'badge ' + (employee.status === 'ACTIVE' ? 'badge-success' : 'badge-secondary')}>{STATUS_LABELS[employee.status] || employee.status || 'Unknown'}</span>{!(employee.id > 0) && <span className="badge badge-warning">Profile needs setup</span>}<dl><dt>Email</dt><dd>{employee.email || 'Not set'}</dd><dt>Joining date</dt><dd>{employee.joinDate || 'Not set'}</dd><dt>Monthly salary</dt><dd>{employee.salary == null || (!(employee.id > 0) && Number(employee.salary) === 0) ? 'Not set' : currency(employee.salary)}</dd></dl>{can(employee.id > 0 ? 'UPDATE_EMPLOYEE' : 'CREATE_EMPLOYEE') && <button className="btn btn-secondary" disabled={action.busy} onClick={() => open(employee)}>{employee.id > 0 ? 'Edit profile' : 'Set up profile'}</button>}</article>)}</div>}
    <Modal isOpen={Boolean(form)} title={form?.id > 0 ? 'Edit employee' : form?.userId ? 'Set up employee profile' : 'Add employee'} onClose={() => setForm(null)} onSubmit={save} submitting={action.busy} isDirty={Boolean(form && JSON.stringify(form) !== initial)} size="large">
      {form && <form className="management-form" onSubmit={e => { e.preventDefault(); save(); }}><Feedback error={formError || action.error} /><fieldset disabled={action.busy}><div className="form-grid">{[
        ['employeeId', 'Employee ID', 'text', true], ['firstName', 'First name', 'text', true], ['lastName', 'Last name', 'text', false], ['email', 'Work email', 'email', true], ['phone', 'Phone', 'tel', false], ['designation', 'Designation', 'text', true], ['joinDate', 'Joining date', 'date', false], ['salary', 'Monthly salary (INR)', 'number', false]
      ].map(([key, label, type, required]) => <div className="form-group" key={key}><label className="form-label" htmlFor={'employee-' + key}>{label}{required ? ' *' : ''}</label><input id={'employee-' + key} className="form-control" type={type} required={required} min={type === 'number' ? '0' : undefined} max={type === 'number' ? '99999999.99' : undefined} step={type === 'number' ? '0.01' : undefined} maxLength={key === 'employeeId' ? 50 : key === 'phone' ? 20 : 100} readOnly={(key === 'employeeId' && (form.id > 0 || Boolean(form.userId))) || (key === 'designation' && Boolean(form.userId))} value={form[key]} onChange={e => setForm({ ...form, [key]: e.target.value })} />{key === 'designation' && form.userId && <small className="management-muted">Managed by the linked account's role.</small>}{key === 'salary' && <small className="management-muted">Payroll uses this monthly amount. A positive salary and joining date are needed to generate payroll.</small>}</div>)}<div className="form-group"><label className="form-label" htmlFor="employee-status">Status</label><select id="employee-status" className="form-control" value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>{STATUSES.map(value => <option key={value} value={value}>{STATUS_LABELS[value]}</option>)}</select></div></div></fieldset></form>}
    </Modal>
  </div>;
}
