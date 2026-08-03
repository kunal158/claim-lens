import { useMutation } from '@tanstack/react-query';
import { runBaselineEval, runRetrievalBackedEval } from '../api/eval';
import { EvalReportCard } from '../components/EvalReportCard';

export function EvalPage() {
  const baseline = useMutation({ mutationFn: () => runBaselineEval(false) });
  const retrievalBacked = useMutation({ mutationFn: () => runRetrievalBackedEval(false) });

  return (
    <div className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="mb-2 text-xl font-semibold text-slate-900">Eval comparison</h1>
      <p className="mb-6 text-sm text-slate-500">
        Compares no-retrieval baseline accuracy against retrieval-backed accuracy on the same
        hand-labeled claim set.
      </p>

      <div className="mb-6 flex gap-3">
        <button
          onClick={() => baseline.mutate()}
          disabled={baseline.isPending}
          className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
        >
          {baseline.isPending ? 'Running…' : 'Run baseline'}
        </button>
        <button
          onClick={() => retrievalBacked.mutate()}
          disabled={retrievalBacked.isPending}
          className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
        >
          {retrievalBacked.isPending ? 'Running…' : 'Run retrieval-backed'}
        </button>
      </div>

      {(baseline.error || retrievalBacked.error) && (
        <p className="mb-4 text-sm text-red-500">One of the eval runs failed — check the backend logs.</p>
      )}

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        {baseline.data && <EvalReportCard title="No-retrieval baseline" report={baseline.data} />}
        {retrievalBacked.data && <EvalReportCard title="Retrieval-backed" report={retrievalBacked.data} />}
      </div>

      {baseline.data && retrievalBacked.data && retrievalBacked.data.accuracyPct < baseline.data.accuracyPct && (
        <div className="mt-6 rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800">
          Retrieval-backed accuracy ({retrievalBacked.data.accuracyPct.toFixed(1)}%) is lower than the
          no-retrieval baseline ({baseline.data.accuracyPct.toFixed(1)}%) — giving the model evidence made
          it more confidently wrong on this claim set, rather than less.
        </div>
      )}
    </div>
  );
}
