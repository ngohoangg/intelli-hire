import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { provideI18nTesting } from '../../../../core/i18n/testing';
import { AuthService } from '../../../../core/services/auth.service';
import { OAuthCallbackComponent } from './oauth-callback.component';

function activatedRouteStub(params: {
  code?: string;
  state?: string;
  provider?: string;
}): Partial<ActivatedRoute> {
  const { code, state, provider } = params;
  return {
    snapshot: {
      queryParamMap: convertToParamMap({ ...(code ? { code } : {}), ...(state ? { state } : {}) }),
      paramMap: convertToParamMap(provider ? { provider } : {})
    } as never
  };
}

async function setup(
  routeParams: { code?: string; state?: string; provider?: string },
  authSpy: jasmine.SpyObj<AuthService>
): Promise<ComponentFixture<OAuthCallbackComponent>> {
  await TestBed.configureTestingModule({
    imports: [OAuthCallbackComponent],
    providers: [
      provideI18nTesting(),
      provideRouter([]),
      { provide: AuthService, useValue: authSpy },
      { provide: ActivatedRoute, useValue: activatedRouteStub(routeParams) }
    ]
  }).compileComponents();

  return TestBed.createComponent(OAuthCallbackComponent);
}

describe('OAuthCallbackComponent', () => {
  it('should create', async () => {
    const authSpy = jasmine.createSpyObj<AuthService>('AuthService', ['handleOAuthCallback']);
    const fixture = await setup({}, authSpy);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('renders the Vietnamese title', async () => {
    const authSpy = jasmine.createSpyObj<AuthService>('AuthService', ['handleOAuthCallback']);
    const fixture = await setup({}, authSpy);
    fixture.detectChanges();

    const heading: HTMLElement = fixture.nativeElement.querySelector('h1');
    expect(heading.textContent?.trim()).toBe('Đang xác thực...');
  });

  it('renders the Vietnamese missing-params message via the translate pipe when query params are absent', async () => {
    const authSpy = jasmine.createSpyObj<AuthService>('AuthService', ['handleOAuthCallback']);
    const fixture = await setup({}, authSpy);
    fixture.detectChanges();

    const error: HTMLElement | null = fixture.nativeElement.querySelector('.error-banner');
    expect(error?.textContent?.trim()).toBe('Thiếu tham số callback OAuth.');
    expect(authSpy.handleOAuthCallback).not.toHaveBeenCalled();
  });

  it('renders the Vietnamese state-mismatch message via the translate pipe on a CSRF mismatch', async () => {
    const authSpy = jasmine.createSpyObj<AuthService>('AuthService', ['handleOAuthCallback']);
    localStorage.setItem('oauth_state', 'expected-state');
    const fixture = await setup({ code: 'abc', state: 'wrong-state', provider: 'google' }, authSpy);
    fixture.detectChanges();

    const error: HTMLElement | null = fixture.nativeElement.querySelector('.error-banner');
    expect(error?.textContent?.trim()).toBe('Trạng thái OAuth không khớp — có thể là tấn công CSRF.');
  });

  it('falls back to the Vietnamese default message when the OAuth exchange fails with no backend message', async () => {
    const authSpy = jasmine.createSpyObj<AuthService>('AuthService', ['handleOAuthCallback']);
    authSpy.handleOAuthCallback.and.returnValue(throwError(() => ({})));
    localStorage.setItem('oauth_state', 'matching-state');
    const fixture = await setup({ code: 'abc', state: 'matching-state', provider: 'google' }, authSpy);
    fixture.detectChanges();

    const error: HTMLElement | null = fixture.nativeElement.querySelector('.error-banner');
    expect(error?.textContent?.trim()).toBe('Xác thực OAuth thất bại.');
  });
});
