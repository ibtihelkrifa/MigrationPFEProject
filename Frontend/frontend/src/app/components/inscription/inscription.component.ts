import { Component, OnInit } from '@angular/core';
import { FormControl, Validators, FormGroup, NgForm } from '@angular/forms';
import { User } from '../../models/user';
import { InscriptionService } from '../../services/inscription/inscription.service';
import { Router } from '@angular/router';
import { AuthService } from '../../models/authorization/auth-service';
import { SignUpInfo } from '../../models/authorization/sign-up-info';

@Component({
  selector: 'app-inscription',
  templateUrl: './inscription.component.html',
  styleUrls: ['./inscription.component.css']
})
export class InscriptionComponent implements OnInit {

  constructor(private inscriptionSerice: InscriptionService,private router:Router,private authService: AuthService) { }

  ngOnInit() {
  }

  signInForm = new FormGroup({
    identifiant: new FormControl('',[Validators.required]),
    email: new FormControl('', [Validators.email, Validators.required]),
    password: new FormControl('', [ Validators.required, Validators.minLength(6)]),
    nom: new FormControl('',[Validators.required])
  })

  submitted=false
  alert: string
  form: any = {};
  signupInfo: SignUpInfo;
  isSignedUp = false;
  isSignUpFailed = false;
  errorMessage = '';
  /*onCreateSubmit() {
    const { email, password,prenom,nom } = this.signInForm.value
    const user = new User
    user.email=email
    user.firstName=nom
    user.lastName=prenom
    user.password=password
    this.inscriptionSerice.create(user);
    this.router.navigate(['/Login']);

  }*/



  onCreateSubmit() {

    const { identifiant,email, password,nom } = this.signInForm.value

    this.submitted=true
    this.signupInfo = new SignUpInfo(nom,identifiant,email,password);

    this.authService.signUp(this.signupInfo).subscribe(
      data => {
        console.log(data);
        this.isSignedUp = true;
        this.isSignUpFailed = false;
        this.router.navigate(["/"])

      },
      error => {
        console.log(error)
        console.log('hi'+error.erros);
        this.errorMessage = error.error.message;
        this.isSignUpFailed = true;
      }
    );
  }

}
