import React from 'react';

export const SkeletonLine = ({ width = '100%', height = 12 }) => (
  <div className="skel-line" style={{ width, height }} aria-hidden="true" />
);

export const SkeletonTable = ({ rows = 5, cols = 4 }) => (
  <div className="skel-table" role="status" aria-label="Loading records">
    {Array.from({ length: rows }).map((_, r) => (
      <div key={r} className="skel-row">
        {Array.from({ length: cols }).map((__, c) => (
          <div key={c} className="skel-cell"><SkeletonLine /></div>
        ))}
      </div>
    ))}
    <span className="sr-only">Loading records…</span>
  </div>
);

export const SkeletonCards = ({ count = 3 }) => (
  <div className="skel-cards" role="status" aria-label="Loading">
    {Array.from({ length: count }).map((_, i) => (
      <div key={i} className="skel-card">
        <SkeletonLine width="60%" height={16} />
        <SkeletonLine width="90%" />
        <SkeletonLine width="80%" />
      </div>
    ))}
    <span className="sr-only">Loading…</span>
  </div>
);

export default SkeletonTable;
