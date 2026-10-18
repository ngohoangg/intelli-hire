import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { AuthService } from './core/services/auth.service';
import { IdleService } from './core/services/idle.service';
import { RouterOutlet } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { AuthUser } from './core/models/auth.model';

describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>;
  let idleSpy: jasmine.SpyObj<IdleService>;
  let authSpy: jasmine.SpyObj<AuthService> & { currentUser$: BehaviorSubject<AuthUser | null> };
  let currentUser$: BehaviorSubject<AuthUser | null>;

  const mockUser: AuthUser = { accountId: 1, email: 'a@b.com', username: 'user', roles: [] };

  beforeEach(async () => {
    currentUser$ = new BehaviorSubject<AuthUser | null>(null);
    idleSpy = jasmine.createSpyObj('IdleService', ['start', 'stop']);
    authSpy = jasmine.createSpyObj('AuthService', ['logout'], { currentUser$: currentUser$.asObservable() }) as any;

    await TestBed.configureTestingModule({
      imports: [AppComponent, RouterOutlet],
      providers: [
        { provide: AuthService, useValue: authSpy },
        { provide: IdleService, useValue: idleSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
  });

  it('should call idleService.start() when user logs in', () => {
    fixture.detectChanges();
    currentUser$.next(mockUser);
    expect(idleSpy.start).toHaveBeenCalledTimes(1);
    expect(idleSpy.stop).not.toHaveBeenCalled();
  });

  it('should call idleService.stop() when user logs out', () => {
    fixture.detectChanges();
    currentUser$.next(mockUser);
    currentUser$.next(null);
    expect(idleSpy.stop).toHaveBeenCalledTimes(1);
  });

  it('should call idleService.start() on init if user already logged in', () => {
    currentUser$.next(mockUser); // already logged in before init
    fixture.detectChanges();
    expect(idleSpy.start).toHaveBeenCalledTimes(1);
  });

  it('should NOT call start/stop on init if no user session', () => {
    fixture.detectChanges();
    expect(idleSpy.start).not.toHaveBeenCalled();
    expect(idleSpy.stop).not.toHaveBeenCalled();
  });

  it('should stop idle and unsubscribe on destroy', () => {
    fixture.detectChanges();
    currentUser$.next(mockUser); // start idle
    idleSpy.start.calls.reset();
    idleSpy.stop.calls.reset();

    fixture.destroy();

    expect(idleSpy.stop).toHaveBeenCalledTimes(1);
    // Further emissions should not trigger start/stop
    currentUser$.next(null);
    expect(idleSpy.stop).toHaveBeenCalledTimes(1); // no extra calls
  });
});
