<template>
  <div class="meta-data-content page-css list-css" v-loading="pageLoading">
    <Filters
        :filters="filters"
        @onSearch="searchEvent"
        @onReset="resetEvent"
    ></Filters>
    <div class="functions-row">
      <el-button type="primary" @click="newEvent">新建数据中心</el-button>
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
                min-width="140"
            ></el-table-column>
            <el-table-column prop="region" label="数据中心ID" min-width="120"></el-table-column>
            <el-table-column
                prop="subscriptionName"
                label="订阅"
                min-width="120"
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
        :title="dataForm.id ? '编辑数据中心' : '新建数据中心'"
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
          <el-form-item label="数据中心名称" prop="regionName">
            <el-input
                v-model="dataForm.regionName"
                placeholder="请输入数据中心名称"
                maxlength="40"
                clearable
                show-word-limit
            />
          </el-form-item>
          <el-form-item label="订阅" prop="subscriptionId" key="subscriptionId">
            <RemoteSelect v-model="dataForm.subscriptionId" placeholder="请选择订阅" :filterable="true" :disabled="dataForm.id!==''"
                          optionsApi="/admin/api/azure/metas/listSubscription" optionsApiType="get"
                          :optionsProps="{label: 'name', value: 'id'}"
                          @change="subsciptionChanged"></RemoteSelect>
          </el-form-item>
          <el-form-item label="数据中心" prop="region" key="region">
            <RemoteSelect v-model="dataForm.region" placeholder="请选择Azure数据中心"
                          :filterable="true"  :disabled="dataForm.id!==''"
                          optionsApi="/admin/api/azure/metas/getRegionList" optionsApiType="get"
                          :optionsDefaultArgs="regionArgs" :argChecks="['subscriptionId']"
                          :optionsProps="{label: 'displayName', value: 'name'}"
                          @change="regionChanged"></RemoteSelect>
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
import RemoteSelect from "@/components/base/remote-select/remote-select.vue";

const route = useRoute();
const router = useRouter();

const pageLoading = ref(false);
const filters = ref([
  {
    type: 'hidden',
    key: 'type',
    props: {},
    default: "SupportedRegionList",
  },
  {
    type: 'combo-box',
    key: 'regionName',
    props: {},
    selOptionType: '',
    options: [
      {
        "type": "el-input",
        "label": "名称",
        "rules": [],
        "key": "regionName",
        "props": {"placeholder": "请输入数据中心名称", "clearable": true}
      },
      {
        "type": "el-input",
        "label": "ID",
        "rules": [],
        "key": "region",
        "props": {"placeholder": "请输入数据中心ID", "clearable": true}
      }
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
  ElMessageBox.confirm("您确定需要删除该数据中心吗？如果该数据中心下已经创建了数据，可能导致删除失败，需要删除对应的数据再进行尝试。", "提示", {
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
  regionName: '',
  region: '',
  key: '',
  displayName: '',
  physicalLocation: '',
  subscriptionId: '',
  subscriptionName: '',
});
const formRules = {
  regionName: FormCheck.required("请输入数据中心名称"),
  region: FormCheck.required("请选择Azure数据中心"),
  subscriptionId: FormCheck.required("请选择订阅"),
};

const newEvent = () => {
  dataForm.id = ''
  dataForm.regionName = ''
  dataForm.region = ''
  dataForm.subscriptionId = ''

  regionArgs.value = {}

  dialogVisible.value = true
}

const editMateData = (item) => {
  dataForm.id = item.id
  dataForm.regionName = item.regionName
  dataForm.region = item.region
  dataForm.key = item.key
  dataForm.displayName = item.displayName
  dataForm.physicalLocation = item.physicalLocation
  dataForm.subscriptionId = item.subscriptionId
  dataForm.subscriptionName = item.subscriptionName

  regionArgs.value = {
    subscriptionId: item.subscriptionId
  }

  dialogVisible.value = true
}

const regionChanged = (regionItem) => {
  dataForm.region = regionItem.name
  dataForm.key = regionItem.key
  dataForm.displayName = regionItem.displayName
  dataForm.physicalLocation = regionItem.physicalLocation
}

const regionArgs = ref({})

const subsciptionChanged = (item) => {
  dataForm.subscriptionId = item.id
  dataForm.subscriptionName = item.name

  regionArgs.value = {
    subscriptionId: item.id
  }
}

const saveDataEvent = () => {
  Ref_DataForm.value.validate((valid, fields) => {
    if (valid) {
      pageLoading.value = true;

      let params = {
        ...dataForm,
        type: 'SupportedRegionList',
      }
      if (dataForm.id) {
        params.id = dataForm.id
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
