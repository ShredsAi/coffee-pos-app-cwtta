package ai.shreds.domain.value_objects;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.lang.reflect.Method;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Value object representing an audit entry for domain entities.
 */
public final class DomainAuditEntry {
    private final UUID entityId;
    private final String entityType;
    private final String changeType;
    private final Map<String, Object> beforeState;
    private final Map<String, Object> afterState;
    private final Instant changedAt;
    private final String changedBy;

    private static final ObjectMapper MAPPER = createConfiguredMapper();

    private DomainAuditEntry(UUID entityId,
                              String entityType,
                              String changeType,
                              Map<String, Object> beforeState,
                              Map<String, Object> afterState,
                              Instant changedAt,
                              String changedBy) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.changeType = changeType;
        this.beforeState = beforeState;
        this.afterState = afterState;
        this.changedAt = changedAt;
        this.changedBy = changedBy;
    }

    private static ObjectMapper createConfiguredMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getChangeType() {
        return changeType;
    }

    public Map<String, Object> getBeforeState() {
        return beforeState;
    }

    public Map<String, Object> getAfterState() {
        return afterState;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    /**
     * Creates an audit entry for an INSERT operation.
     */
    public static DomainAuditEntry forCreate(Object entity, String entityType, String userId) {
        UUID id = extractId(entity);
        Map<String, Object> after = toMap(entity);
        return new DomainAuditEntry(id, entityType, "INSERT", null, after, Instant.now(), userId);
    }

    /**
     * Creates an audit entry for an UPDATE operation.
     */
    public static DomainAuditEntry forUpdate(Object beforeEntity,
                                             Object afterEntity,
                                             String entityType,
                                             String userId) {
        UUID id = extractId(afterEntity);
        Map<String, Object> before = toMap(beforeEntity);
        Map<String, Object> after = toMap(afterEntity);
        return new DomainAuditEntry(id, entityType, "UPDATE", before, after, Instant.now(), userId);
    }

    /**
     * Creates an audit entry for a DELETE operation.
     */
    public static DomainAuditEntry forDelete(Object entity, String entityType, String userId) {
        UUID id = extractId(entity);
        Map<String, Object> before = toMap(entity);
        return new DomainAuditEntry(id, entityType, "DELETE", before, null, Instant.now(), userId);
    }

    private static UUID extractId(Object entity) {
        try {
            Method getId = entity.getClass().getMethod("getId");
            Object idValue = getId.invoke(entity);
            if (idValue instanceof UUID) {
                return (UUID) idValue;
            } else {
                return UUID.fromString(idValue.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract id from entity", e);
        }
    }

    private static Map<String, Object> toMap(Object obj) {
        return MAPPER.convertValue(obj, new TypeReference<Map<String, Object>>() {});
    }
}