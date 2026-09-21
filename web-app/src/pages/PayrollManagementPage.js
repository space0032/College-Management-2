import React, { useCallback, useState } from 'react';
import { getPayroll, generatePayroll, markAsPaid, markAllAsPaid, updatePayrollEntry } from '../services/payrollService';
import SessionManager from '../utils/SessionManager';
import Modal from '../components/Modal';
import { currency, Empty, Feedback, Loading, Stats, useManagementAction, useManagementData } from '../components/ManagementUI';

const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
export default function PayrollManagementPage() {
  const can = code => SessionManager.hasPermission(code);
  const [month, setMonth] = useState(new Date().getMonth() + 1), [year, setYear] = useState(String(new Date().getFullYear()));
  const [query, setQuery] = useState(''), [status, setStatus] = useState('ALL');
  const [edit, setEdit] = useState(null), [formError, setFormError] = useState('');
  const [report, setReport] = useState(null);
  const action = useManagementAction();
  const validPeriod = /^\d{1,4}$/.test(year) && Number(year) >= 1;
  const loader = useCallback(async () => (await getPayroll(month, Number(year))).data?.data || [], [month, year]);
  const list = useManagementData(loader, can('VIEW_PAYROLL') && validPeriod);
  const pending = list.data.filter(entry => entry.status === 'PENDING');
  const sum = (rows, key) => rows.reduce((total, row) => total + (Number(row[key]) || 0), 0);
  const label = MONTHS[month - 1] + ' ' + year;
  const records = list.data.filter(entry => (status === 'ALL' || entry.status === status) && (String(entry.employeeName || '') + ' ' + String(entry.employeeId || '') + ' ' + String(entry.designation || '')).toLowerCase().includes(query.trim().toLowerCase()));
  const generate = () => {
    if (!window.confirm('Generate payroll for ' + label + '? Existing entries will be kept. Ineligible staff will be skipped and listed.')) return;
    action.run(async () => { const response = await generatePayroll(month, Number(year)); setReport({ ...response.data, period: label }); await list.reload(); }, 'Payroll generation completed. Review the batch result below.');
  };
  const pay = entry => {
    if (!window.confirm('Mark ' + currency(entry.netSalary) + ' as paid for ' + entry.employeeName + ' (' + label + ')? Paid entries are locked. This records payment only.')) return;
    action.run(async () => { await markAsPaid(entry.id); await list.reload(); }, 'Payment recorded.');
  };
  const payAll = () => {
    if (!pending.length || !window.confirm('Mark ' + pending.length + ' pending salaries totaling ' + currency(sum(pending, 'netSalary')) + ' as paid for ' + label + '? Paid entries are locked. This records payment only.')) return;
    action.run(async () => { await markAllAsPaid(month, Number(year)); await list.reload(); }, 'Pending payments recorded.');
  };
  const save = async () => {
    const amounts = [edit.bonusesInput, edit.deductionsInput].map(value => String(value).trim());
    if (amounts.some(value => !/^\d+(\.\d{1,2})?$/.test(value) || Number(value) > 99999999.99)) { setFormError('Enter non-negative amounts with at most two decimal places.'); return; }
    const net = Math.round(Number(edit.basicSalary) * 100) + Math.round(Number(amounts[0]) * 100) - Math.round(Number(amounts[1]) * 100);
    if (net < 0 || net > 9999999999) { setFormError('Net salary must be between INR 0.00 and INR 99,999,999.99.'); return; }
    await action.run(async () => { await updatePayrollEntry(edit.id, amounts[0], amounts[1]); setEdit(null); await list.reload(); }, 'Payroll adjustment saved.');
  };
  if (!can('VIEW_PAYROLL')) return <div className="management-page"><Empty title="Access restricted">You need permission to view payroll.</Empty></div>;
  return <div className="management-page">
    <div className="page-header"><div><h1 className="page-title">Payroll</h1><p className="page-subtitle">Monthly salaries, adjustments and payment records.</p></div><div className="management-actions">{can('MANAGE_PAYROLL') && <button className="btn btn-primary" disabled={action.busy || !list.loaded || Boolean(edit)} onClick={generate}>Generate payroll</button>}</div></div>
    <div className="management-toolbar"><label htmlFor="payroll-month">Pay period</label><select id="payroll-month" className="form-control" disabled={action.busy || Boolean(edit)} value={month} onChange={e => { setMonth(Number(e.target.value)); setReport(null); action.clear(); }}>{MONTHS.map((name, index) => <option key={name} value={index + 1}>{name}</option>)}</select><label htmlFor="payroll-year">Year</label><input id="payroll-year" className="form-control" style={{ width: 110 }} disabled={action.busy || Boolean(edit)} type="number" min="1" max="9999" value={year} onChange={e => { setYear(e.target.value); setReport(null); action.clear(); }} /></div>
    {!validPeriod && <Feedback error="Enter a year between 1 and 9999." />}
    <Feedback error={action.error} notice={action.notice} />
    <Stats items={[[ 'Net payroll - ' + label, list.loaded ? currency(sum(list.data.filter(e => e.status !== 'CANCELLED'), 'netSalary')) : '-'], ['Paid', list.loaded ? currency(sum(list.data.filter(e => e.status === 'PAID'), 'netSalary')) : '-'], ['Pending', list.loaded ? currency(sum(pending, 'netSalary')) : '-'], ['Pending employees', list.loaded ? pending.length : '-']]} />
    {report && <section className="management-card" aria-label="Generation result"><h3>Batch result &middot; {report.period}</h3><p>{report.generated} generated &middot; {report.existing || 0} already existed &middot; {(report.skipped || []).length} skipped in total</p>{report.skipped?.length > 0 && <details><summary>Review skipped employees</summary><ul>{report.skipped.map((item, index) => <li key={String(item.employeeId) + '-' + index}>{item.employeeId}: {item.reason}</li>)}</ul></details>}</section>}
    <div className="management-toolbar"><label htmlFor="payroll-search">Find a salary record</label><input id="payroll-search" className="form-control" type="search" placeholder="Employee name, ID or designation" value={query} onChange={e => setQuery(e.target.value)} /><select className="form-control" aria-label="Payment status" value={status} onChange={e => setStatus(e.target.value)}>{['ALL', 'PENDING', 'PAID', 'CANCELLED'].map(value => <option key={value} value={value}>{value === 'ALL' ? 'All statuses' : value}</option>)}</select>{can('MANAGE_PAYROLL') && <button className="btn btn-secondary" disabled={action.busy || !list.loaded || !pending.length || Boolean(edit)} onClick={payAll}>Mark all pending paid</button>}</div>
    {list.loading ? <Loading /> : list.error ? <Feedback error={list.error} onRetry={list.reload} /> : validPeriod && (!records.length ? <Empty title="No payroll records">{query || status !== 'ALL' ? 'Try another search or status filter.' : 'No payroll has been generated for this period.'}</Empty> : <div className="table-wrapper"><table className="data-table"><thead><tr><th>Employee</th><th>Basic</th><th>Bonus</th><th>Deductions</th><th>Net salary</th><th>Status</th><th>Actions</th></tr></thead><tbody>{records.map(entry => <tr key={entry.id}><td><strong>{entry.employeeName || 'Employee ' + entry.employeeId}</strong><div className="management-muted">{entry.designation || 'No designation'} &middot; ID {entry.employeeId}</div></td>{['basicSalary', 'bonuses', 'deductions', 'netSalary'].map(key => <td key={key} className="money">{currency(entry[key])}</td>)}<td><span className={'badge ' + (entry.status === 'PAID' ? 'badge-success' : entry.status === 'PENDING' ? 'badge-warning' : 'badge-secondary')}>{entry.status}</span>{entry.paymentDate && <div className="management-muted">{entry.paymentDate}</div>}</td><td>{entry.status === 'PENDING' ? <div className="management-actions">{can('UPDATE_PAYROLL') && <button className="btn btn-secondary btn-sm" disabled={action.busy} onClick={() => { action.clear(); setFormError(''); setEdit({ ...entry, bonusesInput: String(entry.bonuses ?? 0), deductionsInput: String(entry.deductions ?? 0) }); }}>Adjust</button>}{can('MANAGE_PAYROLL') && <button className="btn btn-primary btn-sm" disabled={action.busy} onClick={() => pay(entry)}>Mark paid</button>}</div> : <span className="management-muted">Locked</span>}</td></tr>)}</tbody></table></div>)}
    <Modal isOpen={Boolean(edit)} title="Adjust pending payroll" onClose={() => setEdit(null)} onSubmit={save} submitting={action.busy} isDirty={Boolean(edit && (Number(edit.bonusesInput) !== Number(edit.bonuses) || Number(edit.deductionsInput) !== Number(edit.deductions)))}>
      {edit && <form className="management-form" onSubmit={e => { e.preventDefault(); save(); }}><p>{edit.employeeName} &middot; {label}</p><Feedback error={formError || action.error} /><fieldset disabled={action.busy}>{[['bonusesInput', 'Bonus (INR)'], ['deductionsInput', 'Deductions (INR)']].map(([key, text]) => <div className="form-group" key={key}><label className="form-label" htmlFor={key}>{text}</label><input id={key} className="form-control" type="number" min="0" max="99999999.99" step="0.01" required value={edit[key]} onChange={e => setEdit({ ...edit, [key]: e.target.value })} /></div>)}<p>Basic salary: {currency(edit.basicSalary)}</p><p><strong>Net salary: {currency((Math.round(Number(edit.basicSalary) * 100) + Math.round(Number(edit.bonusesInput || 0) * 100) - Math.round(Number(edit.deductionsInput || 0) * 100)) / 100)}</strong></p></fieldset></form>}
    </Modal>
  </div>;
}
