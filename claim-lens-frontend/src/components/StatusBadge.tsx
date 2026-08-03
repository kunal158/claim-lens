import type { ReelStatus } from '../types/api';
import { statusLabel } from '../lib/reelStatus';

const COLORS: Record<ReelStatus, string> = {
  PENDING: 'bg-slate-100 text-slate-700',
  TRANSCRIBING: 'bg-blue-100 text-blue-700',
  TRANSCRIBED: 'bg-blue-100 text-blue-700',
  EXTRACTING_CLAIMS: 'bg-blue-100 text-blue-700',
  CLAIMS_EXTRACTED: 'bg-blue-100 text-blue-700',
  RETRIEVING_EVIDENCE: 'bg-blue-100 text-blue-700',
  EVIDENCE_RETRIEVED: 'bg-blue-100 text-blue-700',
  SYNTHESIZING_VERDICTS: 'bg-blue-100 text-blue-700',
  VERDICTS_SYNTHESIZED: 'bg-green-100 text-green-700',
  FAILED: 'bg-red-100 text-red-700',
};

export function StatusBadge({ status }: { status: ReelStatus }) {
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${COLORS[status]}`}>
      {statusLabel(status)}
    </span>
  );
}
