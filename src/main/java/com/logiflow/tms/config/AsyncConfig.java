package com.logiflow.tms.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

/** Exécution asynchrone sur threads virtuels (Java 25), adaptée aux tâches I/O-bound du TMS. */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

  @Override
  public Executor getAsyncExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return (ex, method, params) ->
        org.slf4j.LoggerFactory.getLogger(AsyncConfig.class)
            .error("Exception non interceptée dans la tâche asynchrone {}", method.getName(), ex);
  }

  @Bean
  public Executor applicationTaskExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
