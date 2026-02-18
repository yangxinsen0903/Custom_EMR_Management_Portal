/**Created by liaoyingchao on 12/25/22.*/

<template>
  <div class="script-task page-css list-css" v-loading="pageLoading">
    <Filters :filters="filters" @onSearch="searchEvent" @onReset="resetEvent"></Filters>
    <div class="functions-row" v-if="clusterData.state == '2'">
      <el-button type="primary" @click="createEvent" :disabled="!permissionCheck.currentPermissionCheck(['Maintainer', 'Administrator'])">新增脚本执行任务</el-button>
    </div>
    <div class="full-container">
      <ListContainer ref="TabelContainer" :listApiFunction="getList">
        <template #default="tableData">
          <el-table
                  :height="tableData.data.tableHeight"
                  :data="tableData.data.tableData"
                  stripe
                  header-row-class-name="theader"
                  style="width: 100%">
            <el-table-column
                    prop="confScriptId"
                    label="任务ID"
                    min-width="120">
            </el-table-column>
            <el-table-column
                    prop="scriptName"
                    label="任务名称"
                    min-width="120">
            </el-table-column>
            <el-table-column
                    prop="scriptPath"
                    label="脚本路径"
                    min-width="140">
            </el-table-column>
            <el-table-column
                    prop="scriptParam"
                    label="脚本参数"
                    min-width="120">
            </el-table-column>
            <el-table-column
                    prop="jobStatus"
                    label="状态"
                    width="120">
              <template #default="scope">
                {{ getStatusStr(scope.row.jobStatus) }}
              </template>
            </el-table-column>
            <el-table-column
                    prop="begTime"
                    label="开始时间"
                    :formatter="columnTimeFormat"
                    width="108">
            </el-table-column>
            <el-table-column
                    prop="endTime"
                    label="结束时间"
                    :formatter="columnTimeFormat"
                    width="108">
            </el-table-column>
            <!--<el-table-column-->
                    <!--fixed="right"-->
                    <!--label="管理"-->
                    <!--width="100">-->
              <!--<template #default="scope">-->
                <!--<div>-->
                  <!--<el-button type="primary" text @click="showDetail(scope.row)">任务详情</el-button>-->
                <!--</div>-->
              <!--</template>-->
            <!--</el-table-column>-->
          </el-table>
        </template>
      </ListContainer>
    </div>
    <el-dialog
            class="center-dialog"
            v-model="dialogVisible"
            title="新增脚本执行任务"
            width="800"
            destroy-on-close
    >
      <div class="dialog-content">
        <el-form ref="Ref_editForm" :model="editForm" :rules="formRules" label-width="120px" style="width: 90%;">
          <el-form-item label="任务名称" prop="jobName">
            <el-input v-model="editForm.jobName" placeholder="请输入任务名称" maxlength="36" show-word-limit/>
            <div class="from-item-tip">长度限制为6-36个字符，只允许包含中文、字母、数字、-、_</div>
          </el-form-item>
          <el-form-item label="脚本位置" prop="scriptPath">
            <div style="display: flex;align-items: center;width: 100%;">
              <el-select style="flex: 1;margin-right: 10px;" v-model="editForm.scriptPath" filterable placeholder="请选择脚本" @change="scriptChange">
                <el-option
                        v-for="option in scriptList"
                        :key="option.scriptId"
                        :label="option.scriptName + ' (上传于：' + option.showTime + ')'"
                        :value="option.blobPath"
                />
              </el-select>
              <el-upload
                      style="display:flex;"
                      action="/admin/api/uploadbasescript"
                      :show-file-list="false"
                      :on-success="updateScriptSuccess"
                      :before-upload="beforeUploadScript"
                      :data="updateData"
              >
                <el-button title="上传新脚本"><el-icon><Plus /></el-icon></el-button>
              </el-upload>
              <el-button style="margin-left: 10px;" title="查看脚本内容" v-show="editForm.scriptPath" @click="showScript">
                <el-icon>
                  <Document/>
                </el-icon>
              </el-button>
              <el-dialog class="center-dialog"
                         v-model="showScriptVisible"
                         title="查看脚本内容"
                         width="800"
                         append-to-body
                         destroy-on-close>
                <div style="max-height: 60vh;overflow-y: auto;white-space: pre-wrap;border: 1px solid #ddd;padding: 10px;">{{ scriptText }}</div>
              </el-dialog>
            </div>
            <div class="from-item-tip">请从历史上传文件中选择或上传新脚本文件(.sh格式)</div>
          </el-form-item>
          <el-form-item label="执行节点" prop="groupName">
            <el-table
                    ref="Ref_multipleTable"
                    header-row-class-name="theader"
                    size="small"
                    :data="tableData"
                    style="width: 100%"
                    @selection-change="handleSelectionChange"
                    border
            >
              <el-table-column type="selection" width="60" />
              <el-table-column prop="groupName" label="实例类型" min-width="100"></el-table-column>
              <el-table-column prop="count" label="实例数量" min-width="100"></el-table-column>
            </el-table>
            <div class="from-item-tip">已选择 {{ totalCount }} 个节点</div>
          </el-form-item>
          <el-form-item label="参数" prop="scriptParam">
            <el-input v-model="editForm.scriptParam" placeholder="请输入参数" maxlength="1000" :autosize="{minRows: 4,maxRows: 10}" type="textarea" show-word-limit/>
            <div class="from-item-tip">参数格式请遵循标准Shell规范</div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button :loading="pageLoading" type="primary" @click="saveEvent">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
  import {
    Check,
    Plus,
    Document
  } from '@element-plus/icons-vue'
  import Filters from '@/components/list-comps/filters'
  import ListContainer from '@/components/list-comps/container'
  import {ref, reactive, toRefs, defineProps, defineEmits, computed} from "vue"
  import {useRouter} from 'vue-router'
  import {ElMessage, ElMessageBox} from 'element-plus';
  import {shortcuts} from "@/components/js/shortcuts";
  import clusterApi from "../../../api/cluster";
  import FormCheck from "../../../utils/formCheck";
  import { columnTimeFormat, formatTime } from "@/utils/format-time";
  import permissionCheck from "@/utils/permission-check";

  const props = defineProps({
    clusterId: {
      type: String,
      default: ''
    },
    clusterData: {
      type: Object,
      default: () => {}
    }
  });
  const { clusterId, clusterData } = toRefs(props)

  const pageLoading = ref(false)

  const router = useRouter();

  const jobStatus = [
    {value: 0, label: '创建完成'},
    {value: 1, label: '执行中'},
    {value: 2, label: '执行完成'},
    {value: 3, label: '执行失败'},
  ]

  function getStatusStr(status) {
    let item = jobStatus.find(itm => {
      return itm.value == status
    }) || {}

    return item.label || status
  }

  const filters = ref([
    {
      type: 'combo-box',
      key: 'taskName',
      props: {},
      selOptionType: '',
      options: [
        {
          "type": "el-input",
          "label": "任务名称",
          "rules": [],
          "key": "jobName",
          "props": {"placeholder": "请输入任务名称", "clearable": true}
        },
        {
          "type": "el-input",
          "label": "任务ID",
          "rules": [],
          "key": "jobId",
          "props": {"placeholder": "请输入任务ID", "clearable": true}
        }
      ]
    }
  ])

  function getList(data) {
    data.clusterId = clusterId.value
    return clusterApi.scriptJobList(data)
  }

  const TabelContainer = ref(null)

  function searchEvent(data) {
    let arr = data.time || []

    if (arr.length == 2) {
      data.begTime = arr[0]
      data.endTime = arr[1]
    } else {
      data.begTime = ''
      data.endTime = ''
    }
    TabelContainer.value.filterEvent(data);
  }

  function resetEvent(data) {
    searchEvent(data)
  }

  let dialogVisible = ref(false)
  const Ref_editForm = ref(null)
  const Ref_multipleTable = ref(null)
  const totalCount = ref(0)

  const editForm = reactive({
    jobName: "",
    scriptName: "",
    scriptPath: "",
    groupName: "",
    scriptParam: "",
  })

  const formRules = {
    jobName: [FormCheck.lengthLimit(6, 36), FormCheck.required("请输入脚本名称"), FormCheck.justChineseNumberLetterZGAnd_()],
    scriptPath: [FormCheck.required("请选择脚本路径", 'change'), checkScriptPath()],
    groupName: FormCheck.required("请选择执行节点", 'change'),
  }

  function checkScriptPath() {
    return {
      validator: (rule, value, callback) => {
        if (value) {
          clusterApi.checkScriptUrl({
            customScriptUri: value
          }).then(res => {
            if (res.result == true) {
              let data = res.data
              if (data) {
                callback();
              } else {
                callback(new Error(res.errorMsg));
              }
            } else {
              callback(new Error("脚本路径不正确！"));
              console.log(res)
            }
          })
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  }

  const scriptList = ref([])
  const updateData = ref({})

  function beforeUploadScript(rawFile) {
    console.log(rawFile)
    let fileName = rawFile.name

    updateData.value = {scriptName: fileName}

    let reg = /\.sh$/
    if (!reg.test(fileName)) {
      ElMessage.error('脚本必须以.sh为后缀')
      return false
    } else if (rawFile.size / 1024 / 1024 > 2) {
      ElMessage.error('脚本大小不能超过 2MB!')
      return false
    }
    return true
  }

  function updateScriptSuccess(res) {
    if (res.result == true) {
      let data = res.data || {}
      let blobPath = data.blobPath || ''
      let scriptName = data.scriptName || ''
      let scriptId = data.scriptId || ''
      let updateTime = data.updateTime || new Date()

      scriptList.value.unshift({
        scriptId: scriptId,
        scriptName: scriptName,
        blobPath: blobPath,
        updateTime: updateTime,
        showTime: formatTime(updateTime),
      })

      editForm.scriptPath = blobPath
      editForm.scriptName = scriptName
    } else {
      ElMessage.error(res.errorMsg)
    }
  }

  function getBaseScriptList() {
    clusterApi.getBaseScriptList({
      pageIndex: 1,
      pageSize: 100
    }).then(res => {
      if (res.result == true) {
        scriptList.value = res.data || []
        scriptList.value.forEach(item => {
          item.showTime = formatTime(item.updateTime);
        })
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    })
  }

  function scriptChange() {
    let selItem = scriptList.value.find(item => {
      return item.blobPath == editForm.scriptPath
    }) || {}

    editForm.scriptName = selItem.scriptName
  }

  function handleSelectionChange(val) {
    let nodes = [], count = 0
    val.forEach(item => {
      nodes.push(item.groupName)
      count += parseInt(item.count)
    })
    editForm.groupName = nodes.join(',')
    totalCount.value = count
  }

  const tableData = ref([])

  function getVMGroupsByClusterId() {
    clusterApi.getVMGroupsByClusterId({
      clusterId: clusterId.value
    }).then(res => {
      if (res.result == true) {
        tableData.value = res.data || []
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    })
  }

  function createEvent() {
    getVMGroupsByClusterId()
    getBaseScriptList()

    editForm.jobName = ''
    editForm.scriptName = ''
    editForm.scriptPath = ''
    editForm.groupName = ''
    editForm.scriptParam = ''

    dialogVisible.value = true
  }

  function saveEvent() {
    pageLoading.value = true
    Ref_editForm.value.validate((valid, fields) => {
      if (valid) {

        clusterApi.saveUserCustomerScript({
          clusterId: clusterId.value,
          jobName: editForm.jobName,
          scriptName: editForm.scriptName,
          scriptPath: editForm.scriptPath,
          groupName: editForm.groupName,
          scriptParam: editForm.scriptParam,
        }).then(res => {

          if (res.result == true) {
            ElMessage.success('脚本任务提交成功！')
            dialogVisible.value = false
          } else {
            ElMessage.error(res.errorMsg)
            console.log(res)
          }
        }).finally(() => {
          pageLoading.value = false
        })

      } else {
        pageLoading.value = false
        console.log('error submit!', fields)
      }
    })
  }

  const scriptText = ref('')
  const showScriptVisible = ref(false)

  function showScript() {
    clusterApi.getScriptContent({filePath: editForm.scriptPath}).then(res => {
      if (res.result == true) {
        scriptText.value = res.data
        showScriptVisible.value = true
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    }).catch(err => {
      console.error(err)
    })
  }

</script>

<style lang="stylus" scoped type="text/stylus">
  .script-task {
    padding 10px 0 10px;

    >>>.center-dialog {
      .dialog-content {
        max-height 65vh;
        overflow-y auto;
      }
    }
  }
</style>