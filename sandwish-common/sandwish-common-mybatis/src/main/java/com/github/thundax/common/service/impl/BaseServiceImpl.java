package com.github.thundax.common.service.impl;

import com.github.thundax.common.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/** @author thundax */
@Transactional(readOnly = true)
public class BaseServiceImpl implements BaseService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
}
