/**Created by liaoyingchao on 12/1/22.*/

<template>
  <div class="index page-css list-css" v-loading="pageLoading" v-if="pageShow">
    <Filters :filters="filters" @onSearch="searchEvent" @onReset="resetEvent"></Filters>
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
                    min-width="140">
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
                    width="100">
            </el-table-column>
            <el-table-column
                    prop="begTime"
                    label="开始时间"
                    :formatter="columnTimeFormat"
                    min-width="120">
            </el-table-column>
            <el-table-column
                    prop="endTime"
                    label="结束时间"
                    :formatter="columnTimeFormat"
                    min-width="120">
            </el-table-column>
            <el-table-column
                    fixed="right"
                    label="管理"
                    width="180">
              <template #default="scope">
                <div>
                  <el-button type="primary" text @click="showDetail(scope.row)">任务详情</el-button>
                  <el-button type="primary" v-if="needShowVms(scope.row)" text @click="showVms(scope.row)">实例列表</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </ListContainer>
    </div>
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

    <!-- 伸缩实例列表 -->
    <el-dialog
        class="center-dialog"
        v-model="vmsDialogVisible"
        title="实例列表"
        width="60%"
        append-to-body
        destroy-on-close
    >
      <div class="dialog-content vm-group-vms-list">
        <div>
          <el-table
              height="50vh"
              :data="taskInfoVms"
              stripe
              border
              header-row-class-name="theader"
              style="width: 100%">
            <el-table-column
                label="序号"
                align="center"
                width="60">
              <template #default="scope">
                {{ scope.$index + 1 }}
              </template>
            </el-table-column>
            <el-table-column
                prop="vmName"
                label="实例ID"
                min-width="150">
            </el-table-column>
            <el-table-column
                prop="internalip"
                label="内网IP"
                width="120">
            </el-table-column>
            <el-table-column
                prop="skuName"
                label="规格"
                min-width="100">
            </el-table-column>
            <el-table-column
                label="磁盘"
                min-width="150">
              <template #default="scope">
                <div>
                  系统盘：{{ scope.row.osVolumeSize }}GB*{{ scope.row.osVolumeCount }} &nbsp;
                  数据盘：{{ scope.row.dataVolumeSize }}GB*{{ scope.row.dataVolumeCount }}
                </div>
              </template>
            </el-table-column>
            <el-table-column
                label="状态"
                min-width="80">
              <template #default="scope">
                <div>
                  {{ vmInstanceStateToStr(scope.row.state, scope.row.maintenanceMode) }}
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="vmsDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
  import Filters from '@/components/list-comps/filters'
  import ListContainer from '@/components/list-comps/container'
  import {ref, onBeforeUnmount} from 'vue'
  import {useRouter} from 'vue-router'
  import taskCenterApi from '@/api/task-center'
  import {ElMessage, ElMessageBox} from 'element-plus';
  import {shortcuts} from "../../components/js/shortcuts";
  import TaskDetail from "./detail"
  import {columnTimeFormat, formatTime, timeToUtcTime} from "@/utils/format-time";

  const pageLoading = ref(false)

  const router = useRouter();

  const pageShow = ref(false)
  const jobNames = ref([])
  const jobStates = ref([])
  const startPercent = ref("10")
  const endPercent = ref(100)

  const filters = ref([
    {
      "type": "RemoteSelect",
      "label": "任务名称",
      "rules": [],
      "key": "jobName",
      "props": {
        "placeholder": "请选择任务名称",
        "clearable": true,
        filterable: true,
        multiple: true,
        "collapse-tags": true,
        "collapse-tags-tooltip": true,
        "max-collapse-tags": 2,
        "options": jobNames
      }
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
      "label": "任务状态",
      "rules": [],
      "key": "jobState",
      "props": {
        "placeholder": "请选择任务状态",
        "clearable": true,
        filterable: true,
        "options": jobStates
      }
    },
    {
      type: 'el-date-picker',
      label: "时间",
      key: 'time',
      props: {
        type: "datetimerange",
        "range-separator": "至",
        clearable: true,
        "start-placeholder": "开始时间",
        "end-placeholder": "结束时间",
        shortcuts: shortcuts,
        "value-format": "YYYY-MM-DD HH:mm:ss",
        "disabled-date": (d) => {
          if (d < new Date()) {
            return false;
          }
          return true;
        }
      },
      selOptionType: '',
    },
    {
      "type": "el-input-number",
      "label": "开始百分比",
      "key": "startPercent",
      "default":0,
      "props": {
        "min": 0,
        "max": 100,
        "step": 1,
        "controls": true
      }
    },
    {
      "type": "el-input-number",
      "label": "结束百分比",
      "key": "endPercent",
      "default": 100,
      "props": {
        "min": 0,
        "max": 100,
        "step": 1,
        "controls": true
      }
    },
  ])

  function getJobQueryParamDict() {
    taskCenterApi.getJobQueryParamDict().then(res => {
      if (res.result == true) {
        let d = res.data
        let resNames = d.jobNames || []
        let resStates = d.jobStates || []

        jobNames.value = []
        resNames.forEach(item => {
          jobNames.value.push({
            label: item.value,
            value: item.key,
          })
        })
        jobStates.value = []
        resStates.forEach(item => {
          jobStates.value.push({
            label: item.value,
            value: item.key,
          })
        })
      } else {
        ElMessage.error(res.errorMsg)
      }
    }).finally(() => {
      pageShow.value = true
    })
  }

  getJobQueryParamDict()

  function getList(data) {
    return taskCenterApi.list(data)
  }

  const TabelContainer = ref(null)

  function searchEvent(data) {
    let arr = data.time || []

    if (arr.length == 2) {
      data.begTime = timeToUtcTime(arr[0])
      data.endTime = timeToUtcTime(arr[1])
    } else {
      data.begTime = ''
      data.endTime = ''
    }
    TabelContainer.value.filterEvent(data);
  }

  function resetEvent(data) {
    searchEvent(data)
  }


  let vmsDialogVisible = ref(false)
  const taskInfoVms = ref({})
  let dialogVisible = ref(false)
  const detailData = ref({})

  let taskDetailTimer = null

  // 查询主机列表
  function showVms(item, showLoading = true) {
    if (showLoading) {
      pageLoading.value = true
    }
    let params = {"planId":item.planId};
    taskCenterApi.listByPlanId(params).then(res => {
      // taskCenterApi.getjobdetail({planId: 'f27b9fb4ed704f0087fd908cf2106c91'}).then(res => {
      if (res.result == true) {
        taskInfoVms.value = res.data.vms;
        console.log(taskInfoVms.value)
        vmsDialogVisible.value = true
      } else {
        ElMessage.error(res.errorMsg)
        pageLoading.value = false
      }
    }).finally(() => {
      pageLoading.value = false
    })
  }

  const vmInstanceStateOptions = [
    {"label": "已关闭", "value": 0},
    {"label": "运行中", "value": 1},
    {"label": "销毁中", "value": -10},
    {"label": "已销毁", "value": -1},
    {"label": "未知", "value": -99}
  ]

  function vmInstanceStateToStr(state, maintenanceMode) {
    let option = vmInstanceStateOptions.find(item => {
      return item.value === state
    }) || {}
    let stateStr = option.label || state
    if (maintenanceMode == '1') {
      stateStr += '(维护)'
    }
    return stateStr;
  }

  // 是否需要显示VM列表
  function needShowVms(item) {
    var opType = item.operationType;
    console.log("opType=" + opType)
    return opType == 'scaleout' || opType == 'scalein' || opType == 'clearvms' || opType == 'scaleoutEvictVm';
  }

  // 查询任务详情
  function showDetail(item, showLoading = true) {
    if (showLoading) {
      pageLoading.value = true
    }
    taskCenterApi.getjobdetail({planId: item.planId}).then(res => {
    // taskCenterApi.getjobdetail({planId: 'f27b9fb4ed704f0087fd908cf2106c91'}).then(res => {
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
              showDetail(item, false)
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

  onBeforeUnmount(() => {
    taskDetailClosed()
  })
</script>

<style lang="stylus" scoped type="text/stylus">
  .index {
    .cluster-info {
      .cluster-name {
        font-size 14px;
      }

      .cluster-id {
        font-size 12px;
      }
    }
    .task-info {
      .task-name {
        font-size 14px;
      }

      .task-id {
        font-size 12px;
      }
    }

    >>> .center-dialog {

    }


    .vm-group-vms-list {
      display flex;
      flex-direction column;

      .flex-row {
        display flex;
        align-items flex-start;
        padding-bottom 16px;

        .detail-item {
          flex 1;
          display flex;
          align-items flex-start;
          overflow hidden;

          .label {
            color #606266;
            text-align left;
            width 110px;
            font-size 14px;
            line-height 1.4;
          }

          .value {
            flex 1;
            font-size 14px;
            line-height 1.4;
            overflow hidden;
            word-break break-all;
            padding-right 15px;
          }
        }
      }

      .top-box {
        border 1px solid #ddd;
        padding-top 15px;
        padding-left 10px;
        margin-bottom 10px;

        .label {
          width 120px !important;
        }
      }

      .theader {
        td, th {
          background-color: #F8F8F8 !important;

          .cell {
            word-break break-word;
          }
        }
      }
    }

    .dialog-content {
      overflow-y auto;

      .el-form-item {
        //margin-bottom 30px;
      }

      .dv-count {
        display: flex;
        align-items: center;

        .unit-div {
          background-color: #f5f7fa;
          color: #909399;
          padding: 0 5px;
          box-shadow: 0 1px 0 0 #dcdfe6 inset, 0 -1px 0 0 #dcdfe6 inset, -1px 0 0 0 #dcdfe6 inset;
        }
      }

      .block-div {
        padding 10px 10px 8px;
        border 1px solid #ddd;

        .title {
          font-size 16px;
          padding 0px 0 5px;
          color black;
        }

        .limit-items {
          display flex;
          align-items center;
          justify-content space-between;

          .limit-item {
            display flex;
            align-items center;

            .number {
              color red;
            }
          }
        }

        .rule-div {
          padding-bottom 5px;

          .rule-top-row {
            display flex;
            align-items center;
            justify-content space-between;
            border-top 1px solid #ddd;
            margin-top 8px;
            padding-top 5px;

            .rule-type {
              font-size 14px;
              color black;
            }
          }

          .no-rule {
            text-align center;
            padding 20px;
            color #999;
          }

          .rules-table {
            margin-top 3px;
          }
        }
      }

      .detail-items {

        .flex-row {
          display flex;
          align-items flex-start;
          padding-bottom 16px;

          .detail-item {
            flex 1;
            display flex;
            align-items flex-start;
            overflow hidden;

            .label {
              color #606266;
              text-align left;
              width 180px;
              font-size 14px;
              line-height 1.4;
              padding-left 20px;
            }

            .value {
              flex 1;
              font-size 14px;
              line-height 1.4;
              overflow hidden;
              word-break break-all;
              padding-right 15px;
            }
          }
        }
      }
    }
  }
</style>
