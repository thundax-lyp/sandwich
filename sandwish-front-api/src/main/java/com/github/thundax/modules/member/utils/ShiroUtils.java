package com.github.thundax.modules.member.utils;

import com.github.thundax.modules.member.security.MemberPrincipal;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import java.util.Iterator;

/**
 * @author wdit
 */
public class ShiroUtils {

    /**
     * 获取当前登录者对象
     */
    public static MemberPrincipal getPrincipal() {
        try {
            Subject subject = SecurityUtils.getSubject();
            Object principal = subject.getPrincipal();
            if (principal instanceof MemberPrincipal) {
                return (MemberPrincipal) principal;
            }
        } catch (UnavailableSecurityManagerException | InvalidSessionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取授权主要对象
     */
    public static Subject getSubject() {
        return SecurityUtils.getSubject();
    }


    public static boolean hasAnyPermission(String... permissions) {
        Subject subject = getSubject();
        if (subject != null) {
            for (String permission : permissions) {
                if (subject.isPermitted(permission.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Session getSession() {
        try {
            Subject subject = SecurityUtils.getSubject();
            Session session = subject.getSession(false);

            return session == null ? subject.getSession() : session;

        } catch (InvalidSessionException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String getHost() {
        Session session = getSession();
        return session == null ? null : session.getHost();
    }

    public static void reloadAuthorizing() {
        Subject subject = SecurityUtils.getSubject();

        RealmSecurityManager securityManager = (RealmSecurityManager) SecurityUtils.getSecurityManager();

        Iterator<Realm> iterator = securityManager.getRealms().iterator();
        for (Realm realm = iterator.next(); iterator.hasNext(); ) {
            if (realm instanceof AuthorizingRealm) {
                ((AuthorizingRealm) realm).getAuthorizationCache().remove(subject.getPrincipals());
            }
        }
    }

    public static Object getSessionCache(String key) {
        return getSessionCache(key, null);
    }

    public static Object getSessionCache(String key, Object defaultValue) {
        Session session = getSession();
        if (session == null) {
            return null;
        }
        Object obj = session.getAttribute(key);
        return obj != null ? obj : defaultValue;
    }

    public static void putSessionCache(String key, Object value) {
        Session session = getSession();
        if (session != null) {
            session.setAttribute(key, value);
        }
    }

    public static void removeSessionCache(String key) {
        Session session = getSession();
        if (session != null) {
            session.removeAttribute(key);
        }
    }

}
