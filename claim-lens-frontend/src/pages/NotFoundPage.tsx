import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <div className="mx-auto max-w-3xl px-4 py-16 text-center">
      <p className="text-slate-500">Page not found.</p>
      <Link to="/" className="text-sm text-blue-600 hover:underline">
        Back to dashboard
      </Link>
    </div>
  );
}
