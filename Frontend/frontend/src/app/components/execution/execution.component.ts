import { Component, OnInit, ViewChild } from '@angular/core';
import { ConnectionService } from '../../services/connection/connection.service';
import { ActivatedRoute } from '@angular/router';
import { Execution } from '../../models/execution';
import { MatPaginator, MatTableDataSource, MatSort } from '@angular/material';

@Component({
  selector: 'app-execution',
  templateUrl: './execution.component.html',
  styleUrls: ['./execution.component.css']
})
export class ExecutionComponent implements OnInit {
  username: string;
  executions: any;

  ExecutionColumns: string[] = ['title', 'dateExecution', 'typeExecution', 'resultat'];
  
  dataSource = new MatTableDataSource<Execution>(this.executions);
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  constructor(private connectionService: ConnectionService, private route: ActivatedRoute) {
   this.route.paramMap.subscribe(params=>{
    this.username=params.get('username')

   })

   }

  ngOnInit() {
  
  this.connectionService.getexecutions(this.username).subscribe(data=>{
    this.executions=data
    this.executions.paginator=this.paginator
this.executions.sort=this.sort
  })



  }


 /* applyFilter(filterValue: string) {
    this.executions.filter = filterValue.trim().toLowerCase();
  }*/


}
