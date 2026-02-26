import React from 'react';
import { Navigate } from 'react-router-dom';
import SessionManager from '../utils/SessionManager';

const PrivateRoute = ({ children }) => {
  const token = SessionManager.getToken();
  return token ? children : <Navigate to="/" replace />;
};

export default PrivateRoute;
