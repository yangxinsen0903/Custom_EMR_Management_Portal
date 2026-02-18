/**Created by liaoyingchao on 1/30/23.*/

<template>
  <div class="arguments-detail page-css detail-css" v-loading="pageLoading">
    <div class="option-group">
      <div style="display: flex;align-items: center;">
        <div>实例组：</div>
        <el-select v-model="selectGroup">
          <el-option
                  v-for="option in cfgsOptions"
                  :key="option.group"
                  :label="option.name"
                  :value="option.group"
          />
        </el-select>
      </div>
      <div>
        <el-button type="primary" :icon="RefreshRight" @click="reloadEvent" :disabled="clusterData.state != '2'">刷新</el-button>
      </div>
    </div>
    <div class="no-data" v-if="clusterCfgs.length == 0">暂无参数配置</div>
    <div class="detail-block-div" v-for="cfg in clusterCfgs">
      <div class="sub-title">参数类别：{{ cfg.classification }}</div>
      <div class="detail-items">
        <div class="flex-row" v-for="item in cfg.cfgList">
          <div class="detail-item">
            <div class="label">参数：</div>
            <div class="value">{{ item.key }}</div>
          </div>
          <div class="detail-item">
            <div class="label">值：</div>
            <div class="value">{{ item.value }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { RefreshRight } from '@element-plus/icons-vue'
  import {ref, reactive, toRefs, defineProps, defineEmits, computed, onMounted} from "vue"

  const props = defineProps({
    clusterData: {
      type: Object,
      default: () => {}
    }
  });
  const { clusterData } = toRefs(props)

  const pageLoading = ref(false)

  const cfgsOptions = ref([])
  
  function initOptions() {
    cfgsOptions.value = []

    let cfgs = clusterData.value.clusterCfgs || []

    cfgsOptions.value.push({
      name: '集群默认',
      group: 'default',
      cfgs: cfgs,
    })

    let instanceGroupSkuCfgs = clusterData.value.instanceGroupSkuCfgs || []
    instanceGroupSkuCfgs.forEach(item => {
      let groupCfgs = item.groupCfgs || []
      if (groupCfgs.length) {
        cfgsOptions.value.push({
          name: item.groupName,
          group: item.groupName,
          cfgs: groupCfgs,
        })
      }
    })
  }

  const selectGroup = ref('default')

  const clusterCfgs = computed(() => {
    let item = cfgsOptions.value.find(itm => {
      return itm.group == selectGroup.value
    }) || {}
    let cfgs = item.cfgs || []

    cfgs.forEach(item => {
      let cfg = item.cfg || {}
      let arr = []
      for (let key in cfg) {
        arr.push({
          key: key,
          value: cfg[key]
        })
      }
      item.cfgList = arr
    })
    return cfgs
  })

  function reloadEvent() {
    initOptions();
  }

  onMounted(() => {
    initOptions();
  })
</script>

<style lang="stylus" scoped type="text/stylus">
  .arguments-detail {
    padding-top 5px;
    overflow-y auto;

    .option-group {
      display flex;
      align-items center;
      padding 10px 0;
      color #666;
      font-size 14px;
      justify-content space-between;
    }

    .detail-block-div {
      border 1px solid #ddd;
      background #f6f6f6;
      padding 15px 15px 0;
      margin-bottom 10px;
      .label {
        width 60px !important;
      }
    }

    .no-data {
      width 100%;
      margin-top 120px;
      text-align center;
      font-size 16px;
      color #999;
    }
  }
</style>