import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Inventory } from '../models/inventory.model';

// Material is an alias of Inventory in the backend
export type Material = Inventory;

@Injectable({
  providedIn: 'root'
})
export class MaterialService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiBaseUrl}/api/materials`;

  /**
   * Get all materials for the current business
   */
  list(): Observable<Material[]> {
    return this.http.get<Material[]>(this.API_URL);
  }

  /**
   * Get a single material by ID
   */
  get(id: string): Observable<Material> {
    return this.http.get<Material>(`${this.API_URL}/${id}`);
  }

  /**
   * Create a new material
   */
  create(material: Material): Observable<Material> {
    return this.http.post<Material>(this.API_URL, material);
  }

  /**
   * Update an existing material
   */
  update(id: string, material: Partial<Material>): Observable<Material> {
    return this.http.put<Material>(`${this.API_URL}/${id}`, material);
  }

  /**
   * Delete a material (soft delete - sets status to 'deleted')
   */
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
