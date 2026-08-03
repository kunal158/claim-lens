import type { Verdict } from '../types/api';

const COLORS: Record<Verdict, string> = {
  TRUE: 'bg-green-100 text-green-700',
  FALSE: 'bg-red-100 text-red-700',
  UNVERIFIABLE: 'bg-slate-100 text-slate-600',
};

export function VerdictBadge({ verdict }: { verdict: Verdict }) {
  return (
    <span className={`inline-flex shrink-0 items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${COLORS[verdict]}`}>
      {verdict}
    </span>
  );
}
