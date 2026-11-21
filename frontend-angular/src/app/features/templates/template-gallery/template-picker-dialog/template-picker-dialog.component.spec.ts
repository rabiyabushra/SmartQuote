import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogRef } from '@angular/material/dialog';
import { TemplatePickerDialogComponent } from './template-picker-dialog.component';

describe('TemplatePickerDialogComponent', () => {
  let component: TemplatePickerDialogComponent;
  let fixture: ComponentFixture<TemplatePickerDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TemplatePickerDialogComponent],
      providers: [
        { provide: MatDialogRef, useValue: { close: jasmine.createSpy('close') } }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TemplatePickerDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
