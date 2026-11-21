import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

import { QuotationSearchFilterComponent } from './quotation-search-filter.component';

describe('QuotationSearchFilterComponent', () => {
  let component: QuotationSearchFilterComponent;
  let fixture: ComponentFixture<QuotationSearchFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuotationSearchFilterComponent],
      providers: [provideNoopAnimations()]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuotationSearchFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
