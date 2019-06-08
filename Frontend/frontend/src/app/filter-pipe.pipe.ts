import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filter'
})
export class FilterPipePipe implements PipeTransform {

  transform(executions: any[], term: string): any {
    if(term === undefined)return executions;
      return executions.filter(function(execution){
      return execution.title.toLowerCase().includes(term.toLowerCase());
    })

  }

}
