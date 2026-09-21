import React from 'react';
import { createRoot } from 'react-dom/client';
import { act, Simulate } from 'react-dom/test-utils';
import PlacementPage from '../pages/PlacementPage';
import ScholarshipPage from '../pages/ScholarshipPage';
import SessionManager from '../utils/SessionManager';
import * as placements from '../services/placementService';
import * as scholarships from '../services/scholarshipService';

jest.mock('../services/placementService');
jest.mock('../services/scholarshipService');
jest.mock('../utils/exportUtils', () => ({ exportToCSV: jest.fn(), exportToExcel: jest.fn() }));

let container, root;
beforeEach(() => {
  global.IS_REACT_ACT_ENVIRONMENT = true;
  container = document.createElement('div');
  document.body.appendChild(container);
  root = createRoot(container);
  SessionManager.setSession({ id: 99, username: 'S100', role: 'STAFF', permissions: [{ code: 'VIEW_PLACEMENT' }, { code: 'VIEW_SCHOLARSHIP' }] });
  placements.getCompanies.mockResolvedValue({ data: [] });
  placements.getDrives.mockResolvedValue({ data: [{ id: 1, companyName: 'Acme', jobRole: 'Engineer' }] });
  scholarships.getScholarships.mockResolvedValue({ data: [{ id: 1, title: 'Merit', amount: null, status: 'OPEN' }] });
});
afterEach(() => {
  act(() => root.unmount());
  container.remove();
  SessionManager.clearSession();
  jest.clearAllMocks();
});
const render = async component => { await act(async () => { root.render(component); }); };
const button = label => Array.from(document.querySelectorAll('button')).find(item => item.textContent.includes(label));

test('placement viewers do not fetch student applications or receive create/apply actions', async () => {
  await render(<PlacementPage />);
  expect(placements.getApplicationsForStudent).not.toHaveBeenCalled();
  expect(button('+ Add')).toBeUndefined();
  expect(button('Apply')).toBeUndefined();
  expect(container.textContent).toContain('Acme');
});

test('placement application failures show a retry state instead of an empty list', async () => {
  placements.getApplicationsForDrive.mockRejectedValue(new Error('offline'));
  await render(<PlacementPage />);
  await act(async () => { Simulate.click(button('View applications')); });
  expect(document.body.textContent).toContain('Could not load applications');
  expect(document.body.textContent).not.toContain('No applications yet');
});

test('scholarship viewers can browse missing amounts without create permission', async () => {
  await render(<ScholarshipPage />);
  expect(container.textContent).toContain('Merit');
  expect(button('New Grant')).toBeUndefined();
  expect(button('Review Candidates')).toBeDefined();
});

test('scholarship review tolerates a missing student name and hides status actions from viewers', async () => {
  scholarships.getApplications.mockResolvedValue({ data: [{ id: 2, studentId: 7, studentName: null, status: 'APPLIED', statement: 'My application' }] });
  await render(<ScholarshipPage />);
  await act(async () => { Simulate.click(button('Review Candidates')); });
  expect(container.textContent).toContain('Student 7');
  expect(button('Approve application')).toBeUndefined();
});
