/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 操作系统镜像的Mapper
 * @author wangda
 * @date 2024/6/3
 */
@Mapper
@Repository
public interface OsImageMapper {
    List<Map<String, String>> queryStackVersionImage();
}
