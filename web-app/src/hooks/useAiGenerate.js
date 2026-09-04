import { useCallback, useEffect, useRef, useState } from 'react';
import { generateDraft } from '../services/aiService';

const TIMEOUT_MS = 35000;

/**
 * State hook for a single AI draft request.
 * Aborts in-flight requests on unmount / re-run and surfaces
 * user-friendly errors for timeout, rate-limit and config states.
 */
export default function useAiGenerate() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState('');
  const [model, setModel] = useState('');
  const abortRef = useRef(null);

  useEffect(() => () => {
    if (abortRef.current) abortRef.current.abort();
  }, []);

  const run = useCallback(async (feature, payload) => {
    if (abortRef.current) abortRef.current.abort();
    const controller = new AbortController();
    abortRef.current = controller;
    setLoading(true);
    setError('');
    setResult('');
    setModel('');
    const timer = setTimeout(() => controller.abort(), TIMEOUT_MS);
    try {
      const res = await generateDraft(feature, payload, controller.signal);
      const text = res.data?.text || '';
      setResult(text);
      setModel(res.data?.model || '');
      return text;
    } catch (err) {
      if (err?.code === 'ERR_CANCELED' || err?.name === 'CanceledError') {
        setError('Request timed out. Please try again.');
      } else if (err.response?.status === 429) {
        setError('Too many AI requests. Please wait a while and retry.');
      } else if (err.response?.status === 403) {
        setError('You do not have permission to use AI drafting.');
      } else if (err.response?.status === 503) {
        setError(err.response?.data?.error || 'AI assistance is not configured.');
      } else {
        setError(err.response?.data?.error || 'AI request failed. Please try again.');
      }
      return '';
    } finally {
      clearTimeout(timer);
      setLoading(false);
    }
  }, []);

  const reset = useCallback(() => {
    setError('');
    setResult('');
    setModel('');
  }, []);

  return { loading, error, result, model, run, reset, setResult };
}
