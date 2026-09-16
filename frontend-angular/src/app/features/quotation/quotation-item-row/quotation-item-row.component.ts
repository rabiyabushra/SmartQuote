import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

export interface QuotationItemRowValue {
  lineNumber: number;
  itemDescription: string;
  quantity: number | null;
  rate: number | null;
  unit?: string;
}

@Component({
  selector: 'app-quotation-item-row',
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl: './quotation-item-row.component.html',
  styleUrl: './quotation-item-row.component.scss'
})
export class QuotationItemRowComponent {
  @Input({ required: true }) quotationId!: string;
  @Input({ required: true }) lineNumber!: number;
  @Input() itemDescription = '';
  @Input() quantity: number | null = null;
  @Input() rate: number | null = null;
  @Input() unit = 'pcs';
  @Input() removable = true;
  @Input() disabled = false;

  @Output() itemChange = new EventEmitter<QuotationItemRowValue>();
  @Output() remove = new EventEmitter<number>();

  descriptionError = '';
  quantityError = '';
  rateError = '';

  emitChange(): void {
    this.itemChange.emit({
      lineNumber: this.lineNumber,
      itemDescription: this.itemDescription,
      quantity: this.parseNumber(this.quantity),
      rate: this.parseNumber(this.rate),
      unit: this.unit
    });
  }

  onDescriptionVoice(value: string): void {
    this.descriptionError = '';
    this.itemDescription = value;
    this.emitChange();
  }

  onQuantityVoice(value: string): void {
    this.quantityError = '';
    this.quantity = this.parseNumber(value);
    this.emitChange();
  }

  onRateVoice(value: string): void {
    this.rateError = '';
    this.rate = this.parseNumber(value);
    this.emitChange();
  }

  onVoiceError(field: 'description' | 'quantity' | 'rate', message: string): void {
    if (field === 'description') {
      this.descriptionError = message;
      return;
    }
    if (field === 'quantity') {
      this.quantityError = message;
      return;
    }
    this.rateError = message;
  }

  onRemove(): void {
    this.remove.emit(this.lineNumber);
  }

  private parseNumber(value: number | string | null): number | null {
    if (value === null || value === undefined || value === '') {
      return null;
    }
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
  }
}
