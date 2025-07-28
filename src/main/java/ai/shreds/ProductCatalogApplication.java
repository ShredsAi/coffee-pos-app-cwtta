package ai.shreds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot application class for Product Catalog Shred.
 * This is the entry point for the application.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"ai.shreds"})
@EntityScan(basePackages = {"ai.shreds.infrastructure.entities"})
@EnableJpaRepositories(basePackages = {"ai.shreds.infrastructure.repositories"})
public class ProductCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductCatalogApplication.class, args);
    }
}
