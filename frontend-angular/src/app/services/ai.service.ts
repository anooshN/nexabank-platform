import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ChatMessage { role: 'user' | 'bot'; content: string; timestamp: Date; }

@Injectable({ providedIn: 'root' })
export class AiService {
  private readonly API = `${environment.apiUrl}/ai`;
  private conversationId = `conv-${Date.now()}`;
  constructor(private http: HttpClient) {}

  chat(message: string): Observable<{ response: string; conversationId: string }> {
    return this.http.post<any>(`${this.API}/chat`, { message, conversationId: this.conversationId });
  }
  getInsights(transactionSummary: string): Observable<{ insights: string }> {
    return this.http.post<any>(`${this.API}/insights`, { transactionSummary });
  }
}
