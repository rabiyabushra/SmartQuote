import { QuotationStatus } from './quotation.model';

export type DateCreatedPreset = 'today' | 'thisWeek' | 'thisMonth' | 'custom';
export type FollowUpPreset = 'overdue' | 'thisWeek' | 'custom';

export interface QuotationFilters {
  customerName: string;
  statuses: QuotationStatus[];
  dateCreatedPreset: DateCreatedPreset | null;
  dateFrom: Date | null;
  dateTo: Date | null;
  sortBy: 'newest' | 'oldest' | 'followup';
  city: string;
  state: string;
  employeeIds: string[];
  followUpPreset: FollowUpPreset | null;
  followUpFrom: Date | null;
  followUpTo: Date | null;
}

export const EMPTY_FILTERS: QuotationFilters = {
  customerName: '',
  statuses: [],
  dateCreatedPreset: null,
  dateFrom: null,
  dateTo: null,
  sortBy: 'newest',
  city: '',
  state: '',
  employeeIds: [],
  followUpPreset: null,
  followUpFrom: null,
  followUpTo: null
};
