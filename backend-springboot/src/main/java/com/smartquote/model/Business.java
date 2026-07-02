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

@Document(collection = "businesses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Business {

    @Id
    private String id;

    @NotBlank
    @Field("businessName")
    private String businessName;

    @NotBlank
    @Field("ownerName")
    private String ownerName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(max = 15)
    private String phone;

    @Size(min = 15, max = 15)
    private String gstin;

    @NotBlank
    private String state;

    @Field("bankDetails")
    private String bankDetails;

    @CreatedDate
    @Field("createdAt")
    private LocalDateTime createdAt;
}
