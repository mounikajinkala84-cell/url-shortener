package com.mounika.urlshortener.repository;

import com.mounika.urlshortener.model.UrlMapping;
import com.mounika.urlshortener.model.UrlMappingEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.time.Instant;
import java.util.Optional;

/**
 * DynamoDB-backed repository for URL mappings.
 * Activated when aws.dynamodb.enabled=true in application.properties.
 *
 * Table design:
 *   - Table name: url-shortener
 *   - Partition key: shortCode (String)
 *   - No sort key needed (each short code is unique)
 *
 * Why DynamoDB:
 *   - O(1) lookup by short code (partition key)
 *   - Scales automatically to millions of URLs
 *   - Built-in TTL support for auto-expiring links
 *   - Pay per request pricing for variable traffic
 */
@Repository
@ConditionalOnProperty(name = "aws.dynamodb.enabled", havingValue = "true")
public class DynamoDbUrlRepository extends UrlRepository {

    private final DynamoDbTable<UrlMappingEntity> table;

    public DynamoDbUrlRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table("url-shortener",
                TableSchema.fromBean(UrlMappingEntity.class));
    }

    @Override
    public void save(UrlMapping mapping) {
        // Also save to in-memory (parent) for fast reads
        super.save(mapping);

        // Persist to DynamoDB
        UrlMappingEntity entity = toEntity(mapping);
        table.putItem(entity);
    }

    @Override
    public Optional<UrlMapping> findByShortCode(String shortCode) {
        // Try in-memory first (cache hit)
        Optional<UrlMapping> cached = super.findByShortCode(shortCode);
        if (cached.isPresent()) {
            return cached;
        }

        // Fall back to DynamoDB
        Key key = Key.builder().partitionValue(shortCode).build();
        UrlMappingEntity entity = table.getItem(key);
        if (entity == null) {
            return Optional.empty();
        }

        UrlMapping mapping = fromEntity(entity);
        super.save(mapping); // Cache it in memory
        return Optional.of(mapping);
    }

    @Override
    public void incrementClickCount(String shortCode) {
        super.incrementClickCount(shortCode);

        // Update DynamoDB
        findByShortCode(shortCode).ifPresent(mapping -> {
            UrlMappingEntity entity = toEntity(mapping);
            table.updateItem(entity);
        });
    }

    private UrlMappingEntity toEntity(UrlMapping mapping) {
        UrlMappingEntity entity = new UrlMappingEntity();
        entity.setShortCode(mapping.getShortCode());
        entity.setLongUrl(mapping.getLongUrl());
        entity.setCreatedAt(mapping.getCreatedAt().toEpochMilli());
        entity.setExpiresAt(mapping.getExpiresAt() != null ? mapping.getExpiresAt().toEpochMilli() : 0);
        entity.setClickCount(mapping.getClickCount());
        return entity;
    }

    private UrlMapping fromEntity(UrlMappingEntity entity) {
        return UrlMapping.builder()
                .shortCode(entity.getShortCode())
                .longUrl(entity.getLongUrl())
                .createdAt(Instant.ofEpochMilli(entity.getCreatedAt()))
                .expiresAt(entity.getExpiresAt() > 0 ? Instant.ofEpochMilli(entity.getExpiresAt()) : null)
                .clickCount(entity.getClickCount())
                .build();
    }
}
