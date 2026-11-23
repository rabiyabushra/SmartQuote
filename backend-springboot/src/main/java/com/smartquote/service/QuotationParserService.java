package com.smartquote.service;

import com.smartquote.dto.ParsedQuotationResponseDto;
import com.smartquote.dto.ParsedQuotationResponseDto.ParsedItemDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Primary and ONLY commercial quotation parser for SmartQuote.
 * 100% Free, local open-source parser using Java pattern matching & spoken number evaluation.
 * Accurately extracts Customer Name, Phone, GST rate, and Line Items (Description, Quantity, Unit, Rate).
 */
@Service
public class QuotationParserService {

    private static final Logger log = LoggerFactory.getLogger(QuotationParserService.class);

    private static final Map<String, Long> SINGLE_NUMBER_WORDS = new LinkedHashMap<>();
    static {
        SINGLE_NUMBER_WORDS.put("zero", 0L);
        SINGLE_NUMBER_WORDS.put("one", 1L);
        SINGLE_NUMBER_WORDS.put("two", 2L);
        SINGLE_NUMBER_WORDS.put("three", 3L);
        SINGLE_NUMBER_WORDS.put("four", 4L);
        SINGLE_NUMBER_WORDS.put("five", 5L);
        SINGLE_NUMBER_WORDS.put("six", 6L);
        SINGLE_NUMBER_WORDS.put("seven", 7L);
        SINGLE_NUMBER_WORDS.put("eight", 8L);
        SINGLE_NUMBER_WORDS.put("nine", 9L);
        SINGLE_NUMBER_WORDS.put("ten", 10L);
        SINGLE_NUMBER_WORDS.put("eleven", 11L);
        SINGLE_NUMBER_WORDS.put("twelve", 12L);
        SINGLE_NUMBER_WORDS.put("thirteen", 13L);
        SINGLE_NUMBER_WORDS.put("fourteen", 14L);
        SINGLE_NUMBER_WORDS.put("fifteen", 15L);
        SINGLE_NUMBER_WORDS.put("sixteen", 16L);
        SINGLE_NUMBER_WORDS.put("seventeen", 17L);
        SINGLE_NUMBER_WORDS.put("eighteen", 18L);
        SINGLE_NUMBER_WORDS.put("nineteen", 19L);
        SINGLE_NUMBER_WORDS.put("twenty", 20L);
        SINGLE_NUMBER_WORDS.put("thirty", 30L);
        SINGLE_NUMBER_WORDS.put("forty", 40L);
        SINGLE_NUMBER_WORDS.put("fifty", 50L);
        SINGLE_NUMBER_WORDS.put("sixty", 60L);
        SINGLE_NUMBER_WORDS.put("seventy", 70L);
        SINGLE_NUMBER_WORDS.put("eighty", 80L);
        SINGLE_NUMBER_WORDS.put("ninety", 90L);
    }

    private static final Pattern SPOKEN_NUMBER_PATTERN = Pattern.compile(
            "(?i)\\b(?:zero|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety|hundred|thousand|lakh|lacs|lac|crore|crores|million|and)(?:[\\s\\-]+(?:zero|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety|hundred|thousand|lakh|lacs|lac|crore|crores|million|and))*\\b"
    );

    public ParsedQuotationResponseDto parseQuotationTranscript(String transcript) {
        if (transcript == null || transcript.isBlank()) {
            throw new IllegalArgumentException("Transcript content cannot be empty.");
        }

        ParsedQuotationResponseDto dto = new ParsedQuotationResponseDto();
        dto.setRawTranscript(transcript);

        String normalizedText = normalizeTranscriptText(transcript);

        // 1. Customer Name Extraction
        extractCustomerName(transcript, dto);

        // 2. Customer Phone Extraction
        extractCustomerPhone(transcript, dto);

        // 3. GST / Tax Extraction
        extractTaxDetails(normalizedText, dto);

        // 4. Line Items Extraction (items, quantity, unit, rate)
        extractLineItems(normalizedText, transcript, dto);

        // 5. Backend Calculations
        calculateBackendTotals(dto);

        return dto;
    }

    private String normalizeTranscriptText(String input) {
        String text = input.trim();
        // Replace spoken number phrases with digits
        text = convertSpokenNumbersToDigits(text);
        return text;
    }

