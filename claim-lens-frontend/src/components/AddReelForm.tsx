import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { createReelFromUrl, processReel, uploadReel } from '../api/reels';
import type { ReelResponse } from '../types/api';

function extractError(err: unknown): string {
  if (axios.isAxiosError(err)) {
    const data = err.response?.data as { error?: string; status?: number } | undefined;
    if (data?.error) return `${data.error} (${data.status ?? err.response?.status})`;
    return err.message;
  }
  return 'Something went wrong.';
}

export function AddReelForm() {
  const [tab, setTab] = useState<'url' | 'file'>('url');
  const [url, setUrl] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const afterCreate = async (reel: ReelResponse) => {
    queryClient.invalidateQueries({ queryKey: ['reels'] });
    try {
      await processReel(reel.id);
    } catch (err) {
      // 409 means the reel is already past PENDING (e.g. a dedup hit on an
      // in-progress/completed reel) — safe to ignore, nothing to (re)start.
      if (!axios.isAxiosError(err) || err.response?.status !== 409) throw err;
    }
    navigate(`/reels/${reel.id}`);
  };

  const urlMutation = useMutation({
    mutationFn: () => createReelFromUrl(url),
    onSuccess: afterCreate,
    onError: (err) => setErrorMessage(extractError(err)),
  });

  const fileMutation = useMutation({
    mutationFn: () => uploadReel(file as File),
    onSuccess: afterCreate,
    onError: (err) => setErrorMessage(extractError(err)),
  });

  const isPending = urlMutation.isPending || fileMutation.isPending;

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4">
      <div className="mb-3 flex gap-2 text-sm">
        <button
          className={`rounded px-3 py-1 ${tab === 'url' ? 'bg-blue-600 text-white' : 'bg-slate-100 text-slate-600'}`}
          onClick={() => setTab('url')}
        >
          Paste URL
        </button>
        <button
          className={`rounded px-3 py-1 ${tab === 'file' ? 'bg-blue-600 text-white' : 'bg-slate-100 text-slate-600'}`}
          onClick={() => setTab('file')}
        >
          Upload file
        </button>
      </div>

      {tab === 'url' ? (
        <form
          onSubmit={(e) => {
            e.preventDefault();
            setErrorMessage(null);
            urlMutation.mutate();
          }}
          className="flex gap-2"
        >
          <input
            type="text"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="https://www.instagram.com/reel/..."
            className="flex-1 rounded border border-slate-300 px-3 py-2 text-sm"
            required
          />
          <button
            type="submit"
            disabled={isPending}
            className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
          >
            {urlMutation.isPending ? 'Adding…' : 'Add reel'}
          </button>
        </form>
      ) : (
        <form
          onSubmit={(e) => {
            e.preventDefault();
            setErrorMessage(null);
            if (file) fileMutation.mutate();
          }}
          className="flex flex-col gap-3"
        >
          <label
            htmlFor="reel-file-input"
            onDragOver={(e) => {
              e.preventDefault();
              setIsDragging(true);
            }}
            onDragLeave={() => setIsDragging(false)}
            onDrop={(e) => {
              e.preventDefault();
              setIsDragging(false);
              const dropped = e.dataTransfer.files?.[0];
              if (dropped) setFile(dropped);
            }}
            className={`flex cursor-pointer flex-col items-center justify-center gap-2 rounded-lg border-2 border-dashed px-4 py-8 text-center transition-colors ${
              isDragging
                ? 'border-blue-400 bg-blue-50'
                : file
                  ? 'border-green-300 bg-green-50'
                  : 'border-slate-300 bg-slate-50 hover:border-blue-300 hover:bg-blue-50'
            }`}
          >
            <svg
              className={`h-8 w-8 ${isDragging || file ? 'text-blue-500' : 'text-slate-400'}`}
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth={1.5}
            >
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 16.5V9m0 0-3 3m3-3 3 3M6.75 19.5a4.5 4.5 0 0 1-1.41-8.775 5.25 5.25 0 0 1 10.233-2.33 3.75 3.75 0 0 1 4.157 3.99A4.5 4.5 0 0 1 17.25 19.5H6.75Z" />
            </svg>
            {file ? (
              <p className="text-sm font-medium text-slate-700">{file.name}</p>
            ) : (
              <>
                <p className="text-sm font-medium text-slate-600">Drag and drop a video file here</p>
                <p className="text-xs text-slate-400">or click to browse</p>
              </>
            )}
            <input
              id="reel-file-input"
              type="file"
              accept="video/*"
              onChange={(e) => setFile(e.target.files?.[0] ?? null)}
              className="sr-only"
            />
          </label>
          <button
            type="submit"
            disabled={isPending || !file}
            className="self-end rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
          >
            {fileMutation.isPending ? 'Uploading…' : 'Upload'}
          </button>
        </form>
      )}

      {errorMessage && <p className="mt-2 text-xs text-red-600">{errorMessage}</p>}
    </div>
  );
}
