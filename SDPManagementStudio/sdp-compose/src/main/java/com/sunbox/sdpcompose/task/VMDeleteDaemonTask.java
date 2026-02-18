package com.sunbox.sdpcompose.task;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson.JSONObject;
import com.netflix.discovery.converters.Auto;
import com.sunbox.dao.mapper.InfoClusterNeoMapper;
import com.sunbox.dao.mapper.InfoClusterVmClearLogMapper;
import com.sunbox.domain.*;
import com.sunbox.sdpcompose.mapper.ConfClusterMapper;
import com.sunbox.sdpcompose.mapper.InfoClusterVmMapper;
import com.sunbox.sdpcompose.service.IAzureService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 从 InfoClusterVmClearLog 表中查询需要删除的VM。
 * @author : [niyang]
 * @className : VMDeleteDaemonTask
 * @description : VM资源删除守护任务
 * @createTime : [2023/7/25 3:15 PM]
 */
@Component
public class VMDeleteDaemonTask implements BaseCommonInterFace {

    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private InfoClusterVmClearLogMapper infoClusterVmClearLogMapper;

    @Autowired
    private IAzureService iAzureService;

    @Autowired
    private ConfClusterMapper confClusterMapper;

    @Autowired
    private InfoClusterVmMapper infoClusterVmMapper;

    /**
     *  巡检失败的资源释放任务并发送VM删除申请仅限task任务。
     */
    @Scheduled(fixedDelay = 120000L)
    public void sendDeleteRequest() {

        String rediskey="VMDeleteDaemonTaskKey";
        boolean lock = redisLock.tryLock(rediskey);
        if (!lock) {
            getLogger().error("未拿到VMDeleteDaemonTaskKey锁，返回。");
        }
        try{
            getLogger().info("开始巡检失败的资源释放任务, 并发送VM删除申请(仅限task任务)...");
            //1.获取需要处理的数据
            List<InfoClusterVmClearLog> clearLogs = infoClusterVmClearLogMapper.queryNeedClearVmLog();
            List<InfoClusterVmClearLog> afterFilterLogs =  filterLastCreateTimeLogs(clearLogs);

            if (clearLogs != null && afterFilterLogs.size()>0){
                getLogger().info("查询到需要删除VM:{}条",afterFilterLogs.size());

                for (InfoClusterVmClearLog item:afterFilterLogs){
                    ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(item.getClusterId());
                    if (confCluster.getState().equals(ConfCluster.CREATED)){
                        String region=confCluster.getRegion();
                        InfoClusterVm infoClusterVm = infoClusterVmMapper.selectByPrimaryKey(InfoClusterVmKey.of(item.getClusterId(), item.getVmName()));
                        String hostName = Objects.isNull(infoClusterVm)? "": infoClusterVm.getHostName();
                        ResultMsg msg = iAzureService.deleteVMInstance(item.getVmName(), hostName, region);
                        if (msg.getResult() && msg.getData()!=null){
                            JSONObject response = (JSONObject) msg.getData();
                            String jobid = response.getString("id");
                            item.setStatus(InfoClusterVmClearLog.VM_Clear_Status_Deleting);
                            item.setJobId(jobid);
                            item.setModifiedTime(new Date());
                            infoClusterVmClearLogMapper.updateByPrimaryKeySelective(item);
                        }else{
                            getLogger().error("发送单个删除VM请求异常，vmName:{},异常：{}",item,msg);
                        }
                        ThreadUtil.sleep(1000);
                    }else{
                        continue;
                    }
                }
            }
        }catch (Exception e){
            getLogger().error("删除VM异常，",e);
        }finally {
            try {
                if (lock) {
                    redisLock.unlock(rediskey);
                }
            } catch (Exception e) {
                getLogger().error("VMDeleteDaemonTaskKey, 释放锁异常", e);
            }
        }
    }

    /**
     * 集群可能存在不同的任务中，根据clusterId 和 vmName createTime 过滤重复获取最新的记录
     *
     * @param infoClusterVmClearLogs
     * @return
     */
    private List<InfoClusterVmClearLog> filterLastCreateTimeLogs(List<InfoClusterVmClearLog> infoClusterVmClearLogs){
         List<InfoClusterVmClearLog> resultList = new CopyOnWriteArrayList<>();

         infoClusterVmClearLogs.stream().forEach(x->{

             // resultList 查询相同的clusterid 和 vmName的对象
             Optional<InfoClusterVmClearLog> find = resultList.stream().filter(y->{
                return y.getClusterId().equals(x.getClusterId()) && y.getVmName().equals(x.getVmName());
             }).findFirst();

             // 存在且 时间小于当前的，直接替换
             if (find.isPresent() && x.getCreatedTime().compareTo(find.get().getCreatedTime())>0){
                 resultList.remove(find.get());
                 resultList.add(x);
             }
             if (!find.isPresent()){
                 resultList.add(x);
             }
         });
         return resultList;
    }



    /**
     *  查询删除任务的结果。
     */
    @Scheduled(fixedDelay = 120000L)
    public void queryDeleteResult(){
        String rediskey="VMDeleteDaemonQueryTaskKey";
        boolean lock = redisLock.tryLock(rediskey);
        if (!lock) {
            getLogger().error("VMDeleteDaemonQueryTaskKey，返回。");
        }
        try{
            getLogger().info("开始查询删除任务的结果...");
            List<InfoClusterVmClearLog> deletingLogs = infoClusterVmClearLogMapper.queryDeletingVMTask();
            if (deletingLogs != null && deletingLogs.size() > 0){
                getLogger().info("查询到需要查询结果到删除任务，共：{}条",deletingLogs.size());
                for (InfoClusterVmClearLog item: deletingLogs){
                    ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(item.getClusterId());
                    if (confCluster.getState().equals(ConfCluster.DELETED)){
                        item.setStatus(InfoClusterVmClearLog.VM_Clear_Status_Deleted);
                        item.setModifiedTime(new Date());
                        infoClusterVmClearLogMapper.updateByPrimaryKeySelective(item);
                        continue;
                    }
                    ResultMsg msg = iAzureService.getJobsStatusWithRequestTimeout(item.getJobId(),confCluster.getRegion());
                    if (msg.getData() != null) {
                        JSONObject response = (JSONObject) msg.getData();
                        if (response.containsKey("status")) {
                            if (response.getString("status").equalsIgnoreCase("Completed")) {
                               item.setStatus(InfoClusterVmClearLog.VM_Clear_Status_Deleted);
                               item.setModifiedTime(new Date());
                               infoClusterVmClearLogMapper.updateByPrimaryKeySelective(item);
                               infoClusterVmMapper.updateVMStateByClusterIdAndVMName(item.getClusterId(),
                                       item.getVmName(), InfoClusterVm.VM_DELETED);
                            }

                            if (response.getString("status").equalsIgnoreCase("Failed")) {
                                item.setStatus(InfoClusterVmClearLog.VM_Clear_Status_Failed);
                                item.setModifiedTime(new Date());
                                infoClusterVmClearLogMapper.updateByPrimaryKeySelective(item);
                            }
                        }
                    }
                    ThreadUtil.sleep(1000);
                }
            }
        }catch (Exception e){
            getLogger().error("VMDeleteDaemonQueryTask异常",e);
        }finally {
            try {
                if (lock) {
                    redisLock.unlock(rediskey);
                }
            } catch (Exception e) {
                getLogger().error("VMDeleteDaemonQueryTaskKey, 释放锁异常", e);
            }
        }
    }

}
