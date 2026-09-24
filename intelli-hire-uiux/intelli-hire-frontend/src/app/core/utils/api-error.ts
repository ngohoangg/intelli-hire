import { HttpErrorResponse } from '@angular/common/http';

/**
 * Backend error bodies come in two shapes:
 * - auth service (camelCase): { status, error, code, message, details, fieldErrors: { field: msg } }
 * - question/resume/interviews/grading/report services (snake_case BaseResponse):
 *   { success, error_code, data, errors: [{ field, message, rejected_value }], timestamp }
 *   (no top-level `message`)
 */
interface FieldError {
  field?: string;
  message?: string;
  rejected_value?: unknown;
}

interface ApiErrorBody {
  message?: string;
  errors?: Array<FieldError | string> | null;
  fieldErrors?: Record<string, string> | null;
}

function isNonEmptyString(value: unknown): value is string {
  return typeof value === 'string' && value.trim().length > 0;
}

/**
 * Extracts a human-readable error message from an HttpErrorResponse, handling
 * both the auth service's camelCase body and the snake_case BaseResponse body
 * used by the other backend services.
 *
 * Resolution order: top-level `message` -> deduplicated `errors[].message` ->
 * `fieldErrors` values -> null (no usable message found).
 */
export function extractApiErrorMessage(err: HttpErrorResponse | null | undefined): string | null {
  const body = err?.error as ApiErrorBody | string | null | undefined;

  if (isNonEmptyString(body)) {
    return body;
  }

  if (!body || typeof body !== 'object') {
    return null;
  }

  if (isNonEmptyString(body.message)) {
    return body.message;
  }

  if (Array.isArray(body.errors) && body.errors.length > 0) {
    const messages = body.errors
      .map(entry => (typeof entry === 'string' ? entry : entry?.message))
      .filter(isNonEmptyString);
    const unique = Array.from(new Set(messages));
    if (unique.length > 0) {
      return unique.join('; ');
    }
  }

  if (body.fieldErrors && typeof body.fieldErrors === 'object') {
    const values = Object.values(body.fieldErrors).filter(isNonEmptyString);
    if (values.length > 0) {
      return values.join('; ');
    }
  }

  return null;
}
