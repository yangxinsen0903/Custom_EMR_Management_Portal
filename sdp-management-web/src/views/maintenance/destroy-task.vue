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
            <el-table-column prop="destroyTaskId" label="销毁任务ID" min-width="160"></el-table-column>
            <el-table-column prop="clusterName" label="集群名称" min-width="100"></el-table-column>
            <el-table-column prop="clusterId" label="集群ID" min-width="160"></el-table-column>
            <el-table-column prop="isWhiteAddr" label="销毁白名单" width="100">
              <template #default="scope">
                {{ scope.row.isWhiteAddr == 1 ? '是' : '否' }}
              </template>
            </el-table-column>
            <el-table-column prop="destroyStatus" label="销毁状态" width="100">
              <template #default="scope">
                {{ stateToStr(scope.row.destroyStatus) }}
              </template>
            </el-table-column>
            <el-table-column
                prop="startDestroyTime"
                label="开始销毁时间"
                :formatter="columnTimeFormat"
                min-width="120">
            </el-table-column>
            <el-table-column
                prop="endDestroyTime"
                label="结束销毁时间"
                :formatter="columnTimeFormat"
                min-width="120">
            </el-table-column>
            <el-table-column
                prop="createdTime"
                label="创建时间"
                :formatter="columnTimeFormat"
                min-width="120">
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="120">
              <template #default="scope">
                <div>
                  <el-button type="primary" text @click="retryActivity(scope.row)" v-if="scope.row.destroyStatus == 0">重新尝试</el-button>
                  <el-button type="warning" text @click="cancelTask(scope.row)" v-if="scope.row.destroyStatus == 1">取消销毁</el-button>
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
import Filters from "@/components/list-comps/filters";
import ListContainer from "@/components/list-comps/container";
import { ref, reactive } from "vue";
import { shortcuts } from "../../components/js/shortcuts";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import OperationApi from "../../api/operations";
import taskCenterApi from "@/api/task-center";
import { columnTimeFormat, formatTime } from "@/utils/format-time";
import operation from "../../api/operations";

const route = useRoute();
const router = useRouter();

const stateOptions = [
  {"label": "销毁失败", "value": '0'},
  {"label": "待销毁", "value": '1'},
  {"label": "销毁中", "value": '2'},
  {"label": "已销毁", "value": '3'},
  {"label": "任务已取消", "value": '4'},
]

function stateToStr(state) {
  let option = stateOptions.find(item => {
    return item.value == state
  }) || {}
  return option.label || state
}

const pageLoading = ref(false);
const filters = ref([
  {
    type: "el-input",
    label: "集群名称",
    key: "clusterName",
    props: { placeholder: "请输入集群名称", clearable: true },
  },
  {
    type: "RemoteSelect",
    label: "销毁状态",
    rules: [],
    key: "destroyStatus",
    props: {
      placeholder: "请选择销毁状态",
      clearable: true,
      filterable: true,
      options: stateOptions,
    },
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

function getList(data) {
  return OperationApi.queryDestroyTask(data);
}

function retryActivity(item) {
  ElMessageBox.confirm(`您确定重新尝试销毁任务吗？`, '销毁任务', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    pageLoading.value = true
    operation.retryActivity({
      clusterId: item.clusterId
    }).then(res => {
      if (res.result == true) {
        ElMessage.success('销毁任务已重试！')
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    }).finally(() => {
      pageLoading.value = false
    })
  })
}

function cancelTask(item) {
  ElMessageBox.confirm(`您确定重置销毁限流中的任务吗？`, '重置销毁任务', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    pageLoading.value = true
    operation.cancelTask({
      clusterId: item.clusterId
    }).then(res => {
      if (res.result == true) {
        ElMessage.success('已重置销毁限流中的任务！')
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    }).finally(() => {
      pageLoading.value = false
    })
  })
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
