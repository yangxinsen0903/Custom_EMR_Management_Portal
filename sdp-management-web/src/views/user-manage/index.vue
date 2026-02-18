/**Created by liaoyingchao on 12/1/22.*/

<template>
  <div class="index page-css list-css" v-loading="pageLoading">
    <Filters
      :filters="filters"
      @onSearch="searchEvent"
      @onReset="resetEvent"
    ></Filters>
    <div class="functions-row">
      <el-button type="primary" @click="createEvent">创建用户</el-button>
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
            <el-table-column prop="userName" label="用户名" min-width="120">
            </el-table-column>
            <el-table-column prop="realName" label="姓名" min-width="140">
            </el-table-column>
            <el-table-column prop="emNumber" label="员工工号" min-width="140">
            </el-table-column>
            <el-table-column prop="regionList" label="数据中心" min-width="140">
              <template #default="scope">
                <div>
                  <el-tag style="margin: 2px 6px;" v-for="region in scope.row.regionList" effect="light">{{ region.regionName }}</el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="roleName" label="用户角色" min-width="140">
              <template #default="scope">
                <div>
                  <el-tag style="margin: 2px 6px;" type="info" v-for="region in scope.row.baseUserRole" effect="light">{{ region.roleName }}</el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column
              prop="createdTime"
              label="创建时间"
              :formatter="columnTimeFormat"
              min-width="120"
            >
            </el-table-column>
            <el-table-column fixed="right" label="管理" width="180">
              <template #default="scope">
                <div>
                  <el-button
                    type="primary"
                    text
                    @click="editEvent(scope.row)"
                    >编辑</el-button
                  >
                  <el-button
                    type="warning"
                    text
                    @click="resetPwdEvent(scope.row)"
                    >修改密码</el-button
                  >
                  <el-button type="danger" text @click="deleteEvent(scope.row)"
                    >删除</el-button
                  >
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
      :title="userForm.userId ? '编辑用户' : '新建用户'"
      width="700"
      destroy-on-close
    >
      <div>
        <el-form
          ref="Ref_UserForm"
          :model="userForm"
          :rules="userFormRules"
          label-width="100px"
          style="width: 90%"
        >
          <el-form-item label="用户名" prop="userName">
            <el-input
              v-model="userForm.userName"
              placeholder="请输入用户名"
              :disabled="!!userForm.userId"
              maxlength="20"
              clearable
              show-word-limit
            />
            <div class="from-item-tip">
              请设置6-20位，"英文字母+数字"的用户名
            </div>
          </el-form-item>
          <el-form-item label="用户姓名" prop="realName">
            <el-input
              v-model="userForm.realName"
              placeholder="请输入用户姓名"
              maxlength="40"
              clearable
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="员工工号" prop="emNumber">
            <el-input
              v-model="userForm.emNumber"
              placeholder="请输入员工工号"
              maxlength="40"
              clearable
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="密码" prop="password" v-if="!userForm.userId">
            <el-input
              v-model="userForm.password"
              placeholder="请输入密码"
              maxlength="20"
              show-password
              clearable
              show-word-limit
            />
            <div class="from-item-tip">
              请设置6-20位的密码，需要同时包含大小写英文字母和阿拉伯数字和特殊字符
            </div>
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword" v-if="!userForm.userId">
            <el-input
              v-model="userForm.confirmPassword"
              placeholder="请输入确认密码"
              maxlength="100"
              show-password
              clearable
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="数据中心" prop="regions" key="regions">
            <RemoteSelect v-model="userForm.regions" placeholder="请选择数据中心"
                          optionsApi="/admin/meta/selectMetaDataList" :multiple="true"
                          :filterable="true" :optionsDefaultArgs="regionArgs"
                          :optionsProps="{label: 'regionName', value: 'region'}"
                          @change="regionChanged"/>
          </el-form-item>
          <el-form-item label="用户角色" prop="roleCode" key="roleCode">
            <RemoteSelect v-model="userForm.roleCode" placeholder="请选择用户角色"
                          :options="roleOptions"
                          @change="roleChanged"/>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button :loading="pageLoading" type="primary" @click="editUser" v-if="userForm.userId">确定</el-button>
          <el-button :loading="pageLoading" type="primary" @click="createUser" v-else>确定</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog
      class="center-dialog"
      v-model="pwdDialogVisible"
      title="修改密码"
      width="700"
      destroy-on-close
    >
      <div>
        <el-form
          ref="Ref_PwdForm"
          :model="userForm"
          :rules="userFormRules"
          label-width="100px"
          style="width: 90%"
        >
          <el-form-item label="用户名">
            <el-input
              v-model="userForm.userName"
              placeholder="请输入用户名"
              maxlength="100"
              clearable
              show-word-limit
              disabled
            />
          </el-form-item>
          <el-form-item label="用户姓名">
            <el-input
              v-model="userForm.realName"
              placeholder="请输入用户姓名"
              maxlength="100"
              clearable
              show-word-limit
              disabled
            />
          </el-form-item>
          <!--<el-form-item label="旧密码" prop="password">-->
          <!--<el-input v-model="userForm.password" placeholder="请输入旧密码" maxlength="100" show-password clearable show-word-limit/>-->
          <!--</el-form-item>-->
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="userForm.newPassword"
              placeholder="请输入新密码"
              maxlength="100"
              show-password
              clearable
              show-word-limit
            />
            <div class="from-item-tip">
              请设置6-20位的密码，需要同时包含大小写英文字母和阿拉伯数字和特殊字符
            </div>
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmNewPassword">
            <el-input
              v-model="userForm.confirmNewPassword"
              placeholder="请输入确认密码"
              maxlength="100"
              show-password
              clearable
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="pwdDialogVisible = false">关闭</el-button>
          <el-button :loading="pageLoading" type="primary" @click="resetPwd"
            >确定</el-button
          >
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import Filters from "@/components/list-comps/filters";
import ListContainer from "@/components/list-comps/container";
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import userApi from "../../api/user";
import FormCheck from "../../utils/formCheck";
import { columnTimeFormat } from "@/utils/format-time";
import md5 from "js-md5";
import RemoteSelect from "@/components/base/remote-select/remote-select.vue";

