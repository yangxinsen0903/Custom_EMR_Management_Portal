/**Created by liaoyingchao on 12/5/22.*/

<template>
  <div class="label">
    <div class="label-row" v-for="(labelItem, idx) in labels">
      <div class="label-input">
        <el-select style="width: 100%;"
                   placeholder="请选择或输入标签Key后回车保存"
                   v-model="labelItem.key"
                   @change="changeKey(labelItem)"
                   filterable
                   allow-create
                   default-first-option
                   :disabled="keyDisabled(labelItem)">
          <el-option
                  v-for="option in options"
                  :key="option.dictValue"
                  :label="option.dictName"
                  :value="option.dictValue"
                  :disabled="option.disabled"
          />
        </el-select>
      </div>
      <div class="label-input" v-if="labelItem.key == 'svcid'">
        <el-select style="width: 100%;" class="remote-select"
                   v-model="selectedSvcId"
                   :multiple="false"
                   :allow-create="true"
                   :remote="true"
                   :filterable="true"
                   :default-first-option="false"
                   :reserve-keyword="true"
                   :remote-show-suffix="true"
                   :remote-method="loadServiceFromRemote"
                   @change="svcIdChanged"
        >
          <el-option v-for="item in serviceList"
                     :key="item.svc"
                     :label="item.svcid"
                     :value="item.svcid">
          </el-option>
        </el-select>
      </div>
      <div class="label-input" v-else-if="labelItem.key == 'svc'">
        <el-input style="width: 100%;" v-model="labelItem.value" @change="setModelValue" :disabled="false"></el-input>
      </div>
      <div class="label-input" v-else-if="labelItem.key == 'for'">
        <el-input style="width: 100%;" v-model="labelItem.value" @change="setModelValue"></el-input>
      </div>
      <div class="label-input" v-else-if="labelItem.key == 'service'">
        <el-select style="width: 100%;" class="remote-select"
                   v-model="selectedSystem"
                   :multiple="false"
                   :allow-create="true"
                   :remote="true"
                   :filterable="true"
                   :default-first-option="false"
                   :reserve-keyword="true"
                   :remote-show-suffix="true"
                   :remote-method="loadSystemFromRemote"
                   @change="setModelValue"
        >
          <el-option v-for="item in systemList"
                     :key="item.svcid"
                     :label="item.svc"
                     :value="item.svc">
          </el-option>
        </el-select>
      </div>
      <div class="label-input" v-else>
        <el-select style="width: 100%;" placeholder="请选择或输入标签Value后回车保存" v-model="labelItem.value"
                   @change="changeValue(labelItem)" filterable allow-create default-first-option>
          <el-option
                  v-for="option in valueOptions[labelItem.key]"
                  :key="option.tagVal"
                  :label="option.tagVal"
                  :value="option.tagVal"
          />
        </el-select>
      </div>
      <div class="label-btn">
        <el-button text :icon="Delete" circle @click="removeRow(idx)" :disabled="keyDisabled(labelItem)"/>
      </div>
    </div>
    <div style="width: 100%;padding-right: 50px;">
      <div class="add-btn" @click="addRow">添加标签</div>
    </div>
  </div>
</template>

