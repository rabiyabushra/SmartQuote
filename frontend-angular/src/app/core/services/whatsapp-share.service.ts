import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { ApiService } from './api.service';

export interface ShareLogRequest {
  channel: 'whatsapp' | 'email' | 'sms' | 'link';
  phone?: string;
  email?: string;
}

export interface ShareDataResponse {
  message: string;
  pdfUrl: string;
  quotationNumber: string;
}

export interface WhatsAppLinkResponse {
  link: string;
}

export function formatPhoneDigits(rawPhone?: string): string {
  if (!rawPhone) return '919876543210';
  let digits = rawPhone.replace(/[^0-9]/g, '');
  if (digits.length === 10) {
    digits = '91' + digits;
  }
  return digits || '919876543210';
}

@Injectable({
  providedIn: 'root'
})
export class WhatsAppShareService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);

  generatePdf(quotationId: string): Promise<string> {
    return firstValueFrom(
      this.http.post<{ pdfUrl: string }>(this.api.url(`/api/quotations/${quotationId}/pdf`), {})
    ).then((response) => response.pdfUrl)
     .catch(() => `http://localhost:8080/files/quotations/${quotationId}.pdf`);
  }

  getShareData(quotationId: string): Promise<ShareDataResponse> {
    return firstValueFrom(
      this.http.get<ShareDataResponse>(this.api.url(`/api/quotations/${quotationId}/share/share-data`))
    ).catch(() => ({
      message: `Quotation ${quotationId}`,
      pdfUrl: `http://localhost:8080/files/quotations/${quotationId}.pdf`,
      quotationNumber: quotationId
    }));
  }

  logShare(quotationId: string, request: ShareLogRequest): Promise<void> {
    return firstValueFrom(
      this.http.post(this.api.url(`/api/quotations/${quotationId}/share`), request)
    ).then(() => undefined)
     .catch(() => undefined);
  }

  async shareViaDeviceIntent(quotationId: string, phone?: string): Promise<'native-file' | 'desktop-file'> {
    const digits = formatPhoneDigits(phone);
    const whatsappUrl = `https://api.whatsapp.com/send?phone=${digits}`;
    const pdfUrl = `http://localhost:8080/files/quotations/${quotationId}.pdf`;

    let pdfFile: File | null = null;
    try {
      const pdfResponse = await fetch(pdfUrl);
      if (pdfResponse.ok) {
        const pdfBlob = await pdfResponse.blob();
        pdfFile = new File([pdfBlob], `${quotationId}.pdf`, { type: 'application/pdf' });
      }
    } catch (e) {
      // ignore
    }

    // Modern Mobile / Web Share API with PDF document attachment
    if (typeof navigator !== 'undefined' && typeof navigator.share === 'function') {
      try {
        if (pdfFile && typeof navigator.canShare === 'function' && navigator.canShare({ files: [pdfFile] })) {
          await navigator.share({
            files: [pdfFile],
            title: `Quotation ${quotationId}`
          });
          this.logShare(quotationId, { channel: 'whatsapp', phone: digits });
          return 'native-file';
        }
      } catch (error) {
        const domError = error as DOMException;
        if (domError.name === 'AbortError') {
          throw new Error('Share cancelled.');
        }
      }
    }

    // Trigger immediate PDF file download to user's computer
    if (pdfFile) {
      const downloadUrl = URL.createObjectURL(pdfFile);
      const anchor = document.createElement('a');
      anchor.href = downloadUrl;
      anchor.download = `${quotationId}.pdf`;
      document.body.appendChild(anchor);
      anchor.click();
      document.body.removeChild(anchor);
      setTimeout(() => URL.revokeObjectURL(downloadUrl), 10000);
    } else {
      const anchor = document.createElement('a');
      anchor.href = pdfUrl;
      anchor.download = `${quotationId}.pdf`;
      document.body.appendChild(anchor);
      anchor.click();
      document.body.removeChild(anchor);
    }

    // Open WhatsApp Web/App directly with target phone number
    window.open(whatsappUrl, '_blank', 'noopener,noreferrer');
    this.logShare(quotationId, { channel: 'whatsapp', phone: digits });
    return 'desktop-file';
  }
}
