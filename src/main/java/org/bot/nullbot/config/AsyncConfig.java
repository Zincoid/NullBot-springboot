package org.bot.nullbot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer
{
    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);


    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        // Java 21+：使用虚拟线程（正式功能）
        logger.info("Java 21+ 环境：使用虚拟线程执行器");
        return Executors.newVirtualThreadPerTaskExecutor();

        // Java 19-20：使用虚拟线程（预览功能）
        // logger.info("Java 19-20 环境：使用虚拟线程执行器（预览功能）");
        // return createVirtualThreadExecutorWithPreview();

        // Java 8-18：使用传统线程池
        // logger.info("Java 8-18 环境：使用传统线程池（虚拟线程不可用）");
        // return createTraditionalThreadPool();
    }

    /**
     * 创建传统线程池（Java 8-18 兼容）
     */
    private Executor createTraditionalThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 根据 CPU 核心数配置
        int corePoolSize = Runtime.getRuntime().availableProcessors();

        executor.setCorePoolSize(corePoolSize * 2);      // IO密集型：2倍CPU核心数
        executor.setMaxPoolSize(corePoolSize * 4);       // 最大线程数
        executor.setQueueCapacity(100);                  // 队列容量
        executor.setThreadNamePrefix("async-");          // 线程名前缀
        executor.setKeepAliveSeconds(60);                // 空闲线程存活时间

        // 拒绝策略：调用者运行（避免任务丢失）
        executor.setRejectedExecutionHandler(
                new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()
        );

        executor.initialize();
        return executor;
    }

    /**
     * 使用反射创建虚拟线程执行器（兼容 Java 19-20 预览版）
     */
    private Executor createVirtualThreadExecutorWithPreview() {
        try {
            // 反射调用，避免编译错误
            Method method = Executors.class.getMethod("newVirtualThreadPerTaskExecutor");
            return (Executor) method.invoke(null);
        } catch (Exception e) {
            logger.warn("无法创建虚拟线程执行器，降级到传统线程池", e);
            return createTraditionalThreadPool();
        }
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, obj) -> {
            logger.error("Async method {} threw an exception", method.getName(), throwable);
            for (Object param : obj) {
                logger.info("Parameter value - {}", param);
            }
        };
    }
}
