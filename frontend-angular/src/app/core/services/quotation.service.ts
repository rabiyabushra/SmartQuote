import { Injectable, computed, signal } from '@angular/core';
import { QuotationStatus, QuotationSummary } from '../models/quotation.model';
import { QuotationTemplateId } from '../models/quotation-template.model';

export interface QuotationRecord {
  summary: QuotationSummary;
  templateId: QuotationTemplateId;
  formData: any;
}

@Injectable({ providedIn: 'root' })
export class QuotationService {
  private records = signal<Record<string, QuotationRecord>>(this.seedMockData());

  /** Flat list for the dashboard, always in sync with the underlying records */
  quotations = computed(() => Object.values(this.records()).map(r => r.summary));

  getById(id: string): QuotationRecord | undefined {
    return this.records()[id];
  }

  // TODO: replace with QuotationApiService.create(templateId, formData)
  create(templateId: QuotationTemplateId, formData: any): string {
    const id = this.generateId();
    const summary = this.buildSummaryFromFormData(id, formData);
    this.records.update(r => ({ ...r, [id]: { summary, templateId, formData } }));
    return id;
  }

  updateStatus(id: string, status: QuotationStatus): void {
    this.records.update(r => {
      const record = r[id];
      if (!record) return r;
      return { ...r, [id]: { ...record, summary: { ...record.summary, status } } };
    });
  }

  private generateId(): string {
    return 'q' + Date.now().toString(36) + Math.floor(Math.random() * 1000);
  }

  private buildSummaryFromFormData(id: string, formData: any): QuotationSummary {
    return {
      id,
      quoteId: formData.quoteNumber ?? id,
      customerName: formData.customerName ?? formData.clientName ?? 'Unnamed Customer',
      contactNumber: formData.contactNumber ?? formData.contact ?? formData.clientContact ?? '—',
      email: formData.email ?? '—',
      status: 'Draft',
      employeeName: 'You',
      city: formData.city ?? '—',
      state: formData.state ?? '—',
      dateCreated: formData.quoteDate ? new Date(formData.quoteDate) : new Date(),
      followUpDate: null,
      isFollowUpOverdue: false
    };
  }

