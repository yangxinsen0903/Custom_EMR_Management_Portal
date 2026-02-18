/**Created by liaoyingchao on 12/5/22.*/

<template>
  <div class="step-4">
    <div class="full-page-block-div">
      <div class="block-title" v-if="!isResource">配置清单</div>
      <div class="detail-block-div">
        <div class="sub-title">
          <div>基础配置</div>
          <el-button
            type="primary"
            text
            size="small"
            @click="changeStep(0)"
            v-if="!isResource"
            >编辑</el-button
          >
        </div>
        <div class="detail-items">
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">集群名称</div>
              <div class="value">{{ modelValue.clusterName }}</div>
            </div>
          </div>
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">集群标签</div>
              <div class="value">
                {{ getLabelsStr(modelValue.tagMap) }}
              </div>
              <el-button
                type="primary"
                text
                size="small"
                @click="editLabels"
                v-if="isResource && modelValue.state == '2' && permissionCheck.currentPermissionCheck(['Maintainer', 'Administrator'])"
                >编辑集群标签</el-button
              >
            </div>
          </div>
          <div class="flex-row">
            <div class="detail-item" style="align-items: center;">
              <div class="label">直接销毁白名单</div>
              <el-switch class="value"
                         style="height: 20px;"
                  v-model="modelValue.isWhiteAddr"
                  inline-prompt
                  active-text="on"
                  inactive-text="off"
                  :active-value="1"
                  :inactive-value="0"
                  :disabled="!(isResource && modelValue.state == '2' &&permissionCheck.currentPermissionCheck(['Maintainer', 'Administrator']))"
                  @change="updateDestroyStatusEvent"
              />
            </div>
          </div>
        </div>
      </div>
      <div class="detail-block-div">
        <div class="sub-title">
          <div>软件配置</div>
          <el-button
            type="primary"
            text
            size="small"
            @click="changeStep(1)"
            v-if="!isResource"
            >编辑</el-button
          >
        </div>
        <div class="detail-items">
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">产品版本</div>
              <div class="value">
                {{ modelValue.instanceGroupVersion.clusterReleaseVer }}
              </div>
            </div>
            <div class="detail-item">
              <div class="label">应用场景</div>
              <div class="value">
                {{
                  modelValue.scene == "" || modelValue.scene == "DEFAULT"
                    ? "默认场景"
                    : modelValue.scene
                }}
              </div>
            </div>
          </div>
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">部署组件</div>
              <div class="value">
                <span
                  v-for="(app, idx) in modelValue.instanceGroupVersion
                    .clusterApps"
                  >{{ idx > 0 ? "、" : ""
                  }}{{ app.appName + " " + app.appVersion }}</span
                >
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="detail-block-div">
        <div class="sub-title">
          <div>
            硬件配置 &nbsp; &nbsp;<span
              style="color: red"
              v-if="modelValue.warnMessage"
              ><b>[{{ modelValue.warnMessage }}]</b></span
            >
          </div>
          <el-button
            type="primary"
            text
            size="small"
            @click="changeStep(2)"
            v-if="!isResource"
            >编辑</el-button
          >
        </div>
        <div class="detail-items">
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">数据中心</div>
              <div class="value">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="modelValue.regionName || modelValue.region"
                >
                  <div class="one-line-value">
                    {{ modelValue.regionName || modelValue.region }}
                  </div>
                </el-tooltip>
              </div>
            </div>
            <div class="detail-item">
              <div class="label">可用区</div>
              <div class="value">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="modelValue.zoneName || modelValue.zone"
                >
                  <div class="one-line-value">
                    {{ modelValue.zoneName || modelValue.zone }}
                  </div>
                </el-tooltip>
              </div>
            </div>
          </div>
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">高可用</div>
              <div class="value">
                {{ modelValue.isHa == 1 ? "开启" : "关闭" }}
              </div>
            </div>
            <div class="detail-item">
              <div class="label">集群外网</div>
              <div class="value">{{ "关闭" }}</div>
            </div>
          </div>
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">集群网络</div>
              <div class="value">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="modelValue.vNetName || modelValue.vNet"
                >
                  <div class="one-line-value">
                    {{ modelValue.vNetName || modelValue.vNet }}
                  </div>
                </el-tooltip>
              </div>
            </div>
            <div class="detail-item">
              <div class="label">集群子网</div>
              <div class="value">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="modelValue.subNetName || modelValue.subNet"
                >
                  <div class="one-line-value">
                    {{ modelValue.subNetName || modelValue.subNet }}
                  </div>
                </el-tooltip>
              </div>
            </div>
          </div>
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">集群安全组</div>
              <div class="value">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="
                    modelValue.masterSecurityGroupName ||
                    modelValue.masterSecurityGroup
                  "
                >
                  <div class="one-line-value">
                    {{
                      modelValue.masterSecurityGroupName ||
                      modelValue.masterSecurityGroup
                    }}
                  </div>
                </el-tooltip>
              </div>
            </div>
            <div class="detail-item">
              <div class="label">集群子安全组</div>
              <div class="value">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="
                    modelValue.slaveSecurityGroupName ||
                    modelValue.slaveSecurityGroup
                  "
                >
                  <div class="one-line-value">
                    {{
                      modelValue.slaveSecurityGroupName ||
                      modelValue.slaveSecurityGroup
                    }}
                  </div>
                </el-tooltip>
              </div>
            </div>
          </div>
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">Ambari元数据</div>
              <div class="value">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="
                    modelValue.ambariDbCfgs.url +
                    ':' +
                    modelValue.ambariDbCfgs.port +
                    '/' +
                    modelValue.ambariDbCfgs.database
                  "
                >
                  <div class="one-line-value">
                    {{ modelValue.ambariDbCfgs.url }}:{{
                      modelValue.ambariDbCfgs.port
                    }}/{{ modelValue.ambariDbCfgs.database }}
                  </div>
                </el-tooltip>
              </div>
            </div>
            <div class="detail-item">
              <div class="label">Hive元数据</div>
              <div class="value" v-if="modelValue.hiveMetadataDbCfgs.url">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="
                    modelValue.hiveMetadataDbCfgs.url +
                    ':' +
                    modelValue.hiveMetadataDbCfgs.port +
                    '/' +
                    modelValue.hiveMetadataDbCfgs.database
                  "
                >
                  <div class="one-line-value">
                    {{ modelValue.hiveMetadataDbCfgs.url }}:{{
                      modelValue.hiveMetadataDbCfgs.port
                    }}/{{ modelValue.hiveMetadataDbCfgs.database }}
                  </div>
                </el-tooltip>
              </div>
            </div>
          </div>
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">登录方式</div>
              <div class="value">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="modelValue.keypairId"
                >
                  <div class="one-line-value">{{ modelValue.keypairId }}</div>
                </el-tooltip>
              </div>
            </div>
            <div class="detail-item">
              <div class="label">集群托管标识</div>
              <div class="value">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="modelValue.vmMIName || modelValue.vmMI"
                >
                  <div class="one-line-value">
                    {{ modelValue.vmMIName || modelValue.vmMI }}
                  </div>
                </el-tooltip>
              </div>
            </div>
          </div>
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">ganglia监控</div>
              <div class="value">
                {{ modelValue.enableGanglia == "1" ? "开启" : "关闭" }}
              </div>
            </div>
            <div class="detail-item"></div>
          </div>
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">日志桶</div>
              <div class="value">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="modelValue.logPath"
                >
                  <div class="one-line-value">{{ modelValue.logPath }}</div>
                </el-tooltip>
              </div>
            </div>
            <div class="detail-item">
              <div class="label">日志捅托管标识</div>
              <div class="value">
                <el-tooltip
                  popper-class="value-popper"
                  effect="light"
                  :content="modelValue.logMIName || modelValue.logMI"
                >
                  <div class="one-line-value">
                    {{ modelValue.logMIName || modelValue.logMI }}
                  </div>
                </el-tooltip>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="detail-block-div">
        <div class="sub-title">
          <div>集群节点</div>
          <el-button
            type="primary"
            :icon="RefreshRight"
            text
            size="small"
            @click="refreshEvent"
            v-if="isResource && modelValue.state == '2'"
            >刷新</el-button
          >
        </div>
        <div class="detail-items">
          <div class="flex-row">
            <el-table
              :data="modelValue.instanceGroupSkuCfgs"
              header-row-class-name="theader"
              border
              style="width: 100%"
            >
              <el-table-column prop="vmRole" label="实例类型" min-width="100">
                <template #default="scope">
                  {{ scope.row.vmRole }}实例配置
                </template>
              </el-table-column>
              <el-table-column
                prop="groupName"
                label="实例组名称"
                min-width="100"
              ></el-table-column>
              <el-table-column prop="skuName" label="型号" min-width="150">
                <template #default="scope">
                  <div v-if="scope.row.vmRole == 'Master' && !modelValue.isHa">
                    将部署在Ambari实例
                  </div>
                  <div v-else-if="scope.row.purchaseType == 2">
                    <span style="margin-right: 15px;">共{{scope.row.skuNames.length}}个机型</span>
                    <check-skus :skuNames="scope.row.skuNames" :region="modelValue.region"></check-skus>
                  </div>
                  <div v-else>
                    {{ scope.row.skuName }} {{ scope.row.vCPUs }}核{{
                      scope.row.memoryGB
                    }}G
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                prop="dataVolumeSize"
                label="存储"
                min-width="200"
              >
                <template #default="scope">
                  <div v-if="scope.row.vmRole == 'Master' && !modelValue.isHa">
                    将部署在Ambari实例
                  </div>
                  <div v-else>
                    <div>
                      系统盘：{{ diskTypeToName(scope.row.osVolumeType) }}
                      {{ scope.row.osVolumeSize }}G * 1
                    </div>
                    <div>
                      数据盘：{{ diskTypeToName(scope.row.dataVolumeType) }}
                      {{ scope.row.dataVolumeSize }}G *
                      {{ scope.row.dataVolumeCount }}
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                prop="cnt"
                label="实例数量"
                min-width="150"
                v-if="!isResource"
              >
                <template #default="scope">
                  <div v-if="scope.row.vmRole == 'Master' && !modelValue.isHa">
                    将部署在Ambari实例
                  </div>
                  <div v-else>实例数量*{{ scope.row.cnt }}</div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>
    <div class="full-page-buttons" v-if="!isResource">
      <el-button
        class="pre-step"
        type="warning"
        size="large"
        @click="cancelEvent"
        >取消创建</el-button
      >
      <el-button class="pre-step" size="large" @click="changeStep(2)"
        >上一步</el-button
      >
      <el-button
        type="primary"
        size="large"
        class="next-step"
        @click="createEvent('SPLIT')"
        >增量创建</el-button
      >
      <el-button
        type="primary"
        size="large"
        class="next-step"
        @click="createEvent('DIRECTLY')"
        >全量创建</el-button
      >
    </div>
  </div>
