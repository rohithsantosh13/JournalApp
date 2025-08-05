import { Routes } from '@angular/router';
import { AuthGuard } from './auth.guard';
import { PrivateGuard } from './private.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent), canActivate: [AuthGuard] },
  { path: 'register', loadComponent: () => import('./components/register/register.component').then(m => m.RegisterComponent) },
  { path: 'dashboard', loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent), canActivate: [PrivateGuard] },
  { path: '**', redirectTo: 'login' }, // Wildcard route for undefined paths
];