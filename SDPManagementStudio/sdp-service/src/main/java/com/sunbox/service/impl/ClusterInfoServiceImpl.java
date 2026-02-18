package com.sunbox.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.sunbox.dao.mapper.ConfClusterNeoMapper;
import com.sunbox.dao.mapper.ConfClusterTagNeoMapper;
import com.sunbox.dao.mapper.WorkOrderApprovalRequestMapper;
import com.sunbox.domain.BaseUserInfo;
import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.ConfClusterTag;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.cluster.*;
import com.sunbox.service.BizConfigService;
import com.sunbox.service.IClusterInfoService;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.service.IUserInfoService;
import com.sunbox.service.consts.CommonConstant;
import com.sunbox.util.HttpClientUtil;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.sunbox.service.consts.CommonConstant.*;
import static com.sunbox.service.consts.SheinParamConstant.*;

@Service
public class ClusterInfoServiceImpl implements IClusterInfoService, BaseCommonInterFace {

    @Autowired
    private BizConfigService bizConfigService;

    @Autowired
    private WorkOrderApprovalRequestMapper  workOrderApprovalRequestMapper;

    @Autowired
    private IUserInfoService userInfoService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private ConfClusterTagNeoMapper confClusterTagMapper;
    @Autowired
    private ConfClusterNeoMapper confClusterNeoMapper;
    @Autowired
    private IMetaDataItemService metaDataItemService;


    //工单创建集群
    @Override
    public ResultMsg createClusterByWorkOrderTicket(String jsonStr,String clusterId,String userName) {
        // checkInPutParam(jsonStr);
        TicketCreateRequest request = convertParam(jsonStr,"创建大数据集群:");
        ResultMsg resultMsg = sendHttpRequest(request,clusterId,REQUEST_TYPE_CREATE,userName);

        return resultMsg;
    }

    @Override
    public ResultMsg destroyClusterByWorkOrderTicket(String jsonStr, String clusterId,String userName) {
        // checkInPutParam(jsonStr);
        Map<String, String> inParam = new HashMap<>();
        ConfClusterTag confClusterTag = new ConfClusterTag();
        confClusterTag.setClusterId(clusterId);
        List<ConfClusterTag> confClusterTagList = confClusterTagMapper.selectByObject(confClusterTag);
        ConfCluster confCluster = confClusterNeoMapper.selectByPrimaryKey(clusterId);
        Map<String, String> regionMap = metaDataItemService.getRegionMap();
        inParam.put("regionName",regionMap.get(confCluster.getRegion()));
        inParam.put("clusterName",confCluster.getClusterName());
        Map<String, String> tagMap = new HashMap<>();
        for (ConfClusterTag clusterTag : confClusterTagList) {
            tagMap.put(clusterTag.getTagGroup(),clusterTag.getTagVal());
        }
        inParam.put("tagMap",JSONObject.toJSONString(tagMap));
        inParam.put("region",confCluster.getRegion());
        Map<String, String> instanceGroupVersion = new HashMap<>();
        instanceGroupVersion.put("clusterReleaseVer",confCluster.getClusterReleaseVer());
        inParam.put("instanceGroupVersion",JSONObject.toJSONString(instanceGroupVersion));
        TicketCreateRequest request = convertParam(JSONObject.toJSONString(inParam),"销毁大数据集群:");

        ResultMsg resultMsg = sendHttpRequest(request,clusterId,REQUEST_TYPE_DESTORY,userName);

        return resultMsg;
    }


