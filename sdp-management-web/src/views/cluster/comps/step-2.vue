/**Created by liaoyingchao on 12/5/22.*/

<template>
  <div class="step-2">
    <el-form size="large" ref="RefForm" :model="modelValue" :rules="formRules" label-width="120px">
      <div class="full-page-block-div">
        <div class="block-title">集群版本</div>
        <div class="flex-row">
          <el-form-item label="产品版本" :prop="'instanceGroupVersion.clusterReleaseVer'">
            <el-select class="input-width" placeholder="请选择产品版本"
                       v-model="modelValue.instanceGroupVersion.clusterReleaseVer" @change="versionChange"
                       :disabled="isCopy">
              <el-option
                      v-for="option in versions"
                      :key="option.releaseVersion"
                      :label="option.releaseVersion"
                      :value="option.releaseVersion"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="应用场景">
            <el-select class="input-width" placeholder="请选择应用场景" v-model="modelValue.scene" @change="sceneChange"
                       :disabled="isCopy">
              <el-option label="默认场景" value=""/>
              <el-option label="HBase" value="HBase"/>
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="组件选择" v-if="apps.length">
          <div class="plugin-div">
            <div class="plugin-item" :class="{disabled: app.required == '1' || isCopy}" v-for="(app, idx) in apps"
                 @click="appClickEvent(app)">
              <div class="text">{{ app.appName }} {{ app.appVersion }}</div>
              <div class="select-div" v-if="app.selected">
                <el-icon color="#fff" size="12px">
                  <Check/>
                </el-icon>
              </div>
            </div>
          </div>
        </el-form-item>
      </div>
      <div class="full-page-block-div">
        <div class="block-title">组件配置</div>
        <el-form-item label="参数配置">
          <ClusterArguments v-model="modelValue.clusterCfgs"></ClusterArguments>
        </el-form-item>
      </div>
      <div class="full-page-block-div">
        <div class="block-title">高级设置</div>
        <!--<el-form-item label="Ambari用户名" prop="ambariUsername">-->
        <!--<el-input class="input-width" v-model="modelValue.ambariUsername" placeholder="请输入Ambari用户名" show-word-limit maxlength="50" clearable disabled/>-->
        <!--</el-form-item>-->
        <!--<el-form-item label="Ambari密码" prop="ambariPassword">-->
        <!--<el-input class="input-width" v-model="modelValue.ambariPassword" placeholder="请输入Ambari密码" type="password"-->
        <!--show-password show-word-limit maxlength="20" clearable/>-->
        <!--</el-form-item>-->
        <el-form-item label="直接销毁白名单">
          <el-switch
                  v-model="modelValue.isWhiteAddr"
                  inline-prompt
                  active-text="on"
                  inactive-text="off"
                  :active-value="1"
                  :inactive-value="0"
          />
        </el-form-item>
        <el-form-item label="关闭保护">
          <el-switch
                  v-model="modelValue.deleteProtected"
                  inline-prompt
                  active-text="on"
                  inactive-text="off"
                  active-value="1"
                  inactive-value="0"
          />
        </el-form-item>
        <el-form-item label="初始化脚本">
          <el-table :data="modelValue.confClusterScript" header-row-class-name="theader" border style="width: 100%">
            <el-table-column prop="runTiming" label="运行时机" min-width="100">
              <template #default="scope">
                {{ scope.row.runTiming == 'aftervminit' ? '实例初始化后' :
                scope.row.runTiming == 'beforestart' ? '集群启动前' : '集群启动后' }}
              </template>
            </el-table-column>
            <el-table-column prop="scriptName" label="名称" min-width="100"/>
            <el-table-column prop="sortNo" label="执行顺序" width="100"/>
            <el-table-column prop="scriptPath" label="脚本位置" min-width="200"/>
            <el-table-column prop="scriptParam" label="参数" min-width="200"/>
            <el-table-column prop="name" label="操作" width="140">
              <template #default="scope">
                <el-button type="primary" text size="small" @click="editScript(scope.row, scope.$index)">编辑</el-button>
                <el-button type="danger" text size="small" @click="deleteScrpit(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="from-item-tip">表格中的所有脚本会根据时机，从上往下执行</div>
          <div style="margin-top: 8px;" class="add-btn" @click="addScript"
               v-if="modelValue.confClusterScript.length < 48">添加引导操作
          </div>
        </el-form-item>
      </div>
    </el-form>
    <div class="full-page-buttons">
      <el-button class="pre-step" type="warning" size="large" @click="cancelEvent">取消创建</el-button>
      <el-button class="pre-step" size="large" @click="preEvent">上一步</el-button>
      <el-button type="primary" size="large" class="next-step" @click="nextEvent">下一步</el-button>
    </div>
    <el-dialog
            class="center-dialog"
            v-model="dialogVisible"
            title="操作引导"
            width="800"
            destroy-on-close
    >
      <div class="dialog-content">
        <el-form ref="Ref_editForm" :model="editForm" :rules="scriptFormRules" label-width="120px" style="width: 90%;">
          <el-form-item label="运行时机" prop="runTiming">
            <el-radio-group v-model="editForm.runTiming">
              <el-radio-button label="aftervminit" :disabled="runTiming_1_list.length == 16">实例初始化后</el-radio-button>
              <el-radio-button label="beforestart" :disabled="runTiming_2_list.length == 16">集群启动前</el-radio-button>
              <el-radio-button label="afterstart" :disabled="runTiming_3_list.length == 16">集群启动后</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="名称" prop="scriptName">
            <el-input v-model="editForm.scriptName" placeholder="请输入名称" maxlength="64" show-word-limit/>
            <div class="from-item-tip">长度限制为1-64个字符，只能包含中文、字母、数字</div>
          </el-form-item>
          <el-form-item label="脚本位置" prop="scriptPath">
            <!--<el-input v-model="editForm.scriptPath" placeholder="请输入脚本位置" maxlength="200" show-word-limit/>-->
            <div style="display: flex;align-items: center;width: 100%;">
              <el-select style="flex: 1;margin-right: 10px;" v-model="editForm.scriptPath" filterable
                         placeholder="请选择脚本">
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
                <el-button title="上传新脚本">
                  <el-icon>
                    <Plus/>
                  </el-icon>
                </el-button>
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
          <el-form-item label="执行顺序" prop="sortNo">
            <el-input v-model="editForm.sortNo" placeholder="请输入执行顺序" maxlength="2" show-word-limit/>
            <div class="from-item-tip">脚本会按照运行时机，然后按照执行顺序执行，执行顺序数值越小越早执行</div>
          </el-form-item>
          <el-form-item label="参数" prop="scriptParam">
            <el-input v-model="editForm.scriptParam" placeholder="请输入参数" maxlength="1000"
                      :autosize="{minRows: 4,maxRows: 10}" type="textarea" show-word-limit/>
            <div class="from-item-tip">参数格式请遵循标准Shell规范</div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEvent">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
  import {
    Document,
    Check,
    Plus
  } from '@element-plus/icons-vue'
  import {ref, reactive, toRefs, defineProps, defineEmits, computed} from "vue"
  import FormCheck from "../../../utils/formCheck";
  import ClusterArguments from "./arguments"
  import clusterApi from "../../../api/cluster";
  import {useVmodel} from "../../../hooks/useVmodel";
  import {ElMessage, ElMessageBox} from 'element-plus';
  import {formatTime} from "../../../utils/format-time";

  const emit = defineEmits();
  const props = defineProps({
    modelValue: Object,
    isCopy: Boolean
  });
  const {isCopy} = toRefs(props)
  const modelValue = useVmodel(props);

  const RefForm = ref(null)

  const formRules = {
    "instanceGroupVersion.clusterReleaseVer": FormCheck.required("请选择产品版本"),
    ambariUsername: FormCheck.required("请输入Ambari用户名"),
    ambariPassword: [FormCheck.required("请输入Ambari密码"), FormCheck.lengthLimit(6, 20), FormCheck.complexPassword()],
  }

  function nextEvent() {
    RefForm.value.validate((valid, fields) => {
      if (valid) {
        emit('changeStep', 2)
      } else {
        ElMessage.error("部分填写数据出错，请检查填写数据！")
        console.log('error submit!', fields)
      }
    })
  }

  function preEvent() {
    emit('changeStep', 0)
  }

  import { useRoute, useRouter } from 'vue-router'
  import {debounce} from "../../../utils/tools";
  const router = useRouter()

  function cancelEvent() {
    router.go(-1)
  }

  // 可用版本
  const versions = ref([])

  function getReleases() {
    clusterApi.getReleases().then(res => {
      if (res.result == true) {
        versions.value = res.data
        if (!modelValue.value.instanceGroupVersion.clusterReleaseVer) {
          if (versions.value.length > 0) {
            modelValue.value.instanceGroupVersion.clusterReleaseVer = versions.value[0].releaseVersion
            console.log("Stack Version: ",modelValue.value.instanceGroupVersion.clusterReleaseVer)
            getReleaseApps()
          }
        }
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    })
  }

  getReleases()

  const apps = ref([])

  let orginApps = []

  if (modelValue.value.instanceGroupVersion.clusterReleaseVer) {
    getReleaseApps()
  }

  function getReleaseApps() {
    clusterApi.getReleaseApps({
      releaseVersion: modelValue.value.instanceGroupVersion.clusterReleaseVer,
      scene: modelValue.value.scene,
    }).then(res => {
      if (res.result == true) {
        orginApps = res.data

        initApps();
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    })
  }

  function initApps() {
    let arr = []
    for (let i = 0; i < orginApps.length; i++) {
      let item = orginApps[i]
      // if ('HBase' == modelValue.value.scene) {
      //   if (item.appName == 'Hive' || item.appName == 'SPARK3' || item.appName == 'TEZ' || item.appName == 'SQOOP') {
      //     continue;
      //   }
      // }
      arr.push(item)
    }
    apps.value = arr

    let clusterApps = modelValue.value.instanceGroupVersion.clusterApps
    if (clusterApps.length) {
      apps.value.forEach(itm => {
        let idx = clusterApps.findIndex(item => {
          return itm.appName.toLowerCase() == item.appName.toLowerCase() && itm.appVersion == item.appVersion
        })
        if (idx > -1) {
          itm.selected = true
        }
      })
    } else {
      apps.value.forEach(itm => {
        if (itm.required == '1') {
          itm.selected = true
        }
      })
      appsChange()
    }
  }

  function versionChange() {
    modelValue.value.instanceGroupVersion.clusterApps = []

    getReleaseApps()
  }

  function sceneChange(val) {
    modelValue.value.instanceGroupVersion.clusterApps = []
    console.log(val)

    if (modelValue.value.scene == 'HBase') {
      modelValue.value.isHa = 1
    }

    getReleaseApps()
    // initApps()
  }

  function appClickEvent(app) {
    if (app.required == '1' || isCopy.value) {
      return;
    }

    app.selected = !app.selected

    if (app.appName.toLowerCase() == 'spark3') {
      let hiveApp = apps.value.find(item => {
        return item.appName.toLowerCase() == 'hive'
      })
      if (hiveApp) {
        if (app.selected) {
          hiveApp.required = 1
          hiveApp.selected = true
        } else {
          hiveApp.required = 0
        }
      }
      let tezApp = apps.value.find(item => {
        return item.appName.toLowerCase() == 'tez'
      })
      if (tezApp) {
        if (app.selected) {
          tezApp.required = 1
          tezApp.selected = true
        } else {
          tezApp.required = 0
        }
      }
    }

    appsChange()
  }

  function appsChange() {
    let clusterApps = []

    apps.value.forEach(itm => {
      if (itm.selected) {
        clusterApps.push({
          appName: itm.appName,
          appVersion: itm.appVersion,
        })
      }
    })

    modelValue.value.instanceGroupVersion.clusterApps = clusterApps
  }

  let dialogVisible = ref(false)

  const editForm = reactive({
    scriptName: "",
    runTiming: "aftervminit",
    scriptPath: "",
    sortNo: 1,
    scriptParam: "",
    editIndex: -1
  })

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
              // ElMessage.error(res.errorMsg)
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

  const scriptFormRules = {
    scriptName: [FormCheck.required("请输入脚本名称"), FormCheck.justChineseNumberLetter()],
    scriptPath: [FormCheck.required("请选择脚本路径", 'change'), checkScriptPath()],
    sortNo: [FormCheck.required("请输入执行顺序"), FormCheck.justPositiveInt()],
    runTiming: FormCheck.required("请选择运行时间")
  }

  const Ref_editForm = ref(null)

  function addScript() {
    getBaseScriptList()
    editForm.scriptName = ''
    editForm.runTiming = 'aftervminit'
    editForm.scriptPath = ''
    editForm.sortNo = 1
    editForm.scriptParam = ''
    editForm.editIndex = -1

    if (runTiming_1_list.value.length < 16) {
      editForm.runTiming = 'aftervminit'
    } else if (runTiming_2_list.value.length < 16) {
      editForm.runTiming = 'beforestart'
    } else {
      editForm.runTiming = 'afterstart'
    }

    dialogVisible.value = true
  }

  const runTiming_1_list = computed(() => {
    let arr = modelValue.value.confClusterScript.filter((item) => {
      return item.runTiming == 'aftervminit'
    })
    return arr
  })
  const runTiming_2_list = computed(() => {
    let arr = modelValue.value.confClusterScript.filter((item) => {
      return item.runTiming == 'beforestart'
    })
    return arr
  })
  const runTiming_3_list = computed(() => {
    let arr = modelValue.value.confClusterScript.filter((item) => {
      return item.runTiming == 'afterstart'
    })
    return arr
  })

  const runTimeToIndex = {
    "aftervminit": 1,
    "beforestart": 2,
    "afterstart": 3,
  }

  const saveEvent =  debounce(function() {
    Ref_editForm.value.validate((valid, fields) => {
      if (valid) {
        let item = {
          scriptName: editForm.scriptName,
          runTiming: editForm.runTiming,
          scriptPath: editForm.scriptPath,
          sortNo: editForm.sortNo,
          scriptParam: editForm.scriptParam
        }
        if (editForm.editIndex > -1) {
          modelValue.value.confClusterScript.splice(editForm.editIndex, 1, item)
        } else {
          modelValue.value.confClusterScript.push(item)
        }

        dialogVisible.value = false

        modelValue.value.confClusterScript = modelValue.value.confClusterScript.sort((a, b) => {
          if (a.runTiming != b.runTiming) {
            let v1 = runTimeToIndex[a.runTiming], v2 = runTimeToIndex[b.runTiming]
            console.log(v1, v2)
            return v1 - v2;
          } else {
            return a.sortNo - b.sortNo
          }
        })

        console.log(modelValue.value.confClusterScript)
      } else {
        console.log('error submit!', fields)
      }
    })
  }, 500)

  function editScript(item, index) {
    getBaseScriptList()
    editForm.scriptName = item.scriptName
    editForm.runTiming = item.runTiming
    editForm.scriptPath = item.scriptPath
    editForm.sortNo = item.sortNo
    editForm.scriptParam = item.scriptParam
    editForm.editIndex = index

    dialogVisible.value = true
  }

  function deleteScrpit(index) {
    modelValue.value.confClusterScript.splice(index, 1)
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
  .step-2 {
    .plugin-div {
      display grid;
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      grid-gap: 15px;
      width 100%;

      .plugin-item {
        display flex;
        align-items center;
        justify-content center;
        position relative;
        background-color #EDEEF3;
        overflow hidden;
        cursor pointer;
        user-select none;

        &.disabled {
          cursor not-allowed;
        }

        .text {
          font-size 14px;
        }

        .select-div {
          position absolute;
          right 0;
          bottom 0;
          width 22px;
          height 22px;
          background: linear-gradient(-45deg, #535E85, #535E85 50%, transparent 50%, transparent 1px);
          padding-top 2px;
          padding-left 9px;
          font-size 0;
        }
      }
    }

    .dialog-content {
      .el-form-item {
        margin-bottom 30px;
      }
    }
  }
</style>
