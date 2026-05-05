package com.github.thundax.modules.sys.entity.valueobject;

import java.util.Objects;

/**
 * 后台用户等级。
 */
public final class UserRank {

    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 9;

    private final int value;

    private UserRank(int value) {
        this.value = value;
    }

    public static UserRank of(Integer value) {
        if (value == null || value < MIN_VALUE) {
            return new UserRank(MIN_VALUE);
        }
        if (value >= MAX_VALUE) {
            return new UserRank(MAX_VALUE);
        }
        return new UserRank(value);
    }

    public int value() {
        return value;
    }

    public boolean canAccess(Integer targetRank) {
        return value >= of(targetRank).value();
    }

    public boolean canAccess(UserRank targetRank) {
        return value >= (targetRank == null ? MIN_VALUE : targetRank.value());
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof UserRank)) {
            return false;
        }
        UserRank userRank = (UserRank) that;
        return value == userRank.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
