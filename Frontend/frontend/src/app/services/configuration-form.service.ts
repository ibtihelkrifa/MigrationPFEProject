import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { FormGroup, FormBuilder, FormArray } from '@angular/forms';
import { ConfForm } from '../models/conf-form';
import { Configuration } from '../models/configuration';
import { TransformationForm } from '../models/transformation-form';
import { Transformation } from '../models/transformation';
import { HttpClient } from 'selenium-webdriver/http';

@Injectable({
  providedIn: 'root'
})
export class ConfigurationFormService {
 
 
 
  private confform: BehaviorSubject<  FormGroup | undefined > = new BehaviorSubject(this.fb.group(new ConfForm(new Configuration())))
  
 
  confForm$: Observable<FormGroup> = this.confform.asObservable()

  constructor(private fb: FormBuilder) { }

  addTransformation(index:number) {

    const currentform = this.confform.getValue()

    const currentTransformations = currentform.get('transformations') as FormArray

    currentTransformations.push(
      this.fb.group(
        new TransformationForm(new Transformation(index))
      )
    )
    this.confform.next(currentform)
  }


  deleteTransformation(i: number) {
    const currentform = this.confform.getValue()
    const currentTransformations = currentform.get('transformations') as FormArray

    currentTransformations.removeAt(i)

    this.confform.next(currentform)
  }
  
}
