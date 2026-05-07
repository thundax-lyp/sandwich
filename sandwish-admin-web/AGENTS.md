# Admin Web Agent

This file extends the repository root `AGENTS.md` for work under `sandwish-admin-web/`.

## Scope

- React + TypeScript + Vite admin console.
- UI library: Ant Design.
- Routing: React Router.
- Server state: TanStack Query.
- Tests: Vitest + Testing Library.

## Core Conventions

- Keep source file names in kebab-case, for example `current-user-service.ts`.
- Prefer arrow functions for most frontend methods.
- Prefer `export const XxxPage = () => {}` for React components.

## File And Code Style

- Organize pages as `src/pages/<module>/<domain>/<domain-page>.tsx`.
- Put domain-specific page components under `src/pages/<module>/<domain>/components/`.
- Put shared frontend components under `src/components/`.
- Keep React components as named arrow-function exports.
- Keep shared API access in `src/api/` or `src/service/`.
- Keep page-specific API access next to the page as `src/pages/<module>/<domain>/<domain>-service.ts`.
- Do not call `fetch` directly from pages or layouts.
- Keep auth token and permission persistence in `src/auth/`.
- Use TypeScript interfaces for request and response shapes when the shape is shared or crosses file boundaries.

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
