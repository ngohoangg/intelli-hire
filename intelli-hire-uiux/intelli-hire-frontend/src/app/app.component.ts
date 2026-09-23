import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Subscription } from 'rxjs';
import { pairwise, startWith } from 'rxjs/operators';
import { AuthService } from './core/services/auth.service';
import { IdleService } from './core/services/idle.service';
import { AuthUser } from './core/models/auth.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit, OnDestroy {
  private userSub: Subscription | null = null;

  constructor(private auth: AuthService, private idle: IdleService) {}

  ngOnInit(): void {
    this.userSub = this.auth.currentUser$.pipe(
      startWith(null as AuthUser | null),
      pairwise()
    ).subscribe(([prev, curr]) => {
      if (curr) {
        this.idle.start();
      } else if (prev !== null) {
        this.idle.stop();
      }
    });
  }

  ngOnDestroy(): void {
    this.userSub?.unsubscribe();
    this.idle.stop();
  }
}
