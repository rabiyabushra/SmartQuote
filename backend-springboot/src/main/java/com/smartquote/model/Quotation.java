package com.smartquote.model;

import com.smartquote.model.enums.QuotationPriority;
import com.smartquote.model.enums.QuotationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "quotations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quotation {

    @Id
    private String id;

    @NotBlank
    @Field("quotationNumber")
    private String quotationNumber;

    @NotBlank
    @Field("businessId")
    private String businessId;

    @NotBlank
    @Field("customerId")
    private String customerId;

    @Field("templateId")
    private String templateId;

    @NotNull
    private QuotationPriority priority;

    @NotNull
    private QuotationStatus status;

    @Valid
    @Builder.Default
    private List<QuotationItem> items = new ArrayList<>();

    @NotNull
    @PositiveOrZero
    private Double subtotal;

    @NotNull
    @PositiveOrZero
    private Double cgst;

    @NotNull
    @PositiveOrZero
    private Double sgst;

    @NotNull
    @PositiveOrZero
    private Double igst;

    @NotNull
    @PositiveOrZero
    @Field("gstPercentage")
    private Double gstPercentage;

    @NotNull
    @PositiveOrZero
    @Field("grandTotal")
    private Double grandTotal;

    @CreatedDate
    @Field("createdAt")
    private LocalDateTime createdAt;
}
