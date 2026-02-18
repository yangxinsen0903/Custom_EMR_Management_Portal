<template>
  <div class="index page-css list-css" v-loading="pageLoading">
    <Filters
        :filters="filters"
        @onSearch="searchEvent"
        @onReset="resetEvent"
    ></Filters>
    <div class="functions-row">
      <el-button type="primary" @click="newEvent">新建集群信息收集记录</el-button>
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
            <el-table-column
                prop="id"
                label="任务ID"
                min-width="180"
            ></el-table-column>
            <el-table-column
                prop="clusterId"
                label="集群ID"
                min-width="180"
            ></el-table-column>
            <el-table-column
                prop="clusterName"
                label="集群名称"
                min-width="120"
            ></el-table-column>
            <el-table-column
                prop="filePath"
                label="文件路径"
                min-width="200"
            ></el-table-column>
            <el-table-column
                prop="state"
                label="状态"
                min-width="80"
            ></el-table-column>
          </el-table>
        </template>
      </ListContainer>
    </div>
    <el-dialog
        class="center-dialog"
        v-model="dialogVisible"
        title="新建集群信息收集记录"
        width="800"
        destroy-on-close
    >
      <div>
        <el-form
            ref="Ref_DataForm"
            :model="dataForm"
            :rules="formRules"
            label-width="auto"
        >
          <el-form-item label="集群" prop="clusterId">
            <TableSelect :options="clusterList" :tableColumn="clusterColumns" height="400px"
                         v-model="dataForm.clusterId" :mainKey="'clusterId'"
                         @change="clusterChanged">
            </TableSelect>
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
import { ref, reactive } from "vue";
import { shortcuts } from "../../components/js/shortcuts";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import OperationApi from "../../api/operations";
import taskCenterApi from "@/api/task-center";
import { columnTimeFormat, formatTime } from "@/utils/format-time";
import RemoteSelect from "@/components/base/remote-select/remote-select.vue";
import FormCheck from "@/utils/formCheck";
import TableSelect from "@/components/base/table-select/index.vue";
import EvictionHistoryChart from "@/views/cluster/comps/eviction-history-chart.vue";
import PriceHistoryChart from "@/views/cluster/comps/price-history-chart.vue";
import clusterApi from "@/api/cluster";
import metaDataApi from "@/api/meta-data";

const route = useRoute();
const router = useRouter();

const pageLoading = ref(false);
const filters = ref([
  {
    type: 'combo-box',
    key: 'clusterName',
    props: {},
    selOptionType: '',
    options: [
      {
        "type": "el-input",
        "label": "集群名称",
        "rules": [],
        "key": "clusterName",
        "props": {"placeholder": "请输入集群名称", "clearable": true}
      },
      {
        "type": "el-input",
        "label": "集群ID",
        "rules": [],
        "key": "clusterId",
        "props": {"placeholder": "请输入集群ID", "clearable": true}
      }
    ]
  },
]);

const TabelContainer = ref(null);

let lastFilterData = {}

function searchEvent(data) {
  lastFilterData = data
  TabelContainer.value.filterEvent(data);
}

function resetEvent(data) {
  searchEvent(data);
}

function getList(data) {
  return OperationApi.collectClusterInfoList(data);
}

const dialogVisible = ref(false);
const Ref_DataForm = ref(null);
const dataForm = reactive({
  clusterId: ''
});
const formRules = {
  clusterId: FormCheck.required("请选择集群")
};

const clusterList = ref([])
const clusterColumns = [
  {prop: 'clusterId', label: '集群ID', minWidth: 120, isFilter: true},
  {prop: 'clusterName', label: '集群名称', minWidth: 120, isFilter: true},
  {prop: 'ambariNodeNum', label: 'Ambari', width: 80, isFilter: false},
  {prop: 'masterNodeNum', label: 'Master', width: 80, isFilter: false},
  {prop: 'coreNodeNum', label: 'Core', width: 80, isFilter: false},
  {prop: 'taskNodeNum', label: 'Task', width: 80, isFilter: false},
  {prop: 'serviceNum', label: '服务数', width: 80, isFilter: false},
]

function getClusterList() {
  clusterList.value = []
  clusterApi.list({
    pageIndex: 1,
    pageSize: 100,
    state: [2]
  }).then(res => {
    if (res.result == true) {
      let arr = res.data || []
      clusterList.value = arr

    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

getClusterList()

const clusterChanged = () => {

}

const newEvent = () => {
  dataForm.clusterId = ''

  dialogVisible.value = true
}

const saveDataEvent = () => {
  Ref_DataForm.value.validate((valid, fields) => {
    if (valid) {
      pageLoading.value = true;

      let params = {
        ...dataForm
      }

      OperationApi.collectClusterInfoByClusterId(params).then((res) => {
        if (res.result == true) {
          ElMessage.success("创建成功！");
          dialogVisible.value = false

          searchEvent(lastFilterData);
        } else {
          ElMessage.error(res.errorMsg);
        }
      }).finally(() => {
        pageLoading.value = false;
      });
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
</style>
