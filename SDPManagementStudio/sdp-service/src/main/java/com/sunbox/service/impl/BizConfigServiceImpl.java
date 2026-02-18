/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import com.sunbox.dao.mapper.BizConfigMapper;
import com.sunbox.dao.mapper.ConfClusterNeoMapper;
import com.sunbox.dao.mapper.WorkOrderApprovalRequestMapper;
import com.sunbox.domain.BizConfig;
import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.OrderApprovalRequest;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.cluster.WorkOrderApprovalRequest;
import com.sunbox.domain.cluster.WorkOrderApprovalResponse;
import com.sunbox.service.BizConfigGroup;
import com.sunbox.service.BizConfigService;
import com.sunbox.util.DistributedRedisLock;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.sunbox.constant.BizConfigConstants.*;

/**
 * @author wangda
 * @date 2024/7/12
 */
@Service
public class BizConfigServiceImpl implements BizConfigService {

    private static final Logger log = LoggerFactory.getLogger(BizConfigServiceImpl.class);
    @Autowired
    private BizConfigMapper bizConfigMapper;

    @Resource
    private DistributedRedisLock redisLock;

    @Autowired
    private WorkOrderApprovalRequestMapper workOrderMapper;

    @Autowired
    private ConfClusterNeoMapper confCluster;

    /**
     * 初始化销毁集群的缓存配置
     */
    @PostConstruct
    public void initDestroyCache() {
        updateDestroyCache();
    }

    public void updateDestroyCache(){
        Map<String, String> configMap = getConfigMap();
        redisLock.updateLimitConfig(DESTORYTASKKEY, Long.parseLong(configMap.get(DESTORYINTERVALSECOND)), Long.parseLong(configMap.get(DESTORYLIMITCOUNT)));
    }

    public void updateDestroyCache(BizConfig bizConfig){
        String time = null, count=null;
        Map<String, String> configMap = getConfigMapDB();
        if (LIMITTIME.equalsIgnoreCase(bizConfig.getCfgKey())) {
            time = bizConfig.getCfgValue();
            count = configMap.get(LIMITCOUNT);
        }
        if (LIMITCOUNT.equalsIgnoreCase(bizConfig.getCfgKey())) {
            time = configMap.get(LIMITTIME);
            count = bizConfig.getCfgValue();
        }
        if(StringUtils.isNotEmpty(time) && StringUtils.isNotEmpty(count)){
            redisLock.updateLimitConfig(DESTORYTASKKEY, Long.parseLong(time), Long.parseLong(count));
        }else {
            throw new RuntimeException("保存配置失败");
        }
    }

    @Override
    public void insert(BizConfig bizConfig) {
        Assert.notNull(bizConfig, "需要保存的BizConfig不能为空");
        List<BizConfig> bizConfigs = bizConfigMapper.selectByCategoryAndKey(bizConfig.getCategory(), bizConfig.getCfgKey());
        if (CollectionUtil.isNotEmpty(bizConfigs)) {
            throw new RuntimeException("保存配置失败,配置已经存在: category=" + bizConfig.getCategory()
                    + " cfgKey=" + bizConfig.getCfgKey());
        }
        int affect = bizConfigMapper.insert(bizConfig);
        if (affect == 0) {
            throw new RuntimeException("保存配置失败");
        }
    }

    @Override
    public List<BizConfigGroup> getGroupedConfigs() {
        List<BizConfigGroup> result = new ArrayList<>();

        List<BizConfig> bizConfigs = bizConfigMapper.selectAll();
        Map<String, List<BizConfig>> bizConfigMap = bizConfigs.stream().collect(Collectors.groupingBy(BizConfig::getCategory));
        for (Map.Entry<String, List<BizConfig>> entry : bizConfigMap.entrySet()) {
            BizConfigGroup group = new BizConfigGroup();
            group.setCategory(entry.getKey());
            group.setConfigs(entry.getValue());
            result.add(group);
        }
        return result;
    }

    @Override
    public List<BizConfig> getAllConfigs() {
        return bizConfigMapper.selectAll();
    }

