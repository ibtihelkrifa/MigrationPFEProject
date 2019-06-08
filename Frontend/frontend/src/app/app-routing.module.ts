import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthGuardService as AuthGuard } from './services/auth/auth-guard.service';
import { AdminComponent } from './components/admin/admin.component';
import { LoginComponent } from './components/login/login.component';
import { Configure2Component } from './components/configure2/configure2.component';

import { InscriptionComponent } from './components/inscription/inscription.component';
import { Page404NotFoundComponent } from './components/page404-not-found/page404-not-found.component';
import { ExecutionComponent } from './components/execution/execution.component';
const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'Admin/:username', component: AdminComponent },
  {path: 'Inscription', component:InscriptionComponent},
  {path:'404',component:Page404NotFoundComponent},
    {path:'Configure2/:username',component: Configure2Component},
{path:'executions/:username', component: ExecutionComponent},
  { path: '**', redirectTo: '/404' }

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
