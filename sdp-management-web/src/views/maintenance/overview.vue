<template>
  <div class="index">
    <div class="top-select">
      <div class="top-label">数据中心</div>
      <RemoteSelect v-model="selectRegion" placeholder="请选择数据中心" :options="dataCenter"
                    @change="regionChange"  v-if="dataCenter.length > 0"/>
    </div>
    <div class="box">
      <div class="vmBox">
        <div class="block-title-row">
          <div class="title">清理VM数量概况</div>
        </div>
        <div class="overview-items">
          <div class="item" v-for="item in VMItems">
            <div class="item-icon">
              <img :src="item.icon" />
            </div>
            <div class="item-number">{{ item.count }}</div>
            <div class="item-name">{{ item.name }}</div>
          </div>
        </div>
      </div>
      <div class="azureBox">
        <div class="block-title-row">
          <div class="title">Azure申请资源失败查询概况</div>
        </div>
        <div class="overview-items">
          <div class="item" v-for="item in AzureItems">
            <div class="item-icon">
              <img :src="item.icon" />
            </div>
            <div class="item-number">{{ item.count }}</div>
            <div class="item-name">{{ item.name }}</div>
          </div>
        </div>
      </div>
    </div>
    <div class="block">
      <div class="block-title-row">
        <div class="title">VM三方差异对比</div>
      </div>
      <div class="overview-items">
        <div class="item" v-for="item in overviewItems">
          <div class="item-icon">
            <img :src="item.icon" />
          </div>
          <div class="item-number">{{ item.count }}</div>
          <div class="item-name">{{ item.name }}</div>
        </div>
      </div>
    </div>
    <div class="tableArea">
      <div class="leftBox">
        <div class="title">
          任务执行统计时间：{{ beginTime + "~" + endTime }}
        </div>
        <div class="leftTable">
          <el-table :data="taskExecutionData" style="height: 400px" border>
            <el-table-column
              prop="operationName"
              label="操作类型"
              min-width="100"
            />
            <el-table-column prop="totalCount" label="总数" min-width="80" />
            <el-table-column
              prop="successCount"
              label="成功次数"
              min-width="100"
            />
            <el-table-column
              prop="failureCount"
              label="执行失败次数"
              min-width="120"
            />
            <el-table-column
              prop="timeoutCount"
              label="超时失败"
              min-width="100"
            />
            <el-table-column prop="successRate" label="成功率" min-width="80" />
          </el-table>
        </div>
      </div>
      <div class="rightBox">
        <div class="title">
          缩容失败数量统计：{{ failBeginTime + "~" + failEndTime }}
        </div>
        <div class="rightTable">
          <el-table :data="failNumData" style="height: 400px" border>
            <el-table-column prop="taskName" label="任务名称" min-width="100" />
            <el-table-column
              prop="purchaseType"
              label="类型(按需/竞价)"
              min-width="140"
            />
            <el-table-column
              prop="taskCount"
              label="任务数量"
              min-width="100"
            />
            <el-table-column prop="vmCount" label="VM数量" min-width="100" />
            <el-table-column prop="cpuCount" label="CPU核数" min-width="100" />
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from "vue";
import OperationApi from "../../api/operations";
import { ElMessage } from "element-plus";
import RemoteSelect from "@/components/base/remote-select/remote-select.vue";
import cluster from "@/api/cluster";

const VMItems = ref([
  {
    icon: require("@/assets/home/jqzs.png"),
    count: 0,
    name: "待处理",
  },
  {
    icon: require("@/assets/home/jqzs.png"),
    count: 0,
    name: "发送删除请求",
  },
  {
    icon: require("@/assets/home/jqzs.png"),
    count: 0,
    name: "删除中",
  },
  {
    icon: require("@/assets/home/jqzs.png"),
    count: 0,
    name: "已删除",
  },
  {
    icon: require("@/assets/home/jqzs.png"),
    count: 0,
    name: "已冻结",
  },
]);
const AzureItems = ref([
  {
    icon: require("@/assets/home/jqzs.png"),
    count: 0,
    name: "待处理",
  },
  {
    icon: require("@/assets/home/jqzs.png"),
    count: 0,
    name: "已处理",
  },
]);
const overviewItems = ref([
  {
    icon: require("@/assets/home/jqzs.png"),
    count: 0,
    name: "SDP数量",
  },
  {
    icon: require("@/assets/home/jqzs.png"),
    count: 0,
    name: "Yarn数量",
  },
  {
    icon: require("@/assets/home/jqzs.png"),
    count: 0,
    name: "Azure数量",
  },
  {
    icon: require("@/assets/home/jqzs.png"),
    count: 0,
    name: "SDP与Yarn差异",
  },
  {
    icon: require("@/assets/home/jqzs.png"),
    count: 0,
    name: "Azure与Yarn差异",
  },
  {
    icon: require("@/assets/home/jqzs.png"),
    count: 0,
    name: "SDP与Azure差异",
  },
]);
const taskExecutionData = ref([]);
const failNumData = ref([]);
const beginTime = ref("");
const endTime = ref("");
const failBeginTime = ref("");
const failEndTime = ref("");

