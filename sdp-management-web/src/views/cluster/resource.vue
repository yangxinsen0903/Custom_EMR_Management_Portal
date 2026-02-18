/**Created by liaoyingchao on 12/14/22.*/

<template>
  <div class="resource page-css detail-css" v-loading="pageLoading">
    <el-tabs v-model="activeName" class="full-tabs" @tab-change="tabChange">
      <el-tab-pane label="基础信息" name="baseinfo">
        <div class="scroll-div" style="padding-top: 20px;" v-if="clusterData.clusterName">
          <Step4 v-model="clusterData" :isResource="true" @editLabels="editLabels" @refreshEvent="refreshEvent"></Step4>
        </div>
      </el-tab-pane>
      <el-tab-pane label="节点组管理" name="groups">
        <div style="width: 100%;height: 100%;" v-if="groupsLoaded">
          <VmGroup :clusterId="clusterId" :clusterData="clusterData" @toAmbari="toAmbari"></VmGroup>
        </div>
      </el-tab-pane>
      <el-tab-pane label="节点管理" name="nodes">
        <div style="width: 100%;height: 100%;" v-if="nodesLoaded">
          <VmList ref="Ref_VmList" style="flex: 1;min-height: 200px;" :clusterId="clusterId"></VmList>
        </div>
      </el-tab-pane>
      <el-tab-pane label="脚本执行" name="script">
        <div style="width: 100%;height: 100%;" v-if="scriptLoaded">
          <ScriptTask :clusterId="clusterId" :clusterData="clusterData"></ScriptTask>
        </div>
      </el-tab-pane>
      <el-tab-pane label="参数配置" name="arguments">
        <div style="width: 100%;height: 100%;" v-if="argumentsLoaded">
          <ArgumentsDetail :clusterData="clusterData"></ArgumentsDetail>
        </div>
      </el-tab-pane>
    </el-tabs>
    <el-dialog
            class="center-dialog"
            v-model="labelDialogVisible"
            title="编辑集群标签"
            width="700"
            destroy-on-close
    >
      <div class="dialog-content" style="max-height: 70vh;overflow-y: auto;">
        <el-form size="large">
          <el-form-item>
            <ClusterLabel v-model="tagMap"></ClusterLabel>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="labelDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEvent" v-loading="saveLoading">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
  import {ref, reactive, defineEmits, defineProps, toRefs, onMounted, nextTick} from 'vue'
  import {ElMessage, ElMessageBox} from 'element-plus';
  import {ArrowDown, ArrowUp} from '@element-plus/icons-vue'
  import { useRoute, useRouter } from 'vue-router'
  import PageTitle from "../../utils/page-title";
  import clusterApi from "../../api/cluster";
  import utils from "../../utils/utils";
  import Step4 from './comps/step-4'
  import ScriptTask from './comps/script-task'
  import VmList from './comps/vm-list'
  import VmGroup from './comps/vm-group'
  import ClusterLabel from "./comps/label"
  import ArgumentsDetail from "./comps/arguments-detail"

  const pageLoading = ref(false)

  const route = useRoute()
  const router = useRouter()

  const activeName = ref('baseinfo')

  const scriptLoaded = ref(false)
  const nodesLoaded = ref(false)
  const groupsLoaded = ref(false)
  const argumentsLoaded = ref(false)

  function tabChange() {
    if (activeName.value == 'script') {
      nextTick(() => {
        scriptLoaded.value = true
      })
    } else if (activeName.value == 'groups') {
      nextTick(() => {
        groupsLoaded.value = true
      })
    } else if (activeName.value == 'nodes') {
      nextTick(() => {
        nodesLoaded.value = true
      })
    } else if (activeName.value == 'arguments') {
      nextTick(() => {
        argumentsLoaded.value = true
      })
    }
  }

  let showGroup = ref(true)

  function vmGroupShow(type) {
    showGroup.value = type

    // refreshVmList.value = false
    // nextTick(() => {
    //   refreshVmList.value = true
    // })
  }

  // const Ref_VmList = ref(null)
  //
  // function vmGroupResize() {
  //   Ref_VmList.value.vmGroupResize()
  // }

  function showBackBox(msg) {
    setTimeout(() => {
      if (route.path == '/clusterlist/clusterresource') {
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
  const clusterData = ref({})

  function getDetail() {

    pageLoading.value = true
    clusterApi.getClusterDetail({
      clusterId: clusterId
    }).then(res => {
      pageLoading.value = false
      if (res.result == true) {
        clusterData.value = res.data || {}
        clusterData.value.clusterId = clusterId

        clusterData.value.clusterCfgs = clusterData.value.clusterCfgs || []
        clusterData.value.confClusterScript = clusterData.value.confClusterScript || []

        let instanceGroupSkuCfgs = clusterData.value.instanceGroupSkuCfgs || []

        const idxArr = ['Ambari', 'ambari', 'Master', 'master', 'Core', 'core', 'Task', 'task']
        instanceGroupSkuCfgs.sort((a,b)=>{
          return idxArr.indexOf(a.vmRole) - idxArr.indexOf(b.vmRole)
        })

        clusterData.value.instanceGroupSkuCfgs = instanceGroupSkuCfgs

        // 检查是否本地Ambari数据库且集群数量超过200台
        if (clusterData.value.ambariDbCfgs.url === 'localhost' && clusterData.value.state === 2) {
          // 计算集群节点总数
          var vmCount = 0
          clusterData.value.instanceGroupSkuCfgs.forEach(item => {
            vmCount += item.cnt
          })
          if (vmCount > 200) {
            clusterData.value.warnMessage = "集群规模为"+vmCount+"台，已大于200台，不建议使用内置Ambari数据库";
            console.log(clusterData.value.warnMessage)
          }
        }

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

  function refreshEvent() {
    getDetail();
  }

  let labelDialogVisible = ref(false)
  const tagMap = ref({})
  const saveLoading = ref(false)

  function editLabels() {
    tagMap.value = JSON.parse(JSON.stringify(clusterData.value.tagMap))

    labelDialogVisible.value = true
  }

  function saveEvent() {
    saveLoading.value = true
    clusterApi.updateResourceGroupTags({
      clusterId: clusterId,
      tags: tagMap.value
    }).then(res => {
      saveLoading.value = false
      if (res.result == true) {
        labelDialogVisible.value = false
        ElMessage.success('更新成功！')
        clusterData.value.tagMap = tagMap.value
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    }).catch(err => {
      saveLoading.value = false
      console.log(err)
    })
  }

  function toAmbari() {
    if (clusterData.value.state != '2') {
      ElMessage.warning('集群已销毁或还未创建成功！')
      return ;
    }
    let ambariIp = route.query.ambariIp || ''

    let url = 'http://' + ambariIp + ':8080'
    window.open(url)
  }

  onMounted(() => {
    let clusterName = route.query.clusterName || ''
    let title = '集群资源管理'
    if (clusterName) {
      title = '集群[' + clusterName + ']资源管理'
    }
    PageTitle.setTitle(title)
  })
</script>

<style lang="stylus" scoped type="text/stylus">
  .resource {
    overflow hidden;
    padding 5px 12px 0px;
    .vm-group-div {
      border-bottom 1px solid #ddd;
      position relative;
      .small-row {
        padding 4px 0;
        line-height 18px;
        font-size 12px;
        cursor pointer;
      }
      .vm-group-btn {
        position absolute;
        right 0px;
        top 0px;
        cursor pointer;
        padding 4px 8px;
        display flex;
        align-items center;
        i, span {
          display block;
          font-size 13px;
          color #666;
          margin-left 6px;
        }
        &:hover {
          i, span {
            color #315FCE;
          }
        }
      }
    }
    >>>.full-tabs {
      width 100%;
      height 100%;
      .el-tabs__header {
        margin-bottom 0;
      }
      .el-tabs__content {
        height calc(100% - 40px);
        .el-tab-pane {
          height 100%;
          .scroll-div {
            height 100%;
            overflow-y auto;
          }
        }
      }
    }
    .top-buttons {
      padding 15px 0px;
      .el-button {
        width 120px;
      }
    }
  }
</style>
