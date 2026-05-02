package com.github.thundax.modules.member.service;

public interface MemberAccessService {

    String getCurrentMemberId();

    Object getSessionCache(String name);

    void setSessionCache(String name, Object value);
}
