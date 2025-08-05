import { Component, OnInit, HostListener, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  journalEntries: any[] = [];
  selectedEntry: any = null;
  loading = false;
  errorMsg = '';
  successMsg = '';
  showSettingsMenu = false;
  currentUser: any = null;
  showUserSettings = false;
  userSettings = { userName: '', sentimentAnalysis: false };
  userSettingsMsg = '';
  greetingMessage: string = 'Welcome!';
  @ViewChild('profilePanel') profilePanel!: ElementRef;

  constructor(private readonly apiService: ApiService, private router: Router) {}

  ngOnInit() {
    this.currentUser = this.apiService.getCurrentUser();
    this.fetchEntries();
    this.setupGreeting();
  }

  private async setupGreeting() {
    try {
      // First try to get user's location
      const position = await this.apiService.getUserLocation();
      const { latitude, longitude } = position.coords;

      console.log('Location obtained:', { latitude, longitude });

      // Convert coordinates to city name
      const city = await this.apiService.getCityFromCoordinates(latitude, longitude);
      console.log('City determined:', city);

      // Get greeting with city
      this.apiService.getGreeting(city).subscribe({
        next: (response) => {
          console.log('Greeting response:', response);
          this.greetingMessage = response;
        },
        error: (error) => {
          console.error('Error fetching greeting:', error);
          const userName = this.currentUser?.userName || this.currentUser?.email || 'there';
          this.greetingMessage = `Welcome back, ${userName}!`;
        }
      });

    } catch (error) {
      console.log('Location access denied or error:', error);
      this.greetingMessage = `Welcome back, ${this.currentUser?.userName || 'there'}!`;
    }
  }



  fetchEntries(selectId?: string) {
    this.loading = true;
    this.apiService.getJournalEntries().subscribe({
      next: (entries: any[]) => {
        console.log("fetchEntries: Received entries from backend:", JSON.stringify(entries, null, 2));
        this.journalEntries = entries;
        this.loading = false;
        if (entries.length > 0) {
          if (selectId) {
            const found = entries.find(e => e.id === selectId);
            this.selectedEntry = found || entries[0];
          } else {
            this.selectedEntry = entries[0];
          }
        } else {
          this.selectedEntry = null;
        }
        console.log("fetchEntries: selectedEntry is now", this.selectedEntry);
      },
      error: (error: any) => {
        this.loading = false;
        this.errorMsg = 'Failed to load journal entries';
      }
    });
  }

  selectEntry(entry: any) {
    console.log("selectEntry: setting selectedEntry to", entry);
    this.selectedEntry = entry;
  }

  createNewEntry() {
    this.selectedEntry = { title: '', content: '' };
    this.errorMsg = '';
    this.successMsg = '';
  }

  private clearMessages() {
    setTimeout(() => {
      this.successMsg = '';
      this.errorMsg = '';
    }, 3000); // Clear messages after 3 seconds
  }

  saveEntry() {
    this.errorMsg = '';
    this.successMsg = '';
    if (!this.selectedEntry.title && !this.selectedEntry.content) {
      this.errorMsg = 'Title or content required.';
      return;
    }
    this.loading = true;
    if (this.selectedEntry.id) {
      // Update existing
      this.apiService.updateJournalEntry(this.selectedEntry.id, this.selectedEntry).subscribe({
        next: (updated) => {
          this.successMsg = 'Journal entry updated!';
          this.fetchEntries(this.selectedEntry.id);
          this.loading = false;
          this.clearMessages();
        },
        error: () => {
          this.errorMsg = 'Failed to update entry.';
          this.loading = false;
        }
      });
    } else {
      // Create new
      this.apiService.createJournalEntry(this.selectedEntry).subscribe({
        next: (created) => {
          this.successMsg = 'Journal entry created!';
          this.fetchEntries(created.id);
          this.loading = false;
          this.clearMessages();
        },
        error: () => {
          this.errorMsg = 'Failed to create entry.';
          this.loading = false;
        }
      });
    }
  }

  deleteEntry() {
    console.log("deleteEntry: Attempting to delete entry:", this.selectedEntry);
    console.log("deleteEntry: Checking for id: ", this.selectedEntry?.id);
    if (!this.selectedEntry?.id) {
      this.errorMsg = 'Cannot delete entry without a valid ID.';
      return;
    }
    this.loading = true;
    this.apiService.deleteJournalEntry(this.selectedEntry.id).subscribe({
      next: () => {
        this.successMsg = 'Journal entry deleted!';
        console.log("Delete successful.");
        this.journalEntries = this.journalEntries.filter(entry => entry.id !== this.selectedEntry.id);
        this.selectedEntry = null;
        this.loading = false;
        this.clearMessages();
      },
      error: (error) => {
        console.error("Error during deletion:", error);
        this.errorMsg = 'Failed to delete entry.';
        this.loading = false;
      }
    });
  }

  logout() {
    this.apiService.logout();
    this.router.navigate(['/login']);
  }

  toggleSettingsMenu() {
    this.showSettingsMenu = !this.showSettingsMenu;
  }

  openUserSettings() {
    this.userSettings.userName = this.currentUser?.userName || '';
    this.userSettings.sentimentAnalysis = this.currentUser?.sentimentAnalysis || false;
    this.userSettingsMsg = '';
    this.showUserSettings = true;
  }

  closeUserSettings() {
    this.showUserSettings = false;
  }

  saveUserSettings() {
    this.apiService.updateUser({
      userName: this.userSettings.userName,
      sentimentAnalysis: this.userSettings.sentimentAnalysis
    }).subscribe({
      next: (updatedUser) => {
        this.userSettingsMsg = 'Profile updated!';
        localStorage.setItem('user', JSON.stringify(updatedUser));
        this.currentUser = updatedUser;
        setTimeout(() => this.closeUserSettings(), 1000);
      },
      error: () => {
        this.userSettingsMsg = 'Failed to update profile.';
      }
    });
  }

  logoutFromSettings() {
    this.apiService.logout();
    this.router.navigate(['/login']);
    this.closeUserSettings();
  }

  // Close the profile panel when clicking outside or pressing Escape
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (this.showUserSettings && this.profilePanel && !this.profilePanel.nativeElement.contains(event.target)) {
      this.closeUserSettings();
    }
  }

  @HostListener('document:keydown.escape')
  onEscape() {
    if (this.showUserSettings) {
      this.closeUserSettings();
    }
  }
}