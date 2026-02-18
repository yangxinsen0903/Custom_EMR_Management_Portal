package com.sunbox.sdpvmsr.service.impl;

import cn.hutool.core.thread.ThreadUtil;

import com.sunbox.dao.mapper.InfoClusterVmDeleteMapper;
import com.sunbox.domain.InfoClusterVmDelete;
import com.sunbox.domain.server.Sys;
import com.sunbox.sdpvmsr.configuration.QueryDeleteJobAsyncConfig;
import com.sunbox.sdpvmsr.configuration.SendDeleteReqAsyncConfig;
import com.sunbox.sdpvmsr.service.IVMAsyncDeleteService;
import com.sunbox.sdpvmsr.service.IVMDeleteProcessService;
import com.sunbox.service.IVMDeleteService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : [niyang]
 * @className : VMAsyncDeleteServiceImpl
 * @description : [描述说明该类的功能]
 * @createTime : [2023/7/30 9:04 PM]
 */
@Service
public class VMAsyncDeleteServiceImpl  implements IVMAsyncDeleteService, BaseCommonInterFace {

    @Value("${sdp.vm.senddelete.timeout:300}")
    private Integer sdpSendDeleteTimeOut;

    @Value("${sdp.vm.deleting.timeout:600}")
    private Integer sdpSendDeletingTimeOut;

    @Value("${sdp.vmsr.sendDeleteReqpool.coresize:100}")
    private Integer threadSendDeleteReqpoolCoreSize;

    @Value("${sdp.vmsr.sendDeleteReqpool.maxsize:300}")
    private Integer threadSendDeleteReqpoolMaxSize;

    @Value("${sdp.vmsr.sendDeleteReqpool.querycapacity:1000}")
    private Integer threadSendDeleteReqpoolQueryCapacity;

    @Value("${sdp.vmsr.sendDeleteReqpool.keepaliveseconds:60}")
    private Integer threadSendDeleteReqpoolKeepAliveSeconds;

    @Value("${sdp.vmsr.queryDeleteJob.coresize:100}")
    private Integer threadPoolCoreSize;

    @Value("${sdp.vmsr.queryDeleteJob.maxsize:300}")
    private Integer threadPoolMaxSize;

    @Value("${sdp.vmsr.queryDeleteJob.querycapacity:1000}")
    private Integer threadPoolQueryCapacity;

    @Value("${sdp.vmsr.queryDeleteJob.keepaliveseconds:60}")
    private Integer threadPoolKeepAliveSeconds;

    @Value("${sdp.vmsr.queryDeleteJob.locktime:120}")
    private Long queryDeleteJobLockTimeSeconds;

    @Value("${sdp.vmsr.queryDeleteJob.thread.timeout:75}")
    private Long queryDeleteJobThreadTimeOutSeconds;


    @Value("${sdp.vmsr.sendDeleteReq.locktime:120}")
    private Long sendDeleteReqLockTimeSeconds;

    @Value("${sdp.vmsr.sendDeleteReq.thread.timeout:75}")
    private Long sendDeleteReqThreadTimeOutSeconds;

    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private IVMDeleteService ivmDeleteService;

    @Autowired
    private InfoClusterVmDeleteMapper infoClusterVmDeleteMapper;

    @Autowired
    private IVMDeleteProcessService deleteProcessService;

    private static ThreadPoolExecutor sendExecutor;

    private static ThreadPoolExecutor queryExecutor;

