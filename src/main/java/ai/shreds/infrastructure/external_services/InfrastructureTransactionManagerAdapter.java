package ai.shreds.infrastructure.external_services;

import ai.shreds.application.ports.ApplicationTransactionOutputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Infrastructure adapter for transaction management.
 * Uses Spring's transaction management infrastructure.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InfrastructureTransactionManagerAdapter implements ApplicationTransactionOutputPort {

    private final PlatformTransactionManager transactionManager;
    private final TransactionTemplate transactionTemplate;
    
    @Value("${transaction.default-timeout:30}")
    private int defaultTransactionTimeout;
    
    private ThreadLocal<TransactionStatus> currentTransaction = new ThreadLocal<>();

    @Override
    public void beginTransaction() {
        if (currentTransaction.get() != null) {
            throw new IllegalStateException("Transaction already in progress");
        }
        
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        definition.setTimeout(defaultTransactionTimeout);
        
        TransactionStatus status = transactionManager.getTransaction(definition);
        currentTransaction.set(status);
        log.debug("Transaction started");
    }

    @Override
    public void commitTransaction() {
        TransactionStatus status = currentTransaction.get();
        if (status == null) {
            throw new IllegalStateException("No active transaction to commit");
        }
        
        transactionManager.commit(status);
        currentTransaction.remove();
        log.debug("Transaction committed");
    }

    @Override
    public void rollbackTransaction() {
        TransactionStatus status = currentTransaction.get();
        if (status == null) {
            throw new IllegalStateException("No active transaction to rollback");
        }
        
        transactionManager.rollback(status);
        currentTransaction.remove();
        log.debug("Transaction rolled back");
    }

    @Override
    public void runInTransaction(Runnable operation) {
        transactionTemplate.executeWithoutResult(status -> {
            try {
                operation.run();
            } catch (Exception e) {
                log.error("Transaction operation failed", e);
                throw e; // Re-throw to trigger rollback
            }
        });
    }
}