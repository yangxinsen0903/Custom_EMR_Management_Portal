package com.sunbox.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.InfoClusterOperationPlanActivityLogWithBLOBs;
import com.sunbox.domain.ResultMsg;
import com.sunbox.service.IAzureService;
import com.sunbox.service.IVMService;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author : [niyang]
 * @className : VMServiceImpl
 * @description : [描述说明该类的功能]
 * @createTime : [2023/8/1 12:42 PM]
 */
@Service
public class VMServiceImpl implements IVMService, BaseCommonInterFace {

    @Autowired
    private IAzureService azureService;

    /**
     * 查询jobDetail
     *
     * @param jobId
     * @param clusterName
     * @return
     */
    @Override
    public JSONObject getProvisionDetail(String jobId, String clusterName,String region) {
        ResultMsg detailMsg = azureService.provisionDetail(jobId,region);
        if (detailMsg.getResult() && detailMsg.getData() != null) {
            JSONObject datajson = JSON.parseObject(JSON.toJSONString(detailMsg.getData()));
            ResultMsg ckMsg=checkProvisionDetailComplete(datajson, clusterName);
            getLogger().info("检查provisionDetail结果：{}",ckMsg);
            if (ckMsg.getResult()){
                return datajson;
            }
        }
        return null;
    }


    /**
     * 检测ProvisionDetail数据是否完整
     * 由于Azure的ProvisionDetail接口为聚合接口，存在数据不完整的情况，
     * 在使用该接口时需要校验数据的完整性。
     * 数据的不完整性主要表现在deployDetailResults已经有结果，对于Succeeded的deploy，
     * 在provisionedVmGroups属性中没有给出详细的VM信息。
     * 判断的基本逻辑： 统计deployDetailResults中Succeeded的VM数量 与provisionedVmGroups中对应的角色VM数量比较
     * provisionedVmGroups中的VM数量要>=deployDetailResults中Succeeded的VM数量
     *
     * @param detailMsg
     * @param clusterName
     * @return
     */
    private ResultMsg checkProvisionDetailComplete(JSONObject detailMsg, String clusterName){
        ResultMsg msg = new ResultMsg<>();

        //region provisonDetail接口返回success
        if (detailMsg!=null &&
                detailMsg.containsKey("provisionStatus") &&
                detailMsg.getString("provisionStatus").equalsIgnoreCase("Succeed")){
            JSONArray vmGroups = detailMsg.getJSONArray("provisionedVmGroups");
            if(vmGroups != null && vmGroups.size()>0){
                msg.setResult(true);
                return msg;
            }
        }
        //endregion

        //region 统计deployDetailResults中成功的VM 按角色
        JSONArray deployDetailResults = detailMsg.getJSONArray("deployDetailResults");

        if (deployDetailResults == null){
            getLogger().warn("deployDetailResults结果为空。");
            msg.setResult(false);
            return msg;
        }


        int cntAmb = 0, cntMst =0, cntCor = 0, cntTsk =0;

        for (int i =0;i<deployDetailResults.size();i++){
            JSONObject deploy = deployDetailResults.getJSONObject(i);

            if (deploy.containsKey("provisionState") &&
                    deploy.getString("provisionState").equalsIgnoreCase("Succeeded")){
                String deployName = deploy.getString("deployName");
                if (deployName.startsWith(clusterName+"-amb-")){
                    JSONArray vms = deploy.getJSONArray("vMs");
                    if (vms !=null && vms.size()>0){
                        cntAmb += vms.size();
                    }
                }

                if (deployName.startsWith(clusterName+"-cor-")){
                    JSONArray vms = deploy.getJSONArray("vMs");
                    if (vms !=null && vms.size()>0){
                        cntCor += vms.size();
                    }
                }
                if (deployName.startsWith(clusterName+"-mst-")){
                    JSONArray vms = deploy.getJSONArray("vMs");
                    if (vms !=null && vms.size()>0){
                        cntMst += vms.size();
                    }
                }
                if (deployName.startsWith(clusterName+"-tsk-")){
                    JSONArray vms = deploy.getJSONArray("vMs");
                    if (vms !=null && vms.size()>0){
                        cntTsk += vms.size();
                    }
                }
            }
        }
        getLogger().info("cnAmb:{},cntMst:{},cntCor:{},cntTsk:{}",cntAmb,cntMst,cntCor,cntTsk);
        //endregion

        //region 统计provisionedVmGroups各个vmrole成功的数量
        JSONArray vmGroups = detailMsg.getJSONArray("provisionedVmGroups");
        Integer cntCore=0,cntMaster=0,cntAmbari=0,cntTask=0;

        for (int i = 0; i < vmGroups.size(); i++) {
            JSONObject group = vmGroups.getJSONObject(i);
            if (group != null && group.containsKey("groupName") && group.containsKey("count")) {
                String groupName = group.getString("groupName");
                Integer count = group.getInteger("count");
                switch (groupName) {
                    case "core":
                        cntCore = count;
                        break;
                    case "master":
                        cntMaster = count;
                        break;
                    case "ambari":
                        cntAmbari = count;
                        break;
                    case "task":
                        cntTask = count;
                        break;
                }
            }
        }
        getLogger().info("cntAmbari:{},cntMaster:{},cntCore:{},cntTask:{}",cntAmbari, cntMaster, cntCore, cntTask);
        //endregion

        //region 逻辑判断
        if (cntAmbari < cntAmb){
            msg.setResult(false);
            msg.setErrorMsg("Ambari数据不完整。");
            return msg;
        }

        if (cntMaster < cntMst){
            msg.setResult(false);
            msg.setErrorMsg("Master数据不完整。");
            return msg;
        }

        if (cntCore < cntCor){
            msg.setResult(false);
            msg.setErrorMsg("Core数据不完整。");
            return msg;
        }

        if (cntTask < cntTsk){
            msg.setResult(false);
            msg.setErrorMsg("Task数据不完整。");
            return msg;
        }

        //endregion

        msg.setResult(true);
        return msg;

    }
}
