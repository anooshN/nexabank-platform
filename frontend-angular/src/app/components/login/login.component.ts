import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({ selector: 'app-login', templateUrl: './login.component.html', styleUrls: ['./login.component.scss'] })
export class LoginComponent {
  loginForm: FormGroup;
  loading = false; hidePass = true;
  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router, private snack: MatSnackBar) {
    this.loginForm = this.fb.group({ username: ['', [Validators.required]], password: ['', [Validators.required]] });
  }
  onSubmit() {
    if (this.loginForm.invalid) return;
    this.loading = true;
    const { username, password } = this.loginForm.value;
    this.auth.login(username, password).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => { this.snack.open(err.error?.message || 'Login failed', 'Close', { duration: 3000 }); this.loading = false; }
    });
  }
}
