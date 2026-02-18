<template>
<div class="business-config">
  <div class="content">
    <div class="left-menus">
      <div class="menu-item" :class="{active: category.category == selectedCategory.category}"
           v-for="category in allConfigs" @click="selectCategory(category)">
        {{ category.category }}
      </div>
    </div>
    <div class="config-items">
      <div class="category-title">{{ selectedCategory.category }}</div>
      <div class="config-row" v-for="conf in selectedCategory.configs">
        <div class="label">
          <div style="margin-right: 6px;">{{conf.name}}</div>
          <el-tooltip :content="conf.remark" placement="bottom" effect="light" v-if="conf.remark">
            <el-icon style="display: flex;" color="#999"><InfoFilled /></el-icon>
          </el-tooltip>
        </div>
        <div>
          <el-input v-model="conf.cfgValue" style="width: 400px" placeholder="请输入" />
        </div>
        <div style="margin-left: 10px;">
          <el-button @click="updateConfigItem(conf)">更新</el-button>
        </div>
      </div>
    </div>
  </div>
</div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import systemManage from "@/api/system-manage";
import { InfoFilled } from "@element-plus/icons-vue"

const allConfigs = ref([])
const selectedCategory = ref({})

const getGroupedBizConfigs = () => {
  systemManage.getGroupedBizConfigs({}).then(res => {
    if (res.result == true) {
      let arr = res.data || []
      allConfigs.value = arr
      if (arr.length > 0) {
        selectCategory(arr[0])
      }
    } else {
      ElMessage.error(res.errorMsg)
    }
  })
}

const selectCategory = (category) => {
  selectedCategory.value = category
}

const updateConfigItem = (conf) => {
  systemManage.updateBizConfigs({
    id: conf.id,
    name: conf.name,
    category: conf.category,
    cfgKey: conf.cfgKey,
    cfgValue: conf.cfgValue,
    remark: conf.remark,
    sortNo: conf.sortNo,
    state: conf.state
  }).then(res => {
    if (res.result == true) {
      ElMessage.success('更新成功！')
    } else {
      ElMessage.error(res.errorMsg)
    }
  })
}

onMounted(() => {
  getGroupedBizConfigs()
})
</script>

<style scoped lang="stylus">
.business-config {
  width 100%;
  height 100%;
  background-color white;
  overflow hidden;
  padding 10px;

  .content {
    width 100%;
    height 100%;
    display flex;
    border 1px solid #ddd;

    .left-menus {
      width 180px;
      height 100%;
      overflow-y auto;
      border-right 1px solid #ddd;

      .menu-item {
        padding 16px 10px;
        font-size 14px;
        border-bottom 1px solid #ddd;
        cursor pointer;
        text-align center;
        &.active {
          color #165bd3;
        }
      }
    }

    .config-items {
      flex 1;
      height 100%;
      overflow-y auto;
      padding 0px 20px;

      .category-title {
        font-size 16px;
        font-weight bold;
        padding 16px 0;
      }

      .config-row {
        display flex;
        align-items center;
        font-size 14px;
        padding 10px 0;

        .label {
          width 240px;
          display flex;
          align-items center;
          justify-content flex-end;
          margin-right 10px;
        }
      }
    }
  }
}
</style>
