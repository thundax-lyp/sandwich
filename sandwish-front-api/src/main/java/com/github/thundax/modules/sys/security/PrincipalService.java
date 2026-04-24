//package com.github.thundax.modules.sys.security;
//
//import com.github.thundax.modules.sys.entity.User;
//import com.github.thundax.modules.sys.utils.UserUtils;
//
//public class PrincipalService implements com.github.thundax.common.security.PrincipalService {
//
//	@Override
//	public String getPrincipalSerial() {
//		User currentUser = UserUtils.getUser();
//		if (currentUser != null) {
//			return currentUser.getId();
//		} else {
//			return null;
//		}
//	}
//
//}
