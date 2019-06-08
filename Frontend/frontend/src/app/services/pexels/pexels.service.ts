import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../../environments/environment'

@Injectable({
  providedIn: 'root'
})
export class PexelsService {

  constructor(private http: HttpClient) { }

  // fetchThumbnailUrl(query: String): String {
  //   let url = ''
  //   this.http.get("https://api.pexels.com/v1/search?query=" + query + "&per_page=1&page=1", { headers: new HttpHeaders({ Authorization: environment.pexelApiKey }) })
  //     .subscribe(data => {return data.photos[0].src.tiny)
  //   return url;

}
