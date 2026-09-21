import React, { act } from 'react';
import { createRoot } from 'react-dom/client';
import { Simulate } from 'react-dom/test-utils';
import { MemoryRouter } from 'react-router-dom';
import InstituteManagementPage from '../pages/InstituteManagementPage';
import RoleManagementPage from '../pages/RoleManagementPage';
import EmployeeManagementPage from '../pages/EmployeeManagementPage';
import PayrollManagementPage from '../pages/PayrollManagementPage';
import SessionManager from '../utils/SessionManager';
import * as institute from '../services/instituteService';
import * as employees from '../services/employeeService';
import * as payroll from '../services/payrollService';

jest.mock('../services/instituteService');
jest.mock('../services/employeeService');
jest.mock('../services/payrollService');
jest.mock('../utils/exportUtils', () => ({ exportToCSV: jest.fn() }));
let container, root;
const user = (...permissions) => SessionManager.setSession({ id: 99, username: 'HR99', role: 'HR', permissions: permissions.map(code => ({ code })) });
const button = text => Array.from(document.querySelectorAll('button')).find(item => item.textContent === text);
const render = async (page, route = '/') => { await act(async () => { root.render(<MemoryRouter initialEntries={[route]} future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>{page}</MemoryRouter>); }); };
const click = async element => { expect(element).toBeDefined(); await act(async () => Simulate.click(element)); };
const change = async (id, value) => { await act(async () => Simulate.change(document.getElementById(id), { target: { value } })); };
const deferred = () => { let resolve; const promise = new Promise(done => { resolve = done; }); return { promise, resolve }; };
beforeEach(() => {
  global.IS_REACT_ACT_ENVIRONMENT = true;
  container = document.createElement('div'); document.body.appendChild(container); root = createRoot(container);
  institute.getDepartments.mockResolvedValue({ data: [] });
  institute.getRoles.mockResolvedValue({ data: [{ id: 1, code: 'HR', name: 'Human resources' }, { id: 2, code: 'FINANCE', name: 'Finance' }] });
  institute.getUsers.mockResolvedValue({ data: [] });
  institute.getAllPermissions.mockResolvedValue({ data: [{ id: 10, name: 'View employees', code: 'VIEW_EMPLOYEE', category: 'Staff' }, { id: 20, name: 'Update employees', code: 'UPDATE_EMPLOYEE', category: 'Staff' }] });
  institute.getRolePermissions.mockResolvedValue({ data: [] });
  employees.getEmployees.mockResolvedValue({ data: [] });
  payroll.getPayroll.mockResolvedValue({ data: { data: [] } });
  jest.spyOn(window, 'confirm').mockReturnValue(true);
  jest.spyOn(SessionManager, 'refreshPermissions').mockResolvedValue([]);
});
afterEach(() => { act(() => root.unmount()); container.remove(); SessionManager.clearSession(); jest.restoreAllMocks(); jest.resetAllMocks(); });

