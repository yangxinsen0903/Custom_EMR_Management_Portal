/**Created by liaoyingchao on 12/25/22.*/

<template>
  <div class="page-css list-css vm-list" v-loading="pageLoading">
    <Filters :labelWidth="'80px'" :filters="filters" @onSearch="searchEvent" @onReset="resetEvent"></Filters>
    <div class="full-container">
      <ListContainer ref="TabelContainer" :listApiFunction="getList">
        <template #default="tableData">
          <el-table
                  :height="tableData.data.tableHeight"
                  :data="tableData.data.tableData"
                  stripe
                  border
                  header-row-class-name="theader"
                  style="width: 100%">
            <el-table-column
                    prop="group_name"
                    label="实例组名称"
                    width="100">
            </el-table-column>
            <el-table-column
                    prop="vm_name"
                    label="实例ID"
                    min-width="120">
            </el-table-column>
            <el-table-column
                    prop="payInfo"
                    label="付费信息"
                    width="200">
              <template #default="scope">
                <div>
                  <div v-if="scope.row.purchase_type==='1'">
                    <div>标准价：{{scope.row.ondemond_price}} USD/h</div>
                  </div>

                  <div v-if="scope.row.purchase_type==='2'">
                    <div v-if="scope.row.price_strategy===1">
                      <div>出价：按照标准价{{scope.row.max_price}}% ({{scope.row.bidAmount}} USD/h)</div>
                      <div>成交价：按照标准价{{scope.row.closingPriceScale}}% ({{scope.row.spot_price}} USD/h)</div>
                    </div>

                    <div v-if="scope.row.price_strategy===2">
                      <div>出价：{{scope.row.max_price}} USD/h</div>
                      <div>成交价：{{scope.row.spot_price}} USD/h</div>
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column
                    prop="state"
                    label="状态"
                    width="100">
              <template #default="scope">
                {{ stateToStr(scope.row.state) }}
              </template>
            </el-table-column>
            <el-table-column
                    prop="internalIp"
                    label="内网IP"
                    min-width="100">
            </el-table-column>
            <el-table-column
                    prop="sku_name"
                    label="规格"
                    min-width="100">
            </el-table-column>
            <el-table-column
                prop="cpu_type"
                label="CPU类型"
                min-width="100">
            </el-table-column>
            <el-table-column
                prop="vcpus"
                label="cpu核数"
                min-width="100">
            </el-table-column>
            <el-table-column
                prop="memory"
                label="内存(GB)"
                min-width="100">
            </el-table-column>
            <el-table-column
                    prop="storage"
                    label="磁盘"
                    min-width="140">
              <template #default="scope">
                <div>
                  <div>系统盘：{{scope.row.os_volume_size}}GB*{{scope.row.os_volume_count}}</div>
                  <div>数据盘：{{scope.row.data_volume_size}}GB*{{scope.row.data_volume_count}}</div>
                </div>
              </template>
            </el-table-column>
            <!--<el-table-column-->
                    <!--prop="serveCount"-->
                    <!--label="服务数量"-->
                    <!--width="100">-->
            <!--</el-table-column>-->
            <el-table-column
                    prop="create_begtime"
                    label="创建时间"
                    :formatter="columnTimeFormat"
                    width="108">
            </el-table-column>
          </el-table>
        </template>
      </ListContainer>
    </div>
  </div>
</template>

<script setup>
  import Filters from '@/components/list-comps/filters'
  import ListContainer from '@/components/list-comps/container'
  import {ref, reactive, toRefs, defineProps, defineEmits, defineExpose, computed} from "vue"
  import {useRouter} from 'vue-router'
  import taskCenterApi from '@/api/task-center'
  import {ElMessage, ElMessageBox} from 'element-plus';
  import clusterApi from "../../../api/cluster";
  import { columnTimeFormat } from "@/utils/format-time";

  const props = defineProps({
    clusterId: {
      type: String,
      default: ''
    }
  });
  const { clusterId } = toRefs(props)

  const pageLoading = ref(false)

  const typeOptions = [
    {"label": "全部", "value": ""},
    {"label": "Ambari", "value": 'Ambari'},
    {"label": "Master", "value": 'Master'},
    {"label": "Core", "value": 'Core'},
    {"label": "Task", "value": 'Task'},
  ]
  
  const stateOptions = [
    {"label": "全部", "value": ""},
    {"label": "停止", "value": 0},
    {"label": "运行中", "value": 1},
    {"label": "销毁中", "value": -10},
    {"label": "已销毁", "value": -1},
    {"label": "未知", "value": -99},
  ]

  function stateToStr(state) {
    let option = stateOptions.find(item => {
      return item.value === state
    }) || {}
    return option.label || state
  }

  const filters = ref([
    {
      "type": "el-input",
      "label": "实例Id",
      "rules": [],
      "key": "vmName",
      "props": {"placeholder": "请输入实例Id", "clearable": true}
    },
    {
      "type": "RemoteSelect",
      "label": "实例组",
      "rules": [],
      "key": "groupName",
      "props": {
        "placeholder": "请选择实例组",
        "clearable": true,
        "optionsApi": '/admin/api/getVMGroupsByClusterId',
        "optionsApiType": 'get',
        "optionsDefaultArgs": {
          clusterId: clusterId.value
        },
        optionsProps: {
          label: 'groupName',
          value: 'groupName'
        }
      }
    },
    {
      "type": "RemoteSelect",
      "label": "实例状态",
      "rules": [],
      "key": "state",
      "props": {
        "placeholder": "请选择实例状态",
        "clearable": true,
        "options": stateOptions
      }
    },
  ])

  function getList(data) {
    data.clusterId = clusterId.value
    return clusterApi.getvmList(data)
  }

  const TabelContainer = ref(null)

  function searchEvent(data) {
    TabelContainer.value.filterEvent(data);
  }

  function resetEvent(data) {
    searchEvent(data)
  }

  const vmGroupResize = function() {
    TabelContainer.value.resizeContainer();
  }
  defineExpose({vmGroupResize})
</script>

<style lang="stylus" scoped type="text/stylus">
  .vm-list {
    overflow hidden;
    padding 12px 0 10px;
    >>> .center-dialog {

    }
  }
</style>