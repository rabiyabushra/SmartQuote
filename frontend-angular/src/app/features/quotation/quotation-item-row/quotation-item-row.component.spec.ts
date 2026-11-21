import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

import { QuotationItemRowComponent } from './quotation-item-row.component';

describe('QuotationItemRowComponent', () => {
  let component: QuotationItemRowComponent;
  let fixture: ComponentFixture<QuotationItemRowComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuotationItemRowComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations()
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuotationItemRowComponent);
    component = fixture.componentInstance;
    component.quotationId = 'QT-001';
    component.lineNumber = 1;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
