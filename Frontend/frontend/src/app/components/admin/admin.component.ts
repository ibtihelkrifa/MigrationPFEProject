import { Component, OnInit } from '@angular/core';
import { NgxSmartModalService } from 'ngx-smart-modal';
import { NgForm } from '@angular/forms';
import { ConnectionService } from '../../services/connection/connection.service';
import { BaseSource } from '../../models/base-source';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { TableSource } from '../../models/table-source';
import { BaseCible } from '../../models/base-cible';
import { TableCible } from '../../models/table-cible';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {

  RelationnelDisplayedColumns: string[] = ['nomcolonne', 'typecolonne'];
  HbaseDisplayedColumns: String[]=['nomcolonneFamily','nomcolonne' ]

  constructor(private connectionservice: ConnectionService,public ngxSmartModalService: NgxSmartModalService, private router: Router) { }

  ngOnInit() {

     this.connectionservice.getcurrentconnectionsource().subscribe(
       data=>{
        this.basesource$=data
        console.log(this.basesource$)
        
        if(this.basesource$ != null)
        {
        this.connectionservice.getsourcestables(this.basesource$).subscribe(data=>{
          this.tablessources$=data;
        })
      }
       
      
      }
     )
     
     this.connectionservice.getcurrentcible().subscribe(
       data=>{
         this.basecible$=data

         if(this.basecible$ != null)
         {
         this.connectionservice.getciblestables(this.basecible$).subscribe(
           data=>{
             this.tablescibles$=data
           }
         )
       }}
     )
   
  }

  tablescibles$: TableCible[]
  tablessources$: TableSource[]
  basesource$: BaseSource
  basecible$: BaseCible
  openModal(modalName: string) {
    this.ngxSmartModalService.open(modalName)
  }

  closeModal(modalName: string) { this.ngxSmartModalService.close(modalName) }

 
alert: string
alert2: string
alert3:string
alert4:string

  onConnectSource(f: NgForm)
  {
        
         const baseseource= new BaseSource()
         baseseource.ip= f.value.ip
         baseseource.nomBase=f.value.nomBD
         baseseource.port=f.value.port
         baseseource.user=f.value.user
         baseseource.password=f.value.password
         
         this.connectionservice.connectsource(baseseource).subscribe(
           basesource=>{

            console.log(baseseource)
             if (baseseource != null)
             {
               this.basesource$= baseseource
               this.alert=""
              this.alert2="connection réusi !"
              this.connectionservice.getsourcestables(this.basesource$).subscribe(
                data=>{
                  this.tablessources$=data
                  console.log(this.tablescibles$)
                }
              )
             }
             else
             {
               this.alert2=""
              this.alert="connection non réussi, Vérifiez Bien vos paramétres!"
              
             }

           }
         )

  }
   
 

  onConnectCible(f: NgForm) {
        const basecible = new BaseCible()
        basecible.master=f.value.master
        basecible.quorum=f.value.quorum
        basecible.port=f.value.port
        console.log(basecible)
        this.connectionservice.connectcible(basecible).subscribe(
          basecible=>{
            if(basecible!= null)
            {
              this.basecible$=basecible
              this.alert3=""
              this.alert4="connection réusi !"
              console.log(basecible)
              this.connectionservice.getciblestables(this.basecible$).subscribe(
                
                  data =>{
                    this.tablescibles$=data
                    console.log(data)
                  }
     
              )
            }
            else
            {
              this.alert4=""
              this.alert3="connection non réussi, Vérifiez Bien vos paramétres!"

            }
          }
        )
  
}









}
