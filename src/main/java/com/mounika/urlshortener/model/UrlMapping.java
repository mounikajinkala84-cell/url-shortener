package com.mounika.urlshortener.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlMapping {
    private String shortCode;
    private String longUrl;
    private Instant createdAt;
    private Instant expiresAt;
    private long clickCount;
}
