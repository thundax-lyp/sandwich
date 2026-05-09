package com.github.thundax.modules.member.entity.valueobject;

import java.util.List;
import java.util.stream.Collectors;

public final class MemberIdCodec {

    private MemberIdCodec() {}

    public static MemberId toDomain(Long value) {
        return MemberId.ofNullable(value);
    }

    public static Long toValue(MemberId id) {
        return id == null ? null : id.value();
    }

    public static String toStringValue(MemberId id) {
        return id == null ? null : String.valueOf(id.value());
    }

    public static List<MemberId> toDomains(List<Long> values) {
        return values == null
                ? null
                : values.stream().map(MemberIdCodec::toDomain).collect(Collectors.toList());
    }

    public static List<Long> toValues(List<MemberId> ids) {
        return ids == null ? null : ids.stream().map(MemberIdCodec::toValue).collect(Collectors.toList());
    }
}
