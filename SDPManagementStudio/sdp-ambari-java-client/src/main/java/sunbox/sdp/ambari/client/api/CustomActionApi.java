package sunbox.sdp.ambari.client.api;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Call;
import com.sunbox.util.RandomUtil;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sunbox.sdp.ambari.client.*;
import sunbox.sdp.ambari.client.auth.HttpBasicAuth;
import sunbox.sdp.ambari.client.model.RootServiceResponseWrapperPack;
import sunbox.sdp.ambari.client.model.createclusterprocess.CreateClusterProcessStageWrapper;
import sunbox.sdp.ambari.client.model.createclusterprocess.CreateClusterProcessTask;
import sunbox.sdp.ambari.client.model.createclusterprocess.CreateClusterProcessTaskWrapper;
import sunbox.sdp.ambari.client.model.customaction.*;
import sunbox.sdp.ambari.client.model.customaction.enums.ConfigGroupField;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对自定义的行为操作进行封装
 * @author: wangda
 * @date: 2023/1/2
 */
public class CustomActionApi {

    private Logger logger = LoggerFactory.getLogger(CustomActionApi.class);

    private ApiClient apiClient;

    public CustomActionApi() {
        this(Configuration.getDefaultApiClient());
    }

    public CustomActionApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }


    /**
     * 设置集群自动重启
     * @param clusterName Ambari的集群名
     */
    public void autoStartSetting(String clusterName, boolean enableAutoStart) {
        String url = "/clusters/" + clusterName;

        try {
            AutoStartRequest request = new AutoStartRequest(enableAutoStart);
            Call call = buildCall("PUT", url, request);
            apiClient.execute(call);
        } catch (Exception ex) {
            throw new RuntimeException("调用Ambari设置集群服务自动重启失败：" + ex.getMessage(), ex);
        }
    }
    /**
     * 组件自动重启
     * @param clusterName 集群名称
     * @param request 自动重启的参数
     */
    public void componentAutoStart(String clusterName, ComponentAutoStartRequest request) {
        try {
            String url = buildEnableAutoStartUrl(clusterName);
            Call call = buildCall("PUT", url, request);
//        Type localVarReturnType = new TypeToken<CreateClusterResponseWrapper>(){}.getType();
//        apiClient.execute(call, localVarReturnType);
            apiClient.execute(call);
        } catch (Exception ex) {
            throw new RuntimeException("调用Ambari设置组件自动重启失败：" + ex.getMessage(), ex);
        }
    }

    private String buildEnableAutoStartUrl(String clusterName) {
        StringBuilder sb = new StringBuilder();
        sb.append("/clusters/").append(clusterName).append("/components?");
        return sb.toString();
    }

    /**
     * 关闭一个集群的全部服务<br/>
     * 可重复执行
     * @param clusterName 集群名称
     * @return 如果已经执行过，没有需要关闭的服务时，返回null
     */
    public InProgressResponse stopAllServiceOfCluster(String clusterName) {
        // URL
        String url = "/clusters/" + clusterName + "/services?";

        // 参数
        StopAllServiceRequest request = StopAllServiceRequest.of(clusterName);

        // 调用Ambari
        Gson gson = new Gson();
        try {
            // 请求Ambari
            Call call = buildCall("PUT", url, request);

            Type localVarReturnType = new TypeToken<InProgressResponse>(){}.getType();
            ApiResponse<InProgressResponse> apiResp = apiClient.execute(call, localVarReturnType);
            return apiResp.getData();
        } catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            logger.error("请求Ambari 关闭集群全部服务失败，集群名：{}, 请求参数：{}" ,clusterName, gson.toJson(request));
            throw new RuntimeException("请求Ambari 关闭集群全部服务失败，集群名：" + clusterName, ex);
        }
    }


    /**
     * 添加向集群添加主机。可以得复调用此接口
     * @param clusterName 集群名
     * @param hostNames 主机名称列表
     */
    public void addHost(String clusterName, List<String> hostNames) {
        // 拼接请求URL
        String url = "/clusters/" + clusterName + "/hosts";

        // 生成请求对象
        List<Map<String, Hosts>> requestBody = new ArrayList<>();
        for (String hostName : hostNames) {
            Map<String, Hosts> hostMap = new HashMap<>();
            hostMap.put("Hosts", Hosts.of(hostName));
            requestBody.add(hostMap);
        }

        addHostIgnoreConflict(clusterName, requestBody);
    }

    private void addHostIgnoreConflict(String clusterName, List<Map<String, Hosts>> requestBody) {
        if (CollectionUtil.isEmpty(requestBody)) {
            logger.info("请求的主机列表为空，不需要添加主机. 集群名：{}" ,clusterName);
            return;
        }

        // 拼接请求URL
        String url = "/clusters/" + clusterName + "/hosts";
        Gson gson = new Gson();
        try {
            // 请求Ambari
            Call call = buildCall("POST", url, requestBody);
            apiClient.execute(call);
        } catch (ApiException ex) {
            // 1. 主机已经存在, 则去掉这些已存在的主机, 重新安装一次..
            if (isConflict(ex)) {
                // 获取到已经存在的主机
                List<String> conflictHost = extractConflictHostName(ex.getResponseBody());
                logger.error("请求Ambari给集群清加主机时,主机已经存在, 去掉该主机后重新增加一次. 集群名：{}, 主机名：{}" ,clusterName, conflictHost);
                // 从主机列表中删除已经存在的主机
                requestBody.removeIf(hostMap -> conflictHost.contains(hostMap.get("Hosts").getHostName()));

                // 递归重新调用此接口
                addHostIgnoreConflict(clusterName, requestBody);
            } else {
                // 2.
                String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
                logger.error(fullMsg, ex);
                throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
            }
        } catch (Exception ex) {
            logger.error("请求Ambari给集群清加主机失败，集群名：{}, 请求参数：{}" ,clusterName, gson.toJson(requestBody));
            throw new RuntimeException("请求Ambari给集群清加主机失败，集群名：" + clusterName, ex);
        }
    }

    private boolean isConflict(ApiException ex) {
        if (Objects.isNull(ex)) {
            return false;
        }

        return ex.getCode() == 409 && StrUtil.contains(ex.getResponseBody(), "already exists");
    }

    private List<String> extractConflictHostName(String responseBody) {
        JSONObject jsonObject = JSONObject.parseObject(responseBody);
        if (Objects.isNull(jsonObject)) {
            return Collections.emptyList();
        }
        String message = jsonObject.getString("message");
        if (StrUtil.isBlank(message)) {
            return Collections.emptyList();
        }

        String keyword = "hostName=";
        Integer index = message.indexOf(keyword);
        if (index < 0) {
            return Collections.emptyList();
        }

        String hostStr = message.substring(keyword.length() + index);
        String[] hostArr = hostStr.split(",");
        return Arrays.asList(hostArr);
    }

    /**
     * 在主机上面配置组件
     * @param clusterName 集群名
     * @param hostNames 主机名列表
     * @param components 组件列表
     */
    public void configHostComponent(String clusterName, List<String> hostNames, List<String> components) {
        // 拼URL
        String url = "/clusters/" + clusterName + "/hosts";
        // 生成请求对象
        ConfigHostComponentRequest request = ConfigHostComponentRequest.of(clusterName, hostNames, components);

        // 调用Ambari
        try {
            Call call = buildCall("POST", url, request);
            apiClient.execute(call);
        }  catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            Gson gson = new Gson();
            logger.error("请求Ambari给集群中主机配置组件失败，集群名：{}, 请求参数：{}" ,clusterName, gson.toJson(request));
            throw new RuntimeException("请求Ambari给集群中主机配置组件失败，集群名：" + clusterName, ex);
        }
    }

    /**
     * 在主机上安装组件, 安装的组件是 <code>configHostComponent</code> 接口配置的组件
     * @param clusterName 集群名
     * @param hostNames 主机名
     * @return 请求号, 用于查询安装进度
     */
    public InProgressResponse installHostComponent(String clusterName, List<String> hostNames) {
        // 拼URL
        String url = "/clusters/" + clusterName + "/host_components";

        // 生成请求对象
        InstallHostComponentRequest request = InstallHostComponentRequest.of(clusterName, hostNames);

        // 调用Ambari
        try {
            Call call = buildCall("PUT", url, request);
            Type localVarReturnType = new TypeToken<InProgressResponse>(){}.getType();
            ApiResponse<InProgressResponse> apiResp = apiClient.execute(call, localVarReturnType);
            return apiResp.getData();
        }  catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            Gson gson = new Gson();
            logger.error("请求Ambari给集群中主机安装组件失败，集群名：{}, 请求参数：{}" ,clusterName, gson.toJson(request));
            throw new RuntimeException("请求Ambari给集群中主机安装组件失败，集群名：" + clusterName, ex);
        }
    }

    public InProgressResponse startHostComponent(String clusterName, List<String> hosts, List<String> components) {
        // 拼URL
        String url = "/clusters/"+clusterName+"/host_components";

        // 生成请求对象
        StartHostComponentRequest request = StartHostComponentRequest.of(clusterName, hosts, components);

        // 调用Ambari
        try {
            Call call = buildCall("PUT", url, request);
            Type localVarReturnType = new TypeToken<InProgressResponse>(){}.getType();
            ApiResponse<InProgressResponse> apiResp = apiClient.execute(call, localVarReturnType);
            InProgressResponse inProgressResponse = apiResp.getData();
            if (Objects.isNull(inProgressResponse) && apiResp.getStatusCode()/100 != 2 ) {
                throw new RuntimeException("请求Ambari启动集群中主机上的组件,返回内容为空. statusCode=" + apiResp.getStatusCode());
            }

            if (Objects.isNull(inProgressResponse) && apiResp.getStatusCode()/100 == 2 ) {
                inProgressResponse = new InProgressResponse();
                inProgressResponse.setTaskCompleted(true);
            }
            return inProgressResponse;
        }  catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            Gson gson = new Gson();
            logger.error("请求Ambari启动集群中主机上的组件失败，集群名：{}, 请求参数：{}" ,clusterName, gson.toJson(request));
            throw new RuntimeException("请求Ambari启动集群中主机上的组件失败，集群名：" + clusterName, ex);
        }
    }

    /**
     * Decommission一个组件
     * @param clusterName 集群名
     * @param hosts 组机列表
     * @param componentName 组件名， 只支持 NODEMANAGER 和 DATANODE。其它组件不需要Decommission
     * @return
     */
    public InProgressResponse decommissionComponent(String clusterName, List<String> hosts, String componentName) {
        // 拼URL
        String url = "/clusters/" + clusterName + "/requests";

        // 生成请求对象
        DecommissionHostComponentRequest request = DecommissionHostComponentRequest.of(clusterName, hosts, componentName);

        // 调用Ambari
        try {
            Call call = buildCall("POST", url, request);
            Type localVarReturnType = new TypeToken<InProgressResponse>(){}.getType();
            ApiResponse<InProgressResponse> apiResp = apiClient.execute(call, localVarReturnType);
            return apiResp.getData();
        }  catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            Gson gson = new Gson();
            logger.error("请求Ambari Decommission主机失败，集群名：{}, 请求参数：{}" ,clusterName, gson.toJson(request));
            throw new RuntimeException("请求Ambari Decommission主机失败，集群名：" + clusterName, ex);
        }
    }

    /**
     * Decommission一个组件
     * @param clusterName 集群名
     * @param hostName 节点hostName
     * @param componentName 组件名， 只支持 NODEMANAGER 和 DATANODE。其它组件不需要Decommission
     * @return
     */
    public InProgressResponse decommissionComponent(String clusterName, String hostName, String componentName) {
        // 拼URL
        String url = "/clusters/" + clusterName + "/requests";

        // 生成请求对象
        DecommissionHostComponentRequest request = DecommissionHostComponentRequest.of(clusterName, hostName, componentName);

        // 调用Ambari
        try {
            Call call = buildCall("POST", url, request);
            Type localVarReturnType = new TypeToken<InProgressResponse>(){}.getType();
            ApiResponse<InProgressResponse> apiResp = apiClient.execute(call, localVarReturnType);
            return apiResp.getData();
        }  catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            Gson gson = new Gson();
            logger.error("请求Ambari Decommission主机失败，集群名：{}, 请求参数：{}" ,clusterName, gson.toJson(request));
            throw new RuntimeException("请求Ambari Decommission主机失败，集群名：" + clusterName, ex);
        }
    }


    public InProgressResponse decommissionRegionServer(String clusterName, List<String> hosts) {
        // 拼URL
        String url = "/clusters/" + clusterName + "/request_schedules";

        // 生成请求对象
        DecommissionRegionServerRequest request = DecommissionRegionServerRequest.of(clusterName, hosts);
        // 调用Ambari
        try {
            Call call = buildCall("POST", url, request.toRequestMap());
            Type localVarReturnType = new TypeToken<InProgressResponse>(){}.getType();
            ApiResponse<InProgressResponse> apiResp = apiClient.execute(call, localVarReturnType);
            return apiResp.getData();
        }  catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            Gson gson = new Gson();
            logger.error("请求Ambari Decommission RegionServer失败，集群名：{}, 请求参数：{}" ,clusterName, gson.toJson(request));
            throw new RuntimeException("请求Ambari RegionServer失败，集群名：" + clusterName, ex);
        }
    }

    public RegionServerDecommissionProgress queryRegionServerDecommissionProgress(String clusterName, Integer requestId) {
        String url = "/clusters/" + clusterName + "/request_schedules/" + requestId;

        // 调用Ambari
        try {
            Call call = buildCall("GET", url, null);
            Type localVarReturnType = new TypeToken<RegionServerDecommissionProgress>(){}.getType();
            ApiResponse<RegionServerDecommissionProgress> apiResp = apiClient.execute(call, localVarReturnType);
            return apiResp.getData();
        }  catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            Gson gson = new Gson();
            logger.error("请求Ambari 查询 RegionServer Decommission进度失败，集群名：{}" ,clusterName);
            throw new RuntimeException("请求Ambari 查询 RegionServer Decommission进度失败，集群名：" + clusterName, ex);
        }
    }


    /**
     * 批量关闭一批主机上的组件
     * @param clusterName 集群名
     * @param hosts 主机列表
     * @param components 组件列表
     * @return 查询关闭进展的响应对象
     */
    public InProgressResponse stopHostComponent(String clusterName, List<String> hosts, List<String> components) {
        // 关闭服务
        // 拼URL
        String url = "/clusters/"+clusterName+"/host_components?";

        // 生成请求对象
        StopAllHostComponentRequest request = StopAllHostComponentRequest.of(clusterName, hosts, components);
        // 调用Ambari
        try {
            Call call = buildCall("PUT", url, request);
            Type localVarReturnType = new TypeToken<InProgressResponse>(){}.getType();
            ApiResponse<InProgressResponse> apiResp = apiClient.execute(call, localVarReturnType);
            return apiResp.getData();
        }  catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            Gson gson = new Gson();
            logger.error("请求Ambari关闭集群中主机上的组件失败，集群名：{}, 请求参数：{}" ,clusterName, gson.toJson(request));
            throw new RuntimeException("请求Ambari关闭集群中主机上的组件失败，集群名：" + clusterName, ex);
        }
    }

    /**
     * 关闭一个主机上的一个组件
     * @param clusterName 集群名
     * @param host 主机名
     * @param component 组件名称
     * @return 查询关闭进展的响应对象
     */
    public InProgressResponse stopHostComponent(String clusterName, String serviceName, String host, String component) {
        // 关闭服务
        // 拼URL
        String url = "/clusters/"+clusterName+"/hosts/" + host + "/host_components/" + component + "?";

        // 生成请求对象
        StopAllHostComponentRequest request = StopAllHostComponentRequest.of(clusterName, serviceName, host, component);
        // 调用Ambari
        try {
            Call call = buildCall("PUT", url, request);
            Type localVarReturnType = new TypeToken<InProgressResponse>(){}.getType();
            ApiResponse<InProgressResponse> apiResp = apiClient.execute(call, localVarReturnType);
            InProgressResponse respData = apiResp.getData();
            if (Objects.isNull(respData)) {
                logger.info("请求Ambari关闭集群中一台主机上的一个组件时，主件已经关闭。集群：" + clusterName + ", 服务：" + serviceName + ", 主机：" + host + ", 组件：" + component);
                respData = new InProgressResponse();
                respData.setTaskCompleted(true);
            }
            return respData;
        }  catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);

            // 如果没找到主机或没找到组件，直接返回成功
            // ServiceComponentHost not found
            // Host not found
            if (ex.getCode() == 404 && StrUtil.containsAny(ex.getResponseBody(), "ServiceComponentHost not found", "Host not found")) {
                logger.info("请求Ambari关闭集群中一台主机上的一个组件时，主机或组件不存在，认为关闭成功：" + clusterName + ", 服务：" + serviceName + ", 主机：" + host + ", 组件：" + component);
                InProgressResponse respData = new InProgressResponse();
                respData.setTaskCompleted(true);
                return respData;
            }
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            String errmsg = "请求Ambari关闭集群中一台主机上的一个组件失败，集群：" + clusterName + ", 服务：" + serviceName + ", 主机：" + host + ", 组件：" + component + "; ";
            logger.error(errmsg, ex);
            throw new RuntimeException(errmsg + ex.getMessage(), ex);
        }
    }

    /**
     * 批量删除主机
     * @param clusterName 集群名
     * @param hosts 主机列表
     * @return 删除主机的响应, 注：需要做判空检查
     */
    public DeleteHostsResponse deleteHosts(String clusterName, List<String> hosts) {
        // 删除主机
        // 拼URL
        String url = "/clusters/" + clusterName + "/hosts";

        // 生成请求对象
        DeleteHostsRequest request = DeleteHostsRequest.of(hosts);

        // 调用Ambari
        try {
            Call call = buildCall("DELETE", url, request);
            Type localVarReturnType = new TypeToken<DeleteHostsResponse>(){}.getType();
            ApiResponse<DeleteHostsResponse> apiResp = apiClient.execute(call, localVarReturnType);
            return apiResp.getData();
        }  catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            Gson gson = new Gson();
            logger.error("请求Ambari批量删除主机失败，集群名：{}, 请求参数：{}" ,clusterName, gson.toJson(request));
            throw new RuntimeException("请求Ambari批量删除主机失败，集群名：" + clusterName, ex);
        }
    }

    /**
     * 删除一个主机，如果主机不存在，也会返回删除成功。
     * @param clusterName 集群名
     * @param host 主机
     * @return 删除主机的响应, 注：需要做判空检查
     */
    public DeleteHostsResponse deleteHost(String clusterName, String host) {
        // 删除主机
        if (StrUtil.isEmpty(clusterName) || StrUtil.isEmpty(host)) {
            throw new IllegalArgumentException("调用Ambari删除一个主机，参数不能为空。clusterName:" + clusterName + ",host:"+host);
        }

        // 拼URL
        String url = "/clusters/" + clusterName + "/hosts/" + host;

        // 调用Ambari
        try {
            Call call = buildCall("DELETE", url, null);
            Type localVarReturnType = new TypeToken<DeleteHostsResponse>(){}.getType();
            ApiResponse<DeleteHostsResponse> apiResp = apiClient.execute(call, localVarReturnType);
            DeleteHostsResponse respData = apiResp.getData();
            if (Objects.isNull(respData)) {
                // 如果没抛异常，但是返回值为空，也认为是成功的
                respData = new DeleteHostsResponse();
                respData.setDeleteResult(new ArrayList<>());
            }
            return respData;
        }  catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            Gson gson = new Gson();
            logger.error("请求Ambari删除单个主机失败，集群名：{}, 主机名称：{}" ,clusterName, host);
            throw new RuntimeException("请求Ambari删除单个主机失败，集群名：" + clusterName + ", 主机名称：" + host, ex);
        }
    }

    /**
     * 删除一个主机，如果主机不存在，也会返回删除成功。
     * @param host 主机
     * @return 只要不抛异常, 就认为是删除成功
     */
    public void deleteHost(String host) {
        // 删除主机
        if (StrUtil.isEmpty(host)) {
            throw new IllegalArgumentException("调用Ambari删除一个主机，参数不能为空。host:"+host);
        }

        // 拼URL /hosts/{hostName}
        String url = "/hosts/" + host;

        // 调用Ambari
        try {
            Call call = buildCall("DELETE", url, null);
            Type localVarReturnType = new TypeToken<Void>(){}.getType();
            apiClient.execute(call, localVarReturnType);
        }  catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            Gson gson = new Gson();
            logger.error("请求Ambari删除单个主机失败，主机名称：{}", host);
            throw new RuntimeException("请求Ambari删除单个主机失败，主机名称：" + host, ex);
        }
    }

    public QueryComponentInHostsResponse queryComponentInHosts(String clusterName, List<String> components) {
        // URL
        String url = "/clusters/" + clusterName + "/components/?";

        // 参数比较复杂，需要拼接一下参数
        QueryComonentInHostsRequest request = QueryComonentInHostsRequest.of(components);

        // Header参数
        HttpBasicAuth basicAuth = (HttpBasicAuth)apiClient.getAuthentications().get("httpBasicAuth");
        String authStr =  basicAuth.getUsername() + ":" + basicAuth.getPassword();
        authStr = Base64.getEncoder().encodeToString(authStr.getBytes());

        Map<String, String> header = new HashMap<>();
        //设置请求格式
        header.put("Content-type", "application/json");
        header.put("X-Requested-By", "ambari");
        //设置编码语言
        header.put("Accept-Charset", "UTF-8");
        //设置Http Basic认证
        header.put("Authorization","Basic " + authStr);

        // 调用Ambari
        Gson gson = new Gson();
        String fullUrl = apiClient.getBasePath() + url + request.generateQueryString();

        try {
            URL ambariServer = new URL(fullUrl);
            HttpURLConnection conn = (HttpURLConnection)ambariServer.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(apiClient.getConnectTimeout());
            conn.setReadTimeout(apiClient.getReadTimeout());

            header.entrySet().forEach(entry -> {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            });
            // body里携带内容
            conn.setDoOutput(false);
            conn.setDoInput(true);

            StringBuilder result = new StringBuilder();
            int code = conn.getResponseCode();
            if(code == 200){
                String response = readContentFromStream(conn.getInputStream());
                if (apiClient.isDebugging()) {
                    logger.info(response);
                }
                QueryComponentInHostsResponse hostResp = gson.fromJson(response, QueryComponentInHostsResponse.class);
                return hostResp;

            } else {
                String error = readContentFromStream(conn.getInputStream());
                throw new RuntimeException("请求Ambari 获取一个组件所布署的主机信息出错, status:" + code + " content:" + error);
            }
        } catch (Exception ex) {
            throw new RuntimeException("请求Ambari 获取一个组件所布署的主机信息出错", ex);
        }
    }

    public QueryServiceInHostsResponse queryServiceInHosts(String clusterName, String serviceName) {
        // URL
        String url = "/clusters/" + clusterName + "/hosts?" +
                    "page_size=1000&from=0&host_components/HostRoles/service_name.matches("+serviceName+")&minimal_response=true&_=" + RandomUtil.getRandomBySize(5);

        // Header参数
        HttpBasicAuth basicAuth = (HttpBasicAuth)apiClient.getAuthentications().get("httpBasicAuth");
        String authStr =  basicAuth.getUsername() + ":" + basicAuth.getPassword();
        authStr = Base64.getEncoder().encodeToString(authStr.getBytes());

        Map<String, String> header = new HashMap<>();
        //设置请求格式
        header.put("Content-type", "application/json");
        header.put("X-Requested-By", "ambari");
        //设置编码语言
        header.put("Accept-Charset", "UTF-8");
        //设置Http Basic认证
        header.put("Authorization","Basic " + authStr);

        // 调用Ambari
        Gson gson = new Gson();
        String fullUrl = apiClient.getBasePath() + url;

        try {
            URL ambariServer = new URL(fullUrl);
            HttpURLConnection conn = (HttpURLConnection)ambariServer.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(apiClient.getConnectTimeout());
            conn.setReadTimeout(apiClient.getReadTimeout());

            header.entrySet().forEach(entry -> {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            });
            // body里携带内容
            conn.setDoOutput(false);
            conn.setDoInput(true);

            StringBuilder result = new StringBuilder();
            int code = conn.getResponseCode();
            if(code == 200){
                String response = readContentFromStream(conn.getInputStream());

                QueryServiceInHostsResponse hostResp = gson.fromJson(response, QueryServiceInHostsResponse.class);
                return hostResp;

            } else {
                String error = readContentFromStream(conn.getInputStream());
                throw new RuntimeException("请求Ambari 获取一个服务所布署的主机信息出错, status:" + code + " content:" + error);
            }
        } catch (Exception ex) {
            throw new RuntimeException("请求Ambari 获取一个服务所布署的主机信息出错", ex);
        }
    }


    private String readContentFromStream(InputStream is) {
        if (Objects.isNull(is)) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        //定义BufferedReader输入流读取响应,getInputStream()会自动建立连接,无需手动调用connect()连接
        try (BufferedReader in = new BufferedReader(new InputStreamReader(is,"UTF-8"))){
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage() + result.toString(), ex);
            throw new RuntimeException("请求Ambari 获取一个组件所布署的主机信息时，读取返回内容出错。" + ex.getMessage(), ex);
        }
        return result.toString();
    }

    /**
     * 查询主机中的组件信息
     * @param clusterName 集群
     * @param hosts 主机列表
     * @return 组件信息
     */
    public QueryHostsComponentResponse queryHostsComponents(String clusterName, List<String> hosts) {
        // 拼URL
        String url = "/clusters/" + clusterName + "/hosts?";

        //
        QueryHostsComponentsRequest request = QueryHostsComponentsRequest.of(hosts);

        // Query查询参数
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("fields", "Hosts/host_name,Hosts/host_state,host_components/HostRoles/state,host_components/HostRoles/maintenance_state," +
                "host_components/HostRoles/stale_configs,host_components/HostRoles/service_name,host_components/HostRoles/display_name," +
                "host_components/HostRoles/desired_admin_state,");
        queryParam.put("sortBy", "Hosts/host_name.asc");
        queryParam.put("minimal_response", "true");
        queryParam.put("_", RandomUtil.getRandomBySize(8));

        // Header参数
        Map<String, String> header = getAmbariCommonHeader();

        // 调用Ambari
        Gson gson = new Gson();
        try {
            HttpResponse httpResponse = HttpUtils.doGet(apiClient.getBasePath(), url, header, queryParam, request);
            String responseBody = EntityUtils.toString(httpResponse.getEntity());
            if (apiClient.isDebugging()) {
                logger.info("queryHostsComponents: request:{}, response:{}", gson.toJson(request), responseBody);
            }
            QueryHostsComponentResponse resp = gson.fromJson(responseBody, QueryHostsComponentResponse.class);
            return resp;
        } catch (Exception ex) {
            logger.error("请求Ambari 查询主机中组件失败，集群名：{}, 请求参数：{}" ,clusterName, gson.toJson(request));
            throw new RuntimeException("请求Ambari,查询主机中组件失败，集群名：" + clusterName, ex);
        }
    }

    /**
     * Rebalance HDFS
     * @param clusterName 集群名称
     * @param nameNodeHost Active的NameNode主机名
     * @param threshold 偏移平均值的阈值，默认为10
     * @return
     */
    public InProgressResponse rebalanceHdfs(String clusterName, String nameNodeHost, Integer threshold) {
        if (Objects.isNull(threshold)) {
            threshold = 10;
        }
        // Url
        String url = "/clusters/" + clusterName + "/requests";
        // 请求对象
        RebalanceRequest request = RebalanceRequest.of(nameNodeHost, threshold);

        // 调用Ambari Api
        Gson gson = new Gson();
        try {
            Call call = buildCall("POST", url, request);
            Type localVarReturnType = new TypeToken<InProgressResponse>(){}.getType();
            ApiResponse<InProgressResponse> apiResp = apiClient.execute(call, localVarReturnType);
            return apiResp.getData();
        } catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            logger.error("请求Ambari Rebalance HDFS出错，集群名：{}, 请求参数：{}" ,clusterName, gson.toJson(request));
            throw new RuntimeException("请求Ambari Rebalance HDFS出错，集群名：" + clusterName, ex);
        }
    }

    /**
     * 重启大数据服务
     * @param clusterName 集群名
     * @param serviceName 服务名
     * @param components 组件列表,包括组件和主机名
     * @return 进展
     */
    public InProgressResponse restartService(String clusterName, String serviceName, List<RequestResourceFilter> components) {
        // 参数检查
        Preconditions.checkNotNull(clusterName, "重启服务时,集群名不能为空");
        Preconditions.checkNotNull(serviceName, "重启服务时,需要重启的服务不能为空");
        Preconditions.checkNotNull(components, "重启服务时,服务下的组件不能为空");
        // 拼URL
        String url = "/clusters/" + clusterName + "/requests";

        // 生成请求对象
        RestartServiceRequest request = RestartServiceRequest.of(clusterName, serviceName, components);

        // 调用接口
        Gson gson = new Gson();
        try {
            Call call = buildCall("POST", url, request);
            Type localVarReturnType = new TypeToken<InProgressResponse>(){}.getType();
            ApiResponse<InProgressResponse> apiResp = apiClient.execute(call, localVarReturnType);
            return apiResp.getData();
        } catch (ApiException ex) {
            logger.error(ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("请求Ambari 重启服务出错，集群名：{}, 服务名:{}, 请求参数：{}" ,clusterName, serviceName, gson.toJson(request));
            throw new RuntimeException("请求Ambari 重启服务出错， "+serviceName+"，集群名：" + clusterName, ex);
        }
    }

    /**
     * 重启指定主机上的大数据组件
     * @param clusterName 集群名
     * @param hosts 主机列表
     * @param components 组件列表,包括组件和主机名
     * @return 进展
     */
    public InProgressResponse restartHostsComponents(String clusterName, String serviceName, List<String> hosts, List<String> components) {
        // 参数检查
        Preconditions.checkNotNull(clusterName, "重启服务时,集群名不能为空");
        Preconditions.checkNotNull(hosts, "重启服务时,需要重启的服务不能为空");
        Preconditions.checkNotNull(components, "重启服务时,服务下的组件不能为空");
        // 拼URL
        String url = "/clusters/"+clusterName+"/requests";

        // 生成请求对象
        RestartComponentsInHostsRequest request = RestartComponentsInHostsRequest.of(clusterName, serviceName, hosts, components);

        // 调用接口
        Gson gson = new Gson();
        try {
            Call call = buildCall("POST", url, request);
            Type localVarReturnType = new TypeToken<InProgressResponse>(){}.getType();
            ApiResponse<InProgressResponse> apiResp = apiClient.execute(call, localVarReturnType);
            return apiResp.getData();
        } catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            logger.error("请求Ambari 重启指定主机的组件时出错，集群名：{}, 服务名:{}, 请求参数：{}" ,clusterName, serviceName, gson.toJson(request));
            throw new RuntimeException("请求Ambari 重启指定主机的组件时出错， "+serviceName+"，集群名：" + clusterName, ex);
        }
    }

    public InProgressResponse restartHostsComponents(String clusterName, String serviceName, Set<String> hostsComponents) {
        // 参数检查
        Preconditions.checkNotNull(clusterName, "重启服务时,集群名不能为空");
        Preconditions.checkNotNull(hostsComponents, "重启服务时,需要重启的服务不能为空");
        // 拼URL
        String url = "/clusters/"+clusterName+"/requests";

        // 生成请求对象
        RestartComponentsInHostsRequest request = RestartComponentsInHostsRequest.of(clusterName, serviceName, hostsComponents);

        // 调用接口
        Gson gson = new Gson();
        try {
            Call call = buildCall("POST", url, request);
            Type localVarReturnType = new TypeToken<InProgressResponse>(){}.getType();
            ApiResponse<InProgressResponse> apiResp = apiClient.execute(call, localVarReturnType);
            return apiResp.getData();
        } catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            logger.error("请求Ambari 重启指定主机的组件时出错，集群名：{}, 服务名:{}, 请求参数：{}" ,clusterName, serviceName, gson.toJson(request));
            throw new RuntimeException("请求Ambari 重启指定主机的组件时出错， "+serviceName+"，集群名：" + clusterName, ex);
        }
    }
    public QueryConfigGroupDetailsResponse queryConfigGroupDetails(String clusterName, ConfigGroupField queryField, String queryValue){
        Objects.requireNonNull(clusterName, "查询配置组详情时，必须指定集群名");
        Objects.requireNonNull(queryValue, "查询配置组详情时，必须指定查询的值");
        Objects.requireNonNull(queryField, "查询配置组详情时，必须指定查询的指段名");
        QueryConfigGroupsResponse queryConfigGroupsResponse = queryConfigGroups(clusterName, queryField, queryValue);
        if (queryConfigGroupsResponse.getItems().isEmpty()){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        HashMap<String, String> groupMap = new HashMap<>();
        //拼接参数
        for (ConfigGroupWrapper item : queryConfigGroupsResponse.getItems()) {
            for (Map<String, Object> desiredConfig : item.getConfigGroup().getDesiredConfigs()) {
                Object type = desiredConfig.get("type");
                Object tag = desiredConfig.get("tag");
                sb.append("(type=").append(type).append("&tag=").append(tag).append(")|");
                groupMap.put(String.join(":",type.toString(),tag.toString()),item.getConfigGroup().getGroupName());
            }
        }
        if (sb.length() <= 0){
            return null;
        }
        QueryConfigGroupDetailsResponse configGroupDetailsResponse = queryConfigGroupDetails(clusterName, sb.deleteCharAt(sb.length() -1 ).toString());
        //添加配置组名称
        for (ConfigGroupDetailWrapper item : configGroupDetailsResponse.getItems()) {
            item.setGroupName(groupMap.get(String.join(":", item.getType(), item.getTag())));
        }
        return configGroupDetailsResponse;
    }
    public QueryConfigGroupDetailsResponse queryConfigGroupDetails(String clusterName,String queryValue){
        Objects.requireNonNull(clusterName, "查询配置组详情时，必须指定集群名");
        StringBuilder url = new StringBuilder().append("/clusters/").append(clusterName).append("/configurations");
        if (queryValue != null){
            url.append("?").append(queryValue).append("&_=").append(System.currentTimeMillis());
        }else {
            url.append("?_=").append(System.currentTimeMillis());
        }
        Map<String, String> ambariCommonHeader = getAmbariCommonHeader();
        try {
            Gson gson = new Gson();
            String response = httpUrlconnectionGet(apiClient.getBasePath() + url, ambariCommonHeader);
            QueryConfigGroupDetailsResponse configGroupDetailsResponse = gson.fromJson(response, QueryConfigGroupDetailsResponse.class);
            return configGroupDetailsResponse;
        }catch (Exception ex) {
            logger.error("请求Ambari 查询配置组详情出错，集群名：{}, 查询值：{}" ,clusterName,  queryValue);
            throw new RuntimeException("请求Ambari 查询配置组详情出错，集群名：" + clusterName + ", 查询值：" + queryValue, ex);
        }
    }
    /**
     * 查询配置组，默认配置组在此查询列表中，只返回自己管理的配置组
     * @param clusterName 集群名称
     * @param queryField 查询字段名，当选择ConfigGroupField.ID时，queryValue不支持多值
     * @param queryValue 查询值，如果传Null或空字符串，或*, 则查询全部
     * @return 配置组响应对象
     */

    public QueryConfigGroupsResponse queryConfigGroups(String clusterName, ConfigGroupField queryField, String queryValue) {
        Objects.requireNonNull(queryField, "查询配置组时，必须指定查询的指段名");
        Objects.requireNonNull(queryValue, "查询配置组时，必须指定查询的值");

        StringBuilder url = new StringBuilder();
        url.append("/clusters/").append(clusterName).append("/config_groups?");
        if (StrUtil.isNotBlank(queryValue) && !Objects.equals("*", queryValue)) {
            url.append("ConfigGroup/").append(queryField.getFieldName()).append(".in(").append(queryValue).append(")&");
        }
        url.append("fields=*&_=").append(RandomUtil.getRandomBySize(5));

        Map<String, String> header = getAmbariCommonHeader();

        // 调用接口
        Gson gson = new Gson();
        try {
            String response = httpUrlconnectionGet(apiClient.getBasePath() + url, header);
            QueryConfigGroupsResponse queryConfigGroupsResponse = gson.fromJson(response, QueryConfigGroupsResponse.class);
            return queryConfigGroupsResponse;
        } catch (Exception ex) {
            logger.error("请求Ambari 查询配置组列表出错，集群名：{}, 查询字段:{}, 查询值：{}" ,clusterName, queryField.getFieldName(), queryValue);
            throw new RuntimeException("请求Ambari 查询配置组列表出错，集群名：" + clusterName + ", 查询字段:"
                    + queryField.getFieldName() + ", 查询值：" + queryValue, ex);
        }

    }

    @NotNull
    private Map<String, String> getAmbariCommonHeader() {
        HttpBasicAuth basicAuth = (HttpBasicAuth)apiClient.getAuthentications().get("httpBasicAuth");
        String authStr =  basicAuth.getUsername() + ":" + basicAuth.getPassword();
        authStr = Base64.getEncoder().encodeToString(authStr.getBytes());

        Map<String, String> header = new HashMap<>();
        //设置请求格式
        header.put("Content-type", "application/json");
        header.put("X-Requested-By", "ambari");
        //设置编码语言
        header.put("Accept-Charset", "UTF-8");
        //设置Http Basic认证
        header.put("Authorization","Basic " + authStr);
        return header;
    }

    /**
     * 创建配置组
     * @param clusterName 集群名
     * @param configGroups 配置组
     * @return 创建结果，需要查询
     */
    public CreateConfigGroupResponse createConfigGroupWithWrapperObj(String clusterName, List<ConfigGroupWrapper> configGroups) {
        Objects.requireNonNull(clusterName, "创建配置组时，必须指定集群名");
        Objects.requireNonNull(configGroups, "创建配置组时，要创建的配置组不能为空");
        Preconditions.checkState(configGroups.size() > 0, "要创建的配置组不能为空");

        // 拼接URL
        String url = "/clusters/" + clusterName + "/config_groups";

        // 调用接口
        Gson gson = new Gson();
        try {
            Call call = buildCall("POST", url, configGroups);
            Type localVarReturnType = new TypeToken<CreateConfigGroupResponse>(){}.getType();
            ApiResponse<CreateConfigGroupResponse> apiResp = apiClient.execute(call, localVarReturnType);
            return apiResp.getData();
        } catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            logger.error("请求Ambari 创建配置组出错，集群名：{}, 请求参数：{}" ,clusterName, gson.toJson(configGroups));
            throw new RuntimeException("请求Ambari 创建配置组出错", ex);
        }

    }

    /**
     * 创建一个配置组<br/>
     * 在一个大数据服务下，一个主机只能在一个配置组中；<br/>
     * 配置组的名称 + Tag是配置组的唯一标识，也就是说一个配置组可以打多个标识。 <br/>
     * 主机名配置不对，或者集群名已经存在， 返回409错误。
     * @param clusterName
     * @param configGroups
     * @return
     */
    public CreateConfigGroupResponse createConfigGroup(String clusterName, List<ConfigGroup> configGroups) {
        Objects.requireNonNull(clusterName, "创建配置组时，必须指定集群名");
        Objects.requireNonNull(configGroups, "创建配置组时，要创建的配置组不能为空");
        Preconditions.checkState(configGroups.size() > 0, "要创建的配置组不能为空");

        List<ConfigGroupWrapper> wrapperList = configGroups.stream().map(ConfigGroupWrapper::of).collect(Collectors.toList());

        return createConfigGroupWithWrapperObj(clusterName, wrapperList);
    }

    /**
     * 删除一个配置组，不报异常即为删除成功
     * @param clusterName 集群名称
     * @param configGroupId 要删除的配置组ID
     */
    public void deleteConfigGroup(String clusterName, Long configGroupId) {
        Objects.requireNonNull(clusterName, "删除配置组时，必须指定集群名称");
        Objects.requireNonNull(configGroupId, "删除配置组时，必须指定要删除的配置组的ID");

        String url = "/clusters/" + clusterName + "/config_groups/" + configGroupId;

        // 调用接口
        Gson gson = new Gson();
        try {
            Call call = buildCall("DELETE", url, null);

            apiClient.execute(call);
        } catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            logger.error("请求Ambari 删除一个配置组出错，集群名：{}, 配置组ID：{}" ,clusterName, configGroupId);
            throw new RuntimeException("请求Ambari 删除一个配置组出错", ex);
        }

    }

    /**
     * 更新配置组中的配置，配置时一定要把配置信息，主机都写上。
     * @param clusterName 集群名
     * @param configGroup 配置组
     * @return
     */
    public UpdateConfigGroupResponse updateConfigGroup(String clusterName, ConfigGroup configGroup) {
        Objects.requireNonNull(clusterName, "修改配置组时，必须指定集群名称");
        Objects.requireNonNull(configGroup, "修改配置组时，配置组对象不能为空");
        Objects.requireNonNull(configGroup.getId(), "修改配置组时，配置组Id不能为空");
        // 组装请求对象
        ConfigGroupWrapper configGroupWrapper = new ConfigGroupWrapper();
        configGroupWrapper.setConfigGroup(configGroup);

        return updateConfigGroup(clusterName,configGroup.getId(), configGroupWrapper);
    }
    public UpdateConfigGroupResponse updateConfigGroup(String clusterName,Long id, ConfigGroupWrapper configGroup) {

        String url = "/clusters/" + clusterName + "/config_groups/" + id;
        // 调用Ambari
        Gson gson = new Gson();
        try {
            Call call = buildCall("PUT", url, Collections.singletonList(configGroup));
            Type localVarReturnType = new TypeToken<UpdateConfigGroupResponse>(){}.getType();
            ApiResponse<UpdateConfigGroupResponse> apiResp = apiClient.execute(call, localVarReturnType);
            return apiResp.getData();
        } catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            logger.error("请求Ambari 修改配置组出错，集群名：{}, 请求参数：{}" ,clusterName, gson.toJson(configGroup));
            throw new RuntimeException("请求Ambari 修改配置组出错", ex);
        }
    }

    /**
     * 更新集群的默认配置。<br/>
     * 配置时会进行全量覆盖，也就是当只设置core-site一个参数时，更新过后ambari上显示core-site只有一个参数了。
     * @param clusterName 集群名称
     * @param configs 配置
     */
    public UpdateClusterDefaultConfigResponse updateClusterDefaultConfig(String clusterName, List<DefaultClusterConfigWrapper> configs) {
        Preconditions.checkNotNull(clusterName, "更新默认集群配置时，集群名不能为空");
        Preconditions.checkNotNull(configs, "更新默认集群配置时，配置项不能为空");

        // 拼URL
        String url = "/clusters/" + clusterName;

        // 请求
        Gson gson = new Gson();
        try {
            Call call = buildCall("PUT", url, configs);
            Type localVarReturnType = new TypeToken<UpdateClusterDefaultConfigResponse>(){}.getType();
            ApiResponse<UpdateClusterDefaultConfigResponse> apiResp = apiClient.execute(call, localVarReturnType);
            return apiResp.getData();
        } catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            logger.error("请求Ambari更新集群默认配置失败，集群名：{}" ,clusterName);
            throw new RuntimeException("请求Ambari更新集群默认配置失败，集群名：" + clusterName, ex);
        }


    }

    /**
     * 查询集群的默认配置
     * @param clusterName 集群名
     * @param services 要查询配置的服务名，如果不填，查询全部
     * @return
     */
    public DefaultClusterConfigResponse queryDefaultClusterConfig(String clusterName, List<String> services) {
        Preconditions.checkNotNull(clusterName, "查询集群默认配置，集群名不能为空");

        if (Objects.isNull(services)) {
            services = new ArrayList<>();
        }

        // URL
        String serviceStr = Strings.join(services, ',');
        StringBuilder url = new StringBuilder();
        url.append("/clusters/").append(clusterName).append("/configurations/service_config_versions?");
        if (StrUtil.isNotEmpty(serviceStr)) {
            url.append("service_name.in(").append(serviceStr).append(")&");
        }
        url.append("is_current=true&fields=*&_=").append(RandomUtil.getRandomBySize(5));

        // 调用接口
        String httpResp = httpUrlconnectionGet(apiClient.getBasePath() + url.toString(), getAmbariCommonHeader());
        if (apiClient.isDebugging()) {
            logger.info(httpResp);
        }

        // 转为响应对象
        Gson gson = new Gson();
        DefaultClusterConfigResponse resp = gson.fromJson(httpResp, DefaultClusterConfigResponse.class);
        return resp;
    }

    public List<String> queryAllHostsName(String clusterName) {
        List<Hosts> hosts;
        if (StringUtils.isNotEmpty(clusterName)){
            hosts = queryAllHosts(clusterName);
        }else{
            hosts = queryAllHosts();
        }
        if (CollectionUtil.isEmpty(hosts)) {
            return Lists.newArrayList();
        }

        List<String> hostNames = new ArrayList<>();
        for (Hosts host : hosts) {
            hostNames.add(host.getHostName());
        }
        return hostNames;
    }

    public List<Hosts> queryAllHosts(){
        String url = "/hosts?fields=Hosts/host_status&_=" + System.currentTimeMillis();

        Gson gson = new Gson();
        try {
            Call call = buildCall("GET", url, null);
            Type localVarReturnType = new TypeToken<RootServiceResponseWrapperPack<QueryHostsComponentItem>>(){}.getType();
            ApiResponse<RootServiceResponseWrapperPack<QueryHostsComponentItem>> apiResp = apiClient.execute(call, localVarReturnType);
            RootServiceResponseWrapperPack<QueryHostsComponentItem>  resp = apiResp.getData();
            List<Hosts> result = new ArrayList<>();
            if (Objects.isNull(resp)) {
                return result;
            } else {
                List<QueryHostsComponentItem> items = resp.getItems();
                if (Objects.isNull(items)) {
                    return result;
                }

                for (QueryHostsComponentItem item : items) {
                    Hosts host = item.getHost();
                    if (Objects.isNull(host)) {
                        continue;
                    }
                    result.add(host);
                }
            }
            return result;
        } catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            logger.error("请求Ambari查询全部主机失败");
            throw new RuntimeException("请求Ambari查询全部主机失败：", ex);
        }
    }

    public List<Hosts> queryAllHosts(String clusterName) {

        String url = "/clusters/" + clusterName + "/hosts?fields=Hosts/host_status&_=" + RandomUtil.getRandomBySize(5);

        Gson gson = new Gson();
        try {
            Call call = buildCall("GET", url, null);
            Type localVarReturnType = new TypeToken<RootServiceResponseWrapperPack<QueryHostsComponentItem>>(){}.getType();
            ApiResponse<RootServiceResponseWrapperPack<QueryHostsComponentItem>> apiResp = apiClient.execute(call, localVarReturnType);
            RootServiceResponseWrapperPack<QueryHostsComponentItem>  resp = apiResp.getData();
            List<Hosts> result = new ArrayList<>();
            if (Objects.isNull(resp)) {
                return result;
            } else {
                List<QueryHostsComponentItem> items = resp.getItems();
                if (Objects.isNull(items)) {
                    return result;
                }

                for (QueryHostsComponentItem item : items) {
                    Hosts host = item.getHost();
                    if (Objects.isNull(host)) {
                        continue;
                    }
                    result.add(host);
                }
            }
            return result;
        } catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            logger.error("请求Ambari查询集群全部主机失败，集群名：{}" ,clusterName);
            throw new RuntimeException("请求Ambari查询集群全部主机失败，集群名：" + clusterName, ex);
        }

    }

    /**
     * 查询一个异步流程中，一个任务的执行状态
     * @param clusterName 集群名
     * @param requestId 请求Id
     * @param taskId 任务Id
     * @return 任务执行状态
     */
    public CreateClusterProcessTask queryTaskProgress(String clusterName, Long requestId, Long taskId) {
        String url = "/clusters/" + clusterName + "/requests/" + requestId + "/tasks/" + taskId;

        try {
            Call call = buildCall("GET", url, null);
            Type localVarReturnType = new TypeToken<CreateClusterProcessTaskWrapper>(){}.getType();
            ApiResponse<CreateClusterProcessTaskWrapper> apiResp = apiClient.execute(call, localVarReturnType);
            CreateClusterProcessTaskWrapper resp = apiResp.getData();
            if (Objects.isNull(resp)) {
                return null;
            }
            return resp.getTasks();
        } catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            logger.error("请求Ambari查询任务执行状态出错，集群名：{}, requestId:{}, taskId:{}" ,
                    clusterName, requestId, taskId);
            throw new RuntimeException("请求Ambari查询任务执行状态出错，集群名：" + clusterName, ex);
        }
    }

    /**
     * 查询一个异步流程中，一个Stage的执行状态
     * @param clusterName 集群名
     * @param requestId 请求ID
     * @param stageId 阶段ID
     * @return Stage的执行状态
     */
    public CreateClusterProcessStageWrapper queryStageProgress(String clusterName, Long requestId, Long stageId) {
        String url = "/clusters/" + clusterName + "/requests/" + requestId + "/stages/" + stageId;

        try {
            Call call = buildCall("GET", url, null);
            Type localVarReturnType = new TypeToken<CreateClusterProcessStageWrapper>(){}.getType();
            ApiResponse<CreateClusterProcessStageWrapper> apiResp = apiClient.execute(call, localVarReturnType);
            CreateClusterProcessStageWrapper resp = apiResp.getData();
            return resp;
        } catch (ApiException ex) {
            String fullMsg = ex.getMessage() + "||" + ex.getCode() + ":" + ex.getResponseBody();
            logger.error(fullMsg, ex);
            throw new ApiException(fullMsg, ex, ex.getCode(), ex.getResponseHeaders(), ex.getResponseBody());
        } catch (Exception ex) {
            logger.error("请求Ambari查询任务执行状态出错，集群名：{}, requestId:{}, stageId:{}" ,
                    clusterName, requestId, stageId);
            throw new RuntimeException("请求Ambari查询任务执行状态出错，集群名：" + clusterName, ex);
        }
    }

    public com.squareup.okhttp.Call buildCall(String httpMethod, String url, Object body) throws ApiException {
        return buildCall(httpMethod, url, null, body);
    }

    public com.squareup.okhttp.Call buildCall(String httpMethod, String url, List<Pair> queryParams, Object body) throws ApiException {

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = queryParams;

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
                "text/plain"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        String[] localVarAuthNames = new String[] {  };
        return apiClient.buildCall(url, httpMethod, localVarQueryParams, localVarCollectionQueryParams, body, localVarHeaderParams, localVarFormParams, localVarAuthNames, null);
    }

    public String httpUrlconnectionGet(String fullUrl, Map<String, String> header) {
        try {
            URL ambariServer = new URL(fullUrl);
            HttpURLConnection conn = (HttpURLConnection)ambariServer.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(apiClient.getConnectTimeout());
            conn.setReadTimeout(apiClient.getReadTimeout());

            if (Objects.nonNull(header)) {
                header.entrySet().forEach(entry -> {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                });
            }

            // body里携带内容
            conn.setDoOutput(false);
            conn.setDoInput(true);

            int code = conn.getResponseCode();
            if(code == 200){
                String response = readContentFromStream(conn.getInputStream());
                if (apiClient.isDebugging()) {
                    logger.info(response);
                }
                return response;
            } else {
                String error = readContentFromStream(conn.getInputStream());
                throw new RuntimeException("请求Ambari 获取一个服务所布署的主机信息出错, status:" + code + " content:" + error);
            }
        } catch (Exception ex) {
            throw new RuntimeException("请求Ambari 获取一个服务所布署的主机信息出错", ex);
        }
    }

}
