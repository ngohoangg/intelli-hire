# IntelliHire Design System
**Theme:** Neon Cyberpunk (Pure × Corporate)
**Last updated:** 2026-03-15

## Design Direction

Neon cyan as primary accent, magenta as secondary tension. Ultra-dark backgrounds make neons pop.
Think: AI hiring platform built in Neo Tokyo 2077 — bold but professional.
Not garish rainbow chaos — every neon element earns its place.

## Color Tokens (`src/styles/_variables.scss`)

### Backgrounds
| Token | Value | Usage |
|---|---|---|
| `--bg-void` | `#030306` | Deepest background (CSS grid base) |
| `--bg-primary` | `#07070F` | App body background |
| `--bg-secondary` | `#0D0D1A` | Sidebar |
| `--bg-card` | `#111120` | Card surfaces |
| `--bg-card-hover` | `#181830` | Card hover |
| `--bg-elevated` | `#1A1A35` | Modals, dropdowns |

### Neon Accents
| Token | Value | Usage |
|---|---|---|
| `--cyan` | `#00F5FF` | Primary neon — CTA, focus, active |
| `--magenta` | `#FF00AA` | Secondary neon — hover tension |
| `--purple` | `#9B00FF` | AI/processing states |
| `--accent` | `var(--cyan)` | Backward-compat alias — do not remove |

### Status
| Token | Value |
|---|---|
| `--success` | `#00FF88` |
| `--warning` | `#FFCC00` |
| `--danger` | `#FF1744` |

### Glows & Shadows
| Token | Usage |
|---|---|
| `--glow-cyan` | Full cyan neon glow (box-shadow) |
| `--glow-magenta` | Full magenta neon glow |
| `--glow-subtle` | Ambient cyan glow for buttons |
| `--cyan-glow` | Cyan background/fill glow (rgba) |
| `--shadow-elevated` | Modal depth shadow |

## Typography

| Usage | Font | Size | Weight |
|---|---|---|---|
| Headings | Syne (`--font-display`) | 20–28px | 700–800 |
| Body | DM Sans (`--font-body`) | 14px | 400–600 |
| Labels/Tags | JetBrains Mono (`--font-mono`) | 11px uppercase | 600 |
| Mono values | JetBrains Mono | 13px | 500 |

## Spacing & Radius

| Token | Value | Usage |
|---|---|---|
| `--radius-xs` | `4px` | Tags, badges, small chips |
| `--radius-sm` | `8px` | Buttons, nav items |
| `--radius-md` | `12px` | Small cards, dropdowns |
| `--radius-lg` | `16px` | Cards (default) |
| `--radius-xl` | `20px` | Dialogs, large panels |

Spacing base unit: 4px. Common values: 4, 8, 12, 16, 20, 24, 32, 40, 48, 64.

## Animations (`src/styles/_animations.scss`)

| Name | Applied to | Interval |
|---|---|---|
| `glitch` | Page `h1` headings (via SCSS) | 4s |
| `neon-flicker` | Brand icon | 5s |
| `grid-pulse` | App shell background | 8s |
| `scan-line-v` | Sidebar vertical line | 6s |
| `nav-scan` | Active nav item horizontal | 3.5s |
| `hud-ping` | Status badge processing dot | 2s |
| `terminal-cursor` | Empty state text | 1s |
| `corner-glow-rotate` | Card corner accent (optional) | 3s |

## Component Patterns

### Cards
- `border-radius: var(--radius-lg)` + `clip-path` corner cut (both coexist)
- Hover: cyan border (`--border-accent`) + top glow line (`::before`)
- Neon glow applied to wrapper `::before`, NOT on card itself (clip-path clips box-shadow)

### Buttons
- `btn-primary`: outlined neon — transparent bg + `2px solid var(--cyan)`, hover fills `rgba(cyan, 0.10)`
- Active/pressed: magenta shift (`border-color: var(--magenta)`)
- `btn-ghost`: hover → magenta border + glow
- `btn-danger`: outlined red — same pattern as primary

### Status Badges
- Font: `var(--font-mono)`, 11px, uppercase, `--radius-xs`
- Processing/active states: `hud-ping` animation on dot

### Form Inputs
- `.ih-field` wrapper: focus → cyan border + cyan label

## File Structure

```
src/
  styles/
    _variables.scss   — all CSS custom properties (:root tokens)
    _animations.scss  — all @keyframes (existing + cyberpunk)
    _mixins.scss      — SCSS mixins: neon-glow-*(), clip-corner(), glitch-text(), mono-label
    _components.scss  — global utility classes + Angular Material overrides
  styles.scss         — manifest: Google Fonts + @import partials + base reset
  design-system.md    — this file
app/
  app.component.html  — scan line div in sidebar
  app.component.scss  — grid background, sidebar styles, nav styles
```

## Do / Don't

| Do | Don't |
|---|---|
| Use `var(--cyan)` or `var(--accent)` | Hardcode `#00F5FF` or `rgba(0,240,255,...)` |
| Use `var(--font-mono)` for labels/tags | Use `'Courier New'` or bare `monospace` |
| Add glitch via SCSS `animation: glitch` on `h1` | Add HTML `.glitch-text` class or glitch body text |
| Use `clip-path` + `border-radius` together on cards | Use only `border-radius` on cards |
| Put neon glow on wrapper `::before`, not the card | Put `box-shadow` directly on `clip-path` element |
| Use `--danger` token for error colors | Hardcode `#FF3D71` (old value) |
