/**Created by liaoyingchao on 12/1/22.*/

<template>
  <div class="index page-css list-css" v-loading="pageLoading">
    <Filters
        :filters="filters"
        @onSearch="searchEvent"
        @onReset="resetEvent"
    ></Filters>
    <div class="functions-row">
      <el-button type="primary" @click="createEvent">新增镜像</el-button>
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
            <el-table-column prop="imgId" label="镜像ID" min-width="180" show-overflow-tooltip></el-table-column>
            <el-table-column prop="releaseVersion" label="版本信息" min-width="100" show-overflow-tooltip></el-table-column>
            <el-table-column prop="imageVersion" label="镜像版本" min-width="100" show-overflow-tooltip></el-table-column>
            <el-table-column prop="osImageId" label="资源ID" min-width="180" show-overflow-tooltip></el-table-column>
            <el-table-column prop="osImageType" label="类型" min-width="100" show-overflow-tooltip></el-table-column>
            <el-table-column prop="osVersion" label="操作系统版本" min-width="120" show-overflow-tooltip></el-table-column>
            <el-table-column
                prop="createdTime"
                label="创建时间"
                :formatter="columnTimeFormat"
                min-width="120"
            >
            </el-table-column>
            <el-table-column fixed="right" label="管理" width="120">
              <template #default="scope">
                <div>
                  <el-button type="" text @click="checkScript(scope.row)">查看脚本</el-button>
                  <el-button type="" text @click="copyISO(scope.row)">复制</el-button>
<!--                  <el-button type="danger" text @click="deleteEvent(scope.row)">删除</el-button>-->
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
        :title="isoForm.id ? '编辑镜像' : '新增镜像'"
        width="900"
        destroy-on-close
    >
      <div>
        <div class="steps-div">
          <Steps v-model="stepIndex" :stepOptions="stepOptions"></Steps>
        </div>
        <div style="height: 400px;overflow-y: auto;">
          <el-form
              v-if="stepIndex == 0"
              ref="Ref_isoForm"
              :model="isoForm"
              :rules="isoFormRules"
              label-width="120px"
          >
            <el-form-item label="应用SDP版本" prop="releaseVersion" key="releaseVersion">
              <RemoteSelect v-model="isoForm.releaseVersion" placeholder="请选择应用SDP版本" optionsApi="/admin/api/getreleases"
                            :clearable="true" :filterable="true" optionsApiType="get"
                            :optionsProps="{label: 'releaseVersion',value: 'releaseVersion'}" />
            </el-form-item>
            <el-form-item label="镜像资源ID" prop="osImageId">
              <el-input
                  v-model="isoForm.osImageId"
                  placeholder="请输入镜像资源ID"
                  clearable
              />
            </el-form-item>
            <el-form-item label="操作系统版本" prop="osVersion">
              <el-input
                  v-model="isoForm.osVersion"
                  placeholder="请输入操作系统版本"
                  clearable
              />
            </el-form-item>
          </el-form>
          <div v-if="stepIndex == 1">
            <div v-for="(item, index) in isoForm.imageScriptList" style="margin-bottom: 18px;border-bottom: 1px solid #ddd;position: relative;">
              <el-form label-width="120px">
                <el-form-item label="脚本名称" prop="scriptName" style="width: 40%;float: left;">
                  <el-input v-model="item.scriptName" placeholder="请输入脚本名称" clearable/>
                </el-form-item>
                <el-form-item label="执行时机" prop="runTiming" style="width: 40%;">
                  <RemoteSelect v-model="item.runTiming" placeholder="请选择执行时机"
                                :filterable="true" :allow-create="true" default-first-option
                                :options="runTimingOptions" />
                </el-form-item>
                <el-form-item label="playbook脚本" prop="playbookUri">
                  <el-input v-model="item.playbookUri" placeholder="请输入playbook脚本" clearable/>
                </el-form-item>
                <el-form-item label="脚本地址" prop="scriptFileUri">
                  <el-input v-model="item.scriptFileUri" placeholder="请输入脚本地址" clearable/>
                </el-form-item>
                <el-form-item label="扩展参数" prop="extraVars">
                  <el-input v-model="item.extraVars" placeholder="请输入扩展参数" clearable/>
                </el-form-item>
              </el-form>
              <div style="position: absolute;right: 0px;top: 0px;">
                <el-button name="移除脚本" type="warning" :icon="Delete"
                           @click="removeConf(index)">移除脚本</el-button>
              </div>
            </div>
            <div style="padding: 15px;display: flex;align-items: center;justify-content: center;
            border: 1px #ddd dashed;margin-top: 10px;cursor: pointer;" @click="addScript">
              <el-icon><Plus/></el-icon>
              <div style="margin-left: 10px;">增加脚本</div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="nextSetp" v-show="stepIndex == 0">下一步</el-button>
          <el-button type="warning" @click="stepIndex = 0" v-show="stepIndex == 1">上一步</el-button>
          <el-button :loading="pageLoading" type="primary" @click="createISO" v-show="stepIndex == 1">确定</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog
        class="center-dialog"
        v-model="checkScriptVisible"
        title="脚本查看"
        width="80%"
        destroy-on-close
    >
      <div>
        <el-table height="400" :data="scriptList" stripe header-row-class-name="theader" style="width: 100%">
          <el-table-column prop="scriptName" label="脚本名称" min-width="120" show-overflow-tooltip></el-table-column>
          <el-table-column prop="runTiming" label="运行时机" min-width="120" show-overflow-tooltip></el-table-column>
          <el-table-column prop="imgId" label="镜像ID" min-width="180" show-overflow-tooltip></el-table-column>
          <el-table-column prop="playbookUri" label="playbook脚本" min-width="120" show-overflow-tooltip></el-table-column>
          <el-table-column prop="scriptFileUri" label="脚本地址" min-width="120" show-overflow-tooltip></el-table-column>
          <el-table-column prop="extraVars" label="扩展参数" min-width="180" show-overflow-tooltip></el-table-column>
        </el-table>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="checkScriptVisible = false">关闭</el-button>
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

