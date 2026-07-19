import { Injectable, computed, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { QuotationStatus, QuotationSummary } from '../models/quotation.model';
import { QuotationTemplateId } from '../models/quotation-template.model';
import { 
  QuotationRequest, 
  QuotationResponse, 
  PdfResponse,
  StatusUpdateRequest 
} from '../models/quotation-backend.model';

export interface QuotationRecord {
  summary: QuotationSummary;
  templateId: QuotationTemplateId;
  formData: any;
}

@Injectable({ providedIn: 'root' })
export class QuotationService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiBaseUrl}/api/quotations`;

  // Keep the local signal-based store for UI state management
  private records = signal<Record<string, QuotationRecord>>({}); 
  quotations = computed(() => Object.values(this.records()).map(r => r.summary));

  /**
   * Get all quotations from backend
   */
  list(): Observable<QuotationResponse[]> {
    return this.http.get<QuotationResponse[]>(this.API_URL);
  }

  /**
   * Get a single quotation by ID from backend
   */
  get(id: string): Observable<QuotationResponse> {
    return this.http.get<QuotationResponse>(`${this.API_URL}/${id}`);
  }

  /**
   * Create a new quotation on backend
   */
  create(request: QuotationRequest): Observable<QuotationResponse> {
    return this.http.post<QuotationResponse>(this.API_URL, request);
  }

  /**
   * Update an existing quotation
   */
  update(id: string, request: QuotationRequest): Observable<QuotationResponse> {
    return this.http.put<QuotationResponse>(`${this.API_URL}/${id}`, request);
  }

  /**
   * Update quotation status only
   */
  updateStatus(id: string, status: QuotationStatus): Observable<QuotationResponse> {
    const request: StatusUpdateRequest = { status };
    return this.http.patch<QuotationResponse>(`${this.API_URL}/${id}/status`, request);
  }

  /**
   * Delete a quotation (soft delete)
   */
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  /**
   * Generate PDF for a quotation
   */
  generatePdf(id: string): Observable<PdfResponse> {
    return this.http.post<PdfResponse>(`${this.API_URL}/${id}/pdf`, {});
  }

  // Local UI state management methods (for backward compatibility)
  
  getById(id: string): QuotationRecord | undefined {
    return this.records()[id];
  }

  createLocal(templateId: QuotationTemplateId, formData: any): string {
    const id = this.generateId();
    const summary = this.buildSummaryFromFormData(id, formData);
    this.records.update(r => ({ ...r, [id]: { summary, templateId, formData } }));
    return id;
  }

  updateLocalStatus(id: string, status: QuotationStatus): void {
    this.records.update(r => {
      const record = r[id];
      if (!record) return r;
      return { ...r, [id]: { ...record, summary: { ...record.summary, status } } };
    });
  }

  private generateId(): string {
    return 'q' + Date.now().toString(36) + Math.floor(Math.random() * 1000);
  }

  private buildSummaryFromFormData(id: string, formData: any): QuotationSummary {
    return {
      id,
      quoteId: formData.quoteNumber ?? id,
      customerName: formData.customerName ?? formData.clientName ?? 'Unnamed Customer',
      contactNumber: formData.contactNumber ?? formData.contact ?? formData.clientContact ?? '—',
      email: formData.email ?? '—',
      status: 'Draft',
      employeeName: 'You',
      city: formData.city ?? '—',
      state: formData.state ?? '—',
      dateCreated: formData.quoteDate ? new Date(formData.quoteDate) : new Date(),
      followUpDate: null,
      isFollowUpOverdue: false
    };
  }
}