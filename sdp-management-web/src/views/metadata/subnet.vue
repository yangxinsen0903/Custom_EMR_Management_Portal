<template>
  <div class="meta-data-content page-css list-css" v-loading="pageLoading">
    <Filters
        :filters="filters"
        @onSearch="searchEvent"
        @onReset="resetEvent"
    ></Filters>
    <div class="functions-row">
      <el-button type="primary" @click="newEvent">新建子网</el-button>
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
                prop="subnetName"
                label="子网名称"
                min-width="160"
            ></el-table-column>
            <el-table-column
                prop="subnetId"
                label="子网ID"
                min-width="160"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="virtualNetworkName"
                label="虚拟网络"
                min-width="160"
            ></el-table-column>
            <el-table-column
                prop="virtualNetworkResourceId"
                label="虚拟网络ID"
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
        :title="dataForm.id ? '编辑子网' : '新建子网'"
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
                          optionsApi="/admin/meta/selectMetaDataList"
                          :clearable="true" :filterable="true" :optionsDefaultArgs="regionArgs"  :disabled="dataForm.id!==''"
                          :optionsProps="{label: 'regionName', value: 'region'}"
                          @change="regionChanged"/>
          </el-form-item>
          <el-form-item label="子网名称" prop="subnetName">
            <el-input v-model="dataForm.subnetName" placeholder="请输入子网名称" maxlength="100" clearable show-word-limit/>
          </el-form-item>
          <el-form-item label="虚拟网络" prop="virtualNetworkResourceId">
            <RemoteSelect v-model="dataForm.virtualNetworkResourceId" placeholder="请选择虚拟网络"
                          optionsApi="/admin/api/azure/metas/getNetworkList"
                          :clearable="true" :filterable="true" :optionsDefaultArgs="virtualNetArgs"
                          :optionsProps="{label: 'name', value: 'resourceId'}"
                          :argChecks="['region']" optionsApiType="get"
                          @change="virtualNetChanged"/>
          </el-form-item>
          <el-form-item label="子网" prop="subnetId">
            <RemoteSelect v-model="dataForm.subnetId" placeholder="请选择子网"
                          optionsApi="/admin/api/azure/metas/getSubnetListById"
                          :clearable="true" :filterable="true" :optionsDefaultArgs="subNetArgs"
                          :optionsProps="{label: 'name', value: 'resourceId'}"
                          :argChecks="['region', 'vnetId']" optionsApiType="get"
                          @change="subNetChanged"/>
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
    default: "SupportedSubnetList",
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
        label: "子网名称",
        key: "subnetName",
        props: {placeholder: "请输入子网名称", clearable: true},
      },
      {
        type: "el-input",
        label: "子网ID",
        key: "subnetId",
        props: {placeholder: "请输入子网ID", clearable: true},
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
  subnetId: '',
  subnetName: '',
  subnetDisplayName: '',
  virtualNetworkResourceId: '',
  virtualNetworkName: '',
});
const formRules = {
  region: [
    FormCheck.required("请选择数据中心"),
    FormCheck.lengthLimit(6, 20),
    FormCheck.justLetterAndNumber(),
  ],
  subnetId: FormCheck.required("请选择子网"),
  virtualNetworkResourceId: FormCheck.required("请选择虚拟网络"),
  subnetName: FormCheck.required("请输入子网名称"),
};

const newEvent = () => {
  dataForm.id = ''
  dataForm.region = ''
  dataForm.regionName = ''
  dataForm.subnetId = ''
  dataForm.subnetName = ''
  dataForm.subnetDisplayName = ''
  dataForm.virtualNetworkResourceId = ''
  dataForm.virtualNetworkName = ''

  virtualNetArgs.value = {}
  subNetArgs.value = {}

  dialogVisible.value = true
}

const editMateData = (item) => {
  dataForm.id = item.id
  dataForm.region = item.region
  dataForm.regionName = item.regionName
  dataForm.subnetId = item.subnetId
  dataForm.subnetName = item.subnetName
  dataForm.subnetDisplayName = item.subnetDisplayName
  dataForm.virtualNetworkResourceId = item.virtualNetworkResourceId
  dataForm.virtualNetworkName = item.virtualNetworkName

  virtualNetArgs.value = {region: dataForm.region}
  subNetArgs.value = {region: dataForm.region, vnetId: dataForm.virtualNetworkResourceId}

  dialogVisible.value = true
}

const virtualNetArgs = ref({})
const subNetArgs = ref({})

const regionChanged = (item) => {
  dataForm.regionName = item.label

  virtualNetArgs.value = {region: dataForm.region}
  subNetArgs.value = {region: dataForm.region}
}

const virtualNetChanged = (item) => {
  dataForm.virtualNetworkName = item.name

  subNetArgs.value = {region: dataForm.region, vnetId: dataForm.virtualNetworkResourceId}
}

const subNetChanged = (item) => {
  dataForm.subnetDisplayName = item.name
}


const saveDataEvent = () => {
  Ref_DataForm.value.validate((valid, fields) => {
    if (valid) {
      pageLoading.value = true;

      let params = {
        type: 'SupportedSubnetList',
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