    @Override
    public void updateBizConfig(BizConfig bizConfig) {
        Assert.notNull(bizConfig, "被更新的配置不能为空");
        Assert.notNull(bizConfig.getId(), "被更新的配置Id不能为空");
        if (LIMITTIME.equalsIgnoreCase(bizConfig.getCfgKey()) || LIMITCOUNT.equalsIgnoreCase(bizConfig.getCfgKey())) {
            updateDestroyCache(bizConfig);
        }
        bizConfigMapper.updateConfByCfgkey(bizConfig);
    }

    @Override
    public void delete(Long id) {
        Assert.notNull(id, "待删除的配置Id不能为空");
        bizConfigMapper.deleteById(id);
    }

    @Override
    public <T> T getConfigValue(String category, String key, Class<T> clz) {
        List<BizConfig> bizConfigs = bizConfigMapper.selectByCategoryAndKey(category, key);
        if (Objects.isNull(bizConfigs)) {
            return null;
        } else {
            BizConfig cfg = bizConfigs.get(0);
            if (clz == Integer.class) {
                return (T)cfg.getValueAsInt();
            } else if (clz == Boolean.class) {
                return (T)cfg.getValueAsBoolean();
            } else if (clz == Long.class) {
                return (T)cfg.getValueAsLong();
            } else if (clz == String.class) {
                return (T)cfg.getValueAsStr();
            } else {
                return (T)cfg.getCfgValue();
            }
        }
    }


    @Override
    public ResultMsg getDestoryClusterLimitConfig() {
        Map<String, String> hashMap = getConfigMap();
        ResultMsg resultMsg = ResultMsg.SUCCESS(hashMap);
        return resultMsg;
    }

    public Map<String, String> getConfigMap() {
        List<BizConfig> bizConfigTime = bizConfigMapper.selectByCategoryAndKey(null, LIMITTIME);
        List<BizConfig> bizConfigCount = bizConfigMapper.selectByCategoryAndKey(null, LIMITCOUNT);

        log.info("从数据库加载配置{},结果为:{}", LIMITTIME, JSONUtil.toJsonStr(bizConfigTime));
        log.info("从数据库加载配置{},结果为:{}", LIMITCOUNT, JSONUtil.toJsonStr(bizConfigCount));

        Map<String, String> hashMap = new HashMap<>();
        hashMap.put(DESTORYINTERVALSECOND, bizConfigTime.get(0).getCfgValue());
        hashMap.put(DESTORYLIMITCOUNT, bizConfigCount.get(0).getCfgValue());
        return hashMap;
    }

    public Map<String, String> getConfigMapDB() {
        List<BizConfig> bizConfigTime = bizConfigMapper.selectByCategoryAndKey(null, LIMITTIME);
        List<BizConfig> bizConfigCount = bizConfigMapper.selectByCategoryAndKey(null, LIMITCOUNT);
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put(bizConfigTime.get(0).getCfgKey(), bizConfigTime.get(0).getCfgValue());
        hashMap.put(bizConfigCount.get(0).getCfgKey(), bizConfigCount.get(0).getCfgValue());
        return hashMap;
    }

    @Override
    public ResultMsg updateDestoryClusterLimitConfig(Map<String, String> param) {
        String destoryIntervalSecond = param.get(DESTORYINTERVALSECOND);
        String destoryLimitCount = param.get(DESTORYLIMITCOUNT);
        BizConfig bizConfig = new BizConfig();
        bizConfig.setCfgKey(LIMITTIME);
        bizConfig.setCfgValue(destoryIntervalSecond);
        bizConfigMapper.updateConfByCfgkey(bizConfig);

        BizConfig bizConfig2 = new BizConfig();
        bizConfig2.setCfgKey(LIMITCOUNT);
        bizConfig2.setCfgValue(destoryLimitCount);
        bizConfigMapper.updateConfByCfgkey(bizConfig2);
        //更新缓存
        redisLock.updateLimitConfig(DESTORYTASKKEY, Long.parseLong(destoryIntervalSecond), Long.parseLong(destoryLimitCount));
        return ResultMsg.SUCCESS();
    }

    @Override
    public List<BizConfig> getConfigValueByKey(List<String> keyList) {
        List<BizConfig> bizConfigs = bizConfigMapper.selectByCfgKey(keyList);
        return bizConfigs;
    }

