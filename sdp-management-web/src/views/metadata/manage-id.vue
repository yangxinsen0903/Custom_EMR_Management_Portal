<template>
  <div class="meta-data-content page-css list-css" v-loading="pageLoading">
    <Filters
        :filters="filters"
        @onSearch="searchEvent"
        @onReset="resetEvent"
    ></Filters>
    <div class="functions-row">
      <el-button type="primary" @click="newEvent">新建托管标识</el-button>
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
                prop="miName"
                label="托管标识名称"
                min-width="120"
            ></el-table-column>
            <el-table-column
                prop="tenantId"
                label="tenant ID"
                min-width="120"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="clientId"
                label="client ID"
                min-width="120"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="resourceId"
                label="resource ID"
                min-width="120"
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
        :title="dataForm.id ? '编辑托管标识' : '新建托管标识'"
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
          <el-form-item label="数据中心" prop="region" key="region">
            <RemoteSelect v-model="dataForm.region" placeholder="请选择数据中心"
                          optionsApi="/admin/meta/selectMetaDataList"
                          :filterable="true" :optionsDefaultArgs="regionArgs"  :disabled="dataForm.id!==''"
                          :optionsProps="{label: 'regionName', value: 'region'}"
                          @change="regionChanged"/>
          </el-form-item>
          <el-form-item label="订阅" prop="subscriptionId" key="subscriptionId">
            <RemoteSelect v-model="dataForm.subscriptionId" placeholder="请选择订阅" :filterable="true"
                          optionsApi="/admin/api/azure/metas/listSubscription" optionsApiType="get"
                          :optionsProps="{label: 'name', value: 'id'}"
                          @change="subsciptionChanged"></RemoteSelect>
          </el-form-item>
          <el-form-item label="托管标识名称" prop="miName">
            <el-input v-model="dataForm.miName" placeholder="请输入托管标识名称" maxlength="100" clearable show-word-limit/>
          </el-form-item>
          <el-form-item label="托管标识" prop="resourceId" key="resourceId">
            <RemoteSelect v-model="dataForm.resourceId" placeholder="请选择托管标识"
                          optionsApi="/admin/api/azure/metas/getManagedIdentityList"
                          :filterable="true" :optionsDefaultArgs="miArgs"
                          :optionsProps="{label: 'name', value: 'resourceId'}"
                          :argChecks="['region','subscriptionId']" optionsApiType="get"
                          @change="miChanged"/>
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
    default: "SupportedManagedIdentityList",
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
    type: "el-input",
    label: "托管标识名称",
    key: "miName",
    props: {placeholder: "请输入托管标识名称", clearable: true},
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
  miName: '',
  tenantId: '',
  clientId: '',
  resourceId: '',
  name: '',
  principalId: '',
  subscriptionId: '',
  subscriptionName: '',
});
const formRules = {
  region: FormCheck.required("请选择数据中心"),
  miName: FormCheck.required("请输入托管标识名称"),
  resourceId: FormCheck.required("请选择托管标识"),
  subscriptionId: FormCheck.required("请选择订阅"),
};

const miArgs = ref({})

const newEvent = () => {
  dataForm.id = ''
  dataForm.region = ''
  dataForm.regionName = ''
  dataForm.miName = ''
  dataForm.tenantId = ''
  dataForm.clientId = ''
  dataForm.resourceId = ''
  dataForm.name = ''
  dataForm.principalId = ''
  dataForm.subscriptionName = ''
  dataForm.subscriptionId = ''

  miArgs.value = {}

  dialogVisible.value = true
}

const editMateData = (item) => {
  dataForm.id = item.id
  dataForm.region = item.region
  dataForm.regionName = item.regionName
  dataForm.miName = item.miName
  dataForm.tenantId = item.tenantId
  dataForm.clientId = item.clientId
  dataForm.resourceId = item.resourceId
  dataForm.name = item.name
  dataForm.principalId = item.principalId
  dataForm.subscriptionId = item.subscriptionId
  dataForm.subscriptionName = item.subscriptionName

  miArgs.value = {region: dataForm.region,subscriptionId:dataForm.subscriptionId}

  dialogVisible.value = true
}

const regionChanged = (item) => {
  dataForm.regionName = item.label

  miArgs.value = {region: dataForm.region,subscriptionId:dataForm.subscriptionId}
}

const miChanged = (item) => {
  dataForm.tenantId = item.tenantId
  dataForm.clientId = item.clientId
  dataForm.resourceId = item.resourceId
  dataForm.name = item.name
  dataForm.principalId = item.principalId
}

const saveDataEvent = () => {
  Ref_DataForm.value.validate((valid, fields) => {
    if (valid) {
      pageLoading.value = true;

      let params = {
        type: 'SupportedManagedIdentityList',
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

  miArgs.value = {region: dataForm.region,subscriptionId:dataForm.subscriptionId}
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
    .select-data-info {
      display flex;
      align-items center;
      margin-bottom 18px;
      .item {
        margin-right 40px;
        margin-left 24px;
      }
    }
  }
}
</style>
