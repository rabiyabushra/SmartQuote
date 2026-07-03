import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { BusinessProfile, QuotationTemplateOption } from '../../../core/models/business-profile.model';

type EditableField =
  | 'businessName' | 'ownerName'
  | 'email' | 'contactNumber' | 'gstin' | 'address'
  | 'termsAndConditions' | 'footerNotes';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatIconModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatTooltipModule,
    MatSnackBarModule
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent {
  private snackBar = inject(MatSnackBar);

  // TODO: replace with BusinessService.getProfile()
  profile = signal<BusinessProfile>({
    businessName: 'Sharma Fabrication Works',
    ownerName: 'Rajesh Sharma',
    logoUrl: null,
    email: 'rajesh@sharmafab.com',
    contactNumber: '+91 98765 43210',
    gstin: '27ABCDE1234F1Z5',
    address: 'Plot 14, MIDC Industrial Area, Pune, MH 411019',
    selectedTemplateId: 'template4',
    termsAndConditions: '1. Prices valid for 15 days.\n2. 50% advance required.\n3. GST as applicable.',
    footerNotes: 'Thank you for your business. For queries, contact us at the number above.'
  });

  templates: QuotationTemplateOption[] = [
    { id: 'template1', name: 'Template1', thumbnailUrl: 'assets/templates/template1-thumb.png' },
    { id: 'template2', name: 'Template2', thumbnailUrl: 'assets/templates/template2-thumb.png' },
    { id: 'template3', name: 'Template3', thumbnailUrl: 'assets/templates/template3-thumb.png' },
    { id: 'template4', name: 'Template4', thumbnailUrl: 'assets/templates/template4-thumb.png' },
    { id: 'template5', name: 'Template5', thumbnailUrl: 'assets/templates/template5-thumb.png' }
  ];

  /** Tracks which single field is currently in edit mode (null = none) */
  editingField = signal<EditableField | null>(null);

  /** Draft value used while a field is being edited, so Escape can revert cleanly */
  private draftValue = '';

  isEditing(field: EditableField): boolean {
    return this.editingField() === field;
  }

  startEdit(field: EditableField): void {
    this.draftValue = this.profile()[field] as string;
    this.editingField.set(field);
  }

  saveEdit(field: EditableField, value: string): void {
    this.profile.update(p => ({ ...p, [field]: value }));
    this.editingField.set(null);
    // TODO: BusinessService.updateProfile({ [field]: value })
    this.snackBar.open('Saved', undefined, { duration: 1500 });
  }

  cancelEdit(field: EditableField): void {
    this.profile.update(p => ({ ...p, [field]: this.draftValue }));
    this.editingField.set(null);
  }

  onLogoFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      this.profile.update(p => ({ ...p, logoUrl: reader.result as string }));
      // TODO: BusinessService.uploadLogo(file)
    };
    reader.readAsDataURL(file);
  }

  selectTemplate(templateId: string): void {
    this.profile.update(p => ({ ...p, selectedTemplateId: templateId }));
    // TODO: BusinessService.setDefaultTemplate(templateId)
    this.snackBar.open('Default template updated', undefined, { duration: 1500 });
  }

  openAccountSettings(): void {
    // TODO: route to account/security settings screen
    console.log('Open account settings');
  }
}