const pageLoading = ref(false);

const router = useRouter();

const regionArgs = {type: 'SupportedRegionList'}

const roleOptions = [
  {"label": "超级管理员", "value": 'Administrator'},
  {"label": "运维人员", "value": 'Maintainer'},
  {"label": "普通人员", "value": 'Staff'},
]

const filters = ref([
  {
    type: "el-input",
    label: "用户名",
    key: "userName",
    props: { placeholder: "请输入用户名", clearable: true },
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
    "type": "RemoteSelect",
    "label": "用户角色",
    "rules": [],
    "key": "roleCode",
    "props": {
      "placeholder": "请选择用户角色",
      "clearable": true,
      "collapseTags": true,
      multiple: false,
      collapseTagsTooltip: false,
      "options": roleOptions
    }
  },
]);

function getList(data) {
  return userApi.list(data);
}

const TabelContainer = ref(null);

function searchEvent(data) {
  TabelContainer.value.filterEvent(data);
}

function resetEvent(data) {
  searchEvent(data);
}

let dialogVisible = ref(false);

const userForm = reactive({
  userId: "",
  userName: "",
  realName: "",
  emNumber: "",
  password: "",
  newPassword: "",
  confirmPassword: "",
  confirmNewPassword: "",
  regions: [],
  regionDatas: [],
  roleCode: '',
  roleName: '',
});

function checkConfirmPassword(type) {
  return {
    validator: (rule, value, callback) => {
      if (value) {
        if (type == 1) {
          if (userForm.password != userForm.confirmPassword) {
            return callback(new Error("两次输入密码不一致！"));
          }
        } else {
          if (userForm.newPassword != userForm.confirmNewPassword) {
            return callback(new Error("两次输入密码不一致！"));
          }
        }
      }
      callback();
    },
    trigger: "blur",
  };
}

