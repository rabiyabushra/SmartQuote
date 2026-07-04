import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JobWorkQuotationFormComponent } from './job-work-quotation-form.component';

describe('JobWorkQuotationFormComponent', () => {
  let component: JobWorkQuotationFormComponent;
  let fixture: ComponentFixture<JobWorkQuotationFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JobWorkQuotationFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(JobWorkQuotationFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
