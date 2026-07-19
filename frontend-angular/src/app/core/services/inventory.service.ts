import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Inventory } from '../models/inventory.model';

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiBaseUrl}/api/inventory`;

  /**
   * Get all inventory items for the current business
   */
  list(): Observable<Inventory[]> {
    return this.http.get<Inventory[]>(this.API_URL);
  }

  /**
   * Get a single inventory item by ID
   */
  get(id: string): Observable<Inventory> {
    return this.http.get<Inventory>(`${this.API_URL}/${id}`);
  }

  /**
   * Create a new inventory item
   */
  create(inventory: Inventory): Observable<Inventory> {
    return this.http.post<Inventory>(this.API_URL, inventory);
  }

  /**
   * Update an existing inventory item
   */
  update(id: string, inventory: Partial<Inventory>): Observable<Inventory> {
    return this.http.put<Inventory>(`${this.API_URL}/${id}`, inventory);
  }

  /**
   * Delete an inventory item (soft delete - sets status to 'deleted')
   */
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
