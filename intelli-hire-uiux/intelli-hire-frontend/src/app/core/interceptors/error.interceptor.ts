import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // 404 là bình thường khi dữ liệu chưa tồn tại (file, contact, skill-profile...).
      // Các component tự xử lý trong error callback, không cần snackbar.
      if (error.status === 404) {
        return throwError(() => error);
      }

      let message = 'Đã xảy ra lỗi không mong muốn';

      if (error.error?.message) {
        message = error.error.message;
      } else if (error.error?.errorCode) {
        message = `Lỗi ${error.error.errorCode}: ${error.error.message ?? error.statusText}`;
      } else if (error.status === 403) {
        message = 'Truy cập bị từ chối. Thiếu header bắt buộc.';
      } else if (error.status === 0) {
        message = 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra backend có đang chạy không.';
      }

      snackBar.open(message, 'Đóng', {
        duration: 5000,
        panelClass: ['error-snackbar']
      });

      return throwError(() => error);
    })
  );
};
