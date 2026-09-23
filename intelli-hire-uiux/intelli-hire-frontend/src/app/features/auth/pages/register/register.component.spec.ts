import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { throwError } from 'rxjs';

import { provideI18nTesting } from '../../../../core/i18n/testing';
import { AuthService } from '../../../../core/services/auth.service';
import { RegisterComponent } from './register.component';

describe('RegisterComponent', () => {
  let fixture: ComponentFixture<RegisterComponent>;
  let authSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    authSpy = jasmine.createSpyObj('AuthService', ['register']);

    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        provideI18nTesting(),
        provideRouter([]),
        { provide: AuthService, useValue: authSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('renders the Vietnamese title and subtitle', () => {
    const heading: HTMLElement = fixture.nativeElement.querySelector('h1');
    const subtitle: HTMLElement = fixture.nativeElement.querySelector('.subtitle');
    expect(heading.textContent?.trim()).toBe('Tạo tài khoản');
    expect(subtitle.textContent?.trim()).toBe('Tham gia IntelliHire — nền tảng tuyển dụng ứng dụng AI');
  });

  it('renders the Vietnamese validation message when the password is too short', () => {
    const passwordControl = fixture.componentInstance.form.get('password');
    passwordControl?.setValue('a');
    passwordControl?.markAsTouched();
    fixture.detectChanges();

    const error: HTMLElement | null = fixture.nativeElement.querySelector('.field-error');
    expect(error?.textContent?.trim()).toBe('Mật khẩu phải có ít nhất 8 ký tự');
  });

  it('falls back to the Vietnamese default message when the backend returns no error message', () => {
    authSpy.register.and.returnValue(throwError(() => ({})));
    fixture.componentInstance.form.setValue({
      email: 'a@b.com',
      username: 'jane_doe',
      password: 'password1',
      confirmPassword: 'password1',
      firstName: '',
      lastName: ''
    });

    fixture.componentInstance.submit();

    expect(fixture.componentInstance.error).toBe('Đăng ký thất bại');
  });
});