function getVMItemsFn() {
  OperationApi.getAbnormalVmCleanSummary({region: selectRegion.value}).then((res) => {
    if (res.result == true) {
      VMItems.value[0].count = res.data.pending || 0;
      VMItems.value[1].count = res.data.requestDeleting || 0;
      VMItems.value[2].count = res.data.deleting || 0;
      VMItems.value[3].count = res.data.completed || 0;
      VMItems.value[4].count = res.data.frozen || 0;
    } else {
      ElMessage.error(res.errorMsg);
    }
  });
}

function getAzureItemsFn() {
  OperationApi.getAbnormalVmRetrySummary({region: selectRegion.value}).then((res) => {
    if (res.result == true) {
      AzureItems.value[0].count = res.data.pending;
      AzureItems.value[1].count = res.data.completed;
    } else {
      ElMessage.error(res.errorMsg);
    }
  });
}

function getOverviewItemsFn() {
  OperationApi.vmDiffStat({region: selectRegion.value}).then((res) => {
    if (res.result == true) {
      overviewItems.value[0].count = res.data.sdpVmCount || 0;
      overviewItems.value[1].count = res.data.yarnVmCount || 0;
      overviewItems.value[2].count = res.data.azureVmCount || 0;
      overviewItems.value[3].count = res.data.sdpYarnDiff || 0;
      overviewItems.value[4].count = res.data.azureYarnDiff || 0;
      overviewItems.value[5].count = res.data.azureSdpDiff || 0;
    } else {
      ElMessage.error(res.errorMsg);
    }
  });
}

function getTaskExecutionDataFn() {
  OperationApi.getPlanResultReport({region: selectRegion.value}).then((res) => {
    if (res.result == true) {
      beginTime.value = res.data.beginTime;
      endTime.value = res.data.endTime;
      taskExecutionData.value = res.data.records || [];
    } else {
      ElMessage.error(res.errorMsg);
    }
  });
}

function getFailNumData() {
  OperationApi.getScaleInFailureResultReport({region: selectRegion.value}).then((res) => {
    if (res.result == true) {
      failBeginTime.value = res.data.beginTime;
      failEndTime.value = res.data.endTime;
      failNumData.value = res.data.records || [];
    } else {
      ElMessage.error(res.errorMsg);
    }
  });
}

// 刷新页面数据
function refresh() {
  console.log("开始刷新运维管理Dashboard数据")
  getVMItemsFn();
  getAzureItemsFn();
  getOverviewItemsFn();
  getTaskExecutionDataFn();
  getFailNumData();
  console.log("刷新运维管理Dashboard数据完成")
}

// 自动刷新定时期
const refreshTimer = ref(null);

function refreshForTimer() {
  if (refreshTimer.value) {
    clearTimeout(refreshTimer.value)
  }
  refresh();
  refreshTimer.value = setTimeout(() => {
    refreshForTimer();
  }, 30000);
}

// 数据中心
const dataCenter = ref([{
  label: '数据加载中...',
  value: '数据加载中...',
}])
const selectRegion = ref('')

