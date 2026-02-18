/**Created by liaoyingchao on 12/1/22.*/

<template>
  <div class="index page-css list-css" v-loading="pageLoading">
    <Filters
        :filters="filters"
        @onSearch="searchEvent"
        @onReset="resetEvent"
    ></Filters>
    <div class="functions-row">
      <el-button type="primary" @click="createEvent">新增配置</el-button>
    </div>
    <div class="full-container">
      <ListContainer ref="TabelContainer" :listApiFunction="getList">
        <template #default="tableData">
          <el-table
              :height="tableData.data.tableHeight"
              :data="tableData.data.tableData"
              stripe
              header-row-class-name="theader"
              style="width: 100%"
          >
            <el-table-column prop="componentCode" label="大数据组件" min-width="120" show-overflow-tooltip></el-table-column>
            <el-table-column prop="configTypeCode" label="配置文件" min-width="120" show-overflow-tooltip></el-table-column>
            <el-table-column prop="dynamicType" label="动态配置类型" min-width="120" show-overflow-tooltip></el-table-column>
            <el-table-column prop="itemType" label="高可用" min-width="80">
              <template #default="scope">
                {{ itemTypeToStr(scope.row.itemType) }}
              </template>
            </el-table-column>
            <el-table-column prop="state" label="状态" min-width="80">
              <template #default="scope">
                {{ stateToStr(scope.row.state) }}
              </template>
            </el-table-column>
            <el-table-column prop="serviceCode" label="组件" min-width="120" show-overflow-tooltip></el-table-column>
            <el-table-column prop="stackCode" label="SDP版本" min-width="120" show-overflow-tooltip></el-table-column>
            <el-table-column prop="key" label="key" min-width="120" show-overflow-tooltip></el-table-column>
            <el-table-column prop="value" label="value" min-width="120" show-overflow-tooltip></el-table-column>
            <el-table-column fixed="right" label="管理" width="120">
              <template #default="scope">
                <div>
                  <el-button type="" text @click="editEvent(scope.row)">编辑</el-button>
                  <el-button type="danger" text @click="deleteconfig(scope.row)">删除</el-button>
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
        :title="confForm.id ? '编辑配置' : '新增配置'"
        width="800"
        destroy-on-close
    >
      <div>
        <el-form
            ref="Ref_confForm"
            :model="confForm"
            :rules="confFormRules"
            label-width="120px"
            :inline="true"
        >
          <el-form-item label="SDP版本" prop="stackCode" key="stackCode" style="width: 340px">
            <RemoteSelect v-model="confForm.stackCode" placeholder="请选择SDP版本" optionsApi="/admin/api/getreleases"
                          :clearable="true" :filterable="true" optionsApiType="get" :multiple="true" collapse-tags
                          :disabled="confForm.id != ''"
                          :optionsProps="{label: 'releaseVersion',value: 'releaseVersion'}" @change="releaseChange" />
          </el-form-item>
          <el-form-item label="高可用" prop="itemType" key="itemType" style="width: 340px">
            <RemoteSelect v-model="confForm.itemType" placeholder="请选择高可用" :multiple="true"
                          :disabled="confForm.id != ''"
                          :options="itemTypeOptions" :clearable="true"/>
          </el-form-item>
          <el-form-item label="组件" prop="serviceCode" key="serviceCode" style="width: 340px">
            <RemoteSelect v-model="confForm.serviceCode" placeholder="请选择组件" optionsApi="/admin/api/querycomponentlist"
                          :disabled="confForm.id != ''"
                          :clearable="true" :filterable="true" optionsApiType="get" :multiple="true" collapse-tags
                          :optionsProps="{label: 'serviceCode',value: 'serviceCode'}" />
          </el-form-item>
          <el-form-item label="配置文件" prop="configTypeCode" key="configTypeCode" style="width: 340px">
            <RemoteSelect v-model="confForm.configTypeCode" placeholder="请选择配置文件" optionsApi="/admin/api/queryprofileslist"
                          :disabled="confForm.id != ''"
                          :clearable="true" :filterable="true" optionsApiType="get" :optionsDefaultArgs="configArgs"
                          :optionsProps="{label: 'configTypeCode',value: 'configTypeCode'}" />
          </el-form-item>
          <el-form-item label="key" prop="key" style="width: 340px">
            <el-input v-model="confForm.key" placeholder="请输入Key" clearable :disabled="confForm.id != ''"/>
          </el-form-item>
          <el-form-item label="value" prop="value" style="width: 340px">
            <el-input v-model="confForm.value" placeholder="请输入value" clearable />
          </el-form-item>
          <el-form-item label="是否是Content" prop="isContentProp" style="width: 340px">
            <el-switch v-model="confForm.isContentProp" :active-value="1" :inactive-value="0" inline-prompt active-text="是" inactive-text="否"/>
          </el-form-item>
          <el-form-item label="状态" prop="state" style="width: 340px">
            <el-switch v-model="confForm.state" :active-value="'VALID'" :inactive-value="'INVALID'"
                       inline-prompt active-text="有效" inactive-text="无效"/>
          </el-form-item>
          <el-form-item label="是否是动态值" prop="isDynamic" style="width: 340px">
            <el-switch v-model="confForm.isDynamic" :active-value="1" :inactive-value="0" inline-prompt active-text="是" inactive-text="否"/>
          </el-form-item>
          <el-form-item label="动态值类型" prop="dynamicType" style="width: 340px" v-if="confForm.isDynamic == 1">
            <el-input v-model="confForm.dynamicType" placeholder="请输入动态值类型" clearable />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="updateconfig" v-if="confForm.id">保存</el-button>
          <el-button type="primary" @click="addconfig" v-else>保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import Filters from "@/components/list-comps/filters.vue";
