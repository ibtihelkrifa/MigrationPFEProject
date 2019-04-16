import { Injectable } from '@angular/core';
import { User } from '../../models/user';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class InscriptionService {

  constructor(private http: HttpClient) { }

  create(user: User) {
     this.http.post<User>("http://localhost:8081/users",user).subscribe(
       user =>{}
     )
  }



}
