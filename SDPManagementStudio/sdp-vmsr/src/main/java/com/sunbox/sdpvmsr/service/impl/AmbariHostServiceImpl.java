package com.sunbox.sdpvmsr.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import com.sunbox.domain.InfoClusterAmbariHostDelete;
import com.sunbox.sdpvmsr.service.IAmbariHostService;
import com.sunbox.service.INeoAmbariService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author : [niyang]
 * @className : AmbariHostServiceImpl
 * @description : [描述说明该类的功能]
 * @createTime : [2023/8/20 8:06 AM]
 */
@Service
public class AmbariHostServiceImpl implements IAmbariHostService, BaseCommonInterFace {

    @Autowired
    private INeoAmbariService neoAmbariService;


    @Value("${sdp.vmsr.sendDeleteAmbariReqpool.coresize:120}")
    private Integer threadSendDeleteReqpoolCoreSize;

    @Value("${sdp.vmsr.sendDeleteAmbariReqpool.maxsize:300}")
    private Integer threadSendDeleteReqpoolMaxSize;

    @Value("${sdp.vmsr.sendDeleteAmbariReqpool.querycapacity:1000}")
    private Integer threadSendDeleteReqpoolQueryCapacity;

    @Value("${sdp.vmsr.sendDeleteAmbariReqpool.keepaliveseconds:60}")
    private Integer threadSendDeleteReqpoolKeepAliveSeconds;


    @Value("${sdp.vmsr.sendDeleteAmbariReq.thread.timeout:600}")
    private Long sendDeleteReqThreadTimeOutSeconds;

    @Value("${sdp.vmsr.sendDeleteAmbariHostReq.thread.timeout:750}")
    private Long sendDeleteAmbariHosthreadTimeOutSeconds;


    @Autowired
    private DistributedRedisLock redisLock;

    private static ThreadPoolExecutor deleteExecutor;


    @PostConstruct
    public void init() {

        ThreadFactory sendThreadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("deleteAmbariHost-pool-" + threadNumber.getAndIncrement());
                return thread;
            }
        };

        deleteExecutor = new ThreadPoolExecutor(threadSendDeleteReqpoolCoreSize, threadSendDeleteReqpoolMaxSize,
                threadSendDeleteReqpoolKeepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(threadSendDeleteReqpoolQueryCapacity), sendThreadFactory, new ThreadPoolExecutor.AbortPolicy());

    }


    @Scheduled(fixedDelay = 60000L)
    @Override
    public void aSyncDeleteAmbariHost() {
        String lockKey="asyncgetambarihost_leasetime";

        boolean lock = redisLock.tryLock(lockKey,TimeUnit.SECONDS,1,sendDeleteAmbariHosthreadTimeOutSeconds);
        try{
            if (!lock){
                getLogger().info("getDeleteAmbariHost未获取到锁，退出，等待下一个周期。");
                return;
            }
            List<InfoClusterAmbariHostDelete> deleteList =
                    neoAmbariService.getNeedClearAmbariHost();
            if (deleteList==null || deleteList.size()==0){
                getLogger().info("未获取到DeleteList数据");
                return;
            }

            getLogger().info("获取到DeleteList数量：{}",deleteList.size());

            Map<String,List<InfoClusterAmbariHostDelete>> map=deleteList.stream().collect(Collectors.groupingBy(item->{
                return item.getClusterId();
            }));

            getLogger().info("需要处理到集群数量：{}",map.size());
            List<Future<?>> threads = new CopyOnWriteArrayList<>();

            for (Map.Entry<String, List<InfoClusterAmbariHostDelete>> entry : map.entrySet()) {
                String clusterId = entry.getKey();
                List<InfoClusterAmbariHostDelete> deleteHosts = entry.getValue();

                Future<?> submit = deleteExecutor.submit(() -> {
                    MDC.put("clusterId",clusterId);
                    neoAmbariService.deleteAmbariHosts(deleteHosts);
                    MDC.remove("clusterId");
                });
                threads.add(submit);
                ThreadUtil.sleep(100);
            }


            Long begTime = System.currentTimeMillis()/1000;
            //endregion
            getLogger().info("发起删除请求结束。");

            //region 等待任务线程超时
            for (Future<?> future:threads){
                try {
                    if (!future.isDone()) {
                        Long endTime = System.currentTimeMillis()/1000;
                        Long dur = getTimeOut(begTime,endTime,sendDeleteReqThreadTimeOutSeconds);
                        // 等待任务完成，最多等待75秒
                        future.get(dur, TimeUnit.SECONDS);
                        getLogger().info("等待{}秒后，完成发送删除请求任务",(System.currentTimeMillis()/1000-endTime));
                    }else{
                        getLogger().info("发送删除请求任务完成");
                    }
                } catch (InterruptedException e) {
                    // 线程被中断异常处理
                    getLogger().error("线程被中断异常处理",e);
                } catch (ExecutionException e) {
                    // 任务执行异常处理
                    getLogger().error("任务执行异常处理",e);
                } catch (TimeoutException e) {
                    // 超时异常处理
                    future.cancel(true); // 取消任务执行
                    getLogger().error("超时异常处理",e);
                } catch (Exception e){
                    getLogger().error("查询异常",e);
                }
            }
            getLogger().info("deleteExecutor线程池中Active数量：{}",deleteExecutor.getActiveCount());
            //endregion


        }catch (Exception e){
            getLogger().error("处理异常，",e);
        }finally {
            if (redisLock.isLocked(lockKey)){
                redisLock.unlock(lockKey);
            }
        }
    }

    private  long getTimeOut(long begTime,long endTimeOut,Long timeOut){
        Long dur = timeOut-(endTimeOut-begTime);
        if (dur<0){
            return 1L;
        }else{
            return dur;
        }
    }
}
