package com.github.thundax.modules.member.security.shiro.session;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author wdit */
public class ShiroSessionListener implements SessionListener {

    private static final Logger logger = LoggerFactory.getLogger(ShiroSessionListener.class);

    private final MemberSessionDao sessionDao;

    public ShiroSessionListener(MemberSessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    @Override
    public void onStart(Session session) {
        // 会话创建时触发
        // logger.info("SessionListener session {} 被创建", session.getId());
    }

    @Override
    public void onStop(Session session) {
        sessionDao.delete(session);
        // 会话被停止时触发
        // logger.info("ShiroSessionListener session {} 被销毁", session.getId());
    }

    @Override
    public void onExpiration(Session session) {
        sessionDao.delete(session);
        // 会话过期时触发
        // logger.info("ShiroSessionListener session {} 过期", session.getId());
    }
}
