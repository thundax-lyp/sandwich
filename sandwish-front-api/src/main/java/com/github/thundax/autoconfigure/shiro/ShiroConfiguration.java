package com.github.thundax.autoconfigure.shiro;

import com.github.thundax.modules.member.security.MemberAuthenticationFilter;
import com.github.thundax.modules.member.security.MemberAuthorizingRealm;
import com.github.thundax.modules.member.security.shiro.cache.RedisCacheManager;
import com.github.thundax.modules.member.security.shiro.cache.ShiroJetCacheStore;
import com.github.thundax.modules.member.security.shiro.session.MemberSessionDao;
import com.github.thundax.modules.member.security.shiro.session.RedisSessionDaoImpl;
import com.github.thundax.modules.member.security.shiro.session.ShiroSessionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/** @author wdit */
@Configuration
public class ShiroConfiguration {

    @Value("#{ @environment['shiro.loginUrl'] ?: '/login' }")
    protected String loginUrl;

    @Value("#{ @environment['shiro.successUrl'] ?: '/index' }")
    private String successUrl;

    @Value("#{ @environment['vltava.whiteCaptcha'] ?: '' }")
    private String whiteCapture;

    @Value("${vltava.defalut-password:12312321321}")
    private String defalutPassword;

    private final ShiroProperties properties;

    public ShiroConfiguration(ShiroProperties properties) {
        this.properties = properties;
    }

    @Bean
    public AuthorizingRealm authorizingRealm() {
        MemberAuthorizingRealm realm = new MemberAuthorizingRealm(whiteCapture, defalutPassword);
        realm.setCredentialsMatcher(new HashedCredentialsMatcher("MD5"));
        return realm;
    }

    @Bean
    public MemberSessionDao sessionDAO() {
        RedisSessionDaoImpl sessionDao = new RedisSessionDaoImpl();
        sessionDao.setActiveSessionsCacheName("interaction-shiro-activeSessionCache");
        sessionDao.setExpireSeconds(1200);
        return sessionDao;
    }

    @Bean
    public RedisCacheManager redisCacheManager(ShiroJetCacheStore cacheStore) {
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setCacheStore(cacheStore);
        redisCacheManager.setExpireSeconds(1200);
        return redisCacheManager;
    }

    @Bean
    public DefaultWebSessionManager sessionManager(MemberSessionDao sessionDAO) {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();

        sessionManager.setSessionDAO(sessionDAO);
        sessionManager.setGlobalSessionTimeout(3600000);
        sessionManager.setSessionValidationInterval(3600000);
        sessionManager.setSessionValidationSchedulerEnabled(true);
        sessionManager.setDeleteInvalidSessions(true);

        SimpleCookie cookie = new SimpleCookie("interaction.sid");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(-1);
        sessionManager.setSessionIdCookie(cookie);
        sessionManager.setSessionIdCookieEnabled(true);

        sessionManager.setSessionIdUrlRewritingEnabled(false);

        List<SessionListener> sessionListenerList = new ArrayList<>();
        sessionListenerList.add(new ShiroSessionListener(sessionDAO));
        sessionManager.setSessionListeners(sessionListenerList);

        return sessionManager;
    }

    @Bean
    public DefaultWebSecurityManager securityManager(
            RedisCacheManager cacheManager, DefaultWebSessionManager sessionManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();

        securityManager.setRealm(authorizingRealm());
        securityManager.setSessionManager(sessionManager);
        securityManager.setCacheManager(cacheManager);

        return securityManager;
    }

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();

        if (properties.getChainDefinition() == null || properties.getChainDefinition().isEmpty()) {
            chainDefinition.addPathDefinition("/static/**", "anon");
            chainDefinition.addPathDefinition("/servlet/**", "anon");
            chainDefinition.addPathDefinition("/auth/register", "anon");
            chainDefinition.addPathDefinition("/auth/login", "authc");
            chainDefinition.addPathDefinition("/auth/logout", "logout");
            chainDefinition.addPathDefinition("/member/**", "authc");

        } else {
            chainDefinition.addPathDefinitions(properties.getChainDefinition());
        }

        return chainDefinition;
    }

    @Bean
    public Map<String, Filter> filterMap() {
        Map<String, Filter> filterMap = new HashMap<>();

        MemberAuthenticationFilter authcFilter = new MemberAuthenticationFilter();
        authcFilter.setLoginUrl(loginUrl);
        authcFilter.setSuccessUrl(successUrl);
        filterMap.put("authc", authcFilter);

        LogoutFilter logoutFilter = new LogoutFilter();
        logoutFilter.setRedirectUrl(loginUrl);
        filterMap.put("logout", logoutFilter);

        return filterMap;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(
            WebSecurityManager securityManager,
            ShiroFilterChainDefinition shiroFilterChainDefinition) {
        ShiroFilterFactoryBean filterFactoryBean = new ShiroFilterFactoryBean();

        filterFactoryBean.setLoginUrl(loginUrl);
        filterFactoryBean.setSuccessUrl(successUrl);

        filterFactoryBean.setSecurityManager(securityManager);
        filterFactoryBean.setFilterChainDefinitionMap(
                shiroFilterChainDefinition.getFilterChainMap());
        filterFactoryBean.setFilters(filterMap());

        return filterFactoryBean;
    }

    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator =
                new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }
}
