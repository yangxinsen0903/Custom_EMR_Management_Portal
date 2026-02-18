/**Created by liaoyingchao on 2023/2/27.*/

<template>
  <div class="elastic-scaling" v-loading="pageLoading">
    <div class="block-div">
      <div class="title">实例数限制</div>
      <div class="limit-items">
        <div class="limit-item">最大实例数：<span class="number">{{ ruleConfData.confData.maxCount }}</span></div>
        <div class="limit-item">最小实例数：<span class="number">{{ ruleConfData.confData.minCount }}</span></div>
        <div>
          <el-button type="primary" text @click="resetLimit">修改限制</el-button>
        </div>
      </div>
    </div>
    <div class="block-div rules-div">
      <div style="display: flex;align-items: center;justify-content: space-between;">
        <div class="title">触发规则</div>
<!--        <div>冷却结束时间：{{ coolingTimeFormat(ruleConfData.confData.freezingEndTime) }}</div>-->
        <div style="display: flex;align-items: center;">
          <div style="margin-right: 10px;">托管式弹性扩缩容</div>
          <el-switch
              v-model="ruleConfData.confData.isFullCustody"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="开启"
              inactive-text="关闭"
              @click="isFullCustodyChenged"
          />
          <!-- 设置全托管弹性扩缩容参数  -->
          <el-button type="primary" text
                     @click="fullCustodyConfigDialogVisible = true"
                     v-show="isShowCustodyButton()">配置托管参数
          </el-button>
        </div>
      </div>
      <div class="rule-div" v-if="ruleConfData.confData.isFullCustody == 1">
        <div class="rule-top-row">
          <div class="rule-type">托管式弹性扩缩容规则</div>
        </div>
        <div class="rules-table">
          <div style="display: flex;line-height: 32px;">
            <div style="flex: 1">
              <span>执行集群启动前脚本：</span>
              <span>{{ ruleConfData.confData.enableBeforestartScript ? '是' : '否' }}</span>
            </div>
            <div style="flex: 1">
              <span>执行集群启动后脚本：</span>
              <span>{{ ruleConfData.confData.enableAfterstartScript ? '是' : '否' }}</span>
            </div>
          </div>
          <div style="display: flex;line-height: 32px;">
            <div style="flex: 1">
              <span>优雅缩容：</span>
              <span>{{ ruleConfData.confData.isGracefulScalein ? '是' : '否' }}</span>
            </div>
            <div style="flex: 1">
              <span>等待时间：</span>
              <span>{{ ruleConfData.confData.scaleinWaitingTime || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="rule-div" v-if="ruleConfData.confData.isFullCustody == 0">
        <div class="rule-top-row">
          <div class="rule-type">扩容规则</div>
          <div>
            <el-button type="primary" text @click="addExpandRule">添加规则</el-button>
          </div>
        </div>
        <div class="rules-table">
          <el-table :data="ruleConfData.expandRules" header-row-class-name="theader" border style="width: 100%">
            <el-table-column prop="esRuleName" label="规则名称" min-width="100"></el-table-column>
            <el-table-column prop="loadMetric" label="集群负载指标" min-width="100">
              <template #default="scope">
                {{ loadMetricToStr(scope.row.loadMetric) }}
              </template>
            </el-table-column>
            <el-table-column label="统计规则" min-width="80">
              <template #default="scope">
                {{ aggregateTypeToStr(scope.row.aggregateType) }} {{ scope.row.operator }} {{ scope.row.threshold }}
              </template>
            </el-table-column>
            <el-table-column prop="lastComputedTime" label="最后检查时间" :formatter="columnTimeFormat"
                             width="170"></el-table-column>
            <el-table-column prop="lastExecuteTime" label="最近一次执行时间" :formatter="columnTimeFormat"
                             width="170"></el-table-column>
            <el-table-column prop="operations" label="管理" width="150">
              <template #default="scope">
                <div class="operations-div">
                  <el-button type="primary" text @click="detailRuleEvent(scope.row)">详情</el-button>
                  <el-button type="primary" text @click="editRuleEvent(scope.row)">编辑</el-button>
                  <el-button type="primary" text @click="deleteElasticScalingRule(scope.row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <div class="rule-div" v-if="ruleConfData.confData.isFullCustody == 0">
        <div class="rule-top-row">
          <div class="rule-type">缩容规则</div>
          <div>
            <el-button type="primary" text @click="addReduceRule">添加规则</el-button>
          </div>
        </div>
        <div class="rules-table">
          <el-table :data="ruleConfData.reduceRules" header-row-class-name="theader" border style="width: 100%">
            <el-table-column prop="esRuleName" label="规则名称" min-width="100"></el-table-column>
            <el-table-column prop="loadMetric" label="集群负载指标" min-width="100">
              <template #default="scope">
                {{ loadMetricToStr(scope.row.loadMetric) }}
              </template>
            </el-table-column>
            <el-table-column label="统计规则" min-width="80">
              <template #default="scope">
                {{ aggregateTypeToStr(scope.row.aggregateType) }} {{ scope.row.operator }} {{ scope.row.threshold }}
              </template>
            </el-table-column>
            <el-table-column prop="lastComputedTime" label="最后检查时间" :formatter="columnTimeFormat"
                             width="170"></el-table-column>
            <el-table-column prop="lastExecuteTime" label="最近一次执行时间" :formatter="columnTimeFormat"
                             width="170"></el-table-column>
            <el-table-column prop="operations" label="管理" width="150">
              <template #default="scope">
                <div class="operations-div">
                  <el-button type="primary" text @click="detailRuleEvent(scope.row)">详情</el-button>
                  <el-button type="primary" text @click="editRuleEvent(scope.row)">编辑</el-button>
                  <el-button type="primary" text @click="deleteElasticScalingRule(scope.row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
    <el-dialog
            class="center-dialog"
            v-model="limitDialogVisible"
            title="实例数限制"
            width="500"
            destroy-on-close
            :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <el-form ref="Ref_limitForm" :model="limitForm" :rules="limitFormRules" label-width="180px"
                 style="width: 80%;">
          <el-form-item label="当前最大节点数">
            <div>{{ ruleConfData.confData.maxCount }}</div>
          </el-form-item>
          <el-form-item label="最大节点数修改为" prop="maxCount">
            <el-input v-model="limitForm.maxCount"></el-input>
          </el-form-item>
          <el-form-item label="当前最小节点数">
            <div>{{ ruleConfData.confData.minCount }}</div>
          </el-form-item>
          <el-form-item label="最小节点数修改为" prop="minCount">
            <el-input v-model="limitForm.minCount"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="limitDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="limitSaveEvent">确定</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog
            class="center-dialog rule-dialog"
            v-model="fullCustodyDialogVisible"
            title="托管式弹性扩缩容规则"
            width="700"
            append-to-body
            destroy-on-close
            :show-close="false"
            :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <el-form ref="Ref_fullCustodyForm" :model="fullCustodyForm" :rules="fullCustodyFormRules" label-width="180px"
                 style="width: 80%;">
          <el-form-item label="执行集群启动前脚本：" prop="enableBeforestartScript">
            <el-switch
                v-model="fullCustodyForm.enableBeforestartScript"
                inline-prompt
                active-text="是"
                :active-value="1"
                :inactive-value="0"
                inactive-text="否"
            />
          </el-form-item>
          <el-form-item label="执行集群启动后脚本：" prop="enableAfterstartScript">
            <el-switch
                v-model="fullCustodyForm.enableAfterstartScript"
                inline-prompt
                active-text="是"
                :active-value="1"
                :inactive-value="0"
                inactive-text="否"
            />
          </el-form-item>
          <el-form-item label="优雅缩容：" prop="isGracefulScalein">
            <template #label>
              <div style="display: flex;align-items: center;">
                <span>优雅缩容：</span>
                <el-tooltip effect="light" placement="right">
                  <el-icon><QuestionFilled /></el-icon>
                  <template #content>
                    <div style="max-width: 320px;">您可以设置超时时间，释放YARN上作业所在的节点。如果节点没有运行YARN上的作业或者作业运行超出您设置的超时时间，则释放此节点。超时时间最大值为1800秒。</div>
                  </template>
                </el-tooltip>
              </div>
            </template>
            <el-switch
                v-model="fullCustodyForm.isGracefulScalein"
                inline-prompt
                active-text="是"
                :active-value="1"
                :inactive-value="0"
                inactive-text="否"
                @change="isGracefulScaleinChange"
            />
          </el-form-item>
          <el-form-item label="等待时间：" prop="scaleinWaitingTime" v-if="fullCustodyForm.isGracefulScalein == 1">
            <el-input class="input-width" v-model="fullCustodyForm.scaleinWaitingTime"
                      placeholder="请输入缩容等待时间">
              <template #append>秒</template>
            </el-input>
            <div class="from-item-tip">可选缩容等待时间范围为60-1800秒</div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelFullCustodySaveEvent">取消</el-button>
          <el-button type="primary" @click="fullCustodySaveEvent">确定</el-button>
        </div>
      </template>
    </el-dialog>

<!--  全托管弹性扩缩容自定义配置对话框  -->
    <el-dialog
        class="center-dialog rule-dialog"
        v-model="fullCustodyConfigDialogVisible"
        title="托管式弹性扩缩容自定义配置"
        width="600"
        append-to-body
        destroy-on-close
        :show-close="false"
        :close-on-click-modal="false"
    >
      <div class="dialog-content">

        <el-form ref="Ref_fullCustodyConfigForm" :model="fullCustodyConfigForm" label-width="180px"
                 style="width: 80%;">
          <el-form-item label="是否开启自定义配置" prop="scaleoutMetric">
            <el-switch
                v-model="fullCustodyConfigEnabled"
                inline-prompt
                active-text="开启"
                inactive-text="关闭"
            />
          </el-form-item>
          <el-form-item label="扩容指标：" prop="scaleoutMetric">
            <el-select v-model="fullCustodyConfigForm.scaleoutMetric"
                       :disabled="!fullCustodyConfigEnabled"
                       placeholder="选择监控的指标" style="width: 240px">
              <el-option key="App" label="App" value="App" />
              <el-option key="Container" label="Container" value="Container" />
            </el-select>
          </el-form-item>
          <el-form-item label="缩容内存百分比阈值：" prop="scaleinMemoryThreshold">
            <el-input-number v-model="fullCustodyConfigForm.scaleinMemoryThreshold"
                             :disabled="!fullCustodyConfigEnabled"
                             :min="1" :max="100">
            </el-input-number>%
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelFullCustodyConfigSaveEvent">取消</el-button>
          <el-button type="primary" @click="fullCustodyConfigSaveEvent">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
            class="center-dialog rule-dialog"
            v-model="ruleDialogVisible"
            :title="'弹性' + (ruleForm.scalingType == 1 ? '扩容' : '缩容') + '规则配置'"
            width="700"
            destroy-on-close
            append-to-body
            :close-on-click-modal="false"
    >
      <div class="dialog-content" v-loading="pageLoading">
        <el-form ref="Ref_ruleForm" :model="ruleForm" :rules="ruleFormRules" label-width="180px"
                 style="width: 95%;">
          <el-form-item label="规则名称：" prop="esRuleName">
            <el-input v-model="ruleForm.esRuleName" placeholder="请输入规则名称" class="input-width" maxlength="100"
                      show-word-limit clearable></el-input>
          </el-form-item>
          <el-form-item :label="'单次' + (ruleForm.scalingType == 1 ? '扩容' : '缩容') + '数：'"
                        prop="perSalingCout">
            <el-input v-model="ruleForm.perSalingCout"
                      :placeholder="'请输入单次' + (ruleForm.scalingType == 1 ? '扩容' : '缩容') + '数'"
                      class="input-width">
              <template #append>台</template>
            </el-input>
          </el-form-item>
          <!--<el-form-item label="扩容执行方式">-->
          <!--<el-radio-group v-model="ruleForm.type">-->
          <!--<el-radio-button label="1">按时间执行</el-radio-button>-->
          <!--<el-radio-button label="2">按负载执行</el-radio-button>-->
          <!--</el-radio-group>-->
          <!--</el-form-item>-->
          <template v-if="ruleForm.type == 1">
            <el-form-item label="执行次数">
              <el-radio-group v-model="ruleForm.times">
                <el-radio-button label="1">重复执行</el-radio-button>
                <el-radio-button label="2">只执行一次</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <template v-if="ruleForm.times == 1">
              <el-form-item label="执行时间">
                <el-radio-group v-model="ruleForm.time">
                  <el-radio-button label="1">每日执行</el-radio-button>
                  <el-radio-button label="2">按周几执行</el-radio-button>
                  <el-radio-button label="3">按日执行</el-radio-button>
                  <el-radio-button label="4">连续时间执行</el-radio-button>
                </el-radio-group>
                <div v-if="ruleForm.time == 1">
                  <el-checkbox-group v-model="ruleForm.timeArr">
                    <el-checkbox v-for="hour in 24" :key="hour" :label="hour">{{ hour }}点</el-checkbox>
                  </el-checkbox-group>
                </div>
                <div v-else-if="ruleForm.time == 2">
                  <el-checkbox-group v-model="ruleForm.timeArr">
                    <el-checkbox v-for="day in 7" :key="day" :label="day">周{{ day }}</el-checkbox>
                  </el-checkbox-group>
                </div>
                <div v-else-if="ruleForm.time == 3">
                  <el-checkbox-group v-model="ruleForm.timeArr">
                    <el-checkbox v-for="day in 31" :key="day" :label="day">{{ day }}号</el-checkbox>
                  </el-checkbox-group>
                </div>
              </el-form-item>
              <el-form-item label="执行起止日期">
                <el-date-picker
                        v-model="ruleForm.timeRange"
                        type="datetimerange"
                        range-separator="至"
                        start-placeholder="开始时间"
                        end-placeholder="结束时间"
                />
              </el-form-item>
              <el-form-item label="重试过期时间">
                <el-input v-model="ruleForm.retryTime" class="input-width"
                          placeholder="请输入重试过期时间"></el-input>
              </el-form-item>
            </template>
            <template v-else>
              <el-form-item label="执行时间">
                <el-date-picker
                        v-model="ruleForm.time"
                        type="datetime"
                        placeholder="执行时间"
                />
              </el-form-item>
            </template>
          </template>
          <template v-else>
            <el-form-item label="集群负载指标：" prop="loadMetric">
              <el-select class="input-width" placeholder="请选择集群负载指标" v-model="ruleForm.loadMetric">
                <el-option :label="opt.label" :value="opt.value" :key="opt.value" v-for="opt in loadMetricOptions"/>
              </el-select>
            </el-form-item>
            <el-form-item label="统计窗口期：" prop="windowSize">
              <template #label>
                <div style="display: flex;align-items: center;">
                  <span>统计窗口期：</span>
                  <el-tooltip effect="light" placement="right">
                    <el-icon><QuestionFilled /></el-icon>
                    <template #content>
                      <div style="max-width: 320px;">您选定的集群负载指标将会在一个统计周期内进行采样和计算，并与您设定的统计规则比对。</div>
                    </template>
                  </el-tooltip>
                </div>
              </template>
              <el-select class="input-width" placeholder="请选择统计窗口期" v-model="ruleForm.windowSize">
                <el-option label="5分钟" value="5"/>
                <el-option label="10分钟" value="10"/>
                <el-option label="30分钟" value="30"/>
              </el-select>
            </el-form-item>
            <el-form-item label="统计规则：" prop="statisticalRules">
              <template #label>
                <div style="display: flex;align-items: center;">
                  <span>统计规则：</span>
                  <el-tooltip effect="light" placement="right">
                    <el-icon><QuestionFilled /></el-icon>
                    <template #content>
                      <div style="max-width: 320px;">您选定的集群负载指标在一个统计周期内，按照选定的聚合维度（平均值、最大值和最小值），达到统计阈值记为一次触发。</div>
                    </template>
                  </el-tooltip>
                </div>
              </template>
              <div style="display: flex;">
                <el-select style="flex: 1;margin-right: 10px;" v-model="ruleForm.aggregateType">
                  <el-option :label="opt.label" :value="opt.value" :key="opt.value"
                             v-for="opt in aggregateTypeOptions"/>
                </el-select>
                <el-select style="flex: 1;margin-right: 10px;" v-model="ruleForm.operator">
                  <el-option label=">=" value=">="/>
                  <el-option label=">" value=">"/>
                  <el-option label="<=" value="<="/>
                  <el-option label="<" value="<"/>
                </el-select>
                <el-input style="flex: 2;" v-model="ruleForm.threshold" placeholder="请输入阈值">
                  <template #append>{{loadMetricToUnit(ruleForm.loadMetric)}}</template>
                </el-input>
              </div>
            </el-form-item>
            <el-form-item label="统计周期个数：" prop="repeatCount">
              <template #label>
                <div style="display: flex;align-items: center;">
                  <span>统计周期个数：</span>
                  <el-tooltip effect="light" placement="right">
                    <el-icon><QuestionFilled /></el-icon>
                    <template #content>
                      <div style="max-width: 320px;">负载指标聚合后达到阈值触发的次数，达到该次数后触发集群弹性伸缩的动作。</div>
                    </template>
                  </el-tooltip>
                </div>
              </template>
              <el-select class="input-width" placeholder="请选择统计周期个数" v-model="ruleForm.repeatCount">
                <el-option label="1个" value="1"/>
                <el-option label="2个" value="2"/>
                <el-option label="3个" value="3"/>
                <el-option label="4个" value="4"/>
              </el-select>
            </el-form-item>
            <el-form-item label="集群冷却时间：" prop="freezingTime">
              <template #label>
                <div style="display: flex;align-items: center;">
                  <span>集群冷却时间：</span>
                  <el-tooltip effect="light" placement="right">
                    <el-icon><QuestionFilled /></el-icon>
                    <template #content>
                      <div style="max-width: 320px;">
                        <p>每次弹性伸缩动作执行完成，到可以再次进行弹性伸缩的时间间隔。在冷却时间内，即使满足弹性伸缩条件也不会发生弹性伸缩动作。</p>
                        <p>即忽略本次在冷却时间内触发的此实例组的所有伸缩动作，直到下一次满足伸缩条件且不在冷却时间内再执行。</p>
                      </div>
                    </template>
                  </el-tooltip>
                </div>
              </template>
              <el-input v-model="ruleForm.freezingTime" placeholder="请输入集群冷却时间" class="input-width">
                <template #append>秒</template>
              </el-input>
            </el-form-item>
            <template v-if="ruleForm.scalingType == 1">
              <el-form-item label="执行实例初始化后脚本：">
                是
              </el-form-item>
              <el-form-item label="执行集群启动前脚本：" prop="enableBeforestartScript">
                <el-switch
                        v-model="ruleForm.enableBeforestartScript"
                        inline-prompt
                        active-text="是"
                        :active-value="1"
                        :inactive-value="0"
                        inactive-text="否"
                />
              </el-form-item>
              <el-form-item label="执行集群启动后脚本：" prop="enableAfterstartScript">
                <el-switch
                        v-model="ruleForm.enableAfterstartScript"
                        inline-prompt
                        active-text="是"
                        :active-value="1"
                        :inactive-value="0"
                        inactive-text="否"
                />
              </el-form-item>
            </template>
            <template v-else>
              <el-form-item label="优雅缩容：" prop="isGracefulScalein">
                <template #label>
                  <div style="display: flex;align-items: center;">
                    <span>优雅缩容：</span>
                    <el-tooltip effect="light" placement="right">
                      <el-icon><QuestionFilled /></el-icon>
                      <template #content>
                        <div style="max-width: 320px;">您可以设置超时时间，释放YARN上作业所在的节点。如果节点没有运行YARN上的作业或者作业运行超出您设置的超时时间，则释放此节点。超时时间最大值为1800秒。</div>
                      </template>
                    </el-tooltip>
                  </div>
                </template>
                <el-switch
                        v-model="ruleForm.isGracefulScalein"
                        inline-prompt
                        active-text="是"
                        :active-value="1"
                        :inactive-value="0"
                        inactive-text="否"
                />
              </el-form-item>
              <el-form-item label="等待时间：" prop="scaleinWaitingtime" v-if="ruleForm.isGracefulScalein == 1">
                <el-input class="input-width" v-model="ruleForm.scaleinWaitingtime"
                          placeholder="请输入缩容等待时间">
                  <template #append>秒</template>
                </el-input>
                <div class="from-item-tip">可选缩容等待时间范围为60-1800秒</div>
              </el-form-item>
            </template>
          </template>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="ruleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addRuleSave" :loading="pageLoading">保存</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog
            class="center-dialog"
            v-model="ruleDetailDialogVisible"
            title="弹性伸缩规则详情"
            width="600"
            destroy-on-close
            :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <div class="detail-items">
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">规则名称：</div>
              <div class="value">{{ ruleForm.esRuleName }}</div>
            </div>
          </div>
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">单次{{ (ruleForm.scalingType == 1 ? '扩容' : '缩容') }}数：</div>
              <div class="value">{{ ruleForm.perSalingCout }} 台</div>
            </div>
          </div>
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">集群负载指标：</div>
              <div class="value">{{ loadMetricToStr(ruleForm.loadMetric) }}</div>
            </div>
          </div>
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">统计窗口期：</div>
              <div class="value">{{ ruleForm.windowSize }} 分钟</div>
            </div>
          </div>
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">统计规则：</div>
              <div class="value">{{ aggregateTypeToStr(ruleForm.aggregateType) }} {{ ruleForm.operator }}
                {{ ruleForm.threshold }}
              </div>
            </div>
          </div>
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">统计周期个数：</div>
              <div class="value">{{ ruleForm.repeatCount }} 个</div>
            </div>
          </div>
          <div class="flex-row">
            <div class="detail-item">
              <div class="label">集群冷却时间：</div>
              <div class="value">{{ ruleForm.freezingTime }} 秒</div>
            </div>
          </div>
          <template v-if="ruleForm.scalingType == 1">
            <div class="flex-row">
              <div class="detail-item">
                <div class="label">执行实例初始化后脚本：</div>
                <div class="value">ON</div>
              </div>
            </div>
            <div class="flex-row">
              <div class="detail-item">
                <div class="label">执行集群启动前脚本：</div>
                <div class="value">{{ ruleForm.enableBeforestartScript ? 'ON' : 'OFF' }}</div>
              </div>
            </div>
            <div class="flex-row">
              <div class="detail-item">
                <div class="label">执行集群启动后脚本：</div>
                <div class="value">{{ ruleForm.enableAfterstartScript ? 'ON' : 'OFF' }}</div>
              </div>
            </div>
          </template>
          <template v-else>
            <div class="flex-row">
              <div class="detail-item">
                <div class="label">优雅缩容：</div>
                <div class="value">{{ ruleForm.isGracefulScalein ? 'ON' : 'OFF' }}</div>
              </div>
            </div>
            <div class="flex-row" v-if="ruleForm.isGracefulScalein == 1">
              <div class="detail-item">
                <div class="label">缩容等待时间：</div>
                <div class="value">{{ ruleForm.scaleinWaitingtime }} 秒</div>
              </div>
            </div>
          </template>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="ruleDetailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="editRuleEvent(ruleForm)">编辑</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { QuestionFilled } from '@element-plus/icons-vue'
import {ref, reactive, defineEmits, defineProps, defineExpose, toRefs, nextTick, onBeforeUnmount, onMounted} from 'vue'
import clusterApi from "../../../api/cluster";
import {ElMessage, ElMessageBox} from 'element-plus';
import FormCheck from "../../../utils/formCheck";
import {throttle} from "../../../utils/tools";
import {formatTime, columnTimeFormat} from "@/utils/format-time";
import permissionCheck from "@/utils/permission-check";

const pageLoading = ref(false)

const loadMetricOptions = [
  {"label": "可用内存百分比", "value": 'MemoryAvailablePrecentage',"unit": '%'},
  {"label": "容器分配比率", "value": 'ContainerPendingRatio',"unit": '%'},
  {"label": "可用vCore百分比", "value": 'VCoreAvailablePrecentage',"unit": '%'},
  {"label": "应用程序挂起数", "value": 'AppsPending',"unit": '个'},
]

function loadMetricToStr(key) {
  let option = loadMetricOptions.find(item => {
    return item.value == key
  }) || {}
  return option.label || key
}

function loadMetricToUnit(key) {
  let option = loadMetricOptions.find(item => {
    return item.value == key
  }) || {}
  return option.unit || key
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

const props = defineProps({
  clusterId: {
    type: String,
    default: ''
  },
  editGroupData: {
    type: Object,
    default: () => {}
  },
  newGroupConf: {
    type: Object,
    default: () => {}
  }
});
const {clusterId, editGroupData, newGroupConf} = toRefs(props)

// 获取伸缩规则数据
const ruleConfData = reactive({
  editGroup: {},
  confData: {},
  expandRules: [],
  reduceRules: [],
})

function initLoaclScalingRules() {
  ruleConfData.editGroup = {}
  ruleConfData.confData = newGroupConf.value
  if(!ruleConfData.confData.isFullCustody){
    ruleConfData.confData.isFullCustody = 0
  }

  let scalingRules = ruleConfData.confData.scalingRules || []
  ruleConfData.expandRules = scalingRules.filter(item => {
    return item.scalingType == 1 || item.scalingType == null
  }) || []
  ruleConfData.reduceRules = scalingRules.filter(item => {
    return item.scalingType == 0
  }) || []
}

function isShowCustodyButton() {
  return ruleConfData.confData.isFullCustody && ruleConfData.confData.dataType != 'local'
}

function getElasticScalingRule() {
  pageLoading.value = true
  clusterApi.getElasticScalingRule({
    clusterId: clusterId.value,
    groupName: editGroupData.value.groupName,
    vmRole: editGroupData.value.vmRole,
  }).then(res => {
    if (res.result == true) {
      ruleConfData.editGroup = editGroupData.value
      ruleConfData.confData = res.data
      if (!ruleConfData.confData.fullCustodyParam) {
        // 默认弄个空的数据
        ruleConfData.confData.fullCustodyParam = {}
      }
      if (!ruleConfData.confData.defaultFullCustodyParam) {
        // 默认弄个空的数据
        ruleConfData.confData.defaultFullCustodyParam = {}
      }

      let param = ruleConfData.confData.fullCustodyParam;
      let defParam = ruleConfData.confData.defaultFullCustodyParam;
      fullCustodyConfigForm.scaleoutMetric = param.scaleoutMetric?
          param.scaleoutMetric:
          defParam.scaleoutMetric;
      fullCustodyConfigForm.scaleinMemoryThreshold = param.scaleinMemoryThreshold?
          param.scaleinMemoryThreshold:
          defParam.scaleinMemoryThreshold;

      fullCustodyConfigEnabled.value = param.scaleinMemoryThreshold != undefined && param.scaleoutMetric != undefined;

      let scalingRules = ruleConfData.confData.scalingRules || []
      ruleConfData.expandRules = scalingRules.filter(item => {
        return item.scalingType == 1 || item.scalingType == null
      }) || []
      ruleConfData.reduceRules = scalingRules.filter(item => {
        return item.scalingType == 0
      }) || []
      if(!ruleConfData.confData.isFullCustody){
        ruleConfData.confData.isFullCustody = 0
      }

    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  }).finally(() => {
    pageLoading.value = false
  })
}

// 编辑节点最大最小值
let limitDialogVisible = ref(false)
const limitForm = reactive({
  minCount: '',
  maxCount: '',
})
const limitFormRules = {
  maxCount: [FormCheck.required('请输入最大节点数'), FormCheck.justPositiveInt(), FormCheck.valueIn(1, 1999)],
  minCount: [FormCheck.required('请输入最小节点数'), FormCheck.justNaturalNumber(), FormCheck.valueIn(0, 1999)],
}
const Ref_limitForm = ref(null)

function resetLimit() {
  limitForm.minCount = ruleConfData.confData.minCount
  limitForm.maxCount = ruleConfData.confData.maxCount

  limitDialogVisible.value = true
}

const limitSaveEvent = throttle(function () {
  Ref_limitForm.value.validate((valid, fields) => {
    if (valid) {
      if (ruleConfData.confData.dataType == 'local') {
        ruleConfData.confData.minCount = limitForm.minCount
        ruleConfData.confData.maxCount = limitForm.maxCount

        limitDialogVisible.value = false
      } else {
        pageLoading.value = true
        clusterApi.updateGroupElasticScaling({
          groupEsId: ruleConfData.confData.groupEsId,
          clusterId: clusterId.value,
          minCount: limitForm.minCount,
          maxCount: limitForm.maxCount,
        }).then(res => {
          if (res.result == true) {

            limitDialogVisible.value = false
            getElasticScalingRule()
          } else {
            ElMessage.error(res.errorMsg)
            console.log(res)
          }
        }).finally(() => {
          pageLoading.value = false
        })
      }
    } else {
      console.log('error submit!', fields)
    }
  })
}, 500)

// 冷却时间
const coolingTimeFormat = function (str) {
  let t = formatTime(str)
  if (t == '-') {
    return str
  }
  return t
}

// 扩缩容托管
let fullCustodyDialogVisible = ref(false)
const fullCustodyForm = reactive({
  scaleinWaitingTime: '',
  isGracefulScalein: '',
  enableAfterstartScript: '',
  enableBeforestartScript: '',
})
const fullCustodyFormRules = {
  isGracefulScalein: FormCheck.required('请选择是否优雅缩容'),
  scaleinWaitingTime: [FormCheck.required('请输入等待时间'), FormCheck.justPositiveInt(), FormCheck.valueIn(60, 1800)],
  enableBeforestartScript: FormCheck.required('请选择'),
  enableAfterstartScript: FormCheck.required('请选择'),
}
const Ref_fullCustodyForm = ref(null)

// 集群全托管的配置
let fullCustodyConfigDialogVisible = ref(false)
let fullCustodyConfigEnabled = ref(false)

const fullCustodyConfigForm = reactive({
  scaleoutMetric: '',
  scaleinMemoryThreshold: '',
})
const Ref_fullCustodyConfigForm = ref(null)

const isFullCustodyChenged = function () {
  let val = ruleConfData.confData.isFullCustody
  if (val == 0) {
    ElMessageBox.confirm('您确定需要关闭扩缩容托管模式吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(() => {
      updateFullCustody()
    }).catch(() => {
      ruleConfData.confData.isFullCustody = 1
    })
  } else {
    fullCustodyForm.scaleinWaitingTime = ruleConfData.confData.scaleinWaitingTime
    fullCustodyForm.isGracefulScalein = ruleConfData.confData.isGracefulScalein
    fullCustodyForm.enableAfterstartScript = ruleConfData.confData.enableAfterstartScript
    fullCustodyForm.enableBeforestartScript = ruleConfData.confData.enableBeforestartScript

    fullCustodyDialogVisible.value = true
  }
}

const cancelFullCustodySaveEvent = throttle(function () {
  ruleConfData.confData.isFullCustody = 0

  fullCustodyDialogVisible.value = false
}, 500)

const fullCustodySaveEvent = throttle(function () {
  Ref_fullCustodyForm.value.validate((valid, fields) => {
    if (valid) {
      ruleConfData.confData.isFullCustody = 1

      updateFullCustody()
    } else {
      console.log('error submit!', fields)
    }
  })
}, 500)

const updateFullCustody = () => {
  if (ruleConfData.confData.dataType == 'local') {
    ruleConfData.confData.scaleinWaitingTime = fullCustodyForm.scaleinWaitingTime
    ruleConfData.confData.isGracefulScalein = fullCustodyForm.isGracefulScalein
    ruleConfData.confData.enableAfterstartScript = fullCustodyForm.enableAfterstartScript
    ruleConfData.confData.enableBeforestartScript = fullCustodyForm.enableBeforestartScript

    fullCustodyDialogVisible.value = false
  } else {
    pageLoading.value = true
    clusterApi.updateGroupElasticScaling({
      groupEsId: ruleConfData.confData.groupEsId,
      clusterId: clusterId.value,
      minCount: ruleConfData.confData.minCount,
      maxCount: ruleConfData.confData.maxCount,
      isFullCustody: ruleConfData.confData.isFullCustody,
      ...fullCustodyForm
    }).then(res => {
      if (res.result == true) {
        ElMessage.success('更新成功！')

        fullCustodyDialogVisible.value = false

        getElasticScalingRule()
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    }).finally(() => {
      pageLoading.value = false
    })
  }
}

// 关闭全托管自定义配置的弹窗
const cancelFullCustodyConfigSaveEvent = throttle(function () {
  fullCustodyConfigDialogVisible.value = false
}, 500)

// 保存全托管自定义配置
const fullCustodyConfigSaveEvent = throttle(function () {
  updateFullCustodyConfig()
}, 500)

const updateFullCustodyConfig = () => {
  // 将fullCustodyConfigForm数据保存到ruleConfData.confData中, 然后调用接口保存
  ruleConfData.confData.fullCustodyParam.scaleoutMetric = fullCustodyConfigForm.scaleoutMetric;
  ruleConfData.confData.fullCustodyParam.scaleinMemoryThreshold = fullCustodyConfigForm.scaleinMemoryThreshold;

  let fullCustodyParam = {
    scaleoutMetric: ruleConfData.confData.fullCustodyParam.scaleoutMetric,
    scaleinMemoryThreshold: ruleConfData.confData.fullCustodyParam.scaleinMemoryThreshold,
  }
  console.log("fullCustodyConfigEnabled:", fullCustodyConfigEnabled.value)
  if (!fullCustodyConfigEnabled.value) {
    fullCustodyParam = {
      scaleoutMetric: null,
      scaleinMemoryThreshold: null,
    }
  }
  console.log("全托管参数:",fullCustodyParam)

  pageLoading.value = true
  clusterApi.updateGroupESFullCustodyParam({
    groupEsId: ruleConfData.confData.groupEsId,
    clusterId: clusterId.value,
    fullCustodyParam: fullCustodyParam
  }).then(res => {
    if (res.result == true) {
      ElMessage.success('更新成功！')

      fullCustodyConfigDialogVisible.value = false

      getElasticScalingRule()
    } else {
      ElMessage.error(res.errorMsg)
      console.log(res)
    }
  }).finally(() => {
    pageLoading.value = false
  })
}


// 编辑规则
let ruleDialogVisible = ref(false)
const Ref_ruleForm = ref(null)

const ruleForm = reactive({
  esRuleId: '',
  scalingType: 1,
  esRuleName: '',
  perSalingCout: '',
  loadMetric: '',
  windowSize: '',
  aggregateType: '',
  operator: '',
  threshold: '',
  repeatCount: '',
  freezingTime: '',
  enableBeforestartScript: 0,
  enableAfterstartScript: 0,
  isGracefulScalein: 0,
  scaleinWaitingtime: 0,
})

const ruleFormRules = {
  esRuleName: FormCheck.required('请输入规则名称'),
  perSalingCout: [FormCheck.required('请输入伸缩数量'), FormCheck.valueIn(1, 1999), FormCheck.justPositiveInt()],
  loadMetric: FormCheck.required('请选择集群负载指标'),
  windowSize: FormCheck.required('请选择统计周期'),
  repeatCount: FormCheck.required('请选择统计周期个数'),
  freezingTime: FormCheck.required('请输入集群冷却时间'),
  enableBeforestartScript: FormCheck.required('请选择'),
  enableAfterstartScript: FormCheck.required('请选择'),
  statisticalRules: checkStatisticalRules(),
  isGracefulScalein: FormCheck.required('请选择是否优雅缩容'),
  scaleinWaitingtime: [FormCheck.required('请输入等待时间'), FormCheck.justPositiveInt(), FormCheck.valueIn(60, 1800)],
}

function checkStatisticalRules() {
  return {
    validator: (rule, value, callback) => {
      if (!ruleForm.aggregateType || !ruleForm.operator || ruleForm.threshold === '') {
        return callback(new Error('请完善统计规则！'))
      }
      let v = ruleForm.threshold;
      let reg = /^[1-9][0-9]*$/;
      if (!reg.test(v)) {
        return callback(new Error('请输入正整数'));
      }
      let val = parseFloat(v)
      if (val < 1 || val > 100) {
        return callback(new Error('请输入1-100中的正整数！'))
      }
      callback()
    },
    trigger: 'blur',
    required: true
  }
}

function addExpandRule() {
  ruleForm.esRuleId = ''
  ruleForm.scalingType = 1
  ruleForm.esRuleName = ''
  ruleForm.perSalingCout = 10
  ruleForm.loadMetric = 'MemoryAvailablePrecentage'
  ruleForm.windowSize = '5'
  ruleForm.aggregateType = 'avg'
  ruleForm.operator = '<='
  ruleForm.threshold = 20
  ruleForm.repeatCount = '1'
  ruleForm.freezingTime = 120
  ruleForm.enableBeforestartScript = 1
  ruleForm.enableAfterstartScript = 1
  ruleForm.isGracefulScalein = 0
  ruleForm.scaleinWaitingtime = ''

  ruleDialogVisible.value = true
}

function addReduceRule() {
  ruleForm.esRuleId = ''
  ruleForm.scalingType = 0
  ruleForm.esRuleName = ''
  ruleForm.perSalingCout = 3
  ruleForm.loadMetric = 'MemoryAvailablePrecentage'
  ruleForm.windowSize = '5'
  ruleForm.aggregateType = 'avg'
  ruleForm.operator = '>='
  ruleForm.threshold = 80
  ruleForm.repeatCount = '1'
  ruleForm.freezingTime = 300
  ruleForm.enableBeforestartScript = 0
  ruleForm.enableAfterstartScript = 0
  ruleForm.isGracefulScalein = 1
  ruleForm.scaleinWaitingtime = 180

  ruleDialogVisible.value = true
}

const addRuleSave = throttle(function () {
  if (pageLoading.value) {
    return ;
  }
  Ref_ruleForm.value.validate((valid, fields) => {
    if (valid) {
      let ruleItem = {
        esRuleId: ruleForm.esRuleId,
        scalingType: ruleForm.scalingType,
        esRuleName: ruleForm.esRuleName,
        perSalingCout: ruleForm.perSalingCout,
        loadMetric: ruleForm.loadMetric,
        windowSize: ruleForm.windowSize,
        aggregateType: ruleForm.aggregateType,
        operator: ruleForm.operator,
        threshold: ruleForm.threshold,
        repeatCount: ruleForm.repeatCount,
        freezingTime: ruleForm.freezingTime,
        enableBeforestartScript: ruleForm.enableBeforestartScript,
        enableAfterstartScript: ruleForm.enableAfterstartScript,
        isGracefulScalein: ruleForm.isGracefulScalein,
        scaleinWaitingtime: ruleForm.scaleinWaitingtime,
      }
      if (ruleConfData.confData.dataType == 'local') {
        if (!ruleItem.esRuleId) {
          ruleItem.esRuleId = new Date().getTime();
          ruleConfData.confData.scalingRules.push(ruleItem)
          ElMessage.success("添加成功！")
        } else {
          let idx = ruleConfData.confData.scalingRules.findIndex(itm => {
            return itm.esRuleId == ruleItem.esRuleId
          })
          ruleConfData.confData.scalingRules.splice(idx, 1, ruleItem)
          ElMessage.success("更新成功！")
        }
        ruleDialogVisible.value = false

        initLoaclScalingRules();

        return ;
      }
      let params = {
        groupName: ruleConfData.editGroup.groupName,
        vmRole: ruleConfData.editGroup.vmRole,
        clusterId: clusterId.value,
        scalingRules: [
          ruleItem
        ]
      }
      pageLoading.value = true
      if (ruleForm.esRuleId) {
        clusterApi.updateElasticScalingRule(params).then(res => {
          if (res.result == true) {
            ruleDialogVisible.value = false
            getElasticScalingRule()
            ElMessage.success("更新成功！")
          } else {
            ElMessage.error(res.errorMsg)
            console.log(res)
          }
        }).finally(() => {
          pageLoading.value = false
        })
      } else {
        clusterApi.postElasticScalingRule(params).then(res => {
          if (res.result == true) {
            ruleDialogVisible.value = false
            getElasticScalingRule()
            ElMessage.success("添加成功！")
          } else {
            ElMessage.error(res.errorMsg)
            console.log(res)
          }
        }).finally(() => {
          pageLoading.value = false
        })
      }
    } else {
      console.log('error submit!', fields)
    }
  })
}, 500)

function deleteElasticScalingRule(item) {
  ElMessageBox.confirm('您确定需要删除该项规则吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {

    if (ruleConfData.confData.dataType == 'local') {
      ElMessage.success("删除成功！")
      let idx = ruleConfData.confData.scalingRules.findIndex(itm => {
        return itm.esRuleId == item.esRuleId
      })
      ruleConfData.confData.scalingRules.splice(idx, 1)

      initLoaclScalingRules()
      return ;
    }

    pageLoading.value = true
    clusterApi.deleteElasticScalingRule({
      groupEsId: ruleConfData.confData.groupEsId,
      scalingRules: [
        {
          esRuleId: item.esRuleId
        }
      ]
    }).then(res => {
      if (res.result == true) {
        ElMessage.success("删除成功！")
        getElasticScalingRule()
      } else {
        ElMessage.error(res.errorMsg)
        console.log(res)
      }
    }).finally(() => {
      pageLoading.value = false
    })
  })
}

let ruleDetailDialogVisible = ref(false)

function detailRuleEvent(item) {
  ruleForm.esRuleId = item.esRuleId
  ruleForm.scalingType = item.scalingType
  ruleForm.esRuleName = item.esRuleName
  ruleForm.perSalingCout = item.perSalingCout
  ruleForm.loadMetric = item.loadMetric
  ruleForm.windowSize = item.windowSize
  ruleForm.aggregateType = item.aggregateType
  ruleForm.operator = item.operator
  ruleForm.threshold = item.threshold
  ruleForm.repeatCount = item.repeatCount
  ruleForm.freezingTime = item.freezingTime
  ruleForm.enableBeforestartScript = item.enableBeforestartScript || 0
  ruleForm.enableAfterstartScript = item.enableAfterstartScript || 0
  ruleForm.isGracefulScalein = item.isGracefulScalein || 0
  ruleForm.scaleinWaitingtime = item.scaleinWaitingtime || ''

  ruleDetailDialogVisible.value = true
}

function editRuleEvent(item) {
  ruleForm.esRuleId = item.esRuleId
  ruleForm.scalingType = item.scalingType
  ruleForm.esRuleName = item.esRuleName
  ruleForm.perSalingCout = item.perSalingCout
  ruleForm.loadMetric = item.loadMetric
  ruleForm.windowSize = item.windowSize
  ruleForm.aggregateType = item.aggregateType
  ruleForm.operator = item.operator
  ruleForm.threshold = item.threshold
  ruleForm.repeatCount = item.repeatCount
  ruleForm.freezingTime = item.freezingTime
  ruleForm.enableBeforestartScript = item.enableBeforestartScript || 0
  ruleForm.enableAfterstartScript = item.enableAfterstartScript || 0
  ruleForm.isGracefulScalein = item.isGracefulScalein || 0
  ruleForm.scaleinWaitingtime = item.scaleinWaitingtime || ''

  ruleDetailDialogVisible.value = false

  ruleDialogVisible.value = true
}

onMounted(() => {

  console.log(newGroupConf.value)

  if (newGroupConf.value.dataType == 'local') {
    initLoaclScalingRules()
  } else {
    getElasticScalingRule()
  }
})

function isGracefulScaleinChange(isGracefulScalein){
  if (isGracefulScalein==0){
    fullCustodyForm.scaleinWaitingTime =0;
  }else {
    fullCustodyForm.scaleinWaitingTime =300;
  }
}
</script>

<style lang="stylus" scoped type="text/stylus">
.elastic-scaling {
  width 100%;

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
          line-height 32px;
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
</style>
