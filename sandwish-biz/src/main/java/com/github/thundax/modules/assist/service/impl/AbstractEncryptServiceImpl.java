package com.github.thundax.modules.assist.service.impl;

import com.github.thundax.modules.assist.service.EncryptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEncryptServiceImpl implements EncryptService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
}
