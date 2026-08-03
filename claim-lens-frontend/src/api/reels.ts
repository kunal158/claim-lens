import { apiClient } from './client';
import type { ClaimResponse, EvidenceResponse, ReelResponse, VerdictSummaryResponse } from '../types/api';

export const listReels = async (): Promise<ReelResponse[]> =>
  (await apiClient.get<ReelResponse[]>('/api/reels')).data;

export const getReel = async (id: number): Promise<ReelResponse> =>
  (await apiClient.get<ReelResponse>(`/api/reels/${id}`)).data;

export const createReelFromUrl = async (url: string): Promise<ReelResponse> =>
  (await apiClient.post<ReelResponse>('/api/reels/url', { url })).data;

export const uploadReel = async (file: File): Promise<ReelResponse> => {
  const form = new FormData();
  form.append('file', file);
  return (await apiClient.post<ReelResponse>('/api/reels', form)).data;
};

export const processReel = async (id: number): Promise<ReelResponse> =>
  (await apiClient.post<ReelResponse>(`/api/reels/${id}/process`)).data;

export const getClaims = async (reelId: number): Promise<ClaimResponse[]> =>
  (await apiClient.get<ClaimResponse[]>(`/api/reels/${reelId}/claims`)).data;

export const getEvidenceForClaim = async (claimId: number): Promise<EvidenceResponse[]> =>
  (await apiClient.get<EvidenceResponse[]>(`/api/claims/${claimId}/evidence`)).data;

export const getVerdictSummary = async (reelId: number, force = false): Promise<VerdictSummaryResponse> =>
  (await apiClient.post<VerdictSummaryResponse>(`/api/reels/${reelId}/synthesize-verdicts?force=${force}`)).data;
