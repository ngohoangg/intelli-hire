import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { provideI18nTesting } from '../../../../core/i18n/testing';
import { AuthService } from '../../../../core/services/auth.service';
import { VerifyEmailComponent } from './verify-email.component';

function activatedRouteStub(token: string | null): Partial<ActivatedRoute> {
  return { snapshot: { queryParamMap: convertToParamMap(token ? { token } : {}) } as never };
}

async function setup(
  token: string | null,
  authSpy: jasmine.SpyObj<AuthService>
): Promise<ComponentFixture<VerifyEmailComponent>> {
  await TestBed.configureTestingModule({
    imports: [VerifyEmailComponent],
    providers: [
      provideI18nTesting(),
      provideRouter([]),
      { provide: AuthService, useValue: authSpy },
      { provide: ActivatedRoute, useValue: activatedRouteStub(token) }
    ]
  }).compileComponents();

  return TestBed.createComponent(VerifyEmailComponent);
}

describe('VerifyEmailComponent', () => {
  it('should create', async () => {
    const authSpy = jasmine.createSpyObj<AuthService>('AuthService', ['verifyEmail']);
    authSpy.verifyEmail.and.returnValue(of(undefined));
    const fixture = await setup('token-value', authSpy);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('renders the Vietnamese page title', async () => {
    const authSpy = jasmine.createSpyObj<AuthService>('AuthService', ['verifyEmail']);
    authSpy.verifyEmail.and.returnValue(of(undefined));
    const fixture = await setup('token-value', authSpy);
    fixture.detectChanges();

    const heading: HTMLElement = fixture.nativeElement.querySelector('h1');
    expect(heading.textContent?.trim()).toBe('Xác minh email');
  });

  it('renders the Vietnamese missing-token message via the translate pipe when no token is present', async () => {
    const authSpy = jasmine.createSpyObj<AuthService>('AuthService', ['verifyEmail']);
    const fixture = await setup(null, authSpy);
    fixture.detectChanges();

    const error: HTMLElement | null = fixture.nativeElement.querySelector('.error-banner');
    expect(error?.textContent?.trim()).toBe('Thiếu mã xác minh.');
    expect(authSpy.verifyEmail).not.toHaveBeenCalled();
  });

  it('renders the Vietnamese success message once verification succeeds', async () => {
    const authSpy = jasmine.createSpyObj<AuthService>('AuthService', ['verifyEmail']);
    authSpy.verifyEmail.and.returnValue(of(undefined));
    const fixture = await setup('token-value', authSpy);
    fixture.detectChanges();

    const success: HTMLElement | null = fixture.nativeElement.querySelector('.success-banner');
    expect(success?.textContent?.trim()).toBe('Xác minh email thành công! Bạn có thể đăng nhập ngay bây giờ.');
  });

  it('falls back to the Vietnamese default message when verification fails with no backend message', async () => {
    const authSpy = jasmine.createSpyObj<AuthService>('AuthService', ['verifyEmail']);
    authSpy.verifyEmail.and.returnValue(throwError(() => ({})));
    const fixture = await setup('token-value', authSpy);
    fixture.detectChanges();

    const error: HTMLElement | null = fixture.nativeElement.querySelector('.error-banner');
    expect(error?.textContent?.trim()).toBe('Xác minh thất bại. Mã có thể đã hết hạn.');
  });
});
