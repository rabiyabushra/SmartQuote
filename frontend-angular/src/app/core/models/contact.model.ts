// Contact model matching backend structure
export interface Contact {
  id?: string;
  businessId?: string;
  contactPerson: string;
  companyName: string;
  email: string;
  phone: string;
  gstType: 'regular' | 'composition' | 'unregistered';
  gstin?: string;
  address: string;
  city: string;
  state: string;
  country: string;
  pincode: string;
  createdBy?: number;
  createdOn?: string;
  updatedBy?: number;
  updatedOn?: string;
  status?: 'active' | 'inactive' | 'deleted';
}
