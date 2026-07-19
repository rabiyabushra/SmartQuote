import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { TemplateService, QuotationTemplate } from '../../../core/services/template.service';

@Component({
  selector: 'app-template-gallery',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './template-gallery.component.html',
  styleUrl: './template-gallery.component.scss'
})
export class TemplateGalleryComponent implements OnInit {
  private templateService = inject(TemplateService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  templates = signal<QuotationTemplate[]>([]);
  isLoading = signal(true);

  ngOnInit(): void {
    this.loadTemplates();
  }

  private loadTemplates(): void {
    this.isLoading.set(true);
    this.templateService.list().subscribe({
      next: (templates) => {
        this.templates.set(templates);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.isLoading.set(false);
        const message = error.error?.message || 'Failed to load templates';
        this.snackBar.open(message, 'Close', { duration: 5000 });
        console.error('Error loading templates:', error);
      }
    });
  }

  onSelectTemplate(template: QuotationTemplate): void {
    // Navigate to create new quotation with this template
    this.router.navigate(['/quotations/new', template.id]);
  }

  onPreviewTemplate(template: QuotationTemplate): void {
    // TODO: Open template preview dialog or navigate to preview page
    this.snackBar.open(`Preview for ${template.name} not yet implemented`, 'Close', { duration: 3000 });
  }

  getTemplateThumbnail(templateId: string): string {
    // Return thumbnail URL - either from backend or fallback to assets
    return `assets/templates/${templateId}-thumb.png`;
  }
}
