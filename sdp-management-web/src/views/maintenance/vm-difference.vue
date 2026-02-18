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
                prop="statementId"
                label="比对任务ID"
                min-width="120"
            >
            </el-table-column>
            <el-table-column
                prop="regionName"
                label="数据中心"
                min-width="90"
            >
            </el-table-column>
            <el-table-column
              prop="startTime"
              label="开始时间"
              :formatter="columnTimeFormat"
              min-width="120"
            >
            </el-table-column>
            <el-table-column
              prop="finishTime"
              label="完成时间"
              :formatter="columnTimeFormat"
              min-width="120"
            >
            </el-table-column>
            <el-table-column prop="status" label="状态" min-width="120">
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="180">
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
    <router-view class="sub-content" />
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
  formatTimeYMD,
  timeToUtcTime,
} from "@/utils/format-time";

const route = useRoute();
const router = useRouter();

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
    type: "el-date-picker",
    label: "统计时间",
    key: "time",
    props: {
      type: "daterange",
      "range-separator": "至",
      clearable: true,
      "start-placeholder": "开始时间",
      "end-placeholder": "结束时间",
      shortcuts: shortcuts,
      "value-format": "YYYY-MM-DD",
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
    data.startDate = formatTimeYMD(arr[0]);
    data.endDate = formatTimeYMD(arr[1]);
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
  return OperationApi.getVmStatements(data);
}

function showDetail(item) {
  router.push({
    path: "/maintenance/vmdifference/differencedetail",
    query: {
      statementId: item.statementId,
    },
  });
}
</script>

<style scoped lang="stylus"></style>
