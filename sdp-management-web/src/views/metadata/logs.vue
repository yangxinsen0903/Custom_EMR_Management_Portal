<template>
  <div class="meta-data-content page-css list-css" v-loading="pageLoading">
    <Filters
        :filters="filters"
        :label-width="'125px'"
        @onSearch="searchEvent"
        @onReset="resetEvent"
    ></Filters>
    <div class="functions-row">
      <el-button type="primary" @click="newEvent">新建日志桶</el-button>
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
                label="订阅中心"
                min-width="110"
            ></el-table-column>
            <el-table-column
                prop="logName"
                label="日志桶名称"
                min-width="100"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="storageAccountName"
                label="账户名称"
                min-width="100"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="resourceId"
                label="账户ID"
                min-width="160"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="name"
                label="日志桶"
                min-width="160"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="blobContainerUrl"
                label="日志桶URL"
                min-width="160"
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
        :title="dataForm.id ? '编辑日志桶' : '新建日志桶'"
        width="700"
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
                          optionsApi="/admin/meta/selectMetaDataList"  :disabled="dataForm.id!==''"
                          :clearable="true" :filterable="true" :optionsDefaultArgs="regionArgs"
                          :optionsProps="{label: 'regionName', value: 'region'}"
                          @change="regionChanged"/>
          </el-form-item>
          <el-form-item label="订阅" prop="subscriptionId" key="subscriptionId">
            <RemoteSelect v-model="dataForm.subscriptionId" placeholder="请选择订阅" :filterable="true"
                          optionsApi="/admin/api/azure/metas/listSubscription" optionsApiType="get"
                          :optionsProps="{label: 'name', value: 'id'}"
                          @change="subsciptionChanged"></RemoteSelect>
          </el-form-item>
          <el-form-item label="日志桶名称" prop="logName">
            <el-input v-model="dataForm.logName" placeholder="请输入日志桶名称" maxlength="100" clearable show-word-limit/>
          </el-form-item>
          <el-form-item label="账户" prop="resourceId">
            <RemoteSelect v-model="dataForm.resourceId" placeholder="请选择账户"
                          optionsApi="/admin/api/azure/metas//getStorageAccountList"
                          :filterable="true" :optionsDefaultArgs="accountArgs"
                          :optionsProps="{label: 'name', value: 'resourceId'}"
                          :argChecks="['region','subscriptionId']" optionsApiType="get"
                          @change="accountChanged"/>
          </el-form-item>
          <el-form-item label="日志桶" prop="blobContainerUrl">
            <RemoteSelect v-model="dataForm.blobContainerUrl" placeholder="请选择日志桶"
                          optionsApi="/admin/api/azure/metas/getLogsBlobContainerListById"
                          :filterable="true" :optionsDefaultArgs="logsArgs"
                          :optionsProps="{label: 'name', value: 'blobContainerUrl'}"
                          :argChecks="['region', 'saId','subscriptionId']" optionsApiType="get"
                          @change="logsChanged"/>
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
    default: "SupportedLogsBlobContainerList",
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
    type: 'combo-box',
    key: 'subnetName',
    props: {},
    selOptionType: '',
    options: [
      {
        type: "el-input",
        label: "日志桶名称",
        key: "logName",
        props: {placeholder: "请输入日志桶名称", clearable: true},
      },
      {
        type: "el-input",
        label: "日志桶URL",
        key: "blobContainerUrl",
        props: {placeholder: "请输入日志桶URL", clearable: true},
      },
    ]
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
  resourceId: '',
  name: '',
  securityGroupName: '',
  subscriptionId:'',
  subscriptionName:''
});
const formRules = {
  region: FormCheck.required("请选择数据中心"),
  logName: FormCheck.required("请输入日志桶名称"),
  resourceId: FormCheck.required("请选择账户"),
  blobContainerUrl: FormCheck.required("请选择日志桶"),
  subscriptionId: FormCheck.required("请选择订阅"),
};

const newEvent = () => {
  dataForm.id = ''
  dataForm.region = ''
  dataForm.regionName = ''
  dataForm.logName = ''
  dataForm.resourceId = ''
  dataForm.storageAccountName = ''
  dataForm.blobContainerUrl = ''
  dataForm.name = ''
  dataForm.subscriptionId = ''
  dataForm.subscriptionName = ''

  accountArgs.value = {}
  logsArgs.value = {}

  dialogVisible.value = true
}

const editMateData = (item) => {
  dataForm.id = item.id
  dataForm.region = item.region
  dataForm.regionName = item.regionName
  dataForm.logName = item.logName
  dataForm.storageAccountName = item.storageAccountName
  dataForm.blobContainerUrl = item.blobContainerUrl
  dataForm.name = item.name
  dataForm.resourceId = item.resourceId
  dataForm.subscriptionId = item.subscriptionId
  dataForm.subscriptionName = item.subscriptionName

  accountArgs.value = {region: dataForm.region,subscriptionId:dataForm.subscriptionId}
  logsArgs.value = {region: dataForm.region, saId: dataForm.resourceId,subscriptionId:dataForm.subscriptionId}

  dialogVisible.value = true
}

const accountArgs = ref({})
const logsArgs = ref({})

const regionChanged = (item) => {
  dataForm.regionName = item.label

  accountArgs.value = {region: dataForm.region,subscriptionId:dataForm.subscriptionId}
  logsArgs.value = {region: dataForm.region, saId: dataForm.resourceId,subscriptionId:dataForm.subscriptionId}
}

const accountChanged = (item) => {
  dataForm.storageAccountName = item.name

  accountArgs.value = {region: dataForm.region,subscriptionId:dataForm.subscriptionId}
  logsArgs.value = {region: dataForm.region, saId: dataForm.resourceId,subscriptionId:dataForm.subscriptionId}
}

const logsChanged = (item) => {
  dataForm.name = item.name
}

const saveDataEvent = () => {
  Ref_DataForm.value.validate((valid, fields) => {
    if (valid) {
      pageLoading.value = true;

      let params = {
        type: 'SupportedLogsBlobContainerList',
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
const subsciptionChanged = (item) => {
  dataForm.subscriptionId = item.id
  dataForm.subscriptionName = item.name

  accountArgs.value = {region: dataForm.region,subscriptionId:dataForm.subscriptionId}
  logsArgs.value = {region: dataForm.region, saId: dataForm.resourceId,subscriptionId:dataForm.subscriptionId}
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
