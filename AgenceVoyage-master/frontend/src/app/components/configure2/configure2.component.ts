import { Component, OnInit } from '@angular/core';
import { FormGroup, FormArray } from '@angular/forms';
import { Subscription } from 'rxjs';
import { ConfigurationFormService } from '../../services/configuration-form.service';
import { TableSource } from '../../models/table-source';

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
 
  constructor(private confFormService: ConfigurationFormService) { }

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

    this.confFormService.addTransformation()
  }

  deleteTransformation(index: number) {
    this.confFormService.deleteTransformation(index)
  }
  



  saveTransformation() {
    console.log('conf saved!')
    console.log(this.confForm.value)
  }

}
