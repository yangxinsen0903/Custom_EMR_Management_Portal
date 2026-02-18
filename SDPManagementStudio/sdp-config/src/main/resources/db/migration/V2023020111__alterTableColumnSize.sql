-- 将conf_tag_keys表字段大小改为128个字符
ALTER TABLE conf_tag_keys MODIFY COLUMN tag_key varchar(128) null comment '标签名称';
