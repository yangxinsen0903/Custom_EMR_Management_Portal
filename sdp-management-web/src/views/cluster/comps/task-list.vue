/**Created by liaoyingchao on 12/25/22.*/

<template>
  <div class="page-css list-css vm-list">
    <Filters :labelWidth="'80px'" :filters="filters" @onSearch="searchEvent" @onReset="resetEvent"></Filters>
    <div class="full-container">
      <ListContainer ref="TabelContainer" :listApiFunction="getList">
        <template #default="tableData">
          <el-table
                  :height="tableData.data.tableHeight"
                  :data="tableData.data.tableData"
                  stripe
                  border
                  header-row-class-name="theader"
                  style="width: 100%">
            <el-table-column
                    prop="operationType"
                    label="伸缩方式"
                    width="130">
              <template #default="scope">
                {{ operationTypeToStr(scope.row) }}
              </template>
            </el-table-column>
            <el-table-column
                    prop="afterScalingCount"
                    label="伸缩期望"
                    width="130">
              <template #default="scope">
                <div style="white-space: pre-wrap;">
                {{ scalingCountToStr(scope.row)}}
                  </div>
              </template>
            </el-table-column>
            <el-table-column
                    prop="state"
                    label="任务状态"
                    width="100">
              <template #default="scope">
                {{ stateToStr(scope.row.state) }}
              </template>
            </el-table-column>
            <el-table-column
                    prop="afterScalingCount"
                    label="伸缩结果"
                    width="130">
              <template #default="scope">
                <div style="white-space: pre-wrap;">
                  {{ scalingCountToStr(scope.row)}}
                </div>
              </template>
            </el-table-column>
            <el-table-column
                    prop="begTime"
                    label="开始时间"
                    :formatter="columnTimeFormat"
                    min-width="108">
            </el-table-column>
            <el-table-column
                    prop="endTime"
                    label="结束时间"
                    :formatter="columnTimeFormat"
                    min-width="108">
            </el-table-column>
            <el-table-column
                    prop="remark"
                    label="备注"
                    min-width="150">
            </el-table-column>
            <el-table-column
                    label="实例列表"
                    width="85">
              <template #default="scope">
                <div style="text-align: center;">
                  <el-button :icon="Document" text @click="showVms(scope.row)"></el-button>
                </div>
              </template>
            </el-table-column>
            <el-table-column
                    label="执行情况"
                    width="85">
              <template #default="scope">
                <div style="text-align: center;">
                  <el-button :icon="Document" text @click="showTaskDetail(scope.row)"></el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </ListContainer>
    </div>
  </div>
</template>

<script setup>
import {Document} from '@element-plus/icons-vue'
import Filters from '@/components/list-comps/filters'
import ListContainer from '@/components/list-comps/container'
import {ref, reactive, toRefs, defineProps, defineEmits, computed} from "vue"
import {useRouter} from 'vue-router'
import taskCenterApi from '@/api/task-center'
import {ElMessage, ElMessageBox} from 'element-plus';
import clusterApi from "../../../api/cluster";
import {columnTimeFormat} from "@/utils/format-time";
import {shortcuts} from "../../../components/js/shortcuts";
import {timeToUtcTime} from "../../../utils/format-time";

const emit = defineEmits(['showVms', 'showTaskDetail']);

const props = defineProps({
  clusterId: {
    type: String,
    default: ''
  },
  groupName: {
    type: String,
    default: ''
  }
});
const {clusterId, groupName} = toRefs(props)

const stateOptions = [
  {"label": "全部", "value": ""},
  {"label": "任务创建", "value": 0},
  {"label": "任务执行中", "value": 1},
  {"label": "任务完成", "value": 2},
  {"label": "任务失败", "value": -9},
]

function stateToStr(state) {
  let option = stateOptions.find(item => {
    return item.value === state
  }) || {}
  return option.label || state
}

const operationTypeOptions = [
  {"label": "全部", "value": '', args: {scalingType: '', opertionType: ''}},
  {"label": "手动扩容", "value": '1', args: {scalingType: '1', opertionType: '1'}},
  {"label": "手动缩容", "value": '2', args: {scalingType: '2', opertionType: '1'}},
  {"label": "弹性扩容", "value": '3', args: {scalingType: '1', opertionType: '2'}},
  {"label": "弹性缩容", "value": '4', args: {scalingType: '2', opertionType: '2'}},
  {"label": "手动创建", "value": '5', args: {scalingType: '1', opertionType: '5'}},
  {"label": "手动删除", "value": '6', args: {scalingType: '2', opertionType: '6'}},
  {"label": "删除扩容实例", "value": '7', args: {scalingType: '2', opertionType: '8'}},
  {"label": "竞价扩容", "value": '8', args: {scalingType: '1', opertionType: '7'}},
  {"label": "竞价缩容", "value": '9', args: {scalingType: '2', opertionType: '7'}},
  {"label": "磁盘扩容", "value": '10', args: {scalingType: '3', opertionType: '1'}},
  {"label": "补全驱逐VM", "value": '11', args: {scalingType: '1', opertionType: '9'}},
  {"label": "pv2磁盘调整", "value": '12', args: {scalingType: '4', opertionType: '1'}},
]

function operationTypeToArgs(params) {
  let tmpOpertiionType = params.tmpOpertiionType || ''
  let otItem = operationTypeOptions.find(item => {
    return item.value == tmpOpertiionType
  }) || {args: {}}

  params.scalingType = otItem.args.scalingType || ''
  params.operatiionType = otItem.args.opertionType || ''
  return params
}

function scalingCountToStr(row){
  if (row.scalingType =='4'){
    return 'IOPS:'+row.beforeScalingCount +"\n"+"吞吐量:"+row.afterScalingCount+"MB"
  }else {
    return row.afterScalingCount + (row.scalingType == 3 ? 'G' : '台')
  }
}

function operationTypeToStr(row) {
  let option = operationTypeOptions.find(item => {
    return item.args.scalingType == row.scalingType && item.args.opertionType == row.operatiionType
  }) || {}
  return option.label || ''
}

const filters = ref([
  {
    "type": "RemoteSelect",
    "label": "伸缩方式",
    "rules": [],
    "key": "tmpOpertiionType",
    "props": {
      "placeholder": "请选择伸缩方式",
      "clearable": true,
      "options": operationTypeOptions
    }
  },
  {
    "type": "RemoteSelect",
    "label": "任务状态",
    "rules": [],
    "key": "state",
    "props": {
      "placeholder": "请选择任务状态",
      "clearable": true,
      "options": stateOptions
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
  }
])

function getList(data) {
  data.inQueue = 0
  data.clusterId = clusterId.value
  data.groupName = groupName.value

  data = operationTypeToArgs(data)

  return clusterApi.scalingLog(data)
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

function showVms(item) {
  emit('showVms', item)
}

function showTaskDetail(item) {
  emit('showTaskDetail', item)
}
</script>

<style lang="stylus" scoped type="text/stylus">
.vm-list {
  overflow hidden;
  padding 0px 0 0px;

  >>> .center-dialog {

  }
}
</style>
