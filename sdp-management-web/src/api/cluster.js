import http from '@/utils/http'

const cluster = {
  list(data) {
    return http.post('/admin/api/queryclusterlist', data)
  },
  gettagKeyList(data) {
    return http.get('/admin/api/gettagkeylist', data)
  },
  // 可用版本
  getReleases(data) {
    return http.get('/admin/api/getreleases', data)
  },
  // 获取发行版本可用的应用组件
  getReleaseApps(data) {
    return http.post('/admin/api/getsceneapps', data)
  },
  // 获取组件配置分类列表
  getClassificationList(data) {
    return http.get('/admin/api/getclassificationlist', data)
  },
  // 获取可用网络列表
  getNetworkList(data) {
    return http.get('/admin/api/getnetworklist', data)
  },
  // 获取数据中心列表
  getRegionDetail(data) {
    return http.get('/admin/api/getRegionDetail', data)
  },

  // 获取数据中心列表
  getCurrentUserRegions(data) {
    return http.get('/admin/api/getRegionsForCurrentUser')
  },

  // 获取az列表
  getazlist(data) {
    return http.get('/admin/api/getazlist', {params: data})
  },
  // 获取可用子网络列表
  getSubnetList(data) {
    return http.get('/admin/api/getsubnetlist', {params: data})
  },
  // 获取主安全组列表
  getPrimarySecurityGroupList(data) {
    return http.get('/admin/api/getprimarysecuritygrouplist', {params: data})
  },
  // 获取子安全组列表
  getSubSecurityGroupList(data) {
    return http.get('/admin/api/getsubsecuritygrouplist', {params: data})
  },
  // 获取登录方式密钥对列表
  getKeypairList(data) {
    return http.get('/admin/api/getkeypairlist', {params: data})
  },
  // 获取登录方式密钥对列表
  getOsDiskTypeList(data) {
    return http.get('/admin/api/getosdisktypelist', {params: data})
  },
  // 获取vmsku列表
  getVmskuList(data) {
    return http.get('/admin/api/getvmskulist', {params: data})
  },
  // 获取 MI 列表
  getMIList(data) {
    return http.get('/admin/api/getMIList', {params: data})
  },
  // 查询是否手动设置Ambari数据库名
  getAmbariDbNameManual(data) {
    return http.get('/admin/api/getAmbariDbNameManual', {params: data})
  },
  // 创建集群
  createCluster(data) {
    return http.post('/admin/api/createcluster', data)
  },
  // 检查数据库是否可用
  checkConnect(data) {
    return http.post('/admin/api/checkconnect', data)
  },
  // 检查集群名称是否可用
  checkClusterName(data) {
    return http.post('/admin/api/checkclustername', data)
  },
  // 根据标签Key获取Value
  getTagValueList(data) {
    return http.post('/admin/api/gettagvaluelist', data)
  },
  // 销毁集群
  deleteCluster(data) {
    return http.post('/admin/api/deletecluster', data)
  },
  // 检查脚本路径是否正确
  checkScriptUrl(data) {
    return http.formDataPost('/admin/api/checkcustomscripturi', data)
  },
  // 获取集群详情
  getClusterDetail(data) {
    return http.get('/admin/api/getClusterDetail', {
      params: data
    })
  },
  // 获取脚本路径
  getBaseScriptList(data) {
    return http.post('/admin/api/getbasescriptlist', data)
  },
  // 获取实例组
  getVMGroupsByClusterId(data) {
    return http.get('/admin/api/getVMGroupsByClusterId', {
      params: data
    })
  },
  // 日志捅备选项
  getLogsBlobContainerList(data) {
    return http.get('/admin/api/getLogsBlobContainerList', {
      params: data
    })
  },
  // 脚本任务列表
  scriptJobList(data) {
    return http.post('/admin/api/scriptjoblist', data)
  },
  // 提交用户脚本任务
  saveUserCustomerScript(data) {
    return http.post('/admin/api/saveUserCustomerScript', data)
  },
  // 获取vmlist
  getvmList(data) {
    return http.post('/admin/api/getvmList', data)
  },
  // 更新资源组标签-全量
  updateResourceGroupTags(data) {
    return http.post('/admin/api/updateResourceGroupTags', data)
  },
  // 获取脚本内容
  getScriptContent(data) {
    return http.get('/admin/api/getBlobContent', {
      params: data
    })
  },
  // 节点信息概览
  getVmOverview(data) {
    return http.post('/admin/api/getVmOverview', data)
  },
  // 扩容
  scaleout(data) {
    return http.post('/admin/api/cluster/scaleout', data)
  },
  // 缩容
  scalein(data) {
    return http.post('/admin/api/cluster/scalein', data)
  },
  // 伸缩记录 // 获取实例组实例变化列表(一天以内的变化值)
  scalingLog(data) {
    return http.post('/admin/api/cluster/scalingLog', data)
  },
  // 获取弹性伸缩规则
  getElasticScalingRule(data) {
    return http.post('/admin/api/cluster/getElasticScalingRule', data)
  },
  // 获取弹性伸缩规则
  updateGroupElasticScaling(data) {
    return http.post('/admin/api/cluster/updateGroupElasticScaling', data)
  },
  // 更新全托管弹性扩缩容参数
  updateGroupESFullCustodyParam(data) {
    return http.post('/admin/api/cluster/updateGroupESFullCustodyParam', data)
  },
  // 添加弹性伸缩规则
  postElasticScalingRule(data) {
    return http.post('/admin/api/cluster/postElasticScalingRule', data)
  },
  // 更新弹性伸缩规则
  updateElasticScalingRule(data) {
    return http.post('/admin/api/cluster/updateElasticScalingRule', data)
  },
  // 删除弹性伸缩规则
  deleteElasticScalingRule(data) {
    return http.post('/admin/api/cluster/deleteElasticScalingRule', data)
  },
  // 添加实例组
  addGroup(data) {
    return http.post('/admin/api/addgroup', data)
  },
  // 删除实例组
  deleteGroup(data) {
    return http.post('/admin/api/deleteGroup', data)
  },
  // 磁盘扩容
  growpart(data) {
    return http.post('/admin/api/growpart', data)
  },
  // 获取任务信息
  getTaskInfo(data) {
    return http.post('/admin/api/getTaskInfo', data)
  },
  // 删除任务实例
  deleteScaleOutTaskVms(data) {
    return http.post('/admin/api/deleteScaleOutTaskVms', data)
  },
  // 获取市场价
  getInstancePrice(data) {
    return http.post('/admin/api/spot/getInstancePriceList', data)
  },
  // 查询竞价实例历史价格
  spotPriceHistory(data) {
    return http.post('/admin/api/spot/spotPriceAndEvictionRateHistory', data)
  },
  // 伸缩期望
  getScaleCountInQueue(data) {
    return http.post('/admin/api/getScaleCountInQueue', data)
  },
  // 手动删除队列中的任务
  cancelScalingTask(data) {
    return http.post('/admin/api/cluster/cancelScalingTask', data)
  },
  // 手动删除队列中的任务
  updateSpotState(data) {
    return http.get("/admin/api/updatespotstate", {
                  params: data
                });
  },
  // 获取集群可用镜像
  getOsImageList(data) {
    return http.get('/admin/api/getAvailableImage', {
      params: data
    });
  },
  // 集群导出
  // downloadClusterBlueprint(data) {
  //   return http.get('/admin/api/downloadClusterBlueprint', {
  //     params: data
  //   })
  // },
  // // 复制创建集群
  // duplicateCluster(data) {
  //   return http.post('/admin/api/duplicateCluster', data)
  // },
  // 集群并行或串行扩缩容
  updateClusterParallel(data) {
    return http.post('/admin/api/updateClusterParallel', data);
  },
  // 获取服务列表
  getservicelist(data) {
    return http.get('/admin/api/getservicelist', {params: data});
  },
  // 获取系统列表
  getsystemlist(data) {
    return http.get('/admin/api/getsystemlist', {params: data});
  },
  // 获取集群销毁限流全局配置
  getDestoryClusterLimitConfig(data) {
    return http.get('/admin/api/getDestoryClusterLimitConfig', {params: data});
  },
  // 保存集群销毁限流全局配置
  saveDestoryClusterLimitConfig(data) {
    return http.post('/admin/api/saveDestoryClusterLimitConfig', data);
  },
  // 保存集群销毁限流全局配置
  cleanAmbariHistory(data) {
    return http.post('/admin/api/cleanAmbariHistory', data);
  },
  // 更新集群中销毁白名单的状态
  updateDestroyStatus(data) {
    return http.post('/admin/api/updatedestroystatus', data)
  },
  // 更新PV2数据盘IOPS和MBPS
  updateDiskIOPSAndThroughput(data) {
    return http.post('/admin/api/updateDiskIOPSAndThroughput', data);
  },
}

export default cluster;
