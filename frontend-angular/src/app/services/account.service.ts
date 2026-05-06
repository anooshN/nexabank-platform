import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Account {
  id: number; accountNumber: string; username: string;
  fullName: string; email: string; accountType: string;
  balance: number; availableBalance: number; currency: string;
  status: string; createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly API = `${environment.apiUrl}/accounts`;
  constructor(private http: HttpClient) {}
  getMyAccounts(username: string): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.API}/user/${username}`);
  }
  getAccount(accountNumber: string): Observable<Account> {
    return this.http.get<Account>(`${this.API}/${accountNumber}`);
  }
  createAccount(data: any): Observable<Account> {
    return this.http.post<Account>(this.API, data);
  }
}
