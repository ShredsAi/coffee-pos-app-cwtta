package ai.shreds.infrastructure.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * JPA configuration for product catalog infrastructure layer.
 * Sets up EntityManagerFactory, TransactionManager, JPA repositories scanning.
 * DataSource is auto-configured by Spring Boot based on application properties.
 * Note: JPA auditing is configured in InfrastructureAuditConfiguration.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "ai.shreds.infrastructure.repositories")
public class InfrastructureJpaConfiguration {

    @Value("${spring.jpa.hibernate.ddl-auto:validate}")
    private String ddlAuto;

    @Value("${spring.jpa.properties.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}")
    private String hibernateDialect;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size:20}")
    private String batchSize;

    @Value("${spring.jpa.properties.hibernate.order_inserts:true}")
    private String orderInserts;

    @Value("${spring.jpa.properties.hibernate.format_sql:false}")
    private String formatSql;

    @Value("${spring.jpa.show-sql:false}")
    private String showSql;

    /**
     * Creates the EntityManagerFactory with Hibernate as the JPA provider.
     * DataSource is auto-injected by Spring Boot's auto-configuration.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, 
            @Qualifier("hibernateProperties") Properties hibernateProperties) {
        
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("ai.shreds.infrastructure.entities");
        em.setJpaVendorAdapter(jpaVendorAdapter());
        em.setJpaProperties(hibernateProperties);
        
        return em;
    }

    /**
     * Hibernate property configuration
     * Respects application.yml settings and can be overridden by test properties
     */
    @Bean
    @Qualifier("hibernateProperties")
    public Properties hibernateProperties() {
        Properties properties = new Properties();
        
        // Respect application.yml configuration and test overrides
        properties.setProperty("hibernate.hbm2ddl.auto", ddlAuto);
        properties.setProperty("hibernate.dialect", hibernateDialect);
        properties.setProperty("hibernate.jdbc.batch_size", batchSize);
        properties.setProperty("hibernate.order_inserts", orderInserts);
        properties.setProperty("hibernate.format_sql", formatSql);
        properties.setProperty("hibernate.show_sql", showSql);
        
        return properties;
    }

    /**
     * Configure the JPA vendor adapter (Hibernate)
     */
    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    /**
     * Configure the transaction manager
     */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
}