</template>

<script setup>
import { Finished, RefreshRight } from "@element-plus/icons-vue";
import { ref, toRefs, defineProps, defineEmits } from "vue";
import FormCheck from "../../../utils/formCheck";
import { useVmodel } from "../../../hooks/useVmodel";
import clusterApi from "../../../api/cluster";
import { ElMessage, ElMessageBox } from "element-plus";
import CheckSkus from "@/views/cluster/comps/check-skus.vue";

const emit = defineEmits(["createEvent", "changeStep", "editLabels"]);
const props = defineProps({
  modelValue: Object,
  isCopy: Boolean,
  isResource: Boolean,
});
const { isCopy, isResource } = toRefs(props);
const modelValue = useVmodel(props);

import { useRoute, useRouter } from "vue-router";
import permissionCheck from "@/utils/permission-check";
const router = useRouter();

function cancelEvent() {
  router.go(-1);
}

function changeStep(idx) {
  emit("changeStep", idx);
}

let timer = null;
function createEvent(creationMode) {
  if (!timer) {
    modelValue.value.creationMode = creationMode;

    emit("createEvent");
    timer = setTimeout(() => {
      timer = null;
    }, 500);
  }
}

// 系统盘类型
const osDiskTypeList = ref([]);

function getOsDiskTypeList() {
  clusterApi.getOsDiskTypeList({region: modelValue.value.region}).then(res => {
    if (res.result == true) {
      osDiskTypeList.value = res.data;
    } else {
      ElMessage.error(res.errorMsg);
      console.log(res);
    }
  });
}

