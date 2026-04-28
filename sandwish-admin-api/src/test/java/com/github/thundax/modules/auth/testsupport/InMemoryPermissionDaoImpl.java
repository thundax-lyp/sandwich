package com.github.thundax.modules.auth.testsupport;

import com.github.thundax.modules.auth.dao.PermissionDao;
import com.github.thundax.modules.auth.entity.PermissionSession;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("test")
@Repository
public class InMemoryPermissionDaoImpl implements PermissionDao {

    private final Map<String, PermissionSession> sessions = new LinkedHashMap<>();
    private final Map<String, Integer> expiredSeconds = new LinkedHashMap<>();
    private int touchCount;
    private int deleteAllCount;

    @Override
    public PermissionSession getByToken(String token) {
        return sessions.get(token);
    }

    @Override
    public void insert(PermissionSession session, int expiredSeconds) {
        sessions.put(session.getToken(), session);
        this.expiredSeconds.put(session.getToken(), expiredSeconds);
    }

    @Override
    public void touch(String token, int expiredSeconds) {
        if (sessions.containsKey(token)) {
            touchCount++;
            this.expiredSeconds.put(token, expiredSeconds);
        }
    }

    @Override
    public void deleteByToken(String token) {
        sessions.remove(token);
        expiredSeconds.remove(token);
    }

    @Override
    public void deleteAll() {
        deleteAllCount++;
        sessions.clear();
        expiredSeconds.clear();
    }

    public int getTouchCount() {
        return touchCount;
    }

    public int getDeleteAllCount() {
        return deleteAllCount;
    }
}