const pageLoading = ref(false);

const router = useRouter();

const runTimingOptions = [
  {label: 'install_ambari_server', value: 'install_ambari_server'},
  {label: 'diskscaleout', value: 'diskscaleout'},
  {label: 'collectLogs', value: 'collectLogs'},
  {label: 'clear_ganglia_data', value: 'clear_ganglia_data'},
  {label: 'run_hdfs_fsck', value: 'run_hdfs_fsck'},
  {label: 'bandwidth', value: 'bandwidth'},
  {label: 'clean_ambari_history', value: 'clean_ambari_history'},
  {label: 'install_ambari_agent', value: 'install_ambari_agent'},
  {label: 'install_tez_ui', value: 'install_tez_ui'},
]

const filters = ref([
  {
    type: "RemoteSelect",
    label: "版本信息",
    rules: [],
    key: "releaseVersion",
    props: {
      placeholder: "请选择版本信息",
      clearable: true,
      filterable: true,
      optionsApi: '/admin/api/getreleases',
      optionsApiType: 'get',
      optionsProps: {
        label: 'releaseVersion',
        value: 'releaseVersion',
      }
    },
  }
]);

function getList(data) {
  return systemManage.listImage(data);
}

const TabelContainer = ref(null);

function searchEvent(data) {
  TabelContainer.value.filterEvent(data);
}

function resetEvent(data) {
  searchEvent(data);
}

let dialogVisible = ref(false);

const stepIndex = ref(0)
const stepOptions = [{
  name: '基本信息',
}, {
  name: '脚本信息',
}]

const isoForm = reactive({
  id: '',
  releaseVersion: "",
  osImageId: "",
  osVersion: '',
  imageScriptList: []
});

const isoFormRules = {
  releaseVersion: FormCheck.required("请选择应用SDP版本", 'change'),
  osImageId: FormCheck.required("请输入镜像资源ID"),
  osVersion: FormCheck.required("请输入操作系统版本"),
};

const Ref_isoForm = ref(null);

function createEvent() {
  isoForm.id = '';
  isoForm.releaseVersion = "";
  isoForm.releaseVersion = "";
  isoForm.osImageId = "";
  isoForm.imageScriptList = [];
  stepIndex.value = 0;

  dialogVisible.value = true;
}

function nextSetp() {
  Ref_isoForm.value.validate((valid, fields) => {
    if (valid) {
      stepIndex.value = 1
    } else {
      console.log("error submit!", fields);
    }
  });
}

function addScript() {
  isoForm.imageScriptList.push({
    "scriptName": "",
    "runTiming": "",
    "playbookUri": "",
    "scriptFileUri": "",
    "extraVars": ""
  })
}

function removeConf(index) {
  isoForm.imageScriptList.splice(index, 1)
}

function createISO() {
  pageLoading.value = true;
  systemManage.saveImageScript(isoForm).then(res => {
    if (res.result == true) {
      dialogVisible.value = false;

      ElMessage.success("保存成功！");
      searchEvent();
    } else {
      ElMessage.error(res.errorMsg)
    }
  }).finally(() => {
    pageLoading.value = false;
  })
}

const checkScriptVisible = ref(false);
const scriptList = ref([])

function checkScript(item) {
  systemManage.listImageScript({
    imgId: item.imgId
  }).then(res => {
    if (res.result == true) {
      scriptList.value = res.data || []
      checkScriptVisible.value = true;
    } else {
      ElMessage.error(res.errorMsg)
    }
  })
}

function copyISO(item) {
  isoForm.id = '';
  isoForm.releaseVersion = item.releaseVersion || '';
  isoForm.osVersion = item.osVersion || '';
  isoForm.osImageId = item.osImageId || '';
  isoForm.imageScriptList = item.imageScriptList || [];

  stepIndex.value = 0;

  systemManage.listImageScript({
    imgId: item.imgId
  }).then(res => {
    if (res.result == true) {
      let arr = res.data || []

      arr.forEach(item => {
        isoForm.imageScriptList.push(item)
      })

      dialogVisible.value = true;
    } else {
      ElMessage.error(res.errorMsg)
    }
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
