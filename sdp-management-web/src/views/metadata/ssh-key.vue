<template>
  <div class="meta-data-content page-css list-css" v-loading="pageLoading">
    <Filters
        :filters="filters"
        :label-width="'125px'"
        @onSearch="searchEvent"
        @onReset="resetEvent"
    ></Filters>
    <div class="functions-row">
      <el-button type="primary" @click="newEvent">新建密钥</el-button>
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
                prop="name"
                label="密钥名称"
                min-width="160"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                label="密钥类型"
                min-width="160"
                show-overflow-tooltip>
              <template #default="scope">
                <div>
                  {{scope.row.keyType=='1'?"公钥":"私钥"}}
                </div>
              </template>
            </el-table-column>
            <el-table-column
                prop="secretResourceId"
                label="密钥ID"
                min-width="160"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="keyVaultResourceId"
                label="key vault ID"
                min-width="160"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="keyVaultResourceName"
                label="key vault名称"
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
        :title="dataForm.id ? '编辑密钥' : '新建密钥'"
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
          <el-form-item label="订阅" prop="subscriptionId" key="subscriptionId">
            <RemoteSelect v-model="dataForm.subscriptionId" placeholder="请选择订阅" :filterable="true"
                          optionsApi="/admin/api/azure/metas/listSubscription" optionsApiType="get"
                          :optionsProps="{label: 'name', value: 'id'}"
                          @change="subsciptionChanged"></RemoteSelect>
          </el-form-item>
          <el-form-item label="密钥名称" prop="name">
            <el-input v-model="dataForm.name" placeholder="请输入密钥名称" maxlength="100" clearable show-word-limit/>
          </el-form-item>
          <el-form-item label="密钥类型" prop="keyType">
            <el-radio-group v-model="dataForm.keyType">
              <el-radio :label="1" >公钥</el-radio>
              <el-radio :label="2" >私钥</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="key vault" prop="keyVaultResourceId">
            <RemoteSelect v-model="dataForm.keyVaultResourceId" placeholder="请选择key vault"
                          optionsApi="/admin/api/azure/metas/supportedKVList"
                          :filterable="true" :optionsDefaultArgs="keyVaultArgs"
                          :optionsProps="{label: 'name', value: 'resourceId'}"
                          :argChecks="['region','subscriptionId']" optionsApiType="get"
                          @change="keyVaultChanged"/>
          </el-form-item>
          <el-form-item label="秘钥" prop="nameInKeyVault">
            <RemoteSelect v-model="dataForm.secretResourceId" placeholder="请选择秘钥"
                          optionsApi="/admin/api/azure/metas/getSSHKeyPairById"
                          :filterable="true" :optionsDefaultArgs="sshKeyArgs"
                          :optionsProps="{label: 'secretName', value: 'secretName'}"
                          :argChecks="['region', 'kvId','subscriptionId']" optionsApiType="get"
                          @change="sshKeyChanged"/>
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
import permissionCheck from "@/utils/permission-check";

const route = useRoute();
const router = useRouter();

const regionArgs = {type: 'SupportedRegionList'}

const pageLoading = ref(false);
const filters = ref([
  {
    type: 'hidden',
    key: 'type',
    props: {},
    default: "SupportedSSHKeyPairList",
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
        label: "密钥名称",
        key: "name",
        props: {placeholder: "请输入密钥名称", clearable: true},
      },
      {
        type: "el-input",
        label: "密钥ID",
        key: "secretResourceId",
        props: {placeholder: "请输入密钥ID", clearable: true},
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
  name: '',
  keyVaultResourceId: '',
  keyVaultResourceName: '',
  secretResourceId: '',
  nameInKeyVault: '',
  keyType:'',
});
const formRules = {
  region: FormCheck.required("请选择数据中心"),
  name: FormCheck.required("请输入密钥名称"),
  keyVaultResourceId: FormCheck.required("请选择key vault"),
  nameInKeyVault: FormCheck.required("请选择密钥"),
  subscriptionId: FormCheck.required("请选择订阅"),
  keyType: FormCheck.required("请选择密钥类型"),
};

const newEvent = () => {
  dataForm.id = ''
  dataForm.region = ''
  dataForm.regionName = ''
  dataForm.name = ''
  dataForm.keyVaultResourceId = ''
  dataForm.keyVaultResourceName = ''
  dataForm.secretResourceId = ''
  dataForm.nameInKeyVault = ''
  dataForm.subscriptionId = ''
  dataForm.subscriptionName = ''
  dataForm.keyType = ''

  keyVaultArgs.value = {region: dataForm.region,subscriptionId:dataForm.subscriptionId}
  sshKeyArgs.value = {region: dataForm.region, kvId: dataForm.keyVaultResourceId,subscriptionId:dataForm.subscriptionId}

  dialogVisible.value = true
}

const editMateData = (item) => {
  dataForm.id = item.id
  dataForm.region = item.region
  dataForm.regionName = item.regionName
  dataForm.name = item.name
  dataForm.keyVaultResourceId = item.keyVaultResourceId
  dataForm.keyVaultResourceName = item.keyVaultResourceName
  dataForm.secretResourceId = item.secretResourceId
  dataForm.nameInKeyVault = item.nameInKeyVault
  dataForm.subscriptionName = item.subscriptionName
  dataForm.subscriptionId = item.subscriptionId
  dataForm.keyType = item.keyType

  keyVaultArgs.value = {region: dataForm.region,subscriptionId:dataForm.subscriptionId}
  sshKeyArgs.value = {region: dataForm.region, kvId: dataForm.keyVaultResourceId,subscriptionId:dataForm.subscriptionId}

  dialogVisible.value = true
}

const keyVaultArgs = ref({})
const sshKeyArgs = ref({})

const regionChanged = (item) => {
  dataForm.regionName = item.label

  keyVaultArgs.value = {region: dataForm.region,subscriptionId:dataForm.subscriptionId}
  sshKeyArgs.value = {region: dataForm.region, kvId: dataForm.keyVaultResourceId,subscriptionId:dataForm.subscriptionId}
}

const keyVaultChanged = (item) => {
  dataForm.keyVaultResourceName = item.name

  keyVaultArgs.value = {region: dataForm.region,subscriptionId:dataForm.subscriptionId}
  sshKeyArgs.value = {region: dataForm.region, kvId: dataForm.keyVaultResourceId,subscriptionId:dataForm.subscriptionId}
}

const sshKeyChanged = (item) => {
  dataForm.nameInKeyVault = item.secretName
  // dataForm.secretResourceId = item.
}

const saveDataEvent = () => {
  Ref_DataForm.value.validate((valid, fields) => {
    if (valid) {
      pageLoading.value = true;

      let params = {
        type: 'SupportedSSHKeyPairList',
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

  keyVaultArgs.value = {region: dataForm.region,subscriptionId:dataForm.subscriptionId}
  sshKeyArgs.value = {region: dataForm.region, kvId: dataForm.keyVaultResourceId,subscriptionId:dataForm.subscriptionId}
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
