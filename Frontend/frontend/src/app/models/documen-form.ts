import {Document} from './document'
import { FormControl, Validators } from '@angular/forms';


export class DocumenForm {


    tablesource= new FormControl()
    colonnecible= new FormControl()
    clejointure= new FormControl()
    colonnessources= new FormControl()
    typejointure= new FormControl()
    constructor(document : Document)
    {
        if(document.tablesource)
        {
            this.tablesource.setValue(document.tablesource)
        }
        this.tablesource.setValidators([Validators.required])
        if(document.colonnescibles)
        {
            this.colonnecible.setValue(document.colonnescibles)
        }
        this.colonnecible.setValidators([Validators.required])

        if(document.clejointure)
        {
            this.clejointure.setValue(document.clejointure)
        }
        this.clejointure.setValidators([Validators.required])

        if(document.colonnessources)
        {
            this.colonnessources.setValue(document.colonnessources)
        }
        this.colonnessources.setValidators([Validators.required])
        if(document.typejointure)
        {
            this.typejointure.setValue(document.typejointure)
        }
        this.typejointure.setValidators([Validators.required])
    }
}
