package com.smartquote.model;

import com.smartquote.model.enums.TemplateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

@Document(collection = "quotation_templates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationTemplate {

    @Id
    private String id;

    @NotBlank
    @Field("businessId")
    private String businessId;

    @NotBlank
    @Field("templateName")
    private String templateName;

    @NotNull
    @Field("templateType")
    private TemplateType templateType;

    @Field("headerLogo")
    private String headerLogo;

    @Field("footerText")
    private String footerText;

    @CreatedDate
    @Field("createdAt")
    private LocalDateTime createdAt;
}
