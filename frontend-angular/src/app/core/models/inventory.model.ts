// Inventory model matching backend structure
export interface Inventory {
  id?: string;
  businessId?: string;
  itemName: string;
  description?: string;
  hsnSacCode: string;
  unitOfMeasurement: string;
  mrp: number;
  sellingPrice: number;
  taxPercentage: number;
  stockQuantity?: number;
  createdBy?: number;
  createdOn?: string;
  updatedBy?: number;
  updatedOn?: string;
  status?: 'active' | 'inactive' | 'deleted';
}
