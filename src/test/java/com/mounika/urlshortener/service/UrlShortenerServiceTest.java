package com.mounika.urlshortener.service;

import com.mounika.urlshortener.model.ShortenRequest;
import com.mounika.urlshortener.model.ShortenResponse;
import com.mounika.urlshortener.model.UrlStats;
import com.mounika.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlShortenerServiceTest {

    private UrlShortenerService service;
    private UrlRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UrlRepository();
        service = new UrlShortenerService(repository);
    }

    @Test
    void shortenUrl_generatesRandomCode() {
        ShortenRequest request = new ShortenRequest();
        request.setUrl("https://www.google.com");

        ShortenResponse response = service.shortenUrl(request);

        assertNotNull(response.getShortCode());
        assertEquals(7, response.getShortCode().length());
        assertEquals("https://www.google.com", response.getLongUrl());
        assertEquals("never", response.getExpiresAt());
    }

    @Test
    void shortenUrl_withCustomCode() {
        ShortenRequest request = new ShortenRequest();
        request.setUrl("https://www.amazon.com");
        request.setCustomCode("amzn");

        ShortenResponse response = service.shortenUrl(request);

        assertEquals("amzn", response.getShortCode());
        assertEquals("https://www.amazon.com", response.getLongUrl());
    }

    @Test
    void shortenUrl_withExpiry() {
        ShortenRequest request = new ShortenRequest();
        request.setUrl("https://www.google.com");
        request.setExpiryMinutes(60L);

        ShortenResponse response = service.shortenUrl(request);

        assertNotEquals("never", response.getExpiresAt());
    }

    @Test
    void shortenUrl_duplicateCustomCodeThrowsException() {
        ShortenRequest request1 = new ShortenRequest();
        request1.setUrl("https://www.google.com");
        request1.setCustomCode("test");
        service.shortenUrl(request1);

        ShortenRequest request2 = new ShortenRequest();
        request2.setUrl("https://www.amazon.com");
        request2.setCustomCode("test");

        assertThrows(IllegalArgumentException.class, () -> service.shortenUrl(request2));
    }

    @Test
    void resolve_validCode_returnsLongUrl() {
        ShortenRequest request = new ShortenRequest();
        request.setUrl("https://www.google.com");
        request.setCustomCode("goog");
        service.shortenUrl(request);

        String result = service.resolve("goog");

        assertEquals("https://www.google.com", result);
    }

    @Test
    void resolve_invalidCode_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> service.resolve("nonexistent"));
    }

    @Test
    void resolve_incrementsClickCount() {
        ShortenRequest request = new ShortenRequest();
        request.setUrl("https://www.google.com");
        request.setCustomCode("click");
        service.shortenUrl(request);

        service.resolve("click");
        service.resolve("click");
        service.resolve("click");

        UrlStats stats = service.getStats("click");
        assertEquals(3, stats.getClickCount());
    }

    @Test
    void getStats_returnsCorrectData() {
        ShortenRequest request = new ShortenRequest();
        request.setUrl("https://www.github.com");
        request.setCustomCode("gh");
        service.shortenUrl(request);

        UrlStats stats = service.getStats("gh");

        assertEquals("gh", stats.getShortCode());
        assertEquals("https://www.github.com", stats.getLongUrl());
        assertEquals(0, stats.getClickCount());
        assertEquals("never", stats.getExpiresAt());
    }

    @Test
    void getStats_invalidCode_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> service.getStats("invalid"));
    }

    @Test
    void resolve_expiredLink_throwsException() {
        ShortenRequest request = new ShortenRequest();
        request.setUrl("https://www.google.com");
        request.setCustomCode("expired");
        request.setExpiryMinutes(0L); // expires immediately

        service.shortenUrl(request);

        // Wait a tiny bit for expiry
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        assertThrows(IllegalArgumentException.class, () -> service.resolve("expired"));
    }
}
