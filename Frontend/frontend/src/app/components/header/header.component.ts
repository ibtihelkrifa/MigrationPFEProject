import { Component, OnInit } from '@angular/core';
import { Router } from "@angular/router"
import { AuthentificationService } from '../../services/auth/authentification.service';
import { ConnectionService } from '../../services/connection/connection.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  constructor(private connectionservice: ConnectionService,private router: Router, private authService: AuthentificationService) { }

  authState = localStorage.getItem('logged')

  logout() {
    this.router.navigate(["/"])
    this.connectionservice.deletebases();

  }

  ngOnInit() {
    this.authService.authState.subscribe((authState: string) => {
      this.authState = authState
    })
  }

}
