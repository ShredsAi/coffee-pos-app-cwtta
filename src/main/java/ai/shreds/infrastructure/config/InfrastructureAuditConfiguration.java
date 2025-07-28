package ai.shreds.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

/**
 * Configuration for JPA auditing and entity change tracking.
 * Handles automatic population of audit fields and entity change events.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class InfrastructureAuditConfiguration {

    /**
     * Provides the current user ID for JPA auditing (@CreatedBy, @LastModifiedBy)
     * Gets the user from Spring Security context or defaults to 'system'
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("system");
            }
            
            return Optional.of(authentication.getName());
        };
    }

    /**
     * Handles entity change events for audit trail creation.
     * This will be called after successful database commits.
     */
    @TransactionalEventListener
    public void handleEntityChangeEvent(EntityChangeEvent event) {
        // This method will be implemented to write audit records
        // when domain events are fired after entity changes
        getCurrentAuditor().ifPresent(auditor -> {
            // Audit logging logic will be implemented when audit repositories are ready
            System.out.println(String.format("Entity %s %s by %s at %s", 
                event.getEntityType(), 
                event.getChangeType(), 
                auditor, 
                event.getTimestamp()));
        });
    }

    /**
     * Gets the current auditor (user) from the security context
     */
    private Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of("system");
        }
        
        return Optional.of(authentication.getName());
    }

    /**
     * Simple event class to represent entity changes
     */
    public static class EntityChangeEvent {
        private final String entityType;
        private final String changeType;
        private final Object entityId;
        private final long timestamp;

        public EntityChangeEvent(String entityType, String changeType, Object entityId) {
            this.entityType = entityType;
            this.changeType = changeType;
            this.entityId = entityId;
            this.timestamp = System.currentTimeMillis();
        }

        public String getEntityType() {
            return entityType;
        }

        public String getChangeType() {
            return changeType;
        }

        public Object getEntityId() {
            return entityId;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}