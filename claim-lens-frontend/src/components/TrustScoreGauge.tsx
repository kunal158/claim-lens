export function TrustScoreGauge({ score }: { score: number | null }) {
  if (score == null) {
    return <div className="text-sm text-slate-400">No trust score (no TRUE/FALSE verdicts)</div>;
  }

  const radius = 36;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference * (1 - score / 100);
  const color = score >= 70 ? '#22c55e' : score >= 40 ? '#f59e0b' : '#ef4444';

  return (
    <div className="flex items-center gap-3">
      <svg width="88" height="88" viewBox="0 0 88 88">
        <circle cx="44" cy="44" r={radius} fill="none" stroke="#e2e8f0" strokeWidth="8" />
        <circle
          cx="44"
          cy="44"
          r={radius}
          fill="none"
          stroke={color}
          strokeWidth="8"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          strokeLinecap="round"
          transform="rotate(-90 44 44)"
        />
        <text x="44" y="50" textAnchor="middle" fontSize="20" fontWeight="600" fill="#0f172a">
          {score}
        </text>
      </svg>
      <div>
        <div className="text-sm font-medium text-slate-700">Trust score</div>
        <div className="text-xs text-slate-500">out of 100</div>
      </div>
    </div>
  );
}
