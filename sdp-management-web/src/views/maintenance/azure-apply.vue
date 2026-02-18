<template>
  <div class="index page-css list-css" v-loading="pageLoading" v-if="pageShow">
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
            <el-table-column prop="id" label="id" min-width="40">
            </el-table-column>
            <el-table-column
                prop="regionName"
                label="数据中心"
                min-width="120"
            ></el-table-column>
            <el-table-column
              prop="clusterName"
              label="集群名称"
              min-width="130"
            >
            </el-table-column>
            <el-table-column prop="planId" label="任务ID" min-width="140">
              <template #default="scope">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="scope.row.planId"
                >
                  <div class="one-line-value">
                    {{ scope.row.planId }}
                  </div>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="jobId" label="Azure申请jobId" min-width="250">
              <template #default="scope">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="scope.row.jobId"
                >
                  <div class="one-line-value">
                    {{ scope.row.jobId }}
                  </div>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column
              prop="status"
              label="状态"
              min-width="40"
              :formatter="statusFormatter"
            >
            </el-table-column>
            <el-table-column
              prop="createdTime"
              label="创建时间"
              :formatter="columnTimeFormat"
              min-width="80"
            >
              <template #default="scope">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="formatTime(scope.row.createdTime)"
                >
                  <div class="one-line-value">
                    {{ formatTime(scope.row.createdTime) }}
                  </div>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column
              prop="modifiedTime"
              label="修改时间"
              :formatter="columnTimeFormat"
              min-width="80"
            >
              <template #default="scope">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="formatTime(scope.row.modifiedTime)"
                >
                  <div class="one-line-value">
                    {{ formatTime(scope.row.modifiedTime) }}
                  </div>
                </el-tooltip>
              </template>
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
const jobNames = ref([]);
const jobStates = ref([]);
const pageShow = ref(false);
const stateOptions = [
  { label: "待处理", value: 0 },
  { label: "已处理", value: 1 },
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
    label: "任务ID",
    key: "planId",
    props: { placeholder: "请输入任务ID", clearable: true },
  },
  {
    type: "combo-box",
    key: "clusterName",
    props: {},
    selOptionType: "",
    options: [
      {
        type: "el-input",
        label: "集群名称",
        rules: [],
        key: "clusterName",
        props: { placeholder: "请输入集群名称", clearable: true },
      },
      {
        type: "el-input",
        label: "集群ID",
        rules: [],
        key: "clusterId",
        props: { placeholder: "请输入集群ID", clearable: true },
      },
    ],
  },
  {
    type: "el-input",
    label: "Azure申请JobId",
    key: "jobId",
    props: { placeholder: "请输入Azure申请JobId", clearable: true },
  },
  {
    type: "RemoteSelect",
    label: "主机状态",
    rules: [],
    key: "status",
    props: {
      placeholder: "请选择主机状态",
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

const TabelContainer = ref(null);

function searchEvent(data) {
  let arr = data.time || [];

  if (arr.length === 2) {
    data.startDate = formatTime(arr[0]);
    data.endDate = formatTime(arr[1]);
  } else {
    data.startDate = "";
    data.endDate = "";
  }

  TabelContainer.value.filterEvent(data);
}

function resetEvent(data) {
  searchEvent(data);
}

function statusFormatter(row) {
  switch (row.status) {
    case 0:
      return "待处理";
    case 1:
      return "已处理";
    default:
      return "";
  }
}

function getList(data) {
  return OperationApi.getVmReqJobFailed(data);
}

function getJobQueryParamDict() {
  taskCenterApi
    .getJobQueryParamDict()
    .then((res) => {
      if (res.result == true) {
        let d = res.data;
        let resNames = d.jobNames || [];
        let resStates = d.jobStates || [];

        jobNames.value = [];
        resNames.forEach((item) => {
          jobNames.value.push({
            label: item.value,
            value: item.key,
          });
        });
        jobStates.value = [];
        resStates.forEach((item) => {
          jobStates.value.push({
            label: item.value,
            value: item.key,
          });
        });
      } else {
        ElMessage.error(res.errorMsg);
      }
    })
    .finally(() => {
      pageShow.value = true;
    });
}

getJobQueryParamDict();
</script>

<style scoped lang="stylus">
.one-line-value {
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
