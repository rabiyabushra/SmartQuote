package com.smartquote.service;

import com.smartquote.dto.ParsedQuotationResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class QuotationParserServiceTest {

    private QuotationParserService quotationParserService;

    @BeforeEach
    void setUp() {
        quotationParserService = new QuotationParserService();
    }

    @Test
    @DisplayName("Example 1: Spoken number words for Ravi Traders with 18 percent GST")
    void testExample1_SpokenNumbersRaviTraders() {
        String transcript = "Create a quotation for Ravi Traders. Two HP LaserJet printers at twelve thousand rupees each, and five boxes of A4 paper at four hundred and fifty rupees each. Add 18 percent GST.";

        ParsedQuotationResponseDto result = quotationParserService.parseQuotationTranscript(transcript);

        assertNotNull(result);
        assertEquals("Ravi Traders", result.getCustomerName());
        assertEquals(BigDecimal.valueOf(18), result.getTaxPercentage());
        assertTrue(result.getItems().size() >= 2);

        // Item 1: HP LaserJet printers
        var item1 = result.getItems().get(0);
        assertEquals(BigDecimal.valueOf(2), item1.getQuantity());
        assertEquals(BigDecimal.valueOf(12000), item1.getRate());
        assertEquals(BigDecimal.valueOf(24000).setScale(2), item1.getLineTotal());

        // Item 2: A4 paper
        var item2 = result.getItems().get(1);
        assertEquals(BigDecimal.valueOf(5), item2.getQuantity());
        assertEquals(BigDecimal.valueOf(450), item2.getRate());
        assertEquals(BigDecimal.valueOf(2250).setScale(2), item2.getLineTotal());

        // Subtotal & Grand Total
        assertEquals(BigDecimal.valueOf(26250).setScale(2), result.getSubtotal());
        assertEquals(BigDecimal.valueOf(4725).setScale(2), result.getTaxAmount());
        assertEquals(BigDecimal.valueOf(30975).setScale(2), result.getGrandTotal());
    }

    @Test
    @DisplayName("Example 2 variations: '10 chairs 800 each', 'ten chairs at eight hundred rupees each', '2 tables 5k each'")
    void testExample2_ChairAndTableVariations() {
        // Variation A: Numeric digits and 5k
        String transcriptA = "10 chairs 800 each, 2 tables 5k each.";
        ParsedQuotationResponseDto resA = quotationParserService.parseQuotationTranscript(transcriptA);
        assertTrue(resA.getItems().size() >= 2);
        assertEquals(BigDecimal.valueOf(10), resA.getItems().get(0).getQuantity());
        assertEquals(BigDecimal.valueOf(800), resA.getItems().get(0).getRate());
        assertEquals(BigDecimal.valueOf(2), resA.getItems().get(1).getQuantity());
        assertEquals(BigDecimal.valueOf(5000.0), resA.getItems().get(1).getRate());

        // Variation B: Spoken words "ten chairs at eight hundred rupees each"
        String transcriptB = "ten chairs at eight hundred rupees each, two tables at five thousand each.";
        ParsedQuotationResponseDto resB = quotationParserService.parseQuotationTranscript(transcriptB);
        assertTrue(resB.getItems().size() >= 2);
        assertEquals(BigDecimal.valueOf(10), resB.getItems().get(0).getQuantity());
        assertEquals(BigDecimal.valueOf(800), resB.getItems().get(0).getRate());
        assertEquals(BigDecimal.valueOf(2), resB.getItems().get(1).getQuantity());
        assertEquals(BigDecimal.valueOf(5000), resB.getItems().get(1).getRate());
    }

    @Test
    @DisplayName("Example 3: '3 laptops at fifteen thousand five hundred each'")
    void testExample3_SpokenFifteenThousandFiveHundred() {
        String transcript = "3 laptops at fifteen thousand five hundred each.";

        ParsedQuotationResponseDto result = quotationParserService.parseQuotationTranscript(transcript);

        assertNotNull(result);
        assertFalse(result.getItems().isEmpty());

        var item = result.getItems().get(0);
        assertEquals(BigDecimal.valueOf(3), item.getQuantity());
        assertEquals(BigDecimal.valueOf(15500), item.getRate());
        assertEquals(BigDecimal.valueOf(46500).setScale(2), item.getLineTotal());
    }

    @Test
    @DisplayName("Example 4: 'Quote for Ramesh Enterprises, add GST'")
    void testExample4_RameshEnterprisesGST() {
        String transcript = "Quote for Ramesh Enterprises, add GST.";

        ParsedQuotationResponseDto result = quotationParserService.parseQuotationTranscript(transcript);

        assertNotNull(result);
        assertEquals("Ramesh Enterprises", result.getCustomerName());
        assertEquals(BigDecimal.valueOf(18), result.getTaxPercentage());
    }

    @Test
    @DisplayName("Example 5: Delivery charges phrase")
    void testExample5_DeliveryCharges() {
        String transcript = "Create quotation for Delta Corp. 5 monitors at 12k each. Include delivery charges of 500.";

        ParsedQuotationResponseDto result = quotationParserService.parseQuotationTranscript(transcript);

        assertNotNull(result);
        assertEquals("Delta Corp", result.getCustomerName());
        assertTrue(result.getItems().size() >= 2);

        boolean foundDelivery = result.getItems().stream()
                .anyMatch(i -> i.getItemDescription().toLowerCase().contains("delivery") && i.getRate().compareTo(BigDecimal.valueOf(500)) == 0);
        assertTrue(foundDelivery, "Delivery charges line item should be extracted");
    }

    @Test
    @DisplayName("Spoken word conversion helper test")
    void testSpokenWordConversion() {
        String input = "two HP LaserJet printers at twelve thousand rupees each";
        String converted = quotationParserService.convertSpokenNumbersToDigits(input);
        assertTrue(converted.contains("2"));
        assertTrue(converted.contains("12000"));
    }

    @Test
    @DisplayName("ABC Enterprises: Compound spoken numbers 'fifty-five thousand' and 'one thousand two hundred'")
    void testAbcEnterprises_CompoundSpokenNumbers() {
        String transcript = "Create a quotation for ABC Enterprises. Three Dell laptops at fifty-five thousand rupees each, four wireless keyboards at one thousand two hundred rupees each, and five Logitech mice at eight hundred rupees each. Add 18 percent GST.";

        ParsedQuotationResponseDto result = quotationParserService.parseQuotationTranscript(transcript);

        assertNotNull(result);
        assertEquals("ABC Enterprises", result.getCustomerName());
        assertEquals(BigDecimal.valueOf(18), result.getTaxPercentage());
        assertEquals(3, result.getItems().size());

        // Item 1: Dell laptops
        var item1 = result.getItems().get(0);
        assertEquals("Dell laptops", item1.getItemDescription());
        assertEquals(BigDecimal.valueOf(3), item1.getQuantity());
        assertEquals(BigDecimal.valueOf(55000), item1.getRate());
        assertEquals(BigDecimal.valueOf(165000).setScale(2), item1.getLineTotal());

        // Item 2: wireless keyboards
        var item2 = result.getItems().get(1);
        assertEquals("wireless keyboards", item2.getItemDescription());
        assertEquals(BigDecimal.valueOf(4), item2.getQuantity());
        assertEquals(BigDecimal.valueOf(1200), item2.getRate());
        assertEquals(BigDecimal.valueOf(4800).setScale(2), item2.getLineTotal());

        // Item 3: Logitech mice
        var item3 = result.getItems().get(2);
        assertEquals("Logitech mice", item3.getItemDescription());
        assertEquals(BigDecimal.valueOf(5), item3.getQuantity());
        assertEquals(BigDecimal.valueOf(800), item3.getRate());
        assertEquals(BigDecimal.valueOf(4000).setScale(2), item3.getLineTotal());

        // Totals
        assertEquals(BigDecimal.valueOf(173800).setScale(2), result.getSubtotal());
        assertEquals(BigDecimal.valueOf(31284).setScale(2), result.getTaxAmount());
        assertEquals(BigDecimal.valueOf(205084).setScale(2), result.getGrandTotal());
    }
}
