import { Component, OnInit } from '@angular/core';
import { FormControl, Validators, FormGroup, NgForm } from '@angular/forms';
import { User } from '../../models/user';
import { InscriptionService } from '../../services/inscription/inscription.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-inscription',
  templateUrl: './inscription.component.html',
  styleUrls: ['./inscription.component.css']
})
export class InscriptionComponent implements OnInit {

  constructor(private inscriptionSerice: InscriptionService,private router:Router) { }

  ngOnInit() {
  }

  signInForm = new FormGroup({
    email: new FormControl('', [Validators.email, Validators.required]),
    password: new FormControl('', [Validators.minLength(6), Validators.required]),
    prenom: new FormControl('',[Validators.minLength(1),Validators.required]),
    nom: new FormControl('',[Validators.minLength(1),Validators.required])
  })

  alert: string

  onCreateSubmit() {
    const { email, password,prenom,nom } = this.signInForm.value
    const user = new User
    user.email=email
    user.firstName=nom
    user.lastName=prenom
    user.password=password
    this.inscriptionSerice.create(user);
    this.router.navigate(['/Login']);

  }
}
