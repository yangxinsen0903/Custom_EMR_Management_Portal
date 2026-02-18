<template>
<div class="table-select detail-css">
  <div class="table-filter-div" v-if="filterColumns.length">
    <template v-for="item in filterColumns">
      <el-select v-model="filterFrom[item.prop]" :placeholder="item.label" clearable v-if="item.controlType=='select'">
        <el-option v-for="opItem in item.options" :value="opItem"></el-option>
      </el-select>
      <el-input v-model="filterFrom[item.prop]" :placeholder="item.label" clearable v-else></el-input>
    </template>
  </div>
  <el-table :data="tableData" border :height="height" stripe header-row-class-name="theader">
    <el-table-column align="center" prop="isSelected" label="选择" width="60">
      <template #default="scope">
        <el-checkbox v-model="scope.row.isSelected" @change="selectChange(scope.row)" :disabled="scope.row.isDisable" />
      </template>
    </el-table-column>
    <el-table-column v-bind="column" v-for="column in showColumns" align="center" />
    <slot></slot>
  </el-table>
</div>
</template>

<script setup>
import { reactive, ref, onMounted, defineProps, defineEmits, toRefs, watch, computed } from 'vue'
import http from '@/utils/http'
import { ElMessage, ElSelect, ElOption } from 'element-plus'

const emit = defineEmits(['change', 'update:modelValue'])

const props = defineProps({
  options: Array, // 数据选项
  optionsApiType: String, // 请求数据接口
  optionsApi: String, // 请求数据接口
  optionsDefaultArgs: Object, // 请求数据接口参数
  argChecks: Array, // 接口请求之前参数完整性检查
  height: {
    type: String,
    default: '300px'
  }, // 表格高度
  tableColumn: Array, // 列属性 { ... , isFilter: true } isFilter为true抽取为筛选条件
  modelValue: {
    type: [String, Object, Array]
  },
  mainKey: String,
  multiple: false
})

let {options, optionsApiType, optionsApi, optionsDefaultArgs, argChecks, height, tableColumn, modelValue, mainKey, multiple} = toRefs(props)
const optionsList = ref([])

const tableData = computed(() => {
  let arr = optionsList.value || []
  for (let key in filterFrom.value) {
    let v = filterFrom.value[key]
    if (v != '') {
      arr = arr.filter(item => {
        let itemVal = item[key]
        if (itemVal != '' && itemVal != undefined && itemVal != null) {
          if (typeof itemVal == 'number') {
            if (itemVal == v) {
              return true
            }
          } else if (typeof itemVal == 'string') {
            if (itemVal.toLowerCase().indexOf(v.toLowerCase()) != -1) {
              return true
            }
          }
        }
        return false
      })
    }
  }
  return arr
})

const showColumns = computed(() => {
  let arr = tableColumn.value.filter(itm => {
    return itm.noColumn != true
  }) || []

  return arr
})

const filterFrom = ref({})
const filterColumns = ref([])

watch(() => options.value, (newValue, oldValue) => {
  if (newValue && oldValue && JSON.stringify(newValue) == JSON.stringify(oldValue)) {
    // 没有变化
    return ;
  }

  initOptions(options.value || [])
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

const initFilter = () => {
  filterColumns.value = []
  filterFrom.value = {}
  tableColumn.value.forEach(item => {
    if (item.isFilter) {
      if (item.controlType == 'select') {
        if (!item.options || item.options.length == 0) {
          item.options = []
        }
        let arr = []
        optionsList.value.forEach(itm => {
          let option = itm[item.prop] || ''
          let idx = arr.findIndex(it => it == option)
          if (idx == -1) {
            arr.push(option)
          }
        })
        item.options = arr
      }
      filterColumns.value.push(item)
      filterFrom.value[item.prop] = ''
    }
  })
}

const initOptions = (arr) => {
  optionsList.value = arr
  if (mainKey.value) {
    optionsList.value.forEach(itm => {
      itm.isSelected = false
    })

    if (multiple.value) {
      optionsList.value.forEach(itm => {
        let idx = modelValue.value.findIndex(selItem => {
          return itm[mainKey.value] == selItem[mainKey.value]
        })
        if (idx != -1) {
          itm.isSelected = true
        } else {
          itm.isSelected = false
        }
      })
    } else {
      let itemData = optionsList.value.find(item => {
        return item[mainKey.value] == modelValue.value
      }) || {}
      itemData.isSelected = true
    }
  }
}

function selectChange(item) {
  if (multiple.value) {
    // 多选
    let arr = optionsList.value.filter(item => item.isSelected) || []

    if (mainKey.value) {
      emit('update:modelValue', arr)
    }
    emit('change', arr)
  } else {
    // 单选
    if (item.isSelected) {
      optionsList.value.forEach(itm => {
        itm.isSelected = false
      })

      item.isSelected = true
    }

    let itemData = optionsList.value.find(item => {
      return item.isSelected
    }) || {}

    if (mainKey.value) {
      let v = itemData[mainKey.value]
      emit('update:modelValue', v)
    }
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
  if (options.value) {
    initOptions(options.value)
  }
  if (optionsApi.value) {
    getOptionsData()
  }
  initFilter()
})
</script>

<style scoped lang="stylus">
.table-select {
  .table-filter-div {
    display flex;
    align-items center;
    padding-bottom 10px;
    div {
      flex 1;
    }
    div:nth-child(n+2) {
      margin-left 5px;
    }
  }
}
</style>