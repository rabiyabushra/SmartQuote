import { Component, Input, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { WhatsAppShareService, formatPhoneDigits } from '../../../core/services/whatsapp-share.service';

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
    MatProgressSpinnerModule
  ],
  templateUrl: './quotation-preview.component.html',
  styleUrl: './quotation-preview.component.scss'
})
export class QuotationPreviewComponent {
  private readonly whatsAppShareService = inject(WhatsAppShareService);

  @Input() quotationId = '';
  @Input() customerPhone = '';

  phone = '';
  pdfUrl = signal<string | null>(null);
  statusMessage = signal('');
  errorMessage = signal('');
  isGeneratingPdf = signal(false);
  isSharing = signal(false);

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
        this.statusMessage.set('PDF file downloaded to your computer & WhatsApp opened with clean chat box! Attach the downloaded PDF document directly.');
      }
    } catch (error) {
      this.errorMessage.set(
        error instanceof Error ? error.message : 'Unable to share via WhatsApp.'
      );
      this.statusMessage.set('');
    } finally {
      this.isSharing.set(false);
    }
  }
}
