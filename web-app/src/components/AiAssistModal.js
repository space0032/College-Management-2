import React, { useState } from 'react';
import Modal from './Modal';
import useAiGenerate from '../hooks/useAiGenerate';

const TONES = ['formal', 'friendly', 'urgent'];
const COUNTS = [3, 5, 8];

/**
 * Reusable AI drafting dialog (F1 assignments, A2 announcements).
 *
 * Props:
 *   isOpen, onClose, onInsert(draftText), feature ('assignment'|'announcement'),
 *   defaults ({ courseName, audience })
 *
 * Draft-only: the result is inserted into the caller's form for human
 * review — nothing is ever saved from this component.
 */
const AiAssistModal = ({ isOpen, onClose, onInsert, feature, defaults }) => {
  const isAssignment = feature === 'assignment';
  const [topic, setTopic] = useState('');
  const [courseName, setCourseName] = useState(defaults?.courseName || '');
  const [count, setCount] = useState(5);
  const [bullets, setBullets] = useState('');
  const [tone, setTone] = useState('formal');
  const { loading, error, result, model, run, reset, setResult } = useAiGenerate();

  // Keep the course default in sync when the caller changes selection.
  React.useEffect(() => {
    if (isOpen && defaults?.courseName) setCourseName(defaults.courseName);
  }, [isOpen, defaults?.courseName]);

  const canGenerate = isAssignment ? topic.trim().length > 0 : bullets.trim().length > 0;

  const handleClose = () => {
    reset();
    onClose();
  };

  const handleGenerate = () => {
    if (!canGenerate || loading) return;
    if (isAssignment) {
      run('assignment', { topic: topic.trim(), courseName: courseName.trim(), count: Number(count) });
    } else {
      run('announcement', {
        bullets: bullets.trim(),
        tone,
        audience: defaults?.audience || 'ALL',
      });
    }
  };

  const handleInsert = () => {
    if (!result) return;
    onInsert(result);
    handleClose();
  };

  return (
    <Modal
      isOpen={isOpen}
      title={isAssignment ? '✨ Generate Assignment Draft' : '✨ Draft Announcement'}
      onClose={handleClose}
    >
      {isAssignment ? (
        <>
          <div className="form-group">
            <label className="form-label">Course (context)</label>
            <input
              type="text"
              className="form-control"
              value={courseName}
              onChange={(e) => setCourseName(e.target.value)}
              placeholder="e.g. Data Structures"
            />
          </div>
          <div className="form-group">
            <label className="form-label">Topic *</label>
            <textarea
              rows={3}
              className="form-control"
              value={topic}
              onChange={(e) => setTopic(e.target.value)}
              placeholder="e.g. Binary search trees: insertion, deletion and traversals"
            />
          </div>
          <div className="form-group">
            <label className="form-label">Questions</label>
            <select className="form-control" value={count} onChange={(e) => setCount(e.target.value)}>
              {COUNTS.map((c) => (
                <option key={c} value={c}>{c} questions</option>
              ))}
            </select>
          </div>
        </>
      ) : (
        <>
          <div className="form-group">
            <label className="form-label">Key points (one per line) *</label>
            <textarea
              rows={4}
              className="form-control"
              value={bullets}
              onChange={(e) => setBullets(e.target.value)}
              placeholder={'e.g.\nLibrary closed on Friday for maintenance\nIssue/return books by Thursday'}
            />
          </div>
          <div className="form-group">
            <label className="form-label">Tone</label>
            <select className="form-control" value={tone} onChange={(e) => setTone(e.target.value)}>
              {TONES.map((t) => (
                <option key={t} value={t}>{t.charAt(0).toUpperCase() + t.slice(1)}</option>
              ))}
            </select>
          </div>
        </>
      )}

      {error && <div className="alert alert-error" style={{ marginBottom: 12 }}>{error}</div>}

      <div style={{ display: 'flex', gap: '10px', marginBottom: result ? '12px' : 0 }}>
        <button
          type="button"
          className="btn btn-primary"
          disabled={!canGenerate || loading}
          onClick={handleGenerate}
          style={{ flex: 1, fontWeight: 'bold' }}
        >
          {loading ? 'Generating…' : result ? 'Regenerate' : 'Generate'}
        </button>
      </div>

      {result && (
        <>
          <div className="form-group">
            <label className="form-label">AI draft (edit before inserting)</label>
            <textarea
              rows={10}
              className="form-control"
              value={result}
              onChange={(e) => setResult(e.target.value)}
              style={{ fontSize: '0.9rem' }}
            />
          </div>
          <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
            <button
              type="button"
              className="btn btn-success"
              onClick={handleInsert}
              style={{ flex: 1, fontWeight: 'bold' }}
            >
              Insert into form
            </button>
          </div>
          <div style={{ fontSize: '0.75rem', color: '#a0aec0', marginTop: '8px' }}>
            Generated by {model || 'AI'} · AI draft — review before saving.
          </div>
        </>
      )}
    </Modal>
  );
};

export default AiAssistModal;
