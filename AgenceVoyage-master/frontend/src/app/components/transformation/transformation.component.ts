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
import { DocumenForm } from '../../models/documen-form';
import {Document } from '../../models/document'
import { ColonneR } from '../../models/colonne-r';
import { TOUCH_BUFFER_MS } from '@angular/cdk/a11y';
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
  richkeys: FormArray
  documents: FormArray
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
  rightcolumns$: any;
  colssource: {field: string; header: string; width: string; field2: string; field3: string}[];
  coldragged: any;
  joinvalue: any="";
  leftcolumns$: ColonneR[];
  hbasecoldrag: any;
  tables$: any;
  hbasecols: any=""
  lefttable$: any;
  righttable$: any;
 

  constructor(private conService: ConnectionService ,private formBuilder: FormBuilder,public ngxSmartModalService: NgxSmartModalService) { }
   FunctionForm:FormGroup

   ngOnInit() {
  
    this.richkeys=this.transformationForm.get('richkeys') as FormArray
    this.documents=this.transformationForm.get('documents') as FormArray
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

this.colssource=[
  {field:'nomcolonne', header: 'Nom Colonne' , width:'25%',field2: 'table.nomTable', field3: 'nomTable'}
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


  addDocument()
{
  const currentdocuments= this.transformationForm.get('documents') as FormArray
  currentdocuments.push(
    this.formBuilder.group(
      new DocumenForm(new Document())
    )
  )
this.hbasecols=""
this.joinvalue=""
  this.leftcolumns$=null
  this.rightcolumns$=null
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

dragHbaseColsStart(event, hbasecoldrag:any)
{
  this.hbasecoldrag=hbasecoldrag
}

dragRightColsStart(event, leftcoldrag:any)
{
  this.coldragged= leftcoldrag
}

dragLeftColsStart(evenet,rightcoldrag:any )
{
  this.coldragged=rightcoldrag
}

dragEnd(event) {
  this.draggedCar = null;
}
dragCibleEnd(event)
{
  this.cibledragged=null;
}

dragRightColsEnd(event)
{
  this.coldragged=null
}

dragLeftColsEnd(event)
{
  this.coldragged=null
}

dragHBaseColsEnd(event)
{
  this.hbasecoldrag=null
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

dropHbasecols(event,index:number,d:number)
{
  if(this.hbasecoldrag)
  { this.hbasecols=this.hbasecols+ this.hbasecoldrag
    $('#hbase'+index+d).val(this.hbasecols);
    this.hbasecoldrag=null
  }
}

dropjoincol(event, index:number, d:number)
{
  if(this.coldragged)
  {
    this.joinvalue=this.joinvalue+this.coldragged
   $('#join'+index+d).val(this.joinvalue) 
   this.coldragged=null
   
  }
}

getSelectedMultiple(event:any){
 
  this.multipleSelected.push(event.target.value);
  console.log(event.target.value);
  

}
useConvert(index,j){
 
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

hidedoc(index:number, d: number)
{
  var x=document.getElementById('doc'+index+d)
  if(x.style.display=='none')
  {
    x.style.display='block'
  }
  else{
    x.style.display='none'
  }
}

getLeftTable(index2:number,d:number,nomTableSource: String)
{
  
  var colonnes: ColonneR[]
 
 
const tablesourceIndex= this.tablessources$.findIndex(el=> el.nomTable==nomTableSource)
colonnes=this.tablessources$[tablesourceIndex].colonnes
this.lefttable$=this.tablessources$[tablesourceIndex].nomTable

  this.rightcolumns$=colonnes
 this.joinvalue=""
}



getRightTable(index2:number,d:number,nomTableSource: String)
{
  var colonnes: ColonneR[]
 
const tablesourceIndex= this.tablessources$.findIndex(el=> el.nomTable==nomTableSource)
colonnes=this.tablessources$[tablesourceIndex].colonnes
this.righttable$=this.tablessources$[tablesourceIndex].nomTable

  this.leftcolumns$=colonnes
  this.joinvalue=""

   
}



}
