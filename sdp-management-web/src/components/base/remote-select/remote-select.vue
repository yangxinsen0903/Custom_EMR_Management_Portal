/**Created by liaoyingchao on 2/15/22.*/

<template>
  <el-select class="remote-select" @change="selectChange" v-model="vModelValue" :multiple="multiple">
    <el-option v-for="item in optionsList" :key="item.value" :label="item.label" :value="item.value"></el-option>
  </el-select>
</template>

<script setup>
  import { reactive, ref, onMounted, defineProps, defineEmits, toRefs, watch } from 'vue'
  import http from '@/utils/http'
  import { ElMessage, ElSelect, ElOption } from 'element-plus'

  const vModelValue = ref('')

  const emit = defineEmits(['change', 'update:modelValue', 'optionsLoaded'])

  const props = defineProps({
    options: Array, // 数据选项
    optionsApiType: String, // 请求数据接口
    optionsApi: String, // 请求数据接口
    optionsDefaultArgs: Object, // 请求数据接口参数
    optionsProps: Object, // label 和 value 的key
    argChecks: Array,
    modelValue: '',
    multiple: false
  })

  let {options, optionsApiType, optionsApi, optionsDefaultArgs, optionsProps, argChecks, modelValue, multiple} = toRefs(props)
  const optionsList = ref([])

  watch(() => modelValue.value, (newValue, oldValue) => {
    if (newValue != vModelValue.value) {
      vModelValue.value = newValue
    }
  });

  watch(() => optionsDefaultArgs.value, (newValue, oldValue) => {
    if (newValue && oldValue && JSON.stringify(newValue) == JSON.stringify(oldValue)) {
      // 没有变化
      return ;
    }

    if (optionsApi.value) {
      getOptionsData()
    }
  });

  const initOptions = (arr) => {
    let labelKey = '',
        valueKey = ''
    if (optionsProps.value) {
      labelKey = optionsProps.value.label
      valueKey = optionsProps.value.value
    }
    if (labelKey && valueKey) {
      optionsList.value = arr.map((item) => {
        return {
          ...item,
          label: item[labelKey],
          value: item[valueKey],
        }
      })
    } else {
      optionsList.value = arr
    }

    if (!multiple.value) {
      let idx = optionsList.value.findIndex(itm => {
        return itm.value == modelValue.value
      })
      if (idx == -1) {
        emit('update:modelValue', '')
      }
    }

    emit('optionsLoaded', optionsList.value)
  }

  function selectChange(selValue) {
    emit('update:modelValue', selValue)
    if (multiple.value) {
      let arr = []
      for (let idx in selValue) {
        let itemData = optionsList.value.find(item => {
          return item.value == selValue[idx]
        }) || {}
        arr.push(itemData)
      }
      emit('change', arr)
    } else {
      let itemData = optionsList.value.find(item => {
        return item.value == selValue
      }) || {}
      emit('change', itemData)
    }
  }

  function getOptionsData() {
    let args = {}
    if (optionsDefaultArgs.value) {
      args = optionsDefaultArgs.value
    }

    if (argChecks.value) {
      for (let i = 0; i < argChecks.value.length; i++) {
        let key = argChecks.value[i]
        if (!args[key]) {
          console.log('参数不完整，请先选择数据中心')
          return ;
        }
      }
    }

    if (optionsApiType.value == 'get') {
      http.get(optionsApi.value, {
        params: args
      }).then((res) => {
        if (res.result == true) {
          let arr = res.data || []
          initOptions(arr)
        } else {
          ElMessage.error(res.errorMsg)
        }
      }).catch((err) => {
        ElMessage.error({message: `exportOrder err ${err}`})
      })
    } else {
      http.post(optionsApi.value, args).then((res) => {
        if (res.result == true) {
          let arr = res.data || []
          initOptions(arr)
        } else {
          ElMessage.error(res.errorMsg)
        }
      }).catch((err) => {
        ElMessage.error({message: `exportOrder err ${err}`})
      })
    }
  }

  onMounted(() => {
    vModelValue.value = modelValue.value || ''
    if (options.value) {
      initOptions(options.value)
    }
    if (optionsApi.value) {
      getOptionsData()
    }
  })
</script>

<style lang="stylus" scoped type="text/stylus">
  .remote-select {
    width 100%;
  }
</style>
