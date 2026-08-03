import { useState } from 'react';
import type { ClaimResponse, ClaimVerdictResponse } from '../types/api';
import { VerdictBadge } from './VerdictBadge';
import { EvidenceList } from './EvidenceList';

export function ClaimCard({ claim, verdict }: { claim: ClaimResponse; verdict?: ClaimVerdictResponse }) {
  const [expanded, setExpanded] = useState(false);

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4">
      <div className="flex items-start justify-between gap-3">
        <p className="text-sm text-slate-800">{claim.claimText}</p>
        {verdict && <VerdictBadge verdict={verdict.verdict} />}
      </div>
      <div className="mt-2 flex flex-wrap items-center gap-3 text-xs text-slate-400">
        <span className="rounded bg-slate-100 px-1.5 py-0.5">{claim.source}</span>
        <span>
          {claim.startS.toFixed(1)}s – {claim.endS.toFixed(1)}s
        </span>
        {verdict && <span>Confidence: {(verdict.confidence * 100).toFixed(0)}%</span>}
      </div>
      {verdict?.reasoning && <p className="mt-2 text-xs text-slate-500">{verdict.reasoning}</p>}
      <button
        onClick={() => setExpanded((v) => !v)}
        className="mt-3 text-xs font-medium text-blue-600 hover:underline"
      >
        {expanded ? 'Hide evidence' : 'Show evidence'}
      </button>
      <EvidenceList claimId={claim.id} enabled={expanded} />
    </div>
  );
}
