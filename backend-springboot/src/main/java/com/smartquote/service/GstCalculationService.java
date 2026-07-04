package com.smartquote.service;

import com.smartquote.model.Quotation;
import com.smartquote.model.Quotation.QuotationItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Implements the GST Logic sheet's derivation rule:
 *   business.state == contact.state -> intra-state -> CGST + SGST (rate / 2 each)
 *   business.state != contact.state -> inter-state -> IGST (full rate)
 *
 * Stateless — QuotationService calls calculate(quotation, businessState, contactState)
 * right before save, and again before PDF render if line items changed.
 */
@Service
public class GstCalculationService {

    private static final int SCALE = 2;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    public boolean isInterstate(String businessState, String contactState) {
        if (businessState == null || contactState == null) return false;
        return !businessState.trim().equalsIgnoreCase(contactState.trim());
    }

    /** Populates every GST + total field on the quotation and its line items. */
    public void calculate(Quotation quotation, String businessState, String contactState) {
        boolean interstate = isInterstate(businessState, contactState);
        quotation.setIsInterstate(interstate);

        List<QuotationItem> items = quotation.getItems();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal cgstTotal = BigDecimal.ZERO;
        BigDecimal sgstTotal = BigDecimal.ZERO;
        BigDecimal igstTotal = BigDecimal.ZERO;

        if (items != null) {
            for (QuotationItem item : items) {
                calculateLineItem(item, interstate);
                subtotal = subtotal.add(nvl(item.getAmount()));
                cgstTotal = cgstTotal.add(nvl(item.getCgstAmount()));
                sgstTotal = sgstTotal.add(nvl(item.getSgstAmount()));
                igstTotal = igstTotal.add(nvl(item.getIgstAmount()));
            }
        }

        BigDecimal discountPercentage = nvl(quotation.getDiscountPercentage());
        BigDecimal discountAmount = subtotal
                .multiply(discountPercentage)
                .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);

        BigDecimal taxAmount = cgstTotal.add(sgstTotal).add(igstTotal);
        BigDecimal grandTotal = subtotal.add(taxAmount).subtract(discountAmount);

        quotation.setSubtotal(round(subtotal));
        quotation.setDiscountAmount(round(discountAmount));
        quotation.setCgstAmount(round(cgstTotal));
        quotation.setSgstAmount(round(sgstTotal));
        quotation.setIgstAmount(round(igstTotal));
        quotation.setTaxAmount(round(taxAmount));
        quotation.setGrandTotal(round(grandTotal));

        // Header-level % fields are only meaningful when every line item shares
        // one tax rate (the common case). With mixed rates per line, the % is
        // left null and the amount fields above remain the source of truth.
        BigDecimal uniformRate = uniformTaxRate(items);
        if (uniformRate != null) {
            if (interstate) {
                quotation.setCgstPercentage(BigDecimal.ZERO);
                quotation.setSgstPercentage(BigDecimal.ZERO);
                quotation.setIgstPercentage(uniformRate);
            } else {
                BigDecimal half = uniformRate.divide(TWO, SCALE, RoundingMode.HALF_UP);
                quotation.setCgstPercentage(half);
                quotation.setSgstPercentage(half);
                quotation.setIgstPercentage(BigDecimal.ZERO);
            }
        } else {
            quotation.setCgstPercentage(null);
            quotation.setSgstPercentage(null);
            quotation.setIgstPercentage(null);
        }
    }

    /** Computes amount, cgst/sgst/igst split, tax_amount, and total_amount for one line item. */
    public void calculateLineItem(QuotationItem item, boolean interstate) {
        BigDecimal quantity = nvl(item.getQuantity());
        BigDecimal rate = nvl(item.getRate());
        BigDecimal amount = round(quantity.multiply(rate));
        item.setAmount(amount);

        BigDecimal taxPercentage = nvl(item.getTaxPercentage());

        if (interstate) {
            item.setCgstPercentage(BigDecimal.ZERO);
            item.setCgstAmount(BigDecimal.ZERO);
            item.setSgstPercentage(BigDecimal.ZERO);
            item.setSgstAmount(BigDecimal.ZERO);
            item.setIgstPercentage(taxPercentage);
            item.setIgstAmount(percentOf(amount, taxPercentage));
        } else {
            BigDecimal half = taxPercentage.divide(TWO, SCALE, RoundingMode.HALF_UP);
            item.setCgstPercentage(half);
            item.setCgstAmount(percentOf(amount, half));
            item.setSgstPercentage(half);
            item.setSgstAmount(percentOf(amount, half));
            item.setIgstPercentage(BigDecimal.ZERO);
            item.setIgstAmount(BigDecimal.ZERO);
        }

        BigDecimal lineTax = nvl(item.getCgstAmount())
                .add(nvl(item.getSgstAmount()))
                .add(nvl(item.getIgstAmount()));
        item.setTaxAmount(round(lineTax));
        item.setTotalAmount(round(amount.add(lineTax)));
    }

    private BigDecimal uniformTaxRate(List<QuotationItem> items) {
        if (items == null || items.isEmpty()) return null;
        BigDecimal first = nvl(items.get(0).getTaxPercentage());
        boolean allSame = items.stream()
                .allMatch(i -> nvl(i.getTaxPercentage()).compareTo(first) == 0);
        return allSame ? first : null;
    }

    private BigDecimal percentOf(BigDecimal base, BigDecimal percentage) {
        return round(base.multiply(percentage).divide(HUNDRED, 4, RoundingMode.HALF_UP));
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}