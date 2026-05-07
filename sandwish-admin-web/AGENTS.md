# Admin Web Agent

This file extends the repository root `AGENTS.md` for work under `sandwish-admin-web/`.

## Scope

- React + TypeScript + Vite admin console.
- UI library: Ant Design.
- Routing: React Router.
- Server state: TanStack Query.
- Tests: Vitest + Testing Library.

## Required Docs

- For admin-web naming, placement, service ownership, frontend layering, and default forbidden directories, read:
    - `docs/00-governance/ADMIN-WEB-NAMING-AND-PLACEMENT-RULES.md`

## Auth And Permission Rules

- Login session setup must bind token and current-user permissions together.
- After successful `/auth/login`, immediately load `/sys/current-user/perms` before navigating into protected pages.
- If permission loading fails during login setup, clear the access token and permissions.
- Clearing the access token must also clear stored permissions.
- Use server-loaded menus for navigation display. Use stored permissions for finer UI capability checks.
- Do not duplicate permission strings in page logic when an existing helper can express the check.

## UI Rules

- The first protected screen should be the admin working shell, not a marketing page.
- Follow the existing Ant Design layout and component style.
- Keep operational pages compact and scannable.
- Use icons for clear toolbar and action affordances when Ant Design provides a suitable icon.
- Avoid adding decorative-only layout sections.

## Testing And Verification

- For auth, permission, routing, request hook, or layout behavior changes, update `src/app.test.tsx` or add a focused test.
- Before committing frontend changes, run the smallest relevant checks:
    - `npm run format:check`
    - `npm run lint`
    - `npm test`
    - `npm run build` when TypeScript, routing, bundle entry, or shared auth/API code changed
- Vite chunk-size warnings are acceptable unless the change intentionally touches bundling or lazy loading.

## Dev Server

- Use `npm run dev -- --port <port>` when a specific port is needed.
- If a dev server is already running for collaboration, leave it running unless the user asks to stop it.
