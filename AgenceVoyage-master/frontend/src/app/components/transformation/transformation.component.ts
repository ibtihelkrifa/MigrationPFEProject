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
import * as $ from 'jquery';

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
  idrichkey=0
  selecteds:any;
  draggedCar:any;
  availableCars:any;
  multipleSelected= new Array();
  cols:any;
  selectedCars= '';
  colonnescibles$: any;
  cibledragged: any;
  cibleselected= '';
  checked: boolean= false;
  colscible: { field: string; header: string; width: string; }[];
 

  constructor(private conService: ConnectionService ,private formBuilder: FormBuilder,public ngxSmartModalService: NgxSmartModalService) { }
   inputTextMap=""
   FunctionForm:FormGroup

   ngOnInit() {
  
    this.richkeys=this.transformationForm.get('richkeys') as FormArray

   // this.transformationForm.controls['id'].disable()
    this.conService.getAllTablessSources().subscribe(
     data =>{
         this.tablessources$=data
       })

       this.conService.getAllTablessCibles().subscribe(
  data =>{
    this.tablescibles$=data
  })

  this.cols = [
    { field: 'nomcolonne', header: 'Column Name', width: '25%'}
];

this.colscible=[
  { field: 'nomcolonneFamily', header: 'Family Column Name', width:'25%'}
]

  }

  selected(value:any,j:any){
  value='#'+value;
  this.selecteds=value;
    console.log(value)
  }

  show(id1:any,id3:any,id2:any)
  {
    var x=document.getElementById(id1+id3+id2)
    if (x.style.display === "none") {
      x.style.display = "block";
    }
  }

  hideconvertisseur(id1:any,id3:any,id2:any)
  {
    var x=document.getElementById(id1+id3+id2)
    if (x.style.display === "block") {
      x.style.display = "none";
    }
  }

  hidepanel(id1:any,id3:any,id2:any)
  {
    var x=document.getElementById(id1+id3+id2)
    if (x.style.display === "none") {
      x.style.display = "block";
    } else {
      x.style.display = "none";
    }
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


  deleterichkey(index:any,j:any)
  {
    var x=document.getElementById('richkey'+index+j)

  }

  delete() {
    this.deleteTransformation.emit(this.index)
  }

  getSourceColumns()
  {
    const tablesourceIndex = this.tablessources$.findIndex(el => el.nomTable == this.transformationForm.value.tablesource)
    this.colonnessource$=this.tablessources$[tablesourceIndex].colonnes
    this.availableCars=this.colonnessource$
  }

  getCibleColumns(){
    const tablecibleindex=this.tablescibles$.findIndex(el => el.nomTable== this.transformationForm.value.tablecible)
    this.colonnescibles$=this.tablescibles$[tablecibleindex].colonnesfamille
  }




  addRichKey()
  {

    
       const currentrichkeys= this.transformationForm.get('richkeys') as FormArray
       currentrichkeys.push(
         this.formBuilder.group(
            new RichKeyForm( new RichKey())
         )
       )
       this.selectedCars=""
       this.cibleselected=""
  }


  getChangeOpperator(change:any, index: number,j:number)
  {
    this.selectedCars=this.selectedCars+change
    $('#name'+index+j).val(this.selectedCars);
  }

  getChangeFunction(change: any,index:number,j:number)
  {
    this.selectedCars=this.selectedCars+change
    $('#name'+index+j).val(this.selectedCars);


  }
  
  getVg(change:any, index:number,j:number)
  {
    this.selectedCars=this.selectedCars+change
    $('#name'+index+j).val(this.selectedCars);

  }

  closeModal(modalName: string) { this.ngxSmartModalService.close(modalName) }

  dragStart(event,car: any) {
    this.draggedCar = '#'+car;
}

dragCibleStart(event,cibledrag:any)
{
  this.cibledragged=cibledrag
}
dragEnd(event) {
  this.draggedCar = null;
}
dragCibleEnd(event)
{
  this.cibledragged=null;
}

drop(event, index:number,j:number) {
  if(this.draggedCar) {
   //   let draggedCarIndex = this.findIndex(this.draggedCar);
      this.selectedCars = this.selectedCars+this.draggedCar;
      
      
      $('#name'+index+j).val(this.selectedCars);
     // this.availableCars = this.availableCars.filter((val,i) => i!=draggedCarIndex);
      this.draggedCar = null;
  }
}

dropcible(event,index:number,j:number)
{
  if(this.cibledragged)
  {
    this.cibleselected=this.cibledragged;
    $('#cible'+index+j).val(this.cibleselected);
     this.cibledragged=null
  }

}

getSelectedMultiple(event:any){
 
  this.multipleSelected.push(event.target.value);
  console.log(event.target.value);
  

}
useConvert(index,j){
  /*var x=document.getElementById('convertisseur'+index+j)
  if(this.checked == true){
    x.style.display = "block";
  }
  else{
    x.style.display = "none";
  }
}*/
var x=document.getElementById('convertisseur'+index+j)
this.checked = !this.checked
if (this.checked == false) {
  console.log("test false");
  x.style.display = "none";
  

  
}
else{
  
  x.style.display = "block";
  console.log("test true");
  
}

 
}

hiderichkey(index:number,j:number)
{
  var x=document.getElementById('richkey'+index+j)
  if(x.style.display=='none')
  {
    x.style.display='block'
  }
  else
  {
    x.style.display='none'
  }

}

}
