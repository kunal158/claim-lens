import type { BaselineEvalReport } from '../types/api';
import { VerdictBadge } from './VerdictBadge';

export function EvalReportCard({ title, report }: { title: string; report: BaselineEvalReport }) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4">
      <h3 className="text-sm font-semibold text-slate-700">{title}</h3>
      <div className="mt-3 flex items-baseline gap-2">
        <span className="text-3xl font-bold text-slate-900">{report.accuracyPct.toFixed(1)}%</span>
        <span className="text-sm text-slate-400">
          ({report.correct}/{report.totalClaims} correct)
        </span>
      </div>

      {report.mismatches.length > 0 && (
        <div className="mt-4">
          <h4 className="text-xs font-medium text-slate-500">Mismatches</h4>
          <ul className="mt-2 space-y-2">
            {report.mismatches.map((m) => (
              <li key={m.claimId} className="rounded-md bg-slate-50 p-2 text-xs">
                <p className="text-slate-700">{m.claimText}</p>
                <div className="mt-1 flex flex-wrap items-center gap-2">
                  <span className="text-slate-400">Human:</span>
                  <VerdictBadge verdict={m.humanLabel} />
                  <span className="text-slate-400">Model:</span>
                  <VerdictBadge verdict={m.modelVerdict} />
                </div>
                {m.modelReasoning && <p className="mt-1 text-slate-500">{m.modelReasoning}</p>}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
