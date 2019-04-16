import { FormArray } from '@angular/forms';
import { Transformation } from './transformation';

export class TransForm {

    richkeys= new FormArray([])

constructor(trans: Transformation)
{
    if(trans.richkeys)
    {
        this.richkeys.setValue(trans.richkeys)
    }
}

}
