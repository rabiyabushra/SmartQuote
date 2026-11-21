import { TestBed } from '@angular/core/testing';
import { VoiceService } from './voice.service';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('VoiceService', () => {
  let service: VoiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(VoiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
