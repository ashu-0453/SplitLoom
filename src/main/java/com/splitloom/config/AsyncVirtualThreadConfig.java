package com.splitloom.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.Executors;

@Configuration
public class AsyncVirtualThreadConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncVirtualThreadConfig.class);

    @Bean(name = "virtualThreadTaskExecutor")
    public AsyncTaskExecutor virtualThreadTaskExecutor() {
        log.info("Initializing Java 21 Project Loom Virtual Thread Task Executor for SplitLoom");
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
