package com.github.thundax.modules.sys.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.thundax.modules.sys.entity.base.BaseUserEncrypt;

/**
 * 用户重要信息加密
 *
 * @author wdit
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEncrypt extends BaseUserEncrypt {

    public static final String BEAN_NAME = "UserEncrypt";

    public UserEncrypt() {
        super();
    }

    public UserEncrypt(String id) {
        super(id);
    }

    @Override
    protected void initialize() {
        super.initialize();
    }
}
