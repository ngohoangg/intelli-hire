import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { provideI18nTesting } from '../../../../core/i18n/testing';
import { AuthService } from '../../../../core/services/auth.service';
import { ResetPasswordComponent } from './reset-password.component';

function activatedRouteStub(token: string | null): Partial<ActivatedRoute> {
  return { snapshot: { queryParamMap: convertToParamMap(token ? { token } : {}) } as never };
}

describe('ResetPasswordComponent', () => {
  let authSpy: jasmine.SpyObj<AuthService>;

  async function setup(token: string | null): Promise<ComponentFixture<ResetPasswordComponent>> {
    authSpy = jasmine.createSpyObj('AuthService', ['resetPassword']);

    await TestBed.configureTestingModule({
      imports: [ResetPasswordComponent],
      providers: [
        provideI18nTesting(),
        provideRouter([]),
        { provide: AuthService, useValue: authSpy },
        { provide: ActivatedRoute, useValue: activatedRouteStub(token) }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(ResetPasswordComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('should create', async () => {
    const fixture = await setup('reset-token');
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('renders the Vietnamese title and subtitle', async () => {
    const fixture = await setup('reset-token');
    const heading: HTMLElement = fixture.nativeElement.querySelector('h1');
    const subtitle: HTMLElement = fixture.nativeElement.querySelector('.subtitle');
    expect(heading.textContent?.trim()).toBe('Đặt lại mật khẩu');
    expect(subtitle.textContent?.trim()).toBe('Nhập mật khẩu mới của bạn');
  });

  it('renders the Vietnamese missing-token message via the translate pipe when no token is present', async () => {
    const fixture = await setup(null);
    fixture.detectChanges();

    const error: HTMLElement | null = fixture.nativeElement.querySelector('.error-banner');
    expect(error?.textContent?.trim()).toBe('Thiếu mã đặt lại mật khẩu.');
  });

  it('shows the Vietnamese success message on submit', async () => {
    const fixture = await setup('reset-token');
    authSpy.resetPassword.and.returnValue(of(undefined));
    fixture.componentInstance.form.setValue({ newPassword: 'password1' });

    fixture.componentInstance.submit();

    expect(fixture.componentInstance.success).toBe('Đặt lại mật khẩu thành công! Bạn có thể đăng nhập ngay bây giờ.');
  });

  it('falls back to the Vietnamese default message when the backend returns no error message', async () => {
    const fixture = await setup('reset-token');
    authSpy.resetPassword.and.returnValue(throwError(() => ({})));
    fixture.componentInstance.form.setValue({ newPassword: 'password1' });

    fixture.componentInstance.submit();

    expect(fixture.componentInstance.error).toBe('Đặt lại thất bại. Mã có thể đã hết hạn.');
  });
});