const userFormRules = {
  userName: [
    FormCheck.required("请输入用户名"),
    FormCheck.lengthLimit(6, 20),
    FormCheck.justLetterAndNumber(),
  ],
  realName: FormCheck.required("请输入用户姓名"),
  emNumber: FormCheck.required("请输入员工工号"),
  password: [
    FormCheck.required("请输入密码"),
    FormCheck.lengthLimit(6, 20),
    FormCheck.complexPassword(),
  ],
  newPassword: [
    FormCheck.required("请输入新密码"),
    FormCheck.lengthLimit(6, 20),
    FormCheck.complexPassword(),
  ],
  confirmPassword: [
    FormCheck.required("请再次输入密码"),
    checkConfirmPassword(1),
  ],
  confirmNewPassword: [
    FormCheck.required("请再次输入密码"),
    checkConfirmPassword(2),
  ],
  regions: FormCheck.required("请选择数据中心"),
  role: FormCheck.required("请选择用户角色"),
};

const Ref_UserForm = ref(null);

const regionChanged = (arr) => {
  userForm.regionDatas = arr
}

const roleChanged = (item) => {
  userForm.roleName = item.label
}

const getRegionsText = (item) => {
  let regions = item.regionList || []
  let arr = []
  regions.forEach(itm => {
    arr.push(itm.regionName || itm.region)
  })

  return arr.join('，')
}

const getRolesText = (item) => {
  let roles = item.baseUserRole || []
  let arr = []
  roles.forEach(itm => {
    arr.push(itm.roleName || itm.roleCode)
  })

  return arr.join('，')
}

function createEvent() {
  dialogVisible.value = true;
  userForm.userId = "";
  userForm.userName = "";
  userForm.realName = "";
  userForm.emNumber = "";
  userForm.password = "";
  userForm.newPassword = "";
  userForm.confirmPassword = "";
  userForm.confirmNewPassword = "";
  userForm.regions = [];
  userForm.regionDatas = [];
  userForm.roleCode = "Staff";
  userForm.roleName = "普通人员";
}

function editEvent(item) {
  let regions = item.regionList || []
  let arr = [], regionDatas = []
  regions.forEach(itm => {
    arr.push(itm.region)
    regionDatas.push(itm)
  })

  let roles = item.baseUserRole || []
  let roleCode = '', roleName = ''
  roles.forEach(itm => {
    roleCode = itm.roleCode
    roleName = itm.roleName
  })

  userForm.userId = item.userId;
  userForm.userName = item.userName;
  userForm.realName = item.realName;
  userForm.emNumber = item.emNumber;
  userForm.regions = arr;
  userForm.regionDatas = regionDatas;
  userForm.roleCode = roleCode;
  userForm.roleName = roleName;

  dialogVisible.value = true;
}

function createUser() {
  Ref_UserForm.value.validate((valid, fields) => {
    if (valid) {
      pageLoading.value = true;
      userApi
        .createUser({
          userName: userForm.userName,
          realName: userForm.realName,
          emNumber: userForm.emNumber,
          password: md5(md5(userForm.userName + userForm.password)),
          regions: userForm.regions,
          roleCode: userForm.roleCode,
          roleName: userForm.roleName,
        })
        .then((res) => {
          if (res.result == true) {
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

function editUser() {
  Ref_UserForm.value.validate((valid, fields) => {
    if (valid) {
      pageLoading.value = true;
      userApi
        .updatePassword({
          userId: userForm.userId,
          userName: userForm.userName,
          realName: userForm.realName,
          emNumber: userForm.emNumber,
          regions: userForm.regions,
          roleCode: userForm.roleCode,
          roleName: userForm.roleName,
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

let pwdDialogVisible = ref(false);

const Ref_PwdForm = ref(null);

function resetPwdEvent(item) {
  pwdDialogVisible.value = true;
  userForm.userName = item.userName;
  userForm.realName = item.realName;
  userForm.password = "";
  userForm.newPassword = "";
  userForm.confirmPassword = "";
  userForm.confirmNewPassword = "";
}

function resetPwd() {
  Ref_PwdForm.value.validate((valid, fields) => {
    if (valid) {
      pageLoading.value = true;
      userApi
        .updatePassword({
          userName: userForm.userName,
          newPassword: md5(md5(userForm.userName + userForm.newPassword)),
        })
        .then((res) => {
          if (res.result == true) {
            ElMessage.success("修改成功！");
            pwdDialogVisible.value = false;
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

function deleteEvent(item) {
  ElMessageBox.confirm("您确定需要删除该用户吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    pageLoading.value = true;
    userApi
      .deleteUser({
        userName: item.userName,
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
