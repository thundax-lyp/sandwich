package com.github.thundax.modules.member.entity.valueobject;

import com.github.thundax.common.id.BaseLongId;

public final class MemberId extends BaseLongId {

    private MemberId(Long value) {
        super(value);
    }

    public static MemberId of(Long value) {
        return new MemberId(value);
    }

    public static MemberId ofNullable(Long value) {
        return value == null ? null : of(value);
    }
}
