import { RichKey } from './rich-key';

export class Transformation {
idtransformation: any
tablesource: String
tablecible: String
idLigne: String
typeidLigne: String
richkeys:RichKey[]
documents: Document[]

constructor(index: number)
{
    this.idtransformation=index
}

}
