export type ReelStatus =
  | 'PENDING'
  | 'TRANSCRIBING'
  | 'TRANSCRIBED'
  | 'EXTRACTING_CLAIMS'
  | 'CLAIMS_EXTRACTED'
  | 'RETRIEVING_EVIDENCE'
  | 'EVIDENCE_RETRIEVED'
  | 'SYNTHESIZING_VERDICTS'
  | 'VERDICTS_SYNTHESIZED'
  | 'FAILED';

export type Verdict = 'TRUE' | 'FALSE' | 'UNVERIFIABLE';

export type ClaimSource = 'SPOKEN' | 'ONSCREEN' | 'BOTH';

export interface ReelResponse {
  id: number;
  status: ReelStatus;
  sourceFilePath: string | null;
  sourceUrl: string | null;
  transcriptSegments: string | null;
  onscreenTextSegments: string | null;
  trustScore: number | null;
  summary: string | null;
  createdAt: string;
}

export interface ClaimResponse {
  id: number;
  reelId: number;
  claimText: string;
  searchQuery: string | null;
  source: ClaimSource;
  startS: number;
  endS: number;
  createdAt: string;
}

export interface EvidenceResponse {
  id: number;
  claimId: number;
  url: string | null;
  title: string | null;
  chunkText: string | null;
  tavilyScore: number | null;
  similarityScore: number;
  createdAt: string;
}

export interface ClaimVerdictResponse {
  id: number;
  claimId: number;
  verdict: Verdict;
  confidence: number;
  reasoning: string | null;
  createdAt: string;
}

export interface VerdictSummaryResponse {
  reelId: number;
  trustScore: number | null;
  summary: string | null;
  verdicts: ClaimVerdictResponse[];
}

export interface MismatchDetail {
  claimId: number;
  claimText: string;
  humanLabel: Verdict;
  modelVerdict: Verdict;
  modelReasoning: string | null;
}

export interface BaselineEvalReport {
  totalClaims: number;
  correct: number;
  accuracyPct: number;
  mismatches: MismatchDetail[];
}
