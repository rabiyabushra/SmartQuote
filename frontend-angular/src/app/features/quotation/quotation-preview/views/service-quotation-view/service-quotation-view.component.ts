import { Component, Input, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { calculateTax, round2 } from '../../../../../core/utils/quotation-tax.util';
import { AuthService } from '../../../../../core/auth/auth.service';

@Component({
  selector: 'app-service-quotation-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './service-quotation-view.component.html',
  styleUrls: ['../../../quotation-form/quotation-document-shared.scss']
})
export class ServiceQuotationViewComponent {
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
    return (Number(row.resources) || 0) * (Number(row.rate) || 0);
  }

  taxSummary = computed(() => {
    const subtotal = this.items().reduce(
      (sum: number, row: any) => sum + (Number(row.resources) || 0) * (Number(row.rate) || 0), 0
    );
    return calculateTax(subtotal);
  });

  balanceDue = computed(() => {
    const t = this.taxSummary();
    const discount = Number(this.data?.discount) || 0;
    const advance = Number(this.data?.advance) || 0;
    return round2(t.total - discount - advance);
  });
}