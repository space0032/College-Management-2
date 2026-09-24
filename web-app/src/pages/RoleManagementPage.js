import React, { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getRoles, addRole, deleteRole, getUsers, updateUserRole, updateUserSecondaryRoles, getAllPermissions, getRolePermissions, setRolePermissions } from '../services/instituteService';
import SessionManager from '../utils/SessionManager';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { Empty, Feedback, Loading, Stats, useManagementAction, useManagementData } from '../components/ManagementUI';

const PROTECTED = ['ADMIN', 'FACULTY', 'STUDENT', 'WARDEN', 'FINANCE'];
const EMPTY = { code: '', name: '', description: '', portalType: 'ADMIN' };
export default function RoleManagementPage() {
  const can = code => SessionManager.hasPermission(code);
  const canView = can('VIEW_ROLE'), canUsers = can('VIEW_USER');
  const [params, setParams] = useSearchParams();
  const tab = ['roles', 'users', 'permissions'].includes(params.get('tab')) ? params.get('tab') : 'roles';
  const [form, setForm] = useState(null), [search, setSearch] = useState(''), [selectedId, setSelectedId] = useState('');
  const [draft, setDraft] = useState({ roleId: '', ids: [] });
  const action = useManagementAction();
  const roleLoader = useCallback(async () => (await getRoles()).data || [], []);
  const userLoader = useCallback(async () => (await getUsers()).data || [], []);
  const catalogLoader = useCallback(async () => (await getAllPermissions()).data || [], []);
  const permissionLoader = useCallback(async () => (await getRolePermissions(selectedId)).data || [], [selectedId]);
  const roles = useManagementData(roleLoader, canView);
  const users = useManagementData(userLoader, canView && canUsers);
  const catalog = useManagementData(catalogLoader, canView && tab === 'permissions');
  const permissions = useManagementData(permissionLoader, canView && tab === 'permissions' && Boolean(selectedId));
  const selected = roles.data.find(role => String(role.id) === selectedId);
  const sorted = ids => [...ids].map(Number).sort((a, b) => a - b).join(',');
  const dirty = permissions.loaded && draft.roleId === selectedId && sorted(draft.ids) !== sorted(permissions.data.map(p => p.id));
  useEffect(() => { if (permissions.loaded) setDraft({ roleId: selectedId, ids: permissions.data.map(p => p.id) }); }, [permissions.loaded, permissions.data, selectedId]);
  useEffect(() => {
    if (!dirty) return undefined;
    const warn = event => { event.preventDefault(); event.returnValue = ''; };
    window.addEventListener('beforeunload', warn);
    return () => window.removeEventListener('beforeunload', warn);
  }, [dirty]);
  const discard = () => !dirty || window.confirm('Discard unsaved permission changes?');
  const changeTab = next => { if (!discard()) return; action.clear(); setSearch(''); setParams({ tab: next }); };
  const choose = id => { if (!discard()) return; action.clear(); setSelectedId(id); };
const create = () => {
    if (!form) { action.clear(); action.run(async () => { throw new Error('Role form state was lost. Reopen the form and retry.'); }); return; }
    action.run(async () => {
    const payload = { ...form, code: form.code.trim().toUpperCase(), name: form.name.trim(), description: form.description.trim() };
    if (!/^[A-Z][A-Z0-9_]{0,49}$/.test(payload.code) || !payload.name) throw new Error('Enter a valid role code and name.');
    await addRole(payload); setForm(null); await roles.reload();
  }, 'Role created.');
  };
  const remove = role => { if (window.confirm('Delete role "' + role.name + '"? Assigned roles must be reassigned first.')) action.run(async () => { await deleteRole(role.id); if (selectedId === String(role.id)) setSelectedId(''); await roles.reload(); }, 'Role deleted.'); };
const assign = (user, roleId) => {
    if (!roleId || Number(roleId) === user.roleId) return;
    const role = roles.data.find(r => r.id === Number(roleId));
    if (!role) { action.clear(); action.run(async () => { throw new Error('The selected role no longer exists. Reload the page and retry.'); }); return; }
    const demotingSelf = user.id === SessionManager.getUserId();
    const message = demotingSelf && Number(roleId) !== SessionManager.getUser()?.roleId
      ? 'You are changing your OWN role to ' + role.name + '. You may lose access to parts of this system. Continue anyway?'
      : 'Assign ' + role.name + ' to ' + user.username + '?';
    if (!window.confirm(message)) return;
    action.run(async () => { await updateUserRole(user.id, Number(roleId)); await SessionManager.refreshPermissions(); await users.reload(); }, 'User role updated.');
  };
const savePermissions = () => action.run(async () => {
    await setRolePermissions(selectedId, draft.ids); await SessionManager.refreshPermissions(); await permissions.reload(); await roles.reload();
  }, 'Permissions saved.');
  const [secondaryUser, setSecondaryUser] = useState(null);
  const [secondaryIds, setSecondaryIds] = useState([]);
  const openSecondary = user => { setSecondaryIds((user.secondaryRoles || []).map(r => r.id)); setSecondaryUser(user); };
  const eligibleSecondaryRoles = user => roles.data.filter(r => r.code !== 'ADMIN' && r.id !== (user && user.roleId));
  const saveSecondary = () => action.run(async () => {
    await updateUserSecondaryRoles(secondaryUser.id, secondaryIds);
    if (secondaryUser.id === SessionManager.getUserId()) await SessionManager.refreshPermissions();
    setSecondaryUser(null); await users.reload();
  }, 'Secondary roles updated.');
  const editable = can('UPDATE_ROLE') && selected?.code !== 'ADMIN' && permissions.loaded && catalog.loaded && draft.roleId === selectedId && !action.busy;
  const grouped = catalog.data.reduce((groups, permission) => { const category = permission.category || 'Other'; (groups[category] ||= []).push(permission); return groups; }, {});
  const toggle = ids => setDraft(prev => ({ ...prev, ids: ids.every(id => prev.ids.includes(id)) ? prev.ids.filter(id => !ids.includes(id)) : [...new Set([...prev.ids, ...ids])] }));
  const counts = users.data.reduce((result, user) => { result[user.roleId] = (result[user.roleId] || 0) + 1; return result; }, {});
  if (!canView) return <div className="management-page"><Empty title="Access restricted">You need permission to view roles.</Empty></div>;
  return <div className="management-page">
    <div className="page-header"><div><h1 className="page-title">Roles & permissions</h1><p className="page-subtitle">Define access, assign roles and review permissions in one place.</p></div>{can('CREATE_ROLE') && <button className="btn btn-primary" disabled={action.busy} onClick={() => { action.clear(); setForm({ ...EMPTY }); }}>Add role</button>}</div>
    <Stats items={[[ 'Roles', roles.loaded ? roles.data.length : '-'], ['User accounts', users.loaded ? users.data.length : '-'], ['Protected roles', roles.loaded ? roles.data.filter(r => r.systemRole || PROTECTED.includes(r.code)).length : '-']]} />
    <div className="management-tabs">{[['roles', 'Roles'], ...(canUsers ? [['users', 'User assignments']] : []), ['permissions', 'Permissions']].map(([key, label]) => <button key={key} aria-pressed={tab === key} className={'btn ' + (tab === key ? 'btn-primary' : 'btn-secondary')} disabled={action.busy} onClick={() => changeTab(key)}>{label}</button>)}</div>
    <Feedback error={action.error} notice={action.notice} />
    {roles.error ? <Feedback error={roles.error} onRetry={roles.reload} /> : roles.loading ? <Loading /> : <>
      {tab === 'roles' && <><div className="management-toolbar"><label htmlFor="role-search">Find a role</label><input id="role-search" className="form-control" type="search" placeholder="Search name or code" value={search} onChange={e => setSearch(e.target.value)} /></div><div className="management-grid">{roles.data.filter(r => (r.name + ' ' + r.code).toLowerCase().includes(search.toLowerCase().trim())).map(role => <article key={role.id} className="management-card"><h3>{role.name}</h3><span className="badge badge-primary">{role.code}</span><p>{role.description || 'No description provided.'}</p><p>{users.loaded ? (counts[role.id] || 0) + ' assigned users' : 'User counts unavailable'} &middot; {role.portalType} portal</p><div className="management-actions"><button className="btn btn-secondary btn-sm" disabled={action.busy} onClick={() => { setSelectedId(String(role.id)); changeTab('permissions'); }}>Permissions</button>{role.systemRole || PROTECTED.includes(role.code) ? <span className="badge badge-secondary">Protected</span> : can('DELETE_ROLE') && <button className="btn btn-danger btn-sm" disabled={action.busy || Boolean(counts[role.id])} onClick={() => remove(role)}>Delete</button>}</div></article>)}</div>{!roles.data.some(r => (r.name + ' ' + r.code).toLowerCase().includes(search.toLowerCase().trim())) && <Empty>No roles match your search.</Empty>}</>}
      {tab === 'users' && (!canUsers ? <Empty title="Access restricted">You need permission to view user accounts.</Empty> : users.loading ? <Loading /> : users.error ? <Feedback error={users.error} onRetry={users.reload} /> : <><div className="management-toolbar"><label htmlFor="user-search">Find an account</label><input id="user-search" type="search" className="form-control" placeholder="Username or role" value={search} onChange={e => setSearch(e.target.value)} /></div><DataTable data={users.data.filter(u => (u.username + ' ' + (u.roleName || u.role || '')).toLowerCase().includes(search.toLowerCase().trim()))} columns={[{ key: 'username', label: 'Username' }, { key: 'roleName', label: 'Role', render: (value, user) => can('UPDATE_USER') ? <select className="form-control" aria-label={'Role for ' + user.username} disabled={action.busy} value={user.roleId || ''} onChange={e => assign(user, e.target.value)}><option value="" disabled>Unassigned</option>{roles.data.map(role => <option key={role.id} value={role.id}>{role.name}</option>)}</select> : value || user.role || 'Unassigned' }, { key: 'secondaryRoles', label: 'Secondary roles', render: (value, user) => <span className="management-secondary">{(user.secondaryRoles || []).map(r => <span key={r.id} className="badge badge-secondary">{r.name}</span>)}{can('UPDATE_USER') && <button className="btn btn-secondary btn-sm" disabled={action.busy} onClick={() => openSecondary(user)}>Manage</button>}</span> }]} /></>)}
      {tab === 'permissions' && <><div className="management-toolbar"><label htmlFor="permission-role">Role</label><select id="permission-role" className="form-control" value={selectedId} disabled={action.busy} onChange={e => choose(e.target.value)}><option value="">Select a role</option>{roles.data.map(role => <option key={role.id} value={role.id}>{role.name}</option>)}</select><span className="management-muted">{dirty ? 'Unsaved changes' : 'Select a role to review its access'}</span>{can('UPDATE_ROLE') && <button className="btn btn-primary" disabled={!editable || !dirty} onClick={savePermissions}>Save permissions</button>}</div>
        {!selected ? <Empty title="Choose a role">Review its permissions before making changes.</Empty> : selected.code === 'ADMIN' ? <Empty title="Administrator access is built in">Administrators have full access. This permission set is read-only.</Empty> : permissions.loading || catalog.loading ? <Loading /> : permissions.error || catalog.error ? <Feedback error={permissions.error || catalog.error} onRetry={() => { permissions.reload(); catalog.reload(); }} /> : Object.entries(grouped).map(([category, perms]) => <fieldset key={category} className="permission-category" disabled={!editable}><legend>{category}</legend><label><input type="checkbox" aria-label={'Select all ' + category + ' permissions'} checked={perms.every(p => draft.ids.includes(p.id))} ref={el => { if (el) el.indeterminate = perms.some(p => draft.ids.includes(p.id)) && !perms.every(p => draft.ids.includes(p.id)); }} onChange={() => toggle(perms.map(p => p.id))} /> Select all in {category}</label><div className="permission-options">{perms.map(permission => <label key={permission.id} className="permission-option"><input type="checkbox" checked={draft.ids.includes(permission.id)} onChange={() => toggle([permission.id])} /><span>{permission.name}<small className="management-muted" style={{ display: 'block' }}>{permission.description || permission.code}</small></span></label>)}</div></fieldset>)}
      </>}
    </>}
    <Modal isOpen={Boolean(form)} title="Create role" onClose={() => setForm(null)} onSubmit={create} submitting={action.busy} submitLabel="Create role" isDirty={Boolean(form && (form.code || form.name || form.description || form.portalType !== EMPTY.portalType))}>{form && <form className="management-form" onSubmit={e => { e.preventDefault(); create(); }}><Feedback error={action.error} /><fieldset disabled={action.busy}>{[['name', 'Role name'], ['code', 'Role code'], ['description', 'Description']].map(([key, label]) => <div className="form-group" key={key}><label className="form-label" htmlFor={'role-' + key}>{label}{key !== 'description' ? ' *' : ''}</label><input id={'role-' + key} className="form-control" required={key !== 'description'} pattern={key === 'code' ? '[A-Z][A-Z0-9_]{0,49}' : undefined} maxLength={key === 'code' ? 50 : key === 'name' ? 100 : 1000} value={form[key]} onChange={e => setForm({ ...form, [key]: key === 'code' ? e.target.value.toUpperCase().replace(/\s/g, '_') : e.target.value })} /></div>)}<div className="form-group"><label htmlFor="role-portal" className="form-label">Portal</label><select id="role-portal" className="form-control" value={form.portalType} onChange={e => setForm({ ...form, portalType: e.target.value })}>{['ADMIN', 'FACULTY', 'STUDENT', 'WARDEN', 'FINANCE'].map(portal => <option key={portal}>{portal}</option>)}</select></div></fieldset></form>}</Modal>
    <Modal isOpen={Boolean(secondaryUser)} title={'Secondary roles for ' + (secondaryUser?.username || '')} onClose={() => setSecondaryUser(null)} onSubmit={saveSecondary} submitting={action.busy} submitLabel="Save secondary roles">{secondaryUser && <div className="management-form"><p className="management-muted">Secondary roles grant their permissions without changing the primary role shown for this user. The ADMIN role cannot be assigned as a secondary role.</p><div className="permission-options">{eligibleSecondaryRoles(secondaryUser).map(role => <label key={role.id} className="permission-option"><input type="checkbox" checked={secondaryIds.includes(role.id)} onChange={() => setSecondaryIds(prev => prev.includes(role.id) ? prev.filter(id => id !== role.id) : [...prev, role.id])} /><span>{role.name}<small className="management-muted" style={{ display: 'block' }}>{role.code}</small></span></label>)}{eligibleSecondaryRoles(secondaryUser).length === 0 && <p className="management-muted">No other roles are available to assign.</p>}</div></div>}</Modal>
  </div>;
}
