// Backend Quotation models matching Spring Boot DTOs

export interface QuotationItemDto {
  lineNumber: number;
  itemId?: string;
  itemDescription: string;
  hsnSacCode?: string;
  quantity: number;
  unit: string;
  rate: number;
  taxPercentage: number;
}

export interface QuotationRequest {
  businessId?: string;
  contactId?: string;
  customerName?: string;
  customerPhone?: string;
  templateId?: string;
  bankId?: string;
  quotationDate?: string; // ISO date string
  dueDate?: string; // ISO date string
  priority?: string;
  billingAddress?: string;
  deliveryAddress?: string;
  discountPercentage?: number;
  items: QuotationItemDto[];
}

export interface QuotationResponse {
  id: string;
  quotationNumber: string;
  businessId: string;
  contactId: string;
  templateId?: string;
  bankId?: string;
  quotationDate: string;
  dueDate?: string;
  status: 'draft' | 'sent' | 'accepted' | 'rejected' | 'expired';
  priority?: string;
  billingAddress?: string;
  deliveryAddress?: string;
  isInterstate?: boolean;
  subtotal: number;
  discountPercentage?: number;
  discountAmount?: number;
  cgstPercentage?: number;
  cgstAmount?: number;
  sgstPercentage?: number;
  sgstAmount?: number;
  igstPercentage?: number;
  igstAmount?: number;
  taxAmount: number;
  grandTotal: number;
  pdfUrl?: string;
  items: QuotationItemDto[];
  createdOn: string;
  updatedOn?: string;
}

export interface PdfResponse {
  pdfUrl: string;
}

export interface StatusUpdateRequest {
  status: string;
}
