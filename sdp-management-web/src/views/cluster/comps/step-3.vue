/**Created by liaoyingchao on 12/5/22.*/

<template>
  <div class="step-3" v-loading="stepLoading" element-loading-text="校验数据库有效性中....">
    <el-form size="large" ref="RefForm" :model="modelValue" :rules="formRules" :scroll-to-error="true"
             label-width="120px" @validate="formValidate">
      <div class="full-page-block-div">
        <div class="block-title">网络配置</div>
        <div class="flex-row">
          <el-form-item label="数据中心" prop="region">
            <el-select class="input-width" placeholder="请选择数据中心" v-model="modelValue.region"
                       @change="regionChange(true)">
              <el-option
                  v-for="option in dataCenter"
                  :key="option.region"
                  :label="option.regionName"
                  :value="option.region"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="可用区" prop="zone">
            <el-select class="input-width" placeholder="请选择可用区" v-model="modelValue.zone" @change="zoneChange">
              <el-option
                  v-for="option in azList"
                  :key="option.logicalZone"
                  :label="option.availabilityZone"
                  :value="option.logicalZone"/>
            </el-select>
          </el-form-item>
        </div>
        <div class="flex-row">
          <el-form-item label="选择网络" prop="vNet">
            <el-select class="input-width" placeholder="请选择网络" v-model="modelValue.vNet" @change="vNetChange">
              <el-option
                  v-for="option in netList"
                  :key="option.dictValue"
                  :label="option.dictName"
                  :value="option.dictValue"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="选择子网" prop="subNet">
            <el-select class="input-width" placeholder="请选择子网" v-model="modelValue.subNet" @change="subNetChange">
              <el-option
                  v-for="option in subNetList"
                  :key="option.subnetId"
                  :label="option.subnetName"
                  :value="option.subnetId"
              />
            </el-select>
          </el-form-item>
        </div>
        <div class="flex-row">
          <el-form-item label="主安全组" prop="masterSecurityGroup">
            <div>
              <el-select class="input-width" placeholder="请选择主安全组" v-model="modelValue.masterSecurityGroup"
                         @change="masterSecurityGroupChange">
                <el-option
                    v-for="option in masterSecurityGroupList"
                    :key="option.resourceId"
                    :label="option.securityGroupName"
                    :value="option.resourceId"
                />
              </el-select>
              <div class="from-item-tip input-width">master实例安全组</div>
            </div>
          </el-form-item>
          <el-form-item label="子安全组" prop="slaveSecurityGroup">
            <div>
              <el-select class="input-width" placeholder="请选择子安全组" v-model="modelValue.slaveSecurityGroup"
                         @change="slaveSecurityGroupChange">
                <el-option
                    v-for="option in subSecurityGroupList"
                    :key="option.resourceId"
                    :label="option.securityGroupName"
                    :value="option.resourceId"
                />
              </el-select>
              <div class="from-item-tip input-width">core\task实例安全组</div>
            </div>
          </el-form-item>
        </div>
      </div>
      <div class="full-page-block-div">
        <div class="block-title">实例配置</div>
        <div class="flex-row">
          <el-form-item label="登录方式" prop="keypairId">
            <div>
              <el-select class="input-width" placeholder="请选择登录方式" v-model="modelValue.keypairId">
                <el-option
                    v-for="option in keypairList"
                    :key="option.keyVaultResourceId"
                    :label="option.name"
                    :value="option.nameInKeyVault"
                />
              </el-select>
              <div class="from-item-tip">选择密钥对</div>
            </div>
          </el-form-item>
          <el-form-item label="集群实例托管标识" prop="vmMI" label-width="180px">
            <div>
              <el-select style="width: 340px" placeholder="请选择集群实例托管标识" v-model="modelValue.vmMI"
                         @change="vmMIChanged">
                <el-option
                    v-for="option in miList"
                    :key="option.resourceId"
                    :label="option.miName"
                    :value="option.resourceId"
                />
              </el-select>
              <div class="from-item-tip"></div>
            </div>
          </el-form-item>
        </div>
        <el-form-item label="高可用">
          <el-switch
              v-model="modelValue.isHa"
              inline-prompt
              active-text="开启"
              :active-value="1"
              :inactive-value="0"
              inactive-text="关闭"
              @change="isHaChange"
              :disabled="isCopy || modelValue.scene == 'HBase'"
          />
        </el-form-item>
        <el-form-item label="系统盘">
          <el-select class="input-width-2" placeholder="请选择磁盘类型" v-model="modelValue.osDiskType"
                     @change="osDiskChange">
            <el-option
                v-for="option in osDiskTypeList"
                :key="option.name"
                :label="option.name"
                :value="option.name"
            />
          </el-select>
          <el-input class="input-width-2" v-model="modelValue.diskSize" @change="osDiskChange" :disabled="true">
            <template #append>GB</template>
          </el-input>
        </el-form-item>
        <el-form-item label="实例设置">
          <el-table v-if="refresh" :data="modelValue.instanceGroupSkuCfgs" header-row-class-name="theader" border
                    style="width: 100%" :span-method="objectSpanMethod">
            <el-table-column prop="vmRole" label="实例类型" min-width="120">
              <template #default="scope">
                {{ scope.row.vmRole }}实例
              </template>
            </el-table-column>
            <el-table-column prop="vmRole" label="实例组名称" min-width="140">
              <template #default="scope">
                <div class="vm-group-name-div">
                  <div class="group-name-edit" v-if="scope.row.vmRole == 'Task' || scope.row.vmRole == 'task'">
                    <template v-if="scope.row.editGroupName">
                      <div class="prepend-div">task-</div>
                      <div class="name-code">
                        <el-input ref="Ref_TaskName_Input" style="margin-left: 8px;" v-model="scope.row.nameCode"
                                  size="small" @blur="groupNameChanged(scope.row)" maxlength="10"></el-input>
                      </div>
                    </template>
                    <div class="name-code" v-else>{{ scope.row.groupName }}</div>
                    <div class="edit-btn" @click="editGroupNameEvent(scope.row)" v-if="!scope.row.editGroupName">
                      <el-icon :size="16">
                        <Edit/>
                      </el-icon>
                    </div>
                  </div>
                  <div v-else>{{ scope.row.groupName }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="scriptName" label="实例规格" min-width="320">
              <template #default="scope">
                <div>
                  <div v-if="scope.row.vmRole == 'Master' && !modelValue.isHa">将部署在Ambari实例</div>
                  <div style="display: flex;align-items: center;" v-else>
                    <div style="flex: 1;">
                      <div v-if="scope.row.purchaseType == 2">
                        <span style="margin-right: 15px;">共{{scope.row.skuNames.length}}个机型</span>
                        <check-skus :skuNames="scope.row.skuNames" :region="modelValue.region"></check-skus>
                      </div>
                      <div v-else>{{ scope.row.skuName }} {{ scope.row.vCPUs }}核{{ scope.row.memoryGB }}G</div>
                      <div>系统盘：{{ diskTypeToName(scope.row.osVolumeType) }}{{ scope.row.osVolumeSize }}G*1</div>
                      <div>数据盘：{{ diskTypeToName(scope.row.dataVolumeType) }}{{ scope.row.dataVolumeSize }}G*{{scope.row.dataVolumeCount}}</div>
                    </div>
                    <div>
                      <el-button type="primary" text size="small" @click="editDisk(scope.row)"
                                 :disabled="modelValue.isHa != 1 && scope.row.vmRole == 'Master'">编辑
                      </el-button>
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="scriptPath" label="实例数量" min-width="180">
              <template #default="scope">
                <div>
                  <div v-if="scope.row.vmRole.toLowerCase() == 'master' && !modelValue.isHa">将部署在Ambari实例</div>
                  <el-input-number style="width: 150px;" v-model="scope.row.cnt" :min="getNodeMinCount(scope.row)"
                                   :max="getNodeMaxCount(scope.row)"
                                   :disabled="scope.row.vmRole.toLowerCase() == 'master' || scope.row.vmRole.toLowerCase() == 'ambari'"
                                   :step-strictly="true" v-else/>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="scaleState" label="弹性伸缩" min-width="120">
              <template #default="scope">
                <div style="display: flex;align-items: center;">
                  <div style="flex: 1;">
                    {{ scaleToStr(scope.row) }}
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="付费类型" width="180">
              <template #default="scope">
                <div>{{ scope.row.purchaseType == 2 ? '竞价' : '标准' }}</div>
                <div class="pay-type-div">
                  <div v-if="scope.row.purchaseType == '2'">
                    <div>
                      <el-select v-model="scope.row.priceStrategy" placeholder="请选择出价类型" clearable size="default">
                        <el-option label="按标准价百分比" :value="1"/>
                        <el-option label="固定价" :value="2"/>
                      </el-select>
                    </div>
                    <div style="margin-top: 8px;">
                      <el-input style="width: 100%;" placeholder="请输入百分比" size="default" v-model="scope.row.maxPrice" v-if="scope.row.priceStrategy == '1'">
                        <template #append>%</template>
                      </el-input>
                      <el-input style="width: 100%;" placeholder="请输入出价" size="default" v-model="scope.row.maxPrice" v-if="scope.row.priceStrategy == '2'"></el-input>
                    </div>
                    <div v-if="scope.row.skuName">
                      <span style="margin-right: 10px;font-size: 12px;line-height: 20px;white-space: nowrap;" v-if="scope.row.onDemandUnitPricePerHourUSD">标准价：{{scope.row.onDemandUnitPricePerHourUSD}} USD/h</span>
                      <span style="margin-right: 10px;font-size: 12px;line-height: 20px;white-space: nowrap;" v-if="scope.row.showPrice">市场价：{{scope.row.showPrice}} USD/h</span>
                      <el-tooltip effect="light" placement="bottom">
                        <span style="font-size: 12px;color: #315FCE;cursor: pointer;line-height: 20px;white-space: nowrap;">查看历史价格</span>
                        <template #content>
                          <div style="max-width: 430px;">
                            <PriceHistoryChart :region="modelValue.region" :skuName="scope.row.skuName"></PriceHistoryChart>
                          </div>
                        </template>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="scope">
                <div class="vmgroup-opts"
                     v-if="scope.row.vmRole.toLowerCase() == 'task' || scope.row.vmRole.toLowerCase() == 'core'">
                  <el-button type="primary" text size="small" @click="editTaskCfg(scope.row)">配置参数</el-button>
                  <el-button type="danger" text size="small" @click="deleteTask(scope.$index, scope.row)"
                             v-if="scope.row.vmRole.toLowerCase() == 'task'">删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <div style="margin-top: 8px;" class="add-btn" @click="addTaskGroup">增加Task实例组</div>
        </el-form-item>
      </div>
      <div class="full-page-block-div">
        <div class="block-title">Ambari元数据</div>
        <div class="flex-row">
          <el-form-item label-width="140px" label="MySQL数据库内置">
            <el-switch
                v-model="modelValue.isEmbedAmbariDb"
                inline-prompt
                active-text="是"
                :active-value="1"
                :inactive-value="0"
                inactive-text="否"
                @change="isEmbedAmbariDbChange"
            />
          </el-form-item>
        </div>
        <div class="flex-row">
          <el-form-item label="主机" label-width="80px" prop="ambariDbCfgs.url" style="width: 50%;">
            <el-input v-model="modelValue.ambariDbCfgs.url" placeholder="请输入主机" show-word-limit clearable
                      :disabled="!!modelValue.isEmbedAmbariDb"/>
          </el-form-item>
          <el-form-item label="端口" label-width="80px" prop="ambariDbCfgs.port" style="width: 20%;">
            <el-input v-model="modelValue.ambariDbCfgs.port" placeholder="请输入数据库端口" show-word-limit clearable
                      :disabled="!!modelValue.isEmbedAmbariDb"/>
          </el-form-item>
          <el-form-item label="库名" label-width="80px" prop="ambariDbCfgs.database" style="width: 30%;">
            <el-input v-model="modelValue.ambariDbCfgs.database" placeholder="请输入数据库库名" show-word-limit
                      clearable :disabled="!canEditDbname"/>
          </el-form-item>
        </div>
        <div class="flex-row">
          <el-form-item label="用户名" label-width="80px" prop="ambariDbCfgs.account" style="width: 50%;">
            <el-input v-model="modelValue.ambariDbCfgs.account" placeholder="请输入用户名" show-word-limit
                      maxlength="50" clearable :disabled="!!modelValue.isEmbedAmbariDb"/>
          </el-form-item>
          <el-form-item label="密码" label-width="80px" prop="ambariDbCfgs.password" style="width: 50%;"
                        v-if="!modelValue.isEmbedAmbariDb">
            <el-input v-model="modelValue.ambariDbCfgs.password" placeholder="请输入密码" type="password" show-password
                      show-word-limit maxlength="50" clearable/>
          </el-form-item>
        </div>
      </div>
      <div class="full-page-block-div" v-if="hasHive">
        <div class="block-title">Hive元数据</div>
        <div class="flex-row">
          <el-form-item label="主机" label-width="80px" prop="hiveMetadataDbCfgs.url" style="width: 50%;">
            <el-input v-model="modelValue.hiveMetadataDbCfgs.url" placeholder="请输入数据库连接" show-word-limit
                      clearable/>
          </el-form-item>
          <el-form-item label="端口" label-width="80px" prop="hiveMetadataDbCfgs.port" style="width: 20%;">
            <el-input v-model="modelValue.hiveMetadataDbCfgs.port" placeholder="请输入数据库端口" show-word-limit
                      clearable/>
          </el-form-item>
          <el-form-item label="库名" label-width="80px" prop="hiveMetadataDbCfgs.database" style="width: 30%;">
            <el-input v-model="modelValue.hiveMetadataDbCfgs.database" placeholder="请输入数据库库名" show-word-limit
                      clearable/>
          </el-form-item>
        </div>
        <div class="flex-row">
          <el-form-item label="用户名" label-width="80px" prop="hiveMetadataDbCfgs.account" style="width: 50%;">
            <el-input v-model="modelValue.hiveMetadataDbCfgs.account" placeholder="请输入用户名" show-word-limit
                      maxlength="50" clearable/>
          </el-form-item>
          <el-form-item label="密码" label-width="80px" prop="hiveMetadataDbCfgs.password" style="width: 50%;">
            <el-input v-model="modelValue.hiveMetadataDbCfgs.password" placeholder="请输入密码" type="password"
                      show-password show-word-limit maxlength="50" clearable/>
          </el-form-item>
        </div>
      </div>
      <div class="full-page-block-div">
        <div class="block-title">系统检测与日志存储</div>
        <el-form-item label="启用ganglia检测" label-width="150px" prop="enableGanglia">
          <el-switch
              v-model="modelValue.enableGanglia"
              inline-prompt
              active-text="是"
              :active-value="1"
              :inactive-value="0"
              inactive-text="否"
              @change="gangliaChanged"
          />
        </el-form-item>
        <el-form-item label="日志桶" label-width="150px" prop="logPath">
          <el-select style="width: 100%" placeholder="请选择日志捅" v-model="modelValue.logPath">
            <el-option
                v-for="option in logOptions"
                :key="option.blobContainerUrl"
                :label="option.logName + '(' + option.blobContainerUrl + ')'"
                :value="option.blobContainerUrl"
            />
          </el-select>
        </el-form-item>
        <div class="flex-row">
          <el-form-item style="width: 50%;" label="日志捅托管标识" label-width="150px" prop="logMI">
            <el-select style="width: 100%" placeholder="请选择日志捅托管标识" v-model="modelValue.logMI"
                       @change="logMIChanged">
              <el-option
                  v-for="option in miList"
                  :key="option.resourceId"
                  :label="option.miName"
                  :value="option.resourceId"
              />
            </el-select>
          </el-form-item>
          <!--<el-form-item style="flex: 1;" label="本机日志保留时长" label-width="150px">-->
          <!--<el-input style="width: 100%" placeholder="请输入本机日志保留时长" maxlength="5" clearable>-->
          <!--<template #append>天</template>-->
          <!--</el-input>-->
          <!--</el-form-item>-->
        </div>
      </div>
    </el-form>
    <div class="full-page-buttons">
      <el-button class="pre-step" type="warning" size="large" @click="cancelEvent">取消创建</el-button>
      <el-button class="pre-step" size="large" @click="preEvent">上一步</el-button>
      <el-button type="primary" size="large" class="next-step" @click="nextEvent" :loading="stepLoading">下一步
      </el-button>
    </div>
    <el-dialog
        class="center-dialog"
        v-model="dialogVisible"
        title="实例规格"
        width="80%"
        destroy-on-close
    >
      <div class="dialog-content">
        <div class="dialog-tip">
          Master实例建议选择内存较大的实例规格，推荐内容大小至少8G。磁盘建议选择云盘可以让集群获得更高的稳定性。
        </div>
        <el-form ref="Ref_editForm" :model="editForm" :rules="scriptFormRules" label-width="auto" style="width: 100%;">
          <el-form-item label="付费类型" prop="purchaseType" v-if="editForm.editItem.vmRole.toLowerCase() == 'task'">
            <el-radio-group v-model="editForm.purchaseType">
              <el-radio :label="1">标准</el-radio>
              <el-radio :label="2">竞价</el-radio>
            </el-radio-group>
          </el-form-item>
<!--          <el-form-item label="按需分配策略" prop="regularAllocationStrategy" v-if="editForm.editItem.vmRole.toLowerCase() == 'task' && editForm.purchaseType == 1">-->
<!--            <el-radio-group v-model="editForm.regularAllocationStrategy">-->
<!--              <el-radio :label="'LowestPrice'">按最低价</el-radio>-->
<!--              <el-radio :label="'Prioritized'">按照指定的优先级</el-radio>-->
<!--            </el-radio-group>-->
<!--          </el-form-item>-->
          <template v-if="editForm.purchaseType == 2">
            <el-form-item label="竞价分配策略" prop="spotAllocationStrategy">
              <el-radio-group v-model="editForm.spotAllocationStrategy">
                <el-radio :label="'LowestPrice'">按最低价</el-radio>
                <el-radio :label="'CapacityOptimized'">容量</el-radio>
                <el-radio :label="'PriceCapacityOptimized'">容量和价格</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="规格选择" prop="skus">
              <div>
                <TableSelect :options="vmskuList" :tableColumn="vmskuColumns" height="210px"
                             v-model="editForm.skus" :mainKey="'name'"
                             :multiple="true" >
                  <el-table-column align="center" label="费用" min-width="200">
                    <template #default="scope">
                      <div>
                        <div>标准价：{{ scope.row.ondemandUnitPrice || '' }} USD/h</div>
                        <div>市场价：{{ scope.row.spotUnitPrice || '' }} USD/h</div>
                        <el-tooltip effect="light" placement="bottom">
                      <span
                          style="font-size: 12px;color: #315FCE;cursor: pointer;line-height: 20px;white-space: nowrap;">查看历史价格</span>
                          <template #content>
                            <div style="max-width: 430px;">
                              <PriceHistoryChart :region="modelValue.region" :skuName="scope.row.name"></PriceHistoryChart>
                            </div>
                          </template>
                        </el-tooltip>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column align="center" label="逐出率" min-width="120">
                    <template #default="scope">
                      <div>
                        <div style="display: flex;align-items: center;justify-content: center;">
                          <div>{{ scope.row.evictionRateUpper }} %</div>
                          <div style="margin-left: 10px;display: flex;" v-if="scope.row.isRecommend"><el-icon><Star /></el-icon></div>
                        </div>
                        <el-tooltip effect="light" placement="bottom">
                      <span
                          style="font-size: 12px;color: #315FCE;cursor: pointer;line-height: 20px;white-space: nowrap;">查看历史逐出率</span>
                          <template #content>
                            <div style="max-width: 430px;">
                              <EvictionHistoryChart :region="modelValue.region"
                                                    :skuName="scope.row.name"></EvictionHistoryChart>
                            </div>
                          </template>
                        </el-tooltip>
                      </div>
                    </template>
                  </el-table-column>
                </TableSelect>
                <div v-show="editForm.skus.length">已选：{{ editForm.skus.map(itm => itm.name) }}</div>
              </div>
            </el-form-item>
          </template>
          <el-form-item label="规格选择" prop="vmskuName" v-else>
            <TableSelect :options="vmskuList" :tableColumn="vmskuColumns" height="210px"
                         v-model="editForm.vmskuName" :mainKey="'name'"
                         @change="vmskuChanged">
              <el-table-column align="center" label="费用" min-width="200">
                <template #default="scope">
                  <div>
                    <div>标准价：{{ scope.row.ondemandUnitPrice || '' }} USD/h</div>
                    <div>市场价：{{ scope.row.spotUnitPrice || '' }} USD/h</div>
                    <el-tooltip effect="light" placement="bottom">
                      <span
                          style="font-size: 12px;color: #315FCE;cursor: pointer;line-height: 20px;white-space: nowrap;">查看历史价格</span>
                      <template #content>
                        <div style="max-width: 430px;">
                          <PriceHistoryChart :region="modelValue.region" :skuName="scope.row.name"></PriceHistoryChart>
                        </div>
                      </template>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column align="center" label="逐出率" min-width="120">
                <template #default="scope">
                  <div>
                    <div style="display: flex;align-items: center;justify-content: center;">
                      <div>{{ scope.row.evictionRateUpper }} %</div>
                      <div style="margin-left: 10px;display: flex;" v-if="scope.row.isRecommend"><el-icon><Star /></el-icon></div>
                    </div>
                    <el-tooltip effect="light" placement="bottom">
                      <span
                          style="font-size: 12px;color: #315FCE;cursor: pointer;line-height: 20px;white-space: nowrap;">查看历史逐出率</span>
                      <template #content>
                        <div style="max-width: 430px;">
                          <EvictionHistoryChart :region="modelValue.region"
                                                :skuName="scope.row.name"></EvictionHistoryChart>
                        </div>
                      </template>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
            </TableSelect>
          </el-form-item>
          <el-form-item label="数据盘" prop="disk">
            <div style="display: flex;width: 100%;">
              <el-select style="flex: 2;" placeholder="请选择磁盘类型" v-model="editForm.dataVolumeType"
                         :disabled="editForm.hasNVMeDisk">
                <el-option
                    v-for="option in osDiskTypeList"
                    :key="option.name"
                    :label="option.name"
                    :value="option.name"
                />
              </el-select>
              <el-input style="flex: 1;margin-left: 15px;" v-model="editForm.dataVolumeSize"
                        :disabled="editForm.hasNVMeDisk">
                <template #append>GB</template>
              </el-input>
              <div class="dv-count"
                   v-if="(editForm.editItem.vmRole.toLowerCase() == 'core' || editForm.editItem.vmRole.toLowerCase() == 'task') || editForm.hasNVMeDisk">
                <el-input-number style="width: 100px;margin-left: 15px;" v-model="editForm.dataVolumeCount" :min="1"
                                 :max="editForm.maxDataDiskCount" controls-position="right"
                                 :disabled="editForm.hasNVMeDisk"/>
                <div class="unit-div">块</div>
              </div>
            </div>
          </el-form-item>
          <el-form-item label="是否启用物理机反亲和：" prop="provisionType"
                        v-if="editForm.editItem.vmRole.toLowerCase() != 'task' && !(modelValue.isHa == 0 && editForm.editItem.vmRole.toLowerCase() == 'ambari')">
            <el-select placeholder="请选择" v-model="editForm.provisionType">
              <el-option
                  v-for="option in provisionTypeOption"
                  :key="option.code"
                  :label="option.name"
                  :value="option.code"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEvent">保存</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog
        class="center-dialog"
        v-model="cfgDialogVisible"
        title="参数配置"
        width="1000"
        destroy-on-close
    >
      <div class="dialog-content" style="height: 60vh;">
        <div class="full-page-block-div">
          <ClusterArguments v-model="editTaskItem.groupCfgs"></ClusterArguments>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cfgDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="cfgSaveEvent">保存</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog
        class="center-dialog"
        v-model="ruleConfDialogVisible"
        title="配置弹性伸缩"
        width="1000"
        destroy-on-close
    >
      <div class="dialog-content">
        <ElasticScaling :editGroupData="editGroupData" :newGroupConf="newGroupConf"></ElasticScaling>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {
  Check,
  Star,
  Edit
} from '@element-plus/icons-vue'
import {ref, reactive, toRefs, defineProps, defineEmits, nextTick, computed, onMounted} from "vue"
import FormCheck from "../../../utils/formCheck";
import {useVmodel} from "../../../hooks/useVmodel";
import clusterApi from "../../../api/cluster";
import TableSelect from "@/components/base/table-select/index.vue"
import {ElMessage, ElMessageBox} from 'element-plus';
import ClusterArguments from "./arguments"
import PriceHistoryChart from "@/views/cluster/comps/price-history-chart.vue";
import ElasticScaling from "@/views/cluster/comps/elastic-scaling.vue";
import EvictionHistoryChart from "@/views/cluster/comps/eviction-history-chart.vue";
import CheckSkus from "@/views/cluster/comps/check-skus.vue";

const emit = defineEmits();
const props = defineProps({
  modelValue: Object,
  isCopy: Boolean
});
const {isCopy} = toRefs(props)
const modelValue = useVmodel(props);

const stepLoading = ref(false)

const RefForm = ref(null)

const isNextClick = ref(false)

function checkConnect(d, type, callback) {
  if (d.url && d.account && d.password && d.port && d.database) {
    clusterApi.checkConnect({
      url: d.url,
      account: d.account,
      password: d.password,
      port: d.port,
      database: d.database,
      type: type
    }).then(res => {
      if (res.result == true) {
        callback(true);
      } else {
        // ElMessage.error(res.errorMsg)
        callback(false, res.errorMsg);
        console.log(res)
      }
    }).catch(err => {
      callback(false, '检查失败！');
      console.log(err)
    })
  } else {
    callback(true);
  }
}

let timer1 = null, result1 = false, err1 = null, timer2 = null, result2 = false, err2 = null

function resultFunc(result, callback, err) {
  if (result == true) {
    callback()
  } else {
    callback(err)
  }
}

function ambariDbCfgsCheck(type) {
  return {
    validator: (rule, value, callback) => {
      if (type == 0) {
        if (timer1) {
          resultFunc(result1, callback, err1)
          return;
        }
        checkConnect(modelValue.value.ambariDbCfgs, type, (result, err) => {
          result1 = result
          err1 = err
          resultFunc(result1, callback, err1)
          timer1 = setTimeout(() => {
            timer1 = null
          }, 200)
        })
      } else {
        if (timer2) {
          resultFunc(result2, callback, err2)
          return;
        }
        checkConnect(modelValue.value.hiveMetadataDbCfgs, type, (result, err) => {
          result2 = result
          err2 = err
          resultFunc(result2, callback, err2)
          timer2 = setTimeout(() => {
            timer2 = null
          }, 200)
        })
      }
    },
    trigger: 'blur'
  }
}

function formValidate(prop, isValid) {
  if (prop.indexOf('ambariDbCfgs.') == 0 && isValid) {
    RefForm.value.clearValidate(["ambariDbCfgs.url", "ambariDbCfgs.account", "ambariDbCfgs.password", "ambariDbCfgs.port", "ambariDbCfgs.database"])
  } else if (prop.indexOf('hiveMetadataDbCfgs.') == 0 && isValid) {
    RefForm.value.clearValidate(["hiveMetadataDbCfgs.url", "hiveMetadataDbCfgs.account", "hiveMetadataDbCfgs.password", "hiveMetadataDbCfgs.port", "hiveMetadataDbCfgs.database"])
  }
}

const formRules = {
  vNet: FormCheck.required("请选择网络", "change"),
  zone: FormCheck.required("请选择可用区", "change"),
  region: FormCheck.required("请选择数据中心", "change"),
  subNet: FormCheck.required("请选择子网", "change"),
  masterSecurityGroup: FormCheck.required("请选择主安全组", 'change'),
  slaveSecurityGroup: FormCheck.required("请选择子安全组", 'change'),
  "ambariDbCfgs.url": [FormCheck.required("请输入数据库主机"), ambariDbCfgsCheck(0)],
  "ambariDbCfgs.account": [FormCheck.required("请输入数据库用户名"), ambariDbCfgsCheck(0)],
  "ambariDbCfgs.password": [FormCheck.required("请输入数据库密码"), ambariDbCfgsCheck(0)],
  "ambariDbCfgs.port": [FormCheck.required("请输入数据库端口"), ambariDbCfgsCheck(0)],
  "ambariDbCfgs.database": [FormCheck.required("请输入数据库库名"), ambariDbCfgsCheck(0)],
  "hiveMetadataDbCfgs.url": [FormCheck.required("请输入数据库主机"), ambariDbCfgsCheck(1)],
  "hiveMetadataDbCfgs.account": [FormCheck.required("请输入数据库用户名"), ambariDbCfgsCheck(1)],
  "hiveMetadataDbCfgs.password": [FormCheck.required("请输入数据库密码"), ambariDbCfgsCheck(1)],
  "hiveMetadataDbCfgs.port": [FormCheck.required("请输入数据库端口"), ambariDbCfgsCheck(1)],
  "hiveMetadataDbCfgs.database": [FormCheck.required("请输入数据库库名"), ambariDbCfgsCheck(1)],
  keypairId: FormCheck.required("请选择登录方式", 'change'),
  vmMI: FormCheck.required("请选择集群实例托管标识", 'change'),
  logPath: FormCheck.required("请选择日志捅", 'change'),
  logMI: FormCheck.required("请选择日志捅托管标识", 'change'),
}

const hasHive = computed(() => {
  let clusterApps = modelValue.value.instanceGroupVersion.clusterApps

  let idx = clusterApps.findIndex(app => {
    return app.appName.toLowerCase() == 'hive'
  })

  return idx > -1
})

function nextEvent() {
  console.log(modelValue.value)
  isNextClick.value = true
  stepLoading.value = true
  RefForm.value.validate((valid, fields) => {
    stepLoading.value = false
    isNextClick.value = false

    if (modelValue.value.enableGanglia == '1') {
      let instanceGroupSkuCfgs = modelValue.value.instanceGroupSkuCfgs || []
      let itemMaster = instanceGroupSkuCfgs.find(item => {
        return item.vmRole.toLowerCase() == "ambari"
      })

      if (itemMaster.vCPUs < 16 || itemMaster.memoryGB < 64) {
        ElMessage.error("启用ganglia检测，限制 Master 实例组规格最低为16c 64G！")
        return;
      }
      if (itemMaster.dataVolumeSize < 2000) {
        ElMessage.error("启用ganglia检测，限制 Master 实例组数据盘最小值为2000G！")
        return;
      }
    }

    if (valid) {
      emit('changeStep', 3)
    } else {
      ElMessage.error("部分填写数据出错，请检查填写数据！")
      console.log('error submit!', fields)
    }
  })
}

import {useRoute, useRouter} from 'vue-router'

const router = useRouter()

function cancelEvent() {
  router.go(-1)
}

function preEvent() {
  emit('changeStep', 1)
}

// 数据中心
const dataCenter = ref([])

function getDataCenter() {
  clusterApi.getCurrentUserRegions().then(res => {
    if (res.result == true) {
      dataCenter.value = res.data || []

      if (dataCenter.value.length) {
        if (!modelValue.value.region) {
          modelValue.value.region = dataCenter.value[0].region
        }
        regionChange(false)
      }
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

function regionChange(reInitData = false) {
  let item = dataCenter.value.find(itm => {
    return itm.region == modelValue.value.region
  }) || {}

  modelValue.value.regionName = item.regionName

  if (reInitData) {
    modelValue.value.zone = ''
    modelValue.value.zoneName = ''
    modelValue.value.vNet = ''
    modelValue.value.vNetName = ''
    modelValue.value.subNet = ''
    modelValue.value.subNetName = ''
    modelValue.value.masterSecurityGroup = ''
    modelValue.value.masterSecurityGroupName = ''
    modelValue.value.slaveSecurityGroup = ''
    modelValue.value.slaveSecurityGroupName = ''
    modelValue.value.keypairId = ''
    modelValue.value.vmMI = ''
    modelValue.value.vmMIName = ''
    modelValue.value.vmMITenantId = ''
    modelValue.value.vmMIClientId = ''
    modelValue.value.osDiskType = ''
    modelValue.value.logPath = ''
    modelValue.value.logMI = ''
    modelValue.value.logMIName = ''
    modelValue.value.logMITenantId = ''
    modelValue.value.logMIClientId = ''
  }

  reloadOptions()
}

// 可用区列表
const azList = ref([])

function getazlist() {
  azList.value = []
  clusterApi.getazlist({region: modelValue.value.region}).then(res => {
    if (res.result == true) {
      azList.value = res.data

      if (azList.value.length) {
        if (!modelValue.value.zone) {
          modelValue.value.zone = azList.value[0].logicalZone
          zoneChange()
        }
      }
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

function zoneChange() {
  let item = azList.value.find(itm => {
    return itm.logicalZone == modelValue.value.zone
  }) || {}

  modelValue.value.zoneName = item.availabilityZone
}

function initDefaultCfgs() {
  modelValue.value.instanceGroupSkuCfgs.forEach(item => {
    item.priceStrategy = item.priceStrategy ? item.priceStrategy : ''
    item.maxPrice = item.maxPrice ? item.maxPrice : ''
    item.purchasePriority = item.purchasePriority ? item.purchasePriority : '1'
    item.purchaseType = item.purchaseType ? item.purchaseType : 1
    item.groupName = item.groupName || item.vmRole
    if (item.vmRole.toLowerCase() == 'task') {
      if (!item.confGroupElasticScalingData) {
        item.confGroupElasticScalingData = {
          groupName: item.groupName,
          vmRole: item.vmRole,
          clusterId: '',
          maxCount: 100,
          minCount: 0,
          scalingRules: [],
        }
      }
    } else {
      item.provisionType = item.provisionType || 'VM_Standalone'
    }
  })

  if (modelValue.value.enableGanglia === null || modelValue.value.enableGanglia === undefined) {
    modelValue.value.enableGanglia = 0

    gangliaChanged()
  }

  modelValue.value.isEmbedAmbariDb = modelValue.value.isEmbedAmbariDb ? modelValue.value.isEmbedAmbariDb : 0
}

// 网络列表
const netList = ref([])

function getNetworkList() {
  netList.value = []
  clusterApi.getNetworkList({region: modelValue.value.region}).then(res => {
    if (res.result == true) {
      netList.value = res.data

      if (netList.value.length) {
        if (!modelValue.value.vNet) {
          modelValue.value.vNet = netList.value[0].dictValue
          vNetChange()
        }
      }
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

function vNetChange() {
  let item = netList.value.find(itm => {
    return itm.dictValue == modelValue.value.vNet
  }) || {}

  modelValue.value.vNetName = item.dictName
}

// 子网络列表
const subNetList = ref([])

function getSubnetList() {
  subNetList.value = []
  clusterApi.getSubnetList({region: modelValue.value.region}).then(res => {
    if (res.result == true) {
      subNetList.value = res.data

      if (subNetList.value.length) {
        if (!modelValue.value.subNet) {
          modelValue.value.subNet = subNetList.value[0].subnetId
        }
      }

      subNetChange()
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

function subNetChange() {
  let item = subNetList.value.find(itm => {
    return itm.subnetId == modelValue.value.subNet
  }) || {}

  modelValue.value.subNetName = item.subnetName

  try {
    let arr = modelValue.value.subNet.split('/')
    modelValue.value.subNetStart = '/' + arr[1] + '/' + arr[2] + '/'

    console.log('subNetChange')
    rechangeMasterSecurityGroup()
    rechangeSlaveSecurityGroup()
  } catch (e) {
    console.log(e)
  }
}

// 主安全组
const all_masterSecurityGroupList = ref([])
const masterSecurityGroupList = ref([])

function getPrimarySecurityGroupList() {
  all_masterSecurityGroupList.value = []
  clusterApi.getPrimarySecurityGroupList({region: modelValue.value.region}).then(res => {
    if (res.result == true) {
      all_masterSecurityGroupList.value = res.data || []

      console.log('getPrimarySecurityGroupList')
      rechangeMasterSecurityGroup()
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

function rechangeMasterSecurityGroup() {
  masterSecurityGroupList.value = []
  if (all_masterSecurityGroupList.value.length && modelValue.value.subNetStart) {
    masterSecurityGroupList.value = all_masterSecurityGroupList.value.filter(item => {
      return item.resourceId.indexOf(modelValue.value.subNetStart) == 0
    }) || []

    if (masterSecurityGroupList.value.length) {
      if (!modelValue.value.masterSecurityGroup) {
        modelValue.value.masterSecurityGroup = masterSecurityGroupList.value[0].resourceId
      }
    }

    masterSecurityGroupChange()
  }
}

function masterSecurityGroupChange() {
  let item = masterSecurityGroupList.value.find(itm => {
    return itm.resourceId == modelValue.value.masterSecurityGroup
  }) || {}

  if (!item.resourceId && masterSecurityGroupList.value.length) {
    item = masterSecurityGroupList.value[0]
  }

  modelValue.value.masterSecurityGroup = item.resourceId || ''
  modelValue.value.masterSecurityGroupName = item.securityGroupName
}

// 子安全组
const all_subSecurityGroupList = ref([])
const subSecurityGroupList = ref([])

function getSubSecurityGroupList() {
  all_subSecurityGroupList.value = []
  clusterApi.getSubSecurityGroupList({region: modelValue.value.region}).then(res => {
    if (res.result == true) {
      all_subSecurityGroupList.value = res.data || []

      rechangeSlaveSecurityGroup()
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

function rechangeSlaveSecurityGroup() {

  subSecurityGroupList.value = []
  if (all_subSecurityGroupList.value.length && modelValue.value.subNetStart) {
    subSecurityGroupList.value = all_subSecurityGroupList.value.filter(item => {
      return item.resourceId.indexOf(modelValue.value.subNetStart) == 0
    }) || []

    if (subSecurityGroupList.value.length) {
      if (!modelValue.value.slaveSecurityGroup) {
        modelValue.value.slaveSecurityGroup = subSecurityGroupList.value[0].resourceId
      }
    }

    slaveSecurityGroupChange()
  }
}

function slaveSecurityGroupChange() {
  let item = subSecurityGroupList.value.find(itm => {
    return itm.resourceId == modelValue.value.slaveSecurityGroup
  }) || {}

  if (!item.resourceId && subSecurityGroupList.value.length) {
    item = subSecurityGroupList.value[0]
  }

  modelValue.value.slaveSecurityGroup = item.resourceId || ''
  modelValue.value.slaveSecurityGroupName = item.securityGroupName
}

// 密钥对
const keypairList = ref([])

function getKeypairList() {
  keypairList.value = []
  clusterApi.getKeypairList({region: modelValue.value.region}).then(res => {
    if (res.result == true) {
      keypairList.value = res.data
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

// mi list
const miList = ref([])

function getMIList() {
  miList.value = []
  clusterApi.getMIList({region: modelValue.value.region}).then(res => {
    if (res.result == true) {
      miList.value = res.data

      vmMIChanged();
      logMIChanged();
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

function vmMIChanged() {
  let item = miList.value.find(itm => {
    return itm.resourceId == modelValue.value.vmMI
  }) || {}

  modelValue.value.vmMIName = item.miName || ''
  modelValue.value.vmMITenantId = item.tenantId || ''
  modelValue.value.vmMIClientId = item.clientId || ''
}

function logMIChanged() {
  let item = miList.value.find(itm => {
    return itm.resourceId == modelValue.value.logMI
  }) || {}

  modelValue.value.logMIName = item.name || ''
  modelValue.value.logMITenantId = item.tenantId || ''
  modelValue.value.logMIClientId = item.clientId || ''
}

// 日志捅列表
const logOptions = ref([])

function getLogsBlobContainerList() {
  logOptions.value = []
  clusterApi.getLogsBlobContainerList({region: modelValue.value.region}).then(res => {
    if (res.result == true) {
      logOptions.value = res.data
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

// 系统盘类型
const osDiskTypeList = ref([])

function getOsDiskTypeList() {
  osDiskTypeList.value = []
  clusterApi.getOsDiskTypeList({region: modelValue.value.region}).then(res => {
    if (res.result == true) {
      osDiskTypeList.value = res.data
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

// 物理机反亲和
const provisionTypeOption = ref([{
  name: '不启用',
  code: 'VM_Standalone'
}, {
  name: '启用',
  code: 'VMSS_Flexible'
}])

// 查询是否手动设置Ambari数据库名
const canEditDbname = ref(true)

function getAmbariDbNameManual() {
  clusterApi.getAmbariDbNameManual({region: modelValue.value.region}).then(res => {
    if (res.result == true) {
      canEditDbname.value = res.data
      if (!canEditDbname.value) {
        let name = modelValue.value.clusterName + '_ambaridb'
        modelValue.value.ambariDbCfgs.database = name.replace(/-/g, "_")

        if (modelValue.value.isEmbedAmbariDb) {
          modelValue.value.ambariDbCfgs.database = 'ambaridb'
        }
      }
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

// 实例信息
const vmskuList = ref([])
const vmskuColumns = [
  {prop: 'name', label: '机型名称', minWidth: 120, isFilter: true},
  {prop: 'family', label: 'sku系列', minWidth: 120, isFilter: true},
  {prop: 'vCoreCount', label: 'CPU核数', width: 100, isFilter: true},
  {prop: 'memoryGB', label: '内存数(GB)', width: 100, isFilter: true},
  {prop: 'ratio', label: 'CPU内存比', width: 100, isFilter: true, noColumn: true, controlType: 'select'},
  {prop: 'maxDataDisksCount', label: '最大磁盘数', width: 100, isFilter: true},
  {prop: 'cpuType', label: 'CPU类型', width: 100, isFilter: false},
]

function getVmskuList() {
  vmskuList.value = []
  clusterApi.getVmskuList({region: modelValue.value.region}).then(res => {
    if (res.result == true) {
      let arr = res.data || []
      vmskuList.value = arr

      vmskuList.value.forEach(item => {
        item.ratio = '1 : ' + parseFloat(item.memoryGB) / parseFloat(item.vCoreCount)
      })

      // if (arr.length) {
      //   let firstObj = arr[0]
      //
      //   modelValue.value.instanceGroupSkuCfgs.forEach(item => {
      //     if (!item.skuName) {
      //       item.skuName = firstObj.name
      //       item.vCPUs = firstObj.vCoreCount
      //       item.memoryGB = firstObj.memoryGB
      //       item.hasNVMeDisk = firstObj.hasNVMeDisk || false
      //
      //       if (firstObj.hasNVMeDisk) {
      //         item.dataVolumeSize = firstObj.tempNVMeDiskSizeGB
      //         item.dataVolumeCount = firstObj.tempNVMeDisksCount
      //         item.dataVolumeType = 'NVMe磁盘'
      //       }
      //
      //     }
      //   })
      // }
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

const refresh = ref(true)

function osDiskChange() {
  modelValue.value.instanceGroupSkuCfgs.forEach(item => {
    item.osVolumeType = modelValue.value.osDiskType
    item.osVolumeSize = modelValue.value.diskSize
  })

  refresh.value = false
  nextTick(() => {
    refresh.value = true
  })
}

function diskTypeToName(type) {
  if (type == 'NVMe磁盘') {
    return 'NVMe磁盘'
  }
  let item = osDiskTypeList.value.find(itm => {
    return itm.name == type
  }) || {}

  return item.name ? item.name + '云盘' : ''
}

function getNodeMinCount(item) {
  if (item.vmRole == 'Core' || item.vmRole == 'core') {
    return 2
  } else if (item.vmRole == 'Task' || item.vmRole == 'task') {
    return 0
  }
  return 1
}

function getNodeMaxCount(item) {
  if (item.vmRole == 'Task' || item.vmRole == 'task') {
    return 1999
  }
  return 1000
}

let dialogVisible = ref(false)

const editForm = reactive({
  vmskuName: "",
  skus: [],
  hasNVMeDisk: false,
  dataVolumeType: "",
  dataVolumeSize: "",
  dataVolumeCount: 1,
  provisionType: '',
  editItem: {},
  purchaseType: 1,
  spotAllocationStrategy: 'LowestPrice',
  regularAllocationStrategy: 'LowestPrice',
  vCPUs: 4,
})

const scriptFormRules = {
  purchaseType: FormCheck.required("请选择付费类型"),
  groupVmType: FormCheck.required(),
  regularAllocationStrategy: FormCheck.required(),
  spotAllocationStrategy: FormCheck.required(),
  vmskuName: FormCheck.required("请选择规格", 'change, blur'),
  disk: {
    validator: (rule, value, callback) => {

      if (!editForm.dataVolumeType) {
        return callback(new Error("请选择数据盘类型！"));
      }

      let reg = /^[1-9][0-9]*$/;
      if (!reg.test(editForm.dataVolumeSize)) {
        return callback(new Error("数据盘大小仅支持正整数"));
      }
      if (!reg.test(editForm.dataVolumeCount)) {
        return callback(new Error("数据盘个数仅支持正整数"));
      }

      if (editForm.dataVolumeSize < 200) {
        return callback(new Error("数据盘不能小于200G！"));
      } else if (editForm.dataVolumeSize > 4096) {
        editForm.dataVolumeSize = 4096
        return callback(new Error("数据盘不能大于4096G，已修改为4096G！"));
      }
      return callback();
    },
    trigger: 'blur',
    required: true
  },
  skus: {
    validator: (rule, value, callback) => {

      if (!editForm.skus) {
        return callback(new Error("请选择机型！"));
      }
      if (editForm.skus.length < 1) {
        return callback(new Error("请选择至少1个机型！"));
      }
      if (editForm.skus.length > 15) {
        return callback(new Error("最多不能超过15个机型！"));
      }
      return callback();
    },
    trigger: 'blur',
    required: true
  },
  provisionType: FormCheck.required("请选择是否启用物理机反亲和"),
}

const Ref_editForm = ref(null)

function editDisk(item) {
  editForm.vmskuName = item.skuName
  let skus = vmskuList.value.filter(itm => {
    if (item.skuNames && item.skuNames.includes(itm.name)) {
      return true
    }
    return false
  })
  editForm.skus = skus || []
  editForm.hasNVMeDisk = item.hasNVMeDisk
  editForm.dataVolumeType = item.dataVolumeType
  editForm.dataVolumeSize = item.dataVolumeSize || 0
  editForm.dataVolumeCount = item.dataVolumeCount || 1
  editForm.provisionType = item.provisionType || ''

  editForm.purchaseType = item.purchaseType || 1
  editForm.spotAllocationStrategy = item.spotAllocationStrategy || 'LowestPrice'
  editForm.regularAllocationStrategy = item.regularAllocationStrategy || 'LowestPrice'
  editForm.vCPUs = item.vCPUs || 4

  editForm.editItem = item

  let arr = []
  vmskuList.value.forEach(opItem => {
    arr.push({
      ...opItem,
      isDisable: vmskuIsDisable(opItem)
    })
  })
  vmskuList.value = arr

  dialogVisible.value = true

  let opItm = vmskuList.value.find(itm => {
    return itm.name == editForm.vmskuName
  }) || {}
  vmskuChanged(opItm)
}

const selectedSkus = computed(() => {
  let arr = vmskuList.value.filter(item => {
    if (item.vCoreCount == editForm.vCPUs) {
      if (parseFloat(item.memoryGB) / parseFloat(item.vCoreCount) == editForm.cpuMemoryRadio) {
        return true
      }
    }
    return false
  })

  return arr
})

function itemSelectedSkus(row) {
  let arr = vmskuList.value.filter(item => {
    if (item.vCoreCount == row.vCPUs) {
      if (parseFloat(item.memoryGB) / parseFloat(item.vCoreCount) == row.cpuMemoryRadio) {
        return true
      }
    }
    return false
  })

  return arr
}

function vmskuIsDisable(option) {
  if (option.name && option.name.includes('_L') && editForm.editItem.vmRole &&
      (editForm.editItem.vmRole.toLowerCase() == 'ambari' || editForm.editItem.vmRole.toLowerCase() == 'master')) {
    return true
  }
  return false
}

function vmskuChanged(item) {
  editForm.vmskuName = item.name

  editForm.hasNVMeDisk = item.hasNVMeDisk

  editForm.maxDataDiskCount = item.maxDataDiskCount || 99

  if (item.hasNVMeDisk) {
    editForm.dataVolumeSize = item.tempNVMeDiskSizeGB
    editForm.dataVolumeCount = item.tempNVMeDisksCount
    editForm.dataVolumeType = 'NVMe磁盘'
  } else {
    if (editForm.dataVolumeType == 'NVMe磁盘') {
      editForm.dataVolumeType = ''
      editForm.dataVolumeCount = 1
    }
  }
}

function saveEvent() {
  Ref_editForm.value.validate((valid, fields) => {
    if (valid) {

      let item = vmskuList.value.find(itm => {
        return itm.name == editForm.vmskuName
      }) || {}

      editForm.editItem.vCPUs = item.vCoreCount
      editForm.editItem.cpuType = item.cpuType
      editForm.editItem.memoryGB = item.memoryGB
      editForm.editItem.skuName = item.name

      editForm.editItem.hasNVMeDisk = editForm.hasNVMeDisk
      editForm.editItem.dataVolumeType = editForm.dataVolumeType
      editForm.editItem.dataVolumeSize = editForm.dataVolumeSize
      editForm.editItem.dataVolumeCount = editForm.dataVolumeCount || 1
      editForm.editItem.provisionType = editForm.provisionType || ''

      editForm.editItem.purchaseType = editForm.purchaseType || 1
      editForm.editItem.skus = editForm.skus || []
      let nameArr = []
      if (editForm.purchaseType == '2') {
        editForm.skus.forEach(itm => {
          nameArr.push(itm.name)
        })
      } else {
        nameArr.push(item.name);
      }
      editForm.editItem.skuNames = nameArr

      if (editForm.editItem.vmRole == 'Ambari' || editForm.editItem.vmRole == 'ambari') {

        let instanceGroupSkuCfgs = modelValue.value.instanceGroupSkuCfgs || []
        let itemMaster = instanceGroupSkuCfgs.find(item => {
          return item.vmRole == "Master" || item.vmRole == 'master'
        })

        if (itemMaster) {
          itemMaster.vCPUs = item.vCoreCount
          itemMaster.cpuType = item.cpuType
          itemMaster.memoryGB = item.memoryGB
          itemMaster.skuNames = nameArr
          itemMaster.skuName = item.name

          itemMaster.dataVolumeType = editForm.dataVolumeType
          itemMaster.dataVolumeSize = editForm.dataVolumeSize
          itemMaster.provisionType = editForm.provisionType
        }
      }

      editForm.editItem.spotAllocationStrategy = editForm.spotAllocationStrategy || 'LowestPrice'
      editForm.editItem.regularAllocationStrategy = editForm.regularAllocationStrategy || 'LowestPrice'

      dialogVisible.value = false
    } else {
      console.log('error submit!', fields)
    }
  })
}

function isHaChange() {
  if (modelValue.value.isHa) {
    let instanceGroupSkuCfgs = modelValue.value.instanceGroupSkuCfgs || []
    let itemMaster = instanceGroupSkuCfgs.find(item => {
      return item.vmRole == "Master" || item.vmRole == 'master'
    })
    itemMaster.cnt = 2

    let itemAmbari = instanceGroupSkuCfgs.find(item => {
      return item.vmRole == "Ambari" || item.vmRole == 'ambari'
    })
    itemAmbari.cnt = 1
  } else {
    let instanceGroupSkuCfgs = modelValue.value.instanceGroupSkuCfgs || []
    let itemMaster = instanceGroupSkuCfgs.find(item => {
      return item.vmRole == "Master" || item.vmRole == 'master'
    })
    itemMaster.cnt = 0
  }
}

function isEmbedAmbariDbChange() {
  if (modelValue.value.isEmbedAmbariDb) {
    modelValue.value.ambariDbCfgs.url = 'localhost'
    modelValue.value.ambariDbCfgs.port = '3306'
    modelValue.value.ambariDbCfgs.database = 'ambaridb'
    modelValue.value.ambariDbCfgs.account = 'root'
    modelValue.value.ambariDbCfgs.password = ''
  } else {
    modelValue.value.ambariDbCfgs.url = ''
    modelValue.value.ambariDbCfgs.port = '3306'
    let name = modelValue.value.clusterName + '_ambaridb'
    modelValue.value.ambariDbCfgs.database = name.replace(/-/g, "_")
    modelValue.value.ambariDbCfgs.account = ''
    modelValue.value.ambariDbCfgs.password = ''
  }
}

function objectSpanMethod({row, column, rowIndex, columnIndex,}) {
  if (modelValue.value.isHa == 1) {
    if (columnIndex === 2) {
      if (rowIndex === 0) {
        return {
          rowspan: 2,
          colspan: 1,
        }
      } else if (rowIndex === 1) {
        return {
          rowspan: 0,
          colspan: 0,
        }
      }
    }
  }
  return {
    rowspan: 1,
    colspan: 1,
  }
}

function addTaskGroup() {
  let instanceGroupSkuCfgs = modelValue.value.instanceGroupSkuCfgs || []

  let lastItem = instanceGroupSkuCfgs[instanceGroupSkuCfgs.length - 1] || {}
  lastItem = JSON.parse(JSON.stringify(lastItem))
  lastItem.vmRole = 'Task'
  lastItem.cnt = 0
  lastItem.nameCode = instanceGroupSkuCfgs.length - 2
  lastItem.groupName = 'task-' + lastItem.nameCode
  lastItem.groupCfgs = []
  lastItem.purchasePriority = '1'
  lastItem.maxPrice = ''

  lastItem.confGroupElasticScalingData = {
    groupName: lastItem.groupName,
    vmRole: lastItem.vmRole,
    clusterId: '',
    maxCount: 100,
    minCount: 0,
    scalingRules: [],
  }


  instanceGroupSkuCfgs.push(lastItem)
}

const Ref_TaskName_Input = ref(null)

function editGroupNameEvent(item) {
  let groupName = item.groupName || ''
  item.nameCode = groupName.replace('task-', '')

  item.editGroupName = true

  nextTick(() => {
    Ref_TaskName_Input.value.focus();
  })
}

function groupNameChanged(item) {
  item.editGroupName = false

  item.groupName = 'task-' + item.nameCode

  item.confGroupElasticScalingData.groupName = item.groupName
}

function deleteTask(index, item) {
  ElMessageBox.confirm(`您确定要删除实例组 " ${item.groupName} " 吗？`, '删除实例组', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    let instanceGroupSkuCfgs = modelValue.value.instanceGroupSkuCfgs || []

    instanceGroupSkuCfgs.splice(index, 1)
  })
}

let cfgDialogVisible = ref(false)
const editTaskItem = reactive({
  editItem: {},
  groupCfgs: []
})

function editTaskCfg(item) {
  editTaskItem.editItem = item
  editTaskItem.groupCfgs = JSON.parse(JSON.stringify(item.groupCfgs || []))

  cfgDialogVisible.value = true
}

function cfgSaveEvent() {
  editTaskItem.editItem.groupCfgs = editTaskItem.groupCfgs || []

  cfgDialogVisible.value = false
}

function getInstancePrice(item) {
  clusterApi.getInstancePrice({
    skuNames: [item.skuName],
    region: modelValue.value.region
  }).then(res => {
    if (res.result == true) {
      if (res.data && res.data.length) {
        let priceItem = res.data[0] || {}
        item.showPrice = priceItem.spotUnitPricePerHourUSD || ''
        item.onDemandUnitPricePerHourUSD = priceItem.onDemandUnitPricePerHourUSD || ''
      }
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

function scaleToStr(item) {
  if (item.vmRole.toLowerCase() == 'task' && item.confGroupElasticScalingData) {
    let scalingRules = item.confGroupElasticScalingData.scalingRules || []
    if (scalingRules.length) {
      let expandRules = scalingRules.filter(itm => {
        return itm.scalingType == 1 || itm.scalingType == null
      }) || []
      let reduceRules = scalingRules.filter(itm => {
        return itm.scalingType == 0
      }) || []
      return `已配置${expandRules.length}条弹性扩容规则；已配置${reduceRules.length}条弹性缩容规则`
    }
    return '未配置'
  } else {
    return '不可配置'
  }
}

const ruleConfDialogVisible = ref(false)
const editGroupData = ref({})
const newGroupConf = ref({})

function editScaleEvent(item) {
  editGroupData.value = item
  newGroupConf.value = item.confGroupElasticScalingData
  newGroupConf.value.dataType = 'local'

  ruleConfDialogVisible.value = true
}

function gangliaChanged() {
  if (modelValue.value.enableGanglia == '1') {
    modelValue.value.diskSize = 200
  } else {
    modelValue.value.diskSize = 100
  }

  osDiskChange()
}

initDefaultCfgs();

function reloadOptions() {
  getazlist()

  getNetworkList()
  getSubnetList()

  getPrimarySecurityGroupList()
  getSubSecurityGroupList()

  getKeypairList()
  getMIList()

  getLogsBlobContainerList()
  getOsDiskTypeList()

  getAmbariDbNameManual()
  getVmskuList()
}

onMounted(() => {
  getDataCenter()
})

</script>

<style lang="stylus" scoped type="text/stylus">
.step-3 {
  .pay-type-div {
    .el-radio {
      margin-right 15px;
    }

    .el-radio:nth-last-child(1) {
      margin-right 0;
    }
  }

  .vm-group-name-div {
    .group-name-edit {
      display flex;
      align-items center;

      .name-code {
        flex 1;
      }

      .edit-btn {
        padding 5px;
        cursor pointer;
        display flex;
        align-items center;
        justify-content center;

        &:hover {
          i {
            color #315FCE;
          }
        }
      }
    }
  }

  .vmgroup-opts {
    .el-button {
      margin-left 0;
      margin-right 10px;
      padding-left 0;
      padding-right 0;
    }
  }

  .el-table {
    --el-table-row-hover-bg-color: transparent
  }

  .dialog-content {
    max-height 60vh;
    overflow-y auto;

    .dialog-tip {
      padding 10px;
      background-color #F0F6FE;
      margin-bottom 30px;
    }

    .dv-count {
      display: flex;
      align-items: center;

      .unit-div {
        background-color: #f5f7fa;
        color: #909399;
        padding: 0 5px;
        box-shadow: 0 1px 0 0 #dcdfe6 inset, 0 -1px 0 0 #dcdfe6 inset, -1px 0 0 0 #dcdfe6 inset;
      }
    }
  }
}
</style>
