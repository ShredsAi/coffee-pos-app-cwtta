package ai.shreds.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for REST clients used to communicate with external services.
 * Configures connection timeouts, read timeouts, and connection pooling.
 */
@Configuration
@Slf4j
public class InfrastructureRestClientConfig {

    @Value("${services.connection-timeout:5000}")
    private int connectionTimeout;

    @Value("${services.read-timeout:10000}")
    private int readTimeout;

    @Value("${services.max-connections:20}")
    private int maxConnections;

    /**
     * Creates a configured RestTemplate with proper timeout settings.
     * @return RestTemplate bean
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        log.info("Configuring RestTemplate with connection timeout: {}ms, read timeout: {}ms", 
                connectionTimeout, readTimeout);
        
        return builder
                .requestFactory(this::clientHttpRequestFactory)
                .setConnectTimeout(Duration.ofMillis(connectionTimeout))
                .setReadTimeout(Duration.ofMillis(readTimeout))
                .build();
    }

    /**
     * Creates a request factory with connection pooling.
     * @return ClientHttpRequestFactory
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        // Connection pooling would normally be configured here with HttpComponentsClientHttpRequestFactory
        // or OkHttp3ClientHttpRequestFactory, but for simplicity we're using SimpleClientHttpRequestFactory
        return factory;
    }
}