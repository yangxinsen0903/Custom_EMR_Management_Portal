/**Created by liaoyingchao on 12/1/22.*/

<template>
  <div class="index page-css list-css" v-loading="pageLoading">
    <Filters
      :filters="filters"
      @onSearch="searchEvent"
      @onReset="resetEvent"
    ></Filters>
    <div class="functions-row">
      <el-button type="primary" @click="createEvent">创建AuthKey</el-button>
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
            <el-table-column prop="id" label="ID" width="80">
            </el-table-column>
            <el-table-column prop="name" label="AuthKey名称" min-width="120">
            </el-table-column>
            <el-table-column prop="accessKey" label="accessKey" min-width="160">
            </el-table-column>
            <el-table-column prop="permission" label="操作权限" min-width="120">
              <template #default="scope">
                {{ getPermissionText(scope.row) }}
              </template>
            </el-table-column>
            <el-table-column
              prop="expirationDate"
              label="过期时间"
              :formatter="columnTimeFormat"
              min-width="120"
            >
            </el-table-column>
            <el-table-column prop="state" label="数据状态" min-width="100">
              <template #default="scope">
                {{ getStateText(scope.row) }}
              </template>
            </el-table-column>
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
                  <el-button type="" text @click="editEvent(scope.row)">编辑</el-button>
                  <el-button type="danger" text @click="deleteEvent(scope.row)">删除</el-button>
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
      :title="authKeyForm.id ? '编辑AuthKey' : '新建AuthKey'"
      width="700"
      destroy-on-close
    >
      <div>
        <el-form
          ref="Ref_AuthKeyForm"
          :model="authKeyForm"
          :rules="authKeyFormRules"
          label-width="120px"
        >
          <el-form-item label="AuthKey名称" prop="name">
            <el-input
              v-model="authKeyForm.name"
              placeholder="请输入AuthKey名称"
              maxlength="40"
              clearable
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="操作权限" prop="permission" key="permission">
            <RemoteSelect v-model="authKeyForm.permission" placeholder="请选择操作权限"
                          :options="permissionOptions" />
          </el-form-item>
          <el-form-item label="有效期" prop="expirationDate">
            <el-date-picker
                v-model="authKeyForm.expirationDate"
                type="datetime"
                placeholder="请选择有效期"
                style="width: 100%"
            />
            <div class="from-item-tip">
              有效期结束之后，auth key将不能再使用了
            </div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button :loading="pageLoading" type="primary" @click="editAuthKey" v-if="authKeyForm.id">确定</el-button>
          <el-button :loading="pageLoading" type="primary" @click="createAuthKey" v-else>确定</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog
      class="center-dialog"
      v-model="showAuthVisible"
      title="创建成功"
      width="500"
      destroy-on-close
    >
      <div>
        <el-form label-width="120px" label-position="top">
          <el-form-item label="AccessKey">
            <div style="color: #3160ed;cursor: pointer;" @click="copyText(authData.accessKey)">{{ authData.accessKey }}</div>
            <div class="from-item-tip">点击上面文字可以复制</div>
          </el-form-item>
          <el-form-item label="SecretKey">
            <div style="color: #3160ed;cursor: pointer;" @click="copyText(authData.secretKey)">{{ authData.secretKey }}</div>
            <div class="from-item-tip">点击上面文字可以复制</div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="copyAuthKey">复制并关闭</el-button>
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
import authKeyApi from "../../../api/system-manage";
import FormCheck from "../../../utils/formCheck";
import {columnTimeFormat, formatTime, getLocalTimeStr, timeToUtcTime} from "@/utils/format-time";
import md5 from "js-md5";
import RemoteSelect from "@/components/base/remote-select/remote-select.vue";
import clipboard from "@/utils/clipboard";

const pageLoading = ref(false);

const router = useRouter();

const permissionOptions = [
  {"label": "只读", "value": 'READ'},
  {"label": "读写", "value": 'READWRITE'},
]

const getPermissionText = (item) => {
  let option = permissionOptions.find((itm) => {
        return itm.value == item.permission;
      }) || {};
  return option.label || '';
}

