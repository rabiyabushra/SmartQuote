import { Component, Input, computed, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { round2 } from '../../../../../core/utils/quotation-tax.util';
import { AuthService } from '../../../../../core/auth/auth.service';

@Component({
  selector: 'app-fabrication-quotation-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './fabrication-quotation-view.component.html',
  styleUrls: ['../../../quotation-form/quotation-document-shared.scss']
})
export class FabricationQuotationViewComponent {
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