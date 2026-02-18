<template>
  <div class="meta-data-content page-css list-css" v-loading="pageLoading">
    <Filters
        :filters="filters"
        :label-width="'125px'"
        @onSearch="searchEvent"
        @onReset="resetEvent"
    ></Filters>
    <div class="functions-row">
      <el-button type="primary" @click="newEvent">新建机型</el-button>
    </div>
    <div class="full-container">
      <ListContainer ref="TabelContainer" :listApiFunction="getList" :has-pagination="false">
        <template #default="tableData">
          <el-table
              :height="tableData.data.tableHeight"
              :data="tableData.data.tableData"
              stripe
              header-row-class-name="theader"
              style="width: 100%"
          >
            <el-table-column
                prop="regionName"
                label="数据中心名称"
                min-width="110"
            ></el-table-column>
            <el-table-column
                prop="subscriptionName"
                label="订阅"
                min-width="120"
            ></el-table-column>
            <el-table-column
                prop="name"
                label="机型名称"
                min-width="160"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="family"
                label="sku系列"
                min-width="140"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="vCoreCount"
                label="CPU核数"
                min-width="100"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="memoryGB"
                label="内存数(GB)"
                min-width="120"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="maxDataDisksCount"
                label="最多磁盘数量"
                min-width="120"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="cpuType"
                label="CPU类型"
                min-width="100"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="lastModifiedTime"
                :formatter="columnTimeFormat"
                label="修改时间"
                min-width="160"
            ></el-table-column>
            <el-table-column
                prop="createTime"
                :formatter="columnTimeFormat"
                label="创建时间"
                min-width="160"
            ></el-table-column>
            <el-table-column fixed="right" label="操作" width="120">
              <template #default="scope">
                <div>
                  <el-button type="primary" text @click="editMateData(scope.row)">编辑</el-button>
                  <el-button type="primary" text @click="deleteMetaDataById(scope.row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </ListContainer>
    </div>
    <el-dialog
        class="center-dialog"
        v-model="dialogVisible"
        :title="dataForm.id ? '编辑机型' : '新建机型'"
        width="900"
        destroy-on-close
    >
      <div>
        <el-form
            ref="Ref_DataForm"
            :model="dataForm"
            :rules="formRules"
            label-width="auto"
        >
          <el-form-item label="数据中心" prop="region">
            <RemoteSelect v-model="dataForm.region" placeholder="请选择数据中心"
                          optionsApi="/admin/meta/selectMetaDataList"
                          :clearable="true" :filterable="true" :optionsDefaultArgs="regionArgs"  :disabled="dataForm.id!==''"
                          :optionsProps="{label: 'regionName', value: 'region'}"
                          @change="regionChanged"/>
          </el-form-item>
          <el-form-item label="机型" prop="name">
            <TableSelect optionsApi="/admin/api/azure/metas/getVMSkuList" :optionsDefaultArgs="vmSkuArgs"
                         :tableColumn="skuColumns" optionsApiType="get" :argChecks="['region']"
                         v-model="dataForm.name" :mainKey="'name'"
                         @change="vmChanged"></TableSelect>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button :loading="pageLoading" type="primary" @click="saveDataEvent">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import Filters from "@/components/list-comps/filters";
import RemoteSelect from "@/components/base/remote-select/remote-select.vue"
import TableSelect from "@/components/base/table-select/index.vue"
import ListContainer from "@/components/list-comps/container";
import {ref, reactive} from "vue";
import {useRoute, useRouter} from "vue-router";
import {ElMessage, ElMessageBox} from "element-plus";
import metaDataApi from "../../api/meta-data";

import {
  columnTimeFormat,
  formatTime,
  formatTimeYMD,
  timeToUtcTime,
} from "@/utils/format-time";
import FormCheck from "@/utils/formCheck";

const route = useRoute();
const router = useRouter();

const regionArgs = {type: 'SupportedRegionList'}

const pageLoading = ref(false);
const filters = ref([
  {
    type: 'hidden',
    key: 'type',
    props: {},
    default: "SupportedVMSkuList",
  },
  {
    type: "RemoteSelect",
    label: "数据中心",
    rules: [],
    key: "region",
    props: {
      placeholder: "请选择数据中心",
      clearable: true,
      filterable: true,
      optionsApi: '/admin/meta/selectMetaDataList',
      optionsDefaultArgs: {type: 'SupportedRegionList'},
      optionsProps: {
        label: 'regionName',
        value: 'region',
      }
    },
  },
  {
    type: 'el-input',
    label: "机型名称",
    key: "name",
    props: {placeholder: "请输入机型名称", clearable: true},
  },
]);

let lastFilterData = {}

const TabelContainer = ref(null);

function searchEvent(data) {
  TabelContainer.value.filterEvent(data);
}

function resetEvent(data) {
  searchEvent(data);
}

function getList(data) {
  lastFilterData = data
  return metaDataApi.selectMetaDataList(data);
}

