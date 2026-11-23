import { Component, inject, signal, computed, OnInit } from '@angular/core';
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
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { DashboardStats, QuotationStatus, QuotationSummary } from '../../../core/models/quotation.model';
import { QuotationSearchFilterComponent } from '../../quotation/quotation-search-filter/quotation-search-filter.component';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';

import { MatDialog } from '@angular/material/dialog';
import { TemplatePickerDialogComponent } from '../../templates/template-gallery/template-picker-dialog/template-picker-dialog.component';

import { QuotationTemplateId } from '../../../core/models/quotation-template.model';

import { QuotationService } from '../../../core/services/quotation.service';
import { ContactService } from '../../../core/services/contact.service';
import { AuthService } from '../../../core/auth/auth.service';
import { forkJoin } from 'rxjs';


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
    MatProgressSpinnerModule,
    MatSnackBarModule,
    QuotationSearchFilterComponent,
    MatDatepickerModule,
    MatNativeDateModule,
    MatDialogModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private quotationService = inject(QuotationService);
  private contactService = inject(ContactService);
  private authService = inject(AuthService);

  // Loading state
  isLoading = signal(true);

  view = signal<'card' | 'table'>('card');
  pageIndex = signal(0);
  pageSize = 6;
  showFilterDrawer = signal(false);
  searchControl = this.fb.control('');
  searchTerm = signal('');
  dateRange = this.fb.group({
    start: [null],
    end: [null]
  });

  activeFilters = signal<{ label: string; key: string }[]>([]);

  stats = signal<DashboardStats>({
    totalQuotations: 0,
    draft: 0,
    sent: 0,
    accepted: 0,
    inNegotiation: 0,
    notConverted: 0
  });

  // All quotations from backend
  private allQuotations = signal<QuotationSummary[]>([]);
  
  // Filtered quotations based on search input and active filters
  filteredQuotations = computed(() => {
    const term = this.searchTerm().toLowerCase().trim();
    let list = this.allQuotations();
    if (term) {
      list = list.filter(q =>
        q.customerName.toLowerCase().includes(term) ||
        q.quoteId.toLowerCase().includes(term) ||
        q.status.toLowerCase().includes(term) ||
        (q.city && q.city.toLowerCase().includes(term)) ||
        (q.state && q.state.toLowerCase().includes(term))
      );
    }
    return list;
  });

  totalCount = computed(() => this.filteredQuotations().length);

  // Paginated quotations for display
  quotations = computed(() => {
    const start = this.pageIndex() * this.pageSize;
    const end = start + this.pageSize;
    return this.filteredQuotations().slice(start, end);
  });

  rangeStart = computed(() => this.totalCount() === 0 ? 0 : this.pageIndex() * this.pageSize + 1);
  rangeEnd = computed(() => Math.min((this.pageIndex() + 1) * this.pageSize, this.totalCount()));

  ngOnInit(): void {
    this.searchControl.valueChanges.subscribe(val => {
      this.searchTerm.set(val || '');
      this.pageIndex.set(0);
    });
    this.loadQuotations();
  }

  /**
   * Load quotations from backend for the current signed-in user
   */
  loadQuotations(): void {
    this.isLoading.set(true);

    // Load quotations from backend
    this.quotationService.list().subscribe({
      next: (backendQuotations) => {
        // Sort newest first so freshly created quotations immediately show at top
        const sortedQuotations = [...backendQuotations].sort((a, b) => {
          const dateA = a.createdOn ? new Date(a.createdOn).getTime() : 0;
          const dateB = b.createdOn ? new Date(b.createdOn).getTime() : 0;
          return dateB - dateA;
        });

        // Load contacts to get customer information
        this.contactService.list().subscribe({
          next: (contacts) => {
            const contactMap = new Map(contacts.map(c => [c.id!, c]));

            const quotationSummaries: QuotationSummary[] = sortedQuotations.map(q => {
              const contact = contactMap.get(q.contactId);
              
              return {
                id: q.id,
                quoteId: q.quotationNumber,
                customerName: q.customerName || contact?.companyName || contact?.contactPerson || 'Valued Customer',
                contactNumber: q.customerPhone || contact?.phone || '—',
                email: contact?.email || '—',
                status: this.mapBackendStatus(q.status),
                employeeName: this.authService.currentUser()?.name || 'You',
                city: contact?.city || '—',
                state: contact?.state || '—',
                dateCreated: new Date(q.createdOn),
                followUpDate: q.dueDate ? new Date(q.dueDate) : null,
                isFollowUpOverdue: q.dueDate ? new Date(q.dueDate) < new Date() : false
              };
            });

            this.allQuotations.set(quotationSummaries);
            this.calculateStats(quotationSummaries);
            this.isLoading.set(false);
          },
          error: (error) => {
            console.error('Error loading contacts:', error);
            const quotationSummaries: QuotationSummary[] = sortedQuotations.map(q => ({
              id: q.id,
              quoteId: q.quotationNumber,
              customerName: q.customerName || 'Valued Customer',
              contactNumber: q.customerPhone || '—',
              email: '—',
              status: this.mapBackendStatus(q.status),
              employeeName: this.authService.currentUser()?.name || 'You',
              city: '—',
              state: '—',
              dateCreated: new Date(q.createdOn),
              followUpDate: q.dueDate ? new Date(q.dueDate) : null,
              isFollowUpOverdue: q.dueDate ? new Date(q.dueDate) < new Date() : false
            }));

            this.allQuotations.set(quotationSummaries);
            this.calculateStats(quotationSummaries);
            this.isLoading.set(false);
          }
        });
      },
      error: (error) => {
        this.isLoading.set(false);
        const message = error.error?.message || 'Failed to load quotations. Please try again.';
        this.snackBar.open(message, 'Close', { duration: 5000 });
        console.error('Error loading quotations:', error);
      }
    });
  }

  /**
   * Map backend status to UI status
   */
  private mapBackendStatus(backendStatus: string): QuotationStatus {
    const statusMap: Record<string, QuotationStatus> = {
      'draft': 'Draft',
      'sent': 'Sent to Customer',
      'accepted': 'Accepted',
      'rejected': 'Not Converted',
      'expired': 'Not Converted'
    };
    return statusMap[backendStatus.toLowerCase()] || 'Draft';
  }

  /**
   * Calculate dashboard statistics
   */
  private calculateStats(quotations: QuotationSummary[]): void {
    const stats: DashboardStats = {
      totalQuotations: quotations.length,
      draft: quotations.filter(q => q.status === 'Draft').length,
      sent: quotations.filter(q => q.status === 'Sent to Customer').length,
      accepted: quotations.filter(q => q.status === 'Accepted').length,
      inNegotiation: quotations.filter(q => q.status === 'In Negotiation').length,
      notConverted: quotations.filter(q => q.status === 'Not Converted').length
    };
    this.stats.set(stats);
  }

  /**
   * Refresh quotations from backend
   */
  refreshQuotations(): void {
    this.pageIndex.set(0);
    this.loadQuotations();
  }

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
    this.applyClientSideFilters();
  }

  clearAllFilters(): void {
    this.activeFilters.set([]);
    this.searchControl.setValue('');
    this.loadQuotations(); // Reload from backend
  }

  /**
   * Apply client-side filters (can be replaced with backend filtering later)
   */
  private applyClientSideFilters(): void {
    // TODO: Implement client-side filtering or backend API filtering
    // For now, just reload
    this.loadQuotations();
  }
