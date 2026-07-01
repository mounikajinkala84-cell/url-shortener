package com.mounika.urlshortener.repository;

import com.mounika.urlshortener.model.UrlMapping;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UrlRepository {

    private final Map<String, UrlMapping> store = new ConcurrentHashMap<>();

    public void save(UrlMapping mapping) {
        store.put(mapping.getShortCode(), mapping);
    }

    public Optional<UrlMapping> findByShortCode(String shortCode) {
        return Optional.ofNullable(store.get(shortCode));
    }

    public boolean existsByShortCode(String shortCode) {
        return store.containsKey(shortCode);
    }

    public void incrementClickCount(String shortCode) {
        store.computeIfPresent(shortCode, (key, mapping) -> {
            mapping.setClickCount(mapping.getClickCount() + 1);
            return mapping;
        });
    }
}
