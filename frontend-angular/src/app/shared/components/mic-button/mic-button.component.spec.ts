import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { MicButtonComponent } from './mic-button.component';

describe('MicButtonComponent', () => {
  let component: MicButtonComponent;
  let fixture: ComponentFixture<MicButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MicButtonComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    fixture = TestBed.createComponent(MicButtonComponent);
    component = fixture.componentInstance;
    component.quotationId = 'q-1';
    component.fieldName = 'itemDescription';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
