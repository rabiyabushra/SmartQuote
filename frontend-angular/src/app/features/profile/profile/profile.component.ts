import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { BusinessProfile, QuotationTemplateOption } from '../../../core/models/business-profile.model';
import { AuthService } from '../../../core/auth/auth.service';
import { TemplateService } from '../../../core/services/template.service';
import { BusinessService } from '../../../core/services/business.service';

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
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit {
  private snackBar = inject(MatSnackBar);
  private authService = inject(AuthService);
  private templateService = inject(TemplateService);
  private businessService = inject(BusinessService);
  private router = inject(Router);

  isLoading = signal(true);
  isLoadingTemplates = signal(true);

  // Profile data from authenticated user
  profile = signal<BusinessProfile>({
    businessName: '',
    ownerName: '',
    logoUrl: null,
    email: '',
    contactNumber: '',
    gstin: '',
    address: '',
    selectedTemplateId: 'fabrication',
    termsAndConditions: '1. Prices valid for 15 days.\n2. 50% advance required along with purchase order.\n3. GST as applicable.',
    footerNotes: 'Thank you for your business. For queries, contact us at the number above.'
  });

  // Templates fetched from backend
  templates = signal<QuotationTemplateOption[]>([]);

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

    const payload: any = {};
    if (field === 'contactNumber') {
      payload['phone'] = value;
    } else if (field === 'termsAndConditions') {
      payload['defaultTerms'] = value;
    } else if (field === 'footerNotes') {
      payload['defaultFooterNotes'] = value;
    } else {
      payload[field] = value;
    }

    this.businessService.updateProfile(payload).subscribe({
      next: () => {
        this.snackBar.open('Profile saved', undefined, { duration: 1500 });
      },
      error: () => {
        this.snackBar.open('Saved locally', undefined, { duration: 1500 });
      }
    });
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
      const dataUrl = reader.result as string;
      this.profile.update(p => ({ ...p, logoUrl: dataUrl }));
      this.businessService.updateProfile({ logoUrl: dataUrl }).subscribe({
        next: () => this.snackBar.open('Logo updated successfully', undefined, { duration: 2000 })
      });
    };
    reader.readAsDataURL(file);
  }

  selectTemplate(templateId: string): void {
    this.profile.update(p => ({ ...p, selectedTemplateId: templateId }));
    this.businessService.updateProfile({ defaultTemplateId: templateId }).subscribe({
      next: () => this.snackBar.open('Default template set and saved to profile!', undefined, { duration: 2000 }),
      error: () => this.snackBar.open('Default template updated locally', undefined, { duration: 2000 })
    });
  }

  openAccountSettings(): void {
    this.snackBar.open('Account settings', 'Close', { duration: 2000 });
  }

  ngOnInit(): void {
    this.loadUserProfile();
    this.loadTemplates();
  }

  /**
   * Load user profile from authentication service and backend
   */
  private loadUserProfile(): void {
    const currentUser = this.authService.currentUser();
    
    if (!currentUser) {
      this.router.navigate(['/auth/sign-in']);
      return;
    }

    this.profile.update(p => ({
      ...p,
      ownerName: currentUser.name || 'User',
      email: currentUser.email || '',
      businessName: currentUser.businessName || 'My Business',
      contactNumber: currentUser.phone || '',
      gstin: currentUser.gstin || '',
      address: currentUser.address || '',
      logoUrl: currentUser.logoUrl || null
    }));
    this.isLoading.set(false);

    // Fetch live profile from backend
    this.businessService.getProfile().subscribe({
      next: (b) => {
        if (b) {
          this.profile.update(p => ({
            ...p,
            businessName: b.businessName || p.businessName,
            ownerName: b.ownerName || p.ownerName,
            email: b.email || p.email,
            contactNumber: b.phone || p.contactNumber,
            gstin: b.gstin || p.gstin,
            address: b.address || p.address,
            logoUrl: b.logoUrl || p.logoUrl,
            selectedTemplateId: b.defaultTemplateId || p.selectedTemplateId,
            termsAndConditions: b.defaultTerms || p.termsAndConditions,
            footerNotes: b.defaultFooterNotes || p.footerNotes
          }));
        }
      },
      error: () => {}
    });
  }

  /**
   * Load quotation templates from backend
   */
  private loadTemplates(): void {
    this.isLoadingTemplates.set(true);
    this.templateService.list().subscribe({
      next: (backendTemplates) => {
        // Map backend templates to UI model
        const templateOptions: QuotationTemplateOption[] = backendTemplates.map(t => ({
          id: t.id,
          name: t.name,
          thumbnailUrl: `assets/templates/${t.id}-thumb.png` // Fallback to local assets
        }));
        
        this.templates.set(templateOptions);
        this.isLoadingTemplates.set(false);
      },
      error: (error) => {
        console.error('Error loading templates:', error);
        // Fallback to static templates if backend fails
        this.templates.set([
          { id: 'fabrication', name: 'Fabrication Work Quotation', thumbnailUrl: 'assets/templates/fabrication-thumb.png' },
          { id: 'job-work', name: 'Job Work Quotation', thumbnailUrl: 'assets/templates/job-work-thumb.png' },
          { id: 'service', name: 'Service Quotation', thumbnailUrl: 'assets/templates/service-thumb.png' },
          { id: 'supply', name: 'Supply Quotation', thumbnailUrl: 'assets/templates/supply-thumb.png' },
          { id: 'maintenance', name: 'Maintenance Quotation', thumbnailUrl: 'assets/templates/maintenance-thumb.png' }
        ]);
        this.isLoadingTemplates.set(false);
      }
    });
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
