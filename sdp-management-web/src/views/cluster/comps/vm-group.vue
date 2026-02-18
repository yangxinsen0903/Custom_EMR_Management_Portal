/**Created by liaoyingchao on 12/29/22.*/

<template>
  <div class="vm-group" v-loading="pageLoading">
    <div class="functions-row">
      <el-button type="primary" @click="newEvent" :disabled="clusterData.state != '2' || !permissionCheck.currentPermissionCheck(['Maintainer', 'Administrator'])">新增实例组</el-button>
      <el-button type="primary" @click="toAmbari" :disabled="clusterData.state != '2' || !permissionCheck.currentPermissionCheck(['Maintainer', 'Administrator'])">Ambari管理</el-button>
      <el-button type="primary" :icon="RefreshRight" @click="reloadEvent" :disabled="clusterData.state != '2'">刷新
      </el-button>
      <el-radio-group v-model="clusterData.isParallelScale" style="margin-left: 80px"  @change="parallelScaleEvent" :disabled="clusterData.state != '2' || !permissionCheck.currentPermissionCheck(['Maintainer', 'Administrator'])">
        <el-radio-button :label="1" >并行扩缩容</el-radio-button>
        <el-radio-button :label="0" >串行扩缩容</el-radio-button>
      </el-radio-group>
    </div>
    <div class="list-data">
      <el-table :data="listData" header-row-class-name="theader" border
                style="width: 100%">
        <el-table-column prop="groupName" label="实例组名称" min-width="100"></el-table-column>
        <el-table-column prop="purchaseType" label="付费类型" min-width="90">
          <template #default="scope">
            <div style="cursor: pointer;" @mouseenter="getGroupInstancePrice(scope.row)">
              <div v-if="scope.row.purchaseType == '2'">
                <el-tooltip effect="light" placement="bottom">
                  <span>竞价</span>
                  <template #content>
                    <div>
                      <div v-if="scope.row.priceStrategy == '1'" style="display: flex;align-items: center;"><div>出价：标准价 {{scope.row.maxPrice}} %</div> <div style="margin-left: 10px;" v-if="scope.row.realPayPrice">({{scope.row.realPayPrice}} USD)</div></div>
                      <div v-else-if="scope.row.priceStrategy == '2'">出价：{{scope.row.maxPrice}} USD</div>
                    </div>
                  </template>
                </el-tooltip>
              </div>
              <div v-else>标准</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="state" label="状态" min-width="90">
          <template #default="scope">
            {{ stateToStr(scope.row.state) }}
          </template>
        </el-table-column>
        <el-table-column prop="vmCountByRole" label="节点数量" min-width="90">
          <template #default="scope">
            {{ scope.row.vmCountByRole }}{{ scope.row.purchaseType == 2 && scope.row.expectCount ? '（' + scope.row.expectCount + '）' : '' }}
          </template>
        </el-table-column>
        <el-table-column prop="sku" label="规格" min-width="130">
          <template #default="scope">
            <div v-if="scope.row.purchaseType == 2">
              <span style="margin-right: 15px;">共{{scope.row.skuNames.length}}个机型</span>
              <check-skus :skuNames="scope.row.skuNames" :region="clusterData.region"></check-skus>
            </div>
            <div v-else>
              {{ scope.row.sku }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sku" label="存储" min-width="130">
          <template #default="scope">
            <div v-if="scope.row.osVolumeType">系统盘：{{ scope.row.osVolumeType }}云盘 {{ scope.row.osVolumeSize }}GB * {{ scope.row.osVolumeCount }}</div>
            <div>数据盘：{{ scope.row.dataVolumeType }}云盘 {{ scope.row.dataVolumeSize }}GB * {{ scope.row.dataVolumeCount }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="scaleState" label="弹性伸缩" min-width="140">
          <template #default="scope">
            {{ scaleToStr(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column prop="scaleState" label="竞价实例开关" min-width="90">
          <template #default="scope">
            <div v-if="scope.row.purchaseType === 2">
              <el-switch v-model="scope.row.spotBuyState" inline-prompt active-text="开启买入" inactive-text="关闭买入" @change="changeSpotState(scope.row)"/><br/>
              <el-switch v-model="scope.row.spotDestoryState" inline-prompt active-text="开启逐出" inactive-text="关闭逐出"  @change="changeSpotState(scope.row)" />
            </div>
            <div v-else>
              不支持
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="operations" label="管理" width="320">
          <template #default="scope">
            <div class="operations-div">
              <el-button type="primary" text @click="operation.event(scope.row)"
                         :disabled="(clusterData.state != '2' || !permissionCheck.currentPermissionCheck(['Maintainer', 'Administrator'])) && operation.name != '伸缩记录'"
                         v-for="(operation, idx) in scope.row.operations" :key="idx">{{ operation.name }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 新增Task实例组 -->
    <el-dialog
            class="center-dialog"
            v-model="newDialogVisible"
            title="新增Task实例组"
            width="80%"
            destroy-on-close
    >
      <div class="dialog-content" style="height: 60vh;overflow-y: auto;">
        <div style="padding: 0 100px 30px;">
          <Steps v-model="stepIndex" :stepOptions="stepOptions"></Steps>
        </div>
        <el-form v-show="stepIndex == 0" ref="Ref_newForm" :model="createForm" :rules="createFormRules"
                 label-width="170px" style="width: 94%;">
          <el-form-item label="实例组名称" prop="groupName">
            <el-input v-model="createForm.nameCode" maxlength="10" show-word-limit placeholder="请输入实例组名称"
                      @change="groupNameCodeChanged">
              <template #prepend>task-</template>
            </el-input>
          </el-form-item>
          <el-form-item label="付费类型" prop="purchaseType">
            <div style="display: flex;align-items: flex-start;">
              <el-radio-group v-model="createForm.purchaseType">
                <el-radio :label="1">标准</el-radio>
                <el-radio :label="2">竞价</el-radio>
              </el-radio-group>
              <div style="margin-left: 30px;flex: 1;" v-if="createForm.purchaseType == '2'">
                <div style="display: flex;align-items: center;">
                  <el-select v-model="createForm.priceStrategy" placeholder="请选择出价类型">
                    <el-option label="按标准价百分比" :value="1"/>
                    <el-option label="固定价" :value="2"/>
                  </el-select>
                  <el-input style="margin-left: 15px;width: 200px;" placeholder="请输入百分比"
                            v-model="createForm.maxPrice" v-if="createForm.priceStrategy == '1'">
                    <template #append>%</template>
                  </el-input>
                  <el-input style="margin-left: 15px;width: 200px;" placeholder="请输入出价"
                            v-model="createForm.maxPrice" v-if="createForm.priceStrategy == '2'"></el-input>
                </div>
<!--                <div v-if="createForm.skuName">-->
<!--                  <span style="margin-right: 10px;font-size: 12px;line-height: 20px;"-->
<!--                        v-if="onDemandUnitPricePerHourUSD">标准价：{{ onDemandUnitPricePerHourUSD }} USD/h</span>-->
<!--                  <span style="margin-right: 10px;font-size: 12px;line-height: 20px;"-->
<!--                        v-if="showPrice">市场价：{{ showPrice }} USD/h</span>-->
<!--                  <el-tooltip effect="light" placement="bottom">-->
<!--                    <span style="font-size: 12px;color: #315FCE;cursor: pointer;line-height: 20px;">查看历史价格</span>-->
<!--                    <template #content>-->
<!--                      <div style="max-width: 430px;">-->
<!--                        <PriceHistoryChart :skuName="createForm.skuName" :region="clusterData.region"></PriceHistoryChart>-->
<!--                      </div>-->
<!--                    </template>-->
<!--                  </el-tooltip>-->
<!--                </div>-->
              </div>
            </div>
          </el-form-item>
<!--          <el-form-item label="按需分配策略" prop="regularAllocationStrategy" v-if="createForm.purchaseType == 1">-->
<!--            <el-radio-group v-model="createForm.regularAllocationStrategy">-->
<!--              <el-radio :label="'LowestPrice'">按最低价</el-radio>-->
<!--              <el-radio :label="'Prioritized'">按照指定的优先级</el-radio>-->
<!--            </el-radio-group>-->
<!--          </el-form-item>-->
          <template v-if="createForm.purchaseType == 2">
            <el-form-item label="竞价分配策略" prop="spotAllocationStrategy">
              <el-radio-group v-model="createForm.spotAllocationStrategy">
                <el-radio :label="'LowestPrice'">按最低价</el-radio>
                <el-radio :label="'CapacityOptimized'">容量</el-radio>
                <el-radio :label="'PriceCapacityOptimized'">容量和价格</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="规格选择" prop="skus">
              <div>
                <TableSelect :options="vmskuList" :tableColumn="vmskuColumns" height="210px"
                             v-model="createForm.skus" :mainKey="'name'"
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
                              <PriceHistoryChart :region="clusterData.region" :skuName="scope.row.name"></PriceHistoryChart>
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
                              <EvictionHistoryChart :region="clusterData.region"
                                                    :skuName="scope.row.name"></EvictionHistoryChart>
                            </div>
                          </template>
                        </el-tooltip>
                      </div>
                    </template>
                  </el-table-column>
                </TableSelect>
                <div v-show="createForm.skus.length">已选：{{ createForm.skus.map(itm => itm.name) }}</div>
              </div>
            </el-form-item>
          </template>
          <el-form-item label="规格选择" prop="vmskuName" v-else>
            <TableSelect :options="vmskuList" :tableColumn="vmskuColumns" height="200px"
                         v-model="createForm.vmskuName" :mainKey="'name'"
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
                          <PriceHistoryChart :region="clusterData.region" :skuName="scope.row.name"></PriceHistoryChart>
                        </div>
                      </template>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
              <el-table-column align="center" label="逐出率" min-width="120">
                <template #default="scope">
                  <div>
                    <div>{{ scope.row.evictionRateUpper }} %</div>
                    <el-tooltip effect="light" placement="bottom">
                      <span style="font-size: 12px;color: #315FCE;cursor: pointer;line-height: 20px;white-space: nowrap;">查看历史逐出率</span>
                      <template #content>
                        <div style="max-width: 430px;">
                          <EvictionHistoryChart :region="clusterData.region" :skuName="scope.row.name"></EvictionHistoryChart>
                        </div>
                      </template>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
            </TableSelect>
          </el-form-item>
          <el-form-item label="镜像版本" prop="osImageVersion">
            <el-select style="width: 100%;" placeholder="请选择镜像" v-model="createForm.osImageVersion"
                       @change="osImageChanged">
              <el-option
                  v-for="option in osImageList"
                  :key="option.imgId"
                  :label="option.imageVersion"
                  :value="option.osImageId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="节点数" prop="cnt">
            <el-input-number v-model="createForm.cnt" :min="0" :max="1999" :step-strictly="true"/>
          </el-form-item>
          <el-form-item label="数据盘" prop="disk">
            <div style="display: flex;width: 100%;">
              <el-select style="flex: 2;" placeholder="请选择磁盘类型" v-model="createForm.dataVolumeType">
                <el-option
                        v-for="option in osDiskTypeList"
                        :key="option.name"
                        :label="option.name"
                        :value="option.name"
                />
              </el-select>
              <el-input style="flex: 1;margin-left: 15px;" v-model="createForm.dataVolumeSize">
                <template #append>GB</template>
              </el-input>
              <div class="dv-count">
                <el-input-number style="width: 100px;margin-left: 15px;" v-model="createForm.dataVolumeCount" :min="1"
                                 :max="100"
                                 controls-position="right"/>
                <div class="unit-div">块</div>
              </div>
            </div>
          </el-form-item>
          <el-form-item label="执行实例初始化后脚本：">
            是
          </el-form-item>
          <el-form-item label="执行集群启动前脚本：" prop="enableBeforestartScript">
            <el-switch
                    v-model="createForm.enableBeforestartScript"
                    inline-prompt
                    active-text="是"
                    :active-value="1"
                    :inactive-value="0"
                    inactive-text="否"
            />
          </el-form-item>
          <el-form-item label="执行集群启动后脚本：" prop="enableAfterstartScript">
            <el-switch
                    v-model="createForm.enableAfterstartScript"
                    inline-prompt
                    active-text="是"
                    :active-value="1"
                    :inactive-value="0"
                    inactive-text="否"
            />
          </el-form-item>
        </el-form>
        <div v-show="stepIndex == 1">
          <div class="full-page-block-div">
            <div class="sub-title">参数配置</div>
            <ClusterArguments v-model="createForm.clusterCfgs"></ClusterArguments>
          </div>
          <div class="full-page-block-div">
            <div class="sub-title">弹性伸缩规则配置</div>
            <div class="add-btn" @click="confElasticRule">

              <div v-if="createForm.confGroupElasticScalingData.isFullCustody===0 || !createForm.confGroupElasticScalingData.isFullCustody">
                <div v-if="createForm.confGroupElasticScalingData.scalingRules.length">已配置{{ createForm.confGroupElasticScalingData.scalingRules.length }}条弹性伸缩规则</div>
                <div v-else>配置弹性伸缩规则</div>
              </div>
              <div v-else>已配置托管式弹性扩缩容</div>

            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="newDialogVisible = false">取消</el-button>
          <el-button @click="stepIndex = 0" v-if="stepIndex == 1">上一步</el-button>
          <el-button type="primary" @click="nextStep" v-if="stepIndex == 0">下一步</el-button>
          <el-button type="primary" @click="newSaveEvent" v-if="stepIndex == 1">确定</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 磁盘扩容 -->
    <el-dialog
            class="center-dialog"
            v-model="diskExpandDialogVisible"
            title="磁盘扩容"
            width="600"
            destroy-on-close
    >
      <div class="dialog-content">
        <el-form ref="Ref_diskExpandForm" :model="diskExpandForm" :rules="diskExpandFormRules" label-width="110px"
                 style="width: 94%;">
          <el-form-item label="实例组名称">
            <div>{{ diskExpandForm.editGroup.groupName }}</div>
          </el-form-item>
          <el-form-item label="当前配置">
            <div>数据盘 {{ diskExpandForm.editGroup.dataVolumeSize }}GB * {{ diskExpandForm.editGroup.dataVolumeCount }}
              {{ diskExpandForm.editGroup.dataVolumeType }}云盘
            </div>
          </el-form-item>
          <!--          <el-form-item label="期望配置">-->
          <!--            <div>数据盘 {{diskExpandForm.hopeSize}}GB * {{diskExpandForm.editGroup.dataVolumeCount}} {{ diskExpandForm.editGroup.dataVolumeType }}云盘</div>-->
          <!--          </el-form-item>-->
          <el-form-item label="磁盘扩容至" prop="dataVolumeSize">
            <div style="display: flex;width: 100%;">
              <div>数据盘</div>
              <el-input style="width: 100px;margin: 0 10px;" v-model="diskExpandForm.dataVolumeSize"></el-input>
              <div>GB *</div>
              <!--              <el-input style="width: 80px;margin: 0 10px;" v-model="diskExpandForm.dataVolumeCount"></el-input>-->
              <div>{{ diskExpandForm.editGroup.dataVolumeCount }} {{
                  diskExpandForm.editGroup.dataVolumeType
                }}云盘
              </div>
            </div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="diskExpandDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="diskExpandSaveEvent">确定</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 扩容 -->
    <el-dialog
            class="center-dialog rule-dialog"
            v-model="expandDialogVisible"
            title="扩容"
            width="800"
            destroy-on-close
    >
      <div class="dialog-content">
        <el-form ref="Ref_krForm" :model="editForm" :rules="editFormRules" label-width="180px" style="width: 94%;">
          <el-form-item label="实例组名称：">
            <div>{{ editForm.editGroup.groupName }}</div>
          </el-form-item>
          <el-form-item label="实例组类型：">
            <div>{{ editForm.editGroup.vmRole }}</div>
          </el-form-item>
          <el-form-item label="当前配置：">
            <div>{{ editForm.editGroup.sku }}</div>
          </el-form-item>
          <el-form-item label="网络/子网：">
            <div>{{ editForm.editGroup.vnet }} / {{ editForm.editGroup.subnet }}</div>
          </el-form-item>
          <el-form-item label="当前数量：">
            <div>{{ editForm.editGroup.vmCountByRole }}</div>
            <div style="margin-left: 10px;">台</div>
          </el-form-item>
          <el-form-item label="当前伸缩期望：" v-if="editForm.editGroup.purchaseType == '2'">
            <template #label>
              <div style="display: flex;align-items: center;">
                <span>当前伸缩期望：</span>
                <el-tooltip effect="light" placement="right">
                  <el-icon><QuestionFilled /></el-icon>
                  <template #content>
                    <div style="max-width: 320px;">该实例组当前所有伸缩任务成功执行完毕后的实例数。数值上等于「当前实例数」+「待执行任务执行成功将造成的实例数变化」+「执行中任务执行成功将造成的实例数变化」。</div>
                  </template>
                </el-tooltip>
              </div>
            </template>
            <div>{{ editForm.hopeCount }}</div>
            <div style="margin-left: 10px;">台</div>
            <div class="from-item-tip">当前实例组任务队列全部成功执行完毕后，该实例组节点数将变为 {{editForm.hopeCount}} 台。</div>
          </el-form-item>
          <el-form-item label="增加期望至：" prop="krCount">
            <template #label>
              <div style="display: flex;align-items: center;">
                <span>增加期望至：</span>
                <el-tooltip effect="light" placement="right">
                  <el-icon><QuestionFilled /></el-icon>
                  <template #content>
                    <div style="max-width: 320px;">希望使该实例组的实例数变化为多少。该字段需大于「{{ editForm.editGroup.purchaseType == '2' ? '当前伸缩期望' : '当前数量' }}」。</div>
                  </template>
                </el-tooltip>
              </div>
            </template>
            <div class="dv-count">
              <el-input-number style="width: 120px;" :min="editForm.hopeCount + 1"
                               :max="editForm.editGroup.vmRole == 'Task' ? 1999 :1000" :step-strictly="true"
                               v-model="editForm.krCount"/>
              <div style="margin-left: 10px;">台</div>
            </div>
          </el-form-item>
          <el-form-item label="执行实例初始化后脚本：">
            是
          </el-form-item>
          <el-form-item label="执行集群启动前脚本：" prop="enableBeforestartScript">
            <el-switch
                    v-model="editForm.enableBeforestartScript"
                    inline-prompt
                    active-text="是"
                    :active-value="1"
                    :inactive-value="0"
                    inactive-text="否"
            />
          </el-form-item>
          <el-form-item label="执行集群启动后脚本：" prop="enableAfterstartScript">
            <el-switch
                    v-model="editForm.enableAfterstartScript"
                    inline-prompt
                    active-text="是"
                    :active-value="1"
                    :inactive-value="0"
                    inactive-text="否"
            />
          </el-form-item>
<!--          <el-form-item label="是否启用物理机反亲和：" prop="provisionType" v-if="editForm.editGroup.vmRole.toLowerCase() != 'task'">-->
<!--            <el-select placeholder="请选择" v-model="editForm.provisionType">-->
<!--              <el-option-->
<!--                      v-for="option in provisionTypeOption"-->
<!--                      :key="option.code"-->
<!--                      :label="option.name"-->
<!--                      :value="option.code"-->
<!--              />-->
<!--            </el-select>-->
<!--          </el-form-item>-->
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="expandDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="expandSaveEvent">确定</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 缩容 -->
    <el-dialog
            class="center-dialog rule-dialog"
            v-model="reduceDialogVisible"
            title="缩容"
            width="800"
            destroy-on-close
    >
      <div class="dialog-content">
        <el-form ref="Ref_srForm" :model="editForm" :rules="editFormRules" label-width="180px" style="width: 94%;">
          <el-form-item label="实例组名称：">
            <div>{{ editForm.editGroup.groupName }}</div>
          </el-form-item>
          <el-form-item label="实例组类型：">
            <div>{{ editForm.editGroup.vmRole }}</div>
          </el-form-item>
          <el-form-item label="当前配置：">
            <div>{{ editForm.editGroup.sku }}</div>
          </el-form-item>
          <el-form-item label="网络/子网：">
            <div>{{ editForm.editGroup.vnet }} / {{ editForm.editGroup.subnet }}</div>
          </el-form-item>
          <el-form-item label="当前数量：">
            <div>{{ editForm.editGroup.vmCountByRole }}</div>
            <div style="margin-left: 10px;">台</div>
          </el-form-item>
          <el-form-item label="当前伸缩期望：" v-if="editForm.editGroup.purchaseType == '2'">
            <template #label>
              <div style="display: flex;align-items: center;">
                <span>当前伸缩期望：</span>
                <el-tooltip effect="light" placement="right">
                  <el-icon><QuestionFilled /></el-icon>
                  <template #content>
                    <div style="max-width: 320px;">该实例组当前所有伸缩任务成功执行完毕后的实例数。数值上等于「当前实例数」+「待执行任务执行成功将造成的实例数变化」+「执行中任务执行成功将造成的实例数变化」。</div>
                  </template>
                </el-tooltip>
              </div>
            </template>
            <div>{{ editForm.hopeCount }}</div>
            <div style="margin-left: 10px;">台</div>
            <div class="from-item-tip">当前实例组任务队列全部成功执行完毕后，该实例组节点数将变为 {{editForm.hopeCount}} 台。</div>
          </el-form-item>
          <el-form-item label="减少期望至：" prop="srCount">
            <template #label>
              <div style="display: flex;align-items: center;">
                <span>减少期望至：</span>
                <el-tooltip effect="light" placement="right">
                  <el-icon><QuestionFilled /></el-icon>
                  <template #content>
                    <div style="max-width: 320px;">希望使该实例组的实例数变化为多少。该字段需小于「{{ editForm.editGroup.purchaseType == '2' ? '当前伸缩期望' : '当前数量' }}」。</div>
                  </template>
                </el-tooltip>
              </div>
            </template>
            <div class="dv-count">
              <el-input-number style="width: 120px;" :min="srMinCount()" :max="srMaxCount()"
                               :step-strictly="true" v-model="editForm.srCount"/>
              <div style="margin-left: 10px;">台</div>
            </div>
          </el-form-item>
          <el-form-item label="优雅缩容：" prop="isGracefulScalein">
            <template #label>
              <div style="display: flex;align-items: center;">
                <span>优雅缩容：</span>
                <el-tooltip effect="light" placement="right">
                  <el-icon>
                    <QuestionFilled/>
                  </el-icon>
                  <template #content>
                    <div style="max-width: 320px;">
                      您可以设置超时时间，释放YARN上作业所在的节点。如果节点没有运行YARN上的作业或者作业运行超出您设置的超时时间，则释放此节点。超时时间最大值为1800秒。
                    </div>
                  </template>
                </el-tooltip>
              </div>
            </template>
            <el-switch
                    v-model="editForm.isGracefulScalein"
                    inline-prompt
                    active-text="是"
                    :active-value="1"
                    :inactive-value="0"
                    inactive-text="否"
            />
          </el-form-item>
          <el-form-item label="等待时间：" prop="scaleinWaitingtime" v-if="editForm.isGracefulScalein == 1">
            <el-input style="width: 200px;" v-model="editForm.scaleinWaitingtime"
                      placeholder="请输入缩容等待时间"></el-input>
            <div style="margin-left: 10px;">秒</div>
            <div class="from-item-tip">可选缩容等待时间范围为60-1800秒</div>
          </el-form-item>
          <el-form-item label="暴力缩容DataNode：" prop="forceScaleinDataNode">
            <el-switch
                    v-model="editForm.forceScaleinDataNode"
                    inline-prompt
                    active-text="开启"
                    :active-value="1"
                    :inactive-value="0"
                    inactive-text="关闭"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="reduceDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="reduceSaveEvent">确定</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 配置弹性伸缩 -->
    <el-dialog
            class="center-dialog"
            v-model="ruleConfDialogVisible"
            title="配置弹性伸缩"
            width="1100"
            destroy-on-close
            @closed="configScaleRuleClosed"
    >
      <div class="dialog-content">
        <ElasticScaling :clusterId="clusterId" :editGroupData="editGroupData"
                        :newGroupConf="newGroupConf"></ElasticScaling>
      </div>
      <template #footer>
        <div class="dialog-footer" style="margin-top: -30px;">
          <el-button @click="ruleConfDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 伸缩记录 -->
    <el-dialog
            class="center-dialog vm-history-dialog"
            v-model="historyDialogVisible"
            :title="historyGroupName + '伸缩记录'"
            width="80%"
            append-to-body
            destroy-on-close
    >
      <div class="dialog-content" style="height: 95vh;display: flex;flex-direction: column;">
        <VmNumChart :clusterId="clusterId" :groupName="historyGroupName"></VmNumChart>
        <div style="flex: 1;">
          <el-tabs v-model="activeName" class="full-tabs" @tab-change="tabChange">
            <el-tab-pane label="任务队列" name="taskList">
              <div style="padding-top: 12px;height: 100%">
                <HistoryList :clusterId="clusterId" :groupName="historyGroupName"></HistoryList>
              </div>
            </el-tab-pane>
            <el-tab-pane label="任务日志" name="taskLog">
              <div style="padding-top: 12px;height: 100%" v-if="taskLogLoaded">
                <TaskList :clusterId="clusterId" :groupName="historyGroupName" @showVms="showVms"
                          @showTaskDetail="showTaskDetail"></TaskList>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
<!--      <template #footer>-->
<!--        <div class="dialog-footer">-->
<!--          <el-button @click="historyDialogVisible = false">关闭</el-button>-->
<!--        </div>-->
<!--      </template>-->
    </el-dialog>
    <!-- 伸缩实例列表 -->
    <el-dialog
            class="center-dialog"
            v-model="vmsDialogVisible"
            title="伸缩实例列表"
            width="80%"
            append-to-body
            destroy-on-close
    >
      <div class="dialog-content vm-group-vms-list">
        <div class="full-page-block-div">
          <div class="detail-items top-box">
            <div class="flex-row">
              <div class="detail-item">
                <div class="label">实例组名称：</div>
                <div class="value">{{ taskInfoData.groupName }}</div>
              </div>
              <div class="detail-item">
                <div class="label">伸缩任务ID：</div>
                <div class="value">{{ taskInfoData.taskId }}</div>
              </div>
              <div class="detail-item">
                <div class="label">任务开始时间：</div>
                <div class="value">{{ formatTime(taskInfoData.begTime) }}</div>
              </div>
            </div>
            <div class="flex-row">
              <div class="detail-item">
                <div class="label">伸缩方式：</div>
                <div class="value">{{ operationTypeToStr(taskInfoData) }}</div>
              </div>
              <div class="detail-item">
                <div class="label">执行状态：</div>
                <div class="value">{{ vmStateToStr(taskInfoData.state) }}</div>
              </div>
              <div class="detail-item">
                <div class="label">任务结束时间：</div>
                <div class="value">{{ formatTime(taskInfoData.endTime) }}</div>
              </div>
            </div>
          </div>
        </div>
        <div>
          <el-table
                  height="40vh"
                  :data="taskInfoVms"
                  stripe
                  border
                  header-row-class-name="theader"
                  style="width: 100%">
            <el-table-column
                    label="序号"
                    align="center"
                    width="60">
              <template #default="scope">
                {{ scope.$index + 1 }}
              </template>
            </el-table-column>
            <el-table-column
                    prop="vmName"
                    label="实例ID"
                    min-width="150">
            </el-table-column>
            <el-table-column
                    prop="internalip"
                    label="内网IP"
                    width="120">
            </el-table-column>
            <el-table-column
                    prop="skuName"
                    label="规格"
                    min-width="100">
            </el-table-column>
            <el-table-column
                    label="磁盘"
                    min-width="200">
              <template #default="scope">
                <div>
                  系统盘：{{ scope.row.osVolumeSize }}GB*{{ scope.row.osVolumeCount }}
                  数据盘：{{ scope.row.dataVolumeSize }}GB*{{ scope.row.dataVolumeCount }}
                </div>
              </template>
            </el-table-column>
            <el-table-column
                label="状态"
                min-width="50">
              <template #default="scope">
                <div>
                  {{ vmInstanceStateToStr(scope.row.state, scope.row.maintenanceMode) }}
                </div>
              </template>
            </el-table-column>
            <el-table-column
                label="是否维护"
                min-width="40">
              <template #default="scope">
                <div>
                  {{ isMaintenanceStr(scope.row.maintenanceMode) }}
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="danger" @click="deleteVms" v-if="taskInfoData.scalingType == '1'">删除相关实例</el-button>
          <el-button @click="vmsDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 任务详情 -->
    <el-dialog
            class="center-dialog"
            v-model="taskDetailDialogVisible"
            title="任务详情"
            width="900"
            destroy-on-close
            append-to-body
            @closed="taskDetailClosed"
    >
      <div>
        <TaskDetail :detailData="detailData"></TaskDetail>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="taskDetailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 清理历史数据 -->
    <el-dialog
        class="center-dialog"
        v-model="cleanAmbariExpandDialogVisible"
        title="清理历史数据"
        width="600"
        destroy-on-close
    >
      <div class="dialog-content">
        <div class="date-picker">
          <span>清理</span>
          <el-date-picker
              v-model="cleanAmbariStartDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择清理日期"
              :clearable="false"
              size="large"
          />
          <span>之前的数据</span>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cleanAmbariExpandDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="cleanAmbariHistoryEvent">确定</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- pv2磁盘调整 -->
    <el-dialog
        class="center-dialog"
        v-model="pv2diskExpandDialogVisible"
        title="pv2磁盘调整"
        width="600"
        destroy-on-close
    >
      <div class="dialog-content">
        <el-form ref="Ref_diskExpandForm" :model="diskExpandForm" :rules="diskExpandFormRules" label-width="110px"
                 style="width: 94%;">
          <el-form-item label="实例组名称">
            <div>{{ diskExpandForm.editGroup.groupName }}</div>
          </el-form-item>
          <el-form-item label="磁盘配置">
            <div>数据盘 {{ diskExpandForm.editGroup.dataVolumeSize }}GB * {{ diskExpandForm.editGroup.dataVolumeCount }}
              {{ diskExpandForm.editGroup.dataVolumeType }}云盘
            </div>
          </el-form-item>
          <el-form-item label="当前配置">
            <div style="display: flex;width: 100%;">
            <div>IOPS:{{diskExpandForm.editGroup.iops}}</div>
            <div style="margin: 0 90px;"> 吞吐量:{{diskExpandForm.editGroup.throughput}}MB</div>
            </div>
          </el-form-item>
          <el-form-item label="IOPS" prop="newDataDiskIOPSReadWrite">
            <div style="display: flex;width: 100%;">
              <el-input v-model="diskExpandForm.newDataDiskIOPSReadWrite"></el-input>
            </div>
          </el-form-item>
          <el-form-item label="吞吐量" prop="newDataDiskMBpsReadWrite">
            <div style="display: flex;width: 100%;">
              <el-input v-model="diskExpandForm.newDataDiskMBpsReadWrite"></el-input>
            </div>
          </el-form-item>

        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="pv2diskExpandDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updateDiskIOPSAndThroughput">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {QuestionFilled, RefreshRight} from '@element-plus/icons-vue'
import {ref, reactive, defineEmits, defineProps, toRefs, nextTick, onBeforeUnmount, computed} from 'vue'
import clusterApi from "../../../api/cluster";
import {ElMessage, ElMessageBox} from 'element-plus';
import HistoryList from "./history-list"
import TaskList from "./task-list"
import FormCheck from "../../../utils/formCheck";
import {throttle} from "../../../utils/tools";
import {formatTime, columnTimeFormat, formatTimeYMD} from "@/utils/format-time";
import Steps from '@/components/base/steps/steps'
import ClusterArguments from "./arguments"
import VmNumChart from "@/views/cluster/comps/vm-num-chart.vue";
import taskCenterApi from "@/api/task-center";
import TaskDetail from "../../task-center/detail"
import ElasticScaling from "@/views/cluster/comps/elastic-scaling.vue";
import PriceHistoryChart from "@/views/cluster/comps/price-history-chart.vue";
import TableSelect from "@/components/base/table-select/index.vue";
import permissionCheck from "@/utils/permission-check";
import EvictionHistoryChart from "@/views/cluster/comps/eviction-history-chart.vue";
import CheckSkus from "@/views/cluster/comps/check-skus.vue";

const coolingTimeFormat = function (str) {
  let t = formatTime(str)
  if (t == '-') {
    return str
  }
  return t
}

const emit = defineEmits(['resizeEvent', 'toAmbari']);

const pageLoading = ref(false)

const props = defineProps({
  clusterId: {
    type: String,
    default: ''
  },
  clusterData: {
    type: Object,
    default: () => {
    }
  }
});
const {clusterId, clusterData} = toRefs(props)

// 物理机反亲和
const provisionTypeOption = ref([{
  name: '不启用',
  code: 'VM_Standalone'
}, {
  name: '启用',
  code: 'VMSS_Flexible'
}])

// 状态
const stateOptions = [
  // {"label": "全部", "value": ""},
  {"label": "待创建", "value": 0},
  {"label": "创建中", "value": 1},
  {"label": "已创建", "value": 2},
  {"label": "释放中", "value": -1},
  {"label": "已释放", "value": -2},
  {"label": "已删除", "value": -3},
]


function stateToStr(state) {
  let option = stateOptions.find(item => {
    return item.value === state
  }) || {}
  return option.label || state
}

const vmInstanceStateOptions = [
  {"label": "已关闭", "value": 0},
  {"label": "运行中", "value": 1},
  {"label": "销毁中", "value": -10},
  {"label": "已销毁", "value": -1},
  {"label": "未知", "value": -99}
]

function vmInstanceStateToStr(state, maintenanceMode) {
  let option = vmInstanceStateOptions.find(item => {
    return item.value === state
  }) || {}
  let stateStr = option.label || state
  if (maintenanceMode == '1') {
    stateStr += '(维护)'
  }
  return stateStr;
}

function isMaintenanceStr(maintenanceMode) {
  if (maintenanceMode == '1') {
    return "是"
  } else {
    return "否"
  }
}

// 弹性扩缩容状态
// 0：不可配置 1：未配置 2：已配置
const scaleState = [
  {"label": "不可配置", "value": 0},
  {"label": "未配置", "value": 1},
  {"label": "已配置", "value": 2}
]

function scaleStateToStr(state) {
  let option = scaleState.find(item => {
    return item.value == state
  }) || {}
  return option.label || state
}

function scaleToStr(item) {
  let str = scaleStateToStr(item.scaleState)
  if (item.scaleState == '1') {
    let scalingRules = item.scalingRules || []
    if (scalingRules.length) {
      let expandRules = scalingRules.filter(itm => {
        return itm.scalingType == 1 || itm.scalingType == null
      }) || []
      let reduceRules = scalingRules.filter(itm => {
        return itm.scalingType == 0
      }) || []
      str = `已配置${expandRules.length}条弹性扩容规则；已配置${reduceRules.length}条弹性缩容规则`
    }
    if (item.isFullCustody === 1){
      str="托管式弹性扩缩容"
    }
  }

  return str
}

const loadMetricOptions = [
  {"label": "可用内存百分比", "value": 'MemoryAvailablePrecentage'},
  {"label": "容器分配比率", "value": 'ContainerPendingRatio'},
  {"label": "可用vCore百分比", "value": 'VCoreAvailablePrecentage'},
]

function loadMetricToStr(key) {
  let option = loadMetricOptions.find(item => {
    return item.value == key
  }) || {}
  return option.label || key
}

const aggregateTypeOptions = [
  {"label": "最大值", "value": 'max'},
  {"label": "最小值", "value": 'min'},
  {"label": "平均值", "value": 'avg'},
]

function aggregateTypeToStr(key) {
  let option = aggregateTypeOptions.find(item => {
    return item.value == key
  }) || {}
  return option.label || key
}

const masterOperations = []

const coreOperations = [
  {
    name: '磁盘扩容',
    event: (item) => {
      diskExpandEvent(item)
    }
  },
  {
    name: '扩容',
    event: (item) => {
      expandEvent(item)
    }
  },
  {
    name: '缩容',
    event: (item) => {
      reduceEvent(item)
    }
  },
  {
    name: '伸缩记录',
    event: (item) => {
      showHistoryEvent(item)
    }
  },
]
const taskOperations = [
  {
    name: '扩容',
    event: (item) => {
      expandEvent(item)
    }
  },
  {
    name: '缩容',
    event: (item) => {
      reduceEvent(item)
    }
  },
  {
    name: '伸缩规则',
    event: (item) => {
      ruleConfEvent(item)
    }
  },
  {
    name: '伸缩记录',
    event: (item) => {
      showHistoryEvent(item)
    }
  },
  {
    name: '删除实例组',
    event: (item) => {
      ElMessageBox.confirm(`您确定要删除实例组 " ${item.groupName} " 吗？`, '删除实例组', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        deleteGroup(item)
      })
    }
  },
]

const pv2Operations = [
  {
    name: 'pv2磁盘调整',
    event: (item) => {
      diskExpandForm.editGroup = item
      diskExpandForm.newDataDiskIOPSReadWrite = item.iops
      diskExpandForm.newDataDiskMBpsReadWrite = item.throughput
      pv2diskExpandDialogVisible.value=true
    }
  },
  {
    name: '伸缩记录',
    event: (item) => {
      showHistoryEvent(item)
    }
  }
]

const ambariOperations = [
  {
    name: '清理历史数据',
    event: (item) => {
      cleanAmbariExpandDialogVisible.value=true;
      cleanAmbariStartDate.value=formatTimeYMD(new Date())
    }
  },
]
function cleanAmbariHistoryEvent(){
  ElMessageBox.confirm(`您确定要清理 " ${cleanAmbariStartDate.value} " 之前的历史数据吗？`, '清理历史数据', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    clusterApi.cleanAmbariHistory({clusterId: clusterId.value,startDate:cleanAmbariStartDate.value}).then(res =>{
      if (res.result == true) {
        cleanAmbariExpandDialogVisible.value=false;
        ElMessage.success("清理历史数据成功！")
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    })

  })
}

const updateDiskIOPSAndThroughput = throttle(function () {
  Ref_diskExpandForm.value.validate((valid, fields) => {
    if (valid) {
      ElMessageBox.confirm(`您确定要磁盘调整为IOPS:${diskExpandForm.newDataDiskIOPSReadWrite}和吞吐量:${diskExpandForm.newDataDiskMBpsReadWrite}MB吗？`, '磁盘调整', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        clusterApi.updateDiskIOPSAndThroughput({
          clusterId: clusterId.value,
          vmConfId: diskExpandForm.editGroup.vmConfId,
          newDataDiskIOPSReadWrite: diskExpandForm.newDataDiskIOPSReadWrite,
          newDataDiskMBpsReadWrite: diskExpandForm.newDataDiskMBpsReadWrite
        }).then(res => {
          if (res.result == true) {
            pv2diskExpandDialogVisible.value = false;
            ElMessage.success("磁盘调整成功！")
          } else {
            ElMessage.error(res.errorMsg)
            console.log(res)
          }
        })

      })
    }
  })
}, 500)



const listData = ref()

function getVmOverview() {
  clusterApi.getVmOverview({clusterId: clusterId.value}).then(res => {
    if (res.result == true) {
      listData.value = res.data
      listData.value.forEach(item => {
        let operationArr=[]
        if (item.vmRole == 'Master') {
          operationArr= [...masterOperations]
        } else if (item.vmRole == 'Core') {
          operationArr = [...coreOperations]
        } else if (item.vmRole == 'Task') {
          if (item.state == -3) {
            operationArr = taskOperations.filter(itm => {
              return itm.name == '伸缩记录'
            }) || []
          } else {
            operationArr = [...taskOperations]
          }
        }else if (item.vmRole == 'Ambari'){
          operationArr = [...ambariOperations]
        }

        if (item.dataVolumeType == 'PremiumV2_LRS'){
          for (let pv2Operation of pv2Operations) {
            if ((item.vmRole == 'Core' || item.vmRole == 'Task') && pv2Operation.name == '伸缩记录'){
              continue
            }
            if (item.state == 2) {
              operationArr.push(pv2Operation)
            }
          }
        }

        item.operations = operationArr
      })
      emit('resizeEvent')
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

getVmOverview()

// 刷新

function reloadEvent() {
  getVmOverview()
}

function parallelScaleEvent(item) {
  clusterApi.updateClusterParallel({
    clusterId: clusterId.value,
    isParallelScale:item
  }).then(res => {
    if (res.result == true) {

    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

// 系统盘类型
const osDiskTypeList = ref([])

function getOsDiskTypeList() {
  clusterApi.getOsDiskTypeList({region: clusterData.value.region}).then(res => {
    if (res.result == true) {
      osDiskTypeList.value = res.data
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
  clusterApi.getVmskuList({region: clusterData.value.region}).then(res => {
    if (res.result == true) {
      let arr = res.data || []
      vmskuList.value = arr

      vmskuList.value.forEach(item => {
        item.ratio = '1 : ' + parseFloat(item.memoryGB) / parseFloat(item.vCoreCount)
      })
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

const osImageList = ref([])

function getOsImageList() {
  clusterApi.getOsImageList({clusterId: clusterId.value}).then(res => {
    if (res.result == true) {
      let arr = res.data || []
      arr.forEach(item => {
        item.sname = item.imageVersion
      })
      osImageList.value = arr
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}


let newDialogVisible = ref(false)
const createForm = reactive({
  vmskuName: '',
  vmskuNames: [],
  skus: [],
  nameCode: '',
  groupName: '',
  cnt: 0,
  hasNVMeDisk: '',
  maxDataDiskCount: 99,
  dataVolumeSize: '',
  dataVolumeCount: '',
  dataVolumeType: '',
  purchaseType: '',
  priceStrategy: '',
  purchasePriority: '',
  maxPrice: '',
  enableBeforestartScript: 0,
  enableAfterstartScript: 0,
  clusterCfgs: [],
  confGroupElasticScalingData: {
    dataType: 'local',
    maxCount: 100,
    minCount: 0,
    scalingRules: [],
    fullCustodyParam: {},  // 全托管参数
    defaultFullCustodyParam: {}  // 全托管默认参数
  },
  groupVmType: 'VM',
  spotAllocationStrategy: 'LowestPrice',
  regularAllocationStrategy: 'LowestPrice',
  vCPUs: 4,
  cpuMemoryRadio: 4,
})
const createFormRules = {
  groupName: FormCheck.required('请输入集群组名称'),
  vmskuName: FormCheck.required('请选择规格'),
  cnt: FormCheck.required('请输入集群组节点数'),
  purchaseType: {
    validator: (rule, value, callback) => {

      if (createForm.purchaseType == '2') {
        if (!createForm.priceStrategy) {
          return callback(new Error("请选择出价类型！"));
        }

        if (!createForm.maxPrice) {
          if (createForm.priceStrategy == '1') {
            return callback(new Error("请输入出价百分比"));
          }
          if (createForm.priceStrategy == '2') {
            return callback(new Error("请输入出价"));
          }
        }
      }

      return callback();
    },
    trigger: 'blur',
    required: true
  },
  groupVmType: FormCheck.required(),
  regularAllocationStrategy: FormCheck.required(),
  spotAllocationStrategy: FormCheck.required(),
  osImageVersion: FormCheck.required('请选择镜像'),
  disk: {
    validator: (rule, value, callback) => {

      if (!createForm.dataVolumeType) {
        return callback(new Error("请选择数据盘类型！"));
      }

      let reg = /^[1-9][0-9]*$/;
      if (!reg.test(createForm.dataVolumeSize)) {
        return callback(new Error("数据盘大小仅支持正整数"));
      }
      if (!reg.test(createForm.dataVolumeCount)) {
        return callback(new Error("数据盘个数仅支持正整数"));
      }

      if (createForm.dataVolumeSize < 200) {
        return callback(new Error("数据盘不能小于200G！"));
      } else if (createForm.dataVolumeSize > 4096) {
        createForm.dataVolumeSize = 4096
        return callback(new Error("数据盘不能大于4096G，已修改为4096G！"));
      }
      return callback();
    },
    trigger: 'blur',
    required: true
  },
  skus: {
    validator: (rule, value, callback) => {

      if (!createForm.skus) {
        return callback(new Error("请选择机型！"));
      }
      if (createForm.skus.length < 1) {
        return callback(new Error("请选择至少1个机型！"));
      }
      if (createForm.skus.length > 15) {
        return callback(new Error("最多不能超过15个机型！"));
      }
      return callback();
    },
    trigger: 'blur',
    required: true
  },
  enableBeforestartScript: FormCheck.required('请选择是否执行集群启动前脚本'),
  enableAfterstartScript: FormCheck.required('请选择是否执行集群启动后脚本'),
}
const stepIndex = ref(0)
const Ref_newForm = ref(null)

const stepOptions = [{
  name: '基础配置',
}, {
  name: '参数与规则配置',
}]

function vmskuChanged(item) {
  createForm.vmskuName = item.name

  createForm.hasNVMeDisk = item.hasNVMeDisk

  createForm.vCPUs = item.hasNVMeDisk
  createForm.memoryGB = item.memoryGB
  createForm.skuName = item.name


  createForm.maxDataDiskCount = item.maxDataDiskCount || 99

  if (item.hasNVMeDisk) {
    createForm.dataVolumeSize = item.tempNVMeDiskSizeGB
    createForm.dataVolumeCount = item.tempNVMeDisksCount
    createForm.dataVolumeType = 'NVMe磁盘'
  } else {
    if (createForm.dataVolumeType == 'NVMe磁盘') {
      createForm.dataVolumeType = ''
      createForm.dataVolumeCount = 1
    }
    if (!createForm.dataVolumeCount) {
      createForm.dataVolumeCount = 1
    }
  }
}

function osImageChanged() {
  console.log("createForm.osImageVersion=" + createForm.osImageVersion)
  let item = osImageList.value.find(itm => {
    return itm.osImageId == createForm.osImageVersion
  }) || {}

  console.log("item=" + item)

  createForm.imgId = item.imgId

  createForm.osImageId = item.osImageId

  console.log(createForm)
}


function newEvent() {
  getVmskuList()
  getOsDiskTypeList()
  getOsImageList()

  stepIndex.value = 0

  createForm.vmskuName = ''
  createForm.vCPUs = ''
  createForm.memoryGB = ''
  createForm.skuName = ''
  createForm.cnt = ''
  createForm.dataVolumeSize = ''
  createForm.dataVolumeCount = ''
  createForm.dataVolumeType = ''
  let i = -2;
  createForm.nameCode = listData.value.length + i
  createForm.groupName = 'task-' + createForm.nameCode
  while (checkGroupName(createForm.groupName)) {
    i++
    createForm.nameCode = listData.value.length + i
    createForm.groupName = 'task-' + createForm.nameCode
  }

  createForm.purchasePriority = '1'
  createForm.purchaseType = 1
  createForm.priceStrategy = ''
  createForm.maxPrice = ''
  createForm.enableBeforestartScript = 1
  createForm.enableAfterstartScript = 1
  createForm.clusterCfgs = []
  createForm.confGroupElasticScalingData = {
    dataType: 'local',
    maxCount: 100,
    minCount: 0,
    scaleinWaitingTime: 300,
    isGracefulScalein: 1,
    enableAfterstartScript: 1,
    enableBeforestartScript: 1,
    scalingRules: []
  }

  createForm.groupVmType = 'VM'
  createForm.spotAllocationStrategy = 'LowestPrice'
  createForm.regularAllocationStrategy = 'LowestPrice'
  createForm.vCPUs = 4
  createForm.cpuMemoryRadio = 4

  newDialogVisible.value = true
}

const selectedSkus = computed(() => {
  let arr = vmskuList.value.filter(item => {
    if (item.vCoreCount == createForm.vCPUs) {
      if (parseFloat(item.memoryGB) / parseFloat(item.vCoreCount) == createForm.cpuMemoryRadio) {
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

function checkGroupName(name) {
  let idx = listData.value.findIndex(itm => {
    return itm.groupName == name
  })

  if (idx == -1) {
    return false
  }
  return true
}

function groupNameCodeChanged() {
  createForm.groupName = 'task-' + createForm.nameCode
}

function confElasticRule() {

  editGroupData.value = {}
  newGroupConf.value = createForm.confGroupElasticScalingData

  ruleConfDialogVisible.value = true
}

const showPrice = ref('')
const onDemandUnitPricePerHourUSD = ref('')

function getInstancePrice() {
  clusterApi.getInstancePrice({
    skuNames: [createForm.skuName],
    region: clusterData.value.region
  }).then(res => {
    if (res.result == true) {
      if (res.data && res.data.length) {
        let priceItem = res.data[0] || {}
        showPrice.value = priceItem.spotUnitPricePerHourUSD || ''
        onDemandUnitPricePerHourUSD.value = priceItem.onDemandUnitPricePerHourUSD || ''
      }
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

function getGroupInstancePrice(item) {
  // if (item.purchaseType == '2' && item.priceStrategy == '1' && !item.showPrice) {
  //   clusterApi.getInstancePrice({
  //     skuNames: [item.sku],
  //     region: clusterData.value.region
  //   }).then(res => {
  //     if (res.result == true) {
  //       item.showPrice = res.data.spotUnitPricePerHourUSD || ''
  //       item.onDemandUnitPricePerHourUSD = res.data.onDemandUnitPricePerHourUSD || ''
  //       item.realPayPrice = parseFloat(item.onDemandUnitPricePerHourUSD) * parseFloat(item.maxPrice) / 100
  //     } else {
  //       ElMessage.error(res.errorMsg)
  //       console.log(res)
  //     }
  //   })
  // }
}

const newSaveEvent = throttle(function () {
  pageLoading.value = true
  let skuNames=[];
  if(createForm.purchaseType==2){
    skuNames=createForm.skus.map(sku => sku.name)
  }else{
    skuNames.push(createForm.skuName)
  }

  clusterApi.addGroup({
    srcClusterId: clusterId.value,
    groupName: createForm.groupName,
    vmRole: 'Task',
    instanceGroupSkuCfgs: [
      {
        cnt: createForm.cnt,
        vmRole: 'Task',
        groupName: createForm.groupName,
        dataVolumeSize: createForm.dataVolumeSize,
        dataVolumeType: createForm.dataVolumeType,
        dataVolumeCount: createForm.dataVolumeCount,
        skuNames: skuNames,
        vCPUs: createForm.vCPUs,
        memoryGB: createForm.memoryGB,
        purchaseType: createForm.purchaseType,
        purchasePriority: createForm.purchasePriority,
        priceStrategy: createForm.priceStrategy,
        maxPrice: createForm.maxPrice,
        enableBeforestartScript: createForm.enableBeforestartScript,
        enableAfterstartScript: createForm.enableAfterstartScript,
        imgId: createForm.imgId,
        osImageId: createForm.osImageId,
        groupVmType: createForm.groupVmType,
        spotAllocationStrategy: createForm.spotAllocationStrategy,
        regularAllocationStrategy: createForm.regularAllocationStrategy,
        cpuMemoryRadio: createForm.cpuMemoryRadio,
      }
    ],
    clusterCfgs: createForm.clusterCfgs,
    confGroupElasticScalingData: {
      groupName: createForm.groupName,
      vmRole: 'Task',
      clusterId: clusterId.value,
      maxCount: createForm.confGroupElasticScalingData.maxCount,
      minCount: createForm.confGroupElasticScalingData.minCount,
      scaleinWaitingTime: createForm.confGroupElasticScalingData.scaleinWaitingTime,
      isGracefulScalein: createForm.confGroupElasticScalingData.isGracefulScalein,
      isFullCustody: createForm.confGroupElasticScalingData.isFullCustody,
      enableAfterstartScript: createForm.confGroupElasticScalingData.enableAfterstartScript,
      enableBeforestartScript: createForm.confGroupElasticScalingData.enableBeforestartScript,
      scalingRules: createForm.confGroupElasticScalingData.isFullCustody===0?createForm.confGroupElasticScalingData.scalingRules:[],
    }
  }).then(res => {
    if (res.result == true) {
      newDialogVisible.value = false
      ElMessage.success("添加成功！")
      getVmOverview();
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  }).finally(() => {
    pageLoading.value = false
  })
}, 500)

function nextStep() {
  Ref_newForm.value.validate((valid, fields) => {
    if (valid) {
      stepIndex.value = 1
    } else {
      console.log('error submit!', fields)
    }
  })
}

const editForm = reactive({
  editGroup: {},
  hopeCount: 1,

  krCount: 1,
  enableBeforestartScript: 0,
  enableAfterstartScript: 0,
  provisionType: '',

  srCount: 1,
  isGracefulScalein: 0,
  scaleinWaitingtime: 0,
  forceScaleinDataNode: 0,

  vmskuName: '',
  dataVolumeCount: 1,
  maxDataDiskCount: 9,
})

const editFormRules = {
  krCount: FormCheck.required('请输入扩容数量增加至多少台'),
  enableBeforestartScript: FormCheck.required('请选择是否执行集群启动前脚本'),
  enableAfterstartScript: FormCheck.required('请选择是否执行集群启动后脚本'),

  srCount: FormCheck.required('请输入缩容数量减少至多少台'),
  isGracefulScalein: FormCheck.required('请选择是否优雅缩容'),
  forceScaleinDataNode: FormCheck.required('请选择是否暴力缩容'),
  scaleinWaitingtime: [FormCheck.required('请输入等待时间'), FormCheck.justPositiveInt(), FormCheck.valueIn(60, 1800)],

  provisionType: FormCheck.required('请选择是否启用物理机反亲和'),
}

let diskExpandDialogVisible = ref(false)
let cleanAmbariExpandDialogVisible = ref(false)
let pv2diskExpandDialogVisible = ref(false)
let cleanAmbariStartDate = ref(null)
const Ref_diskExpandForm = ref(null)
const diskExpandForm = reactive({
  editGroup: {},
  // hopeSize: 0,
  dataVolumeSize: 0,
  dataVolumeCount: 0,
  newDataDiskIOPSReadWrite:0,
  newDataDiskMBpsReadWrite:0,
})
const diskExpandFormRules = {
  dataVolumeSize: {
    validator: (rule, value, callback) => {

      let reg = /^[1-9][0-9]*$/;
      if (!reg.test(diskExpandForm.dataVolumeSize)) {
        return callback(new Error("数据盘大小仅支持正整数"));
      }

      if (diskExpandForm.dataVolumeSize < 200) {
        return callback(new Error("数据盘不能小于200G！"));
      } else if (diskExpandForm.dataVolumeSize > 4096) {
        diskExpandForm.dataVolumeSize = 4096
        return callback(new Error("数据盘不能大于4096G，已修改为4096G！"));
      }

      if (diskExpandForm.dataVolumeSize < diskExpandForm.editGroup.dataVolumeSize) {
        return callback(new Error("新数据盘大小不能小于原大小！"));
      }

      return callback();
    },
    trigger: 'blur',
    required: true
  },
  newDataDiskIOPSReadWrite: {
    validator: (rule, value, callback) => {

      let reg = /^[1-9][0-9]*$/;
      if (!reg.test(diskExpandForm.newDataDiskIOPSReadWrite)) {
        return callback(new Error("IOPS大小仅支持正整数"));
      }

      if (diskExpandForm.newDataDiskIOPSReadWrite<3000 || diskExpandForm.newDataDiskIOPSReadWrite>80000) {
        return callback(new Error("IOPS大小范围:3000-80000"));
      }

      return callback();
    },
    trigger: 'change',
    required: true
  },
  newDataDiskMBpsReadWrite: {
    validator: (rule, value, callback) => {

      let reg = /^[1-9][0-9]*$/;
      if (!reg.test(diskExpandForm.newDataDiskMBpsReadWrite)) {
        return callback(new Error("吞吐量大小仅支持正整数"));
      }
      if (diskExpandForm.newDataDiskMBpsReadWrite<125 || diskExpandForm.newDataDiskMBpsReadWrite>1200) {
        return callback(new Error("吞吐量大小范围:125-1200"));
      }
      return callback();
    },
    trigger: 'change',
    required: true
  }
}

function diskExpandEvent(item) {
  // getScaleCountInQueue({
  //   clusterId: clusterId.value,
  //   groupName: item.groupName,
  //   vmRole: item.vmRole,
  //   scalingType: 3
  // }).then(hCount => {
  diskExpandForm.editGroup = item
  // diskExpandForm.hopeSize = item.dataVolumeSize
  diskExpandForm.dataVolumeSize = item.dataVolumeSize
  diskExpandForm.dataVolumeCount = item.dataVolumeCount

  diskExpandDialogVisible.value = true
  // })
}

const diskExpandSaveEvent = throttle(function () {
  Ref_diskExpandForm.value.validate((valid, fields) => {
    if (valid) {
      pageLoading.value = true
      clusterApi.growpart({
        instanceGroupSkuCfgs: [{
          clusterId: clusterId.value,
          vmRole: diskExpandForm.editGroup.vmRole,
          groupName: diskExpandForm.editGroup.groupName,
          dataVolumeSize: diskExpandForm.dataVolumeSize,
          dataVolumeCount: diskExpandForm.dataVolumeCount
        }]
      }).then(res => {
        if (res.result == true) {
          ElMessage.success('磁盘扩容成功！')
          diskExpandDialogVisible.value = false
        } else {
          ElMessage.error(res.errorMsg)
          console.log(res)
        }
      }).finally(() => {
        pageLoading.value = false
      })
    } else {
      console.log('error submit!', fields)
    }
  })
}, 500)

// let bgDialogVisible = ref(false)
//
// function bgEvent(item) {
//   getVmskuList()
//   getOsDiskTypeList()
//   bgDialogVisible.value = true
// }
//
// function bgSaveEvent() {
//   bgDialogVisible.value = false
// }

let expandDialogVisible = ref(false)
const Ref_krForm = ref(null)

function getScaleCountInQueue(params) {
  return new Promise((resolve, reject) => {
    clusterApi.getScaleCountInQueue(params).then(res => {
      if (res.result == true) {
        resolve(res.data || {})
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    }).catch(err => {
      reject();
    })
  })
}

function expandEvent(item) {

  pageLoading.value = true
  getScaleCountInQueue({
    clusterId: clusterId.value,
    groupName: item.groupName,
    vmRole: item.vmRole,
    scalingType: 2
  }).then(countData => {
    let curCount = countData.insCount || 0
    let hopeCount = countData.expectCount || 0

    item.vmCountByRole = curCount

    editForm.editGroup = item

    editForm.hopeCount = hopeCount
    if (editForm.editGroup.purchaseType == '2') {
      editForm.krCount = editForm.hopeCount + 1
    } else {
      editForm.krCount = item.vmCountByRole + 1
    }
    editForm.enableBeforestartScript = 1
    editForm.enableAfterstartScript = 1
    editForm.provisionType = ''

    expandDialogVisible.value = true
  }).finally(() => {
    pageLoading.value = false
  })
}

function scaleoutEvent(params) {
  pageLoading.value = true
  clusterApi.scaleout(params).then(res => {
    if (res.result == true) {

      expandDialogVisible.value = false
      getVmOverview()
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  }).finally(() => {
    pageLoading.value = false
  })
}

const expandSaveEvent = throttle(function () {
  Ref_krForm.value.validate((valid, fields) => {
    if (valid) {
      let params = {
        clusterId: clusterId.value,
        vmRole: editForm.editGroup.vmRole,
        groupName: editForm.editGroup.groupName,
        scaleOutCount: editForm.krCount - editForm.hopeCount,
        expectCount: editForm.krCount,
        enableBeforestartScript: editForm.enableBeforestartScript,
        enableAfterstartScript: editForm.enableAfterstartScript,
        provisionType: editForm.provisionType,
      }
      if (editForm.krCount > 200) {
        ElMessageBox.confirm(`ganglia 仅能监控200台实例，扩容后集群规模为 ${editForm.krCount} 台，超出200台的部分将无法被监控。`, '扩容提醒', {
          confirmButtonText: '仍要扩容',
          cancelButtonText: '取消',
          type: 'warning',
        }).then(() => {
          scaleoutEvent(params);
        })
      } else {
        ElMessageBox.confirm(`确定要将实例组 ${editForm.editGroup.groupName} 扩容到 ${editForm.krCount} 台吗？`, '扩容实例组', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }).then(() => {
          scaleoutEvent(params);
        })
      }
    } else {
      console.log('error submit!', fields)
    }
  })
}, 500)

let reduceDialogVisible = ref(false)
const Ref_srForm = ref(null)

function reduceEvent(item) {
  pageLoading.value = true
  getScaleCountInQueue({
    clusterId: clusterId.value,
    groupName: item.groupName,
    vmRole: item.vmRole,
    scalingType: 1
  }).then(countData => {
    let curCount = countData.insCount || 0
    let hopeCount = countData.expectCount || 0

    item.vmCountByRole = curCount

    editForm.editGroup = item

    editForm.hopeCount = hopeCount
    if (editForm.editGroup.purchaseType == '2') {
      editForm.srCount = editForm.hopeCount - 1
    } else {
      editForm.srCount = item.vmCountByRole - 1
    }
    if (editForm.srCount < 0) {
      editForm.srCount = 0
    }
    editForm.isGracefulScalein = 1
    editForm.scaleinWaitingtime = 180
    editForm.forceScaleinDataNode = 0

    reduceDialogVisible.value = true
  }).finally(() => {
    pageLoading.value = false
  })
}

function srMinCount() {
  if (editForm.editGroup.vmRole.toLowerCase() == 'core') {
    if (clusterData.value.scene.toLowerCase() == 'hbase') {
      return 3
    }
    return 2
  }
  return 0
}

function srMaxCount() {
  let num = 0
  if (editForm.hopeCount - 1 > 0) {
    num = editForm.hopeCount - 1
  }
  if (num < srMinCount()) {
    num = srMinCount()
  }
  return num
}

const reduceSaveEvent = throttle(function () {
  Ref_srForm.value.validate((valid, fields) => {
    if (valid) {
      ElMessageBox.confirm(`确定要将实例组 ${editForm.editGroup.groupName} 缩容到 ${editForm.srCount} 台吗？`, '缩容实例组', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        pageLoading.value = true
        clusterApi.scalein({
          clusterId: clusterId.value,
          vmRole: editForm.editGroup.vmRole,
          groupName: editForm.editGroup.groupName,
          scaleInCount: editForm.hopeCount - editForm.srCount,
          expectCount: editForm.srCount,
          isGracefulScalein: editForm.isGracefulScalein,
          forceScaleinDataNode: editForm.forceScaleinDataNode,
          scaleinWaitingtime: editForm.scaleinWaitingtime
        }).then(res => {
          if (res.result == true) {

            reduceDialogVisible.value = false
            getVmOverview()
          } else {
            ElMessage.error(res.errorMsg)
            console.log(res)
          }
        }).finally(() => {
          pageLoading.value = false
        })
      })
    } else {
      console.log('error submit!', fields)
    }
  })
}, 500)

let ruleConfDialogVisible = ref(false)
const editGroupData = ref({})
const newGroupConf = ref({})

// const ruleConfData = reactive({
//   editGroup: {},
//   confData: {},
//   expandRules: [],
//   reduceRules: [],
// })

function ruleConfEvent(item) {
  editGroupData.value = item
  newGroupConf.value = {}
  ruleConfDialogVisible.value = true
}

function configScaleRuleClosed() {
  getVmOverview();
}

// let limitDialogVisible = ref(false)
// const limitForm = reactive({
//   minCount: '',
//   maxCount: '',
// })
// const limitFormRules = {
//   maxCount: [FormCheck.required('请输入最大节点数'), FormCheck.justPositiveInt(), FormCheck.valueIn(1, 1999)],
//   minCount: [FormCheck.required('请输入最小节点数'), FormCheck.justPositiveInt(), FormCheck.valueIn(1, 1999)],
// }
// const Ref_limitForm = ref(null)
//
// function resetLimit() {
//   limitForm.minCount = ruleConfData.confData.minCount
//   limitForm.maxCount = ruleConfData.confData.maxCount
//
//   limitDialogVisible.value = true
// }
//
// const limitSaveEvent = throttle(function () {
//   Ref_limitForm.value.validate((valid, fields) => {
//     if (valid) {
//       if (ruleConfData.confData.dataType == 'local') {
//         ruleConfData.confData.minCount = limitForm.minCount
//         ruleConfData.confData.maxCount = limitForm.maxCount
//
//         limitDialogVisible.value = false
//       } else {
//         pageLoading.value = true
//         clusterApi.updateGroupElasticScaling({
//           groupEsId: ruleConfData.confData.groupEsId,
//           clusterId: clusterId.value,
//           minCount: limitForm.minCount,
//           maxCount: limitForm.maxCount,
//         }).then(res => {
//           if (res.result == true) {
//
//             limitDialogVisible.value = false
//             ruleConfEvent(ruleConfData.editGroup)
//           } else {
//             ElMessage.error(res.errorMsg)
//           }
//         }).finally(() => {
//           pageLoading.value = false
//         })
//       }
//     } else {
//       console.log('error submit!', fields)
//     }
//   })
// }, 500)
//
// let ruleDialogVisible = ref(false)
// const Ref_ruleForm = ref(null)
//
// const ruleForm = reactive({
//   esRuleId: '',
//   scalingType: 1,
//   esRuleName: '',
//   perSalingCout: '',
//   loadMetric: '',
//   windowSize: '',
//   aggregateType: '',
//   operator: '',
//   threshold: '',
//   repeatCount: '',
//   freezingTime: '',
//   enableBeforestartScript: 0,
//   enableAfterstartScript: 0,
//   isGracefulScalein: 0,
//   scaleinWaitingtime: 0,
// })
// const ruleFormRules = {
//   esRuleName: FormCheck.required('请输入规则名称'),
//   perSalingCout: [FormCheck.required('请输入伸缩数量'), FormCheck.valueIn(1, 1999), FormCheck.justPositiveInt()],
//   loadMetric: FormCheck.required('请选择集群负载指标'),
//   windowSize: FormCheck.required('请选择统计周期'),
//   repeatCount: FormCheck.required('请选择统计周期个数'),
//   freezingTime: FormCheck.required('请输入集群冷却时间'),
//   enableBeforestartScript: FormCheck.required('请选择'),
//   enableAfterstartScript: FormCheck.required('请选择'),
//   statisticalRules: checkStatisticalRules(),
//   isGracefulScalein: FormCheck.required('请选择是否优雅缩容'),
//   scaleinWaitingtime: [FormCheck.required('请输入等待时间'), FormCheck.justPositiveInt(), FormCheck.valueIn(60, 1800)],
// }
//
// function checkStatisticalRules() {
//   return {
//     validator: (rule, value, callback) => {
//       if (!ruleForm.aggregateType || !ruleForm.operator || ruleForm.threshold === '') {
//         return callback(new Error('请完善统计规则！'))
//       }
//       let v = ruleForm.threshold;
//       let reg = /^[1-9][0-9]*$/;
//       if (!reg.test(v)) {
//         return callback(new Error('请输入正整数'));
//       }
//       let val = parseFloat(v)
//       if (val < 1 || val > 100) {
//         return callback(new Error('请输入1-100中的正整数！'))
//       }
//       callback()
//     },
//     trigger: 'blur',
//     required: true
//   }
// }
//
// function addExpandRule() {
//   ruleForm.esRuleId = ''
//   ruleForm.scalingType = 1
//   ruleForm.esRuleName = ''
//   ruleForm.perSalingCout = 10
//   ruleForm.loadMetric = 'MemoryAvailablePrecentage'
//   ruleForm.windowSize = '5'
//   ruleForm.aggregateType = 'avg'
//   ruleForm.operator = '<='
//   ruleForm.threshold = 20
//   ruleForm.repeatCount = '1'
//   ruleForm.freezingTime = 120
//   ruleForm.enableBeforestartScript = 1
//   ruleForm.enableAfterstartScript = 1
//   ruleForm.isGracefulScalein = 0
//   ruleForm.scaleinWaitingtime = ''
//
//   ruleDialogVisible.value = true
// }
//
// function addReduceRule() {
//   ruleForm.esRuleId = ''
//   ruleForm.scalingType = 0
//   ruleForm.esRuleName = ''
//   ruleForm.perSalingCout = 3
//   ruleForm.loadMetric = 'MemoryAvailablePrecentage'
//   ruleForm.windowSize = '5'
//   ruleForm.aggregateType = 'avg'
//   ruleForm.operator = '>='
//   ruleForm.threshold = 80
//   ruleForm.repeatCount = '1'
//   ruleForm.freezingTime = 300
//   ruleForm.enableBeforestartScript = 0
//   ruleForm.enableAfterstartScript = 0
//   ruleForm.isGracefulScalein = 1
//   ruleForm.scaleinWaitingtime = 1800
//
//   ruleDialogVisible.value = true
// }
//
// function addRuleSave() {
//   Ref_ruleForm.value.validate((valid, fields) => {
//     if (valid) {
//       let ruleItem = {
//         esRuleId: ruleForm.esRuleId,
//         scalingType: ruleForm.scalingType,
//         esRuleName: ruleForm.esRuleName,
//         perSalingCout: ruleForm.perSalingCout,
//         loadMetric: ruleForm.loadMetric,
//         windowSize: ruleForm.windowSize,
//         aggregateType: ruleForm.aggregateType,
//         operator: ruleForm.operator,
//         threshold: ruleForm.threshold,
//         repeatCount: ruleForm.repeatCount,
//         freezingTime: ruleForm.freezingTime,
//         enableBeforestartScript: ruleForm.enableBeforestartScript,
//         enableAfterstartScript: ruleForm.enableAfterstartScript,
//         isGracefulScalein: ruleForm.isGracefulScalein,
//         scaleinWaitingtime: ruleForm.scaleinWaitingtime,
//       }
//       if (ruleConfData.confData.dataType == 'local') {
//         if (!ruleItem.esRuleId) {
//           ruleItem.esRuleId = new Date().getTime();
//           ruleConfData.confData.scalingRules.push(ruleItem)
//           ElMessage.success("添加成功！")
//         } else {
//           debugger
//           let idx = ruleConfData.confData.scalingRules.findIndex(itm => {
//             return itm.esRuleId == ruleItem.esRuleId
//           })
//           ruleConfData.confData.scalingRules.splice(idx, 1, ruleItem)
//           ElMessage.success("更新成功！")
//         }
//         ruleDialogVisible.value = false
//
//         initLoaclScalingRules();
//
//         return ;
//       }
//       let params = {
//         groupName: ruleConfData.editGroup.groupName,
//         vmRole: ruleConfData.editGroup.vmRole,
//         clusterId: clusterId.value,
//         scalingRules: [
//           ruleItem
//         ]
//       }
//       pageLoading.value = true
//       if (ruleForm.esRuleId) {
//         clusterApi.updateElasticScalingRule(params).then(res => {
//           if (res.result == true) {
//             ruleDialogVisible.value = false
//             ruleConfEvent(ruleConfData.editGroup)
//             ElMessage.success("更新成功！")
//           } else {
//             ElMessage.error(res.errorMsg)
//           }
//         }).finally(() => {
//           pageLoading.value = false
//         })
//       } else {
//         clusterApi.postElasticScalingRule(params).then(res => {
//           if (res.result == true) {
//             ruleDialogVisible.value = false
//             ruleConfEvent(ruleConfData.editGroup)
//             ElMessage.success("添加成功！")
//           } else {
//             ElMessage.error(res.errorMsg)
//           }
//         }).finally(() => {
//           pageLoading.value = false
//         })
//       }
//     } else {
//       console.log('error submit!', fields)
//     }
//   })
// }
//
// function deleteElasticScalingRule(item) {
//   ElMessageBox.confirm('您确定需要删除该项规则吗？', '提示', {
//     confirmButtonText: '确定',
//     cancelButtonText: '取消',
//     type: 'warning',
//   }).then(() => {
//
//     if (ruleConfData.confData.dataType == 'local') {
//       ElMessage.success("删除成功！")
//       let idx = ruleConfData.confData.scalingRules.findIndex(itm => {
//         return itm.esRuleId == item.esRuleId
//       })
//       ruleConfData.confData.scalingRules.splice(idx, 1)
//
//       initLoaclScalingRules()
//       return ;
//     }
//
//     pageLoading.value = true
//     clusterApi.deleteElasticScalingRule({
//       groupEsId: ruleConfData.confData.groupEsId,
//       scalingRules: [
//         {
//           esRuleId: item.esRuleId
//         }
//       ]
//     }).then(res => {
//       if (res.result == true) {
//         ElMessage.success("删除成功！")
//         ruleConfEvent(ruleConfData.editGroup)
//       } else {
//         ElMessage.error(res.errorMsg)
//       }
//     }).finally(() => {
//       pageLoading.value = false
//     })
//   })
// }
//
// let ruleDetailDialogVisible = ref(false)
//
// function detailRuleEvent(item) {
//   ruleForm.esRuleId = item.esRuleId
//   ruleForm.scalingType = item.scalingType
//   ruleForm.esRuleName = item.esRuleName
//   ruleForm.perSalingCout = item.perSalingCout
//   ruleForm.loadMetric = item.loadMetric
//   ruleForm.windowSize = item.windowSize
//   ruleForm.aggregateType = item.aggregateType
//   ruleForm.operator = item.operator
//   ruleForm.threshold = item.threshold
//   ruleForm.repeatCount = item.repeatCount
//   ruleForm.freezingTime = item.freezingTime
//   ruleForm.enableBeforestartScript = item.enableBeforestartScript || 0
//   ruleForm.enableAfterstartScript = item.enableAfterstartScript || 0
//   ruleForm.isGracefulScalein = item.isGracefulScalein || 0
//   ruleForm.scaleinWaitingtime = item.scaleinWaitingtime || ''
//
//   ruleDetailDialogVisible.value = true
// }
//
// function editRuleEvent(item) {
//   ruleForm.esRuleId = item.esRuleId
//   ruleForm.scalingType = item.scalingType
//   ruleForm.esRuleName = item.esRuleName
//   ruleForm.perSalingCout = item.perSalingCout
//   ruleForm.loadMetric = item.loadMetric
//   ruleForm.windowSize = item.windowSize
//   ruleForm.aggregateType = item.aggregateType
//   ruleForm.operator = item.operator
//   ruleForm.threshold = item.threshold
//   ruleForm.repeatCount = item.repeatCount
//   ruleForm.freezingTime = item.freezingTime
//   ruleForm.enableBeforestartScript = item.enableBeforestartScript || 0
//   ruleForm.enableAfterstartScript = item.enableAfterstartScript || 0
//   ruleForm.isGracefulScalein = item.isGracefulScalein || 0
//   ruleForm.scaleinWaitingtime = item.scaleinWaitingtime || ''
//
//   ruleDetailDialogVisible.value = false
//
//   ruleDialogVisible.value = true
// }

let historyDialogVisible = ref(false)
const historyGroupName = ref('')

const activeName = ref('taskList')

const taskLogLoaded = ref(false)

function showHistoryEvent(item) {
  activeName.value = 'taskList'
  taskLogLoaded.value = false
  historyGroupName.value = item.groupName
  historyDialogVisible.value = true
}

function tabChange() {
  if (activeName.value == 'taskLog') {
    taskLogLoaded.value = true
  }
}

let vmsDialogVisible = ref(false)
const taskInfoData = ref(null)
const taskInfoVms = ref(null)

function showVms(item) {
  clusterApi.getTaskInfo({
    taskId: item.taskId
  }).then(res => {
    if (res.result == true) {
      let d = res.data || {}
      taskInfoData.value = d.task || {}
      taskInfoVms.value = d.vms || []

      vmsDialogVisible.value = true
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  })
}

let taskDetailDialogVisible = ref(false)
const detailData = ref({})
let taskDetailTimer = null

function showTaskDetail(item, showLoading = true) {
  if (showLoading) {
    pageLoading.value = true
  }
  taskCenterApi.getjobdetail({taskId: item.taskId}).then(res => {
    if (res.result == true) {
      detailData.value = res.data
      if (showLoading) {
        taskDetailDialogVisible.value = true
      }

      if (taskDetailDialogVisible.value) { // 只有打开状态才轮询，防止关闭的时候正在发生请求
        let activityInfos = detailData.value.activityInfos || []
        let allFinished = true
        for (let i = 0; i < activityInfos.length; i++) {
          if (activityInfos[i].endTime == '-') {
            allFinished = false
            break;
          }
        }
        if (!allFinished) {
          if (taskDetailTimer) {
            clearTimeout(taskDetailTimer)
          }
          taskDetailTimer = setTimeout(() => {
            showTaskDetail(item, false)
          }, 10 * 1000)
        }
      }
    } else {
      ElMessage.error(res.errorMsg)
    }
  }).finally(() => {
    pageLoading.value = false
  })
}

function taskDetailClosed() {
  console.log("taskListClosed")
  if (taskDetailTimer) {
    clearTimeout(taskDetailTimer)
    taskDetailTimer = null
  }
}

onBeforeUnmount(() => {
  taskDetailClosed()
})

const deleteVms = throttle(function () {
  ElMessageBox.confirm(`您确定要删除相关实例吗？`, '删除相关实例', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    pageLoading.value = true
    clusterApi.deleteScaleOutTaskVms({
      taskId: taskInfoData.value.taskId
    }).then(res => {
      if (res.result == true) {
        ElMessage.success('删除成功！')

        vmsDialogVisible.value = false
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    }).finally(() => {
      pageLoading.value = false
    })
  })
}, 500)

const operationTypeOptions = [
  {"label": "全部", "value": '', args: {scalingType: '', opertionType: ''}},
  {"label": "手动扩容", "value": '1', args: {scalingType: '1', opertionType: '1'}},
  {"label": "手动缩容", "value": '2', args: {scalingType: '2', opertionType: '1'}},
  {"label": "弹性扩容", "value": '3', args: {scalingType: '1', opertionType: '2'}},
  {"label": "弹性缩容", "value": '4', args: {scalingType: '2', opertionType: '2'}},
  {"label": "手动创建", "value": '5', args: {scalingType: '1', opertionType: '5'}},
  {"label": "手动删除", "value": '6', args: {scalingType: '2', opertionType: '6'}},
  {"label": "删除扩容实例", "value": '7', args: {scalingType: '2', opertionType: '8'}},
  {"label": "竞价扩容", "value": '8', args: {scalingType: '1', opertionType: '7'}},
  {"label": "竞价缩容", "value": '9', args: {scalingType: '2', opertionType: '7'}},
  {"label": "补全驱逐VM", "value": '10', args: {scalingType: '1', opertionType: '9'}},
]

function operationTypeToStr(row) {
  let option = operationTypeOptions.find(item => {
    return item.args.scalingType == row.scalingType && item.args.opertionType == row.operatiionType
  }) || {}
  return option.label || ''
}

const vmStateOptions = [
  {"label": "任务创建", "value": '0'},
  {"label": "任务执行中", "value": '1'},
  {"label": "任务完成", "value": '2'},
  {"label": "任务失败", "value": '-9'},
]

function vmStateToStr(state) {
  let option = vmStateOptions.find(item => {
    return item.value == state
  }) || {}
  return option.label || state
}

function deleteGroup(item) {
  pageLoading.value = true
  clusterApi.deleteGroup({
    clusterId: clusterId.value,
    groupName: item.groupName,
    vmRole: item.vmRole,
  }).then(res => {
    if (res.result == true) {
      ElMessage.success("删除成功！")
      getVmOverview();
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  }).finally(() => {
    pageLoading.value = false
  })
}

function changeSpotState(row) {
  var buyState = row.spotBuyState;
  var destoryState = row.spotDestoryState;
  if (!buyState && !destoryState) {
    row.spotState = 0;
  } else if (!buyState && destoryState) {
    row.spotState = 1;
  } else if (buyState && !destoryState) {
    row.spotState = 2;
  } else if (buyState && destoryState) {
    row.spotState = 3;
  }

  // 请求接口改变状态
  clusterApi.updateSpotState({
    clusterId: clusterId.value,
    groupName: row.groupName,
    spotState: row.spotState,
  }).then(res => {
    if (res.result == true) {
      ElMessage.success("更新成功")
      getVmOverview();
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  }).finally(() => {
    pageLoading.value = false
  })
}

function toAmbari() {
  emit('toAmbari')
}
</script>

<style lang="stylus" scoped type="text/stylus">
.vm-group {
  height 100%;
  overflow-y auto;

  .functions-row {
    margin-top 12px;
  }

  .list-data {
    padding 12px 0;

    .operations-div {
      .el-button {
        padding-left 0;
        padding-right 0;
      }
    }
  }

  .dialog-content {
    overflow-y auto;

    .el-form-item {
      //margin-bottom 30px;
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

    .block-div {
      padding 10px 10px 8px;
      border 1px solid #ddd;

      .title {
        font-size 16px;
        padding 0px 0 5px;
        color black;
      }

      .limit-items {
        display flex;
        align-items center;
        justify-content space-between;

        .limit-item {
          display flex;
          align-items center;

          .number {
            color red;
          }
        }
      }

      .rule-div {
        padding-bottom 5px;

        .rule-top-row {
          display flex;
          align-items center;
          justify-content space-between;
          border-top 1px solid #ddd;
          margin-top 8px;
          padding-top 5px;

          .rule-type {
            font-size 14px;
            color black;
          }
        }

        .no-rule {
          text-align center;
          padding 20px;
          color #999;
        }

        .rules-table {
          margin-top 3px;
        }
      }
    }

    .detail-items {

      .flex-row {
        display flex;
        align-items flex-start;
        padding-bottom 16px;

        .detail-item {
          flex 1;
          display flex;
          align-items flex-start;
          overflow hidden;

          .label {
            color #606266;
            text-align left;
            width 180px;
            font-size 14px;
            line-height 1.4;
            padding-left 20px;
          }

          .value {
            flex 1;
            font-size 14px;
            line-height 1.4;
            overflow hidden;
            word-break break-all;
            padding-right 15px;
          }
        }
      }
    }
  }

  .rules-div {
    margin-top 10px;
    margin-bottom 20px;
  }

  .operations-div {
    .el-button {
      padding-left 0;
      padding-right 0;
    }
  }
}

.rule-dialog {

  .dialog-content {
    max-height 60vh;

    .el-form-item {
      margin-bottom 18px;

      .input-width {
        width 250px;
      }
    }

    .from-item-tip {
      width 100%;
      line-height 1;
      font-size 12px;
      height 17px;
      padding-top 5px;
      color #8f8f8f;
    }
  }
}
.date-picker{
  padding: 30px 0;
  text-align: center;
  span {
    margin 0 10px;
  }
}
</style>

<style lang="stylus" type="text/stylus">
.vm-history-dialog {
  .el-dialog__body {
    padding-top 0;
    padding-bottom 10px;

    .full-tabs {
      width 100%;
      height 100%;

      .el-tabs__header {
        margin-bottom 0;
      }

      .el-tabs__content {
        height calc(100% - 40px);

        .el-tab-pane {
          height 100%;

          .scroll-div {
            height 100%;
            overflow-y auto;
          }
        }
      }
    }
  }
}

.vm-group-vms-list {
  display flex;
  flex-direction column;

  .flex-row {
    display flex;
    align-items flex-start;
    padding-bottom 16px;

    .detail-item {
      flex 1;
      display flex;
      align-items flex-start;
      overflow hidden;

      .label {
        color #606266;
        text-align left;
        width 110px;
        font-size 14px;
        line-height 1.4;
      }

      .value {
        flex 1;
        font-size 14px;
        line-height 1.4;
        overflow hidden;
        word-break break-all;
        padding-right 15px;
      }
    }
  }

  .top-box {
    border 1px solid #ddd;
    padding-top 15px;
    padding-left 10px;
    margin-bottom 10px;

    .label {
      width 120px !important;
    }
  }

  .theader {
    td, th {
      background-color: #F8F8F8 !important;

      .cell {
        word-break break-word;
      }
    }
  }
}
</style>
