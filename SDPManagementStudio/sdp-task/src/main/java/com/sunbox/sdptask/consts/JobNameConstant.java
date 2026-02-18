package com.sunbox.sdptask.consts;

/**
 * @author : [niyang]
 * @className : JobNameConstant
 * @description : [描述说明该类的功能]
 * @createTime : [2022/12/9 3:45 PM]
 */
public class JobNameConstant {

    public static final String Cluster_VM = "cvm_jobid";

    public static final String Run_PlayBook = "run_playbook_jobid";

    public static final String Delete_VM = "delvm_jobid";

    public static final String Install_Ambari_Server = "install_ambari_server_jobid";

    public static final String Install_Ambari_Agent = "install_ambari_agent_jobid";

    public static final String Init_User_Script = "init_user_script_jobid";

    public static final String Before_Cluster_Start = "before_cluster_start_jobid";

    public static final String After_Cluster_Started = "after_cluster_started_jobid";

    public static final String Install_Cluster_App = "install_cluster_app_jobid";

    public static final String Decommonsion_Comonpent = "decommonsion_cluster_jobid";

    public static final String Close_Commonpent = "close_commonpent";

    public static final String Start_Cluster_Apps = "start_cluster_apps";

    /**
     *  ansible 重试结构体对象名称
     */
    public static final String Param_Ansible_Retry_Obj = "ansibleRetryObj";

    /**
     * ambari 重试结构体对象名称
     */
    public static final String Param_Ambari_Retry_obj = "ambariRetryObj";

    /**
     *  azure 申请VM 重试结构体对象名称
     */
    public static final String Param_Azure_Retry_obj = "azure_retryObj";


    /**
     *  ansbile 步骤重试开关变量名称
     */
    public static final String Switch_Ansible_Retry = "ansible_retry";

    /**
     * ambari 步骤重试开关变量名称
     */
    public static final String Switch_Ambari_Retry = "ambari_retry";

    /**
     * ansible 步骤降级开关变量名称
     */
    public static final String Switch_Ansible_Reduce = "reduce";

    /**
     * ansible 步骤降级开关变量名称
     */
    public static final String Switch_Ambari_Reduce = "ambari_reduce";

    /**
     * 安装TezUI
     */
    public static final String Install_Tez_UI = "install_tez_ui";

    /**
     * 重启大数据服务jobid
     */
    public static final String Restart_Cluster_Service = "restart_service_jobid";

    /**
     * 磁盘扩容
     */
    public static final String Saleout_Part_Job = "scaleout_part_job";

}
