import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

import { QuotationPreviewComponent } from './quotation-preview.component';

describe('QuotationPreviewComponent', () => {
  let component: QuotationPreviewComponent;
  let fixture: ComponentFixture<QuotationPreviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuotationPreviewComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideNoopAnimations()
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuotationPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
