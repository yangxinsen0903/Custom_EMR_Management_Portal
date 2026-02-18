package com.sunbox.sdpadmin.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.constant.BizConfigConstants;
import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.ConfGroupElasticScalingRule;
import com.sunbox.sdpadmin.core.util.JacksonUtil;
import com.sunbox.sdpadmin.mapper.ConfClusterMapper;
import com.sunbox.sdpadmin.mapper.ConfGroupElasticScalingRuleMapper;
import com.sunbox.sdpadmin.model.shein.response.SheinResponseModel;
import com.sunbox.service.BizConfigService;
import com.sunbox.web.BaseCommonInterFace;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Shein路由切面
 */
@Order(1)
@Aspect
@Component
public class SheinRouterAspect implements BaseCommonInterFace {

    private Boolean isEnableRouter = false;

    private String sdp1url;

    private String clusterNamePrefix;

    //1,根据clusterId或id路由
    private static final List<String> ROUTING_RULE_ID = Arrays.asList(
            "/sheinapi/v1/getRestartTaskResult",
            "/sheinapi/v1/listInstanceGroups",
            "/sheinapi/v1/listClusterGroupInstances",
            "/sheinapi/v1/descCluster",
            "/sheinapi/v1/descClusterReleaseLabel",
            "/sheinapi/v1/saveElasticScalingRule",
            "/sheinapi/v1/terminateElasticScalingRule",
            "/sheinapi/v1/modifyClusterInstanceGroup",
            "/sheinapi/v1/modifyClusterInstanceGroups",
            "/sheinapi/v1/updateClusterConfig",
            "/sheinapi/v1/addInstanceGroup",
            "/sheinapi/v1/addClusterTags",
            "/sheinapi/v1/updateClusterTags",
            "/sheinapi/v1/delClusterTags",
            "/sheinapi/v1/saveAndExecuteScript",
            "/sheinapi/v1/restartClusterService",
            "/sheinapi/v1/spotstatic",
            "/sheinapi/v1/updatescalevmscope",
            "/sheinapi/v1/addesrule",
            "/sheinapi/v1/pendingscaletasks");
    //2,SDP一期和二期数据合并
    private static final List<String> ROUTING_RULE_JOIN = Arrays.asList(
            "/sheinapi/v1/joblist",
            "/sheinapi/v1/listClusters",
            "/sheinapi/v1/listAvailableClusters");

    //3,设置集群名称，匹配此集群名称前缀的都二期接口
    private static final List<String> ROUTING_RULE_MATCHING = Arrays.asList("/sheinapi/v1/createCluster");
    //4,根据EsRuleId查找到ClusterID。根据clusterId作为路由
    private static final List<String> ROUTING_RULE_ESRULEID = Arrays.asList("/sheinapi/v1/updateesrule");
    //5,删除集群 post params
    private static final List<String> ROUTING_RULE_TERMINATECLUSTER = Arrays.asList("/sheinapi/v1/terminateCluster");

    private static final String SDP1 = "SDP-1.0";

    private static final String SDP2 = "SDP-2.0";

    private static final String API_TOKEN = "apitoken";

    @Autowired
    private ConfClusterMapper confClusterMapper;

    @Autowired
    private BizConfigService bizConfigService;

    @Autowired
    private ConfGroupElasticScalingRuleMapper confGroupElasticScalingRuleMapper;

    @Pointcut("execution(* com.sunbox.sdpadmin.controller.SheinApiController.*(..))")
    public void controllerMethods() {
    }

