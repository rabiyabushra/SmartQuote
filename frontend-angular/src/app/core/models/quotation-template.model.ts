export type QuotationTemplateId =
  | 'job-work'
  | 'fabrication'
  | 'supply'
  | 'service'
  | 'maintenance';

export interface QuotationTemplateMeta {
  id: QuotationTemplateId;
  name: string;
  description: string;
  icon: string;
}

export const QUOTATION_TEMPLATES: QuotationTemplateMeta[] = [
  {
    id: 'job-work',
    name: 'Job Work Quotation',
    description: 'Labour/drilling/welding charges with client- or vendor-supplied material.',
    icon: 'precision_manufacturing'
  },
  {
    id: 'fabrication',
    name: 'Fabrication Work Quotation',
    description: 'Separate Material/Products and Labor tables with a combined grand total.',
    icon: 'construction'
  },
  {
    id: 'supply',
    name: 'Supply Quotation',
    description: 'Simple supply-of-goods quotation with one item table and payment terms.',
    icon: 'local_shipping'
  },
  {
    id: 'service',
    name: 'Service Quotation',
    description: 'Manpower/service deployment quotation with discount and advance lines.',
    icon: 'engineering'
  },
  {
    id: 'maintenance',
    name: 'Maintenance Quotation',
    description: 'Site/project maintenance quotation with staged payment terms.',
    icon: 'build'
  }
];
