import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceQuotationFormComponent } from './service-quotation-form.component';

describe('ServiceQuotationFormComponent', () => {
  let component: ServiceQuotationFormComponent;
  let fixture: ComponentFixture<ServiceQuotationFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ServiceQuotationFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ServiceQuotationFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
