import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  @ViewChild('registrationForm') registrationForm!: NgForm;
  user = { userName: '', email: '', password: '' };
  errorMessage = '';
  successMessage = '';
  loading = false;

  constructor(private apiService: ApiService, private router: Router) {}

  signup() {
    console.log("signup() method called.");
    this.errorMessage = '';
    this.successMessage = '';

    if (this.registrationForm.valid) {
        console.log("Form is valid. Sending data:", this.registrationForm.value);
        this.loading = true;

        this.apiService.signup(this.registrationForm.value).subscribe({
            next: () => {
                console.log("Signup API call successful.");
                this.successMessage = 'Signup successful! Redirecting to login...';
                this.loading = false;
                setTimeout(() => this.router.navigate(['/login']), 1500);
            },
            error: (err) => {
                console.error("Signup API call failed:", err);
                this.loading = false;
                if (err && err.error) {
                    if (typeof err.error === 'string') {
                         this.errorMessage = err.error;
                    } else if (err.error.message) {
                         this.errorMessage = err.error.message;
                    } else {
                         this.errorMessage = 'Signup failed. Please try again.';
                    }
                } else {
                     this.errorMessage = 'Signup failed. Please try again.';
                }
            }
        });
    } else {
        console.log("Form is invalid.");
    }
  }
}
