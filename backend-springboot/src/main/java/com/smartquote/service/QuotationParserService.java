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
 * Does NOT depend on any external API or paid service.
 */
@Service
public class QuotationParserService {

    private static final Logger log = LoggerFactory.getLogger(QuotationParserService.class);

    private static final Map<String, Long> NUMBER_WORDS = new HashMap<>();
    static {
        NUMBER_WORDS.put("zero", 0L);
        NUMBER_WORDS.put("one", 1L);
        NUMBER_WORDS.put("two", 2L);
        NUMBER_WORDS.put("three", 3L);
        NUMBER_WORDS.put("four", 4L);
        NUMBER_WORDS.put("five", 5L);
        NUMBER_WORDS.put("six", 6L);
        NUMBER_WORDS.put("seven", 7L);
        NUMBER_WORDS.put("eight", 8L);
        NUMBER_WORDS.put("nine", 9L);
        NUMBER_WORDS.put("ten", 10L);
        NUMBER_WORDS.put("eleven", 11L);
        NUMBER_WORDS.put("twelve", 12L);
        NUMBER_WORDS.put("thirteen", 13L);
        NUMBER_WORDS.put("fourteen", 14L);
        NUMBER_WORDS.put("fifteen", 15L);
        NUMBER_WORDS.put("sixteen", 16L);
        NUMBER_WORDS.put("seventeen", 17L);
        NUMBER_WORDS.put("eighteen", 18L);
        NUMBER_WORDS.put("nineteen", 19L);
        NUMBER_WORDS.put("twenty", 20L);
        NUMBER_WORDS.put("thirty", 30L);
        NUMBER_WORDS.put("forty", 40L);
        NUMBER_WORDS.put("fifty", 50L);
        NUMBER_WORDS.put("sixty", 60L);
        NUMBER_WORDS.put("seventy", 70L);
        NUMBER_WORDS.put("eighty", 80L);
        NUMBER_WORDS.put("ninety", 90L);
    }

    public ParsedQuotationResponseDto parseQuotationTranscript(String transcript) {
        if (transcript == null || transcript.isBlank()) {
            throw new IllegalArgumentException("Transcript content cannot be empty.");
        }

        ParsedQuotationResponseDto dto = new ParsedQuotationResponseDto();
        dto.setRawTranscript(transcript);

        String normalizedText = normalizeTranscriptText(transcript);

        // 1. Customer Name Extraction
        extractCustomerName(transcript, dto);

        // 2. GST / Tax Extraction
        extractTaxDetails(normalizedText, dto);

        // 3. Line Items Extraction
        extractLineItems(normalizedText, transcript, dto);

        // 4. Backend Calculations
        calculateBackendTotals(dto);

        return dto;
    }

    private String normalizeTranscriptText(String input) {
        String text = input.trim();
        // Replace spoken numbers with digits where applicable
        text = convertSpokenNumbersToDigits(text);
        return text;
    }

