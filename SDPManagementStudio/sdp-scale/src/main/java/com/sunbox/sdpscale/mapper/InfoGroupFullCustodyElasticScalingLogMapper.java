package com.sunbox.sdpscale.mapper;


import cn.hutool.core.util.StrUtil;
import com.sunbox.domain.InfoGroupFullCustodyElasticScalingLog;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;


public interface InfoGroupFullCustodyElasticScalingLogMapper {
    int insertSelective(InfoGroupFullCustodyElasticScalingLog record);

    void update(InfoGroupFullCustodyElasticScalingLog updateComputeResult);


    public static class Test {
        public static void printCreateTalbeSql(Class clazz,String keyName){
            StringBuilder sb = new StringBuilder();
            sb.append("create table ").append(toUnderlineCase(clazz.getSimpleName())).append("(");
            for (Field declaredField : clazz.getDeclaredFields()) {
                sb.append(" ").append(toUnderlineCase(declaredField.getName())).append(" ").append(getDbType(declaredField));
                if (keyName.equalsIgnoreCase(declaredField.getName())){
                    sb.append(" PRIMARY KEY").append(" ");
                    if (getDbType(declaredField).contains("INT")){
                        sb.append("AUTO_INCREMENT");
                    }
                }
                sb.append(",");
            }
            sb.append(");");
            System.out.println(sb);
        }
        public static String space(int size){
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size; i++) {
                sb.append("  ");
            }
            return sb.toString();
        }
        public static void main(String[] args) {
            printCreateTalbeSql(InfoGroupFullCustodyElasticScalingLog.class,"esFullLogId");
        }
        public static void printMapper(){

            Class clazz = InfoGroupFullCustodyElasticScalingLog.class;
            Class mapperClazz = InfoGroupFullCustodyElasticScalingLogMapper.class;
            String idKey = "esFullLogId";
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
            sb.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">").append("\n");
            sb.append("<mapper namespace=\"").append(mapperClazz.getName()).append("\">").append("\n");
            sb.append(space(1)).append("<resultMap id=\"BaseResultMap\" type=\"").append(clazz.getName()).append("\">").append("\n");
            for (Field declaredField : clazz.getDeclaredFields()) {
                String type = getDbType(declaredField);
                String tag = declaredField.getName().equals(idKey) ? "<id" : "<result";
                sb.append(space(2)).append(tag).append(" column=\"").append(toUnderlineCase(declaredField.getName())).append("\" jdbcType=\"")
                        .append(type).append("\"").append(" property=\"").append(declaredField.getName()).append("\"/>").append("\n");
            }
            sb.append(space(1)).append("</resultMap>").append("\n");
            sb.append(space(1)).append("<insert id=\"insertSelective\" parameterType=\"").append(clazz.getName())
                    .append("\"  useGeneratedKeys=\"true\" keyProperty=\"").append(idKey).append("\" keyColumn=\"").append(toUnderlineCase(idKey))
                            .append("\">").append("\n");
            sb.append(space(2)).append("insert into ").append(classToTableName(clazz)).append("\n");
            sb.append(space(2)).append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">").append("\n");
            for (Field declaredField : clazz.getDeclaredFields()) {
                sb.append(space(3)).append("<if test=\"").append(declaredField.getName()).append(" != null\">").append("\n");
                sb.append(space(4)).append(toUnderlineCase(declaredField.getName())).append(",\n");
                sb.append(space(3)).append("</if>").append("\n");
            }
            sb.append(space(2)).append("</trim>").append("\n");
            sb.append(space(2)).append("<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">").append("\n");
            for (Field declaredField : clazz.getDeclaredFields()) {
                sb.append(space(3)).append("<if test=\"").append(declaredField.getName()).append(" != null\">").append("\n");
                sb.append(space(4)).append("#{").append(declaredField.getName()).append(",jdbcType=").append(getDbType(declaredField)).append("},\n");
                sb.append(space(3)).append("</if>").append("\n");
            }
            sb.append(space(2)).append("</trim>").append("\n");
            sb.append(space(1)).append("</insert>").append("\n");
            sb.append(space(1)).append("<update id=\"update\">").append("\n");
            sb.append(space(2)).append("update ").append(classToTableName(clazz)).append("\n");
            sb.append(space(2)).append("<set>").append("\n");
            for (Field declaredField : clazz.getDeclaredFields()) {
                if (declaredField.getName().equals(idKey)){
                    continue;
                }
                sb.append(space(3)).append("<if test=\"").append(declaredField.getName()).append(" != null\">").append("\n");
                sb.append(space(4)).append(toUnderlineCase(declaredField.getName())).append(" = #{").append(declaredField.getName()).append(",jdbcType=").append(getDbType(declaredField)).append("},\n");
                sb.append(space(3)).append("</if>").append("\n");
            }
            sb.append(space(2)).append("</set>").append("\n");
            sb.append(space(2)).append("where ").append(toUnderlineCase(idKey)).append(" = ").append("#{").append(idKey).append("}").append("\n");
            sb.append(space(1)).append("</update>").append("\n");
            sb.append("</mapper>").append("\n");
            System.out.println(sb);
        }
        public static String classToTableName(Class clazz){
            return StrUtil.toUnderlineCase(clazz.getSimpleName());
        }
        public static String toUnderlineCase(String text){
            return StrUtil.toUnderlineCase(text);
        }
        public static String getDbType(Field field){
            Class<?> type = field.getType();
            if (type == Integer.class){
                return "INTEGER";
            }
            if (type == Long.class){
                return "BIGINT";
            }
            if (type == Float.class || type == Double.class){
                return "DOUBLE";
            }
            if (type == String.class){
                return "VARCHAR";
            }
            if (type == Date.class || type == LocalDate.class || type == LocalTime.class || type == LocalDateTime.class){
                return "TIMESTAMP";
            }
            return "未知类型";
        }
    }

}