test('department viewers load only permitted data and cannot mutate records', async () => {
  user('VIEW_DEPARTMENT');
  await render(<InstituteManagementPage />);
  expect(institute.getDepartments).toHaveBeenCalled(); expect(institute.getUsers).not.toHaveBeenCalled();
  expect(button('Add department')).toBeUndefined(); expect(button('User accounts')).toBeUndefined();
});
test('role creation works from the modal footer without a submit event', async () => {
  user('VIEW_ROLE', 'CREATE_ROLE');
  await render(<RoleManagementPage />);
  await click(button('Add role'));
  await change('role-name', 'Librarian'); await change('role-code', 'LIBRARIAN');
  await click(button('Create role'));
  expect(institute.addRole).toHaveBeenCalledWith(expect.objectContaining({ name: 'Librarian', code: 'LIBRARIAN' }));
  expect(document.querySelector('[role=dialog]')).toBeNull();
  expect(institute.getUsers).not.toHaveBeenCalled();
});
test('an unchanged department form closes without a discard prompt', async () => {
  user('VIEW_DEPARTMENT', 'CREATE_DEPARTMENT');
  await render(<InstituteManagementPage />);
  await click(button('Add department')); await click(button('Cancel'));
  expect(window.confirm).not.toHaveBeenCalled();
  expect(document.querySelector('[role=dialog]')).toBeNull();
});
test('late permission responses cannot overwrite the selected role', async () => {
  user('VIEW_ROLE', 'UPDATE_ROLE');
  const first = deferred(), second = deferred();
  institute.getRolePermissions.mockImplementation(id => id === '1' ? first.promise : second.promise);
  await render(<RoleManagementPage />, '/dashboard/roles?tab=permissions');
  await change('permission-role', '1'); await change('permission-role', '2');
  await act(async () => second.resolve({ data: [{ id: 20 }] }));
  await act(async () => first.resolve({ data: [{ id: 10 }] }));
  const checks = Array.from(document.querySelectorAll('.permission-option input'));
  expect(checks.map(input => input.checked)).toEqual([false, true]);
  expect(button('Save permissions').disabled).toBe(true);
});
test('switching roles preserves an unsaved draft when discard is cancelled', async () => {
  user('VIEW_ROLE', 'UPDATE_ROLE');
  await render(<RoleManagementPage />, '/dashboard/roles?tab=permissions');
  await change('permission-role', '1');
  await act(async () => Simulate.change(document.querySelector('.permission-option input')));
  window.confirm.mockReturnValue(false);
  await change('permission-role', '2');
  expect(document.getElementById('permission-role').value).toBe('1');
  expect(button('Save permissions').disabled).toBe(false);
});
test('employee updates send the full profile object, including monthly salary', async () => {
  user('VIEW_EMPLOYEE', 'UPDATE_EMPLOYEE');
  employees.getEmployees.mockResolvedValue({ data: [{ id: 7, employeeId: 'EMP7', firstName: 'Alice', lastName: '', email: 'alice@example.org', designation: 'Teacher', salary: 25000, status: 'ACTIVE' }] });
  await render(<EmployeeManagementPage />); await click(button('Edit profile')); await change('employee-salary', '27500.50'); await click(button('Save'));
  expect(employees.updateEmployee).toHaveBeenCalledWith(expect.objectContaining({ id: 7, salary: '27500.50', employeeId: 'EMP7' }));
  expect(employees.updateEmployee.mock.calls[0]).toHaveLength(1);
});
test('missing employee fields are searchable and profile setup requires creation permission', async () => {
  user('VIEW_EMPLOYEE', 'UPDATE_EMPLOYEE');
  employees.getEmployees.mockResolvedValue({ data: [{ id: 0, employeeId: 'HR1', firstName: null, designation: null, status: 'ACTIVE' }] });
  await render(<EmployeeManagementPage />); await change('employee-search', 'hr1');
  expect(container.textContent).toContain('HR1'); expect(button('Set up profile')).toBeUndefined();
});
test('payroll viewers receive useful errors without payment or generate actions', async () => {
  user('VIEW_PAYROLL'); payroll.getPayroll.mockRejectedValue(new Error('Ledger unavailable'));
  await render(<PayrollManagementPage />);
  expect(container.textContent).toContain('Ledger unavailable'); expect(button('Retry')).toBeDefined();
  expect(button('Generate payroll')).toBeUndefined(); expect(button('Mark all pending paid')).toBeUndefined();
});
test('an old month response cannot replace the latest payroll period', async () => {
  user('VIEW_PAYROLL'); const old = deferred(); payroll.getPayroll.mockReturnValueOnce(old.promise).mockResolvedValue({ data: { data: [{ id: 2, employeeName: 'Current month', netSalary: 1, status: 'PAID' }] } });
  await render(<PayrollManagementPage />);
  const current = Number(document.getElementById('payroll-month').value);
  await change('payroll-month', String(current === 1 ? 2 : 1));
  await act(async () => old.resolve({ data: { data: [{ id: 1, employeeName: 'Old month', status: 'PENDING' }] } }));
  expect(container.textContent).toContain('Current month'); expect(container.textContent).not.toContain('Old month');
});
test('bulk payment confirms pending count and amount and excludes cancelled records', async () => {
  user('VIEW_PAYROLL', 'MANAGE_PAYROLL');
  payroll.getPayroll.mockResolvedValue({ data: { data: [{ id: 1, netSalary: 100.50, status: 'PENDING' }, { id: 2, netSalary: 900, status: 'CANCELLED' }] } });
  await render(<PayrollManagementPage />); await click(button('Mark all pending paid'));
  expect(window.confirm.mock.calls[0][0]).toContain('1 pending salaries');
  expect(window.confirm.mock.calls[0][0]).toContain('100.50'); expect(payroll.markAllAsPaid).toHaveBeenCalledTimes(1);
});
