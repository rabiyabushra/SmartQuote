import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatDividerModule } from '@angular/material/divider';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime } from 'rxjs';

import { QuotationService } from '../../../core/services/quotation.service';
import { ContactService } from '../../../core/services/contact.service';
import { AuthService } from '../../../core/auth/auth.service';
import { QuotationResponse } from '../../../core/models/quotation-backend.model';

interface QuotationListItem extends QuotationResponse {
  customerName?: string;
  customerPhone?: string;
}

@Component({
  selector: 'app-quotation-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatChipsModule,
    MatMenuModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatDividerModule
  ],
  templateUrl: './quotation-list.component.html',
  styleUrl: './quotation-list.component.scss'
})
export class QuotationListComponent implements OnInit {
  private quotationService = inject(QuotationService);
  private contactService = inject(ContactService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  quotations = signal<QuotationListItem[]>([]);
  filteredQuotations = signal<QuotationListItem[]>([]);
  isLoading = signal(true);
  
  searchControl = new FormControl('');
  statusFilter = new FormControl('all');

  displayedColumns: string[] = ['quotationNumber', 'customer', 'quotationDate', 'dueDate', 'status', 'grandTotal', 'actions'];

  ngOnInit(): void {
    this.loadQuotations();
    
    // Setup search
    this.searchControl.valueChanges.pipe(
      debounceTime(300)
    ).subscribe(() => this.applyFilters());

    // Setup status filter
    this.statusFilter.valueChanges.subscribe(() => this.applyFilters());
  }

  loadQuotations(): void {
    this.isLoading.set(true);
    
    this.quotationService.list().subscribe({
      next: (quotations) => {
        // Load contacts to enrich quotation data
        this.contactService.list().subscribe({
          next: (contacts) => {
            const contactMap = new Map(contacts.map(c => [c.id!, c]));
            
            const enrichedQuotations: QuotationListItem[] = quotations.map(q => {
              const contact = contactMap.get(q.contactId);
              return {
                ...q,
                customerName: contact?.companyName || contact?.contactPerson || 'Unknown',
                customerPhone: contact?.phone || '—'
              };
            });

            this.quotations.set(enrichedQuotations);
            this.filteredQuotations.set(enrichedQuotations);
            this.isLoading.set(false);
          },
          error: () => {
            // Even if contacts fail, show quotations
            const enrichedQuotations: QuotationListItem[] = quotations.map(q => ({
              ...q,
              customerName: 'Unknown',
              customerPhone: '—'
            }));
            this.quotations.set(enrichedQuotations);
            this.filteredQuotations.set(enrichedQuotations);
            this.isLoading.set(false);
          }
        });
      },
      error: (error) => {
        this.isLoading.set(false);
        const message = error.error?.message || 'Failed to load quotations';
        this.snackBar.open(message, 'Close', { duration: 5000 });
        console.error('Error loading quotations:', error);
      }
    });
  }

  applyFilters(): void {
    let filtered = this.quotations();

    // Apply search filter
    const searchTerm = this.searchControl.value?.toLowerCase() || '';
    if (searchTerm) {
      filtered = filtered.filter(q =>
        q.quotationNumber.toLowerCase().includes(searchTerm) ||
        q.customerName?.toLowerCase().includes(searchTerm) ||
        q.status.toLowerCase().includes(searchTerm)
      );
    }

    // Apply status filter
    const status = this.statusFilter.value;
    if (status && status !== 'all') {
      filtered = filtered.filter(q => q.status === status);
    }

    this.filteredQuotations.set(filtered);
  }

  getStatusClass(status: string): string {
    const statusMap: Record<string, string> = {
      'draft': 'status-draft',
      'sent': 'status-sent',
      'accepted': 'status-accepted',
      'rejected': 'status-rejected',
      'expired': 'status-expired'
    };
    return statusMap[status.toLowerCase()] || 'status-draft';
  }

  onViewQuotation(id: string): void {
    this.router.navigate(['/quotations', id, 'preview']);
  }

  onEditQuotation(id: string): void {
    this.router.navigate(['/quotations', id, 'edit']);
  }

  onDownloadPdf(id: string): void {
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

  onDeleteQuotation(id: string, quotationNumber: string): void {
    if (confirm(`Are you sure you want to delete quotation ${quotationNumber}?`)) {
      this.quotationService.delete(id).subscribe({
        next: () => {
          this.snackBar.open('Quotation deleted successfully', 'Close', { duration: 3000 });
          this.loadQuotations();
        },
        error: (error) => {
          const message = error.error?.message || 'Failed to delete quotation';
          this.snackBar.open(message, 'Close', { duration: 5000 });
        }
      });
    }
  }

  onNewQuotation(): void {
    this.router.navigate(['/quotations/new']);
  }
}
