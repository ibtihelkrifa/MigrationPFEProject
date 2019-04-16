import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RichKeyComponent } from './rich-key.component';

describe('RichKeyComponent', () => {
  let component: RichKeyComponent;
  let fixture: ComponentFixture<RichKeyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RichKeyComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RichKeyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
