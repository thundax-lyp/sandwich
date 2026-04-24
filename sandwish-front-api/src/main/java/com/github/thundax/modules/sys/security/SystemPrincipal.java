//package com.github.thundax.modules.sys.security;
//
//import java.io.Serializable;
//
//import com.github.thundax.modules.sys.entity.User;
//import com.github.thundax.modules.sys.utils.UserUtils;
//
///**
// * 授权用户信息
// */
//public class SystemPrincipal implements com.github.thundax.common.security.Principal, Serializable {
//
//	private static final long serialVersionUID = 1L;
//
//	private String id; // 编号
//	private String loginName; // 登录名
//	private String name; // 姓名
//
//	public SystemPrincipal(User user) {
//		this.id = user.getId();
//		this.loginName = user.getLoginName();
//		this.name = user.getName();
//	}
//
//	@Override
//	public String getId() {
//		return id;
//	}
//
//	@Override
//	public String getLoginName() {
//		return loginName;
//	}
//
//	@Override
//	public String getName() {
//		return name;
//	}
//
//	@Override
//	public String getParams() {
//		return null;
//	}
//
//	/**
//	 * 获取SESSIONID
//	 */
//	public String getSessionid() {
//		try {
//			return (String) UserUtils.getSession().getId();
//		} catch (Exception e) {
//			return "";
//		}
//	}
//
//	@Override
//	public String toString() {
//		return id;
//	}
//
//}
