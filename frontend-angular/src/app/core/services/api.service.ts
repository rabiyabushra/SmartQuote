import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  readonly baseUrl = (environment.apiBaseUrl || '').replace(/\/api\/?$/, '').replace(/\/$/, '');

  url(path: string): string {
    let normalized = path.startsWith('/') ? path : `/${path}`;
    if (!normalized.startsWith('/api/') && normalized !== '/api' && !normalized.startsWith('/files/')) {
      normalized = `/api${normalized}`;
    }
    return `${this.baseUrl}${normalized}`;
  }
}