    @Override
    public ResultMsg sendHttpRequest(TicketCreateRequest ticketCreateRequest,String clusterId,String requestType,String userName) {
        //把ticketCreateRequest转为json字符串
        String jsonStr = JSON.toJSONString(ticketCreateRequest);
        ResultMsg msg = new ResultMsg();
        Map<String, String> recordMap = new HashMap<>();

        List<String> list = new ArrayList<>();
        list.add(WORKFLOWURL);
        list.add(WORKFLOWTOKEN);
        Map<String, String> configValueMapByKey = bizConfigService.getConfigValueMapByKey(list);
        if (configValueMapByKey == null || configValueMapByKey.isEmpty()) {
            msg.setResult(false);
            msg.setMsg("工单系统URL或token配置信息不存在,请添加");
            return msg;
        }
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(SHEIN_XTOKEN_NAME,configValueMapByKey.get(WORKFLOWTOKEN));

        recordMap.put("headerMap",headerMap.toString());
        recordMap.put(WORKFLOWURL,configValueMapByKey.get(WORKFLOWURL));
        recordMap.put("inParam",jsonStr);


        String result = "";
       // TicketCreateResponse ticketCreateResponse11=new TicketCreateResponse();
        try {
            long start = System.currentTimeMillis();
            // 暂时调用假的接口.
              //ticketCreateResponse11 = ticketCreate(new TicketCreateRequest(),clusterId);
            result = HttpClientUtil.doPost(configValueMapByKey.get(WORKFLOWURL) , jsonStr, headerMap);
            getLogger().info("ClusterInfoServiceImpl.sendHttpRequest, elapse:{}ms, respStr: {}",
                   (System.currentTimeMillis() - start), result);

            msg.setResult(StringUtils.isNotEmpty(result));
            //msg.setResult(true);
        } catch (Exception ex) {
            msg.setResult(false);
            throw new RuntimeException("ticket create fail: result=" + result, ex);
        }
        TicketCreateResponse ticketCreateResponse= null;
        try {
            ticketCreateResponse = JSONObject.parseObject(result, TicketCreateResponse.class);
           // ticketCreateResponse = ticketCreateResponse11;
           // msg.setResult(true);
        } catch (Exception e) {
            String message = StrUtil.format("ClusterInfoServiceImpl.sendHttpRequest.parseObject error, respStr: {}",
                    result);
            msg.setResult(false);
            getLogger().error(message, e);
            throw new RuntimeException("ticket create parseObject fail: message=" + message, e);
        }
        recordMap.put("outParam",ticketCreateResponse.toString());
        //存储 ticket_id,
        WorkOrderApprovalRequest workOrderApprovalRequest = new WorkOrderApprovalRequest();
        workOrderApprovalRequest.setClusterId(clusterId);
        workOrderApprovalRequest.setTicketId(ticketCreateResponse.getData().getTicketid());
        workOrderApprovalRequest.setRequestType(requestType);
        workOrderApprovalRequest.setApprovalState(APPROVAL_STATE_INIT);
        workOrderApprovalRequest.setCreatedTime(new Date());
        workOrderApprovalRequest.setApprovalResult(recordMap.toString());
        workOrderApprovalRequest.setCreatedby(userName);
        workOrderApprovalRequestMapper.insertSelective(workOrderApprovalRequest);
        getLogger().info("ClusterInfoServiceImpl.sendHttpRequest,  workOrderApprovalRequest: {}", workOrderApprovalRequest);
        msg.setData("创建工单成功");
        return msg;

    }

    @Override
    public Boolean checkIsDestroyWorkerOrder(String clusterId) {
        // 有创建 && 没有销毁 动作, 应该销毁.       重试的场景, 打回... 会存入多条数据?.每次的都要记录
        List<WorkOrderApprovalRequest> workOrderApprovalRequests = workOrderApprovalRequestMapper.selectByClusterIds(Collections.singletonList(clusterId));
        getLogger().info("ClusterInfoServiceImpl.checkIsDestroyWorkerOrder,workOrderApprovalRequests: {}", workOrderApprovalRequests);
        if (CollectionUtils.isEmpty(workOrderApprovalRequests)){
            return false;
        }
        List<String> create=new ArrayList<>();
        List<String> destroy=new ArrayList<>();
        for (WorkOrderApprovalRequest request : workOrderApprovalRequests) {
            if(REQUEST_TYPE_CREATE.equalsIgnoreCase(request.getRequestType()) && APPROVAL_STATE_AGREE.equalsIgnoreCase(request.getApprovalState())){
                create.add(request.getRequestType());
            }
            if(REQUEST_TYPE_DESTORY.equalsIgnoreCase(request.getRequestType()) && APPROVAL_STATE_AGREE.equalsIgnoreCase(request.getApprovalState())){
                destroy.add(request.getRequestType());
            }
        }
        if (!CollectionUtils.isEmpty(create) && CollectionUtils.isEmpty(destroy)){
            return true;
        }
        return false;
    }

