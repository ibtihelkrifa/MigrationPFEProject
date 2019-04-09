import { Injectable, Output, EventEmitter } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../../models/user';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthentificationService {

  constructor(private http: HttpClient) { }

  @Output()
  authState: EventEmitter<String> = new EventEmitter()

  emitAuthState() {
    this.authState.emit('true')
  }

  getUserbyMail(email: string): Observable<User> {
    return this.http.get<User>("http://localhost:8081/users/" + email);
  }

}
