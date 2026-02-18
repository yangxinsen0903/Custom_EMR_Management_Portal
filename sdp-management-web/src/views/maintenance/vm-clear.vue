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
            <el-table-column prop="hostName" label="主机名" min-width="250">
              <template #default="scope">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="scope.row.vmName"
                >
                  <div class="one-line-value">
                    {{ scope.row.vmName }}
                  </div>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column
              prop="purchaseType"
              label="购买类型"
              min-width="60"
            >
              <template #default="scope">
                <span>{{ purchaseTypeToStr(scope.row.purchaseType) }}</span>
              </template>
            </el-table-column>
            <el-table-column
              prop="status"
              label="状态"
              min-width="60"
              :formatter="statusFormatter"
            >
            </el-table-column>
            <el-table-column
              prop="begSendRequestTime"
              label="发送删除时间"
              min-width="110"
            >
              <template #default="scope">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="formatTime(scope.row.begSendRequestTime)"
                >
                  <div class="one-line-value">
                    {{ formatTime(scope.row.begSendRequestTime) }}
                  </div>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column
              prop="getDeleteJobidTime"
              label="开始删除时间"
              min-width="110"
            >
              <template #default="scope">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="formatTime(scope.row.getDeleteJobidTime)"
                >
                  <div class="one-line-value">
                    {{ formatTime(scope.row.getDeleteJobidTime) }}
                  </div>
                </el-tooltip>
              </template> </el-table-column
            ><el-table-column
              prop="releaseFreezeTime"
              label="解除冻结时间"
              :formatter="columnTimeFormat"
              min-width="110"
            >
              <template #default="scope">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="formatTime(scope.row.releaseFreezeTime)"
                >
                  <div class="one-line-value">
                    {{ formatTime(scope.row.releaseFreezeTime) }}
                  </div>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="retryCount" label="重试次数" min-width="60">
            </el-table-column>
            <el-table-column
              prop="freezeCount"
              label="冻结次数"
              min-width="60"
            >
            </el-table-column>
            <el-table-column
              prop="createdTime"
              label="创建时间"
              :formatter="columnTimeFormat"
              min-width="110"
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
  { label: "冻结", value: -1 },
  { label: "未删除", value: 0 },
  { label: "删除请求发送中", value: 1 },
  { label: "删除中", value: 2 },
  { label: "删除完成", value: 3 },
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
    key: "hostName",
    props: { placeholder: "请输入主机名", clearable: true },
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

function statusFormatter(row) {
  switch (row.status) {
    case -1:
      return "冻结";
    case 0:
      return "未删除";
    case 1:
      return "删除请求发送中";
    case 2:
      return "删除中";
    case 3:
      return "删除完成";
    default:
      return "";
  }
}

const purchaseTypeOptions = [
  // {"label": "全部", "value": ""},
  { label: "按需", value: 1 },
  { label: "竞价", value: 2 },
];

function purchaseTypeToStr(type) {
  let option =
      purchaseTypeOptions.find((item) => {
        return item.value == type;
      }) || {};
  return option.label || "";
}

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

function getList(data) {
  return OperationApi.getClusterVMDelete(data);
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
