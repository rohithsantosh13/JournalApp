import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-login',
  standalone: true, // Ensure this is a standalone component
  imports: [CommonModule, FormsModule, RouterModule], // Add RouterModule here
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  credentials = { username: '', password: '' };
  errorMessage = '';
  isLoading = false;

  constructor(
    private apiService: ApiService, 
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    // If already authenticated, redirect to dashboard
    if (this.apiService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
      return;
    }
    // Check if there's a Google OAuth2 callback code in the URL
    this.route.queryParams.subscribe(params => {
      const code = params['code'];
      if (code) {
        this.handleGoogleCallback(code);
      }
    });
  }

  login() {
    this.errorMessage = '';
    this.isLoading = true;
    console.log('Login payload:', {
      userName: this.credentials.username,
      password: this.credentials.password
    });
    this.apiService.login({
      userName: this.credentials.username,
      password: this.credentials.password
    }).subscribe({
      next: (token: string) => {
        console.log('Login success:', token);
        localStorage.setItem('token', token);
        // Fetch user profile after login and save to localStorage
        this.apiService.getUserProfile().subscribe({
          next: (user: any) => {
            localStorage.setItem('user', JSON.stringify(user));
            this.isLoading = false;
            this.router.navigate(['/dashboard']);
          },
          error: (err) => {
            console.log('Failed to fetch user profile:', err);
            this.isLoading = false;
            this.router.navigate(['/dashboard']);
          }
        });
      },
      error: (err) => {
        console.log('Login error:', err);
        this.errorMessage = 'Invalid username or password';
        this.isLoading = false;
      }
    });
  }

  loginWithGoogle() {
    this.errorMessage = '';
    this.isLoading = true;
    
    this.apiService.initiateGoogleLogin().subscribe({
      next: (response: any) => {
        console.log('Google login URL:', response.authUrl);
        // Redirect to Google OAuth2 URL
        window.location.href = response.authUrl;
      },
      error: (err) => {
        console.log('Google login error:', err);
        this.errorMessage = 'Failed to initiate Google login';
        this.isLoading = false;
      }
    });
  }

  private handleGoogleCallback(code: string) {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.apiService.handleGoogleCallback(code).subscribe({
      next: (response: any) => {
        console.log('Google callback success:', response);
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify(response.user));
        this.isLoading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        console.log('Google callback error:', err);
        this.errorMessage = 'Google authentication failed';
        this.isLoading = false;
      }
    });
  }
}