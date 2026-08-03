import { apiClient } from './client';
import type { BaselineEvalReport } from '../types/api';

export const runBaselineEval = async (force = false): Promise<BaselineEvalReport> =>
  (await apiClient.post<BaselineEvalReport>(`/api/eval/baseline?force=${force}`)).data;

export const runRetrievalBackedEval = async (force = false): Promise<BaselineEvalReport> =>
  (await apiClient.post<BaselineEvalReport>(`/api/eval/retrieval-backed?force=${force}`)).data;
