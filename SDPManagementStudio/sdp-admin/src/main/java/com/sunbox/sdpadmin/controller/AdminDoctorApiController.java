package com.sunbox.sdpadmin.controller;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.netflix.discovery.converters.Auto;
import com.sunbox.dao.mapper.*;
import com.sunbox.domain.*;
import com.sunbox.domain.azure.AzureVmtraceInfoRequest;
import com.sunbox.sdpadmin.mapper.JobResultsMapper;
import com.sunbox.sdpadmin.mapper.PrometheusMetricsMapper;
import com.sunbox.sdpadmin.service.AdminApiService;
import com.sunbox.sdpadmin.service.IDoctorService;
import com.sunbox.sdpadmin.util.OperationPlanUtils;
import com.sunbox.service.IAzureService;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.service.IVMDeleteService;
import com.sunbox.util.DateUtil;
import jodd.util.CollectionUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author : [niyang]
 * @className : AdminDoctorApiController
 * @description : [排除故障查询专用接口]
 * @createTime : [2023/3/28 1:32 PM]
 */
@RestController
@RequestMapping("/admin/api/dc/")
public class AdminDoctorApiController extends BaseAdminController {
    private static final long HOUR_TICKS = 1000 * 60 * 60;

    @Autowired
    private JobResultsMapper jobResultsMapper;

    @Autowired
    private PrometheusMetricsMapper prometheusMetricsMapper;

    @Autowired
    private AdminApiService adminApiService;

    @Autowired
    private InfoVmStatementMapper infoVmStatementMapper;

    @Autowired
    private InfoVmStatementItemMapper infoVmStatementItemMapper;

    @Autowired
    private InfoVmStatementResultMapper infoVmStatementResultMapper;

    @Autowired
    private InfoClusterVmDeleteMapper infoClusterVmDeleteMapper;

    @Autowired
    private InfoClusterVmReqJobFailedMapper infoClusterVmReqJobFailedMapper;

    @Autowired
    private DailyPlanReportMapper dailyPlanReportMapper;

    @Autowired
    private DailyScaleFailReportMapper dailyScaleFailReportMapper;

    @Autowired
    private IDoctorService doctorService;

    @Autowired
    private IVMDeleteService vmDeleteService;

    @Autowired
    private IMetaDataItemService metaDataItemService;

    @Autowired
    private IAzureService azureService;

    /**
     * 根据jobID 查询 ansible jobresult 信息
     *
     * @param jobid
     * @return
     */
    @GetMapping("/getjobresult")
    public ResultMsg getJobResultByJobId(@RequestParam("jobid") String jobid) {
        ResultMsg msg = new ResultMsg();
        JobResults results = jobResultsMapper.selectByJobId(jobid);
        msg.setData(results);
        return msg;
    }

    /**
     * DC02-获取异常vm的清理情况总揽
     *
     * @return
     */
    @GetMapping("/getAbnormalVmCleanSummary")
    public ResultMsg getAbnormalVmCleanSummary(@RequestParam String region) {
        List<Map> summaryList = vmDeleteService.vmCleanSummary(region);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("pending", 0);
        resultMap.put("deleting", 0);
        resultMap.put("requestDeleting", 0);
        resultMap.put("frozen", 0);
        resultMap.put("completed", 0);
        summaryList.stream().forEach(entry -> {
            String status = (String)entry.get("status");
            Long vmCount = Convert.toLong(entry.get("vm_count"));
            if (StrUtil.equalsIgnoreCase("waitDelete", status)) {
                resultMap.put("pending", vmCount);
            } else if (StrUtil.equalsIgnoreCase("deleting", status)) {
                resultMap.put("deleting", vmCount);
            } else if (StrUtil.equalsIgnoreCase("deleteRequest", status)) {
                resultMap.put("requestDeleting", vmCount);
            } else if (StrUtil.equalsIgnoreCase("frozen", status)) {
                resultMap.put("frozen", vmCount);
            } else if (StrUtil.equalsIgnoreCase("deleted", status)) {
                resultMap.put("completed", vmCount);
            }
        });
        return ResultMsg.SUCCESS(resultMap);
    }

