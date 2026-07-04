import { Component, Input, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { calculateTax } from '../../../../../core/utils/quotation-tax.util';

@Component({
  selector: 'app-maintenance-quotation-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './maintenance-quotation-view.component.html',
  styleUrls: ['../../../quotation-form/quotation-document-shared.scss']
})
export class MaintenanceQuotationViewComponent {
  @Input() data: any;

  businessInfo = signal({
    businessName: 'Sharma Fabrication Works',
    streetAddress: 'Plot 14, MIDC Industrial Area',
    cityPostal: 'Pune, MH 411019',
    gstin: '27ABCDE1234F1Z5',
    contact: '+91 98765 43210 · info@sharmafab.com'
  });

  items = computed(() => this.data?.items ?? []);

  rowAmount(row: any): number {
    return (Number(row.qty) || 0) * (Number(row.unitRate) || 0);
  }

  taxSummary = computed(() => {
    const subtotal = this.items().reduce(
      (sum: number, row: any) => sum + (Number(row.qty) || 0) * (Number(row.unitRate) || 0), 0
    );
    return calculateTax(subtotal);
  });
}