import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Contact } from '../models/contact.model';

@Injectable({
  providedIn: 'root'
})
export class ContactService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiBaseUrl}/api/contacts`;

  /**
   * Get all contacts for the current business
   */
  list(): Observable<Contact[]> {
    return this.http.get<Contact[]>(this.API_URL);
  }

  /**
   * Get a single contact by ID
   */
  get(id: string): Observable<Contact> {
    return this.http.get<Contact>(`${this.API_URL}/${id}`);
  }

  /**
   * Create a new contact
   */
  create(contact: Contact): Observable<Contact> {
    return this.http.post<Contact>(this.API_URL, contact);
  }

  /**
   * Update an existing contact
   */
  update(id: string, contact: Partial<Contact>): Observable<Contact> {
    return this.http.put<Contact>(`${this.API_URL}/${id}`, contact);
  }

  /**
   * Delete a contact (soft delete - sets status to 'deleted')
   */
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
