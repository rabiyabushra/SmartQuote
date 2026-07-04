import { Component, Input, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { round2 } from '../../../../../core/utils/quotation-tax.util';

@Component({
  selector: 'app-fabrication-quotation-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './fabrication-quotation-view.component.html',
  styleUrls: ['../../../quotation-form/quotation-document-shared.scss']
})
export class FabricationQuotationViewComponent {
  @Input() data: any;

  businessInfo = signal({
    businessName: 'Sharma Fabrication Works',
    streetAddress: 'Plot 14, MIDC Industrial Area',
    cityPostal: 'Pune, MH 411019',
    gstin: '27ABCDE1234F1Z5',
    contact: '+91 98765 43210 · info@sharmafab.com'
  });

  materialItems = computed(() => this.data?.materialItems ?? []);
  laborItems = computed(() => this.data?.laborItems ?? []);

  rowAmount(row: any): number {
    const base = (Number(row.qty) || 0) * (Number(row.price) || 0);
    return round2(base * (1 + (Number(row.cgstPercent) || 0) / 100));
  }

  materialTotal = computed(() =>
    round2(this.materialItems().reduce((sum: number, row: any) => sum + this.rowAmount(row), 0))
  );

  laborTotal = computed(() =>
    round2(this.laborItems().reduce((sum: number, row: any) => sum + this.rowAmount(row), 0))
  );

  grandTotal = computed(() => round2(this.materialTotal() + this.laborTotal()));
}