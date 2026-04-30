package com.github.thundax.modules.assist.entity;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.assist.entity.base.BaseAsyncTask;
import com.github.thundax.modules.sys.entity.User;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public class AsyncTask extends BaseAsyncTask {
    @Override
    @NonNull
    public Integer getExpiredSeconds() {
        Integer expiredSeconds = super.getExpiredSeconds();
        return expiredSeconds != null ? expiredSeconds : DEFAULT_EXPIRED_SECONDS;
    }

    public boolean isPrivate() {
        return Boolean.TRUE.equals(getPrivate());
    }

    public boolean isActive() {
        return StringUtils.equals(STATUS_ACTIVE, getStatus());
    }

    public boolean isSuspended() {
        return StringUtils.equals(STATUS_SUSPENDED, getStatus());
    }

    public boolean isSuccess() {
        return StringUtils.equals(STATUS_SUCCESS, getStatus());
    }

    public boolean isBelongTo(User user) {
        return user != null && Objects.equals(EntityIdCodec.toValue(user.getId()), getCreateUserId());
    }
}