    private void extractCustomerName(String originalTranscript, ParsedQuotationResponseDto dto) {
        Pattern pattern = Pattern.compile("(?:create\\s+(?:a\\s+)?quotation\\s+for|quotation\\s+for|quote\\s+for|bill\\s+for|invoice\\s+for|customer\\s+name\\s*:?|customer\\s*:?|client\\s*:?|buyer\\s*:?)\\s+([A-Za-z0-9\\s&]+?)(?:[,\n\\.]|\\bphone\\b|\\bmobile\b|\\bcontact\b|\\bitem\\b|\\bitems\\b|\\badd\\b|\\bwith\\b|\\bquantity\\b|\\bqty\\b|\\bprice\\b|\\brate\\b|\\bgst\\b|\\btax\\b|$)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(originalTranscript);
        if (matcher.find()) {
            String name = matcher.group(1).trim();
            name = name.replaceAll("(?i)^(?:a|the|for)\\s+", "").trim();
            if (!name.isBlank() && name.length() > 1) {
                dto.setCustomerName(name);
            }
        }
    }

    private void extractCustomerPhone(String originalTranscript, ParsedQuotationResponseDto dto) {
        Pattern phonePattern = Pattern.compile("(?:phone|mobile|contact|tel|cell)\\s*(?:no\\.?|number)?:?\\s*(\\+?[0-9][0-9\\s\\-]{8,14}[0-9])", Pattern.CASE_INSENSITIVE);
        Matcher matcher = phonePattern.matcher(originalTranscript);
        if (matcher.find()) {
            String phone = matcher.group(1).replaceAll("[\\s\\-]", "");
            if (phone.length() >= 10) {
                dto.setCustomerPhone(phone);
            }
        }
    }

    private void extractTaxDetails(String text, ParsedQuotationResponseDto dto) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("gst") || lower.contains("tax") || lower.contains("vat")) {
            // Check "gst 18%", "gst 18 percent", "gst rate 18%", "tax 18%", "with 18% gst", "18% gst"
            Pattern pattern1 = Pattern.compile("(?:gst|tax|vat)\\s*(?:rate|percentage|of)?\\s*:?\\s*(\\d+(?:\\.\\d+)?)\\s*(?:%|percent)?", Pattern.CASE_INSENSITIVE);
            Matcher matcher1 = pattern1.matcher(text);
            if (matcher1.find()) {
                dto.setTaxPercentage(new BigDecimal(matcher1.group(1)));
                return;
            }

            Pattern pattern2 = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:%|percent)?\\s*(?:gst|tax|vat)", Pattern.CASE_INSENSITIVE);
            Matcher matcher2 = pattern2.matcher(text);
            if (matcher2.find()) {
                dto.setTaxPercentage(new BigDecimal(matcher2.group(1)));
                return;
            }

