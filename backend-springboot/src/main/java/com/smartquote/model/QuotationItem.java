package com.smartquote.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "quotation_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationItem {

    @Id
    private String id;

    @NotBlank
    @Field("quotationId")
    private String quotationId;

    @NotBlank
    @Field("productName")
    private String productName;

    private String description;

    @NotNull
    @Positive
    private Integer quantity;

    @NotNull
    @PositiveOrZero
    @Field("unitPrice")
    private Double unitPrice;

    @NotNull
    @PositiveOrZero
    @Field("taxRate")
    private Double taxRate;

    @NotNull
    @PositiveOrZero
    @Field("totalPrice")
    private Double totalPrice;
}
