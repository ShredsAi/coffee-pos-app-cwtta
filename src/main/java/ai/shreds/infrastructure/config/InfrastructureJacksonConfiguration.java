package ai.shreds.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson configuration for handling Java 8 date/time types.
 * Registers the JavaTimeModule to support serialization and deserialization of
 * Instant, LocalDate, LocalDateTime, etc., and disables timestamps.
 */
@Configuration
public class InfrastructureJacksonConfiguration {

    /**
     * Configures Jackson ObjectMapper to support Java 8 date/time and disable timestamps.
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
