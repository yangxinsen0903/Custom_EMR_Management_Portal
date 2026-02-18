<template>
  <div class="index page-css list-css" v-loading="pageLoading">
    <Filters
        :filters="filters"
        @onSearch="searchEvent"
        @onReset="resetEvent"
    ></Filters>
    <div class="full-container">
      <ListContainer ref="TabelContainer" :listApiFunction="getList">
        <template #default="tableData">
          <el-table
              :height="tableData.data.tableHeight"
              :data="tableData.data.tableData"
              stripe
              header-row-class-name="theader"
              style="width: 100%"
          >
            <el-table-column
                prop="regionName"
                label="数据中心"
                min-width="120"
            ></el-table-column>
            <el-table-column
                prop="clusterName"
                label="集群名称"
                min-width="120"
            >
              <template #default="scope">
                <el-tooltip
                    popper-class="value-popper"
                    effect="light"
                    :content="scope.row.clusterName"
                >
                  <div class="one-line-value">
                    {{ scope.row.clusterName }}
                  </div>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="groupName" label="实例组名" min-width="120"></el-table-column>
            <el-table-column prop="vmName" label="vmName" min-width="120"></el-table-column>
            <el-table-column prop="hostName" label="HostName" min-width="120"></el-table-column>
            <el-table-column
                prop="purchaseType"
                label="购买类型"
                min-width="80"
            >
              <template #default="scope">
                <span>{{ purchaseTypeToStr(scope.row.purchaseType) }}</span>
              </template>
            </el-table-column>
            <el-table-column
                prop="eventType"
                label="事件类型"
                min-width="80"
            >
              <template #default="scope">
                <span>{{ eventTypeToStr(scope.row.eventType) }}</span>
              </template>
            </el-table-column>
            <el-table-column
                prop="state"
                label="状态"
                min-width="80"
            >
              <template #default="scope">
                <span>{{ stateFormatter(scope.row.state) }}</span>
              </template>
            </el-table-column>
            <el-table-column
                prop="triggerTime"
                label="事件触发时间"
                min-width="120"
                :formatter="columnTimeFormat"
            >
            </el-table-column>
            <el-table-column
              prop="finishTime"
              label="处理完成时间"
              :formatter="columnTimeFormat"
              min-width="120"
          >
          </el-table-column>
          </el-table>
        </template>
      </ListContainer>
    </div>
  </div>
</template>

<script setup>
import Filters from "@/components/list-comps/filters";
import ListContainer from "@/components/list-comps/container";
import { ref, reactive } from "vue";
import { shortcuts } from "../../components/js/shortcuts";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import OperationApi from "../../api/operations";
import taskCenterApi from "@/api/task-center";
import { columnTimeFormat, formatTime } from "@/utils/format-time";

const route = useRoute();
const router = useRouter();
const pageShow = ref(false);
const stateOptions = [
  { label: "初始化", value: 'INIT' },
  { label: "处理中", value: 'PROCESSING' },
  { label: "成功", value: 'SUCCESS' },
  { label: "失败", value: 'FAIL' },
];

const pageLoading = ref(false);
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
      optionsDefaultArgs: {type: 'SupportedRegionList'},
      optionsProps: {
        label: 'regionName',
        value: 'region',
      }
    },
  },
  {
    type: "el-input",
    label: "集群名称",
    key: "clusterName",
    props: { placeholder: "请输入集群名称", clearable: true },
  },
  {
    type: "el-input",
    label: "主机名",
    key: "vmName",
    props: { placeholder: "请输入主机名", clearable: true },
  },
  {
    type: "RemoteSelect",
    label: "状态",
    rules: [],
    key: "state",
    props: {
      placeholder: "请选择状态",
      clearable: true,
      filterable: true,
      options: stateOptions,
    },
  },
  {
    type: "el-date-picker",
    label: "时间",
    key: "time",
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
      },
    },
    selOptionType: "",
  },
]);

function stateFormatter(type) {
  let option =
      stateOptions.find((item) => {
        return item.value == type;
      }) || {};
  return option.label || "";
}

const purchaseTypeOptions = [
  // {"label": "全部", "value": ""},
  { label: "按需", value: 'OnDemand' },
  { label: "竞价", value: 'Spot' },
];

function purchaseTypeToStr(type) {
  let option =
      purchaseTypeOptions.find((item) => {
        return item.value == type;
      }) || {};
  return option.label || "";
}

const eventTypeOptions = [
  { label: "上线", value: 'ONLINE' },
  { label: "下线", value: 'OFFLINE' },
];

function eventTypeToStr(type) {
  let option =
      eventTypeOptions.find((item) => {
        return item.value == type;
      }) || {};
  return option.label || "";
}

const TabelContainer = ref(null);

function searchEvent(data) {
  let arr = data.time || [];

  if (arr.length === 2) {
    data.beginTime = formatTime(arr[0]);
    data.endTime = formatTime(arr[1]);
  } else {
    data.beginTime = "";
    data.endTime = "";
  }

  TabelContainer.value.filterEvent(data);
}

function resetEvent(data) {
  searchEvent(data);
}

function getList(data) {
  return OperationApi.getVmEventList(data);
}
</script>

<style scoped lang="stylus">
.one-line-value {
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
