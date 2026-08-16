import { Link, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { getClaims, getReel, getVerdictSummary, processReel } from '../api/reels';
import { StatusStepper } from '../components/StatusStepper';
import { TrustScoreGauge } from '../components/TrustScoreGauge';
import { ClaimCard } from '../components/ClaimCard';
import { isTerminal } from '../lib/reelStatus';

export function ReelDetailPage() {
  const { id } = useParams();
  const reelId = Number(id);
  const queryClient = useQueryClient();

  const reelQuery = useQuery({
    queryKey: ['reel', reelId],
    queryFn: () => getReel(reelId),
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      return status && isTerminal(status) ? false : 3000;
    },
  });

  const retryMutation = useMutation({
    mutationFn: () => processReel(reelId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reel', reelId] });
      queryClient.invalidateQueries({ queryKey: ['claims', reelId] });
      queryClient.invalidateQueries({ queryKey: ['verdictSummary', reelId] });
    },
  });

  const reel = reelQuery.data;
  const isSynthesized = reel?.status === 'VERDICTS_SYNTHESIZED';

  const claimsQuery = useQuery({
    queryKey: ['claims', reelId],
    queryFn: () => getClaims(reelId),
    enabled: isSynthesized,
  });

  const verdictQuery = useQuery({
    queryKey: ['verdictSummary', reelId],
    queryFn: () => getVerdictSummary(reelId, false),
    enabled: isSynthesized,
  });

  if (reelQuery.isLoading) return <div className="p-8 text-sm text-slate-400">Loading…</div>;
  if (reelQuery.error || !reel) return <div className="p-8 text-sm text-red-500">Reel not found.</div>;

  const verdictsByClaimId = new Map((verdictQuery.data?.verdicts ?? []).map((v) => [v.claimId, v]));

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <Link to="/" className="text-base font-bold text-blue-600 hover:underline">
        ← Back to dashboard
      </Link>
      <h1 className="mb-1 mt-2 text-xl font-semibold text-slate-900">Reel #{reel.id}</h1>
      {reel.sourceUrl ? (
        <a
          href={reel.sourceUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="mb-6 block truncate text-sm text-blue-600 hover:underline"
        >
          {reel.sourceUrl}
        </a>
      ) : (
        <p className="mb-6 truncate text-sm text-slate-500">{reel.sourceFilePath}</p>
      )}

      <div className="mb-6 rounded-lg border border-slate-200 bg-white p-4">
        <StatusStepper
          status={reel.status}
          onRetry={() => retryMutation.mutate()}
          isRetrying={retryMutation.isPending}
        />
      </div>

      {isSynthesized && (
        <>
          <div className="mb-6 rounded-lg border border-slate-200 bg-white p-4">
            <TrustScoreGauge score={reel.trustScore} />
            {reel.summary && <p className="mt-4 text-sm text-slate-600">{reel.summary}</p>}
          </div>

          <h2 className="mb-3 text-sm font-medium text-slate-500">Claims</h2>
          {claimsQuery.isLoading && <p className="text-sm text-slate-400">Loading claims…</p>}
          <div className="space-y-3">
            {claimsQuery.data?.map((claim) => (
              <ClaimCard key={claim.id} claim={claim} verdict={verdictsByClaimId.get(claim.id)} />
            ))}
          </div>
        </>
      )}
    </div>
  );
}
