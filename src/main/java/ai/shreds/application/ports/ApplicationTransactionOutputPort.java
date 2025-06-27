package ai.shreds.application.ports;

/**
 * Output port for transaction management operations.
 * This interface defines the contract for managing transactions
 * at the application layer level.
 */
public interface ApplicationTransactionOutputPort {

    /**
     * Begins a new transaction.
     * This method starts a new transaction context.
     */
    void beginTransaction();

    /**
     * Commits the current transaction.
     * This method commits all changes made within the current transaction.
     */
    void commitTransaction();

    /**
     * Rolls back the current transaction.
     * This method reverts all changes made within the current transaction.
     */
    void rollbackTransaction();

    /**
     * Executes an operation within a transaction.
     * This method provides a convenient way to run operations within
     * a transaction boundary with automatic commit/rollback handling.
     *
     * @param operation The operation to execute within the transaction
     */
    void runInTransaction(Runnable operation);

    /**
     * Executes an operation within a transaction and returns a result.
     * This method provides a convenient way to run operations within
     * a transaction boundary with automatic commit/rollback handling.
     *
     * @param operation The operation to execute within the transaction
     * @param <T> The type of the return value
     * @return The result of the operation
     */
    <T> T runInTransaction(java.util.function.Supplier<T> operation);
}