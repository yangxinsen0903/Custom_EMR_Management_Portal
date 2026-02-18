package com.sunbox.service.impl;
import java.util.Date;

import com.sunbox.dao.mapper.AmbariConfigItemDMapper;
import com.sunbox.dao.mapper.BaseReleaseAppsConfigDataMapper;
import com.sunbox.dao.mapper.ConfigDetailMapper;
import com.sunbox.domain.*;
import com.sunbox.domain.ambari.AmbariConfigItem;
import com.sunbox.service.IConfigInfoService;
import com.sunbox.service.IUserInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class ConfigInfoServiceImpl implements IConfigInfoService {

    @Resource
    ConfigDetailMapper configDetailMapper;

    @Autowired
    AmbariConfigItemDMapper ambariConfigItemMapper;
    @Autowired
    HttpServletRequest httpServletRequest;
    @Autowired
    IUserInfoService userInfoService;
    @Autowired
    BaseReleaseAppsConfigDataMapper baseReleaseAppsConfigDataMapper;


    @Override
    public ResultMsg queryComponentList() {
        List<Map<String, String>> res = new ArrayList<>();
        List<String> serviceCodeList = ambariConfigItemMapper.selectComponentList();
        for (String s : serviceCodeList) {
            Map hashMap = new HashMap();
            hashMap.put("serviceCode", s);
            res.add(hashMap);
        }
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setData(res);
        resultMsg.setResult(true);
        return resultMsg;
    }

    @Override
    public ResultMsg queryProfilesList(List<String> releaseVersion) {
        // 从  base_release_apps_config  表里取.
        List<String> profilesList = baseReleaseAppsConfigDataMapper.selectAll(releaseVersion);
        List<Map<String, String>> res = new ArrayList<>();
//        List<String> profilesList = ambariConfigItemMapper.selectProfilesList();
        for (String s : profilesList) {
            Map hashMap = new HashMap();
            hashMap.put("configTypeCode", s);
            res.add(hashMap);
        }
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setData(res);
        resultMsg.setResult(true);
        return resultMsg;
    }

    @Override
    public ResultMsg queryConfigList(AmbariConfigItemRequest request) {
        request.page();
        if (!StringUtils.isEmpty(request.getItemType())) {
            request.setItemType("1".equals(request.getItemType()) ? "HA" : "NON_HA");
        }
        ResultMsg resultMsg = new ResultMsg();
        List<AmbariConfigItem> ambariConfigItems = ambariConfigItemMapper.selectByPage(request);
        for (AmbariConfigItem ambariConfigItem : ambariConfigItems) {
            //高可用1，非高可用0 */
            ambariConfigItem.setItemType("HA".equalsIgnoreCase(ambariConfigItem.getItemType()) ? "1" : "0");
        }
        int total = ambariConfigItemMapper.selectTotalByPage(request);
        resultMsg.setData(ambariConfigItems);
        resultMsg.setTotal(total);
        resultMsg.setResult(true);
        return resultMsg;
    }

    @Override
    public ResultMsg queryConfigById(String id) {
        ResultMsg resultMsg = new ResultMsg();
        AmbariConfigItem ambariConfigItem = ambariConfigItemMapper.selectByPrimaryKey(Long.valueOf(id));
        if (ambariConfigItem == null) {
            resultMsg.setResult(true);
            return resultMsg;
        }
        ambariConfigItem.setItemType("HA".equalsIgnoreCase(ambariConfigItem.getItemType()) ? "1" : "0");
        ArrayList<AmbariConfigItem> list = new ArrayList<>();
        list.add(ambariConfigItem);
        resultMsg.setData(list);
        resultMsg.setResult(true);
        return resultMsg;
    }

    @Override
    public ResultMsg addConfig(AmbariConfigAddRequest request) {
        List<String> stackCodeList = request.getStackCode();
        List<String> itemTypeList  = request.getItemType();
        List<String> itemTypeListN=new ArrayList<>();
        List<String> serviceCodeList  = request.getServiceCode();
        //把itemTypeList=1,0 转换成 HA,NON_HA
        for (String s : itemTypeList) {
            if ("1".equals(s)) {
                itemTypeListN.add("HA");
            } else{
                itemTypeListN.add("NON_HA");
            }
        }
          List<AmbariConfigItem> itemList = new ArrayList<>();
        for (String stack : stackCodeList) {
            for (String item : itemTypeListN) {
                for (String service : serviceCodeList) {
                    AmbariConfigItem ambariConfigItem = new AmbariConfigItem();
                    ambariConfigItem.setStackCode(stack);
                    ambariConfigItem.setItemType(item);
                    ambariConfigItem.setServiceCode(service);
                    itemList.add(ambariConfigItem);
                }

            }
        }
        BaseUserInfo userRoleByRequest = userInfoService.getUserRoleByRequest(httpServletRequest);
        for (AmbariConfigItem ambariConfigItem : itemList) {
            ambariConfigItem.setKey(request.getKey());
            ambariConfigItem.setValue(request.getValue());
            ambariConfigItem.setIsContentProp(request.getIsContentProp());
            ambariConfigItem.setIsDynamic(request.getIsDynamic());
            ambariConfigItem.setDynamicType(request.getDynamicType());
            ambariConfigItem.setState(request.getState());
            ambariConfigItem.setCreatedBy(userRoleByRequest.getUserName());
            ambariConfigItem.setCreatedTime(new Date());
            //todo 目前用ServiceCode
            ambariConfigItem.setComponentCode(ambariConfigItem.getServiceCode());
            // base_release_apps_config
            ambariConfigItem.setConfigTypeCode(request.getConfigTypeCode());
        }
        ambariConfigItemMapper.insertBatch(itemList);
        return ResultMsg.SUCCESS();
    }

    @Override
    public ResultMsg updateConfig(AmbariConfigAddRequest request) {
        AmbariConfigItem ambariConfigItem = new AmbariConfigItem();
        if(0== request.getIsDynamic()){
            request.setDynamicType("");
        };
        BeanUtils.copyProperties(request,ambariConfigItem);
        ambariConfigItem.setStackCode(request.getStackCode().get(0));
        ambariConfigItem.setItemType("1".equalsIgnoreCase(request.getItemType().get(0))? "HA" : "NON_HA");
        ambariConfigItem.setServiceCode(request.getServiceCode().get(0));
        ambariConfigItemMapper.updateByPrimaryKeySelective(ambariConfigItem);
        return ResultMsg.SUCCESS();
    }

    @Override
    public ResultMsg deleteConfig(AmbariConfigItem request) {
        ambariConfigItemMapper.deleteByPrimaryKey(request.getId());
        return ResultMsg.SUCCESS();
    }

    public void updateConf(String name, String value) {
        ConfigDetail configDetail = new ConfigDetail();
        configDetail.setAkey(name);
        configDetail.setAvalue(value);
        configDetailMapper.updateByaKey(configDetail);
    }
}
