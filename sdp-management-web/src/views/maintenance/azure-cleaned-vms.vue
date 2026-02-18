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
<!--            <el-table-column-->
<!--                prop="id"-->
<!--                label="ID"-->
<!--                min-width="100"-->
<!--            ></el-table-column>-->
            <el-table-column
                prop="clusterId"
                label="集群ID"
                min-width="120"
            ></el-table-column>
<!--            <el-table-column-->
<!--              prop="clusterName"-->
<!--              label="集群名称"-->
<!--              min-width="150"-->
<!--            >-->
<!--              <template #default="scope">-->
<!--                <el-tooltip-->
<!--                  popper-class="value-popper"-->
<!--                  effect="light"-->
<!--                  :content="scope.row.clusterName"-->
<!--                >-->
<!--                  <div class="one-line-value">-->
<!--                    {{ scope.row.clusterName }}-->
<!--                  </div>-->
<!--                </el-tooltip>-->
<!--              </template>-->
<!--            </el-table-column>-->
            <el-table-column
                prop="clusterName"
                label="集群名称"
                min-width="120"
            ></el-table-column>
            <el-table-column
                prop="vmName"
                label="实例名称"
                min-width="120"
            ></el-table-column>
            <el-table-column
                prop="uniqueId"
                label="唯一ID"
                min-width="120"
            ></el-table-column>
            <el-table-column
                prop="privateIp"
                label="privateIp"
                min-width="120"
            ></el-table-column>
            <el-table-column
                prop="zone"
                label="可用区"
                min-width="120"
            ></el-table-column>
            <el-table-column
                prop="priority"
                label="优先级"
                min-width="120"
            ></el-table-column>
            <el-table-column
                prop="vmRole"
                label="伸缩实例组角色"
                min-width="130"
            ></el-table-column>
            <el-table-column
                prop="groupName"
                label="实例组名称"
                min-width="120"
            ></el-table-column>
            <el-table-column
                prop="invokeCount"
                label="调用接口次数"
                min-width="110"
            ></el-table-column>
            <el-table-column
              prop="vmCreatedTime"
              label="vm创建时间"
              min-width="110"
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
            <el-table-column
              prop="createdTime"
              label="任务执行时间"
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
    "type": "el-input",
    "label": "实例名称",
    "rules": [],
    "key": "vmName",
    "props": {"placeholder": "请输入实例名称", "clearable": true}
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
  return OperationApi.getAzureCleanedVms(data);
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
