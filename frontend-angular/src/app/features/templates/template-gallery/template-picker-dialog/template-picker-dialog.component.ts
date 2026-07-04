import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

import { QUOTATION_TEMPLATES, QuotationTemplateId } from '../../../../core/models/quotation-template.model';

@Component({
  selector: 'app-template-picker-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatIconModule, MatButtonModule],
  templateUrl: './template-picker-dialog.component.html',
  styleUrl: './template-picker-dialog.component.scss'
})
export class TemplatePickerDialogComponent {
  private dialogRef = inject(MatDialogRef<TemplatePickerDialogComponent>);

  templates = QUOTATION_TEMPLATES;

  select(templateId: QuotationTemplateId): void {
    this.dialogRef.close(templateId);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