getOsDiskTypeList();

function diskTypeToName(type) {
  if (type == "NVMe磁盘") {
    return "NVMe磁盘";
  }
  let item = osDiskTypeList.value.find((itm) => {
    return itm.dictValue == type;
  });

  if (item) {
    return item.dictName + "云盘";
  }

  return type + "云盘";
}

function getLabelsStr(tagMap) {
  let str = "";
  for (let key in tagMap) {
    if (str) {
      str += "、" + key + "：" + tagMap[key];
    } else {
      str += key + "：" + tagMap[key];
    }
  }
  return str;
}

function editLabels() {
  emit("editLabels");
}

function refreshEvent() {
  emit("refreshEvent");
}

function updateDestroyStatusEvent(){
  console.log( modelValue.value.isWhiteAddr)
  if (isResource.value){
    if( modelValue.value.isWhiteAddr==1){
      //开启
      ElMessageBox.confirm(`您确定要加入直接销毁白名单吗,集群销毁时将被直接销毁`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        console.log("1312")
        updateDestroyStatusApi()
      }).catch(() => {
        modelValue.value.isWhiteAddr = 0
      })
    }else{
      //关闭
      updateDestroyStatusApi()
    }
  }
}
function  updateDestroyStatusApi(){
  clusterApi.updateDestroyStatus({
    clusterId: modelValue.value.clusterId,
    isWhiteAddr: modelValue.value.isWhiteAddr
  }).then(res => {
    if (res.result == true) {

    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}
</script>

<style lang="stylus" scoped type="text/stylus">
.step-4 {
  .one-line-value {
    width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .edit-tags-btn {
    margin 0 15px;
    color #666;
    cursor pointer;
    font-size 14px;
    &:hover {
      color #315FCE;
    }
  }
}
</style>

<style lang="stylus" type="text/stylus">
.value-popper {
  max-width 60%;
}
</style>
