import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

import { FabricationQuotationFormComponent } from './fabrication-quotation-form.component';

describe('FabricationQuotationFormComponent', () => {
  let component: FabricationQuotationFormComponent;
  let fixture: ComponentFixture<FabricationQuotationFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FabricationQuotationFormComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideNoopAnimations()
      ]
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
