import { useNavigate } from 'react-router-dom';
import type { ReelResponse } from '../types/api';
import { StatusBadge } from './StatusBadge';

export function ReelRow({ reel }: { reel: ReelResponse }) {
  const navigate = useNavigate();
  const source = reel.sourceUrl ?? reel.sourceFilePath ?? 'Unknown source';
  return (
    <div
      onClick={() => navigate(`/reels/${reel.id}`)}
      className="flex cursor-pointer items-center justify-between gap-4 rounded-lg border border-slate-200 bg-white p-4 hover:border-blue-300 hover:shadow-sm"
    >
      <div className="min-w-0">
        <div className="text-xs text-slate-400">#{reel.id}</div>
        {reel.sourceUrl ? (
          <a
            href={reel.sourceUrl}
            target="_blank"
            rel="noopener noreferrer"
            onClick={(e) => e.stopPropagation()}
            className="block truncate text-sm text-blue-600 hover:underline"
          >
            {source}
          </a>
        ) : (
          <div className="truncate text-sm text-slate-700">{source}</div>
        )}
        <div className="text-xs text-slate-400">{new Date(reel.createdAt).toLocaleString()}</div>
      </div>
      <div className="flex shrink-0 items-center gap-3">
        {reel.trustScore != null && <span className="text-sm font-semibold text-slate-700">{reel.trustScore}</span>}
        <StatusBadge status={reel.status} />
      </div>
    </div>
  );
}
