/**Created by liaoyingchao on 12/9/22.*/

<template>
  <div class="detail detail-css">
    <div class="detail-block-div">
      <!--<div class="sub-title">任务参数</div>-->
      <div class="detail-items">
        <div class="flex-row">
          <div class="detail-item">
            <div class="label">集群名称</div>
            <div class="value">{{ detailData.clusterName }}</div>
          </div>
          <div class="detail-item">
            <div class="label">Core实例数</div>
            <div class="value">{{ detailData.coreNum }}</div>
          </div>
          <div class="detail-item">
            <div class="label" style="width: 150px;">Ambari/Master实例数</div>
            <div class="value">{{ parseInt(detailData.ambariNum || 0) + parseInt(detailData.masterNum || 0) }}</div>
          </div>
        </div>
        <div class="flex-row">
          <div class="detail-item">
            <div class="label">Task实例数</div>
            <div class="value">{{ detailData.taskNum }}</div>
          </div>
          <div class="detail-item">
            <div class="label">高可用</div>
            <div class="value">{{ detailData.isHa ? '是' : '否' }}</div>
          </div>
          <div class="detail-item"></div>
        </div>
        <div class="flex-row">
          <div class="detail-item">
            <div class="label">服务信息</div>
            <div class="value">{{ detailData.serviceInfos }}</div>
          </div>
        </div>
        <div class="flex-row">
          <el-table :data="detailData.activityInfos" header-row-class-name="theader" border style="width: 100%">
            <el-table-column prop="sortNo" label="序号" width="80">
              <template #default="scope">
                {{ scope.row.sortNo + 1 }}
              </template>
            </el-table-column>
            <el-table-column prop="activityName" label="步骤名称" min-width="100"></el-table-column>
            <el-table-column prop="state" label="状态" width="100"></el-table-column>
            <el-table-column prop="begTime" label="开始时间" :formatter="columnTimeFormat" min-width="120"></el-table-column>
            <el-table-column prop="endTime" label="结束时间" :formatter="columnTimeFormat" min-width="120"></el-table-column>
            <el-table-column prop="scriptPath" label="操作" width="140">
              <template #default="scope">
                <div v-if="scope.row.retry == 1 || scope.row.log">
                  <el-button size="small" @click="retryHandle(scope.row)" v-if="scope.row.retry == 1">重试</el-button>
                  <el-button size="small" @click="checkLog(scope.row)" v-if="scope.row.log">日志</el-button>
                </div>
                <div v-else>--</div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
    <el-dialog
            class="center-dialog"
            v-model="logVisible"
            title="日志"
            width="800"
            destroy-on-close
    >
      <div style="max-height: 60vh;overflow-y: auto;">
        {{ logText }}
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="logVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
  import { ref, reactive, defineEmits, defineProps, toRefs } from 'vue'
  import { columnTimeFormat } from "@/utils/format-time";
  import taskCenterApi from '@/api/task-center'
  // const emit = defineEmits(['onSearch', 'onReset'])
  import {ElMessage, ElMessageBox} from 'element-plus';
  const props = defineProps({
    detailData: {
      type: Object,
      default: () => [],
    },
  })

  let dialogVisible = ref(false)
  const { detailData } = toRefs(props)

  const retryHandle = (row) => {
    taskCenterApi.retry({activityLogId: row.activityLogId}).then(res => {
      if (res.result == true) {
        ElMessage.success('操作成功')
      } else {
        ElMessage.error(res.errorMsg)
      }
    })
  }

  let logVisible = ref(false)
  const logText = ref('')
  const checkLog = (row) => {
    logVisible.value = true
    logText.value = row.log
  }
</script>

<style lang="stylus" scoped type="text/stylus">
  .detail {
    max-height 70vh;
    overflow-y auto;
    margin-bottom -20px;
    /*.detail-items {
      .detail-item {
        .label {
          width 120px !important;
        }
      }
    }*/
  }
</style>
