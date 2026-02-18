/**Created by liaoyingchao on 2023/2/24.*/

<template>
  <div class="vm-num-chart">
    <div class="no-data" v-if="showNoData">暂无伸缩记录数据</div>
    <div id="linechart" class="chart-div" v-else></div>
  </div>
</template>

<script setup>
import { ref, onMounted, defineProps, toRefs } from 'vue'
import * as echarts from "echarts";
import clusterApi from "@/api/cluster";
import {timeToUtcTime, formatTimeHM} from "@/utils/format-time";
import {ElMessage, ElMessageBox} from 'element-plus';

const props = defineProps({
  clusterId: {
    type: String,
    default: ''
  },
  groupName: {
    type: String,
    default: ''
  },
});
const {clusterId, groupName} = toRefs(props)
const showNoData = ref(false)

function loadChartData() {

  let endTime = new Date();
  let begTime = new Date(endTime.getTime() - 24*60*60*1000);

  clusterApi.scalingLog({
    begTime: timeToUtcTime(begTime),
    endTime: timeToUtcTime(endTime),
    clusterId: clusterId.value,
    groupName: groupName.value,
    inQueue: 0,
    state: 2,
    logFlag: 3,
    scalingTypes: [1,2],
  }).then(res => {
    if (res.result == true) {
      let listData = res.data || []
      if (listData.length) {
        initChart(listData)
      } else {
        showNoData.value = true
      }
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

function initChart(listData) {

  let xData = [], yData = []
  for (let i = 0; i < listData.length; i++) {
    let item = listData[i]
    xData.push(formatTimeHM(item.endTime))
    yData.push(item.afterScalingCount || 0)
  }

  let myChart = echarts.init(document.getElementById('linechart'));

  // 指定图表的配置项和数据
  let option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['实例数']
    },
    grid: {
      left: 50,
      right: 10,
      top: 10,
      bottom: 30,
    },
    xAxis: {
      type: 'category',
      axisLabel: {
        // 坐标轴刻度标签的相关设置
        show: true, //控制显隐
        interval: 0,
      },
      axisTick: {
        //x轴刻度相关设置
        alignWithLabel: true,
      },
      data: xData
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        data: yData,
        type: 'line'
      }
    ]
  };

  // 使用刚指定的配置项和数据显示图表。
  myChart.setOption(option);
}

onMounted(() => {
  loadChartData()
})
</script>

<style lang="stylus" scoped type="text/stylus">
.vm-num-chart {
  width 100%;
  height 150px;
  .chart-div {
    width 100%;
    height 100%;
  }
  .no-data {
    width 100%;
    height 100%;
    text-align center;
    line-height 140px;
    font-size 16px;
    color #999;
  }
}
</style>