package ai.shreds.domain.ports;

import ai.shreds.domain.value_objects.DomainAuditEntry;

/**
 * Domain Output Port Audit Writer
 * Interface for audit logging operations
 * Implemented by infrastructure layer to handle audit trail persistence
 */
public interface DomainOutputPortAuditWriter {
    
    /**
     * Writes a product audit entry
     * 
     * @param entry The audit entry containing change information
     */
    void writeProductAudit(DomainAuditEntry entry);
    
    /**
     * Writes a category audit entry
     * 
     * @param entry The audit entry containing change information
     */
    void writeCategoryAudit(DomainAuditEntry entry);
    
    /**
     * Writes an attribute audit entry
     * 
     * @param entry The audit entry containing change information
     */
    void writeAttributeAudit(DomainAuditEntry entry);
    
    /**
     * Writes a media audit entry
     * 
     * @param entry The audit entry containing change information
     */
    void writeMediaAudit(DomainAuditEntry entry);
    
    /**
     * Writes a generic audit entry
     * 
     * @param entry The audit entry containing change information
     * @param entityType The type of entity being audited
     */
    void writeAudit(DomainAuditEntry entry, String entityType);
}