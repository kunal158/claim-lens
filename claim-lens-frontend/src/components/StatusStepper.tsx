import { REEL_STATUS_SEQUENCE, statusLabel, stepIndex } from '../lib/reelStatus';
import type { ReelStatus } from '../types/api';

export function StatusStepper({ status }: { status: ReelStatus }) {
  if (status === 'FAILED') {
    return (
      <div className="rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
        Processing failed. There is no automatic retry — the reel's status
        needs to be reset manually before it can be reprocessed.
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
