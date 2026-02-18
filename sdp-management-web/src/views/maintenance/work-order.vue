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
                prop="clusterId"
                label="集群ID"
                min-width="180"
            ></el-table-column>
            <el-table-column
                prop="clusterName"
                label="集群名称"
                min-width="120"
            ></el-table-column>
            <el-table-column
                prop="requestType"
                label="请求类型"
                min-width="80"
            >
              <template #default="scope">
                <span>{{ requestTypeToStr(scope.row.requestType) }}</span>
              </template>
            </el-table-column>
            <el-table-column
                prop="approvalState"
                label="审核结果"
                min-width="80"
            >
              <template #default="scope">
                <span>{{ approvalToStr(scope.row.approvalState) }}</span>
              </template>
            </el-table-column>
            <el-table-column
                prop="approvalResult"
                label="审核返回结果"
                min-width="120"
            >
              <template #default="scope">
                <el-tooltip
                    popper-class="value-popper"
                    effect="light"
                    :content="scope.row.approvalResult"
                >
                  <div class="one-line-value">
                    {{ scope.row.approvalResult }}
                  </div>
                </el-tooltip>
              </template>
            </el-table-column>

            <el-table-column
                prop="createdTime"
                label="创建时间"
                :formatter="columnTimeFormat"
                min-width="110"
            ></el-table-column>
            <el-table-column
                prop="modifiedTime"
                label="修改时间"
                :formatter="columnTimeFormat"
                min-width="110"
            ></el-table-column>
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
  { label: "创建集群", value: 'create' },
  { label: "销毁集群", value: 'destory' },
];

function requestTypeToStr(type) {
  let option =
      stateOptions.find((item) => {
        return item.value == type;
      }) || {};
  return option.label || "";
}

const approvalOptions = [
  { label: "初始", value: 'init' },
  { label: "通过", value: 'agree' },
  { label: "驳回", value: 'refuse' },
];

function approvalToStr(type) {
  let option =
      approvalOptions.find((item) => {
        return item.value == type;
      }) || {};
  return option.label || "";
}

const pageLoading = ref(false);
const filters = ref([
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
    type: "RemoteSelect",
    label: "请求类型",
    rules: [],
    key: "requestType",
    props: {
      placeholder: "请选择请求类型",
      clearable: true,
      filterable: true,
      options: stateOptions,
    },
  },
  {
    type: "RemoteSelect",
    label: "审核结果",
    rules: [],
    key: "approvalState",
    props: {
      placeholder: "请选择审核结果",
      clearable: true,
      filterable: true,
      options: approvalOptions,
    },
  },
]);

const TabelContainer = ref(null);

function searchEvent(data) {
  TabelContainer.value.filterEvent(data);
}

function resetEvent(data) {
  searchEvent(data);
}

function getList(data) {
  return OperationApi.queryorderapproval(data);
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
