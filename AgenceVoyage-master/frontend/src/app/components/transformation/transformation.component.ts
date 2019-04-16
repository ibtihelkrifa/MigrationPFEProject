import { Component, OnInit, Output, Input, EventEmitter } from '@angular/core';
import { FormGroup, FormBuilder, FormArray, FormControl } from '@angular/forms';
import { ConnectionService } from '../../services/connection/connection.service';
import { BaseSource } from '../../models/base-source';
import { BaseCible } from '../../models/base-cible';
import { TableSource } from '../../models/table-source';
import { TableCible } from '../../models/table-cible';
import { RichKeyForm } from '../../models/rich-key-form';
import { RichKey } from '../../models/rich-key';
import { TransformationForm } from '../../models/transformation-form';
import { Transformation } from '../../models/transformation';
import { BehaviorSubject, Observable } from 'rxjs';
import { MatRadioChange, MatRadioButton, MatSelectChange, MatCheckboxClickAction } from '@angular/material';
import { NgxSmartModalService } from 'ngx-smart-modal';

@Component({
  selector: 'app-transformation',
  templateUrl: './transformation.component.html',
  styleUrls: ['./transformation.component.css']
})
export class TransformationComponent implements OnInit {
  @Input() transformationForm: FormGroup
  @Input() index: number
  @Output() deleteTransformation: EventEmitter<number> = new EventEmitter()
  tablescibles$: any;
  tablessources$: any;
  colonnessource$: any;
  showorhidevalue="fermer"
  richkeys: FormArray
  richkey: FormGroup
  FunctionModal:FormGroup
  FunctionOnColumn:FormControl
  constructor(private conService: ConnectionService ,private formBuilder: FormBuilder,public ngxSmartModalService: NgxSmartModalService) { }
   inputTextMap=""
   FunctionForm:FormGroup
  ngOnInit() {

    
  
    
  
    this.richkeys=this.transformationForm.get('richkeys') as FormArray

    this.transformationForm.controls['id'].disable()
    this.conService.getAllTablessSources().subscribe(
     data =>{
         this.tablessources$=data
       })

       this.conService.getAllTablessCibles().subscribe(
  data =>{
    this.tablescibles$=data
  })

  }

  hide(index:any)
  {
    var x=document.getElementById(index)
    if (x.style.display === "none") {
      x.style.display = "block";
    } else {
      x.style.display = "none";
    }
  }

  delete() {
    this.deleteTransformation.emit(this.index)
  }

  getSourceColumns()
  {
    const tablesourceIndex = this.tablessources$.findIndex(el => el.nomTable == this.transformationForm.value.tablesource)
    this.colonnessource$=this.tablessources$[tablesourceIndex].colonnes

    
  }

  addRichKey()
  {


       const currentrichkeys= this.transformationForm.get('richkeys') as FormArray
       currentrichkeys.push(
         this.formBuilder.group(
            new RichKeyForm( new RichKey())
         )
       )


  }

  getSelectedRadioButton(Change:MatSelectChange)
  {
    this.inputTextMap=this.inputTextMap+Change.value

  }

  getChangeOpperator(change: MatSelectChange)
  {
    this.inputTextMap=this.inputTextMap+change.value

  }

  getChangeFunction()
  {
    this.ngxSmartModalService.open("FunctionModal")

  }
  
  closeModal(modalName: string) { this.ngxSmartModalService.close(modalName) }


}
