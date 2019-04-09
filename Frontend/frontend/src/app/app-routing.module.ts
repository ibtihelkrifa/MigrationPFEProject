import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthGuardService as AuthGuard } from './services/auth/auth-guard.service';
import { AdminComponent } from './components/admin/admin.component';
import { LoginComponent } from './components/login/login.component';

import { InscriptionComponent } from './components/inscription/inscription.component';
const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'Admin', component: AdminComponent },
  {path: 'Inscription', component:InscriptionComponent},
  { path: '**', redirectTo: '' }

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
