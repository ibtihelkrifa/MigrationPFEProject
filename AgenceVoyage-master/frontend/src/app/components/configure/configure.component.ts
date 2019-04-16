import { Component, OnInit } from '@angular/core';
import { FormArray, FormGroup, Validators , FormBuilder} from '@angular/forms';

@Component({
  selector: 'app-configure',
  templateUrl: './configure.component.html',
  styleUrls: ['./configure.component.css']
})
export class ConfigureComponent implements OnInit {
  personalForm: FormGroup;
  constructor(private formBuilder: FormBuilder) { }

  ngOnInit() {
    this.personalForm = this.formBuilder.group({

      transformation: this.formBuilder.array([ this.addTransformation()]),
      RichKey: this.formBuilder.array([this.addRichKey()])

    });


  }
  onSubmit(): void {
    console.log(this.personalForm.value);
  }




  addTransformationButtonClick(): void {
    (<FormArray>this.personalForm.get('transformation')).push(this.addTransformation());
  }


  addRichKeyButtonClick(): void{
    (<FormArray>this.personalForm.get('RichKey')).push(this.addRichKey()) ;
  }
  
  addTransformation(): FormGroup {
    return this.formBuilder.group({
      education: ['', Validators.required],
      age : ['', Validators.required],
      degree: ['Bachelor', Validators.required],

    });
     
  

  }

  addRichKey(): FormGroup
  {
        return this.formBuilder.group({
         richkeysource: ['',Validators.required],
         richkeycible:['', Validators.required]
          //contenu de rich key form
        })
  }




  onClearDataClick() {
    this.personalForm.reset();
  }
}
