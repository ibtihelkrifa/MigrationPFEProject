import { FormArray, FormControl, Validators } from '@angular/forms';
import { Configuration } from './configuration';

export class ConfForm {

    typesimulation = new FormControl()
    transformations = new FormArray([])
    title= new FormControl()

    constructor(conf: Configuration) {
        if (conf.typesimulation) {
            this.typesimulation.setValue(conf.typesimulation)
        }
        this.typesimulation.setValidators([Validators.required])
        if (conf.transformations) {
            this.transformations.setValue(conf.transformations)
        }

        if(conf.title)
        {
            this.title.setValue(conf.title)
        }

        this.title.setValidators([Validators.required])
    }
}