openFilterDrawer(): void {
  this.showFilterDrawer.set(true);
}

closeFilterDrawer(): void {
  this.showFilterDrawer.set(false);
}

applyFilters(filters: any): void {
  console.log('Applied Filters:', filters);
  
  // Build active filters display
  const activeFilters: { label: string; key: string }[] = [];
  
  if (filters.status && filters.status.length > 0) {
    filters.status.forEach((status: string) => {
      activeFilters.push({ label: status, key: `status:${status}` });
    });
  }
  
  if (filters.city) {
    activeFilters.push({ label: filters.city, key: `city:${filters.city}` });
  }
  
  if (filters.state) {
    activeFilters.push({ label: filters.state, key: `state:${filters.state}` });
  }
  
  this.activeFilters.set(activeFilters);
  this.applyClientSideFilters();
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

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize = event.pageSize;
    // Quotations will automatically update via computed signal
  }

  onDownloadQuotation(id: string): void {
    // Generate PDF and download
    this.quotationService.generatePdf(id).subscribe({
      next: (response) => {
        window.open(response.pdfUrl, '_blank');
        this.snackBar.open('PDF generated successfully', 'Close', { duration: 3000 });
      },
      error: (error) => {
        const message = error.error?.message || 'Failed to generate PDF';
        this.snackBar.open(message, 'Close', { duration: 5000 });
      }
    });
  }

  onUpdateStatus(id: string, newStatus: string): void {
    this.quotationService.updateStatus(id, newStatus as QuotationStatus).subscribe({
      next: () => {
        this.snackBar.open(`Status updated to ${newStatus}`, 'Close', { duration: 3000 });
        this.loadQuotations();
      },
      error: (err) => {
        const msg = err.error?.message || 'Failed to update status.';
        this.snackBar.open(msg, 'Close', { duration: 4000 });
      }
    });
  }

  onDeleteQuotation(id: string): void {
    if (confirm('Are you sure you want to delete this quotation?')) {
      this.quotationService.delete(id).subscribe({
        next: () => {
          this.snackBar.open('Quotation deleted successfully.', 'Close', { duration: 3000 });
          this.loadQuotations();
        },
        error: (err) => {
          const msg = err.error?.message || 'Failed to delete quotation.';
          this.snackBar.open(msg, 'Close', { duration: 4000 });
        }
      });
    }
  }

  onShareQuotation(q: QuotationSummary): void {
    const text = encodeURIComponent(`Quotation ${q.quoteId} for ${q.customerName} is ready. View details at: ${window.location.origin}/quotations/${q.id}/preview`);
    const phone = q.contactNumber ? q.contactNumber.replace(/[^0-9]/g, '') : '';
    const whatsappUrl = phone.length >= 10
      ? `https://wa.me/${phone}?text=${text}`
      : `https://api.whatsapp.com/send?text=${text}`;
    window.open(whatsappUrl, '_blank');
  }

  /**
   * Sign out user
   */
  signOut(): void {
    this.authService.signOut().subscribe({
      next: () => {
        this.snackBar.open('Signed out successfully', 'Close', { duration: 3000 });
        this.router.navigate(['/auth/sign-in']);
      },
      error: (error) => {
        console.error('Error signing out:', error);
        // Even if API fails, clear local data and redirect
        this.authService.clearAuthData();
        this.router.navigate(['/auth/sign-in']);
      }
    });
  }
}
