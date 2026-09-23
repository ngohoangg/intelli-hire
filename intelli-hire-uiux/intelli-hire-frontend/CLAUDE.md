# IntelliHire Frontend — Claude Instructions

## Design System

Full spec: `src/design-system.md`
SCSS tokens: `src/styles/_variables.scss`
Animations: `src/styles/_animations.scss`
Mixins: `src/styles/_mixins.scss`

### Core Rules
- **Theme:** Neon Cyberpunk (Pure × Corporate)
- **Primary neon:** `--cyan` (#00F5FF) — CTA, focus, active states
- **Secondary neon:** `--magenta` (#FF00AA) — hover shifts, active tension
- `--accent` = alias for `--cyan` — both valid, prefer `--cyan` in new code
- **Backgrounds:** ultra-dark starting at `--bg-void: #030306` — never use light backgrounds
- **Neon rule:** every neon element MUST have a glow companion (`--glow-cyan`, `--glow-subtle`, etc.)
- **Cards:** use BOTH `border-radius: var(--radius-lg)` AND `clip-path` corner cut; neon glow on `::before` wrapper, NOT on the card itself (clip-path clips box-shadow)
- **Buttons:** outlined neon — `border: 2px solid var(--cyan)`, transparent bg; hover fills `rgba(cyan, 0.10)`; active → magenta shift
- **Animations:** `glitch` on main `h1` headings via SCSS only (not HTML class), `neon-flicker` on brand icon, `grid-pulse` on app background, `scan-line-v` on sidebar, `hud-ping` on status badge dots, `corner-glow-rotate` for card corners (optional), `terminal-cursor` for empty states
- **Font mono** (`--font-mono: 'JetBrains Mono'`): use for skill tags, status codes, table headers, mono labels, file names
- **Status badges:** mono font + `--radius-xs` + `hud-ping` on active/processing dots
- **Token-only rule:** NEVER hardcode hex values in component files — always use CSS custom properties from `_variables.scss`
- **Reduced motion:** handled globally in `_animations.scss` — no per-component handling needed

## Project Structure

- **Framework:** Angular 17+ (standalone components, lazy loading)
- **UI Library:** Angular Material (heavily overridden for dark cyberpunk theme)
- **State:** services + observables (no NgRx)
- **API:** `src/app/core/services/api.service.ts` — base URL from `environment.ts`
- **Proxy:** `proxy.conf.json` routes `/api/*` → `localhost:8080/*` (fixes CORS in dev)

## Feature Modules

```
features/
  resume/         — Resume CRUD (list, create, detail, edit)
  contact/        — Contact info form
  education/      — Education list + form
  experience/     — Experience list + form
  file/           — File upload component
  resume-skills/  — Skills on resume (add dialog + list)
  parse-job/      — Job description parsing status
  skill-profile/  — Skill profile cards
  skills/         — Global skill search
```

## SCSS Architecture

```
src/styles/_variables.scss   — design tokens (:root)
src/styles/_animations.scss  — @keyframes library
src/styles/_mixins.scss      — SCSS mixins
src/styles/_components.scss  — global classes + Material overrides
src/styles.scss              — manifest (imports only)
```

When adding new components: use tokens from `_variables.scss`, never hardcode.
When adding new animations: add `@keyframes` to `_animations.scss`, not inline.
