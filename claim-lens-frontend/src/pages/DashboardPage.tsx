import { useQuery } from '@tanstack/react-query';
import { listReels } from '../api/reels';
import { ReelRow } from '../components/ReelRow';
import { AddReelForm } from '../components/AddReelForm';

export function DashboardPage() {
  const { data, isLoading, error } = useQuery({ queryKey: ['reels'], queryFn: listReels });

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <AddReelForm />

      <h2 className="mb-3 mt-8 text-sm font-medium text-slate-500">Reels</h2>
      {isLoading && <p className="text-sm text-slate-400">Loading…</p>}
      {error && <p className="text-sm text-red-500">Failed to load reels.</p>}
      {data && data.length === 0 && <p className="text-sm text-slate-400">No reels yet — add one above.</p>}
      <div className="space-y-2">
        {data?.map((reel) => (
          <ReelRow key={reel.id} reel={reel} />
        ))}
      </div>
    </div>
  );
}
