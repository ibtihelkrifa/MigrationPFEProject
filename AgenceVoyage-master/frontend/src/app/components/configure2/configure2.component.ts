import { Component, OnInit } from '@angular/core';
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
  constructor(private confFormService: ConfigurationFormService, private messageService: MessageService,private connectionservice : ConnectionService) { }

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
    console.log('conf saved!')
    console.log(this.confForm.value)
    
    const conf= new Configuration()
    conf.transformations=this.confForm.value.transformations
    conf.typesimulation=this.confForm.value.typesimulation
    console.log(conf)

  
    this.connectionservice.configurer(conf)
    Swal.fire(
      'Opération betbet',
      'You clicked the button!',
      'success'
    )
  }


  


}
