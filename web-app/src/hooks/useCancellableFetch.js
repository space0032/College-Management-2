import { useCallback, useEffect, useRef, useState } from 'react';

// Ignores stale responses via request id + aborts on unmount / re-fetch.
// Usage: const { data, loading, error, run, retry } = useCancellableFetch(fetcher, { immediate: true });
export function useCancellableFetch(fetcher, { immediate = false, initialData = null } = {}) {
  const [data, setData] = useState(initialData);
  const [loading, setLoading] = useState(immediate);
  const [error, setError] = useState('');
  const reqId = useRef(0);
  const abortRef = useRef(null);
  const fetcherRef = useRef(fetcher);
  fetcherRef.current = fetcher;

  const run = useCallback(async (...args) => {
    const id = ++reqId.current;
    if (abortRef.current) abortRef.current.abort();
    const controller = new AbortController();
    abortRef.current = controller;
    setLoading(true);
    setError('');
    try {
      const res = await fetcherRef.current(...args, controller.signal);
      if (reqId.current !== id || controller.signal.aborted) return null;
      const payload = res?.data ?? res;
      setData(payload);
      return payload;
    } catch (err) {
      if (controller.signal.aborted) return null;
      if (reqId.current !== id) return null;
      const msg = err?.response?.data?.error || err?.message || 'Failed to load data.';
      setError(msg);
      return null;
    } finally {
      if (reqId.current === id && !controller.signal.aborted) setLoading(false);
    }
  }, []);

  const retry = useCallback((...args) => run(...args), [run]);

  useEffect(() => {
    if (immediate) run();
    return () => {
      reqId.current++;
      abortRef.current?.abort();
    };
  }, [immediate, run]);

  return { data, setData, loading, error, setError, run, retry };
}

export default useCancellableFetch;
