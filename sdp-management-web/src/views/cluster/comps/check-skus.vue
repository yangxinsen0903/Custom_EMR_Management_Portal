<template>
  <el-tooltip effect="light" placement="bottom">
    <span style="font-size: 12px;color: #315FCE;cursor: pointer;line-height: 20px;white-space: nowrap;">查看机型</span>
    <template #content>
      <div style="width: 800px;">
        <el-table :data="showSkus" border stripe header-row-class-name="theader" height="400">
          <el-table-column v-bind="column" v-for="column in showColumns" align="center" />
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
                      <PriceHistoryChart :region="region" :skuName="scope.row.name"></PriceHistoryChart>
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
                  <span style="font-size: 12px;color: #315FCE;cursor: pointer;line-height: 20px;white-space: nowrap;">查看历史逐出率</span>
                  <template #content>
                    <div style="max-width: 430px;">
                      <EvictionHistoryChart :region="region"
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
import {defineProps, onMounted, toRefs, ref, computed} from "vue";
import PriceHistoryChart from "@/views/cluster/comps/price-history-chart.vue";
import EvictionHistoryChart from "@/views/cluster/comps/eviction-history-chart.vue";
import clusterApi from "@/api/cluster";
import {ElMessage, ElMessageBox} from 'element-plus';

const props = defineProps({
  skuNames: Array, //
  region: String, // 数据中心
})
const {skuNames, region} = toRefs(props)

const vmskuColumns = [
  {prop: 'name', label: '机型名称', minWidth: 120, isFilter: true},
  {prop: 'family', label: 'sku系列', minWidth: 120, isFilter: true},
  {prop: 'vCoreCount', label: 'CPU核数', width: 100, isFilter: true},
  {prop: 'memoryGB', label: '内存数(GB)', width: 100, isFilter: true},
  {prop: 'ratio', label: 'CPU内存比', width: 100, isFilter: true, noColumn: true, controlType: 'select'},
  {prop: 'maxDataDisksCount', label: '最大磁盘数', width: 100, isFilter: true},
  {prop: 'cpuType', label: 'CPU类型', width: 100, isFilter: false},
]

const showColumns = computed(() => {
  let arr = vmskuColumns.filter(itm => {
    return itm.noColumn != true
  }) || []

  return arr
})

const showSkus = ref([])

const getShowSkus = () => {
  clusterApi.getVmskuList({region: region.value}).then(res => {
    if (res.result == true) {
      let arr = res.data || []

      showSkus.value = arr.filter(item => skuNames.value && skuNames.value.includes(item.name));
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

onMounted(() => {
  getShowSkus()
})
</script>

<style scoped lang="stylus">
>>>.theader {
  td, th {
    background-color: #F8F8F8 !important;

    .cell {
      word-break break-word;
    }
  }
}
</style>