import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  username: string;
  role: string;
  email: string;
  expiresIn: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API = `${environment.apiUrl}/auth`;
  private currentUser$ = new BehaviorSubject<AuthResponse | null>(this.loadUser());

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API}/login`, { username, password })
      .pipe(tap(res => this.saveUser(res)));
  }

  register(username: string, email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API}/register`, { username, email, password })
      .pipe(tap(res => this.saveUser(res)));
  }

  logout(): void {
    this.http.post(`${this.API}/logout`, {}).subscribe();
    localStorage.removeItem('nexabank_user');
    this.currentUser$.next(null);
  }

  refreshToken(): Observable<AuthResponse> {
    const user = this.getCurrentUser();
    return this.http.post<AuthResponse>(`${this.API}/refresh`, { refreshToken: user?.refreshToken })
      .pipe(tap(res => this.saveUser(res)));
  }

  isAuthenticated(): boolean {
    return !!this.getCurrentUser();
  }

  getCurrentUser(): AuthResponse | null {
    return this.currentUser$.value;
  }

  getToken(): string | null {
    return this.getCurrentUser()?.accessToken || null;
  }

  private saveUser(user: AuthResponse): void {
    localStorage.setItem('nexabank_user', JSON.stringify(user));
    this.currentUser$.next(user);
  }

  private loadUser(): AuthResponse | null {
    const stored = localStorage.getItem('nexabank_user');
    return stored ? JSON.parse(stored) : null;
  }
}
