package com.sunbox.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sunbox.constant.MetaDataConstants;
import com.sunbox.dao.mapper.AuthKeyMapper;
import com.sunbox.dao.mapper.BaseUserRegionMapper;
import com.sunbox.domain.ApiAuthKey;
import com.sunbox.domain.AuthRequest;
import com.sunbox.domain.BaseUserInfo;
import com.sunbox.domain.ResultMsg;
import com.sunbox.service.AuthKeyService;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.service.IUserInfoService;
import com.sunbox.service.consts.SheinParamConstant;
import com.sunbox.util.DateUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthKeyServiceImpl implements AuthKeyService {

    @Resource
    private AuthKeyMapper  authKeyMapper;

    @Resource
    private IUserInfoService userInfoService;

    @Resource
    private BaseUserRegionMapper baseUserRegionMapper;

    @Resource
    private IMetaDataItemService metaDataItemService;

    @Override
    public ResultMsg queryAllAuthKeyList(AuthRequest authRequest) {

        int total = authKeyMapper.selectCountByName(authRequest.getName());
        if (total <= 0) {
            return ResultMsg.SUCCESS();
        }

        if (authRequest.getPageIndex() == null) {
            authRequest.setPageIndex(1);
        }
        if (authRequest.getPageSize() == null) {
            authRequest.setPageSize(10);
        }
        int page = (authRequest.getPageIndex() - 1) * authRequest.getPageSize();
        int pageSize = authRequest.getPageSize();
        List<ApiAuthKey> authKeyList = authKeyMapper.selectAllByName(authRequest.getName(), page, pageSize);
        // 把authKeyList的每一项的secretKey 设置为null
        authKeyList.forEach(item -> item.setSecretKey(null));
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);
        resultMsg.setData(authKeyList);
        resultMsg.setTotal(total);
        return resultMsg;
    }

    @Override
    public ResultMsg createAuthInfo(ApiAuthKey authKey,HttpServletRequest request) {
        BaseUserInfo userRoleByRequest = userInfoService.getUserRoleByRequest(request);
        authKey.setCreatedby(userRoleByRequest.getUserId());
        authKey.setCreatedTime(new Date());
        authKey.setExpirationDate( authKey.getExpirationDate()==null? DateUtil.dateAddOrSubHour(new Date(), 24) : authKey.getExpirationDate() );
        String ak = UUID.randomUUID().toString().replaceAll("-","");
        String sk = UUID.randomUUID().toString().replaceAll("-","");
        authKey.setAccessKey(ak);
        authKey.setSecretKey(sk);
        authKey.setStatus(SheinParamConstant.VALID);
        int res = authKeyMapper.insert(authKey);
        if (res > 0) {
            ApiAuthKey apiAuthKey = new ApiAuthKey();
            apiAuthKey.setAccessKey(ak);
            apiAuthKey.setSecretKey(sk);
            ResultMsg resultMsg = new ResultMsg();
            resultMsg.setResult(true);
            resultMsg.setData(apiAuthKey);
            resultMsg.setMsg("Secret Key只显示一次，请复制并保存好! ");
            return resultMsg ;
        } else {
            return ResultMsg.FAILURE("创建失败！");
        }
    }

    @Override
    public ResultMsg updateAuthKey(ApiAuthKey authKey, HttpServletRequest request) {
        if (StringUtils.isEmpty(authKey.getId()) || StringUtils.isEmpty(authKey.getExpirationDate()) ||
                StringUtils.isEmpty(authKey.getPermission()) || StringUtils.isEmpty(authKey.getName())) {
            return ResultMsg.FAILURE("参数不能为空！");
        }
        authKey.setModifiedTime(new Date());
        BaseUserInfo userRoleByRequest = userInfoService.getUserRoleByRequest(request);
        authKey.setModifiedby(userRoleByRequest.getUserId());
        //判断有效期是否过期, 如果过期,更新为过期, 如果没过期, 更新为未过期
        if (authKey.getExpirationDate().getTime() < new Date().getTime()) {
            authKey.setStatus(SheinParamConstant.EXPIRED);
        } else {
            authKey.setStatus(SheinParamConstant.VALID);
        }
        int i = authKeyMapper.updateByPrimaryKey(authKey);
        return ResultMsg.SUCCESS("更新成功！");
    }

    @Override
    public ResultMsg deleteAuthKey(ApiAuthKey authKey) {
        int i = authKeyMapper.deleteByPrimaryKey(authKey.getId());
        return ResultMsg.SUCCESS("删除成功！");
    }

    @Override
    public ResultMsg getRegionsForCurrentUser(HttpServletRequest request) {
        BaseUserInfo userRoleByRequest = userInfoService.getUserRoleByRequest(request);
        // 包含了多个数据中心
        List<Map<String, Object>> mapList = baseUserRegionMapper.selectRegionByUserIds(Arrays.asList(userRoleByRequest.getUserName()));
        List<JSONObject> dataItemResponses=new ArrayList<>();
        if(CollectionUtils.isEmpty(mapList)){
            ResultMsg.SUCCESS(dataItemResponses);
        }
        Map<String, String> regionMap = metaDataItemService.getRegionMap();
         dataItemResponses = mapList.stream().filter(vo -> vo.get("data")!=null).map(dataItem -> {
            JSONObject object = JSONObject.parseObject((String) dataItem.get("data"));
            object.put("id", dataItem.get("rId"));
            object.put(MetaDataConstants.REMARK, dataItem.get("remark"));
            object.put(MetaDataConstants.VERSION, dataItem.get("version"));
            object.put(MetaDataConstants.TYPE, dataItem.get("type"));
            object.put("createTime", DateUtil.string2Date(dataItem.get("createTime").toString()));
            object.put("lastModifiedTime", DateUtil.string2Date(dataItem.get("createTime").toString()));
            String regionName = regionMap.get(object.getString(MetaDataConstants.REGION));
            object.put(MetaDataConstants.REGION_NAME,regionName);
            return object;
        }).collect(Collectors.toList());
       return ResultMsg.SUCCESS(dataItemResponses);
    }

}