    /**
     * 环绕通知
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("controllerMethods()")
    public Object aroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        this.getParameterFromDb();
        if (!isEnableRouter || StrUtil.isEmpty(sdp1url) || StrUtil.isEmpty(clusterNamePrefix)) {
            getLogger().info("Shein路由未启用或sdp1url,clusterNamePrefix为空!");
            return joinPoint.proceed();
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Assert.notNull(attributes, "ServletRequestAttributes is null");
        HttpServletRequest request = attributes.getRequest();

        // 获取 HTTP 方法和 URI
        String requestMethod = request.getMethod();
        String requestURI = request.getRequestURI();
        String token = this.getToken(request, API_TOKEN);
        //region 删除集群单独处理,因为这个接口post请求params参数,单独处理
        if (ROUTING_RULE_TERMINATECLUSTER.contains(requestURI)) {
            Object result;
            //post请求  params参数
            Map<String, Object> parameterMap = this.getParamsMap(request);
            getLogger().info("ROUTING_RULE_TERMINATECLUSTER,Shein路由,requestURI:{},requestMethod:{},token:{},getParamsMap:{},sdp1url:{}",
                    requestURI,
                    requestMethod,
                    token,
                    parameterMap,
                    sdp1url);
            String clusterReleaseVer = this.routingRuleClusterId(parameterMap);
            if (SDP1.equals(clusterReleaseVer)) {
                //请求sdp1.0 接口
                result = this.httpPostParams(sdp1url + requestURI, token, parameterMap);
            } else {
                result = joinPoint.proceed();
            }
            getLogger().info("ROUTING_RULE_TERMINATECLUSTER,Shein路由返回结果:{},clusterReleaseVer:{}", JSON.toJSONString(result),clusterReleaseVer);
            return result;
        }
        //endregion

        String clusterReleaseVer = SDP2;
        Map<String, Object> parameterMap = this.getParameterMap(joinPoint, request);
        if (ROUTING_RULE_ID.contains(requestURI)) {
            //根据clusterId或id路由
            clusterReleaseVer = this.routingRuleClusterId(parameterMap);
        } else if (ROUTING_RULE_MATCHING.contains(requestURI)) {
            //设置集群名称，匹配此集群名称前缀的都二期接口
            clusterReleaseVer = this.routingRuleClusterNameMatching(parameterMap);
        } else if (ROUTING_RULE_ESRULEID.contains(requestURI)) {
            //根据EsRuleId查找到ClusterID。根据clusterId作为路由
            clusterReleaseVer = this.routingRuleEsRuleId(parameterMap);
        }
        getLogger().info("Shein路由,requestURI:{},requestMethod:{},token:{},parameterMap:{},sdp1url:{},clusterNamePrefix:{}",
                requestURI,
                requestMethod,
                token,
                parameterMap,
                sdp1url,
                clusterNamePrefix);
        //调sdp1.0
        Object result;
        if (clusterReleaseVer.equals(SDP1)) {
            result = this.getSdp1Data(requestURI, requestMethod, parameterMap, token);
            addSDPVersionAttr((SheinResponseModel)result, SDP1);
        } else {
            //放行
            result = joinPoint.proceed();
            addSDPVersionAttr((SheinResponseModel)result, SDP2);
            //SDP一期和二期数据合并
            if (ROUTING_RULE_JOIN.contains(requestURI)) {
                SheinResponseModel sdp1Data = this.getSdp1Data(requestURI, requestMethod, parameterMap, token);
                SheinResponseModel sdp2Data = (SheinResponseModel) result;
                addSDPVersionAttr((SheinResponseModel)sdp1Data, SDP1);
                //数据合并
                result = this.mergingData(sdp1Data, sdp2Data);
            }
        }
        getLogger().info("Shein路由返回结果:{},clusterReleaseVer:{}", JSON.toJSONString(result),clusterReleaseVer);
        return result;
    }

    /**
     * 根据clusterId或id判断是否走SDP1.0
     *
     * @param parameterMap
     */
    private String routingRuleClusterId(Map<String, Object> parameterMap) {
        String clusterReleaseVer = SDP2;
        //根据clusterId或id路由
        String clusterId = (String) parameterMap.get("clusterId");
        if (StrUtil.isEmpty(clusterId)) {
            clusterId = (String) parameterMap.get("id");
        }
        if (StrUtil.isEmpty(clusterId)) {
            clusterId = (String) parameterMap.get("srcClusterId");
        }
        //获取集群信息
        if (StrUtil.isNotEmpty(clusterId)) {
            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
            if (confCluster == null) {
                clusterReleaseVer = SDP1;
            }
        }
        return clusterReleaseVer;
    }

    /**
     * 根据clusterName前缀判断是否走SDP1.0
     *
     * @param parameterMap
     */
    private String routingRuleClusterNameMatching(Map<String, Object> parameterMap) {
        String clusterReleaseVer = SDP2;
        //设置集群名称，匹配此集群名称前缀的都二期接口
        String clusterName = (String) parameterMap.get("clusterName");
        if (StrUtil.isNotEmpty(clusterName)) {
            String[] clusterNamePrefixArray = clusterNamePrefix.split(",");
            long count = Arrays.stream(clusterNamePrefixArray).filter(clusterName::startsWith).count();
            if (count == 0) {
                clusterReleaseVer = SDP1;
            }
        }
        return clusterReleaseVer;
    }

