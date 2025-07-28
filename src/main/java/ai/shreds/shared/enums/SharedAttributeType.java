package ai.shreds.shared.enums;

/**
 * Enumeration of supported attribute data types for product attributes.
 * Each type determines the kind of value that can be stored for an attribute.
 * As specified in UML diagram.
 */
public enum SharedAttributeType {
    TEXT,
    NUMBER,
    BOOLEAN,
    DATE,
    SELECT_SINGLE,
    SELECT_MULTIPLE
}