    /**
     * DC03-获取异常vm的申请情况概览
     *
     * @return
     */
    @GetMapping("/getAbnormalVmRetrySummary")
    public ResultMsg getAbnormalVmRetrySummary(@RequestParam String region) {
        List<Map> summaryList = infoClusterVmReqJobFailedMapper.vmReqJobFailedSummary(region);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("pending", 0);
        resultMap.put("completed", 0);

        summaryList.stream().forEach(summary -> {
            String status = (String)summary.get("status");
            Long vmCount = Convert.toLong(summary.get("vm_req_count"));
            if (StrUtil.equalsIgnoreCase("init", status)) {
                resultMap.put("pending", vmCount);
            } else if (StrUtil.equalsIgnoreCase("done", status)) {
                resultMap.put("completed", vmCount);
            }
        });

        return ResultMsg.SUCCESS(resultMap);
    }

    /**
     * DC04-任务执行情况统计
     *
     * @return
     */
    @GetMapping("/getPlanResultReport")
    public ResultMsg getPlanResultReport(@RequestParam(name="reportDate", required = false) String reportDate,
                                         @RequestParam(name="region") String region) {
        if(StringUtils.isEmpty(reportDate)) {
            reportDate = DateUtil.formatDate("yyyy-MM-dd", DateUtil.addDays(new Date(), -1));
        }
        List<DailyPlanReport> dailyPlanReports = dailyPlanReportMapper.selectByReportId(region, reportDate);

        Map<String, Object> data = new HashMap<>();
        if (dailyPlanReports.size() > 0) {
            data.put("beginTime", cn.hutool.core.date.DateUtil.formatDateTime(dailyPlanReports.get(0).getBeginTime()));
            data.put("endTime", cn.hutool.core.date.DateUtil.formatDateTime(dailyPlanReports.get(0).getEndTime()));
        } else {
            data.put("beginTime", reportDate);
            data.put("endTime", reportDate);
        }
        data.put("records", dailyPlanReports);
        return ResultMsg.SUCCESS(data);
    }

