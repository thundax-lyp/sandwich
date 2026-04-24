package com.github.thundax.modules.assist.service.impl;

import com.github.thundax.common.utils.EncryptUtils;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.assist.service.EncryptService;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.utils.LogServiceHolder;
import com.github.thundax.modules.sys.utils.MenuServiceHolder;
import com.github.thundax.modules.sys.utils.RoleServiceHolder;
import com.github.thundax.modules.sys.utils.UserServiceHolder;

/**
 * 国密服务默认实现
 *
 * @author wdit
 */
public class DefaultEncryptServiceImpl implements EncryptService {

    /**
     * 加密
     *
     * @param value 被加密数据
     * @return 加密结果
     */
    @Override
    public String encrypt(String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }

        return EncryptUtils.sm4Encrypt(value);
    }

    /**
     * 解密
     *
     * @param encryptedValue 被解密数据
     * @return 解密结果
     */
    @Override
    public String decrypt(String encryptedValue) {
        if (StringUtils.isEmpty(encryptedValue)) {
            return "";
        }
        return EncryptUtils.sm4Decrypt(encryptedValue);
    }
}
