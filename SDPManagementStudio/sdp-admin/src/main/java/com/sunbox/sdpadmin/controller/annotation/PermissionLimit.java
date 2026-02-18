package com.sunbox.sdpadmin.controller.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限限制
 * @author xuxueli 2015-12-12 18:29:02
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionLimit {
	
	/**
	 * 登录拦截 (默认拦截)
	 */
	boolean limit() default true;

	/**
	 * 要求管理员权限
	 *
	 * @return
	 */
	boolean adminuser() default false;

	/**
	 * 角色，角色编码, 普通人员:STAFF,运维人员:MAINTAINER,管理员:ADMINISTRATOR
	 *
	 * @return
	 */
	String[] role() default {};

	/**
	 * READWRITE 读写,READ 读
	 * @return
	 */
	String[] sheinPermission() default {};

}