import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { QuotationItemRowComponent, QuotationItemRowValue } from '../quotation-item-row/quotation-item-row.component';
import { QuotationPreviewComponent } from '../quotation-preview/quotation-preview.component';

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
    QuotationItemRowComponent,
    QuotationPreviewComponent
  ],
  templateUrl: './quotation-form.component.html',
  styleUrl: './quotation-form.component.scss'
})
export class QuotationFormComponent {
  quotationId = signal<string>('QT-' + Math.floor(100000 + Math.random() * 900000));
  customerName = signal<string>('Rajesh Enterprises');
  customerPhone = signal<string>('+91 98765 43210');
  gstRate = signal<number>(18);

  items = signal<ItemRowModel[]>([
    { lineNumber: 1, itemDescription: 'Stainless steel fabrication work', quantity: 25, rate: 15000, unit: 'pcs' }
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
}
