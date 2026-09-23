import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { TranslateLoader } from '@ngx-translate/core';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { I18N_NAMESPACES } from './i18n-namespaces';

/**
 * Multi-namespace TranslateLoader.
 *
 * ngx-translate's default HttpLoader fetches one JSON file per language. This
 * app splits each language into per-feature namespace files (see
 * `I18N_NAMESPACES`) so unrelated features don't collide in one giant JSON
 * and lanes can work on separate files without merge conflicts.
 *
 * `getTranslation(lang)` fetches every namespace file for that language in
 * parallel (`GET /i18n/<lang>/<ns>.json`, served from `public/i18n/<lang>/`),
 * then returns `{ [ns]: fileContent }`. Because ngx-translate resolves keys
 * by walking the translation object, this shape means every key is addressed
 * as `<namespace>.<rest.of.the.path>`, e.g. `common.buttons.save`.
 *
 * Failure policy: a namespace file that 404s or fails to parse is logged
 * loudly (with the exact file path) and degraded to an EMPTY object for that
 * namespace only — it does NOT fail the whole language load. One broken or
 * not-yet-authored namespace (e.g. a lane hasn't filled in `reports.json`
 * yet) must not blank every other namespace's keys across the whole app.
 * `common.json` failing is just as recoverable as any other namespace: the
 * missing-translation handler renders the raw key, which is loud enough to
 * notice without crashing bootstrap.
 */
@Injectable()
export class I18nTranslateLoader extends TranslateLoader {
  private readonly http = inject(HttpClient);

  getTranslation(lang: string): Observable<any> {
    const namespaceRequests = I18N_NAMESPACES.map((ns) =>
      this.http.get<Record<string, unknown>>(`/i18n/${lang}/${ns}.json`).pipe(
        catchError((error) => {
          console.error(`[i18n] Failed to load namespace file "/i18n/${lang}/${ns}.json":`, error);
          return of({} as Record<string, unknown>);
        })
      )
    );

    return forkJoin(namespaceRequests).pipe(
      map((files) => {
        const merged: Record<string, unknown> = {};
        I18N_NAMESPACES.forEach((ns, index) => {
          merged[ns] = files[index];
        });
        return merged;
      })
    );
  }
}
