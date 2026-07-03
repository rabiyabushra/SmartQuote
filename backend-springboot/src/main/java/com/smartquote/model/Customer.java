package com.smartquote.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

@Document(collection = "customers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    private String id;

    @NotBlank
    @Field("businessId")
    private String businessId;

    @NotBlank
    private String name;

    @Field("companyName")
    private String companyName;

    @Size(max = 15)
    private String phone;

    @Email
    private String email;

    @Size(min = 15, max = 15)
    private String gstin;

    private String state;

    @CreatedDate
    @Field("createdAt")
    private LocalDateTime createdAt;
}
