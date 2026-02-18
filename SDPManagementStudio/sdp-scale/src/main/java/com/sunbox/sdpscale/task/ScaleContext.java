package com.sunbox.sdpscale.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sunbox.sdpscale.constant.ScaleConstant;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.util.IPUtils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;

@Component
public class ScaleContext implements InitializingBean, ScaleConstant, BaseCommonInterFace {
    @Autowired
    private DistributedRedisLock redisLock;

    @Value("${metric.collect.server.expire.time:300}")
    private Integer serverExpireTime;

    @Value("${metric.expire.time:300}")
    private Integer metricExpireTime;

    public static String ip;

    public static ScaleContext getInstance() {
        return SpringUtil.getBean(ScaleContext.class);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ip = IPUtils.getIpAddress();
        expireServerState();
        expireMetrics();
    }

    /**
     * 定时任务将Scale服务的状态信息写入redis,
     * 因为会同时运行多个Scale服务,每个运行中的Scale服务都需要注册进Redis,并且定期刷新,
     * 如果Scale服务挂了,则需要删除该Scale服务的状态信息
     */
    @Scheduled(cron = "${scale.active.check.expire.time}")
    public void expireServerState() {
        getLogger().info("检查弹性伸缩服务状态-开始");
        boolean tryLock = false;
        List<String> expireList = new ArrayList<>();
        try {
            // 检查并清理过期服务
            tryLock = redisLock.tryLock(metric_machine_check, TimeUnit.SECONDS, 200, 300);
            if (!tryLock) return;

            try {
                //redission版本会导致获取list报错
                redisLock.getList(metric_machine_ips);
                redisLock.getList(lock_metrics);
            } catch (Exception ex) {
                redisLock.delete(metric_machine_ips);
                redisLock.delete(lock_metrics);
            }

            // 更新过期时间
            Integer index = redisLock.listContainValue(metric_machine_ips, ip);
            String newValue = ip + "_" + DateUtil.current();
            if (index != null) {
                redisLock.listSetValue(metric_machine_ips, index, newValue);
            } else {
                // 重新加入
                redisLock.addList(metric_machine_ips, newValue);
            }

            // 检查并清理过期指标
            List<String> list = redisLock.getList(metric_machine_ips);
            for (String ips : list) {
                List<String> split = StrUtil.split(ips, "_");
                String last = CollUtil.getLast(split);
                Long serverTime = Convert.toLong(last);
                if (DateUtil.current() - serverTime > TimeUnit.SECONDS.toMillis(serverExpireTime)) {
                    redisLock.removeValueFromList(metric_machine_ips, ips);
                    expireList.add(ips);
                }
            }
            getLogger().info("检查弹性伸缩服务状态-完成,list={}", expireList);
        } catch (Exception ex) {
            getLogger().error("检查弹性伸缩服务状态-异常", ex);
        } finally {
            if (tryLock) {
                redisLock.unlock(metric_machine_check);
            }
        }
    }

    @Scheduled(cron = "${metric.active.check.expire.time}")
    public void expireMetrics() {
        getLogger().info("检查弹性伸缩指标状态-开始");
        List<String> expireList = new ArrayList<>();
        boolean tryLock = false;
        try {
            // 检查并清理过期指标
            tryLock = redisLock.tryLock(metrics_check, TimeUnit.SECONDS, 200, 300);
            if (!tryLock) return;

            List<String> ipMetrics = redisLock.getList(lock_metrics);
            for (String ipMetric : ipMetrics) {
                List<String> split = StrUtil.split(ipMetric, "_");
                String last = CollUtil.getLast(split);
                Long serverTime = Convert.toLong(last);
                if (DateUtil.current() - serverTime > TimeUnit.SECONDS.toMillis(metricExpireTime)) {
                    redisLock.removeValueFromList(lock_metrics, ipMetric);
                    expireList.add(ipMetric);
                }
            }
            getLogger().error("检查弹性伸缩指标状态-完成,list={}", expireList);
        } catch (Exception ex) {
            getLogger().error("检查弹性伸缩指标状态-异常", ex);
        } finally {
            if (tryLock) {
                redisLock.unlock(metrics_check);
            }
        }
    }

    public int scaleServerCount() {
        return redisLock.listSize(metric_machine_ips);
    }

    public Integer scaleServerIndex() {
        return redisLock.listContainValue(metric_machine_ips, ip);
    }
}