import ListContainer from "@/components/list-comps/container.vue";
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Delete,
  Plus,
} from '@element-plus/icons-vue'
import FormCheck from "@/utils/formCheck";
import systemManage from "@/api/system-manage";
import {columnTimeFormat, formatTime, timeToUtcTime} from "@/utils/format-time";
import RemoteSelect from "@/components/base/remote-select/remote-select.vue";
import clipboard from "@/utils/clipboard";
import Steps from "@/components/base/steps/steps.vue";
import metaDataApi from "@/api/meta-data";

const pageLoading = ref(false);

const router = useRouter();

const itemTypeOptions = [
  {label: '是', value: '1'},
  {label: '否', value: '0'},
]

function itemTypeToStr(state) {
  let option = itemTypeOptions.find(item => {
    return item.value == state
  }) || {}
  return option.label || state
}

const stateOptions = [
  {label: '有效', value: 'VALID'},
  {label: '无效', value: 'INVALID'},
]

function stateToStr(state) {
  let option = stateOptions.find(item => {
    return item.value == state
  }) || {}
  return option.label || state
}

const configArgs = ref({})

const releaseChange = (arr) => {
  let versions = []
  arr.forEach(itm => {
    versions.push(itm.releaseVersion)
  })
  configArgs.value = {
    releaseVersion: versions
  }
}

const filters = ref([
  {
    type: "RemoteSelect",
    label: "组件",
    rules: [],
    key: "serviceCode",
    props: {
      placeholder: "请选择组件",
      clearable: true,
      filterable: true,
      optionsApi: '/admin/api/querycomponentlist',
      optionsDefaultArgs: {},
      optionsApiType: 'get',
      optionsProps: {
        label: 'serviceCode',
        value: 'serviceCode',
      }
    },
  },
  {
    type: "RemoteSelect",
    label: "配置文件",
    rules: [],
    key: "configTypeCode",
    props: {
      placeholder: "请选择配置文件",
      clearable: true,
      filterable: true,
      optionsApi: '/admin/api/getclassificationlist',
      optionsApiType: 'get',
      optionsProps: {
        label: 'dictName',
        value: 'dictValue',
      }
    },
  },
  {
    "type": "RemoteSelect",
    "label": "高可用",
    "rules": [],
    "key": "itemType",
    "props": {
      "placeholder": "请选择高可用",
      "clearable": true,
      "options": itemTypeOptions
    }
  },
  {
    type: "RemoteSelect",
    label: "SDP版本",
    rules: [],
    key: "stackCode",
    props: {
      placeholder: "请选择SDP版本",
      clearable: true,
      filterable: true,
      optionsApi: '/admin/api/getreleases',
      optionsApiType: 'get',
      optionsProps: {label: 'releaseVersion',value: 'releaseVersion'}
    },
  },
  {
    type: "el-input",
    label: "key",
    key: "key",
    props: { placeholder: "请输入key", clearable: true },
  },
]);

