import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { Configure2Component } from './configure2.component';

describe('Configure2Component', () => {
  let component: Configure2Component;
  let fixture: ComponentFixture<Configure2Component>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ Configure2Component ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(Configure2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
