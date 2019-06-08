import { Component, OnInit, HostListener } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { AuthentificationService } from '../../services/auth/authentification.service';
import { User } from '../../models/user';
import { Router } from '@angular/router';
import { AuthService } from '../../models/authorization/auth-service';
import { TokenStorageService } from '../../models/authorization/token-storage-service';
import { AuthLoginInfo } from '../../models/authorization/auth-login-info';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  constructor(private authService: AuthService, private tokenStorage: TokenStorageService, private router: Router) { }
  form: any = {};
  isLoggedIn = false;
  isLoginFailed = false;
  errorMessage = '';
  private loginInfo: AuthLoginInfo;

  ngOnInit() {
  //  if (localStorage.getItem('logged')) this.router.navigate(['/Admin'])
    if (this.tokenStorage.getToken()) {
      this.isLoggedIn = true;
    }

  }

  signInForm = new FormGroup({
    username: new FormControl('', [Validators.required]),
    password: new FormControl('', [Validators.minLength(6), Validators.required])
  })

  alert: string



  onSubmit() {
    const { username, password } = this.signInForm.value

    this.loginInfo = new AuthLoginInfo(username,password);

    this.authService.attemptAuth(this.loginInfo).subscribe(
      data => {
     localStorage.setItem('token',data.accessToken);
     console.log(localStorage.getItem('token'))
      console.log('hi'+data.username)

        this.isLoginFailed = false;
        this.isLoggedIn = true;
        this.reloadPage();
        this.router.navigate(['/Admin',data.username ]);


      },
      error => {
        console.log(error);
        this.errorMessage = error.error.message;
        this.isLoginFailed = true;
      }
    );
  }




  reloadPage() {
    window.location.reload();
  }

}
