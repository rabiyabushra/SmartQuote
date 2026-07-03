import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuotationPreviewComponent } from './quotation-preview.component';

describe('QuotationPreviewComponent', () => {
  let component: QuotationPreviewComponent;
  let fixture: ComponentFixture<QuotationPreviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuotationPreviewComponent]
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
