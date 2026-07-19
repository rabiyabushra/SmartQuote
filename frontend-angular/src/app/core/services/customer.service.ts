import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Contact } from '../models/contact.model';

// Customer is an alias of Contact in the backend
export type Customer = Contact;

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiBaseUrl}/api/customers`;

  /**
   * Get all customers for the current business
   */
  list(): Observable<Customer[]> {
    return this.http.get<Customer[]>(this.API_URL);
  }

  /**
   * Get a single customer by ID
   */
  get(id: string): Observable<Customer> {
    return this.http.get<Customer>(`${this.API_URL}/${id}`);
  }

  /**
   * Create a new customer
   */
  create(customer: Customer): Observable<Customer> {
    return this.http.post<Customer>(this.API_URL, customer);
  }

  /**
   * Update an existing customer
   */
  update(id: string, customer: Partial<Customer>): Observable<Customer> {
    return this.http.put<Customer>(`${this.API_URL}/${id}`, customer);
  }

  /**
   * Delete a customer (soft delete - sets status to 'deleted')
   */
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