    @PostConstruct
    public void init() {

        ThreadFactory sendThreadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("send-pool-" + threadNumber.getAndIncrement());
                return thread;
            }
        };

        sendExecutor = new ThreadPoolExecutor(threadSendDeleteReqpoolCoreSize, threadSendDeleteReqpoolMaxSize,
                threadSendDeleteReqpoolKeepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(threadSendDeleteReqpoolQueryCapacity), sendThreadFactory, new ThreadPoolExecutor.AbortPolicy());

        ThreadFactory queryThreadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("query-pool-" + threadNumber.getAndIncrement());
                return thread;
            }
        };

        queryExecutor = new ThreadPoolExecutor(threadPoolCoreSize, threadPoolMaxSize,
                threadPoolKeepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(threadPoolQueryCapacity), queryThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 发送删除申请
     */
    @Scheduled(fixedDelay = 30000L)
    @Override
    public void sendDeleteRequest() {
        String lockKey="asyncgetdeletevm_leasetime";

        boolean lock = redisLock.tryLock(lockKey,TimeUnit.SECONDS,1,sendDeleteReqLockTimeSeconds);

        try{
            if (!lock){
                getLogger().info("未获取到锁，退出，等待下一个周期。");
                return;
            }

            List<InfoClusterVmDelete> deleteList =
                    ivmDeleteService.getNeedDeleteVM();

            if (deleteList !=null && deleteList.size() >0){
                //region 更新状态
                for (InfoClusterVmDelete vm:deleteList){
                    ivmDeleteService.updateInfoDeleteVMBeforeSendDeleteRequest(vm);
                }
                //endregion

                //region 释放锁
                if (redisLock.isLocked(lockKey)){
                    redisLock.unlock(lockKey);
                }
                //endregion
                List<Future<?>> threads = new CopyOnWriteArrayList<>();
                //region 发起删除请求
                getLogger().info("发起删除请求开始。");
                for (InfoClusterVmDelete vm:deleteList){
                    if (sendExecutor.getActiveCount()>threadSendDeleteReqpoolCoreSize){
                        getLogger().info("sendExecutor线程池核心队列大于核心队列长度，核心长度：{},当前数量:{}",
                                sendExecutor.getActiveCount(),threadSendDeleteReqpoolCoreSize);
                        ThreadUtil.sleep(1000);
                    }
                    Future<?> submit = sendExecutor.submit(() -> {
                        MDC.put("clusterId",vm.getClusterId());
                        MDC.put("planId",vm.getVmName());
                        deleteProcessService.deleteVMSendReq(vm);
                        MDC.remove("clusterId");
                        MDC.remove("planId");
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
                getLogger().info("sendExecutor线程池中Active数量：{}",sendExecutor.getActiveCount());
                //endregion

            }else{
                getLogger().info("未查询要删除的vm，等待下一个周期，退出");
                return;
            }
        }catch (Exception e){
            getLogger().error("发送删除请求异常，",e);
        }finally {
            if (redisLock.isLocked(lockKey)){
                redisLock.unlock(lockKey);
            }
        }
    }


    private static long getTimeOut(long begTime,long endTimeOut,Long timeOut){
        Long dur = timeOut-(endTimeOut-begTime);
        if (dur<0){
            return 1L;
        }else{
            return dur;
        }
    }

    /**
     * 查询删除申请结果
     */
    @Scheduled(fixedDelay = 30000L)
    @Override
    public void getDeleteJobResult() {
        String lockKey = "asyncgetdeletingvm";

        boolean lock = redisLock.tryLock(lockKey,TimeUnit.SECONDS,1,queryDeleteJobLockTimeSeconds);

        try {
            if (!lock) {
                getLogger().info("未获取到锁，退出，等待下一个周期。");
                return;
            }
            List<InfoClusterVmDelete> deletingVMs = ivmDeleteService.getDeletingVM();
            List<Future<?>> threads = new CopyOnWriteArrayList<>();
            //region 发送查询请求
            for (InfoClusterVmDelete vm : deletingVMs){
                if (queryExecutor.getActiveCount()>threadSendDeleteReqpoolCoreSize){
                    getLogger().info("queryExecutor线程池活跃线程数大于核心数量，核心数量：{},当前数量:{}",
                            queryExecutor.getActiveCount(),threadSendDeleteReqpoolCoreSize);
                    ThreadUtil.sleep(1000);
                }
                Future<?> submit = sendExecutor.submit(() -> {
                    MDC.put("clusterId",vm.getClusterId());
                    MDC.put("planId",vm.getVmName());
                    deleteProcessService.deleteJobQuery(vm);
                    MDC.remove("clusterId");
                    MDC.remove("planId");
                });
                threads.add(submit);
                ThreadUtil.sleep(100);
            }

            //endregion 发送查询请求

            Long begTime = System.currentTimeMillis()/1000;

            //region 等待任务线程超时
            for (Future<?> future:threads){
                try {
                    if (!future.isDone()) {
                        Long endTime = System.currentTimeMillis()/1000;
                        Long dur = getTimeOut(begTime,endTime,queryDeleteJobThreadTimeOutSeconds);
                        // 等待任务完成，最多等待多少秒
                        future.get(dur, TimeUnit.SECONDS);
                        getLogger().info("等待{}秒后，完成查询状态任务",(System.currentTimeMillis()/1000-endTime));
                    }else{
                        getLogger().info("查询状态任务已完成");
                    }
                } catch (InterruptedException e) {
                    // 线程被中断异常处理
                    getLogger().error("线程被中断异常处理",e);
                } catch (ExecutionException e) {
                    // 任务执行异常处理
                    getLogger().error("任务执行异常处理",e);
                } catch (TimeoutException e) {
                    // 超时异常处理
                    // 取消任务执行
                    future.cancel(true);
                    getLogger().error("超时异常处理",e);
                } catch (Exception e){
                    getLogger().error("查询异常",e);
                }
            }
            getLogger().info("queryExecutor线程池中Active数量：{}",queryExecutor.getActiveCount());
            //endregion

        }catch (Exception e){
            getLogger().error("getDeleteJobResult",e);
        }finally {
            if (redisLock.isLocked(lockKey)){
                redisLock.unlock(lockKey);
            }
        }
    }


    @Scheduled(fixedDelay = 60000L)
    @Override
    public void timeOutScan() {
        String lockKey = "timeoutscan";
        boolean lock = redisLock.tryLock(lockKey,TimeUnit.SECONDS,1,120);
        try {
            if (!lock) {
                getLogger().info("未获取到锁，退出，等待下一个周期。");
                return;
            }
            List<InfoClusterVmDelete> sendreqtimeouts =
                    ivmDeleteService.getSendRequestTimeOutVms(sdpSendDeleteTimeOut);
            getLogger().info("获取到请求超时的数据：{}",sendreqtimeouts);

            sendreqtimeouts.stream().forEach(x->{
                deleteProcessService.deleteVMFailed(x);
            });

            List<InfoClusterVmDelete> deleteingTimeOutVms =
                    ivmDeleteService.getDeletingTimeOutVms(sdpSendDeletingTimeOut);
            getLogger().info("获取到删除超时的数据：{}",deleteingTimeOutVms);

            deleteingTimeOutVms.stream().forEach(t->{
                deleteProcessService.deleteVMFailed(t);
            });
            Thread.sleep(2000L);
        }catch (Exception e){
            getLogger().error("查询deletevm删除超时处理异常",e);
        }finally {
            if (redisLock.isLocked(lockKey)){
                redisLock.unlock(lockKey);
            }
        }

    }

    /**
     * 释放冻结的任务
     */
    @Scheduled(fixedDelay = 60000L)
    @Override
    public void releaseFreeze() {
        String lockKey = "releaseFreeze";
        boolean lock = redisLock.tryLock(lockKey,TimeUnit.SECONDS,1,120);
        try {
            if (!lock) {
                getLogger().info("未获取到releaseFreeze锁，退出，等待下一个周期。");
                return;
            }
            List<InfoClusterVmDelete> infoClusterVmDeleteList =
                    ivmDeleteService.getNeedReleaseFreeze();
            getLogger().info("获取到需要解除冻结状态的VM：{}", infoClusterVmDeleteList);

            infoClusterVmDeleteList.stream().forEach(x -> {
                x.setStatus(0);
                x.setRetryCount(0);
                x.setModifiedTime(new Date());
                infoClusterVmDeleteMapper.updateByPrimaryKeySelective(x);
            });
            Thread.sleep(2000L);
        }catch (Exception e){
            getLogger().error("解除冻结状态处理异常",e);
        }finally {
            if (redisLock.isLocked(lockKey)){
                redisLock.unlock(lockKey);
            }
        }
    }
}
