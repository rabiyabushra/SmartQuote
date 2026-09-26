export interface ParsedQuotationItem {
  lineNumber: number;
  itemDescription: string;
  quantity: number;
  unit: string;
  rate: number;
  lineTotal: number;
}

export interface ParsedQuotationResponse {
  customerName: string | null;
  customerPhone: string | null;
  items: ParsedQuotationItem[];
  taxType: string;
  taxPercentage: number;
  subtotal: number;
  taxAmount: number;
  grandTotal: number;
  rawTranscript: string;
}

export interface QuotationParseRequest {
  transcript: string;
}
