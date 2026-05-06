import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TransactionService } from '../../services/transaction.service';
import { AccountService, Account } from '../../services/account.service';
import { AuthService } from '../../services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-transfer',
  template: `
    <div class="transfer-page">
      <mat-card class="transfer-card">
        <mat-card-header>
          <mat-icon mat-card-avatar color="primary">swap_horiz</mat-icon>
          <mat-card-title>Transfer Money</mat-card-title>
          <mat-card-subtitle>Fast, secure, instant</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="transferForm" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>From Account</mat-label>
              <mat-select formControlName="fromAccountNumber">
                <mat-option *ngFor="let acc of accounts" [value]="acc.accountNumber">
                  {{ acc.accountNumber }} — {{ acc.currency }} {{ acc.availableBalance | number:'1.2-2' }}
                </mat-option>
              </mat-select>
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>To Account Number</mat-label>
              <input matInput formControlName="toAccountNumber" placeholder="NXB...">
              <mat-icon matSuffix>account_balance</mat-icon>
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Amount</mat-label>
              <input matInput type="number" formControlName="amount" placeholder="0.00">
              <span matPrefix>$ &nbsp;</span>
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Description (optional)</mat-label>
              <input matInput formControlName="description">
            </mat-form-field>
            <div class="ai-note" *ngIf="!loading">
              <mat-icon style="color:#7c3aed;font-size:16px">smart_toy</mat-icon>
              <span>AI fraud check runs automatically on every transfer</span>
            </div>
            <button mat-raised-button color="primary" type="submit"
                    [disabled]="transferForm.invalid || loading" class="full-width submit-btn">
              <mat-spinner *ngIf="loading" diameter="20" style="display:inline-block;margin-right:8px"></mat-spinner>
              {{ loading ? 'Processing...' : 'Send Money' }}
            </button>
          </form>
          <div class="success-box" *ngIf="lastTx">
            <mat-icon color="accent">check_circle</mat-icon>
            <div>
              <strong>Transfer Complete!</strong><br>
              Ref: {{ lastTx.referenceNumber }} &nbsp; Amount: {{ lastTx.currency }} {{ lastTx.amount | number:'1.2-2' }}
            </div>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .transfer-page { max-width: 560px; margin: 0 auto; }
    .transfer-card { border-radius: 14px; }
    .full-width { width: 100%; margin-bottom: 8px; }
    .submit-btn { height: 48px; font-size: 15px; margin-top: 12px; }
    .ai-note { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #7c3aed; background: #f5f3ff; padding: 8px 12px; border-radius: 6px; margin-bottom: 8px; }
    .success-box { display: flex; align-items: center; gap: 12px; margin-top: 20px; padding: 16px; background: #d1fae5; border-radius: 8px; color: #065f46; }
  `]
})
export class TransferComponent implements OnInit {
  transferForm: FormGroup;
  accounts: Account[] = [];
  loading = false;
  lastTx: any = null;

  constructor(private fb: FormBuilder, private txService: TransactionService,
              private accService: AccountService, private auth: AuthService, private snack: MatSnackBar) {
    this.transferForm = this.fb.group({
      fromAccountNumber: ['', Validators.required],
      toAccountNumber: ['', Validators.required],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      description: ['']
    });
  }

  ngOnInit() {
    const user = this.auth.getCurrentUser();
    if (user) this.accService.getMyAccounts(user.username).subscribe(a => this.accounts = a);
  }

  onSubmit() {
    if (this.transferForm.invalid) return;
    this.loading = true; this.lastTx = null;
    this.txService.transfer(this.transferForm.value).subscribe({
      next: (tx) => { this.lastTx = tx; this.loading = false; this.transferForm.reset(); this.snack.open('Transfer successful!', 'Close', { duration: 3000 }); },
      error: (err) => { this.snack.open(err.error?.message || 'Transfer failed', 'Close', { duration: 4000 }); this.loading = false; }
    });
  }
}
