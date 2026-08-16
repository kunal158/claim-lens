import type { ReelStatus } from '../types/api';

export const REEL_STATUS_SEQUENCE: ReelStatus[] = [
  'DOWNLOADING',
  'PENDING',
  'TRANSCRIBING',
  'TRANSCRIBED',
  'EXTRACTING_CLAIMS',
  'CLAIMS_EXTRACTED',
  'RETRIEVING_EVIDENCE',
  'EVIDENCE_RETRIEVED',
  'SYNTHESIZING_VERDICTS',
  'VERDICTS_SYNTHESIZED',
];

export const TERMINAL_STATUSES: ReelStatus[] = ['VERDICTS_SYNTHESIZED', 'FAILED'];

export function isTerminal(status: ReelStatus): boolean {
  return TERMINAL_STATUSES.includes(status);
}

export function stepIndex(status: ReelStatus): number {
  return REEL_STATUS_SEQUENCE.indexOf(status);
}

const STATUS_LABELS: Record<ReelStatus, string> = {
  DOWNLOADING: 'Downloading',
  PENDING: 'Pending',
  TRANSCRIBING: 'Transcribing',
  TRANSCRIBED: 'Transcribed',
  EXTRACTING_CLAIMS: 'Extracting claims',
  CLAIMS_EXTRACTED: 'Claims extracted',
  RETRIEVING_EVIDENCE: 'Retrieving evidence',
  EVIDENCE_RETRIEVED: 'Evidence retrieved',
  SYNTHESIZING_VERDICTS: 'Synthesizing verdicts',
  VERDICTS_SYNTHESIZED: 'Verdicts synthesized',
  FAILED: 'Failed',
};

export function statusLabel(status: ReelStatus): string {
  return STATUS_LABELS[status];
}