function getList(data) {
  return systemManage.queryconfiglist(data);
}

const TabelContainer = ref(null);

function searchEvent(data) {
  TabelContainer.value.filterEvent(data);
}

function resetEvent(data) {
  searchEvent(data);
}

let dialogVisible = ref(false);

const confForm = reactive({
  id: '',
  stackCode: [],
  itemType: [],
  configTypeCode: '',
  serviceCode: [],
  key: '',
  value: '',
  isContentProp: '',
  isDynamic: '',
  dynamicType: '',
  state: '',
});

const confFormRules = {
  stackCode: FormCheck.required("请选择SDP版本"),
  itemType: FormCheck.required("请选择高可用环境"),
  configTypeCode: FormCheck.required("请选择配置文件"),
  serviceCode: FormCheck.required("请选择组件"),
  key: FormCheck.required("请输入Key"),
  value: FormCheck.required("请输入value"),
  isContentProp: FormCheck.required("请选择是否是Content"),
  isDynamic: FormCheck.required("请输入是否是动态值"),
  dynamicType: FormCheck.required("请输入动态值类型"),
  state: FormCheck.required("请选择状态"),
};

const Ref_confForm = ref(null);

function createEvent() {
  confForm.id = '';
  confForm.stackCode = [];
  confForm.itemType = [];
  confForm.configTypeCode = '';
  confForm.serviceCode = [];
  confForm.key = '';
  confForm.value = '';
  confForm.isContentProp = 0;
  confForm.isDynamic = 0;
  confForm.dynamicType = '';
  confForm.state = 'VALID';

  dialogVisible.value = true;
}

function editEvent(item) {
  confForm.id = item.id;
  confForm.stackCode = [item.stackCode];
  confForm.itemType = [parseInt(item.itemType + '')];
  confForm.configTypeCode = item.configTypeCode;
  confForm.serviceCode = [item.serviceCode];
  confForm.key = item.key;
  confForm.value = item.value;
  confForm.isContentProp = item.isContentProp;
  confForm.isDynamic = item.isDynamic;
  confForm.dynamicType = item.dynamicType;
  confForm.state = item.state;

  dialogVisible.value = true;
}

function addconfig() {
  Ref_confForm.value.validate((valid, fields) => {
    if (valid) {
      systemManage.addconfig(confForm).then(res => {
        if (res.result == true) {
          dialogVisible.value = false;

          searchEvent()
        } else {
          ElMessage.error(res.errorMsg)
        }
      })
    } else {
      console.log("error submit!", fields);
    }
  });
}

function updateconfig() {
  Ref_confForm.value.validate((valid, fields) => {
    if (valid) {
      systemManage.updateconfig(confForm).then(res => {
        if (res.result == true) {
          dialogVisible.value = false;

          searchEvent()
        } else {
          ElMessage.error(res.errorMsg)
        }
      })
    } else {
      console.log("error submit!", fields);
    }
  });
}

const deleteconfig = (item) => {
  ElMessageBox.confirm("您确定需要删除该条数据吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    pageLoading.value = true;
    systemManage.deleteconfig({id: item.id}).then(res => {
      if (res.result == true) {
        ElMessage.success("删除成功！")

        searchEvent();
      } else {
        ElMessage.error(res.errorMsg)
      }
    }).finally(() => {
      pageLoading.value = false;
    })
  })
}

</script>

<style lang="stylus" scoped type="text/stylus">
.index {
  .cluster-info {
    .cluster-name {
      font-size 14px;
    }

    .cluster-id {
      font-size 12px;
    }
  }
  .task-info {
    .task-name {
      font-size 14px;
    }

    .task-id {
      font-size 12px;
    }
  }

  >>> .center-dialog {
    .steps-div {
      margin 0px 100px 20px 130px;
    }

    .theader {
      td, th {
        background-color: #F8F8F8 !important;
      }
    }
  }
}
</style>