  // TODO: remove once QuotationApiService is wired to the real backend
  private seedMockData(): Record<string, QuotationRecord> {
    return {
      '1': {
        templateId: 'job-work',
        summary: {
          id: '1', quoteId: 'QT-2026-0041', customerName: 'Rajesh Mehta',
          contactNumber: '+91 98765 43210', email: 'rajesh@email.com',
          status: 'Draft', employeeName: 'Amit Sharma', city: 'Mumbai', state: 'MH',
          dateCreated: new Date('2026-03-01'), followUpDate: new Date('2026-03-08'), isFollowUpOverdue: true
        },
        formData: {
          quoteNumber: 'QT-2026-0041', quoteDate: new Date('2026-03-01'), validUntil: new Date('2026-03-15'),
          gstin: '27ABCDE1234F1Z5',
          customerName: 'Rajesh Mehta', customerAddress: 'Mumbai, MH', contactNumber: '+91 98765 43210',
          scopeOfJobWork: 'Fabrication of MS frame as per approved drawing no.XX\nDrilling, cutting, welding included\nSurface finishing excluded',
          materialSuppliedBy: 'client', materialGrade: 'IS 2062', materialQuantityReceived: '500 kg',
          items: [
            { description: 'Skilled Labour', sacCode: '998732', qty: 20, rate: 150 },
            { description: 'Drilling Charge', sacCode: '998732', qty: 15, rate: 100 },
            { description: 'Welding Charge', sacCode: '998732', qty: 10, rate: 120 }
          ],
          termsAndConditions: 'Work will be executed as per approved drawing and technical specification.\nClient inspection required before dispatch.\nAny deviation must be reported within 3 days of delivery.\nRework will be undertaken only after written confirmation.'
        }
      },
      '2': {
        templateId: 'service',
        summary: {
          id: '2', quoteId: 'QT-2026-0040', customerName: 'Priya Nair',
          contactNumber: '+91 91234 56789', email: 'priya@email.com',
          status: 'Sent to Customer', employeeName: 'Sneha Iyer', city: 'Bangalore', state: 'KA',
          dateCreated: new Date('2026-02-28'), followUpDate: new Date('2026-03-10'), isFollowUpOverdue: false
        },
        formData: {
          quoteNumber: 'QT-2026-0040', quoteDate: new Date('2026-02-28'), validUntil: new Date('2026-03-30'),
          referenceNo: 'REF-2040', paymentTerms: '50% advance',
          customerName: 'Priya Nair', customerAddress: 'Bangalore, KA', contactPerson: 'Priya Nair',
          scopeOfService: 'Manpower deployment as per requirement\nService hours defined per shift\nSupervision included\nConsumables excluded',
          items: [
            { description: 'Site Supervisor', resources: 1, unit: 'per day', rate: 1500 },
            { description: 'Technician', resources: 3, unit: 'per day', rate: 800 }
          ],
          discount: 0, advance: 2000,
          termsAndConditions: 'The above rates are exclusive of taxes.\nClient must provide access and necessary approvals prior to deployment.\nAny additional work will be executed at extra cost upon approval.\nPayment terms: 50% advance, balance within 7 days of invoice.\nThis quotation is valid for 30 days from the date of issue.'
        }
      },
      '3': {
        templateId: 'fabrication',
        summary: {
          id: '3', quoteId: 'QT-2026-0039', customerName: 'Vikram Patel',
          contactNumber: '+91 87654 32109', email: 'vikram@email.com',
          status: 'In Negotiation', employeeName: 'Ravi Kumar', city: 'Delhi', state: 'DL',
          dateCreated: new Date('2026-02-25'), followUpDate: new Date('2026-03-05'), isFollowUpOverdue: true
        },
        formData: {
          quoteNumber: 'QT-2026-0039', quoteDate: new Date('2026-02-25'), validUntil: new Date('2026-03-10'),
          gstin: '27ABCDE1234F1Z5',
          customerName: 'Vikram Patel', customerAddress: 'Delhi, DL', contactPerson: 'Vikram Patel',
          jobRefNo: 'JOB-0039', contact: '+91 87654 32109', email: 'vikram@email.com',
          materialItems: [
            { description: 'MS Angle 50x50x6', qty: 200, price: 65, cgstPercent: 9 },
            { description: 'MS Plate 6mm', qty: 50, price: 120, cgstPercent: 9 }
          ],
          laborItems: [
            { description: 'Cutting & Fabrication', qty: 40, price: 250, cgstPercent: 9 }
          ],
          termsAndConditions: 'Work will be executed as per approved drawing.\nClient inspection required before dispatch.\nRework only after written confirmation.'
        }
      },
      '4': {
        templateId: 'supply',
        summary: {
          id: '4', quoteId: 'QT-2026-0038', customerName: 'Sunita Kapoor',
          contactNumber: '+91 76543 21098', email: 'sunita@email.com',
          status: 'Converted', employeeName: 'Deepa Rao', city: 'Hyderabad', state: 'TS',
          dateCreated: new Date('2026-02-22'), followUpDate: new Date('2026-03-01'), isFollowUpOverdue: false
        },
        formData: {
          quoteNumber: 'QT-2026-0038', quoteDate: new Date('2026-02-22'), validUntil: new Date('2026-03-05'),
          gstin: '27ABCDE1234F1Z5', referenceNo: 'REF-0038',
          customerName: 'Sunita Kapoor', customerAddress: 'Hyderabad, TS',
          contactPerson: 'Sunita Kapoor', contact: '+91 76543 21098',
          paymentTerms: '50% advance, balance on delivery',
          items: [
            { description: 'MS Pipe 2 inch', qty: 100, unitPrice: 85 },
            { description: 'MS Elbow Joint', qty: 40, unitPrice: 45 }
          ],
          termsAndConditions: 'Prices are in INR and valid until the mentioned date.\nDelivery will be made within 7 working days from the date of order confirmation.\n50% advance payment required, balance within 7 days of delivery.'
        }
      },
      '5': {
        templateId: 'maintenance',
        summary: {
          id: '5', quoteId: 'QT-2026-0037', customerName: 'Anand Verma',
          contactNumber: '+91 65432 10987', email: 'anand@email.com',
          status: 'Not Converted', employeeName: 'Amit Sharma', city: 'Chennai', state: 'TN',
          dateCreated: new Date('2026-02-20'), followUpDate: null, isFollowUpOverdue: false
        },
        formData: {
          quoteNumber: 'QT-2026-0037', quoteDate: new Date('2026-02-20'), validUntil: new Date('2026-03-20'),
          paymentTermsShort: 'Staged',
          projectName: 'HVAC Annual Maintenance', siteLocation: 'Chennai Plant',
          clientContact: 'Anand Verma', scopeReference: 'AMC-2026-01', subject: 'Annual HVAC Maintenance Contract',
          clientName: 'Anand Verma', address: 'Chennai, TN', phone: '+91 65432 10987', email: 'anand@email.com',
          scopeOfWork: 'Maintenance tasks described in bullet points\nInspection and assessment before work begins\nConsumables provided separately (if applicable)\nWork to be carried out as per schedule and requirements',
          items: [
            { description: 'AC Unit Servicing', unit: 'per unit', qty: 12, unitRate: 800, taxPercent: 9 },
            { description: 'Duct Cleaning', unit: 'per unit', qty: 6, unitRate: 1200, taxPercent: 9 }
          ],
          paymentTerms: '50% Advance\n30% After Mid-Stage Completion\n20% Upon Final Handover',
          termsAndConditions: 'All tasks will be completed as per approved schedule.\nQuote is based on actual site conditions, final costs may vary if there are any changes beyond the original scope of work.\nOur team will adhere to site safety and regulations at all times.\nThis quotation is valid for 30 days from the date of issue.\nElectricity to be provided by client. Working hours as per agreed schedule.'
        }
      },
      '6': {
        templateId: 'job-work',
        summary: {
          id: '6', quoteId: 'QT-2026-0036', customerName: 'Geeta Singh',
          contactNumber: '+91 54321 09876', email: 'geeta@email.com',
          status: 'Draft', employeeName: 'Sneha Iyer', city: 'Pune', state: 'MH',
          dateCreated: new Date('2026-02-18'), followUpDate: new Date('2026-03-06'), isFollowUpOverdue: true
        },
        formData: {
          quoteNumber: 'QT-2026-0036', quoteDate: new Date('2026-02-18'), validUntil: new Date('2026-03-06'),
          gstin: '27ABCDE1234F1Z5',
          customerName: 'Geeta Singh', customerAddress: 'Pune, MH', contactNumber: '+91 54321 09876',
          scopeOfJobWork: 'Fabrication of MS gate as per approved drawing\nWelding and grinding included\nPainting excluded',
          materialSuppliedBy: 'vendor', materialGrade: 'MS IS 2062', materialQuantityReceived: '150 kg',
          items: [
            { description: 'Skilled Labour', sacCode: '998732', qty: 12, rate: 150 },
            { description: 'Grinding Charge', sacCode: '998732', qty: 8, rate: 90 }
          ],
          termsAndConditions: 'Work will be executed as per approved drawing and technical specification.\nClient inspection required before dispatch.\nAny deviation must be reported within 3 days of delivery.\nRework will be undertaken only after written confirmation.'
        }
      }
    };
  }
}