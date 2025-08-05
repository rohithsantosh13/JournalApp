import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private readonly BASE_URL = 'http://localhost:8080';

  constructor(private readonly http: HttpClient) {}

  private handleError(error: HttpErrorResponse) {
    console.error('An error occurred:', error);
    return throwError(() => error);
  }

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/public/login`, credentials, { responseType: 'text' })
      .pipe(catchError(this.handleError));
  }

  // Google OAuth2 Methods
  initiateGoogleLogin(): Observable<any> {
    return this.http.get(`${this.BASE_URL}/auth/google/login`)
      .pipe(catchError(this.handleError));
  }

  handleGoogleCallback(code: string): Observable<any> {
    return this.http.get(`${this.BASE_URL}/auth/google/callback?code=${code}`)
      .pipe(catchError(this.handleError));
  }

  getJournalEntries(): Observable<any[]> {
    const token = localStorage.getItem('token');
    console.log("using this token to get journal entries", token);
    const headers = { 'Authorization': `Bearer ${token}` };
    return this.http.get<any[]>(`${this.BASE_URL}/journal`, { headers })
      .pipe(catchError(this.handleError));
  }

  createJournalEntry(entry: any): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    return this.http.post(`${this.BASE_URL}/journal`, entry, { headers })
      .pipe(catchError(this.handleError));
  }

  deleteJournalEntry(id: string): Observable<any> {
    const token = localStorage.getItem('token');
    console.log("using this id to delete", id);
    const headers = { 'Authorization': `Bearer ${token}` };
    return this.http.delete(`${this.BASE_URL}/journal/id/${id}`, { headers })
      .pipe(catchError(this.handleError));
  }

  updateJournalEntry(id: string, entry: any): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    return this.http.put(`${this.BASE_URL}/journal/id/${id}`, entry, { headers })
      .pipe(catchError(this.handleError));
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('token');
  }

  getCurrentUser(): any {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  signup(user: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/public/signup`, user, { responseType: 'text' })
      .pipe(catchError(this.handleError));
  }

  updateUser(user: any): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    return this.http.put(`${this.BASE_URL}/user`, user, { headers })
      .pipe(catchError(this.handleError));
  }

  getUserProfile(): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
    return this.http.get(`${this.BASE_URL}/user/me`, { headers })
      .pipe(catchError(this.handleError));
  }

  getUserLocation(): Promise<GeolocationPosition> {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocation is not supported by your browser'));
        return;
      }
      navigator.geolocation.getCurrentPosition(resolve, reject);
    });
  }

  getCityFromCoordinates(latitude: number, longitude: number): Promise<string> {
    return new Promise((resolve, reject) => {
      // Using OpenStreetMap's Nominatim service for reverse geocoding
      const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${latitude}&lon=${longitude}`;
      
      this.http.get(url).subscribe({
        next: (response: any) => {
          if (response && response.address) {
            // Try to get the city name from various possible fields
            const city = response.address.city || 
                        response.address.town || 
                        response.address.village || 
                        response.address.suburb ||
                        'Dayton'; // Default city if none found
            resolve(city);
          } else {
            resolve('Dayton'); // Default city if geocoding fails
          }
        },
        error: () => resolve('Dayton') // Default city on error
      });
    });
  }

  getGreeting(city: string): Observable<string> {
    const token = localStorage.getItem('token');
    const headers = { 'Authorization': `Bearer ${token}` };
   
    
    return this.http.get(`${this.BASE_URL}/user/greetings?city=${encodeURIComponent(city)}`, { 
      headers,
      responseType: 'text'
    }).pipe(catchError(this.handleError));
  }
}

export default ApiService;