const stateOptions = [
  {"label": "全部", "value": ''},
  {"label": "有效", "value": 'VALID'},
  {"label": "作废", "value": 'INVALID'},
  {"label": "已过期", "value": 'EXPIRED'},
]

const getStateText = (item) => {
  let option = stateOptions.find((itm) => {
    return itm.value == item.status;
  }) || {};
  return option.label || '';
}

const filters = ref([
  {
    type: "el-input",
    label: "AuthKey名称",
    key: "name",
    props: { placeholder: "请输入AuthKey名称", clearable: true },
  },
]);

function getList(data) {
  return authKeyApi.queryauthkeylist(data);
}

const TabelContainer = ref(null);

function searchEvent(data) {
  TabelContainer.value.filterEvent(data);
}

function resetEvent(data) {
  searchEvent(data);
}

let dialogVisible = ref(false);

const authKeyForm = reactive({
  id: '',
  name: "",
  permission: "",
  expirationDate: ''
});

const authKeyFormRules = {
  name: FormCheck.required("请输入AuthKey名称"),
  permission: FormCheck.required("请选择操作权限"),
  expirationDate: FormCheck.required("请选择有效期"),
};

const Ref_AuthKeyForm = ref(null);

function createEvent() {
  console.log(authKeyForm)
  authKeyForm.id = '';
  authKeyForm.name = "";
  authKeyForm.permission = "READ";
  authKeyForm.expirationDate = getLocalTimeStr();

  dialogVisible.value = true;
}

function editEvent(item) {
  authKeyForm.id = item.id;
  authKeyForm.name = item.name;
  authKeyForm.permission = item.permission;
  authKeyForm.expirationDate = formatTime(item.expirationDate);

  dialogVisible.value = true;
}

function createAuthKey() {
  Ref_AuthKeyForm.value.validate((valid, fields) => {
    if (valid) {
      pageLoading.value = true;
      authKeyApi
        .createauthkey({
          name: authKeyForm.name,
          permission: authKeyForm.permission,
          expirationDate: timeToUtcTime(authKeyForm.expirationDate)
        })
        .then((res) => {
          if (res.result == true) {
            authData.value = res.data || {}
            showAuthVisible.value = true

            ElMessage.success("创建成功！");
            dialogVisible.value = false;
            searchEvent();
          } else {
            ElMessage.error(res.errorMsg);
          }
        })
        .finally(() => {
          pageLoading.value = false;
        });
    } else {
      console.log("error submit!", fields);
    }
  });
}

function editAuthKey() {
  Ref_AuthKeyForm.value.validate((valid, fields) => {
    if (valid) {
      pageLoading.value = true;
      authKeyApi
        .updateauthkey({
          id: authKeyForm.id,
          name: authKeyForm.name,
          permission: authKeyForm.permission,
          expirationDate: timeToUtcTime(authKeyForm.expirationDate)
        })
        .then((res) => {
          if (res.result == true) {
            ElMessage.success("更新成功！");
            dialogVisible.value = false;
            searchEvent();
          } else {
            ElMessage.error(res.errorMsg);
          }
        })
        .finally(() => {
          pageLoading.value = false;
        });
    } else {
      console.log("error submit!", fields);
    }
  });
}

let showAuthVisible = ref(false);

const authData = ref({})

function copyText(copyStr) {
  if (copyStr != '-') {
    clipboard(copyStr, (result) => {
      if (result) {
        ElMessage.success("复制成功！");
      } else {
        ElMessage.error("复制失败！");
      }
    })
  }
}

function copyAuthKey() {
  copyText(authData.value.secretKey)

  showAuthVisible.value = false
}

function deleteEvent(item) {
  ElMessageBox.confirm("您确定需要删除该条Auth Key吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    pageLoading.value = true;
    authKeyApi
      .deleteauthkey({
        id: item.id,
      })
      .then((res) => {
        if (res.result == true) {
          ElMessage.success("删除成功！");
          searchEvent();
        } else {
          ElMessage.error(res.errorMsg);
        }
      })
      .finally(() => {
        pageLoading.value = false;
      });
  });
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

  }
}
</style>