<script setup>
  import {
    Delete
  } from '@element-plus/icons-vue'
  import {ref, toRefs, defineProps, defineEmits, computed} from "vue"
  import {useVmodel} from "@/hooks/useVmodel";
  import clusterApi from "../../../api/cluster";
  import {ElMessage, ElMessageBox, ElOption, ElSelect} from 'element-plus';
  import RemoteSelect from "@/components/base/remote-select/remote-select.vue";
  import store from "@/store";
  import http from "@/utils/http"

  const emit = defineEmits();
  const props = defineProps({
    modelValue: Object
  });
  const modelValue = useVmodel(props);

  // 加载svc的URL
  const svcApiUrl = "/admin/api/getservicelist"
  // 选中的svcId Tag
  const selectedSvcId = ref('')
  // 加载出来的service列表
  const serviceList = ref([])

  // 加载System的URL
  const systemUrl = "/admin/api/getsystemlist"
  // 选中的System Tag
  const selectedSystem = ref('')
  // 加载出来的System列表
  const systemList = ref([])

  const options = ref([])

  const labels = ref([])


  function keyDisabled(item) {
    return ['svcid', 'svc', 'service', 'for'].includes(item.key);
  }

  function initLabels() {
    labels.value = []
    for (let key in modelValue.value) {
      if (key) {
        labels.value.push({
          key: key,
          value: modelValue.value[key]
        })
      }
    }
    if (labels.value.length == 0) {
      labels.value.push({
        key: '',
        value: ''
      })
    }

    // 设置for Tag的值
    let labelItem = labels.value.find(itm => {
      return itm.key == 'for'
    }) || {}
    if (modelValue.value['for']) {
      labelItem.value = modelValue.value['for']
    } else {
      labelItem.value = store.state.userInfo.userName
    }

    // 设置svcId Tag的值
    let svcIdLabel = labels.value.find(itm => {
      return itm.key == 'svcid'
    })
    selectedSvcId.value = svcIdLabel.value

    // 设置service Tag的值
    let systemLabel = labels.value.find(itm => {
      return itm.key == 'service'
    })
    selectedSystem.value = systemLabel.value

    resetOptions()
  }

  // 从服务器端加载CMDB的Service信息
  function loadServiceFromRemote(param) {
    console.log("Service Tag内容有变动，重新加载：", param)
    http.get(svcApiUrl, {
      params: {param:param}
    }).then((res) => {
      if (res.result == true) {
        let arr = res.data || []
        console.log(res.data)
        serviceList.value = arr
      } else {
        ElMessage.error(res.errorMsg)
      }
    }).catch((err) => {
      ElMessage.error({message: `exportOrder err ${err}`})
    })
  }

  function loadSystemFromRemote(param) {
    console.log("System Tag内容有变动，重新加载：", param)
    http.get(systemUrl, {
      params: {param:param}
    }).then((res) => {
      if (res.result == true) {
        let arr = res.data || []
        console.log(res.data)
        systemList.value = arr
      } else {
        ElMessage.error(res.errorMsg)
      }
    }).catch((err) => {
      ElMessage.error({message: `exportOrder err ${err}`})
    })
  }

  function setModelValue() {
    for (let key in modelValue.value) {
      delete modelValue.value[key]
    }

    labels.value.forEach(item => {
      if (item.key) {
        modelValue.value[item.key] = item.value || ''
      }
    })
    // 设置固定的两个Tag的值：
    modelValue.value["svcid"] = selectedSvcId.value
    modelValue.value["service"] = selectedSystem.value

    console.log("当前设置的Tag值为：", modelValue.value)
  }

  function resetOptions() {
    options.value.forEach(opt => {
      let dictValue = opt.dictValue
      if (modelValue.value[dictValue] != undefined) {
        opt.disabled = true
      } else {
        opt.disabled = false
      }
    })
  }

  function systemChanged(item) {
    setModelValue()
  }


  // svcid 选项修改后的事件处理函数
  // 同步更新
  function svcIdChanged(item) {
    console.log("ServiceId值修改：", item)
    console.log("svcId的options:", serviceList.value)

    // 找到labels里的svc
    let labelItem = labels.value.find(itm => {
      return itm.key == 'svc'
    }) || {}
    console.log('svc label value:', labelItem)

    // 到svcId从服务器端加载的数据里找到全部选择的数据
    let optionItem = serviceList.value.find(itm => {
      return itm.svcid == item
    })
    console.log('selected svcid data:', optionItem)

    if (optionItem) {
      labelItem.value = optionItem.svc
    }

    setModelValue()
  }

  function changeValue(labelItem) {
    console.log("值修改：", labelItem.key, " -> ", labelItem.value)
    if (labelItem.value.length > 100) {
      labelItem.value = ''
      ElMessage.error("输入内容过长，最多不超过100字")
    }
    var reg = /^[\u4e00-\u9fa5a-zA-Z0-9\-_]*$/
    if (!reg.test(labelItem.value)) {
      labelItem.value = ''
      ElMessage.error("仅支持中文、字母、数字、-以及_")
    }
    setModelValue()
  }

  function changeKey(labelItem) {
    if (labelItem.key.length > 100) {
      labelItem.key = ''
      ElMessage.error("输入内容过长，最多不超过100字")
    }
    var reg = /^[\u4e00-\u9fa5a-zA-Z0-9\-_]*$/
    if (!reg.test(labelItem.key)) {
      labelItem.key = ''
      ElMessage.error("仅支持中文、字母、数字、-以及_")
    }

    // tag名称不允许输入“BLOB”、“ADLS”“Azure”“Microsoft”字样，不区分大小写
    if (labelItem.key) {
      let key = labelItem.key.toLowerCase()
      if (["blob","adls","azure","microsoft"].indexOf(key) > -1) {
        ElMessage.error('tag名称不允许为BLOB、ADLS、Azure、Microsoft字样，不区分大小写')
        labelItem.key = ''
      }
    }

    labelItem.value = ''
    setModelValue()

    resetOptions()

    getKeyValues(labelItem.key)
  }

  const valueOptions = ref({})

  function getKeyValues(key) {
    if (valueOptions.value[key] && valueOptions.value[key].length) {
      return ;
    }
    clusterApi.getTagValueList({tagKey: key}).then(res => {
      if (res.result == true) {
        valueOptions.value[key] = res.data
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    })
  }

  function addRow() {
    labels.value.push({key: '', value: ''})
  }

  function removeRow(idx) {
    let item = labels.value[idx]
    let key = item.key
    if (key) {
      delete modelValue.value[key]

      resetOptions()
    }
    labels.value.splice(idx, 1)
  }

  function gettagKeyList() {
    clusterApi.gettagKeyList().then(res => {
      if (res.result == true) {
        options.value = res.data

        initLabels()
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    })
  }

  gettagKeyList()

</script>

<style lang="stylus" scoped type="text/stylus">
  .label {
    min-width 660px;
    //background-color #F8FAFC;

    .label-row {
      display flex;
      align-items center;
      margin-bottom 10px;

      .label-input {
        margin-right 10px;
        width 300px;
      }
    }
    .label-row:nth-child(n+2) {

    }

    .add-btn {
      cursor pointer;
      border 1px dashed #ddd;
      width 100%;
      font-size 14px;
      text-align center;
      height 32px;
      line-height 30px;
      color #7f7f7f;
    }
  }
</style>
