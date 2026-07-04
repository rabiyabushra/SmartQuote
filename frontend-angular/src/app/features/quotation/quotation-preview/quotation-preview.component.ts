import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';

import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

import { QuotationService } from '../../../core/services/quotation.service';
import { JobWorkQuotationViewComponent } from './views/job-work-quotation-view/job-work-quotation-view.component';
import { FabricationQuotationViewComponent } from './views/fabrication-quotation-view/fabrication-quotation-view.component';
import { SupplyQuotationViewComponent } from './views/supply-quotation-view/supply-quotation-view.component';
import { ServiceQuotationViewComponent } from './views/service-quotation-view/service-quotation-view.component';
import { MaintenanceQuotationViewComponent } from './views/maintenance-quotation-view/maintenance-quotation-view.component';

@Component({
  selector: 'app-quotation-preview',
  standalone: true,
  imports: [
    CommonModule, RouterLink, MatIconModule, MatButtonModule,
    JobWorkQuotationViewComponent, FabricationQuotationViewComponent,
    SupplyQuotationViewComponent, ServiceQuotationViewComponent, MaintenanceQuotationViewComponent
  ],
  templateUrl: './quotation-preview.component.html',
  styleUrl: './quotation-preview.component.scss'
})
export class QuotationPreviewComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private quotationService = inject(QuotationService);

  quotationId = toSignal(
    this.route.paramMap.pipe(map(p => p.get('id') ?? '')),
    { initialValue: this.route.snapshot.paramMap.get('id') ?? '' }
  );

  record = computed(() => this.quotationService.getById(this.quotationId()));

  onEdit(): void {
    this.router.navigate(['/quotations', this.quotationId(), 'edit']);
  }

  onDownload(): void {
    // TODO: PdfService.download(this.quotationId())
    console.log('Download PDF for', this.quotationId());
  }

  onBack(): void {
    this.router.navigate(['/quotations']);
  }
}