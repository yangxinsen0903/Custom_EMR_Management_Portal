package com.sunbox.sdpadmin.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.constant.MetaDataConstants;
import com.sunbox.domain.*;
import com.sunbox.sdpadmin.core.util.CookieUtil;
import com.sunbox.sdpadmin.core.util.JacksonUtil;
import com.sunbox.sdpadmin.mapper.BaseUserInfoMapper;
import com.sunbox.dao.mapper.BaseUserRegionMapper;
import com.sunbox.dao.mapper.BaseUserRoleMapper;
import com.sunbox.sdpadmin.model.admin.request.BaseUserInfoRequest;
import com.sunbox.sdpadmin.model.admin.request.LoginData;
import com.sunbox.sdpadmin.model.admin.response.UserRoleRegionOutput;
import com.sunbox.sdpadmin.service.LoginService;
import com.sunbox.sdpadmin.util.RedisUtil;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 */
@Service
public class LoginServiceImpl implements LoginService, BaseCommonInterFace {

    public static final String LOGIN_IDENTITY_KEY = "SDP_LOGIN_IDENTITY";

    public static final String LOGIN_ADMIN_IDENTITY_KEY = "SDP_ADMIN_LOGIN_IDENTITY";

    @Autowired
    private BaseUserInfoMapper baseUserInfoMapper;

    @Autowired
    private DistributedRedisLock redisLock;

    @Resource
	private RedisUtil redisUtil;

    @Autowired
    private BaseUserRoleMapper baseUserRoleMapper;

    @Autowired
    private BaseUserRegionMapper baseUserRegionMapper;

    @Resource
    private IMetaDataItemService metaDataItemService;


    public String makeToken(SysUsers sysUsers){
        String tokenJson = JacksonUtil.writeValueAsString(sysUsers);
        String tokenHex = new BigInteger(tokenJson.getBytes()).toString(16);
        return tokenHex;
    }

    private SysUsers parseToken(String tokenHex){
        SysUsers sysUsers = null;
        if (StringUtils.isNotEmpty(tokenHex)) {
            String tokenJson = new String(new BigInteger(tokenHex, 16).toByteArray());      // username_password(md5)
            sysUsers = JacksonUtil.readValue(tokenJson, SysUsers.class);
        }
        return sysUsers;
    }

    @Override
    public ResultMsg login(HttpServletRequest request, HttpServletResponse response, LoginData loginData) {
        ResultMsg resultMsg = new ResultMsg();
        if (StringUtils.isBlank(loginData.getUserName()) || StringUtils.isBlank(loginData.getPassword())) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("账号或密码为空.");
            return resultMsg;
        }

