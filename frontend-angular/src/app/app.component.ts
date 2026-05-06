import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  template: `
    <div *ngIf="!isLoggedIn()">
      <router-outlet></router-outlet>
    </div>

    <mat-sidenav-container *ngIf="isLoggedIn()" class="sidenav-container">
      <mat-sidenav #sidenav mode="side" opened class="sidenav">
        <div class="brand">
          <span class="brand-icon">N</span>
          <span class="brand-text">NexaBank</span>
        </div>
        <mat-nav-list>
          <a mat-list-item routerLink="/dashboard" routerLinkActive="active">
            <mat-icon matListItemIcon>dashboard</mat-icon>
            <span matListItemTitle>Dashboard</span>
          </a>
          <a mat-list-item routerLink="/transfer" routerLinkActive="active">
            <mat-icon matListItemIcon>swap_horiz</mat-icon>
            <span matListItemTitle>Transfer</span>
          </a>
          <a mat-list-item routerLink="/transactions" routerLinkActive="active">
            <mat-icon matListItemIcon>receipt_long</mat-icon>
            <span matListItemTitle>Transactions</span>
          </a>
          <a mat-list-item routerLink="/ai-chat" routerLinkActive="active">
            <mat-icon matListItemIcon>smart_toy</mat-icon>
            <span matListItemTitle>AI Assistant</span>
          </a>
        </mat-nav-list>
        <div class="sidenav-footer">
          <button mat-button (click)="logout()" class="logout-btn">
            <mat-icon>logout</mat-icon> Sign Out
          </button>
        </div>
      </mat-sidenav>

      <mat-sidenav-content class="main-content">
        <mat-toolbar class="top-toolbar" color="primary">
          <span>{{ getPageTitle() }}</span>
          <span class="spacer"></span>
          <span class="user-chip">{{ getUsername() }}</span>
          <mat-icon [matBadge]="notificationCount" matBadgeColor="warn" *ngIf="notificationCount > 0">notifications</mat-icon>
        </mat-toolbar>
        <div class="page-content">
          <router-outlet></router-outlet>
        </div>
      </mat-sidenav-content>
    </mat-sidenav-container>

    <!-- AI Chat floating button -->
    <button mat-fab class="ai-fab" color="accent"
            *ngIf="isLoggedIn()" routerLink="/ai-chat"
            matTooltip="Ask NexaBot">
      <mat-icon>smart_toy</mat-icon>
    </button>
  `,
  styles: [`
    .sidenav-container { height: 100vh; }
    .sidenav { width: 240px; background: #1a3c5e; }
    .brand { display: flex; align-items: center; gap: 12px; padding: 20px 16px; color: white; }
    .brand-icon { width: 36px; height: 36px; border-radius: 8px; background: #f59e0b; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 18px; color: #1a3c5e; }
    .brand-text { font-size: 20px; font-weight: 700; }
    .mat-mdc-nav-list .active { background: rgba(255,255,255,0.12); border-radius: 8px; }
    .sidenav ::ng-deep .mat-mdc-list-item { color: rgba(255,255,255,0.85) !important; margin: 2px 8px; }
    .sidenav ::ng-deep .mat-icon { color: rgba(255,255,255,0.7) !important; }
    .sidenav-footer { position: absolute; bottom: 20px; width: 100%; padding: 0 8px; }
    .logout-btn { color: rgba(255,255,255,0.7); width: 100%; justify-content: flex-start; gap: 8px; }
    .top-toolbar { box-shadow: 0 2px 8px rgba(0,0,0,0.1); background: white; color: #1a3c5e; }
    .spacer { flex: 1 1 auto; }
    .user-chip { background: #e8f0fe; color: #1a3c5e; padding: 4px 12px; border-radius: 16px; font-size: 13px; margin-right: 12px; }
    .page-content { padding: 24px; background: #f4f6f9; min-height: calc(100vh - 64px); }
    .ai-fab { position: fixed; bottom: 24px; right: 24px; z-index: 100; }
  `]
})
export class AppComponent implements OnInit {
  notificationCount = 0;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {}

  isLoggedIn() { return this.authService.isAuthenticated(); }
  getUsername() { return this.authService.getCurrentUser()?.username || ''; }
  logout() { this.authService.logout(); this.router.navigate(['/login']); }

  getPageTitle() {
    const url = window.location.pathname;
    if (url.includes('dashboard')) return 'Dashboard';
    if (url.includes('transfer')) return 'Money Transfer';
    if (url.includes('transactions')) return 'Transaction History';
    if (url.includes('ai-chat')) return 'AI Banking Assistant';
    return 'NexaBank';
  }
}
