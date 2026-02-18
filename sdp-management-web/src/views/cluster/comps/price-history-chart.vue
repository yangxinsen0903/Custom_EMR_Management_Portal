/**Created by liaoyingchao on 2023/3/6.*/

<template>
  <div class="price-history-chart" v-loading="pageLoading">
    <div class="no-data" v-if="showNoData">暂无历史价格</div>
    <div id="linechart" class="chart-div" v-else></div>
  </div>
</template>

<script setup>
import { ref, onMounted, defineProps, toRefs } from 'vue'
import * as echarts from "echarts";
import clusterApi from "@/api/cluster";
import {timeToUtcTime, formatTimeYMD, formatTimeMD} from "@/utils/format-time";
import {ElMessage, ElMessageBox} from 'element-plus';

const props = defineProps({
  skuName: {
    type: String,
    default: ''
  },
  region: {
    type: String,
    default: ''
  },
});
const {skuName, region} = toRefs(props)
const showNoData = ref(false)

const pageLoading = ref(false)

function loadChartData() {

  // let endTime = new Date();
  // let begTime = new Date(endTime.getTime() - 24*60*60*1000);

  pageLoading.value = true
  clusterApi.spotPriceHistory({
    skuName: skuName.value,
    region: region.value,
    periodDays: 7
  }).then(res => {
    if (res.result == true) {
      let arr = res.data || []
      if (arr.length) {
        initChart(arr)
      } else {
        showNoData.value = true
      }
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  }).finally(() => {
    pageLoading.value = false
  })
}

function initChart(listData) {

  let xData = [], yData = []
  for (let i = 0; i < listData.length; i++) {
    let item = listData[i]
    xData.push(formatTimeMD(item.executeTime))
    yData.push(item.spotUnitPrice || 0)
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
.price-history-chart {
  width 400px;
  padding-top 10px;
  .chart-div {
    width 100%;
    height 150px;
  }
  .no-data {
    width 100%;
    height 150px;
    text-align center;
    line-height 140px;
    font-size 16px;
    color #999;
  }
}
</style>