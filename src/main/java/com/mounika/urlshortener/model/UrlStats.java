package com.mounika.urlshortener.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UrlStats {
    private String shortCode;
    private String longUrl;
    private long clickCount;
    private String createdAt;
    private String expiresAt;
}
