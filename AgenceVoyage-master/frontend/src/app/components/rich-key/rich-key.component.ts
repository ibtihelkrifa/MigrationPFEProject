import { Component, OnInit, Output, Input, EventEmitter } from '@angular/core';
import { FormGroup } from '@angular/forms';

@Component({
  selector: 'app-rich-key',
  templateUrl: './rich-key.component.html',
  styleUrls: ['./rich-key.component.css']
})
export class RichKeyComponent implements OnInit {

  @Input() richKey: FormGroup
  @Input() index2: number
  @Output() deleteRichKey: EventEmitter<number> = new EventEmitter()
  constructor() { }

  ngOnInit() {
  }

  delete() {
    this.deleteRichKey.emit(this.index2)
  }


}