    /**
     * DC05-缩容失败数量统计
     *
     * @return
     */
    @GetMapping("/getScaleInFailureResultReport")
    public ResultMsg getScaleInFailureResultReport(@RequestParam(name="reportDate", required = false) String reportDate,
                                                   @RequestParam(name="region") String region) {
        if (StringUtils.isEmpty(reportDate)) {
            reportDate = DateUtil.formatDate("yyyy-MM-dd", DateUtil.addDays(new Date(), -1));
        }
        List<DailyScaleFailReport> dailyScaleFailReports = this.dailyScaleFailReportMapper.selectByReportId(region, reportDate);
        Map<String, Object> data = new HashMap<>();
        if (dailyScaleFailReports.size() > 0) {
            data.put("beginTime", cn.hutool.core.date.DateUtil.formatDateTime(dailyScaleFailReports.get(0).getBeginTime()));
            data.put("endTime", cn.hutool.core.date.DateUtil.formatDateTime(dailyScaleFailReports.get(0).getEndTime()));
        } else {
            data.put("beginTime", reportDate);
            data.put("endTime", reportDate);
        }
        data.put("records", dailyScaleFailReports);
        return ResultMsg.SUCCESS(data);
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

    /**
     * 获取日期，时分秒为00:00:00
     *
     * @param time
     * @return 日期
     */
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

    public static Date getDayEndTime(Date time) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(time);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public static Date addHour(Date time, int hour) {
        long ticks = time.getTime() + hour * HOUR_TICKS;
        return new Date(ticks);
    }

    public static String getDateTimeString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    /**
     * DC06-SDP三方VM差异比对数据统计
     *
     * @return
     */
    @GetMapping("/vmDiffStat")
    public ResultMsg vmDiffStat(@RequestParam String region) {
        getLogger().info("vmDiffStat begin");
        try {
            InfoVmStatement infoVmStatement = infoVmStatementMapper.selectLatest();
            Integer azureCount = infoVmStatementItemMapper.countByStatementIdAndVmSource(infoVmStatement.getStatementId(), "AZURE",region);
            Integer sdpCount = infoVmStatementItemMapper.countByStatementIdAndVmSource(infoVmStatement.getStatementId(), "SDP",region);
            Integer yarnCount = infoVmStatementItemMapper.countByStatementIdAndVmSource(infoVmStatement.getStatementId(), "YARN",region);
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("sdpVmCount", sdpCount);
            dataMap.put("yarnVmCount", yarnCount);
            dataMap.put("azureVmCount", azureCount);
            dataMap.put("sdpYarnDiff", sdpCount - yarnCount);
            dataMap.put("azureYarnDiff", azureCount - yarnCount);
            dataMap.put("azureSdpDiff", azureCount - sdpCount);
            return ResultMsg.SUCCESS(dataMap);
        } catch (Exception e) {
            getLogger().error("vmDiffStat error", e);
            return ResultMsg.FAILURE(e.getMessage());
        }
    }

    /**
     * DC07-SDP三方VM差异数据对账单查询
     *
     * @return
     */
    @GetMapping("/vmStatements")
    public ResultMsg vmStatements(@RequestParam(name = "region", required = false) String region,
                                  @RequestParam(name = "startDate", required = false) String startDateString,
                                  @RequestParam(name = "endDate", required = false) String endDateString,
                                  @RequestParam("pageIndex") Integer page,
                                  @RequestParam("pageSize") Integer pageSize) {
        getLogger().info("vmStatements begin");
        try {
            Date beginDate = null;
            if (!StringUtils.isEmpty(startDateString)) {
                beginDate = DateUtil.toDate("yyyy-MM-dd", startDateString);
            } else {
                beginDate = DateUtil.addDays(new Date(), -7);
            }
            Date endDate = null;
            if (!StringUtils.isEmpty(endDateString)) {
                endDate = DateUtil.toDate("yyyy-MM-dd", endDateString);
            } else {
                endDate = new Date();
            }

            if (page == null) {
                page = 1;
            }
            if (pageSize == null) {
                pageSize = 20;
            }

            List<InfoVmStatement> infoVmStatementItems = infoVmStatementMapper.queryByCreatedTime(region, beginDate,
                    endDate,
                    (page - 1) * pageSize,
                    pageSize);
            Integer statementCount = infoVmStatementMapper.countByCreatedTime(region, beginDate, endDate);

            Map<String, String> regionMap = metaDataItemService.getRegionMap();
            ResultMsg resultMsg = new ResultMsg();
            List<Map<String, Object>> rows = new ArrayList<>();
            for (InfoVmStatement infoVmStatementItem : infoVmStatementItems) {
                Map<String, Object> row = new HashMap<>();
                row.put("statementId", infoVmStatementItem.getStatementId());
                row.put("region", infoVmStatementItem.getRegion());
                row.put("regionName", regionMap.get(infoVmStatementItem.getRegion()));
                row.put("status", infoVmStatementItem.getStatus());
                row.put("startTime", infoVmStatementItem.getCreatedTime());
                row.put("finishTime", infoVmStatementItem.getModifiedTime());
                rows.add(row);
            }
            resultMsg.setResult(true);
            resultMsg.setData(rows);
            resultMsg.setTotal(statementCount);
            return resultMsg;
        } catch (Exception e) {
            getLogger().error("vmStatements error", e);
            return ResultMsg.FAILURE(e.getMessage());
        }
    }

    /**
     * DC08-SDP三方VM差异数据对账详情查询
     *
     * @return
     */
    @GetMapping("/vmStatementDetails")
    public ResultMsg getVmStatementResults(@RequestParam(name = "statementId", required = false) String statementId,
                                           @RequestParam(name = "clusterId", required = false) String clusterId,
                                           @RequestParam(name = "clusterName", required = false) String clusterName,
                                           @RequestParam(name = "hostName", required = false) String hostName,
                                           @RequestParam(name = "vmRoles[]", required = false) String[] vmRoles,
                                           @RequestParam(name = "diffType", required = false) String diffType,
                                           @RequestParam(name = "purchaseType", required = false) Integer purchaseType,
                                           @RequestParam(name = "minuteBefore", required = false) Integer minuteBefore,
                                           @RequestParam("pageIndex") Integer page,
                                           @RequestParam("pageSize") Integer pageSize) {
        statementId = StrUtil.emptyToNull(statementId);
        clusterId   = StrUtil.emptyToNull(clusterId);
        clusterName = StrUtil.emptyToNull(clusterName);
        hostName    = StrUtil.emptyToNull(hostName);
        vmRoles     = vmRoles == null ? new String[0] : vmRoles;

        getLogger().info("vmStatementDetails begin,statementId:{},clusterId:{},clusterName:{},hostName:{},diffType:{}," +
                        "pageIndex:{},pageSize:{}, vmRole:{}, purchaseType:{}, minuteBefore:{}",
                statementId,
                clusterId,
                clusterName,
                hostName,
                diffType,
                page,
                pageSize,
                vmRoles, purchaseType, minuteBefore);
        try {
            Assert.notEmpty(statementId, "statementId参数不正确");
            page     = ObjectUtil.defaultIfNull(page, 1);
            pageSize = ObjectUtil.defaultIfNull(pageSize, 20);

            List<InfoVmStatementResult> infoVmStatementResults = infoVmStatementResultMapper.queryByStatementId(statementId,
                    clusterId,
                    clusterName,
                    hostName,
                    vmRoles,
                    diffType,
                    purchaseType,
                    minuteBefore,
                    (page - 1) * pageSize,
                    pageSize);

            Integer total = infoVmStatementResultMapper.countByStatementId(statementId,
                    clusterId,
                    clusterName,
                    hostName,
                    vmRoles,
                    diffType,
                    purchaseType,
                    minuteBefore);

            ResultMsg resultMsg = new ResultMsg();
            resultMsg.setResult(true);
            resultMsg.setData(infoVmStatementResults);
            resultMsg.setTotal(total);
            return resultMsg;
        } catch (Exception e) {
            getLogger().error("vmStatementDetails error", e);
            return ResultMsg.FAILURE(e.getMessage());
        }
    }

    /**
     * DC09-自动清理的异常VM列表查询
     *
     * @return
     */
    @GetMapping("/clusterVMDelete")
    public ResultMsg clusterVMDelete(@RequestParam(name = "region", required = false) String region,
                                     @RequestParam(name = "planName", required = false) String planName,
                                     @RequestParam(name = "planId", required = false) String planId,
                                     @RequestParam(name = "clusterId", required = false) String clusterId,
                                     @RequestParam(name = "clusterName", required = false) String clusterName,
                                     @RequestParam(name = "hostName", required = false) String hostName,
                                     @RequestParam(name = "status", required = false) Integer status,
                                     @RequestParam(name = "startDate", required = false) String startDateString,
                                     @RequestParam(name = "endDate", required = false) String endDateString,
                                     @RequestParam("pageIndex") Integer pageIndex,
                                     @RequestParam("pageSize") Integer pageSize) {
        getLogger().info("clusterVMDelete begin");
        try {
            if(StringUtils.isEmpty(planName)){
                planName = null;
            }
            if(StringUtils.isEmpty(planId)){
                planId = null;
            }
            if(StringUtils.isEmpty(clusterId)){
                clusterId = null;
            }
            if(StringUtils.isEmpty(clusterName)){
                clusterName = null;
            }
            if(StringUtils.isEmpty(hostName)){
                hostName = null;
            }

            Date beginDate = null;
            if (StringUtils.isNotEmpty(startDateString)) {
                startDateString = startDateString.replace("+", " ");
                beginDate = cn.hutool.core.date.DateUtil.parse(startDateString);
            } else {
                beginDate = DateUtil.addDays(new Date(), -7);
            }

            Date endDate = null;
            if (StringUtils.isNotEmpty(endDateString)) {
                endDateString = endDateString.replace("+", " ");
                endDate = cn.hutool.core.date.DateUtil.parse(endDateString);
            } else {
                endDate = new Date();
            }

            if (pageIndex == null) {
                pageIndex = 1;
            }
            if (pageSize == null) {
                pageSize = 20;
            }

            getLogger().info("clusterVMDelete region:{}, planName:{},planId:{},clusterId:{},clusterName:{},hostName:{},status:{},beginTime:{},endTime:{},pageIndex:{},pageSize:{}",
                    region,
                    planName,
                    planId,
                    clusterId,
                    clusterName,
                    hostName,
                    status,
                    beginDate,
                    endDate,
                    pageIndex,
                    pageSize);
            List<InfoClusterVmDeleteDetail> infoClusterVmDeletes = infoClusterVmDeleteMapper.queryByTime(
                    region,
                    planName,
                    planId,
                    clusterId,
                    clusterName,
                    hostName,
                    status,
                    beginDate,
                    endDate,
                    (pageIndex - 1) * pageSize,
                    pageSize);
            //增加regionName
            Map<String, String> regionMap = metaDataItemService.getRegionMap();
            for (InfoClusterVmDeleteDetail infoClusterVmDelete : infoClusterVmDeletes) {
                infoClusterVmDelete.setRegionName(regionMap.get(infoClusterVmDelete.getRegion()));
            }
            Long total = infoClusterVmDeleteMapper.countByTime(
                    region,
                    planName,
                    planId,
                    clusterId,
                    clusterName,
                    hostName,
                    status,
                    beginDate,
                    endDate);

            ResultMsg resultMsg = new ResultMsg();
            resultMsg.setResult(true);
            resultMsg.setData(infoClusterVmDeletes);
            resultMsg.setTotal(total);
            return resultMsg;
        } catch (Exception e) {
            getLogger().error("clusterVMDelete error", e);
            return ResultMsg.FAILURE(e.getMessage());
        }
    }

    /**
     * DC10-异常Azure资源申请任务查询
     *
     * @return
     */
    @GetMapping("/vmReqJobFailed")
    public ResultMsg vmReqJobFailed(@RequestParam(name = "region", required = false) String region,
                                    @RequestParam(name = "planName", required = false) String planName,
                                    @RequestParam(name = "planId", required = false) String planId,
                                    @RequestParam(name = "clusterId", required = false) String clusterId,
                                    @RequestParam(name = "clusterName", required = false) String clusterName,
                                    @RequestParam(name = "jobId", required = false) String jobId,
                                    @RequestParam(name = "status", required = false) Integer status,
                                    @RequestParam(name = "beginTime", required = false) String startDateString,
                                    @RequestParam(name = "endTime", required = false) String endDateString,
                                    @RequestParam("pageIndex") Integer pageIndex,
                                    @RequestParam("pageSize") Integer pageSize) {
        getLogger().info("vmReqJobFailed begin");
        try {
            if(StringUtils.isEmpty(planName)){
                planName = null;
            }
            if(StringUtils.isEmpty(planId)){
                planId = null;
            }
            if(StringUtils.isEmpty(clusterId)){
                clusterId = null;
            }
            if(StringUtils.isEmpty(clusterName)){
                clusterName = null;
            }
            if(StringUtils.isEmpty(jobId)){
                jobId = null;
            }

            Date beginDate = null;
            if (!StringUtils.isEmpty(startDateString)) {
                beginDate = DateUtil.toDate("yyyy-MM-dd", startDateString);
            } else {
                beginDate = DateUtil.addDays(new Date(), -7);
            }
            Date endDate = null;
            if (!StringUtils.isEmpty(endDateString)) {
                endDate = DateUtil.toDate("yyyy-MM-dd", endDateString);
            } else {
                endDate = new Date();
            }

            if (pageIndex == null) {
                pageIndex = 1;
            }
            if (pageSize == null) {
                pageSize = 20;
            }

            getLogger().info("vmReqJobFailed region:{}, planName:{},planId:{},clusterId:{},clusterName:{},jobId:{},status:{},beginTime:{},endTime:{},pageIndex:{},pageSize:{}",
                    region,
                    planName,
                    planId,
                    clusterId,
                    clusterName,
                    jobId,
                    status,
                    beginDate,
                    endDate,
                    pageIndex,
                    pageSize);
            List<InfoClusterVmReqJobFailedDetail> infoClusterVmReqJobFaileds = infoClusterVmReqJobFailedMapper.queryByTime(
                    region,
                    planName,
                    planId,
                    clusterId,
                    clusterName,
                    jobId,
                    status,
                    beginDate,
                    endDate,
                    (pageIndex - 1) * pageSize,
                    pageSize);
            //增加regionName
            Map<String, String> regionMap = metaDataItemService.getRegionMap();
            for (InfoClusterVmReqJobFailedDetail infoClusterVmReqJobFailed : infoClusterVmReqJobFaileds) {
                infoClusterVmReqJobFailed.setRegionName(regionMap.get(infoClusterVmReqJobFailed.getRegion()));
            }
            Long total = infoClusterVmReqJobFailedMapper.countByTime(
                    region,
                    planName,
                    planId,
                    clusterId,
                    clusterName,
                    jobId,
                    status,
                    beginDate,
                    endDate);

            ResultMsg resultMsg = new ResultMsg();
            resultMsg.setResult(true);
            resultMsg.setData(infoClusterVmReqJobFaileds);
            resultMsg.setTotal(total);
            return resultMsg;
        } catch (Exception e) {
            getLogger().error("vmReqJobFailed error", e);
            return ResultMsg.FAILURE(e.getMessage());
        }
    }

    /**
     * DC11-SDP巡检报告查询
     *
     * @return
     */
    @GetMapping("/checkReportDaily")
    public ResultMsg checkReportDaily(@RequestParam(name="beginReportDate", required = false) String beginReportDateString,
                                      @RequestParam(name="endReportDate", required = false) String endReportDateString,
                                      @RequestParam(name="region") String region) {
        Date beginDate = null;
        if (!StringUtils.isEmpty(beginReportDateString)) {
            beginDate = getDayBeginTime(DateUtil.toDate("yyyy-MM-dd", beginReportDateString));
        } else {
            beginDate = getDayEndTime(DateUtil.addDays(new Date(), -7));
        }
        Date endDate = null;
        if (!StringUtils.isEmpty(endReportDateString)) {
            endDate = getDayEndTime(DateUtil.toDate("yyyy-MM-dd", endReportDateString));
        } else {
            endDate = getDayEndTime(new Date());
        }

        List<DailyPlanReport> dailyPlanReports = dailyPlanReportMapper.selectByTime(region, beginDate, endDate);
        List<DailyScaleFailReport> dailyScaleFailReports = dailyScaleFailReportMapper.selectByTime(region, beginDate, endDate);


        List<Map<String, Object>> data = new ArrayList<>();

        List<String> reportIds = new ArrayList<>();
        for (DailyPlanReport dailyPlanReport : dailyPlanReports) {
            String reportId = dailyPlanReport.getReportId();
            if(!reportIds.contains(reportId)){
                reportIds.add(reportId);
            }
        }

        for (DailyScaleFailReport dailyScaleFailReport : dailyScaleFailReports) {
            String reportId = dailyScaleFailReport.getReportId();
            if(!reportIds.contains(reportId)){
                reportIds.add(reportId);
            }
        }

        for (String reportId : reportIds) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("reportDate", reportId);

            HashMap<String, Object> planReportMap = new HashMap<>();
            itemMap.put("planReport", planReportMap);
            List<DailyPlanReport> dailyPlanReportItems = new ArrayList<>();
            planReportMap.put("items", dailyPlanReportItems);
            Iterator<DailyPlanReport> dailyPlanReportIterator = dailyPlanReports.iterator();
            while(dailyPlanReportIterator.hasNext()) {
                DailyPlanReport dailyPlanReport = dailyPlanReportIterator.next();
                if (dailyPlanReport.getReportId().equals(reportId)) {
                    dailyPlanReportItems.add(dailyPlanReport);
                    dailyPlanReportIterator.remove();
                }
            }
            if(!dailyPlanReportItems.isEmpty()){
                planReportMap.put("beginTime", dailyPlanReportItems.get(0).getBeginTime());
                planReportMap.put("endTime", dailyPlanReportItems.get(0).getEndTime());
            } else {
                planReportMap.put("beginTime", "");
                planReportMap.put("endTime", "");
            }

            HashMap<String, Object> scaleFailReportMap = new HashMap<>();
            itemMap.put("scaleFailReport", scaleFailReportMap);
            List<DailyScaleFailReport> dailyScaleFailReportItems = new ArrayList<>();
            scaleFailReportMap.put("items", dailyScaleFailReportItems);
            Iterator<DailyScaleFailReport> dailyScaleFailReportIterator = dailyScaleFailReports.iterator();
            while(dailyScaleFailReportIterator.hasNext()) {
                DailyScaleFailReport dailyScaleFailReport = dailyScaleFailReportIterator.next();
                if (dailyScaleFailReport.getReportId().equals(reportId)) {
                    dailyScaleFailReportItems.add(dailyScaleFailReport);
                    dailyScaleFailReportIterator.remove();
                }
            }
            if(!dailyScaleFailReportItems.isEmpty()){
                scaleFailReportMap.put("beginTime", dailyScaleFailReportItems.get(0).getBeginTime());
                scaleFailReportMap.put("endTime", dailyScaleFailReportItems.get(0).getEndTime());
            } else {
                scaleFailReportMap.put("beginTime", "");
                scaleFailReportMap.put("endTime", "");
            }

            data.add(itemMap);
        }
        return ResultMsg.SUCCESS(data);
    }

