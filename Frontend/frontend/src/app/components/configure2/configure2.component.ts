import { Component, OnInit, TemplateRef } from '@angular/core';
import { FormGroup, FormArray } from '@angular/forms';
import { Subscription } from 'rxjs';
import { ConfigurationFormService } from '../../services/configuration-form.service';
import { TableSource } from '../../models/table-source';
import { HttpClient } from 'selenium-webdriver/http';
import { ConnectionService } from '../../services/connection/connection.service';
import { Configuration } from '../../models/configuration';
import { MessageService } from 'primeng/components/common/messageservice';
import Swal from 'sweetalert2'
import { BaseSource } from '../../models/base-source';
import { BaseCible } from '../../models/base-cible';
import { AlertService } from 'ngx-alerts';
import { subscribeOn } from 'rxjs/operators';
import {ToastModule} from 'primeng/toast';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-configure2',
  templateUrl: './configure2.component.html',
  styleUrls: ['./configure2.component.css']
})
export class Configure2Component implements OnInit {
  confForm: FormGroup
  confFormSub: Subscription
  formInvalid: boolean = false;
  transformations: FormArray
  baseSource: BaseSource
  baseCible: BaseCible
  index:number
  public loading = false;
  public loadingTemplate: TemplateRef<any>;
 public rollback= false
 public typeconf:any
 public completedrollback=false
  errormessage: any;
  username: string;
  constructor(private alertService: AlertService,private confFormService: ConfigurationFormService, private messageService: MessageService,private connectionservice : ConnectionService, private route : ActivatedRoute) {

    this.route.paramMap.subscribe(params=>{
      this.username=params.get('username')
      //console.log(params.get('username'))
    })

   }

  ngOnInit() {



    this.confFormSub = this.confFormService.confForm$
    .subscribe(conf => {    
        this.confForm = conf
        this.transformations = this.confForm.get('transformations') as FormArray
      })
  }



  ngOnDestroy() {
    this.confFormSub.unsubscribe()
  }

  addTransformation() {

    this.confFormService.addTransformation(this.index)
  }

  deleteTransformation(index: number) {
    this.confFormService.deleteTransformation(index)
  }
  

  showSuccess() {
    this.messageService.add({severity:'success', summary: 'Success Message', detail:'Configuration en cours'});
}



  saveTransformation() {
    this.loading = true;
    this.errormessage=""
   
   
   const conf= new Configuration()
    conf.transformations=this.confForm.value.transformations
    conf.typesimulation=this.confForm.value.typesimulation
    conf.title=this.confForm.value.title
    this.connectionservice.configurer(conf,this.username).subscribe(res => {

      if(res.typeExecution=="Simulation")
      {
        this.rollback=true
      }

      this.connectionservice.saveExecutionResult("success",this.username);

      console.log(res)
      this.loading = false;
       Swal.fire(
      'Succes !!!',
      'Configuration Done with Success',
      'success'
    )

    


  }
  , err => {
    this.rollback=false
    this.errormessage=err.error.apierror.message
    this.connectionservice.saveExecutionResult("failed",this.username);

    Swal.fire(
      'ERROR !!!',
      this.errormessage,
      'error'
    )
      this.loading = false;
  });
   
  }
  getTypeConf(type: String)
  {
    this.typeconf=type
  }


  Rollback()
  {
    this.completedrollback=true
    this.connectionservice.rollback().subscribe(data =>{
      if(data== "done")
      {
        this.completedrollback=false

        this.rollback=false
        this.messageService.add({severity:'success', summary:'Service Message', detail:'Configuration Deleted With Success'});
        
      }
      else
      { 
        this.completedrollback=false
        this.rollback=false
        this.messageService.add({severity:'Danger', summary:'Service Message', detail:'Configuration Delete Operation Failed'});

      }
    })
  }
  


}
