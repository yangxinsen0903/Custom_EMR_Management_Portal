package com.sunbox.sdpadmin.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface PrometheusMetricsMapper {
    /**
     * 返回Map的字段如下：
     * <ol>
     *     <li>cluster_id: 集群ID</li>
     *     <li>cluster_name：集群名称 </li>
     *     <li>group_name实例组名称 </li>
     *     <li>sku：主机SKU名称 </li>
     *     <li>errcode：错误码，目前：1：Azure购买失败；99：其它 </li>
     *     <li>total_count：总买入次数 </li>
     *     <li>success_count：成功次数 </li>
     *     <li>fail_count：失败次数 </li>
     * </ol>
     *
     * @return
     */
    List<Map> statisticsSpotBuyFailureRate();

    /**
     * 竞价实例逐出率数据统计，返回Map的字段如下：
     * <ol>
     *     <li>cluster_id: 集群ID</li>
     *     <li>cluster_name: 集群名称</li>
     *     <li>group_name: 实例组名</li>
     *     <li>sku_name: sku名称</li>
     *     <li>stat_type: 统计类型：buy:竞价买入；delete：竞价逐出</li>
     *     <li>buy_count: 竞价买入数量</li>
     *     <li>reason_code: 逐出原因代码，event：事件逐出，probe：探活失败逐出</li>
     *     <li>delete_count: 逐出主机数量</li>
     * </ol>
     *
     * @return
     */
    List<Map> statisticsSpotEvictRate();

    /**
     * 统计平均购买时间，返回Map的字段如下：
     * <ol>
     *     <li>cluster_id：集群ID</li>
     *     <li>cluster_name：集群名称</li>
     *     <li>group_name：实例组名称</li>
     *     <li>sku_name：SKU名称</li>
     *     <li>state：状态</li>
     *     <li>total：总次数</li>
     *     <li>time_second：创建耗时，单位秒</li>
     * </ol>
     *
     * @return
     */
    List<Map> statisticsAverageBuyTime();

    /**
     * <ol>
     *     <li>cluster_id：集群ID</li>
     *     <li>cluster_name：集群名称</li>
     *     <li>group_name：实例组名称</li>
     *     <li>sku_name：SKU名称</li>
     *     <li>vm_count：虚拟数量</li>
     *     <li>cpu_count：CPU数据</li>
     * </ol>
     *
     * @return
     */
    List<Map> statisticsClusterResource();


    List<Map> getScaleInFailureResultReport(@Param("beginTime") Date beginTime, @Param("endTime") Date endTime);
}