import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { provideI18nTesting } from '../../../../core/i18n/testing';
import { AuthService } from '../../../../core/services/auth.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let authSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    authSpy = jasmine.createSpyObj(
      'AuthService',
      ['login', 'getOAuthProviders', 'getOAuthAuthorizationUrl'],
      { isLoggedIn: false, isAdmin: false }
    );
    authSpy.getOAuthProviders.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideI18nTesting(),
        provideRouter([]),
        { provide: AuthService, useValue: authSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('renders the Vietnamese title and subtitle', () => {
    const heading: HTMLElement = fixture.nativeElement.querySelector('h1');
    const subtitle: HTMLElement = fixture.nativeElement.querySelector('.subtitle');
    expect(heading.textContent?.trim()).toBe('Đăng nhập');
    expect(subtitle.textContent?.trim()).toBe('Truy cập không gian làm việc IntelliHire của bạn');
  });

  it('renders the Vietnamese submit label', () => {
    const submitLabel: HTMLElement | null = fixture.nativeElement.querySelector('button.btn-primary span');
    expect(submitLabel?.textContent?.trim()).toBe('Đăng nhập');
  });

  it('falls back to the Vietnamese default message when the backend returns no error message', () => {
    authSpy.login.and.returnValue(throwError(() => ({})));
    fixture.componentInstance.form.setValue({ identifier: 'a', password: 'b' });

    fixture.componentInstance.submit();

    expect(fixture.componentInstance.error).toBe('Thông tin đăng nhập không hợp lệ');
  });
});
