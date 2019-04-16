import { RichKey } from './rich-key';
import { FormControl, Validators } from '@angular/forms';

export class RichKeyForm {


mappingformula= new FormControl()
constructor(richkey: RichKey)
{

        if(richkey.mappingformula)
        {
            this.mappingformula.setValue(richkey.mappingformula)
        }

        this.mappingformula.setValidators([Validators.required])
}

}
