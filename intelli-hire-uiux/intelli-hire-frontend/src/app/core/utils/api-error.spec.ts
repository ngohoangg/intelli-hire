import { HttpErrorResponse } from '@angular/common/http';
import { extractApiErrorMessage } from './api-error';

function makeError(body: unknown, status = 400): HttpErrorResponse {
  return new HttpErrorResponse({ error: body, status, statusText: 'Bad Request' });
}

describe('extractApiErrorMessage', () => {
  it('returns the top-level message for the auth camelCase shape', () => {
    const err = makeError({
      status: 400,
      error: 'BAD_REQUEST',
      code: 'AUTH_1001',
      message: 'Invalid current password',
      details: null,
      fieldErrors: null
    });

    expect(extractApiErrorMessage(err)).toBe('Invalid current password');
  });

  it('falls back to fieldErrors values when there is no top-level message', () => {
    const err = makeError({
      status: 400,
      error: 'VALIDATION_FAILED',
      code: 'AUTH_1002',
      message: null,
      details: null,
      fieldErrors: { newPassword: 'Password too short', confirmPassword: 'Passwords do not match' }
    });

    expect(extractApiErrorMessage(err)).toBe('Password too short; Passwords do not match');
  });

  it('joins errors[].message for the snake_case BaseResponse shape', () => {
    const err = makeError({
      success: false,
      error_code: 'VALIDATION_ERROR',
      data: null,
      errors: [
        { field: 'title', message: 'Title is required', rejected_value: null },
        { field: 'title', message: 'Title is required', rejected_value: null },
        { field: 'body', message: 'Body must not be blank', rejected_value: '' }
      ],
      timestamp: '2026-09-24T00:00:00Z'
    });

    expect(extractApiErrorMessage(err)).toBe('Title is required; Body must not be blank');
  });

  it('returns the raw string body when the response body is a plain string', () => {
    const err = makeError('Internal Server Error', 500);

    expect(extractApiErrorMessage(err)).toBe('Internal Server Error');
  });

  it('returns null when the body carries no usable message', () => {
    const err = makeError({
      success: false,
      error_code: 'INTERNAL_ERROR',
      data: null,
      errors: [],
      timestamp: '2026-09-24T00:00:00Z'
    });

    expect(extractApiErrorMessage(err)).toBeNull();
  });

  it('returns null for an empty or missing body', () => {
    expect(extractApiErrorMessage(makeError(null, 0))).toBeNull();
    expect(extractApiErrorMessage(undefined)).toBeNull();
    expect(extractApiErrorMessage(null)).toBeNull();
  });
});
