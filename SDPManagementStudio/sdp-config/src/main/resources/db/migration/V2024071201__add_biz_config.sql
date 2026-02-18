CREATE TABLE IF NOT EXISTS biz_config (
  id BIGINT NOT NULL COMMENT 'ID主键， 不自增，需要手动设置',
  category varchar(128) NOT NULL COMMENT '配置分类',
  name varchar(128) NULL COMMENT '配置名称，中文',
  cfg_key varchar(128) NULL COMMENT '配置的Key',
  cfg_value varchar(2048) NULL COMMENT '配置的值 ',
  remark varchar(256) NULL COMMENT '备注说明 ',
  sort_no int NULL default 0 COMMENT '显示序号',
  state varchar(32) NOT NULL COMMENT '状态：VALID、INVALID、DELETED',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  CONSTRAINT biz_config_pk PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_general_ci
COMMENT='业务配置表，用来管理业务配置参数';

call create_index( 'biz_config', 'biz_config_category_IDX', ' (category)' );
call create_index( 'biz_config', 'biz_config_cfg_key_IDX', ' (cfg_key)' );
