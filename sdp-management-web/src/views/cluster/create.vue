/**Created by liaoyingchao on 12/3/22.*/

<template>
  <div class="create page-css detail-css" v-loading="pageLoading">
    <div class="page-title">创建集群(SDP)</div>
    <div class="steps-div">
      <Steps v-model="stepIndex" :stepOptions="stepOptions"></Steps>
    </div>
    <div class="step-cont">
      <template v-if="stepIndex == 0">
        <Step1 v-model="pageForm" @changeStep="stepIndex = $event"></Step1>
      </template>
      <template v-else-if="stepIndex == 1">
        <Step2 v-model="pageForm" @changeStep="stepIndex = $event"></Step2>
      </template>
      <template v-else-if="stepIndex == 2">
        <Step3 v-model="pageForm" @changeStep="stepIndex = $event"></Step3>
      </template>
      <template v-else-if="stepIndex == 3">
        <Step4 v-model="pageForm" @changeStep="stepIndex = $event" @createEvent="createEvent"></Step4>
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
    clusterName: 'sdp-' + utils.randomString(11), // 集群名称
    tagMap: {
      "svcid": "",
      "svc": "",
      "service": "",
      "for": ""
    }, // 集群标签

    instanceGroupVersion: {
      clusterReleaseVer: "",
      clusterApps: []
    },
    scene: '',
    clusterCfgs: [], // 参数配置

    ambariUsername: 'admin', // Ambari用户名
    ambariPassword: '', // Ambari密码
    deleteProtected: '', // 关闭保护
    isWhiteAddr: 1, // 直接销毁白名单
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
    osDiskType: 'Premium_LRS', // 系统盘类型
    diskSize: 100, // 系统盘大小
    instanceGroupSkuCfgs: [
      {
        vmRole: "Ambari",
        groupName: 'Ambari',
        osVolumeType: "Premium_LRS",
        osVolumeSize: 100,
        cnt: 1,
        dataVolumeType: "Premium_LRS",
        dataVolumeSize: 1000,
        dataVolumeCount: 1,
        memoryGB: 8,
        skuName: "",
        vCPUs: 4,
        cpuType:'',
      },
      {
        vmRole: "Master",
        groupName: 'Master',
        osVolumeType: "Premium_LRS",
        osVolumeSize: 100,
        cnt: 2,
        dataVolumeType: "Premium_LRS",
        dataVolumeSize: 1000,
        dataVolumeCount: 1,
        memoryGB: 8,
        skuName: "",
        vCPUs: 4,
        cpuType:'',
      },
      {
        vmRole: "Core",
        groupName: 'Core',
        osVolumeType: "Premium_LRS",
        osVolumeSize: 100,
        cnt: 2,
        dataVolumeType: "Premium_LRS",
        dataVolumeSize: 1000,
        dataVolumeCount: 1,
        memoryGB: 8,
        skuName: "",
        vCPUs: 4,
        cpuType:'',
      },
      {
        vmRole: "Task",
        groupName: 'task-1',
        osVolumeType: "Premium_LRS",
        osVolumeSize: 100,
        cnt: 0,
        dataVolumeType: "Premium_LRS",
        dataVolumeSize: 1000,
        dataVolumeCount: 1,
        memoryGB: 8,
        skuName: "",
        vCPUs: 4,
        nameCode: 1,
        groupCfgs: [],
        cpuType:'',
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

  function createEvent() {
    pageLoading.value = true
    clusterApi.createCluster(pageForm.value).then(res => {
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
    }).finally(() => {
      pageLoading.value = false
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