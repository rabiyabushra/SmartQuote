import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

import { SupplyQuotationFormComponent } from './supply-quotation-form.component';

describe('SupplyQuotationFormComponent', () => {
  let component: SupplyQuotationFormComponent;
  let fixture: ComponentFixture<SupplyQuotationFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SupplyQuotationFormComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideNoopAnimations()
      ]
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