            // Default to 18% if GST is mentioned without a specific rate
            dto.setTaxPercentage(BigDecimal.valueOf(18));
        } else {
            // Standard MSME default tax rate
            dto.setTaxPercentage(BigDecimal.valueOf(18));
        }
    }

    private void extractLineItems(String normalizedText, String originalTranscript, ParsedQuotationResponseDto dto) {
        // Strip customer header, phone, and tax footer phrases to isolate item text
        String itemsText = normalizedText
                .replaceAll("(?i)^(?:create|make)?\\s*(?:a)?\\s*quotation\\s*for\\s+[^,\\.\\n]+[\\.,\\n]?", "")
                .replaceAll("(?i)quote for\\s+[^,\\.\\n]+[\\.,\\n]?", "")
                .replaceAll("(?i)(?:phone|mobile|contact)\\s*(?:no\\.?|number)?:?\\s*\\+?[0-9\\s\\-]+[\\.,\\n]?", "")
                .replaceAll("(?i)(?:add|include|with|including)?\\s*\\b\\d+(?:\\.\\d+)?\\s*(?:%|percent)\\s*(?:gst|tax|vat)\\b.*$", "")
                .replaceAll("(?i)(?:add|include|with|including)\\s+(?:the\\s+)?(?:\\d+(?:\\.\\d+)?\\s*(?:%|percent)?\\s*)?(?:gst|tax|vat)\\b.*$", "")
                .replaceAll("(?i)\\b(?:gst|tax|vat)\\s*(?:rate|percentage|of)?\\s*:?\\s*(?:\\d+(?:\\.\\d+)?(?:%|percent)?)?.*$", "");

        List<String> rawClauses = new ArrayList<>();

        // Handle delivery charges if mentioned
        if (itemsText.toLowerCase().contains("delivery") || itemsText.toLowerCase().contains("freight") || itemsText.toLowerCase().contains("shipping")) {
            Pattern deliveryPattern = Pattern.compile("(?:include|add)?\\s*(delivery|freight|shipping)\\s*(?:charges|fee)?\\s*(?:of|at|is)?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher mDelivery = deliveryPattern.matcher(itemsText);
            if (mDelivery.find()) {
                String label = mDelivery.group(1).substring(0, 1).toUpperCase() + mDelivery.group(1).substring(1) + " charges";
                BigDecimal fee = new BigDecimal(mDelivery.group(2));
                ParsedItemDto deliveryItem = new ParsedItemDto(1, label, BigDecimal.ONE, "service", fee, fee);
                dto.getItems().add(deliveryItem);

                itemsText = mDelivery.replaceAll("");
            }
        }

        if (itemsText.toLowerCase().matches(".*\\bitem\\s*\\d*[:\\.\\-]?\\s+.*")) {
            String[] parts = itemsText.split("(?i)\\bitem\\s*\\d*[:\\.\\-]?\\s*");
            for (String p : parts) {
                if (!p.trim().isEmpty()) rawClauses.add(p.trim());
            }
        } else {
            // Split by sentence delimiters, semicolons, newlines, or 'and' before next item
            String[] sentences = itemsText.split("(?i)(?:\\.|;|\n|\\band\\s+(?=(?:item\\b|add\\b|\\d+\\s+[a-zA-Z]+|[a-zA-Z]+\\s+\\d+)))");
            for (String sentence : sentences) {
                String s = sentence.trim();
                if (s.isEmpty()) continue;
                if (s.contains(",")) {
                    String[] commaSplit = s.split(",");
                    for (String cs : commaSplit) {
                        if (!cs.trim().isEmpty()) rawClauses.add(cs.trim());
                    }
                } else {
                    rawClauses.add(s);
                }
            }
        }

        int startLine = dto.getItems().size() + 1;
        for (String clause : rawClauses) {
            ParsedItemDto item = parseItemClause(clause, startLine);
            if (item != null) {
                item.setLineNumber(startLine++);
                dto.getItems().add(item);
            }
        }

        if (dto.getItems().isEmpty()) {
            ParsedItemDto fallback = new ParsedItemDto(1, originalTranscript, BigDecimal.ONE, "pcs", BigDecimal.ZERO, BigDecimal.ZERO);
            dto.getItems().add(fallback);
        }
    }

    private ParsedItemDto parseItemClause(String clause, int lineNum) {
        String clean = clause.trim();
        if (clean.isBlank()) return null;

        // Strip leading filler action words
        clean = clean.replaceAll("(?i)^(?:please\\s+)?(?:add|include|also|next)\\s+", "").trim();

        BigDecimal quantity = BigDecimal.ONE;
        BigDecimal unitPrice = BigDecimal.ZERO;
        String unit = "pcs";

        // 1. Extract Unit
        unit = extractUnit(clean);

        // 2. Extract Quantity
        Pattern explicitQty = Pattern.compile("(?:quantity|qty|count|volume)\\s*:?\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
        Matcher mExplicitQty = explicitQty.matcher(clean);
        if (mExplicitQty.find()) {
            quantity = new BigDecimal(mExplicitQty.group(1));
        } else {
            // Pattern like "10 pieces of MS Angle" or "10 kg steel" or "10 chairs"
            Pattern leadingQty = Pattern.compile("^(\\d+(?:\\.\\d+)?)\\s+", Pattern.CASE_INSENSITIVE);
            Matcher mLeadingQty = leadingQty.matcher(clean);
            if (mLeadingQty.find()) {
                quantity = new BigDecimal(mLeadingQty.group(1));
            } else {
                // Mid-clause quantity: "MS Angle 10 pieces" or "MS Angle 10 kg"
                Pattern midQty = Pattern.compile("\\b(\\d+(?:\\.\\d+)?)\\s*(?:pieces|piece|pcs|pc|box|boxes|kg|kgs|kilos|meter|meters|mtr|sets|set|units|unit|nos)\\b", Pattern.CASE_INSENSITIVE);
                Matcher mMidQty = midQty.matcher(clean);
                if (mMidQty.find()) {
                    quantity = new BigDecimal(mMidQty.group(1));
                }
            }
        }

        // 3. Extract Unit Price / Rate
        Pattern explicitPrice = Pattern.compile("(?:price|rate|cost|amount|at|@|for|worth)\\s*(?:is|of|:)?\\s*(?:₹|rs\\.?|rupees|inr)?\\s*(\\d+(?:\\.\\d+)?\\s*k|\\d+(?:\\.\\d+)?)\\s*(?:rupees|rs\\.?|₹)?(?:\\s*(?:each|per\\s+[a-zA-Z]+|per\\s+item|per\\s+unit|/-))?", Pattern.CASE_INSENSITIVE);
        Matcher mExplicitPrice = explicitPrice.matcher(clean);
        if (mExplicitPrice.find()) {
            unitPrice = parsePriceValue(mExplicitPrice.group(1));
        } else {
            Pattern eachPrice = Pattern.compile("(\\d+(?:\\.\\d+)?\\s*k|\\d+(?:\\.\\d+)?)\\s*(?:rupees|rs\\.?|₹)?\\s*(?:each|per\\s+[a-zA-Z]+|per\\s+item|per\\s+unit|/-)", Pattern.CASE_INSENSITIVE);
            Matcher mEachPrice = eachPrice.matcher(clean);
            if (mEachPrice.find()) {
                unitPrice = parsePriceValue(mEachPrice.group(1));
            } else {
                Pattern currencyPrice = Pattern.compile("(?:₹|rs\\.?|inr)\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
                Matcher mCurrencyPrice = currencyPrice.matcher(clean);
                if (mCurrencyPrice.find()) {
                    unitPrice = parsePriceValue(mCurrencyPrice.group(1));
                }
            }
        }

        // 4. Extract Item Description by stripping quantity, unit, price, and filler words
        String description = clean
                .replaceAll("(?i)\\b(?:quantity|qty|count)\\s*:?\\s*\\d+(?:\\.\\d+)?", "")
                .replaceAll("(?i)\\b(?:price|rate|cost|amount|at|@|for|worth)\\s*(?:is|of|:)?\\s*(?:₹|rs\\.?|rupees|inr)?\\s*\\d+(?:\\.\\d+)?(?:\\s*k)?(?:\\s*rupees|\\s*rs\\.?|\\s*₹)?(?:\\s*(?:each|per\\s+[a-zA-Z]+|per\\s+item|per\\s+unit|/-))?", "")
                .replaceAll("(?i)\\b\\d+(?:\\.\\d+)?(?:\\s*k)?\\s*(?:rupees|rs\\.?|₹)?\\s*(?:each|per\\s+[a-zA-Z]+|per\\s+item|per\\s+unit|/-)", "")
                .replaceAll("(?i)(?:₹|rs\\.?|inr)\\s*\\d+(?:\\.\\d+)?", "")
                .replaceAll("(?i)^\\d+(?:\\.\\d+)?\\s+(?:boxes\\s+of|box\\s+of|pcs\\s+of|units\\s+of|kg\\s+of|meters\\s+of|pieces\\s+of|sets\\s+of)?", "")
                .replaceAll("(?i)\\b\\d+(?:\\.\\d+)?\\s*(?:pieces|piece|pcs|pc|boxes|box|kg|kgs|kilos|meters|meter|mtr|sets|set|units|unit|nos|packets|pkt|rolls|tons)\\b", "")
                .replaceAll("(?i)\\b(?:boxes\\s+of|box\\s+of|pcs\\s+of|units\\s+of|kg\\s+of|meters\\s+of|pieces\\s+of|sets\\s+of)\\b", "")
                .replaceAll("(?i)\\b(?:pieces|piece|pcs|pc|boxes|box|carton|cartons|kg|kgs|kilos|kilogram|kilograms|meters|meter|mtr|mtrs|feet|foot|ft|sets|set|packets|packet|pkt|packs|pack|rolls|roll|tons|ton|liters|liter|ltr|nos|units|unit)\\b", "")
                .replaceAll("(?i)\\b(?:price|rate|cost|amount|worth|rupees|rs\\.?|inr|each|per\\s+[a-zA-Z]+)\\b", "")
                .replaceAll("(?i)\\b(?:with|add|including)?\\s*\\d*(?:\\.\\d+)?%?\\s*(?:gst|tax|vat)\\b", "")
                .replaceAll("(?i)\\b\\d+(?:\\.\\d+)?\\b", "")
                .replaceAll("^[\\s,:\\.\\-]+", "")
                .replaceAll("[\\s,:\\.\\-]+$", "")
                .trim();

        if (description.isBlank()) {
            description = "Item " + lineNum;
        }

        return new ParsedItemDto(lineNum, description, quantity, unit, unitPrice, BigDecimal.ZERO);
    }

    private String extractUnit(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("box") || lower.contains("boxes") || lower.contains("carton")) {
            return "box";
        }
        if (lower.contains("kg") || lower.contains("kilo") || lower.contains("kilogram")) {
            return "kg";
        }
        if (lower.contains("gm") || lower.contains("gram")) {
            return "gm";
        }
        if (lower.contains("meter") || lower.contains("metre") || lower.contains("mtr")) {
            return "meter";
        }
        if (lower.contains("feet") || lower.contains("foot") || lower.contains("ft")) {
            return "ft";
        }
        if (lower.contains("set") || lower.contains("sets")) {
            return "set";
        }
        if (lower.contains("packet") || lower.contains("pkt") || lower.contains("pack")) {
            return "pkt";
        }
        if (lower.contains("roll") || lower.contains("rolls")) {
            return "roll";
        }
        if (lower.contains("ton") || lower.contains("tonne")) {
            return "ton";
        }
        if (lower.contains("liter") || lower.contains("litre") || lower.contains("ltr")) {
            return "ltr";
        }
        if (lower.contains("hour") || lower.contains("hrs") || lower.contains("hr")) {
            return "hrs";
        }
        if (lower.contains("bundle") || lower.contains("bundles")) {
            return "bundle";
        }
        if (lower.contains("pair") || lower.contains("pairs")) {
            return "pair";
        }
        if (lower.contains("bag") || lower.contains("bags")) {
            return "bag";
        }
        if (lower.contains("unit") || lower.contains("units") || lower.contains("nos") || lower.contains("number")) {
            return "nos";
        }
        if (lower.contains("piece") || lower.contains("pieces") || lower.contains("pcs") || lower.contains("pc")) {
            return "pcs";
        }
        return "pcs";
    }

    private BigDecimal parsePriceValue(String str) {
        if (str == null) return BigDecimal.ZERO;
        String s = str.toLowerCase().trim();
        if (s.endsWith("k")) {
            double val = Double.parseDouble(s.replace("k", "").trim()) * 1000;
            return BigDecimal.valueOf(val);
        }
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Converts spoken English number words into numeric digits.
     * E.g. "Three Dell laptops at fifty-five thousand rupees each" -> "3 Dell laptops at 55000 rupees each"
     */
    public String convertSpokenNumbersToDigits(String text) {
        if (text == null || text.isBlank()) return text;

        Matcher matcher = SPOKEN_NUMBER_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String phrase = matcher.group();
            if (phrase.equalsIgnoreCase("and")) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(phrase));
            } else {
                long val = parseSpokenNumberPhrase(phrase);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(val)));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private long parseSpokenNumberPhrase(String phrase) {
        String clean = phrase.toLowerCase().replace("-", " ");
        String[] words = clean.split("\\s+");
        long grandTotal = 0;
        long currentPeriod = 0;

        for (String w : words) {
            if (w.equals("and")) continue;
            if (SINGLE_NUMBER_WORDS.containsKey(w)) {
                currentPeriod += SINGLE_NUMBER_WORDS.get(w);
            } else if (w.equals("hundred")) {
                currentPeriod = (currentPeriod == 0 ? 1 : currentPeriod) * 100;
            } else if (w.equals("thousand")) {
                currentPeriod = (currentPeriod == 0 ? 1 : currentPeriod) * 1000;
                grandTotal += currentPeriod;
                currentPeriod = 0;
            } else if (w.equals("lakh") || w.equals("lacs") || w.equals("lac")) {
                currentPeriod = (currentPeriod == 0 ? 1 : currentPeriod) * 100000;
                grandTotal += currentPeriod;
                currentPeriod = 0;
            } else if (w.equals("crore") || w.equals("crores")) {
                currentPeriod = (currentPeriod == 0 ? 1 : currentPeriod) * 10000000;
                grandTotal += currentPeriod;
                currentPeriod = 0;
            } else if (w.equals("million")) {
                currentPeriod = (currentPeriod == 0 ? 1 : currentPeriod) * 1000000;
                grandTotal += currentPeriod;
                currentPeriod = 0;
            }
        }
        return grandTotal + currentPeriod;
    }

    private void calculateBackendTotals(ParsedQuotationResponseDto dto) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (ParsedItemDto item : dto.getItems()) {
            BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ONE;
            BigDecimal rate = item.getRate() != null ? item.getRate() : BigDecimal.ZERO;
            BigDecimal lineTotal = qty.multiply(rate).setScale(2, RoundingMode.HALF_UP);

            item.setLineTotal(lineTotal);
            subtotal = subtotal.add(lineTotal);
        }

        dto.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));

        BigDecimal taxPct = dto.getTaxPercentage() != null ? dto.getTaxPercentage() : BigDecimal.ZERO;
        BigDecimal taxAmount = subtotal.multiply(taxPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        dto.setTaxAmount(taxAmount);

        BigDecimal grandTotal = subtotal.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
        dto.setGrandTotal(grandTotal);
    }
}
