import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MaintenanceQuotationFormComponent } from './maintenance-quotation-form.component';

describe('MaintenanceQuotationFormComponent', () => {
  let component: MaintenanceQuotationFormComponent;
  let fixture: ComponentFixture<MaintenanceQuotationFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MaintenanceQuotationFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MaintenanceQuotationFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
