/**Created by liaoyingchao on 12/5/22.*/

<template>
  <div class="step-1">
    <el-form size="large" ref="RefForm" :model="modelValue" :rules="formRules" label-width="120px">
      <div class="full-page-block-div">
        <div class="block-title">基本配置</div>
        <el-form-item label="集群名称" prop="clusterName">
          <el-input class="input-width" v-model="modelValue.clusterName" placeholder="请输入集群名称" show-word-limit maxlength="39" clearable/>
        </el-form-item>
        <el-form-item label="标签" prop="tagMap">
          <ClusterLabel v-model="modelValue.tagMap"></ClusterLabel>
        </el-form-item>
      </div>
    </el-form>
    <div class="full-page-buttons">
      <el-button class="pre-step" type="warning" size="large" @click="cancelEvent">取消创建</el-button>
      <el-button type="primary" size="large" class="next-step" @click="nextEvent">下一步</el-button>
    </div>
  </div>
</template>

<script setup>
  import {
    Finished
  } from '@element-plus/icons-vue'
  import { useRoute, useRouter } from 'vue-router'
  import userCenter from "@/utils/user-center";
  import store from "@/store";
  import {ref, toRefs, defineProps, defineEmits} from "vue"
  import FormCheck from "../../../utils/formCheck";
  import ClusterLabel from "./label"
  import {useVmodel} from "../../../hooks/useVmodel";
  import clusterApi from "../../../api/cluster";
  import {ElMessage, ElMessageBox} from 'element-plus';
  const router = useRouter()

  const emit = defineEmits();
  const props = defineProps({
    modelValue: Object,
    isCopy: Boolean
  });
  const { isCopy } = toRefs(props)
  const modelValue = useVmodel(props);

  const RefForm = ref(null)

  function checkClusterName() {
    return {
      validator: (rule, value, callback) => {
        if (value) {
          clusterApi.checkClusterName({
            clusterName: value
          }).then(res => {
            if (res.result == true) {
              callback();
            } else {
              // ElMessage.error(res.errorMsg)
              callback(new Error("集群名称已存在！"));
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

  function checkTagMap() {
    return {
      validator: (rule, value, callback) => {
        for (let key in modelValue.value.tagMap) {
          if (['svcid', 'svc', 'service', 'for'].includes(key)) {
            if (!modelValue.value.tagMap[key]) {
              return callback(new Error("'svcid', 'svc', 'service', 'for'这4项必填"));
            }
          }
        }
        callback();
      },
      trigger: 'change'
    }
  }

  const formRules = {
    clusterName: [
      FormCheck.required("请输入集群名称"),
      FormCheck.justLetterAndNumberAnd_(),
      checkClusterName()
    ],
    tagMap: [checkTagMap()]
  }

  function nextEvent() {
    RefForm.value.validate((valid, fields) => {
      if (valid) {
        for (let key in modelValue.value.tagMap) {
          if (!modelValue.value.tagMap[key]) {
              delete modelValue.value.tagMap[key]
          }
        }
        emit('changeStep', 1)
      } else {
        console.log('error submit!', fields)
      }
    })
  }

  function cancelEvent() {
    router.go(-1)
  }

</script>

<style lang="stylus" scoped type="text/stylus">
  .step-1 {

  }
</style>