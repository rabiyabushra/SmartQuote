export interface QuotationTemplateOption {
  id: string;
  name: string;
  thumbnailUrl: string;
}

export interface BusinessProfile {
  businessName: string;
  ownerName: string;
  logoUrl: string | null;
  email: string;
  contactNumber: string;
  gstin: string;
  address: string;
  city?: string;
  state?: string;
  pincode?: string;
  selectedTemplateId: string;
  termsAndConditions: string;
  footerNotes: string;
}
