import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="loader-overlay" *ngIf="isLoading">
      <div class="loader-core">
        <div class="ring ring-outer"></div>
        <div class="ring ring-middle"></div>
        <div class="ring ring-inner"></div>
        <div class="loader-dot"></div>
      </div>
      <span class="loader-label">Loading...</span>
    </div>
  `,
  styles: [`
    .loader-overlay {
      position: fixed;
      inset: 0;
      background: rgba(10, 10, 15, 0.75);
      backdrop-filter: blur(6px);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 20px;
      z-index: 9999;
      animation: fadeIn 0.2s ease;
    }

    @keyframes fadeIn {
      from { opacity: 0; }
      to   { opacity: 1; }
    }

    .loader-core {
      position: relative;
      width: 64px;
      height: 64px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .ring {
      position: absolute;
      border-radius: 50%;
      border: 1.5px solid transparent;
    }

    .ring-outer {
      inset: 0;
      border-top-color: var(--cyan);
      border-right-color: var(--cyan-glow);
      animation: rotate 1.2s linear infinite;
    }

    .ring-middle {
      inset: 8px;
      border-top-color: transparent;
      border-right-color: rgba(0, 245, 255, 0.6);
      border-bottom-color: rgba(0, 245, 255, 0.15);
      animation: rotate 0.9s linear infinite reverse;
    }

    .ring-inner {
      inset: 18px;
      border-top-color: rgba(0, 245, 255, 0.4);
      border-bottom-color: rgba(0, 245, 255, 0.4);
      animation: rotate 1.6s linear infinite;
    }

    .loader-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: var(--cyan);
      box-shadow: 0 0 12px var(--cyan), 0 0 24px var(--cyan-glow);
      animation: pulse-dot 1.2s ease-in-out infinite;
      position: relative;
      z-index: 1;
    }

    .loader-label {
      font-family: var(--font-mono);
      font-size: 12px;
      font-weight: 600;
      color: var(--cyan-glow);
      letter-spacing: 0.12em;
      text-transform: uppercase;
      animation: blink-label 1.5s ease-in-out infinite;
    }

    @keyframes rotate {
      from { transform: rotate(0deg); }
      to   { transform: rotate(360deg); }
    }

    @keyframes pulse-dot {
      0%, 100% { opacity: 1; box-shadow: 0 0 8px var(--cyan), 0 0 20px var(--cyan-glow); }
      50%       { opacity: 0.5; box-shadow: 0 0 4px var(--cyan); }
    }

    @keyframes blink-label {
      0%, 100% { opacity: 0.6; }
      50%       { opacity: 0.3; }
    }
  `]
})
export class LoadingSpinnerComponent {
  @Input() isLoading = false;
}
