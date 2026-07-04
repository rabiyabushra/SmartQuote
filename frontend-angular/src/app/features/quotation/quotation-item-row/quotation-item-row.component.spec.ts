import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuotationItemRowComponent } from './quotation-item-row.component';

describe('QuotationItemRowComponent', () => {
  let component: QuotationItemRowComponent;
  let fixture: ComponentFixture<QuotationItemRowComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuotationItemRowComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuotationItemRowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
