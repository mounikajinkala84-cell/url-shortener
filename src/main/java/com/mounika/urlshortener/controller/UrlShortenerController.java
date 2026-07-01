package com.mounika.urlshortener.controller;

import com.mounika.urlshortener.model.ShortenRequest;
import com.mounika.urlshortener.model.ShortenResponse;
import com.mounika.urlshortener.model.UrlStats;
import com.mounika.urlshortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    public UrlShortenerController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    /**
     * POST /shorten — Create a short URL
     */
    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(@Valid @RequestBody ShortenRequest request) {
        ShortenResponse response = urlShortenerService.shortenUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /{code} — Redirect to original URL
     */
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String longUrl = urlShortenerService.resolve(code);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", longUrl);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * GET /stats/{code} — Get URL statistics
     */
    @GetMapping("/stats/{code}")
    public ResponseEntity<UrlStats> getStats(@PathVariable String code) {
        UrlStats stats = urlShortenerService.getStats(code);
        return ResponseEntity.ok(stats);
    }

    /**
     * Global exception handler for this controller
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
