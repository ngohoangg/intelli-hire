import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { provideI18nTesting } from '../../../../core/i18n/testing';
import { AuthService } from '../../../../core/services/auth.service';
import { ForgotPasswordComponent } from './forgot-password.component';

describe('ForgotPasswordComponent', () => {
  let fixture: ComponentFixture<ForgotPasswordComponent>;
  let authSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    authSpy = jasmine.createSpyObj('AuthService', ['forgotPassword']);

    await TestBed.configureTestingModule({
      imports: [ForgotPasswordComponent],
      providers: [
        provideI18nTesting(),
        provideRouter([]),
        { provide: AuthService, useValue: authSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ForgotPasswordComponent);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('renders the Vietnamese title and subtitle', () => {
    const heading: HTMLElement = fixture.nativeElement.querySelector('h1');
    const subtitle: HTMLElement = fixture.nativeElement.querySelector('.subtitle');
    expect(heading.textContent?.trim()).toBe('Quên mật khẩu');
    expect(subtitle.textContent?.trim()).toBe('Nhập email của bạn để nhận liên kết đặt lại mật khẩu');
  });

  it('shows the Vietnamese success message on submit', () => {
    authSpy.forgotPassword.and.returnValue(of(undefined));
    fixture.componentInstance.form.setValue({ email: 'a@b.com' });

    fixture.componentInstance.submit();

    expect(fixture.componentInstance.success).toBe('Đã gửi liên kết đặt lại! Vui lòng kiểm tra hộp thư email.');
  });

  it('falls back to the Vietnamese default message when the backend returns no error message', () => {
    authSpy.forgotPassword.and.returnValue(throwError(() => ({})));
    fixture.componentInstance.form.setValue({ email: 'a@b.com' });

    fixture.componentInstance.submit();

    expect(fixture.componentInstance.error).toBe('Gửi email đặt lại thất bại');
  });
});
