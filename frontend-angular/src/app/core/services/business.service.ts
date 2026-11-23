import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface BusinessEntity {
  id?: string;
  businessName: string;
  ownerName: string;
  email: string;
  phone: string;
  gstin?: string;
  gstType?: string;
  bankDetails?: string;
  address?: string;
  city?: string;
  state?: string;
  country?: string;
  pincode?: string;
  logoUrl?: string | null;
  defaultTemplateId?: string;
  defaultTerms?: string;
  defaultFooterNotes?: string;
  status?: string;
}

@Injectable({
  providedIn: 'root'
})
export class BusinessService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiBaseUrl}/api/business`;

  getProfile(): Observable<BusinessEntity> {
    return this.http.get<BusinessEntity>(`${this.API_URL}/profile`);
  }

  updateProfile(business: Partial<BusinessEntity>): Observable<BusinessEntity> {
    return this.http.put<BusinessEntity>(`${this.API_URL}/profile`, business);
  }
}