function getDataCenter() {
  cluster.getCurrentUserRegions().then(res => {
    if (res.result == true) {
      let arr = res.data || []

      if (arr.length) {
        selectRegion.value = arr[0].region

        dataCenter.value = []
        nextTick(() => {
          arr.forEach(item => {
            item.label = item.regionName
            item.value = item.region
            dataCenter.value.push(item)
          })
        })

        refreshForTimer()
      } else {
        ElMessage.info("没有获取到数据中心列表")
      }
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

function regionChange() {
  refreshForTimer()
}

onMounted(() => {
  getDataCenter()
})

onUnmounted(() => {
  if (refreshTimer.value) {
    clearTimeout(refreshTimer.value)
  }
  console.log("unmounted: " + refreshTimer.value)
})

</script>

<style scoped lang="stylus" type="text/stylus">
.index {
  width 100%;
  height 100%;
  overflow-y auto;
  display flex;
  flex-direction column;

  .top-select {
    margin-bottom 10px;
    display flex;
    align-items center;
    .top-label {
      line-height 32px;
      height 32px;
      width 70px;
      color #666;
      font-size 14px;
    }
    .remote-select {
      width 300px;
    }
  }

  .box {
    display flex;
    justify-content space-between;

    .vmBox {
      width 69%;
      padding 12px;
      background-color white;

      .block-title-row {
        display flex;
        align-items center;
        justify-content space-between;
        border-bottom 1px solid #ddd;
        padding-bottom 10px;

        .title {
          font-size 16px;
        }
      }

      .overview-items {
        padding 25px 0 15px;
        display flex;
        align-items center;
        justify-content space-around;
        text-align center;

        .item-icon {

        }

        .item-number {
          margin-top 6px;
          font-size 24px;
          font-weight bold;
        }

        .item-name {
          margin-top 6px;
          font-size 14px;
          color #666;
        }
      }
    }

    .azureBox {
      width 30%;
      padding 12px;
      background-color white;

      .block-title-row {
        display flex;
        align-items center;
        justify-content space-between;
        border-bottom 1px solid #ddd;
        padding-bottom 10px;

        .title {
          font-size 16px;
        }

        .right-btns {
          .right-btn {
            display flex;
            align-items center;
            cursor pointer;
            padding 3px 10px;
            color #606266;
            font-size 0

            span, i {
              font-size 14px;
            }
          }

          .right-btn:hover {
            color #3160ed;
          }
        }
      }

      .overview-items {
        padding 25px 0 15px;
        display flex;
        align-items center;
        justify-content space-around;
        text-align center;

        .item-icon {

        }

        .item-number {
          margin-top 6px;
          font-size 24px;
          font-weight bold;
        }

        .item-name {
          margin-top 6px;
          font-size 14px;
          color #666;
        }
      }

      .info-items {
        padding-bottom 8px;

        .info-row {
          display flex;
          align-items flex-start;
          padding-top 15px;

          .label-div {
            width 110px;
            font-size 14px;
          }

          .value-div {
            flex 1;
            font-size 14px;
          }
        }
      }
    }
  }

  .block {
    margin-top 10px;
    padding 12px;
    background-color white;

    .block-title-row {
      display flex;
      align-items center;
      justify-content space-between;
      border-bottom 1px solid #ddd;
      padding-bottom 10px;

      .title {
        font-size 16px;
      }

      .right-btns {
        .right-btn {
          display flex;
          align-items center;
          cursor pointer;
          padding 3px 10px;
          color #606266;
          font-size 0

          span, i {
            font-size 14px;
          }
        }

        .right-btn:hover {
          color #3160ed;
        }
      }
    }

    .overview-items {
      padding 25px 0 15px;
      display flex;
      align-items center;
      justify-content space-around;
      text-align center;

      .item-icon {

      }

      .item-number {
        margin-top 6px;
        font-size 24px;
        font-weight bold;
      }

      .item-name {
        margin-top 6px;
        font-size 14px;
        color #666;
      }
    }

    .info-items {
      padding-bottom 8px;

      .info-row {
        display flex;
        align-items flex-start;
        padding-top 15px;

        .label-div {
          width 110px;
          font-size 14px;
        }

        .value-div {
          flex 1;
          font-size 14px;
        }
      }
    }
  }

  .tableArea {
    margin-top 10px;
    display flex;

    .leftBox {
      flex 1;
      overflow hidden;
      margin-right 10px;
      background-color white;
      padding 12px;

      .title {
        font-size 16px;
        margin-bottom 15px;
      }
    }

    .rightBox {
      flex 1;
      padding 12px;
      overflow hidden;
      background-color white;

      .title {
        font-size 16px;
        margin-bottom 15px;
      }
    }
  }
}
</style>
