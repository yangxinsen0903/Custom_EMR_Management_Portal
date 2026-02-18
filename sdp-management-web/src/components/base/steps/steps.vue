/**Created by liaoyingchao on 12/5/22.*/

<template>
  <div class="steps">
    <div class="step-item" :class="{full: idx < stepOptions.length - 1}" v-for="(option, idx) in stepOptions" :key="idx">
      <div class="content" @click="stepEvent(idx)">
        <div class="icon editing" v-if="modelValue == idx">{{ idx + 1 }}</div>
        <div class="icon finished" v-else-if="modelValue > idx">
          <el-icon size="24" color="#535E85"><Finished /></el-icon>
        </div>
        <div class="icon" v-else>{{ idx + 1 }}</div>
        <div class="title" :class="{editing: modelValue == idx}">{{ option.name }}</div>
      </div>
      <div class="line"></div>
    </div>
  </div>
</template>

<script setup>
  import {
    Finished
  } from '@element-plus/icons-vue'
  import {ref, toRefs, defineProps, defineEmits} from "vue"
  import {useVmodel} from "../../../hooks/useVmodel";

  const emit = defineEmits();
  const props = defineProps({
    modelValue: Number,
    stepOptions: {
      type: Array,
      default: () => []
    }
  });
  const {stepOptions} = toRefs(props)
  const modelValue = useVmodel(props);

  function stepEvent(idx) {
    if (modelValue.value > idx) {
      modelValue.value = idx
    }
  }
</script>

<style lang="stylus" scoped type="text/stylus">
  .steps {
    display flex;
    align-items center;

    .step-item {
      display flex;
      align-items center;

      &.full {
        flex 1;
      }

      .content {
        display flex;
        align-items center;
        color #7f7f7f;
        font-size 16px;
        cursor pointer;

        .icon {
          width 40px;
          height 40px;
          overflow hidden;
          border-radius 20px;
          border 1px solid #7f7f7f;
          margin-right 10px;
          font-weight 600;
          display flex;
          align-items center;
          justify-content center;

          &.editing {
            color white;
            background-color #535E85;
            border-color #535E85;
          }

          &.finished {
            border 2px solid #535E85;
          }
        }

        .title {
          color #7f7f7f;
          &.editing {
            color #7f7f7f;
            font-weight 600;
          }
        }
      }
      .line {
        flex 1;
        height 1px;
        margin 0 10px;
        background-color #7f7f7f;
      }
    }
  }
</style>