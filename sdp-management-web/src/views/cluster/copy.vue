/**Created by liaoyingchao on 12/3/22.*/

<template>
  <div class="create page-css detail-css" v-loading="pageLoading">
    <div class="page-title">创建集群(SDP)</div>
    <div class="steps-div">
      <Steps v-model="stepIndex" :stepOptions="stepOptions"></Steps>
    </div>
    <div class="step-cont" v-if="detailLoaded">
      <template v-if="stepIndex == 0">
        <Step1 v-model="pageForm" :isCopy="true" @changeStep="stepIndex = $event"></Step1>
      </template>
      <template v-else-if="stepIndex == 1">
        <Step2 v-model="pageForm" :isCopy="true" @changeStep="stepIndex = $event"></Step2>
      </template>
      <template v-else-if="stepIndex == 2">
        <Step3 v-model="pageForm" :isCopy="true" @changeStep="stepIndex = $event"></Step3>
      </template>
      <template v-else-if="stepIndex == 3">
        <Step4 v-model="pageForm" :isCopy="true" @changeStep="stepIndex = $event" @createEvent="createEvent"></Step4>
      </template>
    </div>
  </div>
</template>

<script setup>
  import Steps from '@/components/base/steps/steps'
  import {ref, reactive, defineEmits, defineProps, toRefs, nextTick} from 'vue'
  import Step1 from './comps/step-1'
  import Step2 from './comps/step-2'
  import Step3 from './comps/step-3'
  import Step4 from './comps/step-4'
  import clusterApi from "../../api/cluster";
  import {ElMessage, ElMessageBox} from 'element-plus';
  import { useRoute, useRouter } from 'vue-router'
  import utils from "../../utils/utils";
  import userCenter from "@/utils/user-center";
  import store from "@/store";

  let stepIndex = ref(-1)

  userCenter.getUserInfo().then(userInfo => {
    store.state.userInfo = userInfo || {}

    nextTick(() => {
      stepIndex.value = 0
    })
  })

  const pageLoading = ref(false)

  const route = useRoute()
  const router = useRouter()

  const stepOptions = [{
    name: '基础配置',
  }, {
    name: '软件配置',
  }, {
    name: '硬件配置',
  }, {
    name: '确认配置信息',
  }]

  const pageForm = ref({
    clusterName: '', // 集群名称
    tagMap: {}, // 集群标签

    instanceGroupVersion: {
      clusterReleaseVer: "",
      clusterApps: []
    },
    scene: '',
    clusterCfgs: [], // 参数配置

    ambariUsername: 'admin', // Ambari用户名
    ambariPassword: '', // Ambari密码
    deleteProtected: '', // 关闭保护
    isWhiteAddr: '', // 直接销毁白名单
    confClusterScript: [], //初始化脚本

    zone: '', // 可用区
    vNet: '', // 选择网络
    vNetName: '', // 选择网络名称
    subNet: '', // 子网络
    subNetName: '', // 子网络名称
    masterSecurityGroup: '', // 主安全组
    masterSecurityGroupName: '', // 主安全组名称
    slaveSecurityGroup: '', // 子安全组
    slaveSecurityGroupName: '', // 子安全组名称

    keypairId: '', // 登录秘钥
    vmMI: '',
    isHa: 1, // 高可用
    osDiskType: 'StandardSSD_LRS', // 系统盘类型
    diskSize: 100, // 系统盘大小
    instanceGroupSkuCfgs: [
      {
        vmRole: "Ambari",
        osVolumeType: "StandardSSD_LRS",
        osVolumeSize: 100,
        cnt: 1,
        dataVolumeType: "StandardSSD_LRS",
        dataVolumeSize: 1000,
        memoryGB: 8,
        skuName: "",
        vCPUs: 4,
        minNum: 1,
      },
      {
        vmRole: "Master",
        osVolumeType: "StandardSSD_LRS",
        osVolumeSize: 100,
        cnt: 2,
        dataVolumeType: "StandardSSD_LRS",
        dataVolumeSize: 1000,
        memoryGB: 8,
        skuName: "",
        vCPUs: 4,
        minNum: 1,
      },
      {
        vmRole: "Core",
        osVolumeType: "StandardSSD_LRS",
        osVolumeSize: 100,
        cnt: 1,
        dataVolumeType: "StandardSSD_LRS",
        dataVolumeSize: 1000,
        memoryGB: 8,
        skuName: "",
        vCPUs: 4,
        minNum: 2,
      },
      {
        vmRole: "Task",
        osVolumeType: "StandardSSD_LRS",
        osVolumeSize: 100,
        cnt: 0,
        dataVolumeType: "StandardSSD_LRS",
        dataVolumeSize: 1000,
        dataVolumeCount: 1,
        memoryGB: 8,
        skuName: "",
        vCPUs: 4,
        minNum: 0,
      }
    ], // 实例信息

    ambariDbCfgs: { // 集群元数据
      url: '',
      account: '',
      password: '',
      port: '3306',
      database: '',
    },
    hiveMetadataDbCfgs: { // Hive元数据
      url: '',
      account: '',
      password: '',
      port: '3306',
      database: '',
    },
    logPath: '', // 日志桶
    logMI: ''
  })

  function showBackBox(msg) {
    setTimeout(() => {
      if (route.path == '/clustercopy') {
        ElMessageBox.alert(msg, '提示', {
          confirmButtonText: '返回'
        }).then(() => {
          router.go(-1)
        }).catch(() => {
          router.go(-1)
        })
      }
    }, 100)
  }

  const clusterId = route.query.clusterId || ''
  const duplicateClusterName = route.query.clusterName || ''
  const fetchScalingRules = route.query.fetchScalingRules || ''
  const detailLoaded = ref(false)

  function getDetail() {

    pageLoading.value = true
    clusterApi.getClusterDetail({
      clusterId: clusterId,
      fetchScalingRules: fetchScalingRules
    }).then(res => {
      pageLoading.value = false
      if (res.result == true) {
        detailLoaded.value = true
        console.log(res.data)
        pageForm.value = res.data || {}

        // pageForm.value.clusterName = 'sdp-' + utils.randomString(11)
        pageForm.value.clusterCfgs = pageForm.value.clusterCfgs || []
        pageForm.value.confClusterScript = pageForm.value.confClusterScript || []

        let instanceGroupSkuCfgs = pageForm.value.instanceGroupSkuCfgs || []

        if (instanceGroupSkuCfgs && instanceGroupSkuCfgs.length) {
          let item = pageForm.value.instanceGroupSkuCfgs[0]
          pageForm.value.osDiskType = item.osVolumeType
          pageForm.value.diskSize = item.osVolumeSize
        }

        let taskCfg = instanceGroupSkuCfgs.find(itm => {
          return itm.vmRole == 'Task' || itm.vmRole == 'task'
        })

        if (!taskCfg) {
          instanceGroupSkuCfgs.push({
            vmRole: "Task",
            osVolumeType: pageForm.value.osDiskType,
            osVolumeSize: pageForm.value.diskSize,
            cnt: 0,
            dataVolumeType: "StandardSSD_LRS",
            dataVolumeSize: 1000,
            dataVolumeCount: 1,
            memoryGB: 8,
            skuName: "",
            vCPUs: 4,
            minNum: 0,
          })
        }

        const idxArr = ['Ambari', 'ambari', 'Master', 'master', 'Core', 'core', 'Task', 'task']
        instanceGroupSkuCfgs.sort((a,b)=>{
          return idxArr.indexOf(a.vmRole) - idxArr.indexOf(b.vmRole)
        })

        pageForm.value.instanceGroupSkuCfgs = instanceGroupSkuCfgs

      } else {
        showBackBox(res.errorMsg || '获取集群详情失败！')
        console.log(res)
      }
    }).catch(err => {
      pageLoading.value = false
      showBackBox('获取集群详情失败！')
      console.log(err)
    })
  }

  getDetail();

  function createEvent() {
    pageLoading.value = true
    clusterApi.createCluster({
      ...pageForm.value,
      srcClusterId: clusterId
    }).then(res => {
      pageLoading.value = false
      if (res.result == true) {
        ElMessage.success("集群开始创建中...")
        router.replace({
          path: '/clusterlist'
        })
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    }).catch(err => {
      pageLoading.value = false
      console.log(err)
    })
  }

</script>

<style lang="stylus" scoped type="text/stylus">
  .create {
    padding 30px 10%;
    overflow-y auto;

    .steps-div {
      padding-top 30px;
    }

    .step-cont {
      padding-top 36px;
    }
  }
</style>