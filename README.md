# claim-lens

A RAG-based fact-checker for Instagram reels. Extracts factual claims from a
reel's transcript/on-screen text, retrieves web evidence, and synthesizes a
grounded true/false/unverifiable verdict per claim with an overall trust
score.

This repo contains both the backend and frontend:

- `claimlens-api/` — Java/Spring Boot backend
- `claim-lens-frontend/` — React + Tailwind frontend

## Backend

- Java, Spring Boot, Spring Data JPA, PostgreSQL, Flyway
- Gemini (`gemini-2.5-flash` + `gemini-embedding-001`) for extraction, embeddings,
  and verdict synthesis
- Tavily for web search evidence retrieval
- `yt-dlp` for reel download from URL

```
cd claimlens-api
set GEMINI_API_KEY=...
set TAVILY_API_KEY=...
./mvnw spring-boot:run
```

Requires a local PostgreSQL instance (`claimlens` database, migrations run via
Flyway on startup).

## Frontend

- React, TypeScript, Vite
- Tailwind CSS v4
- TanStack Query for data fetching/polling
- react-router-dom for routing

```
cd claim-lens-frontend
npm install
npm run dev
```

Expects the backend running at `http://localhost:8080` (configurable via
`VITE_API_BASE_URL` in `.env.local`).
