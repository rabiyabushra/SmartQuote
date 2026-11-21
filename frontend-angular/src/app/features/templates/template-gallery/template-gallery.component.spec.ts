import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

import { TemplateGalleryComponent } from './template-gallery.component';

describe('TemplateGalleryComponent', () => {
  let component: TemplateGalleryComponent;
  let fixture: ComponentFixture<TemplateGalleryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TemplateGalleryComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideNoopAnimations()
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TemplateGalleryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
