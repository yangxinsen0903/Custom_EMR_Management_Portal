<template>
  <div class="meta-data-content page-css list-css" v-loading="pageLoading">
    <Filters
        :filters="filters"
        :label-width="'125px'"
        @onSearch="searchEvent"
        @onReset="resetEvent"
    ></Filters>
    <div class="functions-row">
      <el-button type="primary" @click="newEvent">新建磁盘</el-button>
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
                label="磁盘名称"
                min-width="160"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="maxSize"
                label="最大容量(G)"
                min-width="110"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="maxThroughput"
                label="最大吞吐(MB/s)"
                min-width="130"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="maxIOPS"
                label="最大IOPS"
                min-width="100"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="remark"
                label="备注"
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
        :title="dataForm.id ? '编辑磁盘' : '新建磁盘'"
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
          <el-form-item label="磁盘" prop="name">
            <RemoteSelect v-model="dataForm.name" placeholder="请选择磁盘"
                          optionsApi="/admin/api/azure/metas/supportedDiskSkuList"
                          :filterable="true" :optionsDefaultArgs="diskArgs"
                          :optionsProps="{label: 'name', value: 'name'}"
                          :argChecks="['region']" optionsApiType="get" />
          </el-form-item>
          <el-form-item label="最大容量(G)" prop="maxSize">
            <el-input v-model="dataForm.maxSize" placeholder="请输入磁盘最大容量(G)" clearable/>
          </el-form-item>
          <el-form-item label="最大吞吐量(MB/s)" prop="maxThroughput">
            <el-input v-model="dataForm.maxThroughput" placeholder="请输入磁盘最大吞吐量(MB/s)" clearable/>
          </el-form-item>
          <el-form-item label="最大IOPS" prop="maxIOPS">
            <el-input v-model="dataForm.maxIOPS" placeholder="请输入磁盘最大IOPS" clearable/>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="dataForm.remark" placeholder="请输入磁盘备注" clearable maxlength="100" show-word-limit/>
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
    default: "SupportedDiskSkuList",
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
    label: "磁盘名称",
    key: "name",
    props: {placeholder: "请输入磁盘名称", clearable: true},
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
  maxSize: '',
  maxThroughput: '',
  maxIOPS: '',
  remark: '',
});
const formRules = {
  region: FormCheck.required("请选择数据中心"),
  name: FormCheck.required("请选择磁盘"),
  maxSize: FormCheck.justNumber(),
  maxThroughput: FormCheck.justNumber(),
  maxIOPS: FormCheck.justNumber(),
};

const newEvent = () => {
  dataForm.id = ''
  dataForm.region = ''
  dataForm.regionName = ''
  dataForm.name = ''
  dataForm.maxSize = ''
  dataForm.maxThroughput = ''
  dataForm.maxIOPS = ''
  dataForm.remark = ''

  diskArgs.value = {region: dataForm.region}

  dialogVisible.value = true
}

const editMateData = (item) => {
  dataForm.id = item.id
  dataForm.region = item.region
  dataForm.regionName = item.regionName
  dataForm.name = item.name
  dataForm.maxSize = item.maxSize
  dataForm.maxThroughput = item.maxThroughput
  dataForm.maxIOPS = item.maxIOPS
  dataForm.remark = item.remark

  diskArgs.value = {region: dataForm.region}

  dialogVisible.value = true
}

const diskArgs = ref({})

const regionChanged = (item) => {
  dataForm.regionName = item.label

  diskArgs.value = {region: dataForm.region}
}

const saveDataEvent = () => {
  Ref_DataForm.value.validate((valid, fields) => {
    if (valid) {
      pageLoading.value = true;

      let params = {
        type: 'SupportedDiskSkuList',
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
