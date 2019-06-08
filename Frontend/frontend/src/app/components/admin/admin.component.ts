import { Component, OnInit } from '@angular/core';
import { NgxSmartModalService } from 'ngx-smart-modal';
import { NgForm } from '@angular/forms';
import { ConnectionService } from '../../services/connection/connection.service';
import { BaseSource } from '../../models/base-source';
import { Router, ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { TableSource } from '../../models/table-source';
import { BaseCible } from '../../models/base-cible';
import { TableCible } from '../../models/table-cible';
interface Source {
  label: string;
  icon: string;
}

interface Cible{
  label: string,
  icon: string
}
@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})


export class AdminComponent implements OnInit {

  
  selectedsource: Source
  selectedcible: Cible
  SelectedSources: Source[]
  SelectedCibles: Cible[]
  RelationnelDisplayedColumns: string[] = ['nomcolonne', 'typecolonne'];
  HbaseDisplayedColumns: String[]=['nomcolonneFamily','nomcolonne' ]

  showDropdown1=true
  showDropdown2=true;
  username: string;
  errormessage: any;



  constructor( private  activeroute: ActivatedRoute, private connectionservice: ConnectionService,public ngxSmartModalService: NgxSmartModalService, private router: Router) {
    this.SelectedSources=[{label:"Mysql",icon:"" },
    {label:"OracleDB" , icon:""},
    {label:"Access", icon:""},
    {label:"MariaDB", icon:""}
]

this.activeroute.paramMap.subscribe(params=>{
  this.username=params.get('username')
  //console.log(params.get('username'))
})

this.SelectedCibles=[{label:"HBase", icon:""},
                  {label:"Hive", icon:""},
                  {label: "ParquetFile", icon: ""}]
   }

   
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
             console.log(data)
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

  closeModal(modalName: string) { this.ngxSmartModalService.close(modalName) 
  
   
  }

 
alert: string
alert2: string
alert3:string
alert4:string

  onConnectSource(f: NgForm)
  {      this.tablessources$=null
         this.alert=""
         this.alert2=""
         const baseseource= new BaseSource()
         baseseource.ip= f.value.ip
         baseseource.nomBase=f.value.nomBD
         baseseource.port=f.value.port
         baseseource.user=f.value.user
         baseseource.password=f.value.password
         baseseource.type=this.selectedsource.label
         console.log(baseseource)
       

         console.log('hi')
         this.connectionservice.connectsource(baseseource,this.username).subscribe(
           data=>{

            console.log(baseseource)
             if (data != null)
             {
               this.basesource$= data
               this.alert=""
              this.alert2="connection réusi !"

              this.closeModal('modal1')
   

              this.connectionservice.getsourcestables(this.basesource$).subscribe(
                data=>{
                  
                  this.tablessources$=data
                   f.reset()
                   this.alert2=""
                   this.alert=""
                  console.log(this.tablessources$)
                }
              )
             }
             else
             {
               this.alert2=""
              this.alert="connection non réussi, Vérifiez Bien vos paramétres!"
              
             }

           }
           , err => {
            this.errormessage=err.error.apierror.message
            console.log(this.errormessage)
            this.alert2=""
            this.alert="connection non réussi, Vérifiez Bien vos paramétres!"

           }
         )

  }
   
 

  onConnectCible(f: NgForm) {
    this.alert3=""
    this.alert4=""
        this.tablescibles$=null
        const basecible = new BaseCible()
        basecible.master=f.value.master
        basecible.quorum=f.value.quorum
        basecible.port=f.value.port
        console.log(basecible)
        console.log(this.username)
        this.connectionservice.connectcible(basecible,this.username).subscribe(
          data=>{
            if(data!= null)
            {
              this.basecible$=data
              this.alert3=""
              this.alert4="connection réusi !"
              f.reset()

              this.closeModal('modal2')

              console.log(basecible)
              this.connectionservice.getciblestables(this.basecible$).subscribe(
                
                  data =>{
                    this.tablescibles$=data
                    this.alert3=""
                    this.alert4=""
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
          , err => {
            this.errormessage=err.error.apierror.message
            console.log(this.errormessage)
            this.alert4=""
            this.alert3="connection non réussi, Vérifiez Bien vos paramétres!"

           }
        )
  
}


afficherSource()
{
  console.log(this.selectedsource)
  if (this.selectedsource.label== "Mysql" || this.selectedsource.label=="OracleDB" )
  {
    this.showDropdown2=false

    this.openModal("modal1")
    this.closeModal("modalsource")
  }
}

afficherCible()
{
  console.log(this.selectedcible)
  if (this.selectedcible.label== "HBase" )
  {
    this.showDropdown1=false

    this.openModal("modal2")
    this.closeModal("modalcible")
  }
}


DeconnectSource()
{
  this.connectionservice.deltesource(this.username);
window.location.reload()
}

DeconnectTarget()
{
  console.log('hi');
  
  this.connectionservice.deletecible(this.username);
  
  window.location.reload()

}





}
