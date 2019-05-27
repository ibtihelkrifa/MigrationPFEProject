import { Injectable } from '@angular/core';
import { BaseSource } from '../../models/base-source';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TableSource } from '../../models/table-source';
import { BaseCible } from '../../models/base-cible';
import { TableCible } from '../../models/table-cible';
import { Configuration } from '../../models/configuration';

@Injectable({
  providedIn: 'root'
})
export class ConnectionService {

  constructor(private http: HttpClient) { }


   connectsource (basesource : BaseSource)
  {
    return  this.http.post<BaseSource>("http://localhost:8081/connectsource", basesource)
  }

  connectcible(basecible :BaseCible)
  {
    return this.http.post<BaseCible>("http://localhost:8081/connectcible",basecible)
  }

   getciblestables(basecible: BaseCible): Observable<TableCible[]>
   {
     var master= basecible.master
     return this.http.get<TableCible[]>("http://localhost:8081/tablescibles/"+ master)

   }

  getsourcestables(baseseource: BaseSource): Observable<TableSource[]>
  {
    var nombase= baseseource.nomBase
    console.log(baseseource)
     return this.http.get<TableSource[]>("http://localhost:8081/tablessources/"+ nombase)
  }



  
 getcurrentconnectionsource(): Observable<BaseSource>
  {
    return this.http.get<BaseSource>("http://localhost:8081/getCurrentSource")
  }
  getcurrentcible(): Observable<BaseCible>
  {
    return this.http.get<BaseCible>("http://localhost:8081/getcurrentcible")
  }

  deletebases()
  {
    this.http.delete<BaseCible>("http://localhost:8081/deletecible").subscribe()
    this.http.delete<BaseSource>("http://localhost:8081/deleteSource").subscribe()
  }


  getAllTablessSources(): Observable<TableSource[]>
  {
    return this.http.get<TableSource[]>("http://localhost:8081/getTablesSources")
  }

  getAllTablessCibles(): Observable<TableCible[]>
  {
    return this.http.get<TableCible[]>("http://localhost:8081/getTablesCibles")
  }


  configurer(conf: Configuration): Observable<Configuration>
  {
    console.log(conf)
    return this.http.post<Configuration>("http://localhost:8081/configurer",conf)
  }

  rollback()
  {
    console.log('hi')
    return this.http.get("http://localhost:8081/rollback",{responseType: 'text'})
  }
    
}
