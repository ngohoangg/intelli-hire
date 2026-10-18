// Polyfill for sockjs-client (CommonJS lib that expects Node.js `global`)
(window as any).global = window;

import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

// sockjs-client requires Node.js `global` — polyfill for browser
(window as any).global = window;

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
