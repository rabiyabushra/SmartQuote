import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatDialogModule } from '@angular/material/dialog';

import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatMenuModule } from '@angular/material/menu';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatBadgeModule } from '@angular/material/badge';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

import { DashboardStats, QuotationStatus, QuotationSummary } from '../../../core/models/quotation.model';
import { QuotationSearchFilterComponent } from '../../quotation/quotation-search-filter/quotation-search-filter.component';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';

import { MatDialog } from '@angular/material/dialog';
import { TemplatePickerDialogComponent } from '../../templates/template-gallery/template-picker-dialog/template-picker-dialog.component';

import { QuotationTemplateId } from '../../../core/models/quotation-template.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatMenuModule,
    MatChipsModule,
    MatButtonToggleModule,
    MatTooltipModule,
    MatBadgeModule,
    MatPaginatorModule,
    QuotationSearchFilterComponent,
    MatDatepickerModule,
    MatNativeDateModule,
    MatDialogModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private dialog = inject(MatDialog);

  view = signal<'card' | 'table'>('card');
  pageIndex = signal(0);
  pageSize = 6;
  totalCount = signal(128);
  showFilterDrawer = signal(false);
  searchControl = this.fb.control('');
  dateRange = this.fb.group({
    start: [null],
    end: [null]
  });

  activeFilters = signal<{ label: string; key: string }[]>([
    { label: 'Draft', key: 'status:Draft' },
    { label: 'John D.', key: 'employee:JohnD' },
    { label: 'Delhi', key: 'city:Delhi' }
  ]);

  stats = signal<DashboardStats>({
    totalQuotations: 128,
    draft: 34,
    sent: 47,
    accepted: 29,
    inNegotiation: 11,
    notConverted: 7
  });

  // TODO: replace with QuotationService.getQuotations(filters, page)
  quotations = signal<QuotationSummary[]>([
    {
      id: '1', quoteId: 'QT-2026-0041', customerName: 'Rajesh Mehta',
      contactNumber: '+91 98765 43210', email: 'rajesh@email.com',
      status: 'Draft', employeeName: 'Amit Sharma', city: 'Mumbai', state: 'MH',
      dateCreated: new Date('2026-03-01'), followUpDate: new Date('2026-03-08'), isFollowUpOverdue: true
    },
    {
      id: '2', quoteId: 'QT-2026-0040', customerName: 'Priya Nair',
      contactNumber: '+91 91234 56789', email: 'priya@email.com',
      status: 'Sent to Customer', employeeName: 'Sneha Iyer', city: 'Bangalore', state: 'KA',
      dateCreated: new Date('2026-02-28'), followUpDate: new Date('2026-03-10'), isFollowUpOverdue: false
    },
    {
      id: '3', quoteId: 'QT-2026-0039', customerName: 'Vikram Patel',
      contactNumber: '+91 87654 32109', email: 'vikram@email.com',
      status: 'In Negotiation', employeeName: 'Ravi Kumar', city: 'Delhi', state: 'DL',
      dateCreated: new Date('2026-02-25'), followUpDate: new Date('2026-03-05'), isFollowUpOverdue: true
    },
    {
      id: '4', quoteId: 'QT-2026-0038', customerName: 'Sunita Kapoor',
      contactNumber: '+91 76543 21098', email: 'sunita@email.com',
      status: 'Converted', employeeName: 'Deepa Rao', city: 'Hyderabad', state: 'TS',
      dateCreated: new Date('2026-02-22'), followUpDate: new Date('2026-03-01'), isFollowUpOverdue: false
    },
    {
      id: '5', quoteId: 'QT-2026-0037', customerName: 'Anand Verma',
      contactNumber: '+91 65432 10987', email: 'anand@email.com',
      status: 'Not Converted', employeeName: 'Amit Sharma', city: 'Chennai', state: 'TN',
      dateCreated: new Date('2026-02-20'), followUpDate: null, isFollowUpOverdue: false
    },
    {
      id: '6', quoteId: 'QT-2026-0036', customerName: 'Geeta Singh',
      contactNumber: '+91 54321 09876', email: 'geeta@email.com',
      status: 'Draft', employeeName: 'Sneha Iyer', city: 'Pune', state: 'MH',
      dateCreated: new Date('2026-02-18'), followUpDate: new Date('2026-03-06'), isFollowUpOverdue: true
    }
  ]);

  rangeStart = signal(1);
  rangeEnd = computed(() => Math.min(this.rangeStart() + this.pageSize - 1, this.totalCount()));

  statusColor(status: QuotationStatus): string {
    const map: Record<QuotationStatus, string> = {
      'Draft': 'status-draft',
      'Sent to Customer': 'status-sent',
      'Accepted': 'status-accepted',
      'In Negotiation': 'status-negotiation',
      'Not Converted': 'status-not-converted',
      'Converted': 'status-converted'
    };
    return map[status];
  }

  removeFilter(key: string): void {
    this.activeFilters.update(filters => filters.filter(f => f.key !== key));
    // TODO: re-fetch quotations with updated filters
  }

  clearAllFilters(): void {
    this.activeFilters.set([]);
    this.searchControl.setValue('');
    // TODO: re-fetch quotations with cleared filters
  }
openFilterDrawer(): void {
  this.showFilterDrawer.set(true);
}

closeFilterDrawer(): void {
  this.showFilterDrawer.set(false);
}

applyFilters(filters: any): void {
  console.log('Applied Filters:', filters);

  // Later this will call the backend
  // this.quotationService.getQuotations(filters);
}

  setView(mode: 'card' | 'table'): void {
    this.view.set(mode);
  }

 onNewQuotation(): void {
   const ref = this.dialog.open(TemplatePickerDialogComponent, { width: '600px' });
   ref.afterClosed().subscribe((templateId: QuotationTemplateId | undefined) => {
     if (templateId) {
       this.router.navigate(['/quotations/new', templateId]);
     }
   });
 }

  onViewQuotation(id: string): void {
    this.router.navigate(['/quotations', id, 'preview']);
  }

  onDownloadQuotation(id: string): void {
    // TODO: PdfService download hook
    console.log('Download', id);
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize = event.pageSize;
    this.rangeStart.set(event.pageIndex * event.pageSize + 1);
    // TODO: re-fetch quotations for the new page
  }
}
