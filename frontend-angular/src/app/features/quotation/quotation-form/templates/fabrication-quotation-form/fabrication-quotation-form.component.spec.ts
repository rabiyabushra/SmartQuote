import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FabricationQuotationFormComponent } from './fabrication-quotation-form.component';

describe('FabricationQuotationFormComponent', () => {
  let component: FabricationQuotationFormComponent;
  let fixture: ComponentFixture<FabricationQuotationFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FabricationQuotationFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FabricationQuotationFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
