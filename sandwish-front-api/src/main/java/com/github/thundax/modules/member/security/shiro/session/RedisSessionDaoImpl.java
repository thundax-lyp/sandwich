package com.github.thundax.modules.member.security.shiro.session;

import com.google.common.collect.Sets;
import com.github.thundax.common.config.Global;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.web.Servlets;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 系统安全认证实现类
 *
 * @author wdit
 */
public class RedisSessionDaoImpl extends EnterpriseCacheSessionDAO implements SessionDAO {

    private static final String CACHE_PREFIX = "interaction-shiro.";

    private RedisTemplate<String, Object> redisTemplate;
    private long expireSeconds;

    public RedisSessionDaoImpl() {
        super();
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;

        RedisSerializer<String> keySerializer = new StringRedisSerializer();
        this.redisTemplate.setKeySerializer(keySerializer);
        this.redisTemplate.setHashKeySerializer(keySerializer);

        RedisSerializer<Object> valueSerializer = new JdkSerializationRedisSerializer();
        this.redisTemplate.setValueSerializer(valueSerializer);
        this.redisTemplate.setHashValueSerializer(valueSerializer);
    }

    public void setExpireSeconds(long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    @Override
    protected void doUpdate(Session session) {
        if (session == null || session.getId() == null) {
            return;
        }

        HttpServletRequest request = Servlets.getRequest();
        if (request != null) {
            String uri = request.getServletPath();
            // 如果是静态文件，则不更新SESSION
            if (Servlets.isStaticFile(uri)) {
                return;
            }
            // 手动控制不更新SESSION
            String updateSession = request.getParameter("updateSession");
            if (Global.FALSE.equals(updateSession) || Global.NO.equals(updateSession)) {
                return;
            }
        }

        String cacheKey = createCacheKey(session.getId());
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        valueOps.set(cacheKey, session);
        redisTemplate.expire(cacheKey, expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    protected void doDelete(Session session) {
        if (session == null || session.getId() == null) {
            return;
        }

        String cacheKey = createCacheKey(session.getId());
        redisTemplate.delete(cacheKey);
    }

    @Override
    protected Serializable doCreate(Session session) {
        HttpServletRequest request = Servlets.getRequest();
        if (request != null) {
            String uri = request.getServletPath();
            // 如果是静态文件，则不创建SESSION
            if (Servlets.isStaticFile(uri)) {
                return null;
            }
        }
        super.doCreate(session);

        return session.getId();
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        String cacheKey = createCacheKey(sessionId);
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        Object value = valueOps.get(cacheKey);
        return (Session) value;
    }

    @Override
    public Session readSession(Serializable sessionId) throws UnknownSessionException {
        try {
            Session s = null;
            HttpServletRequest request = Servlets.getRequest();
            if (request != null) {
                s = (Session) request.getAttribute("session_" + sessionId);
            }
            if (s != null) {
                return s;
            }

            Session session = super.readSession(sessionId);

            if (request != null && session != null) {
                request.setAttribute("session_" + sessionId, session);
            }

            return session;

        } catch (UnknownSessionException e) {
            return null;
        }
    }

    /**
     * 获取活动会话
     *
     * @param includeLeave 是否包括离线（最后访问时间大于3分钟为离线会话）
     */
    @Override
    public Collection<Session> getActiveSessions(boolean includeLeave) {
        return getActiveSessions(includeLeave, null, null);
    }

    /**
     * 获取活动会话
     *
     * @param includeLeave  是否包括离线（最后访问时间大于3分钟为离线会话）
     * @param principal     根据登录者对象获取活动会话
     * @param filterSession 不为空，则过滤掉（不包含）这个会话。
     */
    @Override
    public Collection<Session> getActiveSessions(boolean includeLeave,
                                                 Object principal,
                                                 Session filterSession) {
        // 如果包括离线，并无登录者条件。
        if (includeLeave && principal == null) {
            return getActiveSessions();
        }
        Set<Session> sessions = Sets.newHashSet();
        for (Session session : getActiveSessions()) {
            boolean isActiveSession = false;
            // 符合登陆者条件。
            if (principal != null) {
                PrincipalCollection pc = (PrincipalCollection) session
                        .getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
                if (principal.toString().equals(
                        pc != null ? pc.getPrimaryPrincipal().toString() : StringUtils.EMPTY)) {
                    isActiveSession = true;
                }
            }
            // 过滤掉的SESSION
            if (filterSession != null && filterSession.getId().equals(session.getId())) {
                isActiveSession = false;
            }
            if (isActiveSession) {
                sessions.add(session);
            }
        }
        return sessions;
    }

    private String createCacheKey(Serializable sessionId) {
        return CACHE_PREFIX + sessionId;
    }
}
