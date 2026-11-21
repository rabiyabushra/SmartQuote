import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

import { CustomerService, Customer } from '../../../core/services/customer.service';
import { CustomerDialogComponent } from './customer-dialog.component';
import { debounceTime } from 'rxjs';

@Component({
  selector: 'app-customer-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatDialogModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './customer-list.component.html',
  styleUrl: './customer-list.component.scss'
})
export class CustomerListComponent implements OnInit {
  private customerService = inject(CustomerService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  customers = signal<Customer[]>([]);
  filteredCustomers = signal<Customer[]>([]);
  isLoading = signal(true);
  searchControl = new FormControl('');

  displayedColumns: string[] = ['companyName', 'contactPerson', 'phone', 'email', 'city', 'gstin', 'actions'];

  ngOnInit(): void {
    this.loadCustomers();
    
    // Setup search
    this.searchControl.valueChanges.pipe(
      debounceTime(300)
    ).subscribe(searchTerm => this.filterCustomers(searchTerm || ''));
  }

  loadCustomers(): void {
    this.isLoading.set(true);
    this.customerService.list().subscribe({
      next: (customers) => {
        this.customers.set(customers);
        this.filteredCustomers.set(customers);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.isLoading.set(false);
        const message = error.error?.message || 'Failed to load customers';
        this.snackBar.open(message, 'Close', { duration: 5000 });
        console.error('Error loading customers:', error);
      }
    });
  }

  filterCustomers(searchTerm: string): void {
    if (!searchTerm) {
      this.filteredCustomers.set(this.customers());
      return;
    }

    const term = searchTerm.toLowerCase();
    const filtered = this.customers().filter(customer =>
      customer.companyName.toLowerCase().includes(term) ||
      customer.contactPerson.toLowerCase().includes(term) ||
      customer.phone.includes(term) ||
      customer.email.toLowerCase().includes(term) ||
      customer.city.toLowerCase().includes(term)
    );
    this.filteredCustomers.set(filtered);
  }

  onAddCustomer(): void {
    const dialogRef = this.dialog.open(CustomerDialogComponent, {
      width: '560px',
      data: { customer: null }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.snackBar.open('Customer added successfully', 'Close', { duration: 3000 });
        this.loadCustomers();
      }
    });
  }

  onEditCustomer(customer: Customer): void {
    const dialogRef = this.dialog.open(CustomerDialogComponent, {
      width: '560px',
      data: { customer }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.snackBar.open('Customer updated successfully', 'Close', { duration: 3000 });
        this.loadCustomers();
      }
    });
  }

  onDeleteCustomer(customer: Customer): void {
    if (confirm(`Are you sure you want to delete ${customer.companyName}?`)) {
      this.customerService.delete(customer.id!).subscribe({
        next: () => {
          this.snackBar.open('Customer deleted successfully', 'Close', { duration: 3000 });
          this.loadCustomers();
        },
        error: (error) => {
          const message = error.error?.message || 'Failed to delete customer';
          this.snackBar.open(message, 'Close', { duration: 5000 });
        }
      });
    }
  }
}

