/**
 * Every translation namespace the app ships. Each namespace maps 1:1 to a
 * JSON file at `public/i18n/<lang>/<ns>.json` (served at `/i18n/<lang>/<ns>.json`).
 *
 * Adding a namespace: append it here, then create the matching (empty `{}`)
 * file under `public/i18n/vi/<ns>.json` and `public/i18n/en/<ns>.json`.
 */
export const I18N_NAMESPACES = [
  'common',
  'auth',
  'account',
  'admin',
  'resume',
  'questions',
  'interviews',
  'reports',
] as const;

export type I18nNamespace = (typeof I18N_NAMESPACES)[number];
