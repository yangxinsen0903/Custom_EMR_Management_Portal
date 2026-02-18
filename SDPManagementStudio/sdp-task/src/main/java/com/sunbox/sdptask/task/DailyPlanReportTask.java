package com.sunbox.sdptask.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.dao.mapper.*;
import com.sunbox.domain.*;
import com.sunbox.sdpservice.service.ComposeService;
import com.sunbox.sdptask.mapper.ConfClusterMapper;
import com.sunbox.sdptask.mapper.InfoClusterOperationPlanMapper;
import com.sunbox.sdptask.utils.GetJobQueryParamDictOutput;
import com.sunbox.sdptask.utils.OperationPlanUtils;
import com.sunbox.service.IAzureService;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 巡检报告每日统计任务
 */
@Component
public class DailyPlanReportTask implements BaseCommonInterFace {
    private static final long HOUR_TICKS = 1000 * 60 * 60;

    @Autowired
    private InfoVmStatementMapper infoVmStatementMapper;

    @Autowired
    private InfoVmStatementItemMapper infoVmStatementItemMapper;

    @Autowired
    private InfoVmStatementResultMapper infoVmStatementResultMapper;

    @Autowired
    private InfoClusterVmNeoMapper infoClusterVmNeoMapper;

    @Autowired
    private ConfClusterMapper confClusterMapper;

    @Autowired
    private IAzureService azureService;

    @Autowired
    private DailyPlanReportMapper dailyPlanReportMapper;

    @Autowired
    private DailyScaleFailReportMapper dailyScaleFailReportMapper;

    @Autowired
    private ComposeService composeService;

    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private InfoClusterOperationPlanMapper infoClusterOperationPlanMapper;

    @Autowired
    private IMetaDataItemService metaDataItemService;

    /**
     * 巡检报告统计任务。<p/>
     * 1. 按不同数据中心统计巡检报告
     * 2. 统计各任务的执行数据：总次数，成功次数，失败次数，超时失败，成功率
     * 3. 统计缩容失败数据：任务数量，VM数量，CPU核数
     */
//    @Scheduled(cron = "${daily.plan.report.task.time:* 0/10 * * * ?}") //test
    @Scheduled(cron = "${daily.plan.report.task.time:0 0 0,9 * * ?}")
    public void start() {
        getLogger().info("巡检报告定时任务启动，DailyPlanReportTask start");
        String lockKey = "DailyPlanReportTask";
        boolean lockResult = this.redisLock.tryLock(lockKey);
        if (!lockResult) {
            getLogger().error("获取巡检报告锁失败。DailyPlanReportTask lock error key:{}", lockKey);
            return;
        }

        try {
            // 获取数据中心
            List<String> regions = metaDataItemService.listRegion();

            // 遍历统计各个数据中心数据
            for (String region : regions) {
                genericPlanResultReport(addDay(new Date(), -1), region);
            }
        } catch (Exception e) {
            getLogger().error("genericPlanResultReport error", e);
        }finally {
            this.redisLock.tryUnlock(lockKey);
        }
    }

    public void genericPlanResultReport(Date date, String region) {
        String reportId = DateUtil.format(date, "yyyy-MM-dd");
        getLogger().info("genericPlanResultReport reportId:{}, region:{}", reportId, region);
        Date beginTime = addHour(getDayBeginTime(date), 8);
        Date endTime = addHour(beginTime, 24);

        if (dailyPlanReportMapper.countByReportId(reportId, region) > 0) {
            return;
        }

        getPlanResult(reportId, region, date, beginTime, endTime, "创建集群");
        getPlanResult(reportId, region, date, beginTime, endTime, "竞价买入");
        getPlanResult(reportId, region, date, beginTime, endTime, "弹性扩容");
        getPlanResult(reportId, region, date, beginTime, endTime, "手动扩容");
        getPlanResult(reportId, region, date, beginTime, endTime, "弹性缩容");
        getPlanResult(reportId, region, date, beginTime, endTime, "手动缩容");
        getPlanResult(reportId, region, date, beginTime, endTime, "竞价逐出");
        getPlanResult(reportId, region, date, beginTime, endTime, "清理异常VM");
        getPlanResult(reportId, region, date, beginTime, endTime, "销毁集群");
    }

    private void initScaleInFailure(List<Map<String, Object>> records, String taskName) {
        Map<String, Object> recordOnDemand = new HashMap<>();
        recordOnDemand.put("taskName", taskName);
        recordOnDemand.put("vmPriceType", "按需");
        recordOnDemand.put("taskCount", 0);
        recordOnDemand.put("vmCount", 0);
        recordOnDemand.put("cpuCount", 0);
        records.add(recordOnDemand);

        Map<String, Object> recordSPOT = new HashMap<>();
        recordSPOT.put("taskName", taskName);
        recordSPOT.put("vmPriceType", "竞价");
        recordSPOT.put("taskCount", 0);
        recordSPOT.put("vmCount", 0);
        recordSPOT.put("cpuCount", 0);
        records.add(recordSPOT);
    }

