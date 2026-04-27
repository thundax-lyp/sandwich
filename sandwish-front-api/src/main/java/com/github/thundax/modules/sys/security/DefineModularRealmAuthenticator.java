package com.github.thundax.modules.sys.security;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.pam.AuthenticationStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.realm.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 多realm登陆
 *
 * @author wdit
 */
public class DefineModularRealmAuthenticator extends ModularRealmAuthenticator {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<Class<?>, Object> defineRealms;

    @Override
    public AuthenticationStrategy getAuthenticationStrategy() {
        return super.getAuthenticationStrategy();
    }

    @Override
    protected AuthenticationInfo doMultiRealmAuthentication(
            Collection<Realm> realms, AuthenticationToken token) {
        return super.doMultiRealmAuthentication(realms, token);
    }

    @Override
    protected AuthenticationInfo doSingleRealmAuthentication(
            Realm realm, AuthenticationToken token) {
        if (!realm.supports(token)) {
            String msg =
                    "Realm [" + realm + "] does not support authentication token [" + token + "]";
            throw new UnsupportedTokenException(msg);
        }
        AuthenticationInfo info = null;
        try {
            info = realm.getAuthenticationInfo(token);
            if (info == null) {
                String msg =
                        "Realm ["
                                + realm
                                + "] was unable to find account data for the "
                                + "submitted AuthenticationToken ["
                                + token
                                + "].";
                throw new UnknownAccountException(msg);
            }
        } catch (IncorrectCredentialsException e) {
            throw e;
        } catch (UnknownAccountException e) {
            throw e;
        } catch (Throwable throwable) {
            if (log.isDebugEnabled()) {
                String msg =
                        "Realm ["
                                + realm
                                + "] threw an exception during a multi-realm authentication attempt:";
                log.debug(msg, throwable);
            }
        }

        return info;
    }

    /** 判断Realm是不是null */
    @Override
    protected void assertRealmsConfigured() throws IllegalStateException {
        if (this.defineRealms == null || this.defineRealms.size() <= 0) {
            String msg =
                    "Configuration error:  No realms have been configured!  One or more realms must be "
                            + "present to execute an authentication attempt.";
            throw new IllegalStateException(msg);
        }
    }

    /** 这个方法比较重要,用来判断此次调用是前台还是后台 */
    @Override
    protected AuthenticationInfo doAuthenticate(AuthenticationToken token)
            throws AuthenticationException {
        assertRealmsConfigured();
        if (defineRealms.containsKey(token.getClass())) {
            Realm realm = (Realm) this.defineRealms.get(token.getClass());
            return doSingleRealmAuthentication(realm, token);
        } else {
            return null;
        }
    }

    public Collection<Object> getDefineRealms() {
        return this.defineRealms.values();
    }

    public void setDefineRealms(Collection<Object> defineRealms) {
        this.defineRealms = Maps.newHashMap();
        for (Object o : defineRealms) {
            if (!(o instanceof AcceptTokenType)) {
                log.warn("realm must be AcceptTokenType");
            } else {
                this.defineRealms.put(((AcceptTokenType) o).getAcceptTokenType(), o);
            }
        }
    }
}
