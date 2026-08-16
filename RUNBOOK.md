# Runbook: Render + Neon setup for ClaimLens (prod only)

One-time setup, done through Neon's and Render's dashboards (no Docker Compose,
no VM, no SSH keys, no credit card). After this, every `git push` to `main` on
the `claim-lens` repo redeploys automatically — Render watches the branch
directly.

Single environment (prod only) — 3 pieces: 1 Neon Postgres database, 1 Render
Docker web service (backend), 1 Render Static Site (frontend).

**Note:** `claimlens-api` and `claim-lens-frontend` live in the same GitHub
repo (`kunal158/claim-lens`), not two separate repos — both Render services
below connect to that one repo, distinguished by the **Root Directory**
field.

## 1. Create the Neon database

1. Go to https://neon.tech → sign up with GitHub. No card required for the free tier.
2. Create a project, then a database named `claimlens` inside it (Neon creates one by default — rename or reuse it).
3. From the project dashboard, copy the connection string. It looks like:
   `postgresql://<user>:<password>@<host>/<database>?sslmode=require`
4. Split it into three env var values for later:
   - `DB_URL` = `jdbc:postgresql://<host>/<database>?sslmode=require` (note the `jdbc:` prefix — Neon's own string doesn't have it, JDBC needs it)
   - `DB_USER` = `<user>`
   - `DB_PASSWORD` = `<password>`

Neon's free tier auto-suspends compute when idle and cold-starts on the next
query (similar to Render's free-tier sleep behavior below) — fine for a
personal project, and unlike Render's own free Postgres, **it does not expire
after 90 days**.

## 2. Create the backend web service

1. Dashboard → **New +** → **Web Service**.
2. Connect the `claim-lens` GitHub repo (`kunal158/claim-lens`).
3. **Branch**: `main`.
4. **Root Directory**: `claimlens-api` (this is what tells Render to build only this subfolder — critical in a monorepo).
5. **Runtime**: Render auto-detects the `Dockerfile` inside `claimlens-api/` once the root directory is set — leave it on Docker.
6. **Instance type**: Free.
7. **Name**: e.g. `claimlens-api` (Render locks the service's URL to this name — pick it deliberately, since renaming later means updating `CLAIMLENS_CORS_ALLOWED_ORIGINS`/`VITE_API_BASE_URL` again).
8. Create the service. Don't worry about env vars yet — add them in step 4.

## 3. Create the frontend static site

1. Dashboard → **New +** → **Static Site**.
2. Connect the same `claim-lens` GitHub repo.
3. **Branch**: `main`.
4. **Root Directory**: `claim-lens-frontend`.
5. **Build command**: `npm run build`
6. **Publish directory**: `dist`
7. **Name**: e.g. `claimlens-frontend` — again, note the resulting URL.
8. Create the site. Env vars come next.

## 4. Wire everything together

Once both services exist, each has a fixed `https://<name>.onrender.com` URL
visible on its dashboard page. Now go back and set env vars (this is why
services were created first — the URLs didn't exist until now):

**On the backend service** (Dashboard → service → Environment):

| Key | Value |
|---|---|
| `DB_URL` | `jdbc:postgresql://<neon-host>/<database>?sslmode=require` |
| `DB_USER` | your Neon database user |
| `DB_PASSWORD` | your Neon database password |
| `GEMINI_API_KEY` | your Gemini API key from https://aistudio.google.com/apikey |
| `TAVILY_API_KEY` | your Tavily API key from https://tavily.com |
| `CLAIMLENS_CORS_ALLOWED_ORIGINS` | the frontend's URL, e.g. `https://claimlens-frontend.onrender.com` |

**On the frontend static site** (Dashboard → site → Environment):

| Key | Value |
|---|---|
| `VITE_API_BASE_URL` | the backend's URL, e.g. `https://claimlens-api.onrender.com` |

Saving env vars on Render triggers an automatic redeploy of that service — no
manual restart needed.

## 5. First verification

Open the frontend URL, paste a reel URL, and confirm it moves through the full
pipeline (Downloading → Pending → ... → Verdicts synthesized) exactly as it
does locally. If the backend was asleep, the first request after the frontend
loads may take 30-60 seconds before responding — that's expected, not a failure.

## Ongoing usage

From now on, deploying is just: `git push origin main`. Render picks it up
automatically on both repos — nothing to run here, nothing to touch on this
repo except this documentation.

## Free-tier behavior to expect

- **Backend (web service)**: sleeps after 15 minutes with no requests, then
  takes 30-60 seconds to wake on the next request. Fine for a personal
  project; upgrade to a paid instance ($7/mo) later if you want it always-on.
- **Frontend (static site)**: never sleeps, served from Render's CDN — free
  tier is effectively unlimited for this project's traffic.
- **No persistent disk** on free web services. ClaimLens downloads each reel's
  video to local disk transiently (`claimlens.upload-dir`) just to hand it to
  Gemini for transcription right after creation — no endpoint ever serves that
  file back out, so this is safe. The only real consequence: a restart mid-pipeline
  loses that reel's downloaded file and it lands in `FAILED`; just retry it (the
  reel-retry fix already makes this cheap) or re-add the URL.
- **Neon free tier**: compute auto-suspends when idle and cold-starts on the
  next query (similar wake delay to the backend above); ~0.5GB storage cap,
  comfortably enough for this project's data volume. Unlike Render's own free
  Postgres, it does not expire after 90 days.
