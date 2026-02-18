package com.sunbox.runtime;

import com.netflix.discovery.DiscoveryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaServiceRegistry;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.atomic.AtomicInteger;

public class RuntimeManager {
    private static Logger logger= LoggerFactory.getLogger(RuntimeManager.class);
    private static final Object lockObj = new Object();
    private ConfigurableApplicationContext applicationContext;
    private boolean isInitiated = false;
    private boolean isShuttingDown = false;
    private boolean isShutDown = false;
    private boolean isApiRequestBlocked = false;
    private AtomicInteger execCounter = new AtomicInteger(0);

    public void initiate(ConfigurableApplicationContext applicationContext) {
        logger.info(Thread.currentThread().getName() + " -> RuntimeManager initiate applicationContext:" + applicationContext);
        if (this.isInitiated) {
            logger.info(Thread.currentThread().getName() + " -> RuntimeManager initiate return isInitiated:" + isInitiated);
            return;
        }
        this.isInitiated = true;

        this.applicationContext = applicationContext;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop();
        }));
    }

    private void unregisterFromEureka() {

        logger.info(Thread.currentThread().getName() + " -> unregisterFromEureka begin");
        /*EurekaRegistration eurekaRegistration = applicationContext.getBean(EurekaRegistration.class);
        logger.info(Thread.currentThread().getName() + " -> unregisterFromEureka eurekaRegistration:" + eurekaRegistration);
        EurekaServiceRegistry eurekaServiceRegistry = applicationContext.getBean(EurekaServiceRegistry.class);
        eurekaServiceRegistry.deregister(eurekaRegistration);*/
        DiscoveryManager.getInstance().shutdownComponent();
        logger.info(Thread.currentThread().getName() + " -> unregisterFromEureka end");
    }

    public boolean isShuttingDown() {
        return this.isShuttingDown;
    }

    /**
     * 判断是否能接收api请求
     * @return true不能接收，false可以接收　
     */
    public boolean isApiRequestBlocked() {
        return isApiRequestBlocked;
    }

    /**
     * 执行方法并增加计数器
     *
     * @param runnable 执行的具体逻辑
     */
    public void withCounter(Runnable runnable) {
        execCounter.incrementAndGet();
        try {
            runnable.run();
        } finally {
            execCounter.decrementAndGet();
        }
    }

    private void stop() {
        if (isShuttingDown) {
            logger.info(Thread.currentThread().getName() + " -> RuntimeManager shutdown return isShuttingDown:true");
            return;
        }

        synchronized (lockObj){
            if (isShuttingDown) {
                System.out.println(Thread.currentThread().getName() + " -> RuntimeManager shutdown return isShuttingDown:true");
                return;
            }
            logger.info(Thread.currentThread().getName() + " -> RuntimeManager shutdown isShuttingDown = true");
            isShuttingDown = true;
        }
        logger.info(Thread.currentThread().getName() + " -> RuntimeManager shutdown begin");

        //region eureka unregister
        unregisterFromEureka();

        //endregion
        ICloseProcess closeProcess = CloseEventPorcess.getCloseProcess();
        if (closeProcess!=null) {
            closeProcess.closeProcess();
        }
        try {
            Thread.sleep(5000);
            logger.info("");
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
        isApiRequestBlocked = true;

        long beginWaitTicks = System.currentTimeMillis();
        int execCounterValue = execCounter.get();
        while (execCounterValue > 0) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                logger.error("",e);
            }
            if (System.currentTimeMillis() - beginWaitTicks > 120000) {
                logger.error(Thread.currentThread().getName() + " -> RuntimeManager shutdown wait execCounter:" + execCounterValue + " timeout");
                break;
            }
            execCounterValue = execCounter.get();
        }
        logger.info(Thread.currentThread().getName() + " -> RuntimeManager shutdown end");
        isShutDown = true;
    }

    public void stopAndWait() {
        logger.info("");
        stop();
        while (!isShutDown) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("RuntimeManager stopAndWait",e);
            }
        }
    }
}
