import { Component, Input, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { calculateTax } from '../../../../../core/utils/quotation-tax.util';
import { AuthService } from '../../../../../core/auth/auth.service';

@Component({
  selector: 'app-supply-quotation-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './supply-quotation-view.component.html',
  styleUrls: ['../../../quotation-form/quotation-document-shared.scss']
})
export class SupplyQuotationViewComponent {
  @Input() data: any;

  private authService = inject(AuthService);

  businessInfo = computed(() => {
    const user = this.authService.currentUser();
    return {
      businessName: user?.businessName || '—',
      streetAddress: user?.address?.split(',')[0] || '—',
      cityPostal: user?.address?.split(',').slice(1).join(',').trim() || '—',
      gstin: user?.gstin || '—',
      contact: user?.phone || user?.email || '—'
    };
  });

  items = computed(() => this.data?.items ?? []);

  rowAmount(row: any): number {
    return (Number(row.qty) || 0) * (Number(row.unitPrice) || 0);
  }

  taxSummary = computed(() => {
    const subtotal = this.items().reduce(
      (sum: number, row: any) => sum + (Number(row.qty) || 0) * (Number(row.unitPrice) || 0), 0
    );
    return calculateTax(subtotal);
  });
}