    @GetMapping("/failedloglist")
    public ResultMsg getFailedLogList( @RequestParam("apiName") String apiName,
                                        @RequestParam("failedType") String failedType,
                                        @RequestParam("beginReportDate") String begTime,
                                        @RequestParam("endReportDate") String endTime,
                                        @RequestParam("pageIndex") Integer pageIndex,
                                        @RequestParam("pageSize") Integer pageSize) {
        begTime = StrUtil.emptyToDefault(begTime, cn.hutool.core.date.DateUtil.offsetDay(new Date(), -7).toString());
        endTime = StrUtil.emptyToDefault(endTime, cn.hutool.core.date.DateUtil.formatDateTime(new Date()));
        pageIndex = ObjectUtil.defaultIfNull(pageIndex, 1);
        pageSize = ObjectUtil.defaultIfNull(pageSize, 20);

        Map param = new HashMap();
        param.put("apiName", StrUtil.emptyToDefault(apiName, null));
        param.put("failedType", StrUtil.emptyToDefault(failedType, null));
        param.put("begTime", begTime);
        param.put("endTime", endTime);
        param.put("pageIndex", pageIndex);
        param.put("pageSize", pageSize);
        Integer total = doctorService.getThridApiFailedLogCount(param);
        if (total == 0) {
            return ResultMsg.SUCCESST(new ArrayList<>());
        }
        List<InfoThirdApiFailedLog> list = doctorService.getThridApiFailedLogList(param);
        ResultMsg resultMsg = ResultMsg.SUCCESST(list);
        resultMsg.setTotal(total);
        return resultMsg;
    }

    @GetMapping("/failedlog/{id}")
    public ResultMsg getFailedLog(@PathVariable("id") Long id) {
        InfoThirdApiFailedLog log = doctorService.getThridApiFailedLogById(id);
        return ResultMsg.SUCCESS(log);
    }

    /**
     * 查询Azure端机器清理任务,Azure端僵尸机清理任务,
     * @param request
     * @return
     */
    @PostMapping("/getAzureCleanedVms")
    public ResultMsg listAzureVm(@RequestBody AzureVmtraceInfoRequest request) {
        return doctorService.listAzureVm(request);
    }
}
