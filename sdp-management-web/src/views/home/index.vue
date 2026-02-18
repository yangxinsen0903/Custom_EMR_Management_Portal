/**Created by liaoyingchao on 12/1/22.*/

<template>
  <div class="index">
    <div class="ad-div">
      <div class="ad-logo">
        <img src="@/assets/home/shein-logo.png">
      </div>
      <div class="info-div">
        <div class="ad-title">欢迎使用 Shein Data Platform (SDP 4.0)</div>
        <div class="ad-msg">Shein Data Platform (简称SDP）是自研的大数据运维管控平台，提供简单易集成的Zookeeper、
          HDFS 、MapReduce、 Yarn、 Hive、 Tez、 Spark、 Sqoop、HBase等开源大数据计算和存储引擎 。
        </div>
      </div>
    </div>
    <div class="block">
      <div class="block-title-row">
        <div class="title">
          集群概览
        </div>
        <div class="right-btns">
          <el-button type="primary" @click="newCluster" v-if="permissionCheck.currentPermissionCheck(['Maintainer', 'Administrator'])">直接创建集群</el-button>
          <el-button type="primary" @click="newCluster" v-if="permissionCheck.currentPermissionCheck(['Staff'])">工单创建集群</el-button>
          <el-button @click="toClusterList">管理集群</el-button>
        </div>
      </div>
      <div class="overview-items">
        <div class="item" v-for="item in overviewItems">
          <div class="item-icon">
            <img :src="item.icon">
          </div>
          <div class="item-number">{{ item.count }}</div>
          <div class="item-name">{{ item.name }}</div>
        </div>
      </div>
    </div>
    <div class="block">
      <div class="block-title-row">
        <div class="title">
          版本信息
        </div>
      </div>
      <div class="info-items">
        <el-table :data="versionData" header-row-class-name="theader" border style="width: 100%">
          <el-table-column prop="sdpVersion" label="镜像版本" width="120" />
          <el-table-column prop="imageVersion" label="系统版本" min-width="600" />
        </el-table>
<!--        <div class="info-row">-->
<!--          <div class="label-div">系统版本</div>-->
<!--          <div class="value-div">{{ versionData.sdpVersion }}</div>-->
<!--        </div>-->
<!--        <div class="info-row">-->
<!--          <div class="label-div">镜像版本</div>-->
<!--          <div class="value-div">{{ versionData.imageVersion }}</div>-->
<!--        </div>-->
      </div>
    </div>
    <div class="block news-div">
      <div class="block-title-row">
        <div class="title">
          产品动态
        </div>
        <!--<div class="right-btns">-->
        <!--<div class="right-btn">-->
        <!--<span>更多</span>-->
        <!--<el-icon><ArrowRight /></el-icon>-->
        <!--</div>-->
        <!--</div>-->
      </div>
      <div class="news-list" ref="Ref_news_list">
        <div class="news-item" :class="{'news-bg': idx % 2 == 0}" v-for="(news, idx) in newsItems">
          <div class="news-time">{{ news.time }}</div>
          <div class="news-title">{{ news.title }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  ArrowRight
} from '@element-plus/icons-vue'
import {ref, reactive, defineEmits, defineProps, toRefs, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import HomeApi from "../../api/home";
import {ElMessage, ElMessageBox} from 'element-plus';
import permissionCheck from "@/utils/permission-check";

const router = useRouter()

const overviewItems = ref([
  {
    icon: require("@/assets/home/jqzs.png"),
    state: '',
    count: 0,
    name: '集群总数',
  },
  {
    icon: require("@/assets/home/cjz.png"),
    state: 1,
    count: 0,
    name: '创建中',
  },
  {
    icon: require("@/assets/home/yxz.png"),
    state: 2,
    count: 0,
    name: '运行中',
  },
  {
    icon: require("@/assets/home/yxh.png"),
    state: -3,
    count: 0,
    name: '待销毁',
  },
  {
    icon: require("@/assets/home/yxh.png"),
    state: -1,
    count: 0,
    name: '销毁中',
  },
  {
    icon: require("@/assets/home/yxh.png"),
    state: -2,
    count: 0,
    name: '已销毁',
  },
  {
    icon: require("@/assets/home/yxh.png"),
    state: -4,
    count: 0,
    name: '销毁失败',
  },
  {
    icon: require("@/assets/home/cjsb.png"),
    state: -9,
    count: 0,
    name: '创建失败',
  },
  {
    icon: require("@/assets/home/cjsb.png"),
    state: 3,
    count: 0,
    name: '创建审核中',
  },
  {
    icon: require("@/assets/home/cjsb.png"),
    state: -5,
    count: 0,
    name: '创建审核否决',
  },
  {
    icon: require("@/assets/home/cjsb.png"),
    state: 4,
    count: 0,
    name: '删除审核中',
  },
  // {
  //   icon: require("@/assets/home/cjsb.png"),
  //   state: -6,
  //   count: 0,
  //   name: '删除审核否决',
  // },
])

function clusterOverview() {
  HomeApi.clusterOverview().then(res => {
    if (res.result == true) {
      let arr = res.data || []
      let total = 0
      arr.forEach(item => {
        let count = parseInt(item.state_count || 0)
        total += count

        let ovItem = overviewItems.value.find(itm => {
          return itm.state == item.state
        })
        ovItem.count = count
      })
      overviewItems.value[0].count = total
    } else {
      ElMessage.error(res.errorMsg)
    }
  })
}

clusterOverview()

const versionData = ref([])

function sdpVersionInfo() {
  HomeApi.sdpVersionInfo().then(res => {
    if (res.result == true) {
      let data = res.data || []
      versionData.value = data
    } else {
      ElMessage.error(res.errorMsg)
    }
  })
}

sdpVersionInfo();

const newsItems = ref([
  {
    time: '2025-03-10',
    title: 'SDP4.1.10发布，主要功能：1. SDP1.0镜像: yarn resourcemanager 启动后添加am标签并且Exclusive=false,该队列允许运行Task任务；; ' +
        '2. SDP1.0镜像: 修复Hbase表删除后JMX指标未释放Hbase-27681；3. 竞价实例被驱逐后，保证会自动扩容补足；4. 申请VM资源时，增加校验每个实例组的资源申请情况；' +
        '5. fix: 清理脱管VM时，错误判断VM创建时间。',
  },
  {
    time: '2025-03-03',
    title: 'SDP4.1.9发布，主要功能：1. Azure Fleet支持竞价Fleet只选一个VMSku,SDP取消自动补全3个Sku; 2. 增加自动开启Core实例组am标签功能',
  },
    {
    time: '2025-02-21',
    title: 'SDP4.1.8发布，主要功能：1. SheinApi接口调用增加了日志打印; 2. SheinApi 创建集群接口, az参数兼容一期参数定义',
  },
  {
    time: '2025-02-13',
    title: 'SDP4.1.7发布，主要功能：1. SDP1.0镜像：Ambari 2.7.5支持AM只运行在Core实例组',
  },
  {
    time: '2025-01-17',
    title: 'SDP4.1.6发布，主要功能：1. SDP1.0、SDP2.0镜像：优化Ambari慢查询',
  },
  {
    time: '2025-01-09',
    title: 'SDP4.1.5发布，主要功能：1. 集群全托管扩缩容参数下沉至集群; 2. SheinAPI: 增加全托管弹性扩缩容API',
  },
  {
    time: '2024-12-31',
    title: 'SDP4.1.4发布，主要功能：1. Bugfix: SheinApi创建集群时，自动补全sku优化; 2. Bugfix: Task实例组VM数量为0时，创建集群失败;' +
        '3. Bugfix: 步骤重试时，当前步骤状态未变更; 4. 优化:Ansible api功能优化; 5. 优化:Ansible agent子进程执行任务优化; 6. 优化:Service bus增加消息续租功能。',
  },
  {
    time: '2024-12-09',
    title: 'SDP4.1.3发布，主要功能：1. Bugfix: 修复集群新建实例组时未能动态计算配置; 2. Shein Api接口中，需要合并一期数据的接口增加了sdpVersion字段.',
  },
  {
    time: '2024-10-30',
    title: 'SDP4.1.2发布，主要功能：1. 优化：RM接口删除VM接口增加dnsName字段；' +
        '2. 优化：Service Principal增加证书认证模式；' +
        '3. 优化：创建集群时，for Tag可以手动修改；' +
        '4. 优化：创建集群时，svc和system可以实时更新查询' +
        '5. Bugfix：复制已销毁集群时，不能正确复制手动修改的参数;' +
        '6. Bugfix：复制集群时，不能正确回显源集群的Tag；',
  },
  {
    time: '2024-09-15',
    title: 'SDP4.1.1发布，主要功能：1. 增加Pv2磁盘参数修改功能；2. Spot选择机型优化：去掉单VM选项，改为手选多Sku资源池；' +
        '3. 自动从Azure上清理SDP脱管的VM；' +
        '4. Sheinapi增加接口：增加Pv2磁盘参数修改接口；',
  },
  {
    time: '2024-08-30',
    title: 'SDP4.1.0发布，主要功能：1.SDP在创建集群和销毁集群时，需要对接Shein工单系统；' +
        '2. SDP运维功能增加一键采集日志功能；3. 竞价实例支持多机型资源池:创建集群进行VM SKU选择时，如果实例组付费方式为竞价时，增加配置开关开启Spot多机型资源池；' +
        '4. Shein Api增加Proxy功能：根据请求参数判断请求路由至SDP一期还是SDP二期；' +
        '5. 集群销毁功能优化；6. 镜像升级：hive 增加补丁HIVE-23240，修复spark动态分区不能为静态值问题，涉及：SDP2.0；' +
        '7. 镜像升级：yarn resourcemanager 启动后添加am 标签，涉及：SDP2.0；' +
        '8. ambari 修复清理历史数据报错，涉及：SDP2.0；9. 修改ambari ui 错位问题，涉及：SDP2.0 ',
  },{
    time: '2024-07-30',
    title: 'SDP4.0.0发布，主要功能：1. 支持多区域部署；2. 内置Stack版本升级,同时支持SDP1.0和SDP2.0；' +
        '3. 适配Azure Spot Fleet Api；4. Ambari版本升级至2.7.7；5. OS镜像升级至Ubuntu 22.04；' +
        '6. 增加全托管式弹性伸缩；7. SDP安全增强；' +
        '8. 集群主机上下线需要有事件通知；9. MI相关信息脱敏显示；10. SDP集群删除限流优化；' +
        '11. Shein api Token细化读写权限且支持过期；12. 实例组扩缩容增加串并行切换开关；' +
        '13. 集群Tag录入优化；14. 复制集群优化；15. 修复Ambari Hosts冗余记录问题；' +
        '16. Core实例组缩容偶发Missing Blocks告警处理；',
  },
  {
    time: '2023-10-10',
    title: 'SDP3.0.26发布，主要功能：1. 申请VM资源时，增加vmId字段；',
  },
  {
    time: '2023-09-12',
    title: 'SDP3.0.25发布，主要功能：1. fix:申请主机资源时主机名称中的时间戳问题; 2. fix:VM三方比对时,从Yarn获取数据失败后整个任务中止；',
  },
  {
    time: '2023-08-22',
    title: 'SDP3.0.24发布，主要功能：1. 优化并修复页面显示问题; 2. 修复异常重启时扩缩容无法获取锁问题; 3. VM三方比对时兼容异常格式数据; 4. 优化Prometheus指标只返回运行中集群的数据；' +
        '5. 扩缩容时从Yarn获取主机状态时，只获取运行中的主机状态；6. NodeManager Decommission逻辑优化； 7. 删除ambariHost失败时，加入守护列表；8. 新增shein接口，查询实例组pending的任务；',
  },
  {
    time: '2023-08-08',
    title: 'SDP3.0.23发布，主要功能：1. 新增：SDP/Yarn/Azure三方数据对比功能; 2. 新增：向Azure申请资源超时情况下补偿查询; ' +
        '3. 新增：小批量销毁主机时，采用异步方式调用Azure清理单个主机接口；4. 修改：弹性扩缩容判断规则修改，去掉脱管状态；5. 增加：运维管理相关页面',
  },
  {
    time: '2023-08-01',
    title: 'SDP3.0.22-hotfix发布，主要功能：1. 修复查询rm-api接口异常时，无法更新任务状态的问题；',
  },
  {
    time: '2023-07-27',
    title: 'SDP3.0.22发布，主要功能：1. 增加Prometheus监控指标：销毁类任务失败次数；2. 增加服务重启事件的记录；3. 增加SDP与Yarn主机差异的查询接口；4. 超时任务自动重试一次；5. undertow Web容易线程池调优；6. 调用rm-api接口时增加超时限制：300秒；' +
        '7. 删除VM新增守护队列；8. 删除VM优化，只有一个VM时调用删除单个VM接口；9. Scale获取RM地址由Compose实现转移至本地服务实现；',
  },
  {
    time: '2023-07-13',
    title: 'SDP3.0.21发布，主要功能：1. 优化手动扩容超时处理方式，自动触发清理任务； 2. 优化数据库查询性能，增加索引； 3. 优化Compose JVM参数，设置JVM堆内存大小为Pod的60%； 4. prometheus监控接口增加运行中集群的主机数量与CPU数据指标； 5. 优雅缩容默认等待时间调整为180秒；',
  },
  {
    time: '2023-07-06',
    title: 'SDP3.0.20发布，主要功能：1.优化缩容时删除主机的方式，修改为按单个主机删除；2.优化最后一步失败时的百分比，由原来的100%修改为99%；。',
  },
  {
    time: '2023-06-27',
    title: 'SDP3.0.19发布，主要功能：1. 增加定时检查集群中关闭状态的组件并重启；2. 增加HBase的默认配置文件；3. 监控指标增加集群状态Label；4. 伸缩记录的实例列表增加实例状态字段；5. 新增shein接口-新增弹性伸缩规则接口。',
  },
  {
    time: '2023-06-21',
    title: 'SDP3.0.18发布，主要功能：1. fix:竞价扩缩容时针对实例角色加锁，可能引起同一个集群多个竞价实例组无法同时扩缩容。',
  },
  {
    time: '2023-06-20',
    title: 'SDP3.0.17发布，主要功能：1. Shein接口：新增 23. 更新实例组弹性数量范围-v3.0.17；2. Shein接口：Shein接口：新增 24. 更新弹性伸缩规则-v3.0.17；3. SDP其它问题修复。',
  },
  {
    time: '2023-06-15',
    title: 'SDP3.0.16发布，主要功能：1. 支持Prometheus获取监控指标；2. Shein接口：竞价实例买入逐出统计查询接口。',
  },
  {
    time: '2023-06-13',
    title: 'SDP3.0.15发布，主要功能：1. fix:在回滚失败的扩容任务（竞价买入和弹性扩容）时添加10分钟的redis冷却机制；2. fix:创建集群时因降价导致的扩容索引计算不准确的问题；3.fix:获取maintenance_mode异常引起主机不能正确进入Ambari配置组。',
  },
  {
    time: '2023-06-06',
    title: 'SDP3.0.14发布，主要功能：1. 增加竞价实例中断机制：页面增加开关，可以关闭某个集群某个实例组的竞价实例功能；2. 复制集群时，用户可以选择是否复制弹性规则；3.集群资源页面增加对内置Ambari数据库情况下，集群规模超200台的提醒；4.Shein接口：查询集群实例组-v1.1-v2.1增加字段：maxCnt,minCnt。',
  },
  {
    time: '2023-06-01',
    title: 'SDP3.0.13发布，主要功能：Hotfix 竞价实例组无法扩容；',
  },
  {
    time: '2023-06-01',
    title: 'SDP3.0.12发布，主要功能：1.VM逐出事件优化-逐出VM状态变更，增加Spot扩容失败冷却期，优化竟价逐出缩容发起时机；2.Spot主机缩容增加分类：逐出事件逐出/探活超时逐出；3.任务队列增加优先级，提高扩容优先级，高于竞价实例逐出的缩容；4.修复：竞价实例计算期望值时使用旧数据计算的错误；5.复制集群时自动生成的集群名和原集群名称保持一致；6.集群节点组列表增加存储列，显示实例组数据盘信息；7.临时增加竞价实例组买入逐出开关；',
  },
  {
    time: '2023-05-25',
    title: 'SDP3.0.11发布，主要功能：1.Shein接口增加返回参数（查询集群实例组）；2.增加下载Pod中日志的功能；',
  },
  {
    time: '2023-05-23',
    title: 'SDP3.0.10发布，主要功能：1.修复一批主机中一台主机降级时偶尔将所有主机都销毁的问题；2. 修复竞价实例偶尔判断主机是否逐出错误；3.Shein接口增加参数（resize和查询集群实例组）；4.其它功能优化；',
  },
  {
    time: '2023-05-18',
    title: 'SDP3.0.9发布，主要功能：1.修复实例组取名为task时，不能准确获取实例组主机的问题；2.优化扩容后做数据平衡的方式；3.新增实例组时，优化设置扩缩容规则的显示。',
  },
  {
    time: '2023-05-15',
    title: 'SDP3.0.8发布，主要功能：1.升级镜像，Ambari-Agent打升级Patch；2.修复扩缩容流程偶尔失败的问题，提高扩缩容稳定性；3.修复弹性扩缩容规则变更偶尔失效的问题；4.修复复制集群弹性扩缩容规则无法编辑的问题。',
  },
  {
    time: '2023-05-05',
    title: 'SDP3.0.7发布，主要功能：1.针对Azure限流优化创建集群/销毁集群流程；2.优化弹性扩缩容规则判断；3.优化优雅缩容判断逻辑；',
  },
  {
    time: '2023-04-24',
    title: 'SDP3.0.6发布，主要功能：1.Azure申请主机支持开启反物理亲和；2.Azure资源申请失败时新增重试和降级功能；3.竞价实例扩容优化:单次上限50；4.SDP其它问题修复。',
  },
  {
    time: '2023-04-17',
    title: 'SDP3.0.5发布，主要功能：1.Task实例组支持多磁盘；2.增加竞价实例三次探活的机制；3.优化core实例组的缩容拆分逻辑；4.优化竞价实例买入的调度逻辑，失败后冷却10分钟；5. SDP其它问题修复。',
  },
  {
    time: '2023-04-12',
    title: 'SDP3.0.4发布，主要功能：修复镜像问题，更新新镜像',
  },
  {
    time: '2023-04-10',
    title: 'SDP3.0.3发布，主要功能：1.Hive增加补丁HIVE-21660/HIVE-22165/HIVE-19326；2.SDP任务中心添加任务状态的筛选；3.Shein接口Bug修复；4.SDP其它问题修复。',
  },
  {
    time: '2023-04-04',
    title: 'SDP3.0.2发布，主要功能：1.创建集群时，启动SDP步骤增加重试功能；2.任务队列增加删除功能；3.bugfix:us4创建集群时丢失MI信息；4.页面显示优化。',
  },
  {
    time: '2023-04-03',
    title: 'SDP3.0.1发布，主要功能：1.创建集群增加自动重试功能；2.创建集群增加增量创建模式；3.优化大集群磁盘扩容失败问题；4.优化扩缩容，提高稳定性；5.修复其它问题。',
  },
  {
    time: '2023-03-27',
    title: 'SDP3.0发布，主要功能：1.竞价实例；2.优化创建集群流程；3.修改其它问题。',
  },
  {
    time: '2023-03-20',
    title: 'SDP2.1.4发布，主要功能：1.优化扩容/缩容流程，提高稳定性；2.修复实例组自定义配置bug；3.增加默认配置项；4.修复其它问题。',
  }, {
    time: '2023-03-13',
    title: 'SDP2.1.3发布，主要功能：1.优化系统默认参数；2.优化用户自定义脚本执行；3.优化Shein Api查询结果；4.修复hive metastore 查询表字段数量偶发不准问题；5. 修复其它若干问题。',
  },
  {
    time: '2023-03-10',
    title: 'SDP2.1.2发布，修复AnsibleAgent服务servicebus连接未关闭问题。',
  },
  {
    time: '2023-03-06',
    title: 'SDP2.1.1-hotfix发布，紧急修复Yarn ResourceManager NodeManager动态内存分配错误。',
  },
  {
    time: '2023-03-02',
    title: 'SDP2.1.1发布，主要优化缩容流程，及其它问题修复。',
  },
  {
    time: '2023-2-28',
    title: 'SDP2.1发布，新增功能：1. 支持 Core 节点磁盘扩容；2. 支持新增和删除实例组；3. 支持创建集群时使用内置 MySQL 数据库；4. 支持 ganglia 监控；5. 实例组扩缩容优化。',
  },
  {
    time: '2023-2-27',
    title: 'SDP2.0.5发布，功能：1. 优化TASK实例组缩容流程；2. 修复HBase RegionServer Decommission失败的问题；3. 创建集群增加数据中心选项；4. 其它问题修复。',
  },
  {
    time: '2023-2-15',
    title: 'SDP2.0.4发布，功能：1.TezUI功能上线；2.msi provider修复token过期时间和增加文件句柄关闭处理机制；3.abfs修复rename和custom；4.sshd_config添加参数；5.内置MariaDB Client客户端；6.msi custom provider修复cache过期时间计算逻辑；7.logcat.sh优化；8.修复不能重置用户密码；9.修复销毁集群不允许重试。',
  },
  {
    time: '2023-2-8',
    title: 'SDP2.0.3发布，修改问题：1.azcopy支持日志桶托管MI；2.解决日志归档脚本无权限执行问题；3.MySQL驱动降级到5.1.46。',
  },
  {
    time: '2023-2-6',
    title: 'SDP2.0.2发布，修复扩容失败后偶尔无法再次扩容的问题，及其它若干问题。',
  },
  {
    time: '2023-2-3',
    title: 'SDP2.0.1发布，修复创建集群时偶尔NameNode无法启动问题，及其它若干问题。',
  },
  {
    time: '2023-2-1',
    title: 'SDP2.0发布，增加了Core节点手动伸缩、Task节点手动伸缩、Task节点自动伸缩功能；优化了集群创建性能。',
  },
  {
    time: '2023-1-10',
    title: 'SDP1.2发布，增加了日志归集、HBase对象存储功能。',
  },
  {
    time: '2022-12-31',
    title: 'SDP 1.1发布，增加HBase 功能。',
  },
  {
    time: '2022-12-15',
    title: 'SDP 1.0发布',
  },
])

function newCluster() {
  router.push('/clustercreate')
}

function toClusterList() {
  router.push({
    path: '/clusterlist'
  })
}

const Ref_news_list = ref(null)

onMounted(() => {
  // let height = Ref_news_list.value.clientHeight
  // let num = parseInt(height / 45)
  // newsItems.value = newsItems.value.slice(0,num)
})
</script>

<style lang="stylus" scoped type="text/stylus">
.index {
  width 100%;
  height 100%;
  overflow-y auto;
  display flex;
  flex-direction column;

  .ad-div {
    display flex;
    align-items center;
    padding 12px;
    background-color white;

    .ad-logo {
      padding 10px;

      img {
        display block;
      }
    }

    .info-div {
      flex 1;
      padding 10px;

      .ad-title {
        font-size 18px;
        font-weight bold;
      }

      .ad-msg {
        margin-top 15px;
        font-size 14px;
        color #666;
      }
    }
  }

  .block {
    margin-top 10px;
    padding 12px;
    background-color white;

    .block-title-row {
      display flex;
      align-items center;
      justify-content space-between;
      border-bottom 1px solid #ddd;
      padding-bottom 10px;

      .title {
        font-size 16px;
      }

      .right-btns {
        .right-btn {
          display flex;
          align-items center;
          cursor pointer;
          padding 3px 10px;
          color #606266;
          font-size 0

          span, i {
            font-size 14px;
          }
        }

        .right-btn:hover {
          color #3160ed;
        }
      }
    }

    .overview-items {
      padding 25px 0 5px;
      text-align center;
      display grid
      grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
      grid-gap: 25px 10px;

      .item-icon {

      }

      .item-number {
        margin-top 6px;
        font-size 24px;
        font-weight bold;
      }

      .item-name {
        margin-top 6px;
        font-size 14px;
        color #666;
      }
    }

    .info-items {
      //padding-bottom 8px;
      padding-top 10px;

      >>>.theader {
        td, th {
          background-color: #F8F8F8 !important;
        }
      }

      .info-row {
        display flex;
        align-items flex-start;
        padding-top 15px;

        .label-div {
          width 110px;
          font-size 14px;
        }

        .value-div {
          flex 1;
          font-size 14px;
        }
      }
    }
  }

  .news-div {
    flex 1;
    min-height 400px;

    .news-list {
      height calc(100% - 37px)
      overflow-y auto;
      /*display grid;
      grid-template-rows: repeat(auto-fill, minmax(45px, 1fr));
      grid-gap: 0px;
      overflow hidden;*/

      .news-item {
        display flex;
        align-items flex-start;
        border-bottom 1px solid #ddd;
        overflow hidden;
        padding 14px 10px;
        cursor pointer;

        &.news-bg {
          background-color #FAFAFA;
        }

        .news-time {
          width 100px;
          color #666;
          font-size 14px;
          line-height 20px;
        }

        .news-title {
          flex 1;
          font-size 14px;
          line-height 20px;
          color #333;
          word-break break-all;
        }
      }
    }
  }
}
</style>
