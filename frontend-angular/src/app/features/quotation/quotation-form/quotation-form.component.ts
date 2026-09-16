import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { QuotationItemRowComponent, QuotationItemRowValue } from '../quotation-item-row/quotation-item-row.component';
import { QuotationPreviewComponent } from '../quotation-preview/quotation-preview.component';
import { VoiceQuotationMicComponent } from '../../../shared/components/voice-quotation-mic/voice-quotation-mic.component';
import { ParsedQuotationResponse } from '../../../core/models/parsed-quotation.model';

export interface ItemRowModel {
  lineNumber: number;
  itemDescription: string;
  quantity: number | null;
  rate: number | null;
  unit: string;
}

@Component({
  selector: 'app-quotation-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatChipsModule,
    QuotationItemRowComponent,
    QuotationPreviewComponent,
    VoiceQuotationMicComponent
  ],
  templateUrl: './quotation-form.component.html',
  styleUrl: './quotation-form.component.scss'
})
export class QuotationFormComponent {
  quotationId = signal<string>('QT-' + Math.floor(100000 + Math.random() * 900000));
  customerName = signal<string>('Ravi Traders');
  customerPhone = signal<string>('+91 98765 43210');
  gstRate = signal<number>(18);
  isReviewMode = signal<boolean>(false);
  parseErrorMessage = signal<string>('');

  items = signal<ItemRowModel[]>([
    { lineNumber: 1, itemDescription: 'HP LaserJet Printer', quantity: 2, rate: 12000, unit: 'pcs' },
    { lineNumber: 2, itemDescription: 'A4 Paper Box', quantity: 5, rate: 450, unit: 'box' }
  ]);

  subtotal = computed(() => {
    return this.items().reduce((sum, item) => {
      const qty = item.quantity ?? 0;
      const rate = item.rate ?? 0;
      return sum + (qty * rate);
    }, 0);
  });

  gstAmount = computed(() => {
    return (this.subtotal() * this.gstRate()) / 100;
  });

  grandTotal = computed(() => {
    return this.subtotal() + this.gstAmount();
  });

  onQuotationParsed(parsed: ParsedQuotationResponse): void {
    this.parseErrorMessage.set('');

    if (parsed.customerName) {
      this.customerName.set(parsed.customerName);
    }
    if (parsed.customerPhone) {
      this.customerPhone.set(parsed.customerPhone);
    }
    if (parsed.taxPercentage !== undefined && parsed.taxPercentage !== null) {
      this.gstRate.set(parsed.taxPercentage);
    }

    if (parsed.items && parsed.items.length > 0) {
      const newRows: ItemRowModel[] = parsed.items.map((item, idx) => ({
        lineNumber: idx + 1,
        itemDescription: item.itemDescription,
        quantity: item.quantity,
        rate: item.rate,
        unit: item.unit || 'pcs'
      }));
      this.items.set(newRows);
    }

    this.isReviewMode.set(true);
  }

  onParseError(error: string): void {
    this.parseErrorMessage.set(error);
  }

  addItemRow(): void {
    const current = this.items();
    const nextLineNumber = current.length > 0 ? Math.max(...current.map(i => i.lineNumber)) + 1 : 1;
    this.items.set([
      ...current,
      { lineNumber: nextLineNumber, itemDescription: '', quantity: null, rate: null, unit: 'pcs' }
    ]);
  }

  onItemChange(val: QuotationItemRowValue): void {
    this.items.update(rows =>
      rows.map(r => r.lineNumber === val.lineNumber ? { ...r, ...val } : r)
    );
  }

  onRemoveItem(lineNumber: number): void {
    this.items.update(rows => {
      const filtered = rows.filter(r => r.lineNumber !== lineNumber);
      return filtered.map((item, index) => ({ ...item, lineNumber: index + 1 }));
    });
  }

  toggleReviewMode(): void {
    this.isReviewMode.update(v => !v);
  }
}
