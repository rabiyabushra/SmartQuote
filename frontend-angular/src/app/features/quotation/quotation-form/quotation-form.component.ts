import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';

import { JobWorkQuotationFormComponent } from './templates/job-work-quotation-form/job-work-quotation-form.component';
import { FabricationQuotationFormComponent } from './templates/fabrication-quotation-form/fabrication-quotation-form.component';
import { SupplyQuotationFormComponent } from './templates/supply-quotation-form/supply-quotation-form.component';
import { ServiceQuotationFormComponent } from './templates/service-quotation-form/service-quotation-form.component';
import { MaintenanceQuotationFormComponent } from './templates/maintenance-quotation-form/maintenance-quotation-form.component';
import { QuotationTemplateId } from '../../../core/models/quotation-template.model';


@Component({
  selector: 'app-quotation-form',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    JobWorkQuotationFormComponent,
    FabricationQuotationFormComponent,
    SupplyQuotationFormComponent,
    ServiceQuotationFormComponent,
    MaintenanceQuotationFormComponent
  ],
  templateUrl: './quotation-form.component.html',
  styleUrl: './quotation-form.component.scss'
})
export class QuotationFormComponent {
  private route = inject(ActivatedRoute);

  templateId = toSignal(
    this.route.paramMap.pipe(map(params => params.get('templateId') as QuotationTemplateId | null)),
    { initialValue: this.route.snapshot.paramMap.get('templateId') as QuotationTemplateId | null }
  );
}