    // 假接口
//    @Deprecated
//    public TicketCreateResponse ticketCreate( TicketCreateRequest request,String clusterId) {
//        List<WorkOrderApprovalRequest> workOrderApprovalRequests = workOrderApprovalRequestMapper.selectByClusterIds(Collections.singletonList(clusterId));
//        String ticketId="";
//        if (!CollectionUtils.isEmpty(workOrderApprovalRequests)){
//            ticketId = workOrderApprovalRequests.get(0).getTicketId();
//        }else {
//             ticketId =IdUtil.getSnowflakeNextIdStr();
//        }
//        TicketCreateResponse ticketCreateResponse = new TicketCreateResponse();
//        ticketCreateResponse.setCode("200");
//        Data data = new Data();
//        data.setTicketid(ticketId);
//        ticketCreateResponse.setData(data);
//
//        return  ticketCreateResponse;
//    }


    /**
     * 原始json 转 工单接口入参TicketCreateRequest
     * @param jsonStr 原始json
     * @return
     */
    private TicketCreateRequest convertParam( String jsonStr, String name) {
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        String regionName = jsonObject.get("regionName").toString();
        TicketCreateRequest resTicketInParam = new TicketCreateRequest();
        //resTicketInParam.setCuser("");
        String clusterName = jsonObject.get("clusterName").toString();
        String des=name+clusterName;
        resTicketInParam.setDescription(des);
        //resTicketInParam.setIs_fast_mode(1);
        //resTicketInParam.setIs_fill_empty(false);
        NodeOperator nodeOperator = new NodeOperator();
        ArrayList<String> userIds = new ArrayList<>();
        //当前登录用户的员工ID
        BaseUserInfo userRoleByRequest = userInfoService.getUserRoleByRequest(httpServletRequest);
        userIds.add(userRoleByRequest.getUserId());
        nodeOperator.setOperators(userIds);
        nodeOperator.setNode_name("审批任务");
        ArrayList<NodeOperator> nodeOperators = new ArrayList<>();
        nodeOperators.add(nodeOperator);
        resTicketInParam.setNode_operators(nodeOperators);

        JSONObject tagMap = jsonObject.getJSONObject("tagMap");
        String afor = tagMap.get("for").toString();
        String aservice = tagMap.get("service").toString();
        String asvc = tagMap.get("svc").toString();
        String asvcid = tagMap.get("svcid").toString();
        Field field1 = new Field();
        field1.setField_name("for");
        field1.setText("标签for");
        field1.setValue(afor);

        Field field2 = new Field();
        field2.setField_name("service");
        field2.setText("标签service");
        field2.setValue(aservice);

        Field field3 = new Field();
        field3.setField_name("svc");
        field3.setText("标签svc");
        field3.setValue(asvc);

        Field field4 = new Field();
        field4.setField_name("svcid");
        field4.setText("标签svcid");
        field4.setValue(asvcid);

        Field field5 = new Field();
        field5.setField_name("clusterName");
        field5.setText("集群名称");
        field5.setValue(clusterName);

        Field field6 = new Field();
        field6.setField_name("region");
        field6.setText("数据中心");

        field6.setValue(jsonObject.get("region").toString());

        Field field7 = new Field();
        field7.setField_name("regionName");
        field7.setText("数据中心名称");
        field7.setValue(regionName);

        Field field8 = new Field();
        field8.setField_name("clusterReleaseVer");
        field8.setText("产品版本");
        String clusterReleaseVer = jsonObject.getJSONObject("instanceGroupVersion").get("clusterReleaseVer").toString();
        field8.setValue(clusterReleaseVer);

        Record record = new Record();
        record.setFields(Lists.newArrayList(field1, field2, field3, field4, field5, field6, field7, field8));
        record.setSequence(1);
        resTicketInParam.setRecords(Lists.newArrayList(record));

        ArrayList<String> strings = new ArrayList<>();
        strings.add(CommonConstant.WORKFLOWNAME);
        strings.add(CommonConstant.WORKFLOWIDENTIFIER);
        Map<String, String> configValueMap = bizConfigService.getConfigValueMapByKey(strings);
        resTicketInParam.setWorkflow_identifier(configValueMap.get(CommonConstant.WORKFLOWIDENTIFIER));
        resTicketInParam.setWorkflow_name(configValueMap.get(CommonConstant.WORKFLOWNAME));

        resTicketInParam.setTitle(des);
        resTicketInParam.setUser_type("ulp");
        return resTicketInParam;
    }

}
