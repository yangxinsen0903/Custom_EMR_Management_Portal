import { createRouter, createWebHashHistory, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/main',
    name: 'main',
    meta: {
      title: '主页'
    },
    component: () => import('../views/main.vue'),
    children: [
      // 首页
      {
        path: "/home",
        name: "home",
        meta: {
          title: '概览'
        },
        component: () =>
          import("../views/home/index")
      },
      // 集群
      {
        path: "/clusterlist",
        name: "clusterlist",
        meta: {
          title: '集群列表'
        },
        component: () =>
          import("../views/cluster/list"),
        children: [
          {
            path: "clusterresource",
            name: "clusterresource",
            meta: {
              title: '集群资源管理'
            },
            component: () =>
              import("../views/cluster/resource")
          },
        ]
      },
      // 任务中心
      {
        path: "/taskcenter",
        name: "task-center",
        meta: {
          title: '任务中心'
        },
        component: () =>
          import("../views/task-center/index")
      },
      // 系统管理 用户管理
      {
        path: "/setting/usermanage",
        name: "usermanage",
        meta: {
          title: '系统管理-用户管理'
        },
        component: () =>
          import("../views/user-manage/index")
      },
      // 系统管理 Shein AK管理
      {
        path: "/setting/sheinak",
        name: "sheinak",
        meta: {
          title: '系统管理-Shein AK管理'
        },
        component: () =>
          import("../views/sys-manage/shein-ak/index")
      },
      // 系统管理 业务配置管理
      {
        path: "/setting/businessconfig",
        name: "businessconfig",
        meta: {
          title: '系统管理-业务配置管理'
        },
        component: () =>
          import("../views/sys-manage/business-config")
      },
      // 系统管理 镜像管理
      {
        path: "/setting/iso",
        name: "iso",
        meta: {
          title: '系统管理-镜像管理'
        },
        component: () =>
          import("../views/sys-manage/iso")
      },
      // 系统管理 集群默认配置管理
      {
        path: "/setting/clusterconf",
        name: "clusterconf",
        meta: {
          title: '系统管理-集群默认配置管理'
        },
        component: () =>
          import("../views/sys-manage/cluster-conf")
      },
      // 运维管理 概览
      {
        path: "/maintenance/overview",
        name: "overview",
        meta: {
          title: '运维管理-概览'
        },
        component: () =>
          import("../views/maintenance/overview.vue")
      },
      // 运维管理 VM三方差异
      {
        path: "/maintenance/vmdifference",
        name: "vmdifference",
        meta: {
          title: '运维管理-三方资源比对'
        },
        component: () =>
          import("../views/maintenance/vm-difference.vue"),
        children: [
          {
            path: "differencedetail",
            name: "differenceDetail",
            meta: {
              title: '运维管理-三方资源比对结果'
            },
            component: () =>
              import("../views/maintenance/difference-detail.vue")
          },
        ]
      },
      // 运维管理 异步清理VM
      {
        path: "/maintenance/vmclear",
        name: "vmclear",
        meta: {
          title: '运维管理-异步清理VM'
        },
        component: () =>
          import("../views/maintenance/vm-clear.vue")
      },
      // 运维管理 Azure申请资源查询
      {
        path: "/maintenance/azureapply",
        name: "azureapply",
        meta: {
          title: '运维管理-Azure申请资源查询'
        },
        component: () =>
          import("../views/maintenance/azure-apply.vue")
      },
      // 运维管理 巡检报告
      {
        path: "/maintenance/inspectionreport",
        name: "inspectionreport",
        meta: {
          title: '运维管理-巡检报告'
        },
        component: () =>
          import("../views/maintenance/inspection-report.vue")
      },
      // 运维管理 三方API异常日志
      {
        path: "/maintenance/apiabnormal",
        name: "apiabnormal",
        meta: {
          title: '运维管理-三方API异常日志'
        },
        component: () =>
          import("../views/maintenance/api-abnormal.vue")
      },
      // 运维管理 集群上下线事件管理
      {
        path: "/maintenance/vmeventlist",
        name: "vmeventlist",
        meta: {
          title: '运维管理-集群上下线事件管理'
        },
        component: () =>
          import("../views/maintenance/vm-event-list.vue")
      },
      // 运维管理 销毁任务
      {
        path: "/maintenance/destroytask",
        name: "destroytask",
        meta: {
          title: '运维管理-销毁任务'
        },
        component: () =>
          import("../views/maintenance/destroy-task.vue")
      },
      // 运维管理 工单审批情况
      {
        path: "/maintenance/workorder",
        name: "workorder",
        meta: {
          title: '运维管理-工单审批情况'
        },
        component: () =>
          import("../views/maintenance/work-order.vue")
      },
      // 运维管理 集群信息收集记录
      {
        path: "/maintenance/collectlog",
        name: "collectlog",
        meta: {
          title: '运维管理-集群信息收集记录'
        },
        component: () =>
          import("../views/maintenance/collect-log.vue")
      },
      // 运维管理 azure VM清理
      {
        path: "/metadata/azurecleanedvms",
        name: "azurecleanedvms",
        meta: {
          title: '运维管理-SDP脱管VM列表'
        },
        component: () =>
            import("../views/maintenance/azure-cleaned-vms.vue")
      },
      // 元数据管理 数据中心管理
      {
        path: "/metadata/dataregion",
        name: "dataregion",
        meta: {
          title: '元数据管理-数据中心管理'
        },
        component: () =>
          import("../views/metadata/data-region.vue")
      },
      // 元数据管理 可用区管理
      {
        path: "/metadata/availabilityzone",
        name: "availabilityzone",
        meta: {
          title: '元数据管理-可用区管理'
        },
        component: () =>
          import("../views/metadata/availability-zone.vue")
      },
      // 元数据管理 子网管理
      {
        path: "/metadata/subnet",
        name: "subnet",
        meta: {
          title: '元数据管理-子网管理'
        },
        component: () =>
          import("../views/metadata/subnet.vue")
      },
      // 元数据管理 安全组管理
      {
        path: "/metadata/saftygroup",
        name: "saftygroup",
        meta: {
          title: '元数据管理-安全组管理'
        },
        component: () =>
          import("../views/metadata/safty-group.vue")
      },
      // 元数据管理 机型SKU
      {
        path: "/metadata/vmsku",
        name: "vmsku",
        meta: {
          title: '元数据管理-VMSKU管理'
        },
        component: () =>
          import("../views/metadata/vm-sku.vue")
      },
      // 元数据管理 磁盘SKU
      {
        path: "/metadata/disksku",
        name: "disksku",
        meta: {
          title: '元数据管理-磁盘sku管理'
        },
        component: () =>
          import("../views/metadata/disk-sku.vue")
      },
      // 元数据管理 密钥对管理
      {
        path: "/metadata/sshkey",
        name: "sshkey",
        meta: {
          title: '元数据管理-密钥管理'
        },
        component: () =>
          import("../views/metadata/ssh-key.vue")
      },
      // 元数据管理 key vault
      {
        path: "/metadata/keyvault",
        name: "keyvault",
        meta: {
          title: '元数据管理-Key Vault'
        },
        component: () =>
          import("../views/metadata/key-vault.vue")
      },
      // 元数据管理 托管标识
      {
        path: "/metadata/mi",
        name: "mi",
        meta: {
          title: '元数据管理-托管标识'
        },
        component: () =>
          import("../views/metadata/manage-id.vue")
      },
      // 元数据管理 日志桶管理
      {
        path: "/metadata/logs",
        name: "logs",
        meta: {
          title: '元数据管理-日志桶管理'
        },
        component: () =>
          import("../views/metadata/logs.vue")
      },
    ]
  },
  // 创建集群
  {
    path: "/clustercreate",
    name: "clustercreate",
    meta: {
      title: '创建集群'
    },
    component: () =>
      import("../views/cluster/create")
  },
  // 集群复制
  {
    path: "/clustercopy",
    name: "clustercopy",
    meta: {
      title: '集群复制'
    },
    component: () =>
      import("../views/cluster/copy")
  },
  // 登录
  {
    path: "/login",
    name: "login",
    meta: {
      title: '登录'
    },
    component: () =>
      import("../views/login/login")
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