    private void getPlanResult(String reportId, String region, Date reportDate, Date beginTime, Date endTime, String jobText) {
        try {
            getLogger().info("getPlanResult reportId:{}, region:{}, reportDate:{}, beginTime:{}, endTime:{}, jobText:{}",
                    reportId,
                    region,
                    reportDate,
                    beginTime,
                    endTime,
                    jobText);
            String jobName = null;
            GetJobQueryParamDictOutput jobQueryParamDict = OperationPlanUtils.getJobQueryParamDict();
            List<GetJobQueryParamDictOutput.KvItem> jobNames = jobQueryParamDict.getJobNames();
            for (GetJobQueryParamDictOutput.KvItem name : jobNames) {
                if (name.getValue().equals(jobText)) {
                    jobName = name.getKey();
                    break;
                }
            }

            if (StringUtils.isEmpty(jobName)) {
                throw new RuntimeException("UNKNOWN " + jobText);
            }

            JSONObject paramJson = new JSONObject();
            paramJson.put("begTime", getDateTimeString(beginTime));
            paramJson.put("endTime", getDateTimeString(endTime));
            paramJson.put("pageIndex", 1);
            paramJson.put("pageSize", 2000);
            paramJson.put("jobName", jobName);
            ResultMsg jobListResult = getJobList(paramJson.toJSONString(), region);
            if (!jobListResult.getResult()) {
                throw new RuntimeException(jobListResult.getErrorMsg());
            }
            getLogger().info("jobText:{},result:{}", jobName, jobListResult.getData());

            int totalCount = 0;
            int successCount = 0;
            int failureCount = 0;
            int timeoutCount = 0;

            if (jobListResult.getData() != null) {
                List<Map> resultMapList = (List<Map>) jobListResult.getData();
                for (Map resultMap : resultMapList) {
                    String state = resultMap.get("state").toString();
                    if (state.startsWith("执行失败")) {
                        failureCount++;
                        totalCount++;
                    } else if (state.startsWith("执行超时")) {
                        timeoutCount++;
                        totalCount++;
                    } else if (state.startsWith("执行完成")) {
                        successCount++;
                        totalCount++;
                    }
                }
            }

            DailyPlanReport dailyPlanReport = new DailyPlanReport();
            dailyPlanReport.setId(UUID.randomUUID().toString().replace("-", ""));
            dailyPlanReport.setOperationName(jobText);
            dailyPlanReport.setTotalCount(totalCount);
            dailyPlanReport.setReportId(reportId);
            dailyPlanReport.setRegion(region);
            dailyPlanReport.setSuccessCount(successCount);
            dailyPlanReport.setFailureCount(failureCount);
            dailyPlanReport.setTimeoutCount(timeoutCount);
            if (totalCount == 0) {
                dailyPlanReport.setSuccessRate(BigDecimal.ZERO.doubleValue());
            } else {
                dailyPlanReport.setSuccessRate(new BigDecimal(successCount)
                        .divide(new BigDecimal(totalCount), 2, RoundingMode.UP)
                        .doubleValue());
            }
            dailyPlanReport.setReportDate(reportDate);
            dailyPlanReport.setBeginTime(beginTime);
            dailyPlanReport.setEndTime(endTime);

            dailyPlanReportMapper.insert(dailyPlanReport);
        } catch (Exception e) {
            getLogger().error("getPlanResult error,reportId:{},jobText:{}", reportId, jobText, e);
        }
    }

    private static List<String> getJobNameCodes(JSONObject jsonObject) {
        List<String> jobNameCodes = new ArrayList<>();
        Object jobNameObj = jsonObject.get("jobName");
        if (Objects.nonNull(jobNameObj)) {
            if (jobNameObj instanceof String && StrUtil.isNotEmpty(jobNameObj.toString())) {
                jobNameCodes.add(jobNameObj.toString());
            } else if (jobNameObj instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) jobNameObj;
                for (int i = 0; i < jsonArray.size(); i++) {
                    jobNameCodes.add(jsonArray.getString(i));
                }
            }
        }

