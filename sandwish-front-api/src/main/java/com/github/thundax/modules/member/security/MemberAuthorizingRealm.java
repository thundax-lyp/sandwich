package com.github.thundax.modules.member.security;

import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.common.utils.encrypt.Md5;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.security.exception.DisabledUserException;
import com.github.thundax.modules.member.security.exception.InvalidUserOrPasswordException;
import com.github.thundax.modules.member.service.MemberService;
import com.github.thundax.modules.member.utils.ShiroUtils;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 系统安全认证实现类
 *
 * @author wdit
 */
public class MemberAuthorizingRealm extends AuthorizingRealm {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private MemberService memberService;
    private final String whiteCaptcha;
    private final String defalutPassword;

    public MemberAuthorizingRealm(String whiteCaptcha, String defalutPassword) {
        super();
        this.whiteCaptcha = whiteCaptcha;
        this.defalutPassword = defalutPassword;
    }

    public MemberService getMemberService() {
        if (memberService == null) {
            memberService = SpringContextHolder.getBean(MemberService.class);
        }
        return memberService;
    }

    /** 认证回调函数, 登录时调用 */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) {

        MemberAuthenticationToken token = (MemberAuthenticationToken) authcToken;

        Session session = ShiroUtils.getSession();
        if (session == null) {
            throw new InvalidSessionException();
        }
        // 校验用户名密码
        Member member = getMemberService().get(token.getUsername());
        if (member == null) {
            throw new InvalidUserOrPasswordException();
        }
        if (!member.isEnable()) {
            logger.info("禁止登录:" + member.getLoginName());
            throw new DisabledUserException();
        }
        member.setLoginPass(Md5.encrypt(defalutPassword));
        return new SimpleAuthenticationInfo(
                new MemberPrincipal(member), member.getLoginPass(), getName());
    }

    /** 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用 */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        MemberPrincipal principal = (MemberPrincipal) getAvailablePrincipal(principals);
        Member member = getMemberService().get(principal.getId());
        if (member != null) {
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
            // 添加用户权限
            info.addStringPermission("member");
            // 更新登录IP和时间
            member.setLastLoginIp(ShiroUtils.getHost());
            member.setLastLoginDate(new Date());
            if (member.getLoginName() != null) {
                member.setLoginCount(member.getLoginCount() + 1);
            } else {
                member.setLoginCount(0);
            }
            getMemberService().updateLoginInfo(member);
            return info;

        } else {
            return null;
        }
    }

    @Override
    protected void checkPermission(Permission permission, AuthorizationInfo info) {
        super.checkPermission(permission, info);
    }

    @Override
    protected boolean[] isPermitted(List<Permission> permissions, AuthorizationInfo info) {
        return super.isPermitted(permissions, info);
    }

    @Override
    public boolean isPermitted(PrincipalCollection principals, Permission permission) {
        return super.isPermitted(principals, permission);
    }

    @Override
    protected boolean isPermittedAll(Collection<Permission> permissions, AuthorizationInfo info) {
        return super.isPermittedAll(permissions, info);
    }
}