    /**
     * 根据esRuleId判断是否走SDP1.0
     *
     * @param parameterMap
     * @return
     */
    private String routingRuleEsRuleId(Map<String, Object> parameterMap) {
        String clusterReleaseVer = SDP2;
        //根据EsRuleId查找到ClusterID。根据clusterId作为路由
        String esRuleId = (String) parameterMap.get("esRuleId");
        if (StrUtil.isNotEmpty(esRuleId)) {
            ConfGroupElasticScalingRule confGroupElasticScalingRule = confGroupElasticScalingRuleMapper.selectByPrimaryKey(esRuleId);
            if (confGroupElasticScalingRule == null) {
                clusterReleaseVer = SDP1;
            } else {
                //获取集群信息
                ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(confGroupElasticScalingRule.getClusterId());
                if (confCluster == null) {
                    clusterReleaseVer = SDP1;
                }
            }
        }
        return clusterReleaseVer;
    }

    /**
     * SDP一期和二期数据合并
     *
     * @return SheinResponseModel
     */
    private SheinResponseModel mergingData(SheinResponseModel sdp1Data, SheinResponseModel sdp2Data) {
        SheinResponseModel sheinResponseModel = null;
        getLogger().info("Shein路由,SDP一期和二期数据合并,sdp1Data:{},sdp2Data:{}", sdp1Data.toString(), sdp2Data.toString());
        if (SheinResponseModel.Request_Success.equals(sdp1Data.getCode()) &&
                !SheinResponseModel.Request_Success.equals(sdp2Data.getCode())) {
            sheinResponseModel = sdp1Data;
        }
        if (!SheinResponseModel.Request_Success.equals(sdp1Data.getCode()) &&
                SheinResponseModel.Request_Success.equals(sdp2Data.getCode())) {
            sheinResponseModel = sdp2Data;
        }
        if (SheinResponseModel.Request_Success.equals(sdp1Data.getCode()) &&
                SheinResponseModel.Request_Success.equals(sdp2Data.getCode())) {
            JSONArray sdp2InfoList = JSON.parseArray(JSON.toJSONString(sdp2Data.getInfo()));
            JSONArray sdp1InfoList = JSON.parseArray(JSON.toJSONString(sdp1Data.getInfo()));
            sdp2InfoList.addAll(sdp1InfoList);
            sdp2Data.setInfo(sdp2InfoList);
            sheinResponseModel = sdp2Data;
        }
        return sheinResponseModel;

    }

    private void addSDPVersionAttr(SheinResponseModel responseModel, String version) {
        try {
            if (Objects.isNull(responseModel)) {
                return;
            }

            Object info = responseModel.getInfo();
            if (info instanceof Collection) {
                // info为list的情况, 处理里面的元素
                Collection infoList = (Collection) info;
                if (CollUtil.isEmpty(infoList)) {
                    return;
                }
                // 只处理元素为Map的情况
                Object elementObj = infoList.stream().findFirst().get();
                if (elementObj instanceof Map) {
                    for (Object map : infoList) {
                        Map elementMap = (Map) map;
                        elementMap.put("sdpVersion", version);
                    }
                }
            } else if (info instanceof Map) {
                Map elementMap = (Map) info;
                elementMap.put("sdpVersion", version);
            }
        } catch (Exception ex) {
            getLogger().error(ex.getMessage(), ex);
        }
    }

    /**
     * 通过http获取sdp1.0数据
     *
     * @param requestURI
     * @param requestMethod
     * @param parameterMap
     * @return
     */
    private SheinResponseModel getSdp1Data(String requestURI, String requestMethod, Map<String, Object> parameterMap, String token) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        //请求sdp1.0 接口
        String url = sdp1url + requestURI;
        if (HttpMethod.GET.name().equals(requestMethod)) {
            sheinResponseModel = this.httpGet(url, token, parameterMap);
        }
        if (HttpMethod.POST.name().equals(requestMethod)) {
            sheinResponseModel = this.httpPost(url, token, parameterMap);
        }

