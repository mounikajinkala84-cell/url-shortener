package com.mounika.urlshortener.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ShortenResponse {
    private String shortCode;
    private String shortUrl;
    private String longUrl;
    private String expiresAt;
}