        return jobNameCodes;
    }

    private ResultMsg returnResultMsg(Boolean result, Object data, String msg, Integer total, String errorMsg) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(result);
        resultMsg.setData(data);
        resultMsg.setMsg(msg);
        resultMsg.setErrorMsg(errorMsg);
        resultMsg.setTotal(null == total ? 0 : total);
        return resultMsg;
    }

    private List<String> getJobNameParmas(InfoClusterOperationPlan infoClusterOperationPlan, List<String> jobNameCodes) {
        List<String> jobNames = new ArrayList<>();
        if (CollectionUtil.isEmpty(jobNameCodes)) {
            return jobNames;
        }
        jobNameCodes.forEach(jobNameCode -> {
            jobNames.add(OperationPlanUtils.getPlanName(jobNameCode));
        });

        return jobNames;
    }

    public ResultMsg getJobList(String jsonStr, String region) {
        getLogger().debug("getJobList jsonStr:{}", jsonStr);
        if (StringUtils.isEmpty(jsonStr)) {
            return this.returnResultMsg(false, null, null, null, "request content is empty");
        }
        List<Map> resultMap = new ArrayList<>();
        try {
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if (null != jsonObject && jsonObject.size() > 0) {
                List<String> jobNameCodes = getJobNameCodes(jsonObject);
//                String jobName = jsonObject.containsKey("jobName") ? jsonObject.getString("jobName") : null;
                Integer state = null;
                Object jobState = jsonObject.get("jobState");
                if (jobState != null) {
                    if (!StringUtils.isEmpty(jobState.toString())) {
                        state = Integer.parseInt(jobState.toString());
                    }
                }

                String clusterId = jsonObject.containsKey("clusterId") ? jsonObject.getString("clusterId") : "";
                String clusterName = jsonObject.containsKey("clusterName") ? jsonObject.getString("clusterName") : "";
                String begTime = jsonObject.containsKey("begTime") ? jsonObject.getString("begTime") : "";
                String endTime = jsonObject.containsKey("endTime") ? jsonObject.getString("endTime") : "";
                Integer pageIndex = jsonObject.containsKey("pageIndex") ? jsonObject.getInteger("pageIndex") : 1;
                Integer pageSize = jsonObject.containsKey("pageSize") ? jsonObject.getInteger("pageSize") : 10;
                Integer startPercent = jsonObject.containsKey("startPercent") ? jsonObject.getInteger("startPercent") : null;
                Integer endPercent = jsonObject.containsKey("endPercent") ? jsonObject.getInteger("endPercent") : null;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                InfoClusterOperationPlan infoClusterOperationPlan = new InfoClusterOperationPlan();
//                infoClusterOperationPlan.setPlanName(OperationPlanUtils.getPlanName(jobName));
                infoClusterOperationPlan.setClusterId(clusterId);
                infoClusterOperationPlan.setPageIndex((pageIndex - 1) * (pageSize));
                infoClusterOperationPlan.setPageSize(pageSize);
                infoClusterOperationPlan.setClusterName(clusterName);
                infoClusterOperationPlan.setStartPercent(startPercent);
                infoClusterOperationPlan.setEndPercent(endPercent);
                if (StringUtils.isNotEmpty(begTime) && StringUtils.isNotEmpty(endTime)) {
                    infoClusterOperationPlan.setBegTime(sdf.parse(begTime + " 00:00:01"));
                    infoClusterOperationPlan.setEndTime(sdf.parse(endTime + " 23:59:59"));
                }
                infoClusterOperationPlan.setState(state);

                List<String> jobNames = getJobNameParmas(infoClusterOperationPlan, jobNameCodes);

//                List<InfoClusterOperationPlan> infoClusterOperationPlans = infoClusterOperationPlanMapper.selectByObject(infoClusterOperationPlan);
//                Integer total = infoClusterOperationPlanMapper.countByObject(infoClusterOperationPlan);
                getLogger().debug("selectJobList infoClusterOperationPlan:{}, jobNames:{}", infoClusterOperationPlan, jobNames);
                List<InfoClusterOperationPlan> infoClusterOperationPlans = infoClusterOperationPlanMapper
                        .selectJobList(infoClusterOperationPlan, jobNames, region);
                Integer total = infoClusterOperationPlanMapper.countJobList(infoClusterOperationPlan, jobNames, region);
                if (null != infoClusterOperationPlans && infoClusterOperationPlans.size() > 0) {
                    for (InfoClusterOperationPlan iCop : infoClusterOperationPlans) {
                        String cName = confClusterMapper.selectClusterNameByPrimaryKey(iCop.getClusterId());
                        Map map = new HashMap();
                        map.put("jobName", iCop.getPlanName());
                        map.put("begTime", null == iCop.getBegTime() ? "-" : sdf.format(iCop.getBegTime()));
                        map.put("endTime", null == iCop.getEndTime() ? "-" : sdf.format(iCop.getEndTime()));
                        map.put("clusterName", cName);
                        map.put("clusterId", iCop.getClusterId());
                        map.put("planId", iCop.getPlanId());
                        map.put("state", OperationPlanUtils.getStateText(iCop.getState(), iCop.getPercent()));
                        map.put("scalingTaskId", iCop.getScalingTaskId());
                        map.put("opTaskId", iCop.getOpTaskId());
                        map.put("operationType", iCop.getOperationType());
                        resultMap.add(map);
                    }
                    return this.returnResultMsg(true, resultMap, "success", total, null);
                } else{
                    getLogger().warn("infoClusterOperationPlans = null or infoClusterOperationPlans.size == 0");
                }
            } else {
                getLogger().warn("jsonObject from jsonString size == 0");
            }
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            return this.returnResultMsg(false, null, null, null, "error:" + e.getMessage());
        }
        return this.returnResultMsg(true, null, "success", null, null);
    }

    public static Date getDayBeginTime(Date time) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date addDay(Date time, int day) {
        long ticks = time.getTime() + day * 24 * HOUR_TICKS;
        return new Date(ticks);
    }

    public static Date addHour(Date time, int hour) {
        long ticks = time.getTime() + hour * HOUR_TICKS;
        return new Date(ticks);
    }

    public static String getDateTimeString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }
}