package com.github.thundax.modules.member.service;

public interface MemberAccessSupport {

    String getCurrentMemberId();

    Object getSessionCache(String name);

    void setSessionCache(String name, Object value);
}
