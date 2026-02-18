<template>
  <el-tooltip effect="light" placement="bottom">
    <span style="font-size: 12px;color: #315FCE;cursor: pointer;line-height: 20px;white-space: nowrap;">查看机型</span>
    <template #content>
      <div style="width: 800px;">
        <el-table :data="data" border stripe header-row-class-name="theader">
          <el-table-column v-bind="column" v-for="column in vmskuColumns" align="center" />
          <el-table-column align="center" label="费用" min-width="200">
            <template #default="scope">
              <div>
                <div>标准价：{{ scope.row.ondemandUnitPrice || '' }} USD/h</div>
                <div>市场价：{{ scope.row.spotUnitPrice || '' }} USD/h</div>
                <el-tooltip effect="light" placement="bottom">
                      <span
                          style="font-size: 12px;color: #315FCE;cursor: pointer;line-height: 20px;white-space: nowrap;">查看历史价格</span>
                  <template #content>
                    <div style="max-width: 430px;">
                      <PriceHistoryChart :region="modelValue.region" :skuName="scope.row.name"></PriceHistoryChart>
                    </div>
                  </template>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <el-table-column align="center" label="逐出率" min-width="120">
            <template #default="scope">
              <div>
                <div>{{ scope.row.evictionRateUpper }} %</div>
                <el-tooltip effect="light" placement="bottom">
                      <span
                          style="font-size: 12px;color: #315FCE;cursor: pointer;line-height: 20px;white-space: nowrap;">查看历史逐出率</span>
                  <template #content>
                    <div style="max-width: 430px;">
                      <EvictionHistoryChart :region="modelValue.region"
                                            :skuName="scope.row.name"></EvictionHistoryChart>
                    </div>
                  </template>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </template>
  </el-tooltip>
</template>

<script setup>
import PriceHistoryChart from "@/views/cluster/comps/price-history-chart.vue";
import EvictionHistoryChart from "@/views/cluster/comps/eviction-history-chart.vue";

import {defineProps, onMounted, toRefs} from "vue";

const props = defineProps({
  data: Array, // 数据选项
  skuNamesFilter: Array,
})
let {data,skuNamesFilter} = toRefs(props)

const vmskuColumns = [
  {prop: 'name', label: '机型名称', minWidth: 120, isFilter: true},
  {prop: 'family', label: 'sku系列', minWidth: 120, isFilter: true},
  {prop: 'vCoreCount', label: 'CPU核数', width: 100, isFilter: true},
  {prop: 'memoryGB', label: '内存数(GB)', width: 100, isFilter: true},
  {prop: 'ratio', label: 'CPU内存比', width: 100, isFilter: true, noColumn: true, controlType: 'select'},
  {prop: 'maxDataDisksCount', label: '最大磁盘数', width: 100, isFilter: true},
  {prop: 'cpuType', label: 'CPU类型', width: 100, isFilter: false},
]
data = data.value.filter(item => skuNamesFilter.value!=null && skuNamesFilter.value.includes(item.name));

</script>

<style scoped lang="stylus">

</style>