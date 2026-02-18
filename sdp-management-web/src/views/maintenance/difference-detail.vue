<template>
  <div class="difference-detail page-css list-css" v-loading="pageLoading">
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
            :cell-style="cellStyle"
          >
            <el-table-column
              prop="createdTime"
              label="比对时间"
              :formatter="columnTimeFormat"
              min-width="120"
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
              prop="clusterName"
              label="集群名"
              min-width="130"
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
            <el-table-column prop="vmName" label="vmName" min-width="150">
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
            <el-table-column prop="clusterId" label="集群ID" min-width="120">
              <template #default="scope">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="scope.row.clusterId"
                >
                  <div class="one-line-value">
                    {{ scope.row.clusterId }}
                  </div>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="hostName" label="hostName" min-width="120">
              <template #default="scope">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="scope.row.hostName"
                >
                  <div class="one-line-value">
                    {{ scope.row.hostName }}
                  </div>
                </el-tooltip>
              </template>
            </el-table-column>

            <el-table-column prop="sdpResult" label="SDP" min-width="60">
            </el-table-column>
            <el-table-column prop="yarnResult" label="Yarn" min-width="60">
            </el-table-column>
            <el-table-column prop="azureResult" label="Azure" min-width="60">
            </el-table-column>

            <el-table-column prop="groupName" label="实例组" min-width="80">
            </el-table-column>
            <el-table-column
              prop="purchaseType"
              label="购买类型"
              min-width="70"
            >
              <template #default="scope">
                <span>{{ purchaseTypeToStr(scope.row.purchaseType) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="sku" label="sku" min-width="130">
            </el-table-column>
            <el-table-column prop="cpu" label="cpu" min-width="50">
            </el-table-column>
            <el-table-column prop="memory" label="内存" min-width="50">
            </el-table-column>
            <el-table-column prop="subnet" label="子网" min-width="120">
              <template #default="scope">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="scope.row.subnet"
                >
                  <div class="one-line-value">
                    {{ scope.row.subnet }}
                  </div>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="zoneName" label="可用区" min-width="80">
            </el-table-column>
            <el-table-column
                prop="createdTime"
                label="主机创建时间"
                :formatter="columnTimeFormat"
                min-width="120"
            >
              <template #default="scope">
                <el-tooltip
                    popper-class="value-popper"
                    effect="light"
                    :content="formatTime(scope.row.vmCreatedTime)"
                >
                  <div class="one-line-value">
                    {{ formatTime(scope.row.vmCreatedTime) }}
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
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import OperationApi from "../../api/operations";
import { Loading } from "@element-plus/icons-vue";
import {
  columnTimeFormat, formatTime,
  formatTimeYMD,
  timeToUtcTime,
} from "@/utils/format-time";

const route = useRoute();
const router = useRouter();

const statementId = route.query.statementId || "";

const typeOptions = [
  { label: "SDP与YARN差异", value: "SDP_YARN" },
  { label: "SDP与Azure差异", value: "AZURE_SDP" },
  { label: "YARN与Azure差异", value: "AZURE_YARN" },
];

const purchaseTypeOptions = [
  { label: "按需", value: 1 },
  { label: "竞价", value: 2 },
];

const vmRolesOptions = [
  { label: "Ambari", value: "ambari" },
  { label: "Master", value: "master" },
  { label: "Core", value: "core" },
  { label: "Task", value: "task" },
]

// 查询时间段
const minuteBeforeOptions = [
  { label: "比对时间5分钟前创建", value: 5 },
  { label: "比对时间10分钟前创建", value: 10 },
  { label: "比对时间20分钟前创建", value: 20 },
  { label: "比对时间30分钟前创建", value: 30 },
  { label: "比对时间1小时前创建", value: 60 },
]

function purchaseTypeToStr(type) {
  let option =
    purchaseTypeOptions.find((item) => {
      return item.value == type;
    }) || {};
  return option.label || "";
}

const pageLoading = ref(false);
const filters = ref([
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
    label: "vmName",
    key: "hostName",
    props: { placeholder: "请输入vmName", clearable: true },
  },
  {
    type: "RemoteSelect",
    label: "对比类型",
    rules: [],
    key: "diffType",
    props: {
      placeholder: "请选择对比类型",
      clearable: true,
      filterable: true,
      options: typeOptions,
    },
  }, {
    type: "RemoteSelect",
    label: "主机角色",
    rules: [],
    key: "vmRoles",
    props: {
      placeholder: "请选择主机角色",
      clearable: true,
      filterable: true,
      multiple: true,
      options: vmRolesOptions,
    },
  },{
    type: "RemoteSelect",
    label: "购买类型",
    rules: [],
    key: "purchaseType",
    props: {
      placeholder: "请选择主机角色",
      clearable: true,
      filterable: true,
      options: purchaseTypeOptions,
    },
  },{
    type: "RemoteSelect",
    label: "VM创建时间",
    rules: [],
    key: "minuteBefore",
    props: {
      placeholder: "请选择主机创建时间",
      clearable: true,
      filterable: true,
      options: minuteBeforeOptions,
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

function cellStyle({ row, column }) {
  if (row.sdpResult == "Y" && column.label == "SDP") {
    return { background: "#7fb80e", color: "#fff" };
  } else if (row.sdpResult == "N" && column.label == "SDP") {
    return { background: "#ea66a6", color: "#fff" };
  }
  if (row.yarnResult == "Y" && column.label == "Yarn") {
    return { background: "#7fb80e", color: "#fff" };
  } else if (row.yarnResult == "N" && column.label == "Yarn") {
    return { background: "#ea66a6", color: "#fff" };
  }
  if (row.azureResult == "Y" && column.label == "Azure") {
    return { background: "#7fb80e", color: "#fff" };
  } else if (row.azureResult == "N" && column.label == "Azure") {
    return { background: "#ea66a6", color: "#fff" };
  }
}

function getList(data) {
  data.statementId = statementId;
  console.log(data)
  return OperationApi.getVmStatementDetails(data);
}
</script>

<style scoped lang="stylus">
.one-line-value {
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.difference-detail {

}
</style>
