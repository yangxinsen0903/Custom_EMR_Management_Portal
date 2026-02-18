package com.sunbox.domain.enums;

/**
 * 动态配置类型，标识配置项是哪一类的动态配置。如：HBase的磁盘动态配置等 。
 * @author: wangda
 * @date: 2022/12/22
 */
public enum DynamicType {
    /** HBASE多磁盘动态配置 */
    HBASE_DISK,

    /** CORE实例组多磁盘配置，会根据磁盘数量动态生成多磁盘目录 */
    MULTI_DISK,
    /** TASK实例组多磁盘配置，会根据磁盘数量动态生成多磁盘目录 */
    MULTI_DISK_TASK,
    /** 在core-site配置MI绑定ABFS配置 */
    MI_ABFS,

}