        return sheinResponseModel;
    }

    /**
     * 通过http获取sdp1.0数据,get请求,params参数
     *
     * @param url
     * @param token
     * @param parameterMap
     * @return
     */
    private SheinResponseModel httpGet(String url, String token, Map<String, Object> parameterMap) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        try {
            HttpResponse httpResponse = HttpUtil.createGet(url)
                    .header(API_TOKEN, token)
                    .form(parameterMap)
                    .execute();
            sheinResponseModel = this.handleResponse(httpResponse);
        } catch (Exception e) {
            getLogger().error("httpGet通过http获取sdp1.0数据异常:", e);
            sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
            sheinResponseModel.setMsg("通过http获取sdp1.0数据异常!");
        }
        return sheinResponseModel;
    }

    /**
     * 通过http获取sdp1.0数据,post请求,body参数
     *
     * @param url
     * @param token
     * @param body
     * @return
     */
    private SheinResponseModel httpPost(String url, String token, Map<String, Object> body) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        try {
            HttpResponse httpResponse = HttpUtil.createPost(url)
                    .header(API_TOKEN, token)
                    .body(JSON.toJSONString(body))
                    .execute();
            sheinResponseModel = this.handleResponse(httpResponse);
        } catch (Exception e) {
            getLogger().error("httpPost通过http获取sdp1.0数据异常:", e);
            sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
            sheinResponseModel.setMsg("通过http获取sdp1.0数据异常!");
        }
        return sheinResponseModel;
    }

    /**
     * 通过http获取sdp1.0数据,post请求,Params参数
     *
     * @param url
     * @param token
     * @param params
     * @return
     */
    private SheinResponseModel httpPostParams(String url, String token, Map<String, Object> params) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        try {
            HttpResponse httpResponse = HttpUtil.createPost(url)
                    .header(API_TOKEN, token)
                    .form(params)
                    .execute();
            sheinResponseModel = this.handleResponse(httpResponse);
        } catch (Exception e) {
            getLogger().error("httpPostParams通过http获取sdp1.0数据异常:", e);
            sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
            sheinResponseModel.setMsg("通过http获取sdp1.0数据异常!");
        }
        return sheinResponseModel;
    }

    /**
     * 处理http请求的返回数据
     *
     * @param httpResponse
     * @return
     */
    private SheinResponseModel handleResponse(HttpResponse httpResponse) {
        SheinResponseModel sheinResponseModel = new SheinResponseModel();
        if (httpResponse != null && httpResponse.getStatus() == 200 && StrUtil.isNotEmpty(httpResponse.body())) {
            sheinResponseModel = JSON.parseObject(httpResponse.body(), SheinResponseModel.class);
        } else {
            sheinResponseModel.setCode(SheinResponseModel.Request_Failed);
            sheinResponseModel.setMsg("Shein路由,sdp1.0接口返回信息为空");
        }
        return sheinResponseModel;
    }


    /**
     * 根据请求方式获取参数
     *
     * @return
     */
    private Map<String, Object> getParameterMap(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        String requestMethod = request.getMethod();
        Map<String, Object> parameterMap = new HashMap<>();
        if (HttpMethod.POST.name().equals(requestMethod)) {
            //json参数
            Object[] paramValues = joinPoint.getArgs();
            Object paramValue = paramValues[0];
            if (paramValue instanceof String) {
                parameterMap = JSON.parseObject((String) paramValues[0]);
            } else {
                parameterMap = JacksonUtil.readValue(JacksonUtil.writeValueAsString(paramValues[0]),Map.class);
            }
        } else if (HttpMethod.GET.name().equals(requestMethod)) {
            //params参数 从request中获取,防止sdp1.0和sdp2.0字段数量不一致
            parameterMap = this.getParamsMap(request);
        }
        return parameterMap;
    }
    /**
     * 从request中获取参数params
     *
     * @param request
     * @return
     */
    private Map<String, Object> getParamsMap(HttpServletRequest request) {
        Map<String, Object> parameterMap = new HashMap<>();
        //params参数 从request中获取,防止sdp1.0和sdp2.0参数不一致
        Map<String, String> headerMap = ServletUtil.getParamMap(request);
        Set<String> strings = headerMap.keySet();
        for (String key : strings) {
            parameterMap.put(key, headerMap.get(key));
        }
        return parameterMap;
    }


    /**
     * 从数据库biz_config获取数据
     */
    private void getParameterFromDb() {
        isEnableRouter = bizConfigService.getConfigValue(
                BizConfigConstants.SDP_ROUTER,
                BizConfigConstants.SDP_ROUTER_IS_ENABLE_ROUTER,
                Boolean.class);
        sdp1url = bizConfigService.getConfigValue(
                BizConfigConstants.SDP_ROUTER,
                BizConfigConstants.SDP_ROUTER_SDP1URL,
                String.class);
        clusterNamePrefix = bizConfigService.getConfigValue(
                BizConfigConstants.SDP_ROUTER,
                BizConfigConstants.SDP_ROUTER_CLUSTER_NAME_PREFIX,
                String.class);
    }

    /**
     * 获取指定名称的值,先从header中获取,再从cookie中获取
     *
     * @param httpServletRequest
     * @return
     */
    private String getToken(HttpServletRequest httpServletRequest, String name) {
        String token = httpServletRequest.getHeader(name);
        if (StrUtil.isEmpty(token)) {
            token = this.getTokenByCookie(httpServletRequest, name);
        }
        return token;
    }

    /**
     * 从cookie中获取指定名称的值
     *
     * @param httpServletRequest
     * @param cookieName
     * @return
     */
    private String getTokenByCookie(HttpServletRequest httpServletRequest, String cookieName) {
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies != null && StrUtil.isNotEmpty(cookieName)) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
