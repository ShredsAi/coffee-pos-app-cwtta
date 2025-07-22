package ai.shreds.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Configuration for application event handling.
 * Sets up async event publishing with proper thread pool management.
 */
@Configuration
@Slf4j
public class InfrastructureEventConfig {

    @Value("${events.thread-pool.core-size:2}")
    private int corePoolSize;

    @Value("${events.thread-pool.max-size:5}")
    private int maxPoolSize;

    @Value("${events.thread-pool.queue-capacity:100}")
    private int queueCapacity;

    @Value("${events.async-enabled:true}")
    private boolean asyncEnabled;

    /**
     * Configures the application event multicaster for async event processing.
     * @return ApplicationEventMulticaster bean
     */
    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        
        if (asyncEnabled) {
            eventMulticaster.setTaskExecutor(taskExecutor());
            log.info("Configured async event multicaster with thread pool");
        } else {
            log.info("Configured sync event multicaster");
        }
        
        return eventMulticaster;
    }

    /**
     * Task executor for async event processing.
     * @return TaskExecutor bean
     */
    @Bean(name = "eventTaskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("Event-");
        // Use ThreadPoolExecutor.CallerRunsPolicy for rejected tasks
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        log.info("Configured event task executor - core: {}, max: {}, queue: {}", 
                corePoolSize, maxPoolSize, queueCapacity);
        
        return executor;
    }
}