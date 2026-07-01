package com.mounika.urlshortener.service;

import com.mounika.urlshortener.model.ShortenRequest;
import com.mounika.urlshortener.model.ShortenResponse;
import com.mounika.urlshortener.model.UrlMapping;
import com.mounika.urlshortener.model.UrlStats;
import com.mounika.urlshortener.repository.UrlRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class UrlShortenerService {

    private static final String BASE_URL = "http://localhost:8080/";
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 7;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UrlRepository urlRepository;

    public UrlShortenerService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public ShortenResponse shortenUrl(ShortenRequest request) {
        String shortCode;

        if (request.getCustomCode() != null && !request.getCustomCode().isBlank()) {
            shortCode = request.getCustomCode();
            if (urlRepository.existsByShortCode(shortCode)) {
                throw new IllegalArgumentException("Custom code already in use: " + shortCode);
            }
        } else {
            shortCode = generateUniqueCode();
        }

        Instant now = Instant.now();
        Instant expiresAt = request.getExpiryMinutes() != null
                ? now.plus(request.getExpiryMinutes(), ChronoUnit.MINUTES)
                : null;

        UrlMapping mapping = UrlMapping.builder()
                .shortCode(shortCode)
                .longUrl(request.getUrl())
                .createdAt(now)
                .expiresAt(expiresAt)
                .clickCount(0)
                .build();

        urlRepository.save(mapping);

        return ShortenResponse.builder()
                .shortCode(shortCode)
                .shortUrl(BASE_URL + shortCode)
                .longUrl(request.getUrl())
                .expiresAt(expiresAt != null ? expiresAt.toString() : "never")
                .build();
    }

    public String resolve(String shortCode) {
        UrlMapping mapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Short code not found: " + shortCode));

        if (mapping.getExpiresAt() != null && Instant.now().isAfter(mapping.getExpiresAt())) {
            throw new IllegalArgumentException("Link has expired: " + shortCode);
        }

        urlRepository.incrementClickCount(shortCode);
        return mapping.getLongUrl();
    }

    public UrlStats getStats(String shortCode) {
        UrlMapping mapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Short code not found: " + shortCode));

        return UrlStats.builder()
                .shortCode(mapping.getShortCode())
                .longUrl(mapping.getLongUrl())
                .clickCount(mapping.getClickCount())
                .createdAt(mapping.getCreatedAt().toString())
                .expiresAt(mapping.getExpiresAt() != null ? mapping.getExpiresAt().toString() : "never")
                .build();
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (urlRepository.existsByShortCode(code));
        return code;
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
