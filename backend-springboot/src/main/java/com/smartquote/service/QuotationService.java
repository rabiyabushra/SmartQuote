// QuotationService.java
package com.smartquote.service;

import com.smartquote.model.Business;
import com.smartquote.model.Contact;
import com.smartquote.model.Quotation;
import com.smartquote.repository.BusinessRepository;
import com.smartquote.repository.ContactRepository;
import com.smartquote.repository.QuotationRepository;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Orchestrates quotation creation and updates: assign quotation_number,
 * run GST calculation, persist. Section 4.2 of the file structure plan.
 */
@Service
public class QuotationService {

    private static final Set<String> VALID_STATUSES =
            Set.of("draft", "sent", "accepted", "rejected", "expired");

    private final QuotationRepository quotationRepository;
    private final BusinessRepository businessRepository;
    private final ContactRepository contactRepository;
    private final GstCalculationService gstCalculationService;
    private final MongoOperations mongoOperations;

    public QuotationService(QuotationRepository quotationRepository,
                            BusinessRepository businessRepository,
                            ContactRepository contactRepository,
                            GstCalculationService gstCalculationService,
                            MongoOperations mongoOperations) {
        this.quotationRepository = quotationRepository;
        this.businessRepository = businessRepository;
        this.contactRepository = contactRepository;
        this.gstCalculationService = gstCalculationService;
        this.mongoOperations = mongoOperations;
    }

    /** Creates a new quotation: assigns quotation_number, runs GST calc, saves. */
    public Quotation createQuotation(Quotation quotation) {
        Business business = businessRepository.findById(quotation.getBusinessId())
                .orElseThrow(() -> new IllegalArgumentException("Business not found: " + quotation.getBusinessId()));
        Contact contact = contactRepository.findById(quotation.getContactId())
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + quotation.getContactId()));

        quotation.setQuotationNumber(generateQuotationNumber());
        quotation.setStatus("draft");
        quotation.setIsDeleted(false);
        quotation.setCreatedOn(Instant.now());
        if (quotation.getQuotationDate() == null) {
            quotation.setQuotationDate(java.time.LocalDate.now());
        }

        gstCalculationService.calculate(quotation, business.getState(), contact.getState());

        return quotationRepository.save(quotation);
    }

    /**
     * Updates an existing quotation's editable fields and re-runs GST calc
     * (line items, discount, dates, addresses, template/bank selection can change;
     * quotation_number and businessId are immutable once created).
     */
    public Quotation updateQuotation(String quotationId, Quotation updatedFields) {
        Quotation existing = getQuotation(quotationId);

        if (updatedFields.getContactId() != null) existing.setContactId(updatedFields.getContactId());
        if (updatedFields.getTemplateId() != null) existing.setTemplateId(updatedFields.getTemplateId());
        if (updatedFields.getBankId() != null) existing.setBankId(updatedFields.getBankId());
        if (updatedFields.getDueDate() != null) existing.setDueDate(updatedFields.getDueDate());
        if (updatedFields.getPriority() != null) existing.setPriority(updatedFields.getPriority());
        if (updatedFields.getBillingAddress() != null) existing.setBillingAddress(updatedFields.getBillingAddress());
        if (updatedFields.getDeliveryAddress() != null) existing.setDeliveryAddress(updatedFields.getDeliveryAddress());
        if (updatedFields.getDiscountPercentage() != null) existing.setDiscountPercentage(updatedFields.getDiscountPercentage());
        if (updatedFields.getItems() != null) existing.setItems(updatedFields.getItems());

        Business business = businessRepository.findById(existing.getBusinessId())
                .orElseThrow(() -> new IllegalStateException("Business not found: " + existing.getBusinessId()));
        Contact contact = contactRepository.findById(existing.getContactId())
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + existing.getContactId()));

        gstCalculationService.calculate(existing, business.getState(), contact.getState());
        existing.setUpdatedOn(Instant.now());

        return quotationRepository.save(existing);
    }

    public Quotation getQuotation(String quotationId) {
        return quotationRepository.findById(quotationId)
                .filter(q -> !Boolean.TRUE.equals(q.getIsDeleted()))
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found: " + quotationId));
    }

    /** List of a business's non-deleted quotations — ordered newest first for immediate dashboard display. */
    public List<Quotation> listQuotations(String businessId) {
        return quotationRepository.findByBusinessIdAndIsDeletedFalseOrderByCreatedOnDesc(businessId);
    }

    /** Draft -> Sent -> Accepted/Rejected/Expired. Called after PDF send / WhatsApp share / manual update. */
    public Quotation updateStatus(String quotationId, String newStatus) {
        if (!VALID_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }
        Quotation quotation = getQuotation(quotationId);
        quotation.setStatus(newStatus);
        quotation.setUpdatedOn(Instant.now());
        return quotationRepository.save(quotation);
    }

    /** Soft-delete per schema note: "use instead of overwriting status". */
    public void softDelete(String quotationId) {
        Quotation quotation = getQuotation(quotationId);
        quotation.setIsDeleted(true);
        quotation.setUpdatedOn(Instant.now());
        quotationRepository.save(quotation);
    }

    /** Atomically assigns QT-{year}-{4-digit sequence}, e.g. QT-2026-0001. Sequence resets per calendar year. */
    private String generateQuotationNumber() {
        int year = Instant.now().atZone(ZoneOffset.UTC).getYear();
        String counterKey = "quotation_number_" + year;

        Query query = new Query(where("_id").is(counterKey));
        Update update = new Update().inc("seq", 1);
        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true).upsert(true);

        Counter counter = mongoOperations.findAndModify(query, update, options, Counter.class, "counters");
        long seq = counter != null ? counter.getSeq() : 1;

        return String.format("QT-%d-%04d", year, seq);
    }

    /** Internal document backing the atomic sequence — lives in its own "counters" collection, no repository needed. */
    private static class Counter {
        private String id;
        private long seq;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public long getSeq() { return seq; }
        public void setSeq(long seq) { this.seq = seq; }
    }
}