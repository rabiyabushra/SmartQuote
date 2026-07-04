import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SupplyQuotationFormComponent } from './supply-quotation-form.component';

describe('SupplyQuotationFormComponent', () => {
  let component: SupplyQuotationFormComponent;
  let fixture: ComponentFixture<SupplyQuotationFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SupplyQuotationFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SupplyQuotationFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
