import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from "@angular/router"
import { AuthentificationService } from '../../services/auth/authentification.service';
import { ConnectionService } from '../../services/connection/connection.service';
import { TokenStorageService } from '../../models/authorization/token-storage-service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  info: any;
  username: string;

  constructor(private connectionservice: ConnectionService,private router: Router, private authService: AuthentificationService,private token: TokenStorageService, private route: ActivatedRoute) { 

      this.route.paramMap.subscribe(paramsMap=>{
        this.username=paramsMap.get('username');
        console.log(this.username);
        
      })

  }


 /* logout() {
    this.router.navigate(["/"])
    this.connectionservice.deletebases();

  }*/


  logout() {
    this.token.signOut();
    this.connectionservice.deletebases(this.username);

    window.location.reload();
    this.router.navigate([""])
  }


  ngOnInit() {
    this.info = {
      token: this.token.getToken(),
      username: this.token.getUsername(),
    };
   
  }

}
