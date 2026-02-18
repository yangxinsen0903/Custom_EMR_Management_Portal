package com.sunbox.sdpvmsr.configuration;

import com.sunbox.web.BaseCommonInterFace;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author : [niyang]
 * @className : AsyncConfig
 * @description : [描述说明该类的功能]
 * @createTime : [2023/2/20 12:11 AM]
 */
//@EnableAsync
//@Configuration("queryDeleteJob")
public class QueryDeleteJobAsyncConfig implements AsyncConfigurer, BaseCommonInterFace {

    private ThreadPoolTaskExecutor executorA;

    @Value("${sdp.vmsr.queryDeleteJob.coresize:40}")
    private Integer threadPoolCoreSize;

    @Value("${sdp.vmsr.queryDeleteJob.maxsize:300}")
    private Integer threadPoolMaxSize;

    @Value("${sdp.vmsr.queryDeleteJob.querycapacity:1000}")
    private Integer threadPoolQueryCapacity;

    @Value("${sdp.vmsr.queryDeleteJob.keepaliveseconds:60}")
    private Integer threadPoolKeepAliveSeconds;

    public ThreadPoolTaskExecutor getExecutorA() {
        return executorA;
    }

    public void setExecutorA(ThreadPoolTaskExecutor executorA) {
        this.executorA = executorA;
    }

    @Override
    public Executor getAsyncExecutor() {
        if (executorA == null) {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(threadPoolCoreSize); //核心线程数
            executor.setMaxPoolSize(threadPoolMaxSize);  //最大线程数
            executor.setQueueCapacity(threadPoolQueryCapacity); //队列大小
            executor.setKeepAliveSeconds(threadPoolKeepAliveSeconds); //线程最大空闲时间
            executor.setThreadNamePrefix("queryDeleteJob-");
            // 拒绝策略
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
            executor.initialize();
            getLogger().info("queryDeleteJob设置线程池。");
            executorA=executor;
            return executor;
        }else{
            return executorA;
        }
    }

    // 异常处理器：
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
         return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(Throwable arg0, Method arg1, Object... arg2) {
                getLogger().error("=========================="+arg0.getMessage()+"=======================", arg0);
                getLogger().error("exception method:"+arg1.getName());
            }
        };
    }
}