        BaseUserInfo baseUserInfo = baseUserInfoMapper.selectByUsername(loginData.getUserName());
        if (baseUserInfo == null) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("账号或密码错误.");
            return resultMsg;
        }

        // MD5加密密码
        // String password = MD5Util.md5Hex(MD5Util.md5Hex(loginData.getUserName() + loginData.getPassword()));
        if (loginData.getPassword().equals(baseUserInfo.getPassword())) {
            //获取用户所属数据中心
            List<String> userRegionList = baseUserRegionMapper.listUserRegion(baseUserInfo.getUserId());
            baseUserInfo.setUserRegionList(userRegionList);
            String baseUserInfoJson = JSON.toJSONString(baseUserInfo);
            // 生成 Token
            UUID uuid = UUID.randomUUID();
            String token = uuid.toString().replaceAll("-", "");

            // 缓存到 Redis
            redisLock.save(token, baseUserInfoJson, 60 * 60 * 10);
            // 查询用户的权限
             BaseUserRole baseUserRoles = baseUserRoleMapper.selectRoleByUser(baseUserInfo.getUserName());
            // 组装正确信息
            resultMsg.setResult(true);
            resultMsg.setData(token);
            if(baseUserRoles!=null){
                resultMsg.setExt1(baseUserRoles.getRoleCode());
                resultMsg.setExt2(baseUserRoles.getRoleName());
            }
            getLogger().info("LoginService.login success. username: " + loginData.getUserName());
        } else {
            // 组装错误信息
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("账号或密码错误.");
            getLogger().info("LoginService.login error. username: " + loginData.getUserName());
        }
        return resultMsg;
    }

    @Override
    public ResultMsg userLogout(HttpServletRequest request, HttpServletResponse response) {
        ResultMsg resultMsg = new ResultMsg();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                switch (cookie.getName()) {
                    case "sdptoken":
                        // 清除Redis中用户登录缓存
                        if (redisLock.haveKey(cookie.getValue())) {
                            redisLock.delete(cookie.getValue());
                        }

                        // 清除cookie
                        cookie.setValue("");
                    default:
                        break;
                }
            }
        }

        resultMsg.setResult(true);
        resultMsg.setMsg("success");
        return resultMsg;
    }

    @Override
    public ResultMsg getUserList(BaseUserInfoRequest baseUserInfoRequest) {
        ResultMsg resultMsg = new ResultMsg();
        if (baseUserInfoRequest.getPageIndex() == null) {
            baseUserInfoRequest.setPageIndex(1);
        }
        if (baseUserInfoRequest.getPageSize() == null) {
            baseUserInfoRequest.setPageSize(10);
        }

        // 参数校验
        if (baseUserInfoRequest.getPageIndex() < 1 || baseUserInfoRequest.getPageSize() < 0) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("参数有误.");
            return resultMsg;
        }
//        int total=baseUserInfoMapper.selectAllCountByUsername(baseUserInfoRequest.getUserName(),
//                baseUserInfoRequest.getRealName());
        Map<String, Object> queryCondition = new HashMap<>();
        queryCondition.put("userName",baseUserInfoRequest.getUserName());
        queryCondition.put("realName",baseUserInfoRequest.getRealName());
        queryCondition.put("roleCode",baseUserInfoRequest.getRoleCode());
        queryCondition.put("region",baseUserInfoRequest.getRegion());

        int total = baseUserInfoMapper.selectCountByUserRole(queryCondition);
        if(total<=0){
            resultMsg.setResult(true);
            resultMsg.setTotal(0);
            return resultMsg;
        }
        int page = (baseUserInfoRequest.getPageIndex() - 1) * baseUserInfoRequest.getPageSize();
        int size = baseUserInfoRequest.getPageSize();
