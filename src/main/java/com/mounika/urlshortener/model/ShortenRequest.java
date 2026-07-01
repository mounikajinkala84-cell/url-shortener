package com.mounika.urlshortener.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShortenRequest {
    @NotBlank(message = "URL cannot be blank")
    private String url;
    private String customCode;  // optional custom short code
    private Long expiryMinutes; // optional TTL in minutes
}
