package com.sunbox.sdpadmin.service.impl;

import com.alibaba.fastjson.JSON;
import com.sunbox.dao.mapper.ClusterDestroyTaskMapper;
import com.sunbox.domain.*;
import com.sunbox.sdpadmin.mapper.ConfClusterHostGroupMapper;
import com.sunbox.sdpadmin.mapper.ConfClusterMapper;
import com.sunbox.sdpadmin.mapper.InfoClusterOperationPlanActivityLogMapper;
import com.sunbox.sdpadmin.mapper.InfoClusterOperationPlanMapper;
import com.sunbox.sdpadmin.service.AdminApiService;
import com.sunbox.sdpadmin.service.IDestroyCluTaskService;
import com.sunbox.sdpservice.service.ComposeService;
import com.sunbox.service.consts.DestroyStatusConstant;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

@Service
public class DestroyCluTaskServiceImpl implements IDestroyCluTaskService , BaseCommonInterFace {

    @Resource
    private ClusterDestroyTaskMapper clusterDestroyTaskMapper;

    @Resource
    private InfoClusterOperationPlanMapper planMapper;

    @Resource
    private InfoClusterOperationPlanActivityLogMapper planActivityLogMapper;

    @Autowired
    private ComposeService composeService;

    @Resource
    private ConfClusterMapper confClusterMapper;

    @Resource
    private AdminApiService adminApiService;

    @Autowired
    private ConfClusterHostGroupMapper confClusterHostGroupMapper;

    @Override
    public ResultMsg queryDestroyTask(DestroyTaskRequest request) {
        request.page();
        List<ClusterDestroyTask> clusterDestroyTasks = clusterDestroyTaskMapper.selectByNameAndStatus(
                request.getClusterName(),
                request.getDestroyStatus(),
                request.getPageStart(),
                request.getPageLimit());
        Integer count = clusterDestroyTaskMapper.countByNameAndStatus(
                request.getClusterName(),
                request.getDestroyStatus());

        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);
        resultMsg.setData(clusterDestroyTasks);
        resultMsg.setTotal(count);
        return resultMsg;
    }

    /**
     * 重试销毁失败的任务
     *
     * @param clusterId
     * @return
     */
    @Override
    public ResultMsg retryActivity(String clusterId) {
        ResultMsg resultMsg = new ResultMsg();
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
        if(confCluster== null){
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("该集群不存在"+clusterId);
            return resultMsg;
        }
        if (ConfCluster.DELETED == confCluster.getState()) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("集群已经销毁成功,不能重试");
            return resultMsg;
        }
        // 查询 plan,和log表, 检查log表, 以及 logid, 调用重试接口,
        //如果没有plan, 重新生成plan,
        List<InfoClusterOperationPlan> planList = planMapper.selectType(Collections.singletonList(clusterId), "delete");
        if (CollectionUtils.isEmpty(planList) && ConfCluster.DELETING == confCluster.getState()) {
            // 重新生成plan
            Map<String, String> map = new HashMap<>();
            map.put("clusterId", clusterId);
            map.put("fromTask", "1");
            ResultMsg deletRes = adminApiService.deleteCluster(JSON.toJSONString(map), "system");
            if (deletRes.getResult()) {
                resultMsg.setResult(true);
                resultMsg.setMsg("重试成功");
                return resultMsg;
            } else {
                getLogger().error("ClusterDaemonTask,retryActivity,retryActivity{},deletRes:{}", clusterId, deletRes);
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("重试失败");
                return resultMsg;
            }
        }
        InfoClusterOperationPlan infoClusterOperationPlan = planList.get(0);
        List<Integer> stateList = new ArrayList();
        stateList.add(ConfCluster.DELETING);
        stateList.add(ConfCluster.CREATED);
        // 根据planid, state , 排序 sort_no,  查第一个
        List<InfoClusterOperationPlanActivityLog> activityLogs = planActivityLogMapper.getActByIdName
                (infoClusterOperationPlan.getPlanId(), "deleteVms", stateList);
        if (CollectionUtils.isEmpty(activityLogs)) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("没有找到该集群失败的销毁流程,请稍后重试");
            return resultMsg;
        }
        activityLogs.sort(Comparator.comparing(activity -> activity.getSortNo()));
        InfoClusterOperationPlanActivityLog logInfo = activityLogs.get(0);
        String activityLogId = logInfo.getActivityLogId();

        ResultMsg retryactivity = composeService.retryactivity(activityLogId);
        getLogger().info("retryactivity ,clusterId:{},result:{}", clusterId, retryactivity);
        return retryactivity;
    }

    @Override
    public ResultMsg cancelTask(String clusterId) {
        ResultMsg resultMsg = new ResultMsg();
        // 校验状态 把待销毁的任务取消
        //ClusterDestroyTask clusterDestroyTask = clusterDestroyTaskMapper.selectByPrimaryKey(clusterId);
        List<ClusterDestroyTask> clusterDestroyTaskList = clusterDestroyTaskMapper.selectByClusterId(clusterId);
        if (CollectionUtils.isEmpty(clusterDestroyTaskList)) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("没有此任务,无法取消");
            return resultMsg;
        }
        List<Long> idList=new ArrayList<>();
        for (ClusterDestroyTask clusterDestroyTask : clusterDestroyTaskList) {
            if (DestroyStatusConstant.DESTROY_STATUS_WAITING.equalsIgnoreCase(clusterDestroyTask.getDestroyStatus())) {
                idList.add(clusterDestroyTask.getId());
            }
        }
        if (CollectionUtils.isEmpty(idList)) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("没有查到此任务,无法取消");
            return resultMsg;
        }
       //判断
        List<InfoClusterOperationPlan> planList = planMapper.selectType(Collections.singletonList(clusterId), "delete");
        if (!CollectionUtils.isEmpty(planList)) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("已经生成了该集群的销毁计划,无法取消,请稍后重试");
            return resultMsg;
        }
         clusterDestroyTaskMapper.updateBatchTaskByIds(idList, DestroyStatusConstant.DESTROY_STATUS_CANCEL, null, null);

        // 集群列表页， 应更新成已创建
        ConfCluster confCluster = new ConfCluster();
        confCluster.setClusterId(clusterId);
        confCluster.setState(ConfCluster.CREATED);
        confClusterMapper.updateByPrimaryKeySelective(confCluster);
        confClusterHostGroupMapper.updateByClusterId(clusterId, ConfClusterHostGroup.STATE_RUNNING);
        resultMsg.setResult(true);
        resultMsg.setMsg("取消成功");
        return resultMsg;
    }
}
