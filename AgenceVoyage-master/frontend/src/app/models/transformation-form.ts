import { FormControl, Validators, FormArray, FormBuilder } from '@angular/forms';
import { Transformation } from './transformation';

export class TransformationForm {


    tablesource = new FormControl()
    tablecible = new FormControl()
    idLigne= new FormControl()
    typeidLigne= new FormControl()
    richkeys = new FormArray([])
    constructor(
    transformation: Transformation
  ) {
    if(transformation.tablesource)
    {
    this.tablesource.setValue(transformation.tablesource)
  }
  this.tablesource.setValidators([Validators.required])

  if(transformation.tablecible)
  {
    this.tablecible.setValue(transformation.tablecible)
  }
 // this.tablecible.setValidators([Validators.required])

  if(transformation.idLigne)
  {

    this.idLigne.setValue([transformation.idLigne])
  }
  this.idLigne.setValidators([Validators.required])


  if(transformation.typeidLigne)
  {
    this.typeidLigne.setValue([transformation.typeidLigne])
  }
  this.typeidLigne.setValidators([Validators.required])

  if(transformation.richkeys)
  {
    this.richkeys.setValue(transformation.richkeys)
  }

  

 // var fb= new FormBuilder

  //this.richkeys =fb.array([fb.control('')])

  }

}
