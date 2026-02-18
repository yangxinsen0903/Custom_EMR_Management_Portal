package com.sunbox.sdpadmin.controller;

import com.sunbox.domain.DestroyTaskRequest;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpadmin.service.IDestroyCluTaskService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/admin/api")
public class DestroyCluTaskController extends BaseAdminController {

    @Resource
    IDestroyCluTaskService destroyCluTaskService;

    //增加查询销毁任务的页面：
//1.查询销毁任务列表；
//2.重试销毁失败的任务；
//3.把这个销毁任务取消. 不执行了.
    @PostMapping("/queryDestroyTask")
    public ResultMsg queryDestroyTask(@RequestBody DestroyTaskRequest request) {
        // 集群名称, 等, 分页

        ResultMsg res = destroyCluTaskService.queryDestroyTask(request);

        return res;
    }

    @PostMapping("/retryActivity")
    public ResultMsg retryActivity(@RequestBody DestroyTaskRequest request) {
        // 集群id
        String clusterId = request.getClusterId();
        ResultMsg res = destroyCluTaskService.retryActivity(clusterId);
        return res;
    }

    //把这个销毁任务取消. 不执行了. cancel  任务已取消4, . 把代销会的任务取消?
    @PostMapping("/cancelTask")
    public ResultMsg cancelTask(@RequestBody DestroyTaskRequest request) {
        String clusterId = request.getClusterId();
        ResultMsg res = destroyCluTaskService.cancelTask(clusterId);
        return res;
    }


}
