import React, { useCallback, useState } from 'react';
import { Link } from 'react-router-dom';
import SessionManager from '../utils/SessionManager';
import { getDepartments, addDepartment, updateDepartment, deleteDepartment, getUsers, deleteUser } from '../services/instituteService';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { Empty, Feedback, Loading, Stats, useManagementAction, useManagementData } from '../components/ManagementUI';

const EMPTY = { name: '', code: '', description: '', headOfDepartment: '' };
export default function InstituteManagementPage() {
  const can = code => SessionManager.hasPermission(code);
  const canDepartments = can('VIEW_DEPARTMENT'), canUsers = can('VIEW_USER');
  const [tab, setTab] = useState(canDepartments ? 'departments' : 'users');
  const [search, setSearch] = useState('');
  const [form, setForm] = useState(null);
  const [initial, setInitial] = useState('');
  const [formError, setFormError] = useState('');
  const action = useManagementAction();
  const loader = useCallback(async () => (await (tab === 'departments' ? getDepartments() : getUsers())).data || [], [tab]);
  const list = useManagementData(loader, tab === 'departments' ? canDepartments : canUsers);
  const records = list.data.filter(row => [row.name, row.code, row.description, row.username, row.roleName, row.role].some(value => String(value || '').toLowerCase().includes(search.toLowerCase().trim())));
  const save = async () => {
    if (!form.name.trim() || !/^[A-Z0-9_-]{1,10}$/.test(form.code.trim())) { setFormError('Enter a name and a code of 1-10 letters, digits, underscores or hyphens.'); return; }
    const payload = { ...form, name: form.name.trim(), code: form.code.trim(), description: form.description.trim() };
    await action.run(async () => { await (form.id ? updateDepartment(form.id, payload) : addDepartment(payload)); setForm(null); await list.reload(); }, 'Department saved.');
  };
  const remove = row => {
    const message = tab === 'departments' ? 'Delete department "' + row.name + '"? Referenced departments cannot be deleted.' : 'Delete account "' + row.username + '"? Linked records may also be removed.';
    if (!window.confirm(message)) return;
    action.run(async () => { await (tab === 'departments' ? deleteDepartment(row.id) : deleteUser(row.id)); await list.reload(); }, 'Record deleted.');
  };
  const open = row => { action.clear(); setFormError(''); const value = { ...EMPTY, ...row, description: row?.description || '' }; setForm(value); setInitial(JSON.stringify(value)); };
  return <div className="management-page">
    <div className="page-header"><div><h1 className="page-title">Institute Management</h1><p className="page-subtitle">Maintain departments and manage institute accounts.</p></div><div className="management-actions">{can('VIEW_ROLE') && <Link className="btn btn-secondary" to="/dashboard/roles">Roles & permissions</Link>}{tab === 'departments' && canDepartments && can('CREATE_DEPARTMENT') && <button className="btn btn-primary" disabled={action.busy} onClick={() => open(EMPTY)}>Add department</button>}</div></div>
    <div className="management-tabs" aria-label="Institute sections">{[[canDepartments, 'departments', 'Departments'], [canUsers, 'users', 'User accounts']].filter(([allowed]) => allowed).map(([, key, label]) => <button key={key} aria-pressed={tab === key} className={'btn ' + (tab === key ? 'btn-primary' : 'btn-secondary')} disabled={action.busy} onClick={() => { setTab(key); setSearch(''); action.clear(); }}>{label}</button>)}</div>
    {!canDepartments && !canUsers ? <Empty title="Access restricted">You need permission to view departments or user accounts.</Empty> : <>
      <Feedback error={action.error} notice={action.notice} />
      <Stats items={[[tab === 'departments' ? 'Departments' : 'User accounts', list.loaded ? list.data.length : '-'], ['Matching records', list.loaded ? records.length : '-']]} />
      <div className="management-toolbar"><label htmlFor="institute-search">Search {tab === 'departments' ? 'departments' : 'accounts'}</label><input id="institute-search" className="form-control" type="search" value={search} onChange={e => setSearch(e.target.value)} placeholder="Search by name, code or role" />{tab === 'users' && can('VIEW_ROLE') && <Link className="btn btn-secondary" to="/dashboard/roles?tab=users">Assign roles</Link>}</div>
      {list.loading ? <Loading /> : list.error ? <Feedback error={list.error} onRetry={list.reload} /> : <DataTable data={records} emptyMessage={search ? 'No matching records. Try another search.' : 'No records yet.'} columns={tab === 'departments' ? [
        { key: 'name', label: 'Department' }, { key: 'code', label: 'Code' }, { key: 'description', label: 'Description' },
        { key: 'actions', label: 'Actions', render: (_, row) => <div className="management-actions">{can('UPDATE_DEPARTMENT') && <button className="btn btn-secondary btn-sm" disabled={action.busy} onClick={() => open(row)}>Edit</button>}{can('DELETE_DEPARTMENT') && <button className="btn btn-danger btn-sm" disabled={action.busy} onClick={() => remove(row)}>Delete</button>}</div> }
      ] : [
        { key: 'username', label: 'Username' }, { key: 'roleName', label: 'Role', render: (value, row) => value || row.role || 'Unassigned' },
        { key: 'actions', label: 'Actions', render: (_, row) => can('DELETE_USER') && <button className="btn btn-danger btn-sm" disabled={action.busy || row.id === SessionManager.getUserId()} onClick={() => remove(row)}>{row.id === SessionManager.getUserId() ? 'Current account' : 'Delete account'}</button> }
      ]} />}
    </>}
    <Modal isOpen={Boolean(form)} title={form?.id ? 'Edit department' : 'Add department'} onClose={() => setForm(null)} onSubmit={save} submitting={action.busy} isDirty={Boolean(form && JSON.stringify(form) !== initial)}>
      {form && <form className="management-form" onSubmit={e => { e.preventDefault(); save(); }}><Feedback error={formError || action.error} /><fieldset disabled={action.busy}>{[['name', 'Department name', true], ['code', 'Department code', true], ['description', 'Description', false]].map(([key, label, required]) => <div className="form-group" key={key}><label className="form-label" htmlFor={'department-' + key}>{label}{required ? ' *' : ''}</label><input id={'department-' + key} className="form-control" required={required} maxLength={key === 'code' ? 10 : key === 'name' ? 100 : 1000} value={form[key]} onChange={e => setForm({ ...form, [key]: key === 'code' ? e.target.value.toUpperCase() : e.target.value })} /></div>)}</fieldset></form>}
    </Modal>
  </div>;
}
