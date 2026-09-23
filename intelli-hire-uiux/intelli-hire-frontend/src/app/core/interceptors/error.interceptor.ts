import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateService } from '@ngx-translate/core';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);
  const translate = inject(TranslateService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // 404 là bình thường khi dữ liệu chưa tồn tại (file, contact, skill-profile...).
      // Các component tự xử lý trong error callback, không cần snackbar.
      if (error.status === 404) {
        return throwError(() => error);
      }

      let message = translate.instant('common.errorInterceptor.unexpectedError');

      if (error.error?.message) {
        message = error.error.message;
      } else if (error.error?.errorCode) {
        message = `Error ${error.error.errorCode}: ${error.error.message ?? error.statusText}`;
      } else if (error.status === 403) {
        message = translate.instant('common.errorInterceptor.accessDenied');
      } else if (error.status === 0) {
        message = translate.instant('common.errorInterceptor.cannotConnect');
      }

      snackBar.open(message, translate.instant('common.actions.close'), {
        duration: 5000,
        panelClass: ['error-snackbar']
      });

      return throwError(() => error);
    })
  );
};
