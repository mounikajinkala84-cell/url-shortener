package com.mounika.urlshortener.repository;

import com.mounika.urlshortener.model.UrlMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UrlRepositoryTest {

    private UrlRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UrlRepository();
    }

    @Test
    void save_and_findByShortCode() {
        UrlMapping mapping = UrlMapping.builder()
                .shortCode("abc123")
                .longUrl("https://www.google.com")
                .createdAt(Instant.now())
                .clickCount(0)
                .build();

        repository.save(mapping);

        Optional<UrlMapping> result = repository.findByShortCode("abc123");
        assertTrue(result.isPresent());
        assertEquals("https://www.google.com", result.get().getLongUrl());
    }

    @Test
    void findByShortCode_notFound_returnsEmpty() {
        Optional<UrlMapping> result = repository.findByShortCode("nonexistent");
        assertTrue(result.isEmpty());
    }

    @Test
    void existsByShortCode_returnsTrue_whenExists() {
        UrlMapping mapping = UrlMapping.builder()
                .shortCode("exists")
                .longUrl("https://www.google.com")
                .createdAt(Instant.now())
                .clickCount(0)
                .build();

        repository.save(mapping);

        assertTrue(repository.existsByShortCode("exists"));
    }

    @Test
    void existsByShortCode_returnsFalse_whenNotExists() {
        assertFalse(repository.existsByShortCode("nope"));
    }

    @Test
    void incrementClickCount_increasesCount() {
        UrlMapping mapping = UrlMapping.builder()
                .shortCode("clicks")
                .longUrl("https://www.google.com")
                .createdAt(Instant.now())
                .clickCount(0)
                .build();

        repository.save(mapping);
        repository.incrementClickCount("clicks");
        repository.incrementClickCount("clicks");

        Optional<UrlMapping> result = repository.findByShortCode("clicks");
        assertTrue(result.isPresent());
        assertEquals(2, result.get().getClickCount());
    }
}
