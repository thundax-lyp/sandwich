package com.github.thundax.modules.auth.security.service;

import com.github.thundax.modules.auth.security.subject.Subject;

/**
 * @author wdit
 */
public interface SubjectService {

    /**
     * 获取 subject
     *
     * @param userId userId
     * @return Subject
     */
    Subject getSubject(String userId);

    /**
     * 释放 subject
     *
     * @param userId userId
     */
    void releaseSubject(String userId);

    /**
     * 全部重新加载
     */
    void reloadAll();

}