    @Override
    public Map<String,String> getConfigValueMapByKey(List<String> keyList) {
        Map<String, String> map = new HashMap<>();
        List<BizConfig> bizConfigs = getConfigValueByKey(keyList);
        if(CollectionUtil.isEmpty(bizConfigs)){
            return map;
        }
        for (BizConfig bizConfig : bizConfigs) {
            for (String key : keyList) {
                if(key.equalsIgnoreCase(bizConfig.getCfgKey())){
                    map.put(key, bizConfig.getValueAsStr());
                }
            }
        }
        return map;
    }

    @Override
    public ResultMsg queryOrderApproval(OrderApprovalRequest request) {
        ResultMsg resultMsg = new ResultMsg();
        request.page();
        String clusterName = request.getClusterName();
        ArrayList<WorkOrderApprovalResponse> workOrderApprovalResponses = new ArrayList<>();
        if (StringUtils.isEmpty(clusterName)) {
            int total = workOrderMapper.selectTotal(request);
            if (total < 1) {
                resultMsg.setTotal(total);
                resultMsg.setResult(true);
                resultMsg.setData(workOrderApprovalResponses);
                return resultMsg;
            }
            List<WorkOrderApprovalRequest> workOrderApprovalList = workOrderMapper.selectByPage(request);
            //空
            Set<String> clusterIds = workOrderApprovalList.stream().map(vo -> vo.getClusterId()).collect(Collectors.toSet());
            //查询集群名称
            List<String> list = new ArrayList<>(clusterIds);
            List<ConfCluster> confClusters = confCluster.selectByClusterIds(list);
            for (WorkOrderApprovalRequest workOrderApprovalRequest : workOrderApprovalList) {
                WorkOrderApprovalResponse workOrderApprovalResponse = new WorkOrderApprovalResponse();
                BeanUtils.copyProperties(workOrderApprovalRequest, workOrderApprovalResponse);
                List<ConfCluster> collect = confClusters.stream().filter(vo -> vo.getClusterId().equalsIgnoreCase(workOrderApprovalRequest.getClusterId())).collect(Collectors.toList());
                if (!CollectionUtil.isEmpty(collect)) {
                    String clusterName1 = collect.get(0).getClusterName();
                    workOrderApprovalResponse.setClusterName(clusterName1);
                }
                workOrderApprovalResponses.add(workOrderApprovalResponse);
            }
            resultMsg.setTotal(total);
            resultMsg.setResult(true);
            resultMsg.setData(workOrderApprovalResponses);
            return resultMsg;
        } else {
            //有集群名称
            List<ConfCluster> confClusters = confCluster.selectByName(clusterName);
            if (CollectionUtil.isEmpty(confClusters)) {
                resultMsg.setTotal(0);
                resultMsg.setResult(true);
                resultMsg.setData(workOrderApprovalResponses);
                return resultMsg;
            }
            Set<String> clusterIdSet = confClusters.stream().map(vo -> vo.getClusterId()).collect(Collectors.toSet());
            int total = workOrderMapper.selectTotalByList(request, clusterIdSet);
            List<WorkOrderApprovalRequest> workOrderApprovalRequests = workOrderMapper.selectByPageList(request, clusterIdSet);
            for (WorkOrderApprovalRequest workOrderApprovalRequest : workOrderApprovalRequests) {
                WorkOrderApprovalResponse workOrderApprovalResponse = new WorkOrderApprovalResponse();
                BeanUtils.copyProperties(workOrderApprovalRequest, workOrderApprovalResponse);
                List<ConfCluster> collect = confClusters.stream().filter(vo -> vo.getClusterId().equalsIgnoreCase(workOrderApprovalRequest.getClusterId())).collect(Collectors.toList());
                if (!CollectionUtil.isEmpty(collect)) {
                    String clusterName1 = collect.get(0).getClusterName();
                    workOrderApprovalResponse.setClusterName(clusterName1);
                }
                workOrderApprovalResponses.add(workOrderApprovalResponse);
            }
            resultMsg.setTotal(total);
            resultMsg.setResult(true);
            resultMsg.setData(workOrderApprovalResponses);
            return resultMsg;
        }
    }

}
