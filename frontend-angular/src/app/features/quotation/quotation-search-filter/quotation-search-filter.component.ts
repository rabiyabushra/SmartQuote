import { Component, EventEmitter, Input, Output, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatBadgeModule } from '@angular/material/badge';

import {
  DateCreatedPreset,
  EMPTY_FILTERS,
  FollowUpPreset,
  QuotationFilters
} from '../../../core/models/quotation-filter.model';
import { QuotationStatus } from '../../../core/models/quotation.model';

interface EmployeeOption {
  id: string;
  name: string;
}

@Component({
  selector: 'app-quotation-search-filter',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatIconModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatChipsModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatBadgeModule
  ],
  templateUrl: './quotation-search-filter.component.html',
  styleUrl: './quotation-search-filter.component.scss'
})
export class QuotationSearchFilterComponent {
  /** Whether the drawer is open — parent (dashboard) controls this */
  @Input() isOpen = false;
  @Output() closed = new EventEmitter<void>();
  @Output() apply = new EventEmitter<QuotationFilters>();

  readonly statusOptions: QuotationStatus[] = [
    'Draft', 'Sent to Customer', 'In Negotiation', 'Converted', 'Not Converted'
  ];

  readonly stateOptions = ['MH', 'KA', 'DL', 'TN', 'TS'];

  // TODO: replace with EmployeeService.getEmployees()
  readonly employeeOptions: EmployeeOption[] = [
    { id: 'e1', name: 'Amit Sharma' },
    { id: 'e2', name: 'Sneha Iyer' },
    { id: 'e3', name: 'Ravi Kumar' }
  ];

  filters = signal<QuotationFilters>({ ...EMPTY_FILTERS, statuses: ['In Negotiation'] });

  activeFilterCount = computed(() => {
    const f = this.filters();
    let count = 0;
    if (f.customerName) count++;
    if (f.statuses.length) count++;
    if (f.dateCreatedPreset) count++;
    if (f.city || f.state) count++;
    if (f.employeeIds.length) count++;
    if (f.followUpPreset) count++;
    return count;
  });

  // ---------- Field update helpers (template can't use arrow functions) ----------
  updateCustomerName(value: string): void {
    this.filters.update(f => ({ ...f, customerName: value }));
  }

  updateSortBy(value: 'newest' | 'oldest' | 'followup'): void {
    this.filters.update(f => ({ ...f, sortBy: value }));
  }

  updateDateFrom(date: Date | null): void {
    this.filters.update(f => ({ ...f, dateFrom: date }));
  }

  updateDateTo(date: Date | null): void {
    this.filters.update(f => ({ ...f, dateTo: date }));
  }

  updateCity(value: string): void {
    this.filters.update(f => ({ ...f, city: value }));
  }

  updateState(value: string): void {
    this.filters.update(f => ({ ...f, state: value }));
  }

  updateFollowUpFrom(date: Date | null): void {
    this.filters.update(f => ({ ...f, followUpFrom: date }));
  }

  updateFollowUpTo(date: Date | null): void {
    this.filters.update(f => ({ ...f, followUpTo: date }));
  }

  // ---------- Status (multi-select toggle) ----------
  toggleStatus(status: QuotationStatus): void {
    this.filters.update(f => {
      const statuses = f.statuses.includes(status)
        ? f.statuses.filter(s => s !== status)
        : [...f.statuses, status];
      return { ...f, statuses };
    });
  }

  isStatusSelected(status: QuotationStatus): boolean {
    return this.filters().statuses.includes(status);
  }

  // ---------- Date created preset ----------
  setDateCreatedPreset(preset: DateCreatedPreset): void {
    this.filters.update(f => ({ ...f, dateCreatedPreset: preset }));
  }

  // ---------- City / State quick select ----------
  quickSelectState(state: string): void {
    this.filters.update(f => ({ ...f, state }));
  }

  // ---------- Employee multi-select ----------
  toggleEmployee(employeeId: string): void {
    this.filters.update(f => {
      const employeeIds = f.employeeIds.includes(employeeId)
        ? f.employeeIds.filter(id => id !== employeeId)
        : [...f.employeeIds, employeeId];
      return { ...f, employeeIds };
    });
  }

  isEmployeeSelected(employeeId: string): boolean {
    return this.filters().employeeIds.includes(employeeId);
  }

  employeeName(employeeId: string): string {
    return this.employeeOptions.find(e => e.id === employeeId)?.name ?? '';
  }

  // ---------- Follow-up preset ----------
  setFollowUpPreset(preset: FollowUpPreset): void {
    this.filters.update(f => ({ ...f, followUpPreset: preset }));
  }

  // ---------- Actions ----------
  onClearFilters(): void {
    this.filters.set({ ...EMPTY_FILTERS });
  }

  onClose(): void {
    this.closed.emit();
  }

  onApply(): void {
    this.apply.emit(this.filters());
    this.closed.emit();
  }
}
