import { useQuery } from '@tanstack/react-query';
import { getEvidenceForClaim } from '../api/reels';

export function EvidenceList({ claimId, enabled }: { claimId: number; enabled: boolean }) {
  const { data, isLoading, error } = useQuery({
    queryKey: ['evidence', claimId],
    queryFn: () => getEvidenceForClaim(claimId),
    enabled,
  });

  if (!enabled) return null;
  if (isLoading) return <div className="py-2 text-xs text-slate-400">Loading evidence…</div>;
  if (error) return <div className="py-2 text-xs text-red-500">Failed to load evidence.</div>;
  if (!data || data.length === 0) {
    return <div className="py-2 text-xs text-slate-400">No evidence retrieved for this claim.</div>;
  }

  return (
    <ul className="mt-2 space-y-2 border-t border-slate-100 pt-2">
      {data.map((e) => (
        <li key={e.id} className="rounded-md bg-slate-50 p-2 text-xs">
          {e.url ? (
            <a href={e.url} target="_blank" rel="noreferrer" className="font-medium text-blue-600 hover:underline">
              {e.title ?? e.url}
            </a>
          ) : (
            <span className="font-medium text-slate-700">{e.title ?? 'Untitled source'}</span>
          )}
          {e.chunkText && <p className="mt-1 text-slate-600">{e.chunkText}</p>}
          <div className="mt-1 flex gap-3 text-[10px] text-slate-400">
            {e.tavilyScore != null && <span>Tavily score: {e.tavilyScore.toFixed(2)}</span>}
            <span>Similarity: {e.similarityScore.toFixed(2)}</span>
          </div>
        </li>
      ))}
    </ul>
  );
}
