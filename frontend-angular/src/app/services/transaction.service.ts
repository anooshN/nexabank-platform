import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Transaction {
  id: number; transactionId: string; fromAccountNumber: string;
  toAccountNumber: string; amount: number; currency: string;
  type: string; status: string; description: string;
  referenceNumber: string; fraudScore: number;
  createdAt: string; completedAt: string;
}

export interface PageResponse<T> {
  content: T[]; totalElements: number; totalPages: number; size: number; number: number;
}

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly API = `${environment.apiUrl}/transactions`;
  constructor(private http: HttpClient) {}

  transfer(data: any): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.API}/transfer`, data);
  }
  deposit(data: any): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.API}/deposit`, data);
  }
  getHistory(accountNumber: string, page = 0, size = 20): Observable<PageResponse<Transaction>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Transaction>>(`${this.API}/account/${accountNumber}`, { params });
  }
}
