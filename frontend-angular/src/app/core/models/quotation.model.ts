export type QuotationStatus =
  | 'Draft'
  | 'Sent to Customer'
  | 'Accepted'
  | 'In Negotiation'
  | 'Not Converted'
  | 'Converted';

export interface QuotationSummary {
  id: string;
  quoteId: string;               // e.g. QT-2026-0041
  customerName: string;
  contactNumber: string;
  email: string;
  status: QuotationStatus;
  employeeName: string;
  city: string;
  state: string;
  dateCreated: Date;
  followUpDate: Date | null;
  isFollowUpOverdue: boolean;
}

export interface DashboardStats {
  totalQuotations: number;
  draft: number;
  sent: number;
  accepted: number;
  inNegotiation: number;
  notConverted: number;
}
