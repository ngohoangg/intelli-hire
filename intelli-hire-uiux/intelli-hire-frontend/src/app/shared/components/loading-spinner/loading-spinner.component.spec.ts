import { ComponentFixture, TestBed } from '@angular/core/testing';

import { provideI18nTesting } from '../../../core/i18n/testing';
import { LoadingSpinnerComponent } from './loading-spinner.component';

describe('LoadingSpinnerComponent', () => {
  let fixture: ComponentFixture<LoadingSpinnerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoadingSpinnerComponent],
      providers: [provideI18nTesting()]
    }).compileComponents();

    fixture = TestBed.createComponent(LoadingSpinnerComponent);
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('renders the Vietnamese loading label when isLoading is true', () => {
    fixture.componentInstance.isLoading = true;
    fixture.detectChanges();

    const label: HTMLElement | null = fixture.nativeElement.querySelector('.loader-label');
    expect(label?.textContent?.trim()).toBe('Đang tải...');
  });

  it('renders nothing when isLoading is false', () => {
    fixture.componentInstance.isLoading = false;
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.loader-overlay')).toBeNull();
  });
});
