package com.github.thundax.modules.auth.testsupport;

import com.github.thundax.modules.auth.dao.AccessTokenDao;
import com.github.thundax.modules.auth.entity.AccessToken;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("test")
@Repository
public class InMemoryAccessTokenDaoImpl implements AccessTokenDao {

    private final Map<String, String> tokenUserIds = new LinkedHashMap<>();
    private final Map<String, AccessToken> userTokens = new LinkedHashMap<>();
    private int activeCount;

    @Override
    public int getOnlineCount() {
        return tokenUserIds.size();
    }

    @Override
    public String getUidByToken(String token) {
        return tokenUserIds.get(token);
    }

    @Override
    public AccessToken getByUserId(String userId) {
        return userTokens.get(userId);
    }

    @Override
    public void insert(AccessToken accessToken) {
        tokenUserIds.put(accessToken.getToken(), accessToken.getUserId());
        userTokens.put(accessToken.getUserId(), accessToken);
    }

    @Override
    public void active(AccessToken accessToken) {
        activeCount++;
    }

    @Override
    public void delete(AccessToken accessToken) {
        tokenUserIds.remove(accessToken.getToken());
        userTokens.remove(accessToken.getUserId());
    }

    public int getActiveCount() {
        return activeCount;
    }
}
