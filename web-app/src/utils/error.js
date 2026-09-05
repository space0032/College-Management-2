const getRefId = () => `REF-${Date.now().toString(36).toUpperCase()}-${Math.random().toString(36).slice(2, 6).toUpperCase()}`;

export function getErrorMessage(err, fallback = 'The server could not process this request.') {
  const data = err?.response?.data;
  const status = err?.response?.status;
  const serverMsg = data?.error || data?.message;
  if (serverMsg && typeof serverMsg === 'string' && serverMsg.trim()) {
    return { message: serverMsg, status, refId: getRefId() };
  }
  let message = fallback;
  if (status === 400) message = 'Some details look invalid. Please review the highlighted fields.';
  else if (status === 401) message = 'Your session has expired. Please log in again.';
  else if (status === 403) message = 'You do not have permission to perform this action.';
  else if (status === 404) message = 'The requested record is no longer available.';
  else if (status === 409) message = 'This conflicts with an existing record (e.g. faculty already has a class during this time).';
  else if (status === 422) message = 'Maximum workload exceeded or validation failed.';
  else if (status >= 500) message = 'The server could not process this request. Please try again.';
  else if (err?.code === 'ERR_NETWORK' || err?.message === 'Network Error') message = 'Could not reach the server. Check your connection and retry.';
  return { message, status, refId: getRefId() };
}

export function getSuccessRefId() {
  return getRefId();
}

export default getErrorMessage;
