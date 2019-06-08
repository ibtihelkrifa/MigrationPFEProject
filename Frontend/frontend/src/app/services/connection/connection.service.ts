import { Injectable } from '@angular/core';
import { BaseSource } from '../../models/base-source';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TableSource } from '../../models/table-source';
import { BaseCible } from '../../models/base-cible';
import { TableCible } from '../../models/table-cible';
import { Configuration } from '../../models/configuration';
import { Execution } from '../../models/execution';

@Injectable({
  providedIn: 'root'
})
export class ConnectionService {

  constructor(private http: HttpClient) { }


   connectsource (basesource : BaseSource,  username: string)
  {
    return  this.http.post<BaseSource>("http://localhost:8081/connectsource/"+username, basesource)
  }

  connectcible(basecible :BaseCible,username: string)
  {
    return this.http.post<BaseCible>("http://localhost:8081/connectcible/"+username,basecible)
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

  deletebases(username)
  {
    this.http.delete<BaseCible>("http://localhost:8081/deletecible/"+username).subscribe()
    this.http.delete<BaseSource>("http://localhost:8081/deleteSource/"+username).subscribe()
  }


  deltesource(username)
  {
    this.http.delete<BaseSource>("http://localhost:8081/deleteSource/"+username).subscribe()
  }
  deletecible(username)
  {
    this.http.delete<BaseCible>("http://localhost:8081/deletecible/"+ username).subscribe()
  }



  getAllTablessSources(): Observable<TableSource[]>
  {
    return this.http.get<TableSource[]>("http://localhost:8081/getTablesSources")
  }

  getAllTablessCibles(): Observable<TableCible[]>
  {
    return this.http.get<TableCible[]>("http://localhost:8081/getTablesCibles")
  }


  configurer(conf: Configuration,username): Observable<Execution>
  {
    console.log(conf)
    return this.http.post<Execution>("http://localhost:8081/configurer/"+username,conf)
  }

  rollback()
  {
    console.log('hi')
    return this.http.get("http://localhost:8081/rollback",{responseType: 'text'})
  }

  saveExecutionResult(result,username)
  {
    return this.http.get("http://localhost:8081/resultexecution/"+result+"/"+username,{responseType: 'text'}).subscribe(data=>{
      console.log(data);
      
    })
  }

  getexecutions(username)
  {
    return this.http.get<Execution[]>("http://localhost:8081/executions/"+username)
  }

    
}
