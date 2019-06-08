import { RichKey } from './rich-key';
import { FormControl, Validators, MinLengthValidator } from '@angular/forms';

export class RichKeyForm {


mappingformula= new FormControl()
colonnes= new FormControl()
condition= new FormControl()
colonnecible= new FormControl()
converter= new FormControl()
pattern= new FormControl()
typecolonnecible= new FormControl()
constructor(richkey: RichKey)
{

        if(richkey.mappingformula)
        {
            this.mappingformula.setValue(richkey.mappingformula)
        }

        this.mappingformula.setValidators(Validators.minLength(1))
        if(richkey.colonnessources)
        {   
            this.colonnes.setValue(this.colonnes);
        }

      //  this.colonnes.setValidators(Validators.required)

        if(richkey.condition)
        {
            this.condition.setValue(this.condition)
        }

        if(richkey.colonnecible)
        {
            this.colonnecible.setValue(this.colonnecible)
        }
        this.colonnecible.setValidators(Validators.required)
        
        if(richkey.converter)
        {
            this.converter.setValue(richkey.converter)
        }
        if(richkey.pattern)
        {
            this.pattern.setValue(richkey.pattern)
        }
        if(richkey.typecolonnecible)
  {
    this.typecolonnecible.setValue(richkey.typecolonnecible)
  }
  this.typecolonnecible.setValidators([Validators.required])

}

}
