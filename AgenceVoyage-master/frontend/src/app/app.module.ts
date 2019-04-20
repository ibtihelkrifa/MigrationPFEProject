
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { AppRoutingModule } from './app-routing.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgxSmartModalModule } from 'ngx-smart-modal';
import { MatCardModule } from '@angular/material/card';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, MatSidenav, MatSidenavModule } from '@angular/material';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatCheckboxModule } from '@angular/material/checkbox';
import {MatListModule} from '@angular/material/list';
import { AppComponent } from './app.component';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { HeaderComponent } from './components/header/header.component';
import { AdminComponent } from './components/admin/admin.component';
import { LoginComponent } from './components/login/login.component';
import { NotifierModule } from 'angular-notifier';
import { InscriptionComponent } from './components/inscription/inscription.component';
import { FooterComponent } from './components/footer/footer.component';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import { ConfigureComponent } from './components/configure/configure.component';
import { Configure2Component } from './components/configure2/configure2.component';
import { TransformationComponent } from './components/transformation/transformation.component';
import { RichKeyComponent } from './components/rich-key/rich-key.component';
import {MatRadioModule, MatRadioButton} from '@angular/material/radio';
import { Page404NotFoundComponent } from './components/page404-not-found/page404-not-found.component';
import { AuthInterceptor } from './models/authorization/auth-interceptor';
import {DragDropModule} from 'primeng/dragdrop';
import {TableModule} from 'primeng/table';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    AdminComponent,
    LoginComponent,
    InscriptionComponent,
    FooterComponent,
    ConfigureComponent,
    Configure2Component,
    TransformationComponent,
    RichKeyComponent,
    Page404NotFoundComponent,       

  ],
  imports: [
    NgxSmartModalModule.forRoot(),
    DragDropModule,
    TableModule,
    MatSidenavModule,
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatToolbarModule,
    HttpClientModule,
    NgbModule.forRoot(),
    ReactiveFormsModule,
    FormsModule,
    MatCardModule,
    MatTabsModule,
    MatTableModule,
    MatButtonModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    MatExpansionModule,
    MatCheckboxModule,
    NotifierModule,
    MatListModule,
    MatButtonToggleModule,
    
    MatRadioModule
    
  ],

  exports: [MatSidenavModule],
  providers: [{
    provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true
  }],
  bootstrap: [AppComponent]
})
export class AppModule { }
