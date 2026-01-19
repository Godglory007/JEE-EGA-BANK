import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewCustomerFullComponent } from './new-customer-full.component';

describe('NewCustomerFullComponent', () => {
  let component: NewCustomerFullComponent;
  let fixture: ComponentFixture<NewCustomerFullComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NewCustomerFullComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NewCustomerFullComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
