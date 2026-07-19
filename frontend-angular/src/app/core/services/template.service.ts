import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface QuotationTemplate {
  id: string;
  name: string;
  description?: string;
  category?: string;
  fields?: any;
}

@Injectable({
  providedIn: 'root'
})
export class TemplateService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiBaseUrl}/api/templates`;

  /**
   * Get all quotation templates
   */
  list(): Observable<QuotationTemplate[]> {
    return this.http.get<QuotationTemplate[]>(this.API_URL);
  }

  /**
   * Get a single template by ID
   */
  get(id: string): Observable<QuotationTemplate> {
    return this.http.get<QuotationTemplate>(`${this.API_URL}/${id}`);
  }
}
