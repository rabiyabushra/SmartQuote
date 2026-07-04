export interface TaxBreakdown {
  subtotal: number;
  cgst: number;
  sgst: number;
  total: number;
}

export function calculateTax(subtotal: number, cgstPercent = 9, sgstPercent = 9): TaxBreakdown {
  const cgst = round2(subtotal * (cgstPercent / 100));
  const sgst = round2(subtotal * (sgstPercent / 100));
  return { subtotal: round2(subtotal), cgst, sgst, total: round2(subtotal + cgst + sgst) };
}

export function round2(value: number): number {
  return Math.round((value + Number.EPSILON) * 100) / 100;
}
