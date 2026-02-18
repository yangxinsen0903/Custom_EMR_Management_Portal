/**Created by liaoyingchao on 12/1/22.*/

<template>
  <div class="cluster-list page-css list-css" v-loading="pageLoading">
    <Filters :filters="filters" @onSearch="searchEvent" @onReset="resetEvent"></Filters>
    <div class="functions-row">
      <el-button type="primary" @click="createEvent" v-if="permissionCheck.currentPermissionCheck(['Maintainer', 'Administrator'])">直接创建集群</el-button>
      <el-button type="primary" @click="createEvent" v-if="permissionCheck.currentPermissionCheck(['Staff'])">工单创建集群</el-button>
      <el-button type="primary" text @click="settingEvent" v-if="permissionCheck.currentPermissionCheck(['Maintainer','Administrator'])">全局参数设置</el-button>
    </div>
    <div class="full-container">
      <ListContainer ref="TabelContainer" :listApiFunction="getList">
        <template #default="tableData">
          <el-table
                  :height="tableData.data.tableHeight"
                  :data="tableData.data.tableData"
                  stripe
                  header-row-class-name="theader"
                  style="width: 100%">
            <el-table-column
                    prop="clusterName"
                    label="名称"
                    min-width="120">
              <template #default="scope">
                <div class="cluster-info" @click="toAmbari(scope.row)">
                  <div class="cluster-name">
                    {{ scope.row.clusterName }}
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column
                    prop="regionName"
                    label="数据中心"
                    min-width="60">
            </el-table-column>
            <el-table-column
                    prop="state"
                    label="状态"
                    width="110">
              <template #default="scope">
                <el-button type="primary" text @click="getTaskList(scope.row)">
                  <span>{{ stateToStr(scope.row.state) }}</span>
                  <el-icon style="margin-left: 6px;" class="is-loading" size="18" v-if="scope.row.state == 1 || scope.row.state == -1">
                    <Loading />
                  </el-icon>
                </el-button>
              </template>
            </el-table-column>
            <el-table-column
                    prop="clusterTag"
                    label="集群标签"
                    min-width="120">
              <template #default="scope">
                <!--<el-popover trigger="hover" placement="left" :width="300" popper-class="popper-tag">-->
                  <!--<template #reference>-->
                    <div class="cluster-tags">
                      <template v-for="(item, idx) in scope.row.clusterTag">
                        <div v-if="idx < 1" class="cluster-tag-item single-row">
                          {{ item.tagGroup }}：{{ item.tagVal }}
                        </div>
                      </template>
                      <div class="more-btn" v-if="scope.row.clusterTag.length" @click="moreLabelsEvent(scope.row)">
                        更多
                      </div>
                    </div>
                  <!--</template>-->
                  <!--<div class="popper-tag">-->
                    <!--<div class="cluster-tags">-->
                      <!--<template v-for="(item, idx) in scope.row.clusterTag">-->
                        <!--<div class="cluster-tag-item">-->
                          <!--{{ item.tagGroup }}：{{ item.tagVal }}-->
                        <!--</div>-->
                      <!--</template>-->
                    <!--</div>-->
                  <!--</div>-->
                <!--</el-popover>-->
              </template>
            </el-table-column>
            <el-table-column
                    prop="masterIps"
                    label="Ambari"
                    width="110">
              <template #default="scope">
                <div class="ambari-ip" @mouseover="ipMouseover($event, scope.row.ambariIp)" @mouseout="tipVisible = false"
                     @click="copyIp(scope.row.ambariIp)">
                  {{ scope.row.ambariIp }}
                </div>
                <!--<div class="master-ips">-->
                  <!--<template v-for="(ip, idx) in getMasterIps(scope.row.masterIps)">-->
                    <!--<div class="master-ip-item single-row" v-if="scope.row.ambariIp != ip">-->
                      <!--{{ ip }}-->
                    <!--</div>-->
                  <!--</template>-->
                <!--</div>-->
              </template>
            </el-table-column>
            <el-table-column
                    prop="masterNodeNum"
                    label="Ambari / Master"
                    width="130">
              <template #default="scope">
                {{ scope.row.ambariNodeNum || 1 }} / {{ scope.row.masterNodeNum || 0 }}
              </template>
            </el-table-column>
            <el-table-column
                    prop="coreNodeNum"
                    label="Core"
                    width="70">
            </el-table-column>
            <el-table-column
                    prop="taskNodeNum"
                    label="Task"
                    width="70">
            </el-table-column>
            <el-table-column
                    prop="serviceNum"
                    label="服务数"
                    width="70">
            </el-table-column>
            <el-table-column
                    prop="createdTime"
                    label="创建时间"
                    :formatter="columnTimeFormat"
                    width="170">
            </el-table-column>
            <el-table-column
                    fixed="right"
                    label="管理"
                    width="180">
              <template #default="scope">
                <div>
                  <el-button type="primary" text @click="toResource(scope.row)">资源</el-button>
                  <el-button type="primary" text @click="deleteEvent(scope.row)"  v-if="scope.row.state != '-2' && scope.row.state != '-1'">销毁</el-button>
                  <el-button type="primary" text @click="copyEvent(scope.row)">复制</el-button>
                  <el-button type="primary" text @click="downloadClusterBlueprint(scope.row)" v-if="scope.row.state == '2'">导出</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </ListContainer>
    </div>
    <el-dialog
            class="center-dialog"
            v-model="labelsDialogVisible"
            title="集群标签"
            width="600"
            destroy-on-close
    >
      <div class="dialog-cluster-tags">
        <div class="cluster-tags">
          <template v-for="(item) in clusterTags">
            <div class="cluster-tag-item">
              {{ item.tagGroup }}：{{ item.tagVal }}
            </div>
          </template>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="labelsDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog
            class="center-dialog"
            v-model="dialogVisible"
            title="任务详情"
            width="900"
            destroy-on-close
            @closed="taskDetailClosed"
    >
      <div>
        <TaskDetail :detailData="detailData"></TaskDetail>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog
            class="center-dialog"
            v-model="deleteDialogVisible"
            title="销毁集群"
            width="500"
            destroy-on-close
    >
      <div>
        <div style="display: flex;align-items: center;">
          <el-icon size="30" color="#e6a23c"><Warning /></el-icon>
          <div style="margin-left: 10px;">点击确定后将删除集群及该集群下的所有资源，且不可逆，确定要销毁集群【{{deleteItem.clusterName}}】吗？</div>
        </div>
        <div v-if="deleteItem.deleteProtected == '1'" style="margin-left: 35px;padding-top: 30px;">
          <el-checkbox v-model="deleteItem.check">解除关闭保护</el-checkbox>
          <div style="margin-top: 8px;">此集群开启了关闭保护，若确定销毁，请勾选"解除关闭保护"</div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">关闭</el-button>
          <el-button type="danger" @click="deleteCluster">确定</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog
            class="task-list-dialog center-dialog"
            v-model="taskListVisible"
            title="任务列表"
            width="900"
            destroy-on-close
            @closed="taskListClosed"
    >
      <div>
        <el-table
                :data="taskList"
                stripe
                header-row-class-name="theader"
                height="60vh">
          <el-table-column
                  prop="jobName"
                  label="任务名称/ID"
                  min-width="120">
            <template #default="scope">
              <div class="task-info">
                <div class="task-name">{{ scope.row.jobName }}</div>
                <div class="task-id">{{ scope.row.planId }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column
                  prop="clusterName"
                  label="集群名称/ID"
                  min-width="120">
            <template #default="scope">
              <div class="cluster-info">
                <div class="cluster-name">{{ scope.row.clusterName }}</div>
                <div class="cluster-id">{{ scope.row.clusterId }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column
                  prop="state"
                  label="状态"
                  width="110">
          </el-table-column>
          <el-table-column
                  prop="begTime"
                  label="开始时间"
                  :formatter="columnTimeFormat"
                  width="108">
          </el-table-column>
          <el-table-column
                  prop="endTime"
                  label="结束时间"
                  :formatter="columnTimeFormat"
                  width="108">
          </el-table-column>
          <el-table-column
                  fixed="right"
                  label="管理"
                  width="100">
            <template #default="scope">
              <div>
                <el-button type="primary" text @click="showTaskDetail(scope.row)">任务详情</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="taskListVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
    <el-tooltip
            ref="tooltipRef"
            v-model:visible="tipVisible"
            :popper-options="{
      modifiers: [
        {
          name: 'computeStyles',
          options: {
            adaptive: false,
            enabled: false,
          },
        },
      ],
    }"
            :virtual-ref="buttonRef"
            virtual-triggering
            trigger="click"
            popper-class="singleton-tooltip"
    >
      <template #content>
        <span> {{ tooltipText }} </span>
      </template>
    </el-tooltip>
    <el-dialog
        class="center-dialog"
        v-model="settingDialogVisible"
        title="全局参数设置"
        width="600"
        destroy-on-close
    >
      <div>
        <el-form
            ref="Ref_SettingForm"
            :model="settingForm"
        >
          <div style="display: flex;align-items: center;">
            <el-form-item class="is-required" label="集群销毁限流：" :rules="settingFormRules">
              <el-input
                  v-model="settingForm.destoryIntervalSecond"
                  placeholder="时间区间"
                  style="width: 80px;margin: 0 10px;"
                  clearable
              />
              <div>秒内最多销毁集群</div>
              <el-input
                  v-model="settingForm.destoryLimitCount"
                  placeholder="限流数量"
                  style="width: 80px;margin: 0 10px;"
                  clearable
              />
              <div>个</div>
            </el-form-item>
          </div>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="settingDialogVisible = false">关闭</el-button>
          <el-button :loading="pageLoading" type="primary" @click="saveSetting">保存</el-button>
        </div>
      </template>
    </el-dialog>
    <router-view class="sub-content"/>
  </div>
</template>

<script setup>
  import Filters from '@/components/list-comps/filters'
  import ListContainer from '@/components/list-comps/container'
  import {h,ref,reactive} from 'vue'
  import {useRouter} from 'vue-router'
  import clusterApi from '@/api/cluster'
  import {ElMessage, ElMessageBox, ElCheckbox} from 'element-plus';
  import { Warning, Loading } from '@element-plus/icons-vue'
  import TaskDetail from "../task-center/detail"
  import taskCenterApi from "../../api/task-center";
  import {columnTimeFormat, formatTime} from "@/utils/format-time";
  import clipboard from '@/utils/clipboard'
  import RemoteSelect from "@/components/base/remote-select/remote-select.vue";
  import authKeyApi from "@/api/system-manage";
  import permissionCheck from "@/utils/permission-check";

  const pageLoading = ref(false)

  const router = useRouter();

  const stateOptions = [
    // {"label": "全部", "value": ""},
    {"label": "创建中", "value": 1},
    {"label": "已创建", "value": 2},
    {"label": "待销毁", "value": -3},
    {"label": "销毁中", "value": -1},
    {"label": "已销毁", "value": -2},
    {"label": "销毁失败", "value": -4},
    {"label": "创建失败", "value": -9},
    {"label": "创建审核中", "value": 3},
    {"label": "创建审核否决", "value": -5},
    {"label": "删除审核中", "value": 4},
  ]

  const filters = ref([
    {
      type: "RemoteSelect",
      label: "数据中心",
      rules: [],
      key: "region",
      props: {
        placeholder: "请选择数据中心",
        clearable: true,
        filterable: true,
        // optionsApi: '/admin/meta/selectMetaDataList',
        optionsApiType: 'get',
        optionsApi: '/admin/api/getRegionsForCurrentUser',
        // optionsDefaultArgs: {type: 'SupportedRegionList'},
        optionsProps: {
          label: 'regionName',
          value: 'region',
        }
      },
    },
    {
      type: 'combo-box',
      key: 'clusterName',
      props: {},
      selOptionType: '',
      options: [
        {
          "type": "el-input",
          "label": "集群名称",
          "rules": [],
          "key": "clusterName",
          "props": {"placeholder": "请输入集群名称", "clearable": true}
        },
        {
          "type": "el-input",
          "label": "集群ID",
          "rules": [],
          "key": "clusterId",
          "props": {"placeholder": "请输入集群ID", "clearable": true}
        }
      ]
    },
    {
      "type": "RemoteSelect",
      "label": "状态",
      "rules": [],
      "key": "state",
      "props": {
        "placeholder": "请选择状态",
        "clearable": true,
        "collapseTags": true,
        multiple: true,
        collapseTagsTooltip: true,
        "options": stateOptions
      }
    },
    {
      "type": "el-cascader",
      "label": "集群标签",
      "rules": [],
      "key": "label",
      "props": {
        "placeholder": "请选择集群标签",
        "clearable": true,
        props: {
          lazy: true,
          lazyLoad(node, resolve) {
            const {level} = node

            if (level == 0) {
              clusterApi.gettagKeyList().then(res => {
                if (res.result == true) {
                  let arr = res.data || []
                  let options = [{label: '全部', value: '', leaf: true}]
                  for (let i = 0; i < arr.length; i++) {
                    let item = arr[i]
                    options.push({
                      value: item.dictValue,
                      label: item.dictName,
                      leaf: false
                    })
                  }
                  resolve(options)
                } else {
                  ElMessage.error(res.errorMsg)
                  console.log(res)
                  resolve([])
                }
              })
            } else {
              clusterApi.getTagValueList({tagKey: node.value}).then(res => {
                if (res.result == true) {
                  let arr = res.data
                  let options = []
                  for (let i = 0; i < arr.length; i++) {
                    let item = arr[i]
                    options.push({
                      value: item.tagVal,
                      label: item.tagVal,
                      leaf: true
                    })
                  }
                  resolve(options)
                } else {
                  ElMessage.error(res.errorMsg)
                  console.log(res)
                  resolve([])
                }
              })
            }
          },
        }
      }
    }
  ])

  function getList(data) {
    return clusterApi.list(data)
  }

  function createEvent() {
    // window.open('/clustercreate')
    router.push('/clustercreate')
  }

  const settingDialogVisible = ref(false)
  const Ref_SettingForm = ref(null)
  const settingForm = reactive({
    destoryLimitCount: '',
    destoryIntervalSecond: ''
  });
  const settingFormRules = []
  function settingEvent() {
    clusterApi.getDestoryClusterLimitConfig().then(res => {
      if (res.result == true) {
        let d = res.data || {}
        settingForm.destoryIntervalSecond = d.destoryIntervalSecond || ''
        settingForm.destoryLimitCount = d.destoryLimitCount || ''

        settingDialogVisible.value = true
      } else {
        ElMessage.error(res.errorMsg)
      }
    })
  }

  function saveSetting() {
    let reg = /^[1-9][0-9]*$/;
    if (!reg.test(settingForm.destoryIntervalSecond)) {
      return ElMessage.error("限流时间区间必须为正整数")
    }
    if (!reg.test(settingForm.destoryLimitCount)) {
      return ElMessage.error("限流数量必须为正整数")
    }

    clusterApi.saveDestoryClusterLimitConfig(settingForm).then(res => {
      if (res.result == true) {
        ElMessage.success("保存成功！")

        settingDialogVisible.value = false
      } else {
        ElMessage.error(res.errorMsg)
      }
    })
  }

  function stateToStr(state) {
    let option = stateOptions.find(item => {
      return item.value === state
    }) || {}
    return option.label || ''
  }

  const TabelContainer = ref(null)

  function searchEvent(data) {
    let arr = data.label || []
    let params = {
      ...data
    }
    if (arr.length == 2) {
      params.clusterTag = arr[0]
      params.tagValue = arr[1]
    }
    TabelContainer.value.filterEvent(params);
  }

  function resetEvent(data) {
    searchEvent(data)
  }

  let taskListVisible = ref(false)

  let taskListTimer = null

  const taskList = ref([])

  function getTaskList(item, showLoading = true) {
    if (showLoading) {
      pageLoading.value = true
    }
    taskCenterApi.list({
      pageIndex: 1,
      pageSize: 50,
      clusterId: item.clusterId
    }).then(res => {
      if (res.result == true) {
        taskList.value = res.data
        if (showLoading) {
          taskListVisible.value = true
        }

        if (taskListVisible.value) { // 只有打开状态才轮询，防止关闭的时候正在发生请求
          let allFinished = true
          for (let i = 0; i < taskList.value.length; i++) {
            if (taskList.value[i].endTime == '-') {
              allFinished = false
              break ;
            }
          }
          if (!allFinished) {
            if (taskListTimer) {
              clearTimeout(taskListTimer)
            }
            taskListTimer = setTimeout(() => {
              getTaskList(item, false)
            }, 10 * 1000)
          }
        }
      } else {
        ElMessage.error(res.errorMsg)
      }
    }).finally(() => {
      pageLoading.value = false
    })
  }

  function taskListClosed() {
    console.log("taskListClosed")
    if (taskListTimer) {
      clearTimeout(taskListTimer)
      taskListTimer = null
    }
  }

  let dialogVisible = ref(false)

  let taskDetailTimer = null

  const detailData = ref({})

  function showTaskDetail(item, showLoading = true) {
    if (showLoading) {
      pageLoading.value = true
    }
    taskCenterApi.getjobdetail({planId: item.planId}).then(res => {
      if (res.result == true) {
        detailData.value = res.data
        if (showLoading) {
          dialogVisible.value = true
        }

        if (dialogVisible.value) { // 只有打开状态才轮询，防止关闭的时候正在发生请求
          let activityInfos = detailData.value.activityInfos || []
          let allFinished = true
          for (let i = 0; i < activityInfos.length; i++) {
            if (activityInfos[i].endTime == '-') {
              allFinished = false
              break ;
            }
          }
          if (!allFinished) {
            if (taskDetailTimer) {
              clearTimeout(taskDetailTimer)
            }
            taskDetailTimer = setTimeout(() => {
              showTaskDetail(item, false)
            }, 10 * 1000)
          }
        }
      } else {
        ElMessage.error(res.errorMsg)
      }
    }).finally(() => {
      pageLoading.value = false
    })
  }

  function taskDetailClosed() {
    console.log("taskListClosed")
    if (taskDetailTimer) {
      clearTimeout(taskDetailTimer)
      taskDetailTimer = null
    }
  }

  let deleteDialogVisible = ref(false)
  const deleteItem = ref({})

  function deleteEvent(item) {
    deleteItem.value = item
    deleteItem.value.check = false
    deleteDialogVisible.value = true
  }

  function deleteCluster() {
    if (deleteItem.value.deleteProtected == '1' && deleteItem.value.check == false) {
      ElMessage.error("集群关闭保护中，请先勾选\"解除关闭保护\"再销毁")
      return ;
    }
    pageLoading.value = true
    clusterApi.deleteCluster({
      clusterId: deleteItem.value.clusterId,
      relieveDeleteProtected: deleteItem.value.check
    }).then(res => {
      if (res.result == true) {
        deleteDialogVisible.value = false
        ElMessage.success("集群开始销毁中...")
        searchEvent()
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    }).finally(() => {
      pageLoading.value = false
    })
  }

  function getMasterIps(masterIps) {
    return masterIps.split(',')
  }

  function toAmbari(item) {
    if (item.state != '2') {
      ElMessage.warning('集群已销毁或还未创建成功！')
      return ;
    }
    let url = 'http://' + item.ambariIp + ':8080'
    window.open(url)
  }

  function copyEvent(item) {
    // ElMessageBox.confirm('您确定需要复制该集群吗？', '提示', {
    //   confirmButtonText: '确定',
    //   cancelButtonText: '取消',
    //   type: 'warning',
    // }).then(() => {
    //   router.push({
    //     path: '/clustercopy',
    //     query: {
    //       clusterId: item.clusterId,
    //       clusterName: item.clusterName,
    //     }
    //   })
    // }).catch(() => {
    //
    // })

    const checked = ref(false);
    ElMessageBox.confirm( () => h('p', null, [
      h('span', null, '您确定需要复制该集群吗？'),
      h('br', null, null),
      h(ElCheckbox, {
        modelValue: checked.value,
        label: "同步复制弹性扩缩容规则",
        'onUpdate:modelValue': (val) => {
          checked.value = val
        },
      }),
    ]),'提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(() => {
      var params = {
        clusterId: item.clusterId,
        clusterName: item.clusterName,
        fetchScalingRules: checked.value
      };

      router.push({
        path: '/clustercopy',
        query: {
          clusterId: item.clusterId,
          clusterName: item.clusterName,
          fetchScalingRules: checked.value
        }
      })
    }).catch(() => {

    })


  }

  function downloadClusterBlueprint(item) {
    let url = "/admin/api/downloadClusterBlueprint" + '?clusterId=' + item.clusterId
    window.open(url)
    // pageLoading.value = true
    // clusterApi.downloadClusterBlueprint({
    //   clusterId: item.clusterId
    // }).then(res => {
    //   pageLoading.value = false
    //   if (res.result == true) {
    //     ElMessage.success("导出成功！")
    //   } else {
    //     ElMessage.error(res.errorMsg)
    //   }
    // }).catch(err => {
    //   pageLoading.value = false
    // })
  }

  function toResource(item) {
    router.push({
      path: '/clusterlist/clusterresource',
      query: {
        clusterId: item.clusterId,
        clusterName: item.clusterName,
        ambariIp: item.ambariIp,
      }
    })
  }

  let buttonRef = ref()
  const tooltipRef = ref()
  const tooltipText = ref('复制成功')
  let tipVisible = ref(false)
  let ipTimer = null

  function ipMouseover(e, ipStr) {
    buttonRef.value = e.currentTarget

    if (ipStr != '-') {
      if (ipTimer) {
        clearTimeout(ipTimer)
      }
      tooltipText.value = '点击复制'
      tipVisible.value = true
      ipTimer = setTimeout(() => {
        tipVisible.value = false
      }, 1000)
    }
  }

  function copyIp(ipStr) {
    if (ipStr != '-') {
      clipboard(ipStr, (result) => {
        if (result) {
          if (ipTimer) {
            clearTimeout(ipTimer)
          }
          tooltipText.value = '复制成功'
          tipVisible.value = true
          ipTimer = setTimeout(() => {
            tipVisible.value = false
          }, 500)
        } else {
          console.log('复制失败！')
        }
      })
    }
  }

  let labelsDialogVisible = ref(false)
  const clusterTags = ref([])

  function moreLabelsEvent(item) {
    clusterTags.value = item.clusterTag

    labelsDialogVisible.value = true
  }

</script>

<style lang="stylus" scoped type="text/stylus">
  .cluster-list {
    position relative;

    .functions-row {
      display flex;
      align-items center;
      justify-content space-between;
    }

    .full-container {
      .cluster-info {
        cursor pointer;

        .cluster-name {
          font-size 14px;
          color #315fce;
        }

        .cluster-id {
          font-size 12px;
          color #315fce;
        }
      }

      .ambari-ip {
        color #315fce;
      }

      .cluster-monitor {
        height 24px;
        display flex;
        align-items flex-end;
        cursor pointer;

        .cluster-line {
          margin 0 1px;
          background-color #999;
          width 4px;
        }
      }

      .cluster-tags {
        display flex;
        align-items center;

        .cluster-tag-item {
          padding 0px 5px;
          background-color #b5eeff;
          border-radius 3px;
          margin 3px 8px 3px 0;
          cursor default;
          color #666;
        }
        .more-btn {
          margin-left 10px;
          width 40px;
          text-align center;
          white-space: nowrap;
          font-size 12px;
          cursor pointer;
          &:hover {
            color #315FCE;
          }
        }
      }
      .master-ips {
        display flex;
        flex-wrap wrap;
        .master-ip-item {
          margin 3px 10px 3px 0;
          color #666;
        }
      }
    }

    >>>.task-list-dialog {
      // 表头
      .theader {
        td, th {
          background-color: #F8F8F8 !important;
          .cell {
            word-break break-word;
          }
        }
      }

      .cluster-info, .task-info {
        .cluster-name, .task-name {
          font-size 14px;
        }

        .cluster-id, .task-id {
          font-size 12px;
        }
      }

      .el-table__cell {
        .el-button {
          padding-left 0;
          padding-right 0;
        }
      }
    }

    .dialog-cluster-tags {
      .cluster-tags {
        display flex;
        flex-wrap wrap;

        .cluster-tag-item {
          padding 2px 8px;
          background-color #b5eeff;
          border-radius 3px;
          margin 5px 12px 5px 0;
          cursor default;
          color #666;
        }
      }
    }
  }
</style>
