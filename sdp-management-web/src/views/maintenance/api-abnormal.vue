<template>
  <div class="api-abnormal page-css list-css" v-loading="pageLoading">
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
            <el-table-column prop="id" label="id" width="80"></el-table-column>
            <el-table-column
                prop="regionName"
                label="数据中心"
                min-width="120"
            ></el-table-column>
            <el-table-column
              prop="apiName"
              label="接口名称"
              min-width="120"
            ></el-table-column>
            <el-table-column prop="apiUrl" label="接口url" min-width="200">
              <template #default="scope">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="scope.row.apiUrl"
                >
                  <div class="one-line-value">
                    {{ scope.row.apiUrl }}
                  </div>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="failedType" label="失败类型" min-width="60">
              <template #default="scope">
                {{ stateToStr(scope.row.failedType) }}
              </template>
            </el-table-column>
            <el-table-column
              prop="timeOut"
              label="超时时间"
              min-width="60"
            ></el-table-column>
            <el-table-column
              prop="createdTime"
              :formatter="columnTimeFormat"
              label="创建时间"
              min-width="110"
            ></el-table-column>
            <el-table-column fixed="right" label="操作" width="120">
              <template #default="scope">
                <div>
                  <el-button type="primary" text @click="showDetail(scope.row)"
                    >查看详情</el-button
                  >
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
      title="异常信息"
      width="800"
      destroy-on-close
    >
      <div class="scroll-div">
        <div class="cont-title">关键参数</div>
        <div class="cont-value">
          {{ logDetail.apiKeyParam }}
        </div>
        <div class="cont-title">异常信息</div>
        <div class="cont-value">
          {{ logDetail.exceptionInfo }}
        </div>
      </div>
    </el-dialog>
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
import {
  columnTimeFormat,
  formatTime,
  formatTimeYMD,
  timeToUtcTime,
} from "@/utils/format-time";

const route = useRoute();
const router = useRouter();
const nameOptions = [
  { label: "申请资源", value: "SDP_YARN" },
  { label: "销毁单个主机", value: "AZURE_SDP" },
];

const stateOptions = [
  { label: "超时", value: 1 },
  { label: "异常", value: 2 },
];

function stateToStr(state) {
  let option =
    stateOptions.find((item) => {
      return item.value == state;
    }) || {};
  return option.label || state;
}

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
    label: "接口名称",
    key: "apiName",
    props: { placeholder: "请输入接口名称", clearable: true },
  },
  {
    type: "RemoteSelect",
    label: "失败类型",
    rules: [],
    key: "failedType",
    props: {
      placeholder: "请选择失败类型",
      clearable: true,
      filterable: true,
      options: stateOptions,
    },
  },
  {
    type: "el-date-picker",
    label: "统计时间",
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
    data.beginReportDate = formatTime(arr[0]);
    data.endReportDate = formatTime(arr[1]);
  } else {
    data.beginReportDate = "";
    data.endReportDate = "";
  }

  delete data.time;

  TabelContainer.value.filterEvent(data);
}

function resetEvent(data) {
  searchEvent(data);
}

function getList(data) {
  return OperationApi.getfailedlogs(data);
}

const dialogVisible = ref(false);
const logDetail = ref({});

function showDetail(item) {
  OperationApi.getfailedlogbyid({ id: item.id }).then((res) => {
    if (res.result == true) {
      logDetail.value = res.data;
      dialogVisible.value = true;
    } else {
      ElMessage.error(res.errorMsg);
    }
  });
}
</script>

<style scoped lang="stylus">
.one-line-value {
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.api-abnormal {

  >>>.center-dialog {
    .scroll-div {
      max-height 60vh;
      overflow-y auto;
      .cont-title {
        padding-bottom 12px;
        font-size 14px;
      }
      .cont-value {
        padding 10px;
        font-size 13px;
        line-height 19px;
        border 1px solid #ddd;
        margin-bottom 15px;
      }
    }
  }
}
</style>
