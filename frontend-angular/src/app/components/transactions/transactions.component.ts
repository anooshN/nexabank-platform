import { Component, OnInit, ViewChild } from '@angular/core';
import { TransactionService, Transaction, PageResponse } from '../../services/transaction.service';
import { AccountService, Account } from '../../services/account.service';
import { AuthService } from '../../services/auth.service';
import { MatPaginator } from '@angular/material/paginator';

@Component({
  selector: 'app-transactions',
  template: `
    <div class="tx-page">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Transaction History</mat-card-title>
          <span class="spacer"></span>
          <mat-form-field appearance="outline" style="width:220px;margin:0">
            <mat-label>Account</mat-label>
            <mat-select [(value)]="selectedAccount" (selectionChange)="loadTransactions()">
              <mat-option *ngFor="let acc of accounts" [value]="acc.accountNumber">{{ acc.accountNumber }}</mat-option>
            </mat-select>
          </mat-form-field>
        </mat-card-header>
        <mat-card-content>
          <table mat-table [dataSource]="transactions" class="full-width mat-elevation-z0">
            <ng-container matColumnDef="date">
              <th mat-header-cell *matHeaderCellDef>Date</th>
              <td mat-cell *matCellDef="let tx">{{ tx.createdAt | date:'MMM dd yyyy, HH:mm' }}</td>
            </ng-container>
            <ng-container matColumnDef="reference">
              <th mat-header-cell *matHeaderCellDef>Reference</th>
              <td mat-cell *matCellDef="let tx"><code>{{ tx.referenceNumber }}</code></td>
            </ng-container>
            <ng-container matColumnDef="type">
              <th mat-header-cell *matHeaderCellDef>Type</th>
              <td mat-cell *matCellDef="let tx"><mat-chip>{{ tx.type }}</mat-chip></td>
            </ng-container>
            <ng-container matColumnDef="from">
              <th mat-header-cell *matHeaderCellDef>From</th>
              <td mat-cell *matCellDef="let tx">{{ tx.fromAccountNumber }}</td>
            </ng-container>
            <ng-container matColumnDef="to">
              <th mat-header-cell *matHeaderCellDef>To</th>
              <td mat-cell *matCellDef="let tx">{{ tx.toAccountNumber }}</td>
            </ng-container>
            <ng-container matColumnDef="amount">
              <th mat-header-cell *matHeaderCellDef>Amount</th>
              <td mat-cell *matCellDef="let tx" [class.credit]="tx.toAccountNumber===selectedAccount" [class.debit]="tx.fromAccountNumber===selectedAccount">
                {{ tx.toAccountNumber===selectedAccount ? '+' : '-' }}{{ tx.currency }} {{ tx.amount | number:'1.2-2' }}
              </td>
            </ng-container>
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let tx">
                <span [class]="'badge badge-' + tx.status.toLowerCase()">{{ tx.status }}</span>
              </td>
            </ng-container>
            <ng-container matColumnDef="fraud">
              <th mat-header-cell *matHeaderCellDef>AI Risk</th>
              <td mat-cell *matCellDef="let tx">
                <span *ngIf="tx.fraudScore" [class]="'risk risk-' + getRisk(tx.fraudScore)">
                  {{ (tx.fraudScore * 100).toFixed(0) }}%
                </span>
                <span *ngIf="!tx.fraudScore" class="risk risk-low">—</span>
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="columns"></tr>
            <tr mat-row *matRowDef="let row; columns: columns;"></tr>
          </table>
          <mat-paginator [length]="totalElements" [pageSize]="20" [pageSizeOptions]="[10,20,50]"
                         (page)="onPageChange($event)" #paginator></mat-paginator>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .tx-page {} mat-card-header{display:flex;align-items:center;margin-bottom:16px}
    .spacer{flex:1}.full-width{width:100%}
    .credit{color:#059669;font-weight:600}.debit{color:#dc2626;font-weight:600}
    .badge{padding:3px 10px;border-radius:12px;font-size:11px;font-weight:600}
    .badge-completed{background:#d1fae5;color:#065f46}
    .badge-flagged{background:#fee2e2;color:#991b1b}
    .badge-pending{background:#fef3c7;color:#92400e}
    .badge-failed{background:#f3f4f6;color:#6b7280}
    .risk{padding:2px 8px;border-radius:10px;font-size:11px;font-weight:600}
    .risk-low{background:#d1fae5;color:#065f46}
    .risk-medium{background:#fef3c7;color:#92400e}
    .risk-high{background:#fee2e2;color:#991b1b}
    code{font-family:monospace;font-size:11px;background:#f3f4f6;padding:2px 5px;border-radius:3px}
  `]
})
export class TransactionsComponent implements OnInit {
  @ViewChild('paginator') paginator!: MatPaginator;
  accounts: Account[] = [];
  transactions: Transaction[] = [];
  selectedAccount = '';
  totalElements = 0;
  columns = ['date', 'reference', 'type', 'from', 'to', 'amount', 'status', 'fraud'];

  constructor(private txService: TransactionService, private accService: AccountService, private auth: AuthService) {}

  ngOnInit() {
    const user = this.auth.getCurrentUser();
    if (!user) return;
    this.accService.getMyAccounts(user.username).subscribe(accs => {
      this.accounts = accs;
      if (accs.length > 0) { this.selectedAccount = accs[0].accountNumber; this.loadTransactions(); }
    });
  }

  loadTransactions(page = 0) {
    if (!this.selectedAccount) return;
    this.txService.getHistory(this.selectedAccount, page, 20).subscribe(res => {
      this.transactions = res.content;
      this.totalElements = res.totalElements;
    });
  }

  onPageChange(event: any) { this.loadTransactions(event.pageIndex); }
  getRisk(score: number): string {
    if (score < 0.4) return 'low';
    if (score < 0.7) return 'medium';
    return 'high';
  }
}
