import React, { useState, useRef, useEffect } from 'react';

/**
 * Lightweight typeahead select. `options` are { value, label } objects.
 * Filters by label (case-insensitive, substring), keyboard friendly
 * (Enter/ArrowUp/ArrowDown/Escape), and closes on outside click.
 */
const SearchableSelect = ({ options = [], value, onChange, placeholder = 'Search…', disabled = false }) => {
    const [open, setOpen] = useState(false);
    const [query, setQuery] = useState('');
    const [active, setActive] = useState(0);
    const wrapRef = useRef(null);
    const inputRef = useRef(null);

    const selected = options.find(o => o.value === value);

    useEffect(() => {
        const onDocClick = (e) => {
            if (wrapRef.current && !wrapRef.current.contains(e.target)) setOpen(false);
        };
        document.addEventListener('mousedown', onDocClick);
        return () => document.removeEventListener('mousedown', onDocClick);
    }, []);

    useEffect(() => {
        if (!open) setQuery('');
    }, [open]);

    const filtered = query
        ? options.filter(o => o.label.toLowerCase().includes(query.toLowerCase()))
        : options;

    const pick = (opt) => {
        if (!opt) return;
        onChange?.(opt.value);
        setOpen(false);
    };

    const handleKeyDown = (e) => {
        if (!open && e.key !== 'Enter') return;
        if (e.key === 'ArrowDown') { e.preventDefault(); setActive(a => Math.min(a + 1, filtered.length - 1)); }
        else if (e.key === 'ArrowUp') { e.preventDefault(); setActive(a => Math.max(a - 1, 0)); }
        else if (e.key === 'Enter') {
            e.preventDefault();
            if (open) pick(filtered[active]);
            else setOpen(true);
        }
        else if (e.key === 'Escape') { setOpen(false); }
        else if (e.key === 'Tab') { setOpen(false); }
    };

    return (
        <div ref={wrapRef} style={{ position: 'relative', width: '100%' }}>
            <input
                ref={inputRef}
                type="text"
                className="form-control"
                placeholder={placeholder}
                disabled={disabled}
                value={open ? query : (selected?.label || '')}
                onFocus={() => setOpen(true)}
                onChange={e => { setQuery(e.target.value); setActive(0); if (!open) setOpen(true); }}
                onKeyDown={handleKeyDown}
                role="combobox"
                aria-expanded={open}
                aria-controls={open ? 'searchable-select-list' : undefined}
                aria-autocomplete="list"
                autoComplete="off"
            />
            {open && (
                <div
                    id="searchable-select-list"
                    role="listbox"
                    style={{
                        position: 'absolute', top: 'calc(100% + 4px)', left: 0, right: 0,
                        background: 'white', border: '1px solid #e2e8f0', borderRadius: '8px',
                        boxShadow: '0 10px 20px -4px rgba(0,0,0,0.15)', maxHeight: '260px',
                        overflowY: 'auto', zIndex: 20
                    }}
                >
                    {filtered.length === 0 && (
                        <div style={{ padding: '10px 12px', color: '#94a3b8', fontSize: '0.85rem' }}>
                            No matches.
                        </div>
                    )}
                    {filtered.map((opt, idx) => (
                        <button
                            key={opt.value}
                            type="button"
                            onClick={() => pick(opt)}
                            onMouseEnter={() => setActive(idx)}
                            style={{
                                display: 'block', width: '100%', textAlign: 'left', background: idx === active ? '#eef2ff' : 'white',
                                border: 'none', padding: '8px 12px', fontSize: '0.9rem', color: '#1e293b', cursor: 'pointer'
                            }}
                        >
                            {opt.label}
                            {opt.value === value && <span style={{ color: '#818cf8', float: 'right' }}>✓</span>}
                        </button>
                    ))}
                </div>
            )}
        </div>
    );
};

export default SearchableSelect;