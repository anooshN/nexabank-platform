import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, catchError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private auth: AuthService, private router: Router) {}
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.auth.getToken();
    const user = this.auth.getCurrentUser();
const authReq = token ? req.clone({ setHeaders: {
  Authorization: `Bearer ${token}`,
  'X-Auth-User': user?.username || '',
  'X-Auth-Role': user?.role || ''
}}) : req;
    return next.handle(authReq).pipe(
      catchError((err: HttpErrorResponse) => {
        if (err.status === 401) { this.auth.logout(); this.router.navigate(['/login']); }
        return throwError(() => err);
      })
    );
  }
}
