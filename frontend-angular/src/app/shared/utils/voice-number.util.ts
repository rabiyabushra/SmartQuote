export interface NumberNormalizationResult {
  value: number | null;
  error?: string;
}

const WORD_VALUES: Record<string, number> = {
  zero: 0,
  one: 1,
  two: 2,
  three: 3,
  four: 4,
  five: 5,
  six: 6,
  seven: 7,
  eight: 8,
  nine: 9,
  ten: 10,
  eleven: 11,
  twelve: 12,
  thirteen: 13,
  fourteen: 14,
  fifteen: 15,
  sixteen: 16,
  seventeen: 17,
  eighteen: 18,
  nineteen: 19,
  twenty: 20,
  thirty: 30,
  forty: 40,
  fifty: 50,
  sixty: 60,
  seventy: 70,
  eighty: 80,
  ninety: 90,
  hundred: 100,
  thousand: 1000,
  lakh: 100000,
  lacs: 100000,
  lac: 100000,
  crores: 10000000,
  crore: 10000000
};

const IGNORED_FILLER_WORDS = new Set([
  'pieces', 'piece', 'pcs', 'pc', 'boxes', 'box', 'units', 'unit', 'nos', 'number', 'numbers',
  'kg', 'kgs', 'kilo', 'kilos', 'meter', 'meters', 'mtr', 'mtrs', 'feet', 'ft', 'sets', 'set',
  'packets', 'packet', 'pkt', 'rolls', 'roll', 'tons', 'ton', 'liters', 'liter', 'ltr',
  'rupees', 'rs', 'inr', 'each', 'per', 'at', 'rate', 'price', 'quantity', 'qty', 'count',
  'of', 'and', 'the', 'a', 'an', 'is', 'for'
]);

const UNCERTAIN_NUMBER_MESSAGE =
  'Could not confidently recognize a numeric value. Please edit the value manually.';

function parseNumericString(raw: string): number | null {
  const cleaned = raw
    .replace(/[₹$€£]/g, '')
    .replace(/,/g, '')
    .replace(/\s+/g, '')
    .trim();

  if (!cleaned) {
    return null;
  }

  if (/^\d+(\.\d+)?$/.test(cleaned)) {
    return Number(cleaned);
  }

  return null;
}

function parseWordNumber(text: string): number | null {
  const tokens = text
    .toLowerCase()
    .replace(/[₹$€£,]/g, ' ')
    .replace(/[^a-z0-9\s]/g, ' ')
    .split(/\s+/)
    .filter(Boolean);

  if (tokens.length === 0) {
    return null;
  }

  let total = 0;
  let current = 0;
  let hasValidNumber = false;

  for (const token of tokens) {
    if (/^\d+$/.test(token)) {
      current += Number(token);
      hasValidNumber = true;
      continue;
    }

    if (IGNORED_FILLER_WORDS.has(token)) {
      continue;
    }

    const mapped = WORD_VALUES[token];
    if (mapped === undefined) {
      return null;
    }

    hasValidNumber = true;
    if (mapped === 100 || mapped === 1000 || mapped === 100000 || mapped === 10000000) {
      current = current === 0 ? mapped : current * mapped;
      total += current;
      current = 0;
    } else {
      current += mapped;
    }
  }

  total += current;
  return hasValidNumber ? total : null;
}

export function normalizeSpokenNumber(text: string): NumberNormalizationResult {
  const trimmed = text.trim();
  if (!trimmed) {
    return { value: null, error: UNCERTAIN_NUMBER_MESSAGE };
  }

  const direct = parseNumericString(trimmed);
  if (direct !== null && Number.isFinite(direct)) {
    return { value: direct };
  }

  const digitMatch = trimmed.match(/-?\d[\d,]*(?:\.\d+)?/);
  if (digitMatch) {
    const extracted = parseNumericString(digitMatch[0]);
    if (extracted !== null && Number.isFinite(extracted)) {
      return { value: extracted };
    }
  }

  const wordValue = parseWordNumber(trimmed);
  if (wordValue !== null && Number.isFinite(wordValue)) {
    return { value: wordValue };
  }

  return { value: null, error: UNCERTAIN_NUMBER_MESSAGE };
}
