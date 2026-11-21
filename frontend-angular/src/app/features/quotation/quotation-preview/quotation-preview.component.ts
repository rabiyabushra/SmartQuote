import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { WhatsAppShareService } from '../../../core/services/whatsapp-share.service';
import { QuotationService } from '../../../core/services/quotation.service';
import { QuotationResponse } from '../../../core/models/quotation-backend.model';

@Component({
  selector: 'app-quotation-preview',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatCardModule,
    MatDividerModule,
    MatSnackBarModule
  ],
  templateUrl: './quotation-preview.component.html',
  styleUrl: './quotation-preview.component.scss'
})
export class QuotationPreviewComponent implements OnInit {
  private readonly whatsAppShareService = inject(WhatsAppShareService);
  private readonly quotationService = inject(QuotationService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  @Input() id = '';
  @Input() quotationId = '';
  @Input() customerPhone = '';

  phone = '';
  quotation = signal<QuotationResponse | null>(null);
  pdfUrl = signal<string | null>(null);
  statusMessage = signal('');
  errorMessage = signal('');
  isGeneratingPdf = signal(false);
  isSharing = signal(false);
  isLoading = signal(false);

  ngOnInit(): void {
    if (!this.quotationId && this.id) {
      this.quotationId = this.id;
    }

    if (this.quotationId) {
      this.loadQuotationDetails();
    }
  }

  loadQuotationDetails(): void {
    this.isLoading.set(true);
    this.quotationService.get(this.quotationId).subscribe({
      next: (q) => {
        this.quotation.set(q);
        if (q.pdfUrl) {
          this.pdfUrl.set(q.pdfUrl);
        }
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  async generatePdf(): Promise<void> {
    if (!this.quotationId) {
      this.errorMessage.set('Quotation ID is required.');
      return;
    }

    this.isGeneratingPdf.set(true);
    this.errorMessage.set('');
    this.statusMessage.set('Generating PDF...');

    try {
      const url = await this.whatsAppShareService.generatePdf(this.quotationId);
      this.pdfUrl.set(url);
      this.statusMessage.set('PDF generated successfully.');
      this.snackBar.open('PDF generated successfully', 'Close', { duration: 3000 });
    } catch (error) {
      this.errorMessage.set(
        error instanceof Error ? error.message : 'Unable to generate PDF.'
      );
      this.statusMessage.set('');
    } finally {
      this.isGeneratingPdf.set(false);
    }
  }

  downloadPdfFile(): void {
    const id = this.quotationId || 'QT-319659';
    const link = document.createElement('a');
    link.href = `http://localhost:8080/files/quotations/${id}.pdf`;
    link.download = `${id}.pdf`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    this.statusMessage.set('PDF downloaded to your computer.');
  }

  async shareViaWhatsApp(): Promise<void> {
    if (!this.quotationId) {
      this.errorMessage.set('Quotation ID is required.');
      return;
    }

    this.isSharing.set(true);
    this.errorMessage.set('');
    this.statusMessage.set('Opening WhatsApp & saving PDF...');

    try {
      if (!this.pdfUrl()) {
        const url = await this.whatsAppShareService.generatePdf(this.quotationId);
        this.pdfUrl.set(url);
      }

      const phoneNum = this.phone || this.customerPhone;
      const mode = await this.whatsAppShareService.shareViaDeviceIntent(this.quotationId, phoneNum);
      
      if (mode === 'native-file') {
        this.statusMessage.set('PDF document file attached! Select WhatsApp to send.');
      } else {
        this.statusMessage.set('PDF file downloaded & WhatsApp opened! Attach the downloaded PDF document directly.');
      }

      // Automatically update status to 'sent'
      this.updateStatus('sent');
    } catch (error) {
      this.errorMessage.set(
        error instanceof Error ? error.message : 'Unable to share via WhatsApp.'
      );
      this.statusMessage.set('');
    } finally {
      this.isSharing.set(false);
    }
  }

  updateStatus(newStatus: 'draft' | 'sent' | 'accepted' | 'rejected'): void {
    if (!this.quotationId) return;

    this.quotationService.updateStatus(this.quotationId, newStatus as any).subscribe({
      next: (updated) => {
        if (this.quotation()) {
          this.quotation.update(q => q ? { ...q, status: newStatus } : null);
        }
        this.snackBar.open(`Quotation status updated to ${newStatus.toUpperCase()}`, 'Close', { duration: 3000 });
      },
      error: (err) => {
        console.error('Failed to update status', err);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/quotations']);
  }
}
