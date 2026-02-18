package com.sunbox.sdpadmin.model.admin.request;

import com.mchange.v2.cfg.MConfig;
import com.sunbox.domain.FullCustodyParam;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

@Data
public class ConfGroupElasticScalingData {

    private String userName;

    // 实例组弹性配置ID
    private String groupEsId;

    // 集群ID
    @NotEmpty
    private String clusterId;

    // 实例组名称
    @NotEmpty
    private String groupName;

    // 实例角色
    private String vmRole;

    // 实例组最大实例数
    private Integer maxCount;

    // 实例组最小实例数
    private Integer minCount;
    /**
     * 是否优雅缩容
     */
    private Integer isGracefulScalein;
    /**
     * 优雅缩容等待时间单位：分钟
     */
    private Integer scaleinWaitingTime;
    /**
     * 扩容是否执行启动前脚本
     */
    private Integer enableBeforestartScript;
    /**
     * 扩容是否执行启动后脚本
     */
    private Integer enableAfterstartScript;
    /**
     * 是否开启全托管弹性伸缩
     */
    private Integer isFullCustody;

    /**
     * 开启全托管伸缩后的自定义参数
     */
    private FullCustodyParam fullCustodyParam;

    /**
     * 系统默认全托管参数
     */
    private FullCustodyParam defaultFullCustodyParam;

    // 弹性伸缩时间限制
    private Date scalingLimitTime;

    // 是否有效
    private Integer isValid;

    // 冷却结束时间
    private String freezingEndTime;


    // 实例组弹性伸缩规则配置
    private List<ConfGroupElasticScalingRuleData> scalingRules;


//    @Override
//    public String toString() {
//        return "{" +
//                "\"groupEsId\":\"" + groupEsId + '"' +
//                ", \"clusterId\":\"" + clusterId + '"' +
//                ", \"groupName\":\"" + groupName + '"' +
//                ", \"vmRole\":\"" + vmRole + '"' +
//                ", \"maxCount\":" + maxCount +
//                ", \"minCount\":" + minCount +
//                ", \"scalingLimitTime\":" + scalingLimitTime +
//                ", \"isValid\":" + isValid +
//                ", \"freezingEndTime\":\"" + freezingEndTime + '"' +
//                ", \"scalingRules\":" + scalingRules +
//                "}";
//    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"").append("userName"                   ).append("\":").append("\"").append(userName).append('\"');
        sb.append(",").append("\"").append("groupEsId"                  ).append("\":").append("\"").append(groupEsId).append('\"');
        sb.append(",").append("\"").append("clusterId"                  ).append("\":").append("\"").append(clusterId).append('\"');
        sb.append(",").append("\"").append("groupName"                  ).append("\":").append("\"").append(groupName).append('\"');
        sb.append(",").append("\"").append("vmRole"                     ).append("\":").append("\"").append(vmRole).append('\"');
        sb.append(",").append("\"").append("maxCount"                   ).append("\":").append(maxCount);
        sb.append(",").append("\"").append("minCount"                   ).append("\":").append(minCount);
        sb.append(",").append("\"").append("isGracefulScalein"          ).append("\":").append(isGracefulScalein);
        sb.append(",").append("\"").append("scaleinWaitingTime"         ).append("\":").append(scaleinWaitingTime);
        sb.append(",").append("\"").append("enableBeforestartScript"    ).append("\":").append(enableBeforestartScript);
        sb.append(",").append("\"").append("enableAfterstartScript"     ).append("\":").append(enableAfterstartScript);
        sb.append(",").append("\"").append("isFullCustody"              ).append("\":").append(isFullCustody);
        sb.append(",").append("\"").append("scalingLimitTime"           ).append("\":").append("\"").append(scalingLimitTime).append("\"");
        sb.append(",").append("\"").append("isValid"                    ).append("\":").append(isValid);
        sb.append(",").append("\"").append("freezingEndTime"            ).append("\":").append("\"").append(freezingEndTime).append('\"');
        sb.append("\"").append('}');
      return sb.toString();
    }

    public static void main(String[] args) {

        ConfGroupElasticScalingData confGroupElasticScalingData = new ConfGroupElasticScalingData();
        confGroupElasticScalingData.setGroupEsId("1");
        confGroupElasticScalingData.setGroupName("2");
        confGroupElasticScalingData.setClusterId("3");
        confGroupElasticScalingData.setIsValid(1);
        confGroupElasticScalingData.setScalingLimitTime(new Date());
        confGroupElasticScalingData.setEnableAfterstartScript(1);
        confGroupElasticScalingData.setEnableBeforestartScript(1);
        confGroupElasticScalingData.setIsFullCustody(1);
        confGroupElasticScalingData.setUserName("4");
        confGroupElasticScalingData.setVmRole("5");
        confGroupElasticScalingData.setMaxCount(4);
        confGroupElasticScalingData.setMinCount(3);
        confGroupElasticScalingData.setIsGracefulScalein(1);
        confGroupElasticScalingData.setIsValid(1);
        confGroupElasticScalingData.setScaleinWaitingTime(3);
        System.out.println(confGroupElasticScalingData);


    }
}