const deleteMetaDataById = (item) => {
  ElMessageBox.confirm("您确定需要删除该条数据吗？如果该条数据下已经创建了其他数据，可能导致删除失败，需要删除对应的数据再进行尝试。", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    pageLoading.value = true;
    metaDataApi.deleteMetaDataById({id: item.id}).then(res => {
      if (res.result == true) {
        ElMessage.success("删除成功！")

        searchEvent(lastFilterData);
      } else {
        ElMessage.error(res.errorMsg)
      }
    }).finally(() => {
      pageLoading.value = false;
    })
  })
}

const dialogVisible = ref(false);
const Ref_DataForm = ref(null);
const dataForm = reactive({
  id: '',
  region: '',
  regionName: '',
  name: '',
  family: '',
  vCoreCount: '',
  memoryGB: '',
  maxDataDisksCount: '',
  cpuType: '',
  tempSSDStorageGB: '',
  tempNVMeStorageGB: '',
  tempNVMeDisksCount: '',
  tempNVMeDiskSizeGB: '',
});
const formRules = {
  region: FormCheck.required("请选择数据中心"),
  name: FormCheck.required("请选择机型", "changed"),
};

const newEvent = () => {
  dataForm.id = ''
  dataForm.region = ''
  dataForm.regionName = ''
  dataForm.name = ''
  dataForm.family = ''
  dataForm.vCoreCount = ''
  dataForm.memoryGB = ''
  dataForm.maxDataDisksCount = ''
  dataForm.cpuType = ''
  dataForm.tempSSDStorageGB = ''
  dataForm.tempNVMeStorageGB = ''
  dataForm.tempNVMeDisksCount = ''
  dataForm.tempNVMeDiskSizeGB = ''

  vmSkuArgs.value = {region: dataForm.region}

  dialogVisible.value = true
}

const editMateData = (item) => {
  dataForm.id = item.id
  dataForm.region = item.region
  dataForm.regionName = item.regionName
  dataForm.name = item.name
  dataForm.family = item.family
  dataForm.vCoreCount = item.vCoreCount
  dataForm.memoryGB = item.memoryGB
  dataForm.maxDataDisksCount = item.maxDataDisksCount
  dataForm.cpuType = item.cpuType
  dataForm.tempSSDStorageGB = item.tempSSDStorageGB
  dataForm.tempNVMeStorageGB = item.tempNVMeStorageGB
  dataForm.tempNVMeDisksCount = item.tempNVMeDisksCount
  dataForm.tempNVMeDiskSizeGB = item.tempNVMeDiskSizeGB

  vmSkuArgs.value = {region: dataForm.region}

  dialogVisible.value = true
}

const vmSkuArgs = ref({})
const skuColumns = [
  {prop: 'name', label: '机型名称', minWidth: 160, isFilter: true},
  {prop: 'family', label: 'sku系列', minWidth: 120, isFilter: true},
  {prop: 'vCoreCount', label: 'CPU核数', width: 100, isFilter: true},
  {prop: 'memoryGB', label: '内存数(GB)', width: 100, isFilter: true},
  {prop: 'maxDataDisksCount', label: '最大磁盘数', width: 100, isFilter: true},
  {prop: 'cpuType', label: 'CPU类型', width: 100, isFilter: true},
]

const regionChanged = (item) => {
  dataForm.regionName = item.label

  vmSkuArgs.value = {region: dataForm.region}
}

const vmChanged = (item) => {
  dataForm.name = item.name
  dataForm.family = item.family
  dataForm.vCoreCount = item.vCoreCount
  dataForm.memoryGB = item.memoryGB
  dataForm.maxDataDisksCount = item.maxDataDisksCount
  dataForm.cpuType = item.cpuType
  dataForm.tempSSDStorageGB = item.tempSSDStorageGB
  dataForm.tempNVMeStorageGB = item.tempNVMeStorageGB
  dataForm.tempNVMeDisksCount = item.tempNVMeDisksCount
  dataForm.tempNVMeDiskSizeGB = item.tempNVMeDiskSizeGB
}

const saveDataEvent = () => {
  Ref_DataForm.value.validate((valid, fields) => {
    if (valid) {
      pageLoading.value = true;

      let params = {
        type: 'SupportedVMSkuList',
        ...dataForm
      }
      if (dataForm.id) {
        metaDataApi.updateMetaData(params).then((res) => {
          if (res.result == true) {
            ElMessage.success("保存成功！");
            dialogVisible.value = false

            searchEvent(lastFilterData);
          } else {
            ElMessage.error(res.errorMsg);
          }
        }).finally(() => {
          pageLoading.value = false;
        })
      } else {
        metaDataApi.insertMetaData(params).then((res) => {
          if (res.result == true) {
            ElMessage.success("保存成功！");
            dialogVisible.value = false

            searchEvent(lastFilterData);
          } else {
            ElMessage.error(res.errorMsg);
          }
        }).finally(() => {
          pageLoading.value = false;
        });
      }
    }
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

.meta-data-content {

  >>> .center-dialog {
  }
}
</style>
