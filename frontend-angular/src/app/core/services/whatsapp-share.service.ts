import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ShareLogRequest {
  channel: 'whatsapp' | 'email' | 'sms' | 'link';
  phone?: string;
  email?: string;
}

export interface ShareLog {
  id: string;
  quotationId: string;
  channel: string;
  phone?: string;
  email?: string;
  sharedAt: string;
}

export interface WhatsAppLinkResponse {
  whatsappUrl: string;
}

@Injectable({
  providedIn: 'root'
})
export class WhatsAppShareService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiBaseUrl}/api/quotations`;

  /**
   * Log a share action for a quotation
   */
  logShare(quotationId: string, request: ShareLogRequest): Observable<ShareLog> {
    return this.http.post<ShareLog>(`${this.API_URL}/${quotationId}/share`, request);
  }

  /**
   * Get share history for a quotation
   */
  getShareHistory(quotationId: string): Observable<ShareLog[]> {
    return this.http.get<ShareLog[]>(`${this.API_URL}/${quotationId}/share`);
  }

  /**
   * Get WhatsApp share link for a quotation
   */
  getWhatsAppLink(quotationId: string, phone: string): Observable<WhatsAppLinkResponse> {
    const params = new HttpParams().set('phone', phone);
    return this.http.get<WhatsAppLinkResponse>(
      `${this.API_URL}/${quotationId}/share/whatsapp-link`,
      { params }
    );
  }
}