    private void extractCustomerName(String originalTranscript, ParsedQuotationResponseDto dto) {
        Pattern pattern = Pattern.compile("(?:create\\s+(?:a\\s+)?quotation\\s+for|quotation\\s+for|quote\\s+for|customer\\s+name\\s*:?|client\\s*:?)\\s+([A-Za-z0-9\\s&]+?)(?:\\.|,|\\bitem\\b|\\badd\\b|\\bwith\\b|\\bquantity\\b|\\bqty\\b|\\bprice\\b|$)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(originalTranscript);
        if (matcher.find()) {
            String name = matcher.group(1).trim();
            name = name.replaceAll("(?i)^(?:a|the)\\s+", "").trim();
            if (!name.isBlank() && name.length() > 1) {
                dto.setCustomerName(name);
            }
        }
    }

    private void extractTaxDetails(String text, ParsedQuotationResponseDto dto) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("gst") || lower.contains("tax")) {
            Pattern pattern = Pattern.compile("(\\d+)\\s*(?:%|percent)?\\s*(?:gst|tax)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                dto.setTaxPercentage(new BigDecimal(matcher.group(1)));
            } else {
                dto.setTaxPercentage(BigDecimal.valueOf(18));
            }
        } else {
            dto.setTaxPercentage(BigDecimal.ZERO);
        }
    }

    private void extractLineItems(String normalizedText, String originalTranscript, ParsedQuotationResponseDto dto) {
        // Strip customer header and tax footer phrases to isolate items text
        String itemsText = normalizedText
                .replaceAll("(?i)^(?:create|make)?\\s*(?:a)?\\s*quotation\\s*for\\s+[^,\\.]+[\\.,]?", "")
                .replaceAll("(?i)quote for\\s+[^,\\.]+[\\.,]?", "")
                .replaceAll("(?i)add\\s*\\d*%?\\s*(?:percent)?\\s*gst.*", "");

        List<String> rawClauses = new ArrayList<>();

        // Handle delivery charges if mentioned
        if (itemsText.toLowerCase().contains("delivery") || itemsText.toLowerCase().contains("freight") || itemsText.toLowerCase().contains("shipping")) {
            Pattern deliveryPattern = Pattern.compile("(?:include|add)?\\s*(delivery|freight|shipping)\\s*(?:charges|fee)?\\s*(?:of|at)?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher mDelivery = deliveryPattern.matcher(itemsText);
            if (mDelivery.find()) {
                String label = mDelivery.group(1).substring(0, 1).toUpperCase() + mDelivery.group(1).substring(1) + " charges";
                BigDecimal fee = new BigDecimal(mDelivery.group(2));
                ParsedItemDto deliveryItem = new ParsedItemDto(1, label, BigDecimal.ONE, "service", fee, fee);
                dto.getItems().add(deliveryItem);

                itemsText = mDelivery.replaceAll(""); // remove delivery clause from main item parsing
            }
        }

        if (itemsText.toLowerCase().contains("item ")) {
            String[] parts = itemsText.split("(?i)\\bitem\\b");
            for (String p : parts) {
                if (!p.trim().isEmpty()) rawClauses.add(p.trim());
            }
        } else {
            // Split by sentence delimiters or 'and' between distinct items
            String[] sentences = itemsText.split("(?i)(?:\\.|;|\n|\\band\\b)");
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

        BigDecimal quantity = BigDecimal.ONE;
        BigDecimal unitPrice = BigDecimal.ZERO;
        String unit = "pcs";

        // Extract unit if specified (e.g. boxes of A4 paper -> unit = box)
        if (clean.toLowerCase().contains("boxes of") || clean.toLowerCase().contains("box of")) {
            unit = "box";
        } else if (clean.toLowerCase().contains("kg") || clean.toLowerCase().contains("kilos")) {
            unit = "kg";
        } else if (clean.toLowerCase().contains("hrs") || clean.toLowerCase().contains("hours")) {
            unit = "hrs";
        }

        // Extract Explicit Quantity e.g. "quantity 2", "qty: 5", "5 boxes of A4 paper", "10 chairs", "3 laptops"
        Pattern explicitQty = Pattern.compile("(?:quantity|qty)\\s*:?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher mExplicitQty = explicitQty.matcher(clean);
        if (mExplicitQty.find()) {
            quantity = new BigDecimal(mExplicitQty.group(1));
        } else {
            Pattern leadingQty = Pattern.compile("^(\\d+)\\s+([A-Za-z0-9\\s]+)", Pattern.CASE_INSENSITIVE);
            Matcher mLeadingQty = leadingQty.matcher(clean);
            if (mLeadingQty.find()) {
                quantity = new BigDecimal(mLeadingQty.group(1));
            }
        }

        // Extract Unit Price / Rate e.g. "at 12000 rupees each", "at 12000 each", "800 each", "5k each", "at 15500 each"
        Pattern explicitPrice = Pattern.compile("(?:price|rate|cost|at|for)\\s*:?\\s*(?:₹|rs\\.?|rupees)?\\s*(\\d+(?:\\.\\d+)?\\s*k|\\d+)\\s*(?:rupees)?\\s*(?:each|per item|per unit)?", Pattern.CASE_INSENSITIVE);
        Matcher mExplicitPrice = explicitPrice.matcher(clean);
        if (mExplicitPrice.find()) {
            unitPrice = parsePriceValue(mExplicitPrice.group(1));
        } else {
            Pattern eachPrice = Pattern.compile("(\\d+(?:\\.\\d+)?\\s*k|\\d+)\\s*(?:rupees)?\\s*(?:each|per item|per unit)", Pattern.CASE_INSENSITIVE);
            Matcher mEachPrice = eachPrice.matcher(clean);
            if (mEachPrice.find()) {
                unitPrice = parsePriceValue(mEachPrice.group(1));
            }
        }

        // Extract Item Description by removing quantity, price, and filler words
        String description = clean
                .replaceAll("(?i),?\\s*quantity\\s*\\d+", "")
                .replaceAll("(?i),?\\s*qty\\s*\\d+", "")
                .replaceAll("(?i),?\\s*price\\s*\\d+(?:\\.\\d+)?(?:k)?(?:\\s*rupees)?(?:\\s*each)?", "")
                .replaceAll("(?i),?\\s*rate\\s*\\d+(?:\\.\\d+)?(?:k)?(?:\\s*rupees)?(?:\\s*each)?", "")
                .replaceAll("(?i),?\\s*at\\s*\\d+(?:\\.\\d+)?(?:k)?(?:\\s*rupees)?(?:\\s*each)?", "")
                .replaceAll("(?i),?\\s*for\\s*\\d+(?:\\.\\d+)?(?:k)?(?:\\s*rupees)?(?:\\s*each)?", "")
                .replaceAll("(?i)\\b\\d+(?:\\.\\d+)?k\\s*(?:rupees)?\\s*each\\b", "")
                .replaceAll("(?i)\\b\\d+\\s*(?:rupees)?\\s*each\\b", "")
                .replaceAll("(?i)^\\d+\\s+(?:boxes of|box of|pcs of|units of)?", "")
                .replaceAll("(?i)\\brupees\\b", "")
                .replaceAll("(?i)\\beach\\b", "")
                .replaceAll("^[\\s,:\\.]+", "")
                .replaceAll("[\\s,:\\.]+$", "").trim();

        if (description.isBlank()) {
            description = "Item " + lineNum;
        }

        return new ParsedItemDto(lineNum, description, quantity, unit, unitPrice, BigDecimal.ZERO);
    }

    private BigDecimal parsePriceValue(String str) {
        if (str == null) return BigDecimal.ZERO;
        String s = str.toLowerCase().trim();
        if (s.endsWith("k")) {
            double val = Double.parseDouble(s.replace("k", "").trim()) * 1000;
            return BigDecimal.valueOf(val);
        }
        return new BigDecimal(s);
    }

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
            "(?i)\\b(?:zero|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety|hundred|thousand|lakh|lacs|lac|million|and)(?:[\\s\\-]+(?:zero|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety|hundred|thousand|lakh|lacs|lac|million|and))*\\b"
    );

    /**
     * Converts spoken English number words into numeric digits.
     * E.g. "Three Dell laptops at fifty-five thousand rupees each" -> "3 Dell laptops at 55000 rupees each"
     * "four wireless keyboards at one thousand two hundred rupees each" -> "4 wireless keyboards at 1200 rupees each"
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
