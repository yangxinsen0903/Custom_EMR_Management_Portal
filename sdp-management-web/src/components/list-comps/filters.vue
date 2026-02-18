/**Created by liaoyingchao on 5/1/22.*/

<template>
  <div class="filters">
    <el-form @submit.native.prevent :model="filterData" :label-width="labelWidth || '115px'">
      <div class="filter-items">
        <template v-for="(item, idx) in filters">
          <template v-if="item.type == 'combo-box'">
            <el-form-item :prop="item.key" :label="item.label" :key="idx">
              <template #label="label">
                <el-select v-model="item.key" @change="comboBoxTypeChange(item)">
                  <el-option
                          v-for="option in item.options"
                          :key="option.key"
                          :label="option.label"
                          :value="option.key"
                  />
                </el-select>
              </template>
              <component :is="getCompWithType(item.selOptionType)" v-model="filterData[item.key]" v-bind="item.props" @keyup.enter.native="onSearch"></component>
            </el-form-item>
          </template>
          <template v-else-if="item.type != 'hidden'">
            <el-form-item :prop="item.key" :label="item.label" :key="item.key">
              <component :is="getCompWithType(item.type)" v-model="filterData[item.key]" v-bind="item.props" @keyup.enter.native="onSearch" style="width: 100%;"></component>
            </el-form-item>
          </template>
        </template>
        <div class="btns-div">
          <el-button type="primary" @click="onSearch">
            <el-icon style="vertical-align: middle">
              <component :is="getButtonIcon('Search')"/>
            </el-icon>
            <span style="vertical-align: middle">搜索</span>
          </el-button>
          <el-button @click="onReset">
            <el-icon style="vertical-align: middle">
              <component :is="getButtonIcon('Refresh')"/>
            </el-icon>
            <span style="vertical-align: middle">重置</span>
          </el-button>
          <!--<el-button @click="onReset">-->
            <!--<el-icon style="vertical-align: middle">-->
              <!--<component :is="getButtonIcon('Refresh')"/>-->
            <!--</el-icon>-->
            <!--<span style="vertical-align: middle">重置</span>-->
          <!--</el-button>-->
        </div>
      </div>
    </el-form>
  </div>
</template>

<script setup>
  import { ref, reactive, defineEmits, defineProps, toRefs, onMounted } from 'vue'
  import { getButtonIcon } from "./list-utils";
  import RemoteSelect from "@/components/base/remote-select/remote-select.vue"

  const emit = defineEmits(['onSearch', 'onReset'])

  const props = defineProps({
    labelWidth: String,
    filters: {
      type: Array,
      default: () => [],
    },
  })

  const filterData = reactive({})

  const { filters, labelWidth } = toRefs(props)

  function initFilters() {
    filters.value.forEach(item => {
      if (item.type == 'combo-box') {
        let key = item.key
        let option = null
        if (key) {
          filterData[item.key] = item.default || ''
          option = item.options.find(itm => {
            return itm.key == key
          }) || null
        }
        if (option == null) {
          option = item.options[0]
        }
        item.key = option.key
        item.props = option.props
        item.selOptionType = option.type
      } else {
        filterData[item.key] = item.default || ''
      }
    })
  }

  initFilters()

  function comboBoxTypeChange(item) {
    item.options.forEach(itm => {
      filterData[itm.key] = itm.default || ''
    })

    let key = item.key
    let option = item.options.find(itm => {
      return itm.key == key
    })
    item.key = option.key
    item.props = option.props
    item.selOptionType = option.type
  }

  const onSearch = () => {
    emit('onSearch', filterData)
  }

  const onReset = () => {
    initFilters();

    emit('onReset', filterData)
  }

  const getCompWithType = (type) => {
    switch (type) {
      case 'RemoteSelect':
        return RemoteSelect
    }
    return type
  }

  onMounted(() => {
    emit('onSearch', filterData)
  })

</script>

<style lang="stylus" scoped type="text/stylus">
  .filters {
    background-color #F8F8F8;
    padding 15px 10px;
    .filter-items {
      /*display flex;
      align-items flex-start;
      flex-wrap wrap;*/
      display grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      grid-gap: 12px;
      >>>.el-form-item {
        margin-bottom 0;
        //margin-right 10px;
        .el-form-item__content {
          width 100%;
          //width 200px;
        }
      }
      .btns-div {
        margin-left 10px;
        // flex-grow: 1;
        // flex 1;
        display flex;
        align-items center;
        // justify-content flex-end;
        flex-wrap nowrap;
        //margin-bottom 18px;
        text-align right;
      }
    }
  }
</style>