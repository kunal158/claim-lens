import { REEL_STATUS_SEQUENCE, statusLabel, stepIndex } from '../lib/reelStatus';
import type { ReelStatus } from '../types/api';

export function StatusStepper({
  status,
  onRetry,
  isRetrying,
}: {
  status: ReelStatus;
  onRetry?: () => void;
  isRetrying?: boolean;
}) {
  if (status === 'FAILED') {
    return (
      <div className="flex items-center justify-between gap-4 rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
        <span>
          Processing failed. Retrying will discard any claims/evidence/verdicts
          from this attempt and restart the pipeline from scratch.
        </span>
        {onRetry && (
          <button
            type="button"
            onClick={onRetry}
            disabled={isRetrying}
            className="shrink-0 rounded-md bg-red-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-red-700 disabled:opacity-50"
          >
            {isRetrying ? 'Retrying…' : 'Retry'}
          </button>
        )}
      </div>
    );
  }

  const current = stepIndex(status);

  return (
    <div className="flex items-start">
      {REEL_STATUS_SEQUENCE.map((step, i) => {
        const done = i < current;
        const active = i === current;
        return (
          <div key={step} className="flex flex-1 items-start last:flex-none">
            <div className="flex flex-col items-center gap-1">
              <div
                className={`flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-[10px] font-semibold ${
                  done ? 'bg-green-500 text-white' : active ? 'bg-blue-500 text-white' : 'bg-slate-200 text-slate-500'
                }`}
              >
                {i + 1}
              </div>
              <span className="w-20 text-center text-[10px] leading-tight text-slate-500">{statusLabel(step)}</span>
            </div>
            {i < REEL_STATUS_SEQUENCE.length - 1 && (
              <div className={`mx-1 mt-3 h-0.5 flex-1 ${done ? 'bg-green-500' : 'bg-slate-200'}`} />
            )}
          </div>
        );
      })}
    </div>
  );
}
