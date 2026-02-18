/**Created by liaoyingchao on 12/5/22.*/

<template>
  <div class="arguments">
    <div class="arguments-row" v-for="(labelItem, index) in modelValue">
      <div class="label-input">
        <div class="label single-row">分类：</div>
        <el-select style="width: 100%;" v-model="labelItem.classification" @change="changeKey">
          <el-option
                  v-for="option in options"
                  :key="option.dictValue"
                  :label="option.dictName"
                  :value="option.dictValue"
                  :disabled="option.disabled"
          />
        </el-select>
      </div>
      <div class="arguments-items">
        <div class="item-row" v-for="(item, idx) in labelItem.confs">
          <div class="label-input" style="margin-left: 10px;">
            <div class="label single-row">参数：</div>
            <el-input v-model="item.key" placeholder="请输入参数" maxlength="300" show-word-limit clearable
                      @change="changeSave(labelItem)"></el-input>
          </div>
          <div class="label-input">
            <div class="label">值：</div>
            <textarea v-model="item.value" placeholder="请输入值" @change="changeSave(labelItem)"
                      style="width: 100%;resize:none;"></textarea>
            <!--<el-input v-model="item.value" placeholder="请输入值" type="textarea" :autosize="{ minRows: 1.4, maxRows: 1.4 }" clearable @change="changeSave(labelItem)" style="resize:none;"></el-input>-->
          </div>
          <div class="label-btn">
            <el-button text plain :icon="Delete" circle @click="removeItem(labelItem, idx)"/>
          </div>
        </div>
        <div class="item-row" style="margin-bottom: 0; padding: 4px 0;">
          <div style="margin-left: 50px;" class="add-btn" @click="addItem(labelItem)">添加参数</div>
          <el-button name="移除配置" style="margin: 0 2px 0 20px;" text plain :icon="Delete" circle
                     @click="removeConf(index)"/>
        </div>
      </div>
    </div>
    <div class="args-bottom-btns">
      <div class="add-btn" @click="addConf" v-if="options.length > modelValue.length">添加配置分类</div>
      <div class="import-btn" @click="jsonConf">Json参数配置</div>
    </div>
    <el-dialog class="center-dialog"
               v-model="dialogVisible"
               title="Json参数配置"
               width="800"
               destroy-on-close>
      <div>
        <el-form size="large" ref="RefForm" :model="jsonForm" :rules="jsonFormRules">
          <el-form-item prop="jsonStr">
            <el-input  style="height: 50vh;" type="textarea" resize="none" v-model="jsonForm.jsonStr" placeholder="请输入参数配置的Json字符串" clearable/>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="saveEvent">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
  import {
    Delete
  } from '@element-plus/icons-vue'
  import {ref, reactive, toRefs, defineProps, defineEmits, computed} from "vue"
  import {useVmodel} from "@/hooks/useVmodel";
  import clusterApi from "../../../api/cluster";
  import {ElMessage, ElMessageBox} from 'element-plus';

  const emit = defineEmits();
  const props = defineProps({
    modelValue: Object
  });
  const modelValue = useVmodel(props);

  const options = ref([])

  function initConfs() {
    modelValue.value.forEach(item => {
      item.confs = []
      for (let key in item.cfg) {
        item.confs.push({
          key: key,
          value: item.cfg[key]
        })
      }
    })
  }

  initConfs()

  function changeSave(item) {
    setCfg(item)
  }

  function changeKey() {

    resetOptions()
  }

  function resetOptions() {
    options.value.forEach(opt => {
      let dictValue = opt.dictValue

      let idx = modelValue.value.findIndex(itm => {
        return itm.classification == dictValue
      })

      if (idx > -1) {
        opt.disabled = true
      } else {
        opt.disabled = false
      }
    })
  }

  function addConf() {
    modelValue.value.push({
      classification: '',
      cfg: {},
      confs: []
    })
    // labels.value.push({key: '', value: ''})
  }

  function removeConf(idx) {
    modelValue.value.splice(idx, 1)
    resetOptions()
  }

  function addItem(item) {
    item.confs.push({
      key: '',
      value: ''
    })

    setCfg(item)
  }

  function removeItem(item, idx) {
    item.confs.splice(idx, 1)

    setCfg(item)
  }

  function setCfg(item) {
    let confs = item.confs || []

    item.cfg = {}
    confs.forEach(itm => {
      if (itm.key) {
        item.cfg[itm.key] = itm.value
      }
    })
  }

  function getClassificationList() {
    clusterApi.getClassificationList().then(res => {
      if (res.result == true) {
        options.value = res.data
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    })
  }

  getClassificationList()

  let dialogVisible = ref(false)
  const RefForm = ref(null)
  const jsonForm = reactive({
    jsonStr: ''
  })
  const jsonFormRules = {
    jsonStr: checkJson()
  }

  function checkJson() {
    return {
      validator: (rule, value, callback) => {
        if (value) {
          try {
            let json = JSON.parse(jsonForm.jsonStr)
            jsonForm.jsonStr = JSON.stringify(
              json,   //json格式
              null,             //用于转换结果的函数或数组
              4                 //缩进
            );
          } catch (e) {
            return callback(new Error('Json 格式不正确！' + e.message))
          }
        }
        callback()
      },
      trigger: 'blur'
    }
  }

  function jsonConf() {
    dialogVisible.value = true

    let json = JSON.parse(JSON.stringify(modelValue.value))

    json.forEach(item => {
      delete item.confs
    })

    jsonForm.jsonStr = JSON.stringify(
      json,   //json格式
      null,             //用于转换结果的函数或数组
      4                 //缩进
    );
  }
  
  function saveEvent() {
    RefForm.value.validate((valid, fields) => {
      if (valid) {
        let json = JSON.parse(jsonForm.jsonStr)

        json.forEach(item => {
          let cfg = item.cfg || {}
          let confs = []
          for (let key in cfg) {
            confs.push({
              key: key,
              value: cfg[key]
            })
          }
          item.confs = confs
        })

        modelValue.value = json

        dialogVisible.value = false
      } else {
        console.log('error submit!', fields)
      }
    })
  }

</script>

<style lang="stylus" scoped type="text/stylus">
  .arguments {
    width 100%;
    //background-color #F8FAFC;

    .arguments-row {
      display flex;
      align-items flex-start;
      border 1px dashed #ddd;
      padding 10px;
      margin-bottom 10px;

      .label-input {
        flex 1;
        padding-right 20px;
        display flex;
        align-items center;

        .el-select, .el-input {
          flex 1;
        }

        textarea {
          border-color #dcdfe6;
          height 40px;
          padding 10px 10px 10px;
          font-size 14px;
        }

      }

      .arguments-items {
        flex: 2;
        border-left 1px dashed #ddd;
        padding-left 10px;

        .item-row {
          display flex;
          align-items center;
          width 100%;
        }

        .item-row:nth-child(n+2) {
          margin-top 10px;
        }
      }
    }

    .arguments-row:nth-child(n+2) {
    }

    .args-bottom-btns {
      display flex;
      align-items center;

      .add-btn {
        flex 1;
      }

      .import-btn {
        margin-left 10px;
        cursor: pointer;
        border: 1px dashed #ddd;
        font-size: 14px;
        text-align: center;
        height: 32px;
        line-height: 30px;
        color: #7f7f7f;
        padding 0 10px;
      }
    }

    >>>.el-textarea__inner {
      height 100%;
    }
  }
</style>