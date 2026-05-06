import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { AiService, ChatMessage } from '../../services/ai.service';

@Component({
  selector: 'app-ai-chat',
  template: `
    <div class="chat-page">
      <mat-card class="chat-card">
        <mat-card-header class="chat-header">
          <div class="bot-avatar" mat-card-avatar>N</div>
          <mat-card-title>NexaBot</mat-card-title>
          <mat-card-subtitle>AI Banking Assistant · Powered by Spring AI</mat-card-subtitle>
          <span class="status-dot online"></span>
        </mat-card-header>

        <mat-card-content class="messages-container" #messagesContainer>
          <div *ngFor="let msg of messages" [class]="'message ' + msg.role">
            <div class="bubble">
              <p>{{ msg.content }}</p>
              <span class="time">{{ msg.timestamp | date:'HH:mm' }}</span>
            </div>
          </div>
          <div *ngIf="loading" class="message bot">
            <div class="bubble typing">
              <span></span><span></span><span></span>
            </div>
          </div>
        </mat-card-content>

        <mat-card-actions class="input-area">
          <mat-form-field appearance="outline" class="chat-input">
            <input matInput [(ngModel)]="userInput" (keyup.enter)="sendMessage()"
                   placeholder="Ask about your account, transfers, spending..." [disabled]="loading">
          </mat-form-field>
          <button mat-fab color="primary" (click)="sendMessage()" [disabled]="!userInput.trim() || loading">
            <mat-icon>send</mat-icon>
          </button>
        </mat-card-actions>
      </mat-card>

      <div class="quick-prompts">
        <button mat-stroked-button *ngFor="let p of quickPrompts" (click)="sendQuickPrompt(p)">
          {{ p }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .chat-page { max-width: 720px; margin: 0 auto; display: flex; flex-direction: column; gap: 12px; }
    .chat-card { border-radius: 14px; display: flex; flex-direction: column; }
    .chat-header { background: linear-gradient(135deg, #1a3c5e, #2d6a9f); color: white; padding: 16px; border-radius: 14px 14px 0 0; align-items: center; }
    .bot-avatar { width: 40px; height: 40px; border-radius: 50%; background: #f59e0b; color: #1a3c5e; font-size: 18px; font-weight: 800; display: flex; align-items: center; justify-content: center; }
    .status-dot { width: 10px; height: 10px; border-radius: 50%; background: #34d399; margin-left: auto; box-shadow: 0 0 6px #34d399; }
    .messages-container { height: 420px; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
    .message { display: flex; }
    .message.user { justify-content: flex-end; }
    .message.bot { justify-content: flex-start; }
    .bubble { max-width: 75%; padding: 10px 14px; border-radius: 14px; line-height: 1.5; }
    .message.user .bubble { background: #1a3c5e; color: white; border-radius: 14px 14px 4px 14px; }
    .message.bot .bubble { background: #f3f4f6; color: #111827; border-radius: 14px 14px 14px 4px; }
    .bubble p { margin: 0; white-space: pre-wrap; }
    .time { font-size: 10px; opacity: 0.6; display: block; margin-top: 4px; text-align: right; }
    .typing { display: flex; gap: 4px; align-items: center; padding: 14px 18px; }
    .typing span { width: 8px; height: 8px; border-radius: 50%; background: #6b7280; animation: bounce 1.2s infinite; }
    .typing span:nth-child(2) { animation-delay: 0.2s; }
    .typing span:nth-child(3) { animation-delay: 0.4s; }
    @keyframes bounce { 0%,60%,100%{transform:translateY(0)} 30%{transform:translateY(-8px)} }
    .input-area { display: flex; gap: 8px; padding: 12px 16px; align-items: center; border-top: 1px solid #e5e7eb; }
    .chat-input { flex: 1; margin: 0; }
    .quick-prompts { display: flex; flex-wrap: wrap; gap: 8px; }
    .quick-prompts button { font-size: 12px; border-radius: 20px; }
  `]
})
export class AiChatComponent implements OnInit {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  messages: ChatMessage[] = [];
  userInput = '';
  loading = false;

  quickPrompts = [
    'What is my account balance?',
    'Show my recent transactions',
    'How do I transfer money?',
    'Explain my spending this month',
    'Is my account secure?'
  ];

  constructor(private aiService: AiService) {}

  ngOnInit() {
    this.messages.push({
      role: 'bot',
      content: "Hello! I'm NexaBot, your AI banking assistant. I can help you with account information, transfers, spending insights, and more. How can I help you today?",
      timestamp: new Date()
    });
  }

  sendMessage() {
    const msg = this.userInput.trim();
    if (!msg || this.loading) return;

    this.messages.push({ role: 'user', content: msg, timestamp: new Date() });
    this.userInput = '';
    this.loading = true;
    this.scrollToBottom();

    this.aiService.chat(msg).subscribe({
      next: (res) => {
        this.messages.push({ role: 'bot', content: res.response, timestamp: new Date() });
        this.loading = false;
        this.scrollToBottom();
      },
      error: () => {
        this.messages.push({ role: 'bot', content: "I'm sorry, I'm having trouble connecting right now. Please try again.", timestamp: new Date() });
        this.loading = false;
      }
    });
  }

  sendQuickPrompt(prompt: string) { this.userInput = prompt; this.sendMessage(); }

  scrollToBottom() {
    setTimeout(() => {
      const el = this.messagesContainer?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    }, 100);
  }
}
