import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface TranscriptRequest {
  quotationId: string;
  fieldName: 'itemDescription' | 'quantity' | 'rate';
  audio: File;
}

export interface TranscriptResponse {
  text: string;
  confidence?: number;
}

@Injectable({
  providedIn: 'root'
})
export class VoiceService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiBaseUrl}/api/voice`;

  /**
   * Transcribe audio to text for a quotation field
   * Note: This uses multipart/form-data, not JSON
   */
  transcribe(request: TranscriptRequest): Observable<TranscriptResponse> {
    const formData = new FormData();
    formData.append('quotationId', request.quotationId);
    formData.append('fieldName', request.fieldName);
    formData.append('audio', request.audio);

    return this.http.post<TranscriptResponse>(`${this.API_URL}/transcribe`, formData);
  }
}