//        List<BaseUserInfo> baseUserInfos = baseUserInfoMapper.selectAllByUsername(baseUserInfoRequest.getUserName(),
//                baseUserInfoRequest.getRealName(), page, size);
        queryCondition.put("pageIndex",page);
        queryCondition.put("pageSize",size);
        List<Map<String, String>> userInfoList = baseUserInfoMapper.selectAllUserId(queryCondition);
        if (CollectionUtils.isEmpty(userInfoList)) {
            resultMsg.setResult(true);
            resultMsg.setTotal(0);
            return resultMsg;
        }
        List<UserRoleRegionOutput> res = queryUserInfoByName(userInfoList);
        resultMsg.setResult(true);
        resultMsg.setData(res);
        resultMsg.setTotal(total);
        return resultMsg;
    }

    /**
     * 查询用户，角色，数据中心信息，并且组装出参
     * @param userInfoList
     * @return
     */
    public List<UserRoleRegionOutput> queryUserInfoByName(List<Map<String, String>> userInfoList) {
        if (CollectionUtils.isEmpty(userInfoList)) {
            return new ArrayList<>();
        }
        List<String> userName = userInfoList.stream().map(user -> user.get("userName")).distinct().collect(Collectors.toList());
        //以后慢,并行
        List<BaseUserInfo> baseUserInfos = baseUserInfoMapper.selectAllByPrimaryKeys(userName);
        List<BaseUserRole> baseUserRoles = baseUserRoleMapper.selectAllByPrimaryKeys(userName);
        List<Map<String, Object>> regionList = baseUserRegionMapper.selectRegionByUserIds(userName);

        List<UserRoleRegionOutput> userRoleRegionOutputs = new ArrayList<>();
        for (String userId : userName) {
            UserRoleRegionOutput userRoleRegionOutput = new UserRoleRegionOutput();
           // userRoleRegionOutput.setUserId(userId);
            if (!CollectionUtils.isEmpty(baseUserInfos)) {
                List<BaseUserInfo> collect = baseUserInfos.stream().filter(vo -> vo.getUserName().equalsIgnoreCase(userId)).collect(Collectors.toList());
                BaseUserInfo baseUserInfo = collect.get(0);
                baseUserInfo.setPassword(null);
                BeanUtils.copyProperties(baseUserInfo, userRoleRegionOutput);
            }
            if (!CollectionUtils.isEmpty(baseUserRoles)) {
                List<BaseUserRole> userRoleList = baseUserRoles.stream().filter(vo -> vo.getUserId().equalsIgnoreCase(userId)).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(userRoleList)) {
                    userRoleRegionOutput.setBaseUserRole(userRoleList);
                }
            }
            if (!CollectionUtils.isEmpty(regionList)) {
                 List<Map<String, Object>> regList = regionList.stream().filter(vo -> vo.get("rId") !=null && !StringUtils.isBlank(vo.get("rId").toString()))
                         .filter(vo -> vo.get("userId").toString().equalsIgnoreCase(userId)).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(regList)) {
                     convertRegion(regList);
                    userRoleRegionOutput.setRegionList(regList);
                }
            }
            userRoleRegionOutputs.add(userRoleRegionOutput);
        }
        return userRoleRegionOutputs;
    }

    private void convertRegion(List<Map<String, Object>> regList) {
        Map<String, String> regionMap = metaDataItemService.getRegionMap();
        for (Map<String, Object> dataItem : regList) {
            JSONObject object = JSONObject.parseObject((String) dataItem.get("data"));
            String regionName = regionMap.get(object.getString(MetaDataConstants.REGION));
            dataItem.put(MetaDataConstants.REGION_NAME,regionName);
            dataItem.put("data",null);
        }
    }


    @Override
    public ResultMsg createUser(BaseUserInfoRequest baseUserInfoRequest) {
        ResultMsg resultMsg = new ResultMsg();
        try {
            // 参数校验
            if (StringUtils.isBlank(baseUserInfoRequest.getUserName()) || StringUtils.isBlank(baseUserInfoRequest.getPassword())) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("账户或密码不能为空.");
                return resultMsg;
            }

            BaseUserInfo alreadyData = baseUserInfoMapper.selectByUsername(baseUserInfoRequest.getUserName());
            if (alreadyData != null) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("账户已存在.");
                return resultMsg;
            }
            getLogger().info("LoginService.createUser start. baseUserInfoRequest: " + JSON.toJSONString(baseUserInfoRequest));

            // 加密密码
            String userId = UUID.randomUUID().toString();
            // String password = MD5Util.md5Hex(MD5Util.md5Hex(baseUserInfoRequest.getUserName() + baseUserInfoRequest.getPassword()));
            BaseUserInfo baseUserInfo = new BaseUserInfo();
            BeanUtils.copyProperties(baseUserInfoRequest, baseUserInfo);
            baseUserInfo.setUserId(userId);
            // baseUserInfoRequest.setPassword(password);
            baseUserInfo.setCreatedTime(new Date());
            baseUserInfo.setCreatedby(userId);
            int successCount = baseUserInfoMapper.insert(baseUserInfo);
            int res2 = 0, res3 = 0;
            if (!StringUtils.isBlank(baseUserInfoRequest.getRoleCode())) {
                BaseUserRole baseUserRole = new BaseUserRole();
                BeanUtils.copyProperties(baseUserInfoRequest, baseUserRole);
                baseUserRole.setUserId(baseUserInfoRequest.getUserName());
                baseUserRoleMapper.insert(baseUserRole);
            }

            if (CollectionUtil.isNotEmpty(baseUserInfoRequest.getRegions())) {
                for (String region : baseUserInfoRequest.getRegions()) {
                    BaseUserRegion baseUserRegion = new BaseUserRegion();
                    baseUserRegion.setUserId(baseUserInfoRequest.getUserName());
                    baseUserRegion.setRegion(region);
                    baseUserRegionMapper.insert(baseUserRegion);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("用户创建失败.");
            return resultMsg;
        }
        resultMsg.setResult(true);
        resultMsg.setMsg("用户创建成功.");
        return resultMsg;
    }

    @Override
    public ResultMsg updatePassword(LoginData loginData) {
        ResultMsg resultMsg = new ResultMsg();
        try {
            // 参数校验
            if (StringUtils.isBlank(loginData.getUserName())) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("账号不能为空.");
                return resultMsg;
            }
            getLogger().info("LoginService.updatePassword start. loginData: " + JSON.toJSONString(loginData));

            BaseUserInfo baseUserInfo = baseUserInfoMapper.selectByUsername(loginData.getUserName());
            if (baseUserInfo == null) {
                resultMsg.setResult(false);
                resultMsg.setErrorMsg("用户不存在.");
                return resultMsg;
            }
            if (!StringUtils.isBlank(loginData.getNewPassword())){
                // 新密码加密
                // String newPassword = MD5Util.md5Hex(MD5Util.md5Hex(loginData.getUserName() + loginData.getNewPassword()));
                baseUserInfo.setPassword(loginData.getNewPassword());
                baseUserInfo.setModifiedTime(new Date());
                int successCount = baseUserInfoMapper.updateByPrimaryKeySelective(baseUserInfo);
            }else {
                baseUserInfo.setModifiedTime(new Date());
                baseUserInfo.setRealName(loginData.getRealName());
                baseUserInfo.setEmNumber(loginData.getEmNumber());
                baseUserInfoMapper.updateByPrimaryKeySelective(baseUserInfo);
                // 修改权限
                BaseUserRole baseUserRole = new BaseUserRole();
                BeanUtils.copyProperties(loginData, baseUserRole);
                baseUserRole.setUserId(loginData.getUserName());
                baseUserRoleMapper.deleteByUserId(loginData.getUserName());
                if(!StringUtils.isBlank(loginData.getRoleCode())){
                    baseUserRoleMapper.insert(baseUserRole);
                }
                // 修改数据中心 ,删除, 新增
                baseUserRegionMapper.deleteUserRegion(loginData.getUserName());
                List<BaseUserRegion> baseUserRegions = new ArrayList<>();
                List<String> regionList = loginData.getRegions();
                if (!CollectionUtil.isEmpty(regionList)) {
                    for (String reg : regionList) {
                        BaseUserRegion baseUserRegion1 = new BaseUserRegion();
                        baseUserRegion1.setUserId(loginData.getUserName());
                        baseUserRegion1.setRegion(reg);
                        baseUserRegions.add(baseUserRegion1);
                    }
                    baseUserRegionMapper.batchInsertRegion(baseUserRegions);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMsg.setResult(false);
            resultMsg.setMsg("修改失败.");
            return resultMsg;
        }
        resultMsg.setResult(true);
        resultMsg.setErrorMsg("修改成功.");
        return resultMsg;
    }

    @Override
    public ResultMsg deleteUser(String username) {
        ResultMsg resultMsg = new ResultMsg();
        BaseUserInfo baseUserInfo = baseUserInfoMapper.selectByUsername(username);
        if (baseUserInfo == null) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("用户不存在.");
            return resultMsg;
        }
        getLogger().info("LoginService.deleteUser start. username: " + username);
        try {
            baseUserInfoMapper.deleteByPrimaryKey(baseUserInfo.getUserId());
            baseUserRegionMapper.deleteUserRegion(username);
            baseUserRoleMapper.deleteByUserId(username);
        } catch (Exception e) {
            getLogger().error("LoginService.deleteUser error. username: ", e);
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("用户删除失败.");
        }
        resultMsg.setResult(true);
        resultMsg.setMsg("用户删除成功.");
        return resultMsg;
    }

    @Override
    public ResultMsg getUserInfoByCookie(HttpServletRequest request, HttpServletResponse response) {
        ResultMsg resultMsg = new ResultMsg();
        String token = null;
        BaseUserInfo baseUserInfo = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                switch (cookie.getName()) {
                    case "sdptoken":
                        token = cookie.getValue();
                    default:
                        break;
                }
            }
        }

        if (StringUtils.isNotBlank(token)) {
            baseUserInfo = getUserInfoByToken(token);
            if (baseUserInfo != null) {
                List<Map<String, String>> mapList = new ArrayList<>();
                Map<String, String> userMap = new HashMap<>();
                userMap.put("userName", baseUserInfo.getUserName());
                mapList.add(userMap);
                List<UserRoleRegionOutput> userRoleRegionOutputs = queryUserInfoByName(mapList);
                resultMsg.setResult(true);
                resultMsg.setData(userRoleRegionOutputs.get(0));
                return resultMsg;
            }
        }

        resultMsg.setResult(false);
        resultMsg.setErrorMsg("无用户信息.");
        return resultMsg;
    }

    /**
     * logout
     *
     * @param request
     * @param response
     */
    @Override
    public ReturnT<String> logout(HttpServletRequest request, HttpServletResponse response){
        //从缓存中取出redis的key
        String token = verifyLogin(request,response);
        if(StringUtils.isNotEmpty(token)){
            redisUtil.del(token);
        }
        CookieUtil.remove(request, response, LOGIN_ADMIN_IDENTITY_KEY);
        //移除redis中数据
        return ReturnT.SUCCESS;
    }

    /**
     * logout
     *
     * @param request
     * @return
     */
    public SysUsers ifLogin(HttpServletRequest request, HttpServletResponse response){
        String cookieToken = CookieUtil.getValue(request, LOGIN_IDENTITY_KEY);
        if (cookieToken != null) {
            SysUsers sysUsers = null;
            try {
                sysUsers = parseToken(cookieToken);
            } catch (Exception e) {
                logout(request, response);
            }
            return sysUsers;
        }
        return null;
    }

    /**
     * logout
     *
     * @param request
     * @return
     */
    public String verifyLogin(HttpServletRequest request, HttpServletResponse response){
        return CookieUtil.getValue(request, LOGIN_ADMIN_IDENTITY_KEY);
    }

    /**
     * 根据token获取当前登录用户信息
     * @param token
     * @return
     */
    @Override
    public BaseUserInfo getUserInfoByToken(String token) {
        BaseUserInfo baseUserInfo = null;
        String baseUserInfoJson = redisLock.getValue(token);
        if (StringUtils.isNotBlank(baseUserInfoJson)) {
            try {
                baseUserInfo = JSON.parseObject(baseUserInfoJson, BaseUserInfo.class);
            } catch (Exception e) {
                getLogger().info("LoginService.getUserInfoByCookie baseUserInfo json conversion error. baseUserInfoJson: " + baseUserInfoJson);
            }
        }
        return baseUserInfo;
    }

    @Override
    public BaseUserInfo getUserInfoByRequest(HttpServletRequest request) {
        BaseUserInfo baseUserInfo = new BaseUserInfo();
        String token = null;
        if (request == null) return baseUserInfo;
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return baseUserInfo;
        for (Cookie cookie : cookies) {
            if ("sdptoken".equalsIgnoreCase(cookie.getName())) {
                token = cookie.getValue();
            }
        }
        BaseUserInfo userInfoByToken = this.getUserInfoByToken(token);
        if (userInfoByToken == null) return baseUserInfo;
        userInfoByToken.setPassword(null);
        return userInfoByToken;
    }

    @Override
    public BaseUserRole getUserRoleByRequest(HttpServletRequest request) {
        BaseUserInfo userInfoByToken = this.getUserInfoByRequest(request);
        //
        String userName = userInfoByToken.getUserName();
        return baseUserRoleMapper.selectRoleByUser(userName);
    }

}
