package com.sunbox.sdpadmin.service;

import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpadmin.model.admin.request.AdminSaveClusterRequest;
import com.sunbox.sdpadmin.model.shein.request.InstanceGroupAddConfig;
import com.sunbox.sdpadmin.model.shein.request.SheinRequestModel;

public interface ICheckParam {

    /**
     * 校验时间参数格式是否正确
     * @param datetimeStr 时间字符串
     * @param format 时间格式化字符串
     * @return
     */
    ResultMsg checkDateTimeFormat(String datetimeStr,String format);

    /**
     * 校验起止时间的有效性
     * @param begTime 开始时间
     * @param endTime 结束时间
     * @param format 格式串
     * @return
     */
    ResultMsg checkBEDateTimeValid(String begTime,String endTime,String format);

    /**
     * shein接口Create接口参数检查
     * @param requestModel
     * @return
     */
    ResultMsg checkSheinCreateParam(SheinRequestModel requestModel);

    /**
     *  adminApi接口Create接口参数检查
     * @param adminRequestModel
     * @return
     */
    ResultMsg checkAdminApiCreateParam(AdminSaveClusterRequest adminRequestModel);

    /**
     * 检查ambaridb数据库
     * @param host 数据库主机
     * @param port 端口
     * @param database 数据库名称
     * @param user 用户名
     * @param password 密码
     * @param autocreate 是否自动创建数据库
     * @return
     */
    ResultMsg checkAmbariDb(String host,Integer port,String database,String user,
                            String password,boolean autocreate);

    /**
     * 检查hivemetadb数据库
     * @param host 数据库主机
     * @param port 端口
     * @param database 数据库名称
     * @param user 用户名
     * @param password 密码
     * @return
     */
    ResultMsg checkHiveMetaDb(String host, Integer port, String database, String user,
                              String password);

    ResultMsg checkDataDiskSize(Integer size);

    ResultMsg checkVMSku(String skuname,String region);

    ResultMsg checkDiskSku(String skuname,String region);

    ResultMsg checkDataDiskCnt(String region,String skuName,Integer dataDiskCnt);
}
