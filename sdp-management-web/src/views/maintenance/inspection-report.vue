<template>
  <div class="inspection-report page-css list-css" v-loading="pageLoading">
    <Filters
      :filters="filters"
      @onSearch="searchEvent"
      @onReset="resetEvent"
    ></Filters>
    <div class="full-container">
      <div class="scroll-div">
        <div class="item-div" v-for="item in listData">
          <div class="date-row">{{ formatTimeYMD(item.reportDate) }}</div>
          <div class="cont-div">
            <div class="table-div">
              <div class="title-div">
                任务执行统计时间：{{
                  formatTime(item.planReport.beginTime) + " ~ " + formatTime(item.planReport.endTime)
                }}
              </div>
              <el-table
                :data="item.planReport.items"
                border
                header-row-class-name="theader"
              >
                <el-table-column
                  prop="operationName"
                  label="操作类型"
                  min-width="90"
                />
                <el-table-column
                  prop="totalCount"
                  label="总数"
                  min-width="70"
                />
                <el-table-column
                  prop="successCount"
                  label="成功次数"
                  min-width="90"
                />
                <el-table-column
                  prop="failureCount"
                  label="执行失败次数"
                  min-width="120"
                />
                <el-table-column
                  prop="timeoutCount"
                  label="超时失败"
                  min-width="90"
                />
                <el-table-column
                  prop="successRate"
                  label="成功率"
                  min-width="70"
                />
              </el-table>
            </div>
            <div class="table-div">
              <div class="title-div">
                缩容失败数量统计：{{
                  formatTime(item.scaleFailReport.beginTime) +
                  " ~ " +
                  formatTime(item.scaleFailReport.endTime)
                }}
              </div>
              <el-table
                :data="item.scaleFailReport.items"
                border
                header-row-class-name="theader"
              >
                <el-table-column
                  prop="taskName"
                  label="任务名称"
                  min-width="90"
                />
                <el-table-column
                  prop="purchaseType"
                  label="类型(按需/竞价)"
                  min-width="140"
                />
                <el-table-column
                  prop="taskCount"
                  label="任务数量"
                  min-width="90"
                />
                <el-table-column prop="vmCount" label="VM数量" min-width="90" />
                <el-table-column
                  prop="cpuCount"
                  label="CPU核数"
                  min-width="90"
                />
              </el-table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {columnTimeFormat, formatTime, formatTimeYMD} from "@/utils/format-time";
import Filters from "@/components/list-comps/filters.vue";
import { useRoute, useRouter } from "vue-router";
import { ref, onMounted } from "vue";
import { shortcuts } from "@/components/js/shortcuts";
import OperationApi from "@/api/operations";
import { ElMessage } from "element-plus";

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

function searchEvent(data) {
  let arr = data.time || [];

  if (arr.length === 2) {
    data.beginReportDate = formatTimeYMD(arr[0]);
    data.endReportDate = formatTimeYMD(arr[1]);
  } else {
    data.beginReportDate = "";
    data.endReportDate = "";
  }

  getList(data);
}

function resetEvent(data) {
  searchEvent(data);
}

const listData = ref([]);

function getList(data) {
  OperationApi.checkReportDaily(data).then((res) => {
    if (res.result == true) {
      listData.value = res.data;
    } else {
      ElMessage.error(res.errorMsg);
    }
  });
}
</script>

<style scoped lang="stylus">
.inspection-report {
  .full-container {
    overflow hidden;
    .scroll-div {
      height 100%;
      overflow-y auto;
      .item-div {
        text-align left;
        padding-bottom 15px;
        margin-bottom 10px;
        border-bottom 1px solid #ddd;
        .date-row {
          padding 10px 0 5px;
          font-size 16px;
          font-weight bold;
        }
        .cont-div {
          display flex;
          .table-div {
            flex 1;
            .title-div {
              font-size 14px;
              padding 5px 0 12px;
            }
          }
          .table-div:nth-child(2) {
            margin-left 15px;
          }
        }
      }
    }
  }
}
